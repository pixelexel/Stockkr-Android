package com.akshay.stocks;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.NewsViewHolder> {
    private Context context;
    private ArrayList<NewsItem> newsList = new ArrayList<NewsItem>() ;
    private onNewsItemClickListener mListener;


    public NewsAdapter(Context context , ArrayList<NewsItem> newsList){
        this.context = context;
        this.newsList = newsList;
    }

    public interface onNewsItemClickListener {
        void onNewsItemClick(int position);
    }

    public void setOnItemClickListener(onNewsItemClickListener listener){
        mListener = listener;

    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) return 1;
        else return 2;
    }


    @Override
    public NewsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == 1) {
            View view =  LayoutInflater.from(context).inflate(R.layout.first_news_item, parent, false);
            return new NewsViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.news_item, parent, false);
            return new NewsViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(NewsViewHolder holder, int position) {
        NewsItem currentItem = newsList.get(position);

        String imageUrl = currentItem.getImageUrl();
        String website = currentItem.getWebsite();
        String title = currentItem.getTitle();
        String time = currentItem.getTime();

        holder.textViewWebsite.setText(website);
        holder.textViewTitle.setText(title);
        holder.textViewTime.setText(time);
        Picasso.with(context).load(imageUrl).fit().centerInside().into(holder.imageView);
    }


    @Override
    public int getItemCount() {
        return newsList.size();
    }

    public class NewsViewHolder extends RecyclerView.ViewHolder {
        public ImageView imageView;
        public TextView textViewWebsite;
        public TextView textViewTitle;
        public TextView textViewTime;

        public NewsViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image_view_news);
            textViewWebsite = itemView.findViewById(R.id.text_view_news_website);
            textViewTitle = itemView.findViewById(R.id.text_view_news_title);
            textViewTime = itemView.findViewById(R.id.text_view_news_time);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(mListener != null){
                        int position = getAdapterPosition();
                        if(position != RecyclerView.NO_POSITION){
                            mListener.onNewsItemClick(position);
                        }
                    }
                }
            });
        }
    }

}