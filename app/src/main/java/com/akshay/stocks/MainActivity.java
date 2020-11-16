package com.akshay.stocks;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter;

public class MainActivity extends AppCompatActivity implements StocksSection.ClickListener{

    private static final String TAG = "MainActivity";
    public static final String EXTRA_TICKER = "ticker";
    private List<StockItem> mPortfolioList;
    private List<StockItem> mWatchlist;

    private RecyclerView mRecyclerView;
    private RequestQueue mRequestQueue;
    private SectionedRecyclerViewAdapter sectionAdapter;
    private StocksSection portfolioSection;
    private StocksSection watchlistSection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sectionAdapter = new SectionedRecyclerViewAdapter();
        mRecyclerView = findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        mPortfolioList = new ArrayList<>();
        mWatchlist = new ArrayList<>();
        mRequestQueue = Volley.newRequestQueue(this);

        parseStockList();
    }

    private void parseStockList() {
        String url = "http://8tsathna.us-east-1.elasticbeanstalk.com/api/stocklist";

        JsonObjectRequest request1 = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray jsonArray = response.getJSONArray("stocks");
                    for (int i=0; i< jsonArray.length(); i++){
                        JSONObject stock = jsonArray.getJSONObject(i);

                        String name = stock.getString("name");
                        String ticker = stock.getString("ticker");

                        mPortfolioList.add(new StockItem(name, ticker));
                    }

                    portfolioSection = new StocksSection("Portfolio", mPortfolioList, MainActivity.this::onItemRootViewClicked);
                    sectionAdapter.addSection(portfolioSection);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });

        JsonObjectRequest request2 = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray jsonArray = response.getJSONArray("stocks");

                    for (int i=0; i< jsonArray.length(); i++){
                        JSONObject stock = jsonArray.getJSONObject(i);

                        String name = stock.getString("name");
                        String ticker = stock.getString("ticker");

                        mWatchlist.add(new StockItem(name, ticker));
                    }

                    watchlistSection = new StocksSection("Watchlist", mWatchlist, MainActivity.this::onItemRootViewClicked);
                    sectionAdapter.addSection(watchlistSection);

                    mRecyclerView.setAdapter(sectionAdapter);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });

        mRequestQueue.add(request1);
        mRequestQueue.add(request2);
    }


    @Override
    public void onItemRootViewClicked(@NonNull StocksSection section, int itemAdapterPosition) {
        Intent detailIntent = new Intent(this, DetailActivity.class);
        StockItem clickedItem = section.list.get(sectionAdapter.getPositionInSection(itemAdapterPosition));
        detailIntent.putExtra(EXTRA_TICKER, clickedItem.getTicker());

        startActivity(detailIntent);
    }
}