package com.akshay.stocks;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class ListAdapter extends RecyclerView.Adapter<ListAdapter.ViewHolder> {

    public static final String TAG = "ListAdapter";

    private Context mContext;
    private ArrayList<StockItem> mStockList;
    private OnItemClickListener mListener;

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    public ListAdapter(Context context, ArrayList<StockItem> stockList) {
        mContext = context;
        mStockList = stockList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.stock_list, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Log.d(TAG, "onBindViewHolder: called");
        StockItem currentStock = mStockList.get(position);

        String ticker = currentStock.getTicker();
        String name = currentStock.getName();

        holder.mTextViewTicker.setText(ticker);
        holder.mTextViewName.setText(name);
    }

    @Override
    public int getItemCount() {
        return mStockList.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        public TextView mTextViewTicker;
        public TextView mTextViewName;
        //public RelativeLayout parentLayout;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mTextViewTicker = itemView.findViewById(R.id.text_view_ticker);
            mTextViewName = itemView.findViewById(R.id.text_view_name);
            //parentLayout = itemView.findViewById(R.id.parent_layout);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(mListener != null){
                        int position = getAdapterPosition();
                        if(position != RecyclerView.NO_POSITION){
                            mListener.onItemClick(position);
                        }
                    }
                }
            });
        }
    }
}
