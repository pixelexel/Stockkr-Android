package com.akshay.stocks;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class ListAdapter extends RecyclerView.Adapter<ListAdapter.ListViewHolder>{

    private Context mContext;
    private ArrayList<StockListItem> mStockList;

    public ListAdapter(Context context, ArrayList<StockListItem> stockList){
        mContext = context;
        mStockList = stockList;
    }

    @NonNull
    @Override
    public ListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
       View v = LayoutInflater.from(mContext).inflate(R.layout.stock_list, parent, false );
       return new ListViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ListViewHolder holder, int position) {
        StockListItem currentStock = mStockList.get(position);

        String ticker = currentStock.getTicker();
        String name = currentStock.getName();

        holder.mTextViewTicker.setText(ticker);
        holder.mTextViewName.setText(name);
    }

    @Override
    public int getItemCount() {
        return mStockList.size();
    }


    public class ListViewHolder extends RecyclerView.ViewHolder{
        public TextView mTextViewTicker;
        public TextView mTextViewName;

        public ListViewHolder(@NonNull View itemView) {
            super(itemView);
            mTextViewTicker = itemView.findViewById(R.id.text_view_ticker);
            mTextViewName = itemView.findViewById(R.id.text_view_name);
        }
    }
}
