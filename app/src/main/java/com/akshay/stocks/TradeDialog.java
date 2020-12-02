package com.akshay.stocks;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

import java.security.AccessController;

import static com.akshay.stocks.MainActivity.SHARED_PREFS;

public class TradeDialog extends AppCompatDialogFragment {
    private EditText editShares;
    private TradeDialogListener listener;
    private static final String TAG = "TradeDialog";
    private StockItemDetail mStockItem;
    private TextView textViewTotalCost;
    private TextView textViewTradeDialogHeader;
    private float availableAmount;
    private int existingShares;

    public TradeDialog(StockItemDetail stockItem, float availableAmount, int existingShares) {
        mStockItem = stockItem;
        this.availableAmount = availableAmount;
        this.existingShares = existingShares;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.layout_trade_dialog, null);

        builder.setView(view);

        textViewTradeDialogHeader = view.findViewById(R.id.text_view_trade_dialog_header);
        TextView textViewTradeDialogAvailable = view.findViewById(R.id.text_view_available);
        textViewTradeDialogHeader.setText("Trade " + mStockItem.getTicker() + " shares");
        textViewTradeDialogAvailable.setText("$" + availableAmount + " available to buy " + mStockItem.getTicker());

        editShares = (EditText) view.findViewById(R.id.edit_shares);

        editShares.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void afterTextChanged(Editable editable) {
                int shares;
                if(editable.toString().equals("")){
                    shares = 0;
                } else {
                    shares = Integer.parseInt(editable.toString());
                }
                textViewTotalCost = view.findViewById(R.id.text_view_total_cost);
                textViewTotalCost.setText(shares + " x " + "$" + mStockItem.getLast() + "/share = $" + shares*mStockItem.getLast());
            }
        });


        Button buy = (Button) view.findViewById(R.id.buy_button);
        Button sell = (Button) view.findViewById(R.id.sell_button);

        buy.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String shares = editShares.getText().toString();
                if(validateBuy(shares)){
                    listener.tradeStocks(true, shares);
                }
            }
        });

        sell.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String shares = editShares.getText().toString();
                if(validateSell(shares)){
                    listener.tradeStocks(false, shares);
                }
            }
        });
        return builder.create();
    }

    private boolean validateBuy(String shares) {
        int shares_int = Integer.parseInt(shares);

        if(shares_int > 0){
            if(mStockItem.getLast()*shares_int <= availableAmount){
                return true;
            } else {
                Toast.makeText(getActivity(), "Not enough money to buy", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(getActivity(), "Cannot buy less than 0 shares", Toast.LENGTH_LONG).show();
        }
        return false;
    }

    private boolean validateSell(String shares) {
        int shares_int = Integer.parseInt(shares);

        if(shares_int > 0){
            if(shares_int <= existingShares){
                return true;
            } else {
                Toast.makeText(getActivity(), "Not enough shares to sell", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(getActivity(), "Cannot sell less than 0 shares", Toast.LENGTH_LONG).show();
        }
        return false;
    }


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        try {
            listener = (TradeDialogListener) context;
        } catch (ClassCastException e) {
           throw new ClassCastException((context.toString()) + "must implement TradeDialogListener");
        }
    }

    public interface TradeDialogListener{
        void tradeStocks(boolean option, String shares);
    }

}
