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
import android.view.GestureDetector;

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
    //private ItemTouchHelper mTouchHelper;

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
//        String[] temp_list = {"NVDA", "GOOGL", "AAPL", "MSFT"};
//        JSONArray json_temp_list = null;
//        try {
//            json_temp_list = new JSONArray(temp_list);
//            sharedpreferences = getSharedPreferences(SHARED_PREFS,
//                    MODE_PRIVATE);
//            SharedPreferences.Editor editor = sharedpreferences.edit();
//            editor.putString(watchlist, json_temp_list.toString());
//            editor.putString(portfolio, json_temp_list.toString());
//            editor.commit();
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }

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

    void sortPortfolio() {
        for(int i=0;i<portfolioTickers.length();i++){
            for(int j =0; j<mPortfolioList.size(); j++){
                try {
                    if(mPortfolioList.get(j).getTicker().equals(portfolioTickers.get(i))){
                        StockItem stock = mPortfolioList.get(j);
                        mPortfolioList.remove(stock);
                        mPortfolioList.add(i, stock);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    void sortWatchlist() {
        for(int i=0;i<watchlistTickers.length();i++){
            for(int j =0; j<mWatchlist.size(); j++){
                try {
                    if(mWatchlist.get(j).getTicker().equals(watchlistTickers.get(i))){
                        StockItem stock = mWatchlist.get(j);
                        mWatchlist.remove(stock);
                        mWatchlist.add(i, stock);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void getStockList() {
        //CHECK FOR EMPTY tickers
        Log.d(TAG, "getStockList: " + watchlistTickers);
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
                    sortPortfolio();
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

    public void getWatchList() {
        //CHECK FOR EMPTY tickers
        if (watchlistTickers.length() == 0) {
            watchlistSection = new StocksSection("Watchlist", mWatchlist, MainActivity.this::onItemRootViewClicked);
            sectionAdapter.addSection(watchlistSection);
            mRecyclerView.setAdapter(sectionAdapter);
            return;
        }
        String watchListParams = "";
        Log.d(TAG, "getWatchList: " + watchlistTickers);
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
                    sortWatchlist();
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

    public void removeFromWatchlist(int position) {
        String ticker = mWatchlist.get(position).getTicker();
        mWatchlist.remove(position);
        for (int i = 0; i < watchlistTickers.length(); i++) {

            try {
                if (ticker.equals(watchlistTickers.get(i).toString())) {
                    watchlistTickers.remove(i);
                    break;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        sharedpreferences = getSharedPreferences(SHARED_PREFS,
                MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putString(watchlist, watchlistTickers.toString());
        editor.commit();
    }

    public void moveInList(String sectionTitle, int fromPosition, int toPosition) {
        sharedpreferences = getSharedPreferences(SHARED_PREFS,
                MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();

        if (sectionTitle == "Watchlist") {
            StockItem stock = mWatchlist.get(fromPosition);
            mWatchlist.remove(stock);
            mWatchlist.add(toPosition, stock);
            watchlistTickers = new JSONArray();
            for(int i=0;i<mWatchlist.size();i++) {
                watchlistTickers.put(mWatchlist.get(i).getTicker());
            }
            editor.putString(watchlist, watchlistTickers.toString());
            Log.d(TAG, "moveInList: " + watchlistTickers);
        } else {
            StockItem stock = mPortfolioList.get(fromPosition);
            mPortfolioList.remove(stock);
            mPortfolioList.add(toPosition, stock);
            portfolioTickers = new JSONArray();
            for(int i=0;i<mPortfolioList.size();i++) {
                portfolioTickers.put(mPortfolioList.get(i).getTicker());
            }
            editor.putString(portfolio, portfolioTickers.toString());
        }
        sectionAdapter.notifyDataSetChanged();
        editor.commit();
    }

    ItemTouchHelper.Callback itemTouchHelperCallback = new ItemTouchHelper.Callback() {
        @Override
        public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
            final int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
            final int swipeFlags = ItemTouchHelper.LEFT;
            return makeMovementFlags(dragFlags, swipeFlags);
        }

        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            try {
                StocksSection fromSection = (StocksSection) sectionAdapter.getSectionForPosition(viewHolder.getAdapterPosition());
                StocksSection toSection = (StocksSection) sectionAdapter.getSectionForPosition(target.getAdapterPosition());
                int fromPosition = sectionAdapter.getPositionInSection(viewHolder.getAdapterPosition());
                int toPosition = sectionAdapter.getPositionInSection(target.getAdapterPosition());
                if (fromSection.equals(toSection)) {
                    moveInList(fromSection.title, fromPosition, toPosition);
                }
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            try {
                if (sectionAdapter.getSectionForPosition(viewHolder.getAdapterPosition()) == watchlistSection) {
                    removeFromWatchlist(sectionAdapter.getPositionInSection(viewHolder.getAdapterPosition()));
                }
            } catch (IllegalArgumentException e) {
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