package com.akshay.stocks;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;

import static com.akshay.stocks.MainActivity.EXTRA_TICKER;
import static com.akshay.stocks.MainActivity.SHARED_PREFS;
import static com.akshay.stocks.MainActivity.available;
import static com.akshay.stocks.MainActivity.portfolio;
import static com.akshay.stocks.MainActivity.watchlist;
import static java.lang.Integer.parseInt;


public class DetailActivity extends AppCompatActivity implements TradeDialog.TradeDialogListener, SuccessDialog.SuccessDialogListener, NewsDialog.NewsDialogListener, NewsAdapter.onNewsItemClickListener {

    private String SERVER = "http://9tqatssas.us-east-1.elasticbeanstalk.com";
    private static final String TAG = "DetailActivity";

    private RequestQueue mRequestQueue;
    private TextView textViewName;
    private WebView chart;
    private String ticker;
    private boolean favorite = false;
    private JSONArray watchlistTickers;
    private JSONArray portfolioTickers;
    private Button tradeButton;
    private TradeDialog tradeDialog;
    private StockItemDetail stockItem;
    private SuccessDialog successDialog;

    //Details
    private TextView textViewSharesOwned;

    //News
    private RecyclerView recyclerViewNews;
    private NewsAdapter newsAdapter;
    private ArrayList<NewsItem> newsItemArrayList;
    private RequestQueue requestQueue;
    private NewsDialog newsDialog;

    //Trade
    int existingShares;
    float availableAmount;

    //Fetch
    private ProgressBar spinner;
    private TextView textViewFetch;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        spinner = (ProgressBar) findViewById(R.id.progressBar1);
        textViewFetch = (TextView) findViewById(R.id.text_view_fetch);


        Intent intent = getIntent();
        ticker = intent.getStringExtra(EXTRA_TICKER);
        loadData(ticker);

        TextView textViewTicker = findViewById(R.id.text_view_ticker_detail);
        textViewName = findViewById(R.id.text_view_name_detail);
        chart = (WebView) findViewById(R.id.chart_web_view);
        chart.loadUrl("file:///android_asset/chart_web_view.html?ticker=" + ticker);
        chart.getSettings().setJavaScriptEnabled(true);

        textViewTicker.setText(ticker);

        mRequestQueue = Volley.newRequestQueue(this);

        parseDetails(ticker);

