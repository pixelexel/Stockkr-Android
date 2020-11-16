package com.akshay.stocks;

import android.view.View;


import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import io.github.luizgrp.sectionedrecyclerviewadapter.Section;

import java.util.List;

import io.github.luizgrp.sectionedrecyclerviewadapter.SectionParameters;

final class StocksSection extends Section {

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

        itemHolder.mTextViewTicker.setText(stock.getTicker());
        itemHolder.mTextViewName.setText(stock.getName());

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

//    public interface OnItemClickListener {
//        void onItemClick(int position);
//    }

    interface ClickListener {
        void onItemRootViewClicked(@NonNull final StocksSection section, final int itemAdapterPosition);
    }
}
