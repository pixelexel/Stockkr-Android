package com.akshay.stocks;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.BreakIterator;

final class StockViewHolder extends RecyclerView.ViewHolder {

    final View rootView;
    final TextView mTextViewTicker;
    final TextView mTextViewName;
    final TextView mTextViewChange;
    final TextView mTextViewLast;
    final TextView netWorth;
    final TextView worth;
    final ImageView imageViewArrow;
    final ImageView carrot;




    StockViewHolder(@NonNull View view) {
        super(view);

        rootView = view;
        mTextViewTicker = view.findViewById(R.id.text_view_ticker);
        mTextViewName = view.findViewById(R.id.text_view_name);
        mTextViewChange = view.findViewById(R.id.text_view_change);
        mTextViewLast = view.findViewById(R.id.text_view_last);
        imageViewArrow = view.findViewById(R.id.image_view_arrow);
        netWorth = view.findViewById(R.id.text_view_net_worth);
        worth = view.findViewById(R.id.text_view_worth);
        carrot = view.findViewById(R.id.image_view_carrot);
    }
}
