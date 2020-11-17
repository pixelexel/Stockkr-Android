package com.akshay.stocks;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter;
import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;

public class MainActivity extends AppCompatActivity implements StocksSection.ClickListener {

    private String SERVER = "http://8tsathna.us-east-1.elasticbeanstalk.com";
    private static final String TAG = "MainActivity";
    public static final String EXTRA_TICKER = "ticker";
    private List<StockItem> mPortfolioList;
    private List<StockItem> mWatchlist;

    private RecyclerView mRecyclerView;
    private RequestQueue mRequestQueue;
    private SectionedRecyclerViewAdapter sectionAdapter;
    private StocksSection portfolioSection;
    private StocksSection watchlistSection;

    private JSONArray watchlistTickers;
    private JSONArray portfolioTickers;

    SharedPreferences sharedpreferences;
    public static final String SHARED_PREFS = "sharedPrefs";
    public static final String portfolio = "portfolio";
    public static final String watchlist = "watchlist";

    //private string

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //TEMP
        String[] temp_list = {"NVDA", "GOOGL", "AAPL", "MSFT"};
        JSONArray json_temp_list = null;
        try {
            json_temp_list = new JSONArray(temp_list);
            sharedpreferences = getSharedPreferences(SHARED_PREFS,
                    MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedpreferences.edit();
            editor.putString(watchlist, json_temp_list.toString());
            editor.putString(portfolio, json_temp_list.toString());
            editor.commit();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        sectionAdapter = new SectionedRecyclerViewAdapter();
        mRecyclerView = findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(mRecyclerView);
        mPortfolioList = new ArrayList<>();
        mWatchlist = new ArrayList<>();
        mRequestQueue = Volley.newRequestQueue(this);

        loadStockList();
        getStockList();
    }

    public void loadStockList() {
        sharedpreferences = getSharedPreferences(SHARED_PREFS,
                MODE_PRIVATE);
        try {
            watchlistTickers = new JSONArray(sharedpreferences.getString(watchlist, ""));
            portfolioTickers = new JSONArray(sharedpreferences.getString(portfolio, ""));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void removeFromWatchlist(int position) {
        String ticker = mWatchlist.get(position).getTicker();
        mWatchlist.remove(position);
        sharedpreferences = getSharedPreferences(SHARED_PREFS,
                MODE_PRIVATE);
        for(int i=0; i<watchlistTickers.length();i++){

            try {
                if(ticker.equals(watchlistTickers.get(i).toString())){
                    watchlistTickers.remove(i);
                    break;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putString(watchlist, watchlistTickers.toString());
        editor.commit();
    }

    private void getStockList() {
        //CHECK FOR EMPTY tickers
        if (portfolioTickers.length() == 0) {
            portfolioSection = new StocksSection("Portfolio", mPortfolioList, MainActivity.this::onItemRootViewClicked);
            sectionAdapter.addSection(portfolioSection);
            getWatchList();
            return;
        }

        String portfolioListParams = "";
        for (int i = 0; i < portfolioTickers.length(); i++) {
            try {
                portfolioListParams = portfolioListParams + "&ticker=" + (String) portfolioTickers.get(i);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        String portfolioListUrl = SERVER + "/api/stocklist?" + portfolioListParams.substring(1);

        JsonObjectRequest request1 = new JsonObjectRequest(Request.Method.GET, portfolioListUrl, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray jsonArray = response.getJSONArray("stocks");
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject stock = jsonArray.getJSONObject(i);

                        String name = stock.getString("name");
                        String ticker = stock.getString("ticker");

                        mPortfolioList.add(new StockItem(name, ticker));
                    }
                    portfolioSection = new StocksSection("Portfolio", mPortfolioList, MainActivity.this::onItemRootViewClicked);
                    sectionAdapter.addSection(portfolioSection);
                    getWatchList();

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
    }

    public void getWatchList(){
        //CHECK FOR EMPTY tickers
        if (watchlistTickers.length() == 0) {
            watchlistSection = new StocksSection("Watchlist", mWatchlist, MainActivity.this::onItemRootViewClicked);
            sectionAdapter.addSection(watchlistSection);
            mRecyclerView.setAdapter(sectionAdapter);
            return;
        }
        String watchListParams = "";
        for (int i = 0; i < watchlistTickers.length(); i++) {
            try {
                watchListParams = watchListParams + "&ticker=" + (String) watchlistTickers.get(i);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        String watchListUrl = SERVER + "/api/stocklist?" + watchListParams.substring(1);

        JsonObjectRequest request2 = new JsonObjectRequest(Request.Method.GET, watchListUrl, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray jsonArray = response.getJSONArray("stocks");

                    for (int i = 0; i < jsonArray.length(); i++) {
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
        mRequestQueue.add(request2);
    }


    @Override
    public void onItemRootViewClicked(@NonNull StocksSection section, int itemAdapterPosition) {
        Intent detailIntent = new Intent(this, DetailActivity.class);
        StockItem clickedItem = section.list.get(sectionAdapter.getPositionInSection(itemAdapterPosition));
        detailIntent.putExtra(EXTRA_TICKER, clickedItem.getTicker());

        startActivity(detailIntent);
    }

    ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            try {
                if (sectionAdapter.getSectionForPosition(viewHolder.getAdapterPosition()) == watchlistSection) {
                    removeFromWatchlist(sectionAdapter.getPositionInSection(viewHolder.getAdapterPosition()));
                }
            } catch (IllegalArgumentException e){
                e.printStackTrace();
            }
            sectionAdapter.notifyDataSetChanged();
        }

        @Override
        public float getSwipeThreshold(@NonNull RecyclerView.ViewHolder viewHolder) {
            return 0.5f;
        }

        @Override
        public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
            new RecyclerViewSwipeDecorator.Builder(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                    .addBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.red))
                    .addActionIcon(R.drawable.ic_baseline_delete_24)
                    .create()
                    .decorate();

            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
        }
    };
}