package com.akshay.stocks;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;

import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

public class SuccessDialog extends AppCompatDialogFragment {
    private String mTicker;
    private Integer mShares;
    private boolean mOption;
    private SuccessDialog.SuccessDialogListener listener;

    public SuccessDialog(String ticker, boolean option, Integer shares) {
        mTicker = ticker;
        mOption = option;
        mShares = shares;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.layout_success_dialog, null);

        builder.setView(view);

        TextView textViewSuccessMessage = view.findViewById(R.id.text_view_success_message);
        if(mOption){
            textViewSuccessMessage.setText("You have successfully bought " +  mShares + " of " + mTicker);
        } else {
            textViewSuccessMessage.setText("You have successfully sold " +  mShares + " of " + mTicker);
        }

        Button done = (Button) view.findViewById(R.id.done_button);

        done.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                listener.closeDialog();
            }
        });

        return builder.create();
    }


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        try {
            listener = (SuccessDialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException((context.toString()) + "must implement SuccessDialogListener");
        }
    }

    public interface SuccessDialogListener {
        void closeDialog();
    }

}