        //back
        ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);

        //Trade
        SharedPreferences sharedpreferences = getSharedPreferences(SHARED_PREFS,
                MODE_PRIVATE);
        textViewSharesOwned = findViewById(R.id.text_view_shares_owned);
        existingShares = sharedpreferences.getInt(ticker, 0);

        if (existingShares == 0) {
            textViewSharesOwned.setText("You have 0 shares of " + ticker + ".");
        } else {
            textViewSharesOwned.setText("Shares Owned: " + existingShares);
        }

        tradeButton = (Button) findViewById(R.id.trade_button);
        tradeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openTradeDialog();
            }
        });
        availableAmount = sharedpreferences.getFloat(available, 0);


        //News
        recyclerViewNews = findViewById(R.id.recycler_view_news);
        recyclerViewNews.setHasFixedSize(true);
        recyclerViewNews.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewNews.setNestedScrollingEnabled(false);

        newsItemArrayList = new ArrayList<>();

        requestQueue = Volley.newRequestQueue(this);
        parseNews();
    }

    private void parseNews() {
        String url = SERVER + "/api/news?ticker=" + ticker;

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray jsonArray = response.getJSONArray("articles");


                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject article = jsonArray.getJSONObject(i);

                                String website = article.getJSONObject("source").getString("name");
                                String title = article.getString("title");
                                String imageUrl = article.getString("urlToImage");
                                String url = article.getString("url");
                                String publishedAt = article.getString("publishedAt");
                                String time = getTimeDiff(publishedAt);
                                newsItemArrayList.add(new NewsItem(imageUrl, website, title, url, time));
                            }

                            newsAdapter = new NewsAdapter(DetailActivity.this, newsItemArrayList);
                            recyclerViewNews.setAdapter(newsAdapter);
                            newsAdapter.setOnItemClickListener(DetailActivity.this);

                            textViewFetch.setVisibility(View.GONE);
                            spinner.setVisibility(View.GONE);

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

        requestQueue.add(request);
    }

    private String getTimeDiff(String publishedAt) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        long currentTimestamp = Instant.now().toEpochMilli();
        Date p = null;
        try {
            p = sdf.parse(publishedAt);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        long diff = currentTimestamp - p.getTime();
        long diffMinutes = diff / (60 * 1000) % 60;
        if (diffMinutes < 60) {
            return diffMinutes + " minutes ago";
        }
        long diffHours = diff / (60 * 60 * 1000);
        if (diffHours < 24) {
            return diffHours + " hours ago";
        }
        int diffInDays = (int) diff / (1000 * 60 * 60 * 24);
        return diffInDays + " days ago";
    }

    private void openTradeDialog() {
        tradeDialog = new TradeDialog(stockItem, availableAmount, existingShares);

        tradeDialog.show(getSupportFragmentManager(), "trade dialog");
    }

    @Override
    public void tradeStocks(boolean option, String shares) {
        SharedPreferences sharedpreferences = getSharedPreferences(SHARED_PREFS,
                MODE_PRIVATE);

        if (existingShares == 0) {
            managePortfolio(true);
        }
        int deltaShares = Integer.parseInt(shares);
        if (option) {
            availableAmount -= deltaShares * stockItem.getLast();
            existingShares += deltaShares;
        } else {
            availableAmount += deltaShares * stockItem.getLast();
            existingShares -= deltaShares;
        }
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putFloat(available, availableAmount);

        if (existingShares <= 0) {
            editor.remove(ticker);
            managePortfolio(false);
        } else {
            editor.putInt(ticker, existingShares);
        }

        editor.commit();
        if (existingShares == 0) {
            textViewSharesOwned.setText("You have 0 shares of " + ticker + ".");
        } else {
            textViewSharesOwned.setText("Shares Owned: " + existingShares);
        }
        tradeDialog.dismiss();

        successDialog = new SuccessDialog(ticker, option, deltaShares);
        successDialog.show(getSupportFragmentManager(), "trade dialog");
    }

    private void managePortfolio(boolean b) {
        SharedPreferences sharedpreferences = getSharedPreferences(SHARED_PREFS,
                MODE_PRIVATE);

        try {
            portfolioTickers = new JSONArray(sharedpreferences.getString(portfolio, ""));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (b) {
            portfolioTickers.put(ticker);
        } else {
            for (int i = 0; i < portfolioTickers.length(); i++) {
                try {
                    if (ticker.equals(portfolioTickers.get(i).toString())) {
                        portfolioTickers.remove(i);
                        break;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putString(portfolio, portfolioTickers.toString());
        Log.d(TAG, "portfolio: " + portfolioTickers.toString());
        editor.commit();
    }

    private void loadData(String ticker) {
        SharedPreferences sharedpreferences = getSharedPreferences(SHARED_PREFS,
                MODE_PRIVATE);
        try {
            watchlistTickers = new JSONArray(sharedpreferences.getString(watchlist, ""));
            for (int i = 0; i < watchlistTickers.length(); i++) {
                String list_ticker = (String) watchlistTickers.get(i);
                if (ticker.equals(list_ticker)) {
                    favorite = true;
                    Log.d(TAG, "loadData: Found");
                    invalidateOptionsMenu();
                }

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.detail_menu, menu);
        Log.d(TAG, "onCreateOptionsMenu: " + favorite);
        if (favorite == true) {
            menu.findItem(R.id.action_border).setVisible(false);
            menu.findItem(R.id.action_star).setVisible(true);
        } else {
            menu.findItem(R.id.action_border).setVisible(true);
            menu.findItem(R.id.action_star).setVisible(false);
        }
        return true;
    }

    private void manageWatchlist(boolean fav) {
        SharedPreferences sharedpreferences = getSharedPreferences(SHARED_PREFS,
                MODE_PRIVATE);
        if (fav) {
            watchlistTickers.put(ticker);
        } else {
            for (int i = 0; i < watchlistTickers.length(); i++) {
                try {
                    if (ticker.equals(watchlistTickers.get(i).toString())) {
                        watchlistTickers.remove(i);
                        Log.d(TAG, "manageWatchlist: remove" + watchlistTickers);
                        break;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putString(watchlist, watchlistTickers.toString());
        Log.d(TAG, "manageWatchlist: " + watchlistTickers.toString());
        editor.commit();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                onBackPressed();
                return true;
            case R.id.action_border:
                Toast.makeText(this, ticker + " added to favorites", Toast.LENGTH_LONG).show();
                favorite = true;
                invalidateOptionsMenu();
                manageWatchlist(favorite);
                return true;
            case R.id.action_star:
                Toast.makeText(this, ticker + " removed from favorites", Toast.LENGTH_LONG).show();
                favorite = false;
                invalidateOptionsMenu();
                manageWatchlist(favorite);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    private void parseDetails(String ticker) {
        String url = SERVER + "/api/details?ticker=" + ticker;

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject stock) {
                try {
                    String name = stock.getString("name");
                    String about = stock.getString("description");
                    String close = stock.getString("prevClose");
                    String high = stock.getString("high");
                    String low = stock.getString("low");
                    String open = stock.getString("open");
                    String volume = stock.getString("volume");
                    String mid = stock.getString("mid");
                    String bid = stock.getString("bidPrice");

                    double last = Double.parseDouble(stock.getString("last"));
                    String change = String.format("%.2f", (last - Double.parseDouble(close)));

                    stockItem = new StockItemDetail(name, ticker, last);

                    TextView textViewChange = findViewById(R.id.text_view_change);
                    if (last - Double.parseDouble(close) > 0) {
                        textViewChange.setText("+$" + Math.abs(Double.parseDouble(change)));
                        textViewChange.setTextColor(Color.parseColor("#319C5E"));
                    } else if (last - Double.parseDouble(close) < 0) {
                        textViewChange.setText("-$" + Math.abs(Double.parseDouble(change)));
                        textViewChange.setTextColor(Color.parseColor("#9B4049"));
                    } else {
                        textViewChange.setTextColor(Color.parseColor("#D3D3D3"));
                    }

                    textViewName.setText(name);
                    TextView textViewLast = findViewById(R.id.text_view_last_detail);
                    textViewLast.setText("$" + String.format("%.2f", last));
                    TextView textViewMarketValue = findViewById(R.id.text_view_market_value);
                    if (existingShares == 0) {
                        textViewMarketValue.setText("Start Trading!");
                    } else {
                        textViewMarketValue.setText("Market Value: " + String.format("%.2f", existingShares * last));
                    }

                    TextView textViewDesc = findViewById(R.id.text_view_description);
                    textViewDesc.setText(about);
                    TextView textViewShow = findViewById(R.id.text_view_show);
                    textViewShow.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (textViewShow.getText().equals("Show more...")) {
                                textViewDesc.setMaxLines(40);
                                textViewShow.setText("Show less");
                            } else {
                                textViewDesc.setMaxLines(2);
                                textViewShow.setText("Show more...");
                            }
                        }
                    });

                    TextView textViewCurrent = findViewById(R.id.tv_current);
                    textViewCurrent.setText("Current Price: " + String.valueOf(last));
                    TextView textViewLow = findViewById(R.id.tv_low);
                    textViewLow.setText("Low: " + low);
                    TextView textViewBid = findViewById(R.id.tv_bid);
                    if(bid.equals("null")){
                        bid = "0.0";
                    }
                    textViewBid.setText("Bid Price: " + bid);
                    TextView textViewOpen = findViewById(R.id.tv_open);
                    textViewOpen.setText("Open Price: " + open);
                    TextView textViewMid = findViewById(R.id.tv_mid);
                    if(mid.equals("null")){
                        mid = "0.0";
                    }
                    textViewMid.setText("Mid: " + mid);
                    TextView textViewHigh = findViewById(R.id.tv_high);
                    textViewHigh.setText("High: " + high);
                    TextView textViewVolume = findViewById(R.id.tv_volume);
                    textViewVolume.setText("Volume: " + volume);

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

        mRequestQueue.add(request);
    }

    @Override
    public void onNewsItemClick(int position) {
        NewsItem newsItem = newsItemArrayList.get(position);

        newsDialog = new NewsDialog(newsItem.getTitle(), newsItem.getUrl(), newsItem.getImageUrl());

        newsDialog.show(getSupportFragmentManager(), "news dialog");
    }

    @Override
    public void closeDialog() {
        successDialog.dismiss();
    }

    @Override
    public void closeNewsDialog() {
        newsDialog.dismiss();
    }

}