package com.akshay.stocks;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;
import androidx.core.view.MenuItemCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.Html;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;

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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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
    public static final String worth = "worth";
    public static final String available = "available";
    public static final String init = "init";

    //Search
    private AutoSuggestAdapter autoSuggestAdapter;
    private Handler handler;
    private static final int TRIGGER_AUTO_COMPLETE = 100;
    private static final long AUTO_COMPLETE_DELAY = 300;
    private SearchView.SearchAutoComplete searchAutoComplete;

    private double net_worth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().setElevation(0);

        //Date
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM d, yyyy");
        String date = sdf.format(new Date());
        TextView textViewMainDate = findViewById(R.id.text_view_main_date);
        textViewMainDate.setText(date);

        sharedpreferences = getSharedPreferences(SHARED_PREFS,
                MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        if(!sharedpreferences.getBoolean(init, false)){
            editor.putBoolean(init, true).apply();
            String[] temp_list = {};
            JSONArray json_temp_list = null;
            try {
                json_temp_list = new JSONArray(temp_list);
                editor.putString(watchlist, json_temp_list.toString());
                editor.putString(portfolio, json_temp_list.toString());
                editor.putInt(worth, 20000);
                editor.putInt(available, 20000);
                editor.commit();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        sectionAdapter = new SectionedRecyclerViewAdapter();
        mRecyclerView = findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(mRecyclerView);
        mPortfolioList = new ArrayList<>();
        mWatchlist = new ArrayList<>();
        mRequestQueue = Volley.newRequestQueue(this);
        autoSuggestAdapter = new AutoSuggestAdapter(this,
                android.R.layout.simple_dropdown_item_1line);
        loadStockList();
        getStockList();
    }


    private void makeSearchCall(String text) {
        Search.make(this, text, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                List<String> stringList = new ArrayList<>();
                try {
                    JSONArray array = new JSONArray(response);
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject stock = array.getJSONObject(i);
                        stringList.add(stock.getString("ticker") + " - " + stock.getString("name"));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                autoSuggestAdapter.setData(stringList);
                autoSuggestAdapter.notifyDataSetChanged();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        });
    }

    @SuppressLint("RestrictedApi")
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        MenuItem item = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) item.getActionView();
        searchAutoComplete = (SearchView.SearchAutoComplete) searchView.findViewById(R.id.search_src_text);
        //searchAutoComplete.setTextColor(getResources().getColor(R.color.example_color));
        searchAutoComplete.setDropDownBackgroundResource(R.color.white);
        searchAutoComplete.setThreshold(3);
        searchAutoComplete.setAdapter(autoSuggestAdapter);

        searchAutoComplete.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int
                    count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
                handler.removeMessages(TRIGGER_AUTO_COMPLETE);
                handler.sendEmptyMessageDelayed(TRIGGER_AUTO_COMPLETE,
                        AUTO_COMPLETE_DELAY);
            }
            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                if (msg.what == TRIGGER_AUTO_COMPLETE) {
                    if (!TextUtils.isEmpty(searchAutoComplete.getText())) {
                        makeSearchCall(searchAutoComplete.getText().toString());
                    }
                }
                return false;
            }
        });

        searchAutoComplete.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int itemIndex, long id) {
                String queryString=(String)adapterView.getItemAtPosition(itemIndex);
                searchAutoComplete.setText("" + queryString);
            }
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                loadDetails(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    public void loadDetails(String query) {
        int index = query.indexOf("-");
        if (index != -1)
        {
            Intent detailIntent = new Intent(this, DetailActivity.class);
            detailIntent.putExtra(EXTRA_TICKER, query.substring(0,index-1));
            startActivity(detailIntent);
        }
    }

    public void loadStockList() {
        sharedpreferences = getSharedPreferences(SHARED_PREFS,
                MODE_PRIVATE);
        try {
            watchlistTickers = new JSONArray(sharedpreferences.getString(watchlist, null));
            portfolioTickers = new JSONArray(sharedpreferences.getString(portfolio, null));
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
        if (portfolioTickers.length() == 0) {
            portfolioSection = new StocksSection("PORTFOLIO - You do not have any stocks in your portfolio", mPortfolioList, MainActivity.this::onItemRootViewClicked);
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

                    sharedpreferences = getSharedPreferences(SHARED_PREFS,
                            MODE_PRIVATE);
                    double temp = 0;
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject stock = jsonArray.getJSONObject(i);

                        String ticker = stock.getString("ticker");
                        Double last = Double.parseDouble(stock.getString("last"));
                        Double prevClose = Double.parseDouble(stock.getString("prevClose"));
                        Double change = last - prevClose;

                        int shares = sharedpreferences.getInt(ticker,0);
                        String name =  shares + ".0 shares";

                        temp += last*shares;
                        mPortfolioList.add(new StockItem(name, ticker, last, change));
                    }

                    double available = sharedpreferences.getFloat("available",0);
                    net_worth = available + temp;

                    sortPortfolio();
                    portfolioSection = new StocksSection("PORTFOLIO", mPortfolioList, MainActivity.this::onItemRootViewClicked);
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
            watchlistSection = new StocksSection("WATCHLIST - You do not have any stocks in your watchlist", mWatchlist, MainActivity.this::onItemRootViewClicked);
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
                        Double last = Double.parseDouble(stock.getString("last"));
                        Double prevClose = Double.parseDouble(stock.getString("prevClose"));
                        Double change = last - prevClose;

                        sharedpreferences = getSharedPreferences(SHARED_PREFS,
                                MODE_PRIVATE);
                        int shares = sharedpreferences.getInt(ticker,0);
                        if(shares > 0){
                            name =  shares + ".0 shares";
                        }

                        mWatchlist.add(new StockItem(name, ticker, last, change));
                    }
                    sortWatchlist();
                    watchlistSection = new StocksSection("WATCHLIST", mWatchlist, MainActivity.this::onItemRootViewClicked);
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
           //Log.d(TAG, "moveInList: " + watchlistTickers);
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

        editor.commit();
    }

    ItemTouchHelper.Callback itemTouchHelperCallback = new ItemTouchHelper.Callback() {
        @Override
        public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
            final int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN | ItemTouchHelper.START | ItemTouchHelper.END;
            final int swipeFlags = ItemTouchHelper.LEFT;
            return makeMovementFlags(dragFlags, swipeFlags);
        }

        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            try {
                StocksSection fromSection = (StocksSection) sectionAdapter.getSectionForPosition(viewHolder.getAdapterPosition());
                StocksSection toSection = (StocksSection) sectionAdapter.getSectionForPosition(target.getAdapterPosition());
                int fromPosition = viewHolder.getAdapterPosition();
                int toPosition = target.getAdapterPosition();
                if (fromSection.equals(toSection)) {
                    moveInList(fromSection.title, sectionAdapter.getPositionInSection(fromPosition), sectionAdapter.getPositionInSection(toPosition));
                    sectionAdapter.notifyItemMoved(fromPosition,toPosition);
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