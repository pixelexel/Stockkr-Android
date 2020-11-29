package com.akshay.stocks;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

public class TradeDialog extends AppCompatDialogFragment {
    private EditText editShares;
    private TradeDialogListener listener;
    private static final String TAG = "TradeDialog";
    private StockItem mStockItem;
    private TextView textViewTotalCost;
    private TextView textViewTradeDialogHeader;

    public TradeDialog(StockItem stockItem) {
        mStockItem = stockItem;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.layout_trade_dialog, null);

        builder.setView(view);

        textViewTradeDialogHeader = view.findViewById(R.id.text_view_trade_dialog_header);
        textViewTradeDialogHeader.setText("Trade " + mStockItem.getTicker() + " shares");

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
                listener.tradeStocks(true, shares);
            }
        });

        sell.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String shares = editShares.getText().toString();
                listener.tradeStocks(false, shares);
            }
        });
        return builder.create();
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
