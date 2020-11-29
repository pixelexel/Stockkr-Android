package com.akshay.stocks;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
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

import static com.akshay.stocks.MainActivity.EXTRA_TICKER;
import static com.akshay.stocks.MainActivity.SHARED_PREFS;
import static com.akshay.stocks.MainActivity.watchlist;
import static java.lang.Integer.parseInt;


public class DetailActivity extends AppCompatActivity implements TradeDialog.TradeDialogListener, SuccessDialog.SuccessDialogListener {

    private String SERVER = "http://8tsathna.us-east-1.elasticbeanstalk.com";
    private static final String TAG = "DetailActivity";

    private RequestQueue mRequestQueue;
    private TextView textViewName;
    private WebView chart;
    private String ticker;
    private boolean favorite = false;
    private JSONArray watchlistTickers;
    private Button tradeButton;
    private TradeDialog tradeDialog;
    private StockItem stockItem;
    private SuccessDialog successDialog;

    //Details
    private TextView textViewSharesOwned;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);


        Intent intent = getIntent();
        ticker = intent.getStringExtra(EXTRA_TICKER);
        loadData(ticker);

        TextView textViewTicker  = findViewById(R.id.text_view_ticker_detail);
        textViewName  = findViewById(R.id.text_view_name_detail);
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
        textViewSharesOwned  = findViewById(R.id.text_view_shares_owned);
        textViewSharesOwned.setText("Shares Owned: " + sharedpreferences.getInt(ticker, 0));
        tradeButton = (Button) findViewById(R.id.trade_button);
        tradeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                openTradeDialog();
            }
            });
    }

    private void openTradeDialog() {
        tradeDialog = new TradeDialog(stockItem);

        tradeDialog.show(getSupportFragmentManager(), "trade dialog");
    }

    @Override
    public void tradeStocks(boolean option, String shares) {
        SharedPreferences sharedpreferences = getSharedPreferences(SHARED_PREFS,
                MODE_PRIVATE);
        int existingShares = sharedpreferences.getInt(ticker, 0);
        int deltaShares = Integer.parseInt(shares);
        if(option){
            existingShares +=  deltaShares ;
        } else {
            existingShares -= deltaShares;
        }
        SharedPreferences.Editor editor = sharedpreferences.edit();

        if(existingShares <= 0){
            editor.remove(ticker);
        } else {
            editor.putInt(ticker, existingShares);
        }

        editor.commit();
        textViewSharesOwned.setText("Shares Owned: " + existingShares);
        tradeDialog.dismiss();

        successDialog = new SuccessDialog(ticker, option, deltaShares);
        successDialog.show(getSupportFragmentManager(), "trade dialog");
    }

    @Override
    public void closeDialog() {
        successDialog.dismiss();
    }

    private void loadData(String ticker) {
        SharedPreferences sharedpreferences = getSharedPreferences(SHARED_PREFS,
                MODE_PRIVATE);
        try {
            watchlistTickers = new JSONArray(sharedpreferences.getString(watchlist, ""));
            for (int i = 0; i < watchlistTickers.length(); i++) {
                String list_ticker = (String) watchlistTickers.get(i);
                if(ticker.equals(list_ticker)){
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
        if(favorite == true){
            menu.findItem(R.id.action_star_border).setVisible(false);
            menu.findItem(R.id.action_star).setVisible(true);
        } else {
            menu.findItem(R.id.action_star).setVisible(false);
            menu.findItem(R.id.action_star_border).setVisible(true);
        }
        manageWatchlist(favorite);
        return true;
    }

    private void manageWatchlist(boolean fav) {
        SharedPreferences sharedpreferences = getSharedPreferences(SHARED_PREFS,
                MODE_PRIVATE);
        if(!fav){
            for (int i = 0; i < watchlistTickers.length(); i++) {
                try {
                    if (ticker.equals(watchlistTickers.get(i).toString())) {
                        Log.d(TAG, "manageWatchlist: remove");
                        watchlistTickers.remove(i);
                        break;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        } else {
            watchlistTickers.put(ticker);
        }
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putString(watchlist, watchlistTickers.toString());
        editor.commit();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                onBackPressed();
                return true;
            case R.id.action_star_border:
                Toast.makeText(this, ticker + " added to favorites", Toast.LENGTH_LONG).show();
                favorite = true;
                invalidateOptionsMenu();
            case R.id.action_star:
                Toast.makeText(this, ticker + " removed from favorites", Toast.LENGTH_LONG).show();
                favorite = false;
                invalidateOptionsMenu();
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
                    Double last = Double.parseDouble(stock.getString("last"));
                    stockItem = new StockItem(ticker, name, last);
                    textViewName.setText(name);
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



}