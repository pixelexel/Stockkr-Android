package com.akshay.stocks;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;

public class NetWorthViewHolder {

    final View rootView;
    final TextView mTextViewTicker;
    final TextView mTextViewName;

    NetWorthViewHolder(@NonNull View view) {
        rootView = view;
        mTextViewTicker = view.findViewById(R.id.text_view_ticker);
        mTextViewName = view.findViewById(R.id.text_view_name);
    }
}
