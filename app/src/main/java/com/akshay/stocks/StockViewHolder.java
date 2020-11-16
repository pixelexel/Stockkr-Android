package com.akshay.stocks;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

final class StockViewHolder extends RecyclerView.ViewHolder {

    final View rootView;
    final TextView mTextViewTicker;
    final TextView mTextViewName;
    private ListAdapter.OnItemClickListener mListener;

    StockViewHolder(@NonNull View view) {
        super(view);

        rootView = view;
        mTextViewTicker = view.findViewById(R.id.text_view_ticker);
        mTextViewName = view.findViewById(R.id.text_view_name);
    }
}
