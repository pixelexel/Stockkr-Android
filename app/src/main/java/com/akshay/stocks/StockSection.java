package com.akshay.stocks;

import android.graphics.Color;
import android.view.View;


import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import io.github.luizgrp.sectionedrecyclerviewadapter.Section;

import java.util.List;

import io.github.luizgrp.sectionedrecyclerviewadapter.SectionParameters;

final class StocksSection extends Section{

    final String title;
    final List<StockItem> list;
    final ClickListener clickListener;

    public StocksSection(@NonNull final String title, @NonNull final List<StockItem> list,
                  @NonNull final ClickListener clickListener) {

        super(SectionParameters.builder()
                .itemResourceId(R.layout.stock_list)
                .headerResourceId(R.layout.section_header)
                .build());

        this.title = title;
        this.list = list;
        this.clickListener = clickListener;
    }

    @Override
    public int getContentItemsTotal() {
        return list.size();
    }

    @Override
    public RecyclerView.ViewHolder getItemViewHolder(final View view) {
        return new StockViewHolder(view);
    }

    @Override
    public void onBindItemViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        final StockViewHolder itemHolder = (StockViewHolder) holder;

        final StockItem stock = list.get(position);

//        if(stock.getTicker().equals("worth")){
//            final NetWorthViewHolder netWorthViewHolder = (NetWorthViewHolder) holder;
//        }

        itemHolder.mTextViewTicker.setText(stock.getTicker());
        itemHolder.mTextViewName.setText(stock.getName());
        itemHolder.mTextViewLast.setText(String.valueOf(stock.getLast()));
        itemHolder.mTextViewChange.setText(String.format("%.2f", Math.abs(stock.getChange())));

        if(stock.getChange() > 0){
            itemHolder.imageViewArrow.setImageResource(R.drawable.ic_twotone_trending_up_24);
            itemHolder.mTextViewChange.setTextColor(Color.parseColor("#319C5E"));
        } else {
            itemHolder.imageViewArrow.setImageResource(R.drawable.ic_baseline_trending_down_24);
            itemHolder.mTextViewChange.setTextColor(Color.parseColor("#9B4049"));
        }

        itemHolder.rootView.setOnClickListener(v ->
                clickListener.onItemRootViewClicked(this, itemHolder.getAdapterPosition())
        );
    }

    @Override
    public RecyclerView.ViewHolder getHeaderViewHolder(final View view) {
        return new HeaderViewHolder(view);
    }

    @Override
    public void onBindHeaderViewHolder(final RecyclerView.ViewHolder holder) {
        final HeaderViewHolder headerHolder = (HeaderViewHolder) holder;

        headerHolder.title.setText(title);
    }

    interface ClickListener {
        void onItemRootViewClicked(@NonNull final StocksSection section, final int itemAdapterPosition);
    }
}
