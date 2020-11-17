package com.akshay.stocks;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;
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

import static com.akshay.stocks.MainActivity.EXTRA_TICKER;


public class DetailActivity extends AppCompatActivity {

    private String SERVER = "http://8tsathna.us-east-1.elasticbeanstalk.com";
    private static final String TAG = "DetailActivity";

    private RequestQueue mRequestQueue;
    private TextView textViewName;
    private WebView chart;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        Intent intent = getIntent();
        String ticker  = intent.getStringExtra(EXTRA_TICKER);

        TextView textViewTicker  = findViewById(R.id.text_view_ticker_detail);
        textViewName  = findViewById(R.id.text_view_name_detail);
        chart = (WebView) findViewById(R.id.chart_web_view);
        chart.loadUrl("file:///android_asset/chart_web_view.html?ticker=" + ticker);
        chart.getSettings().setJavaScriptEnabled(true);

        textViewTicker.setText(ticker);

        mRequestQueue = Volley.newRequestQueue(this);

        parseDetails(ticker);
    }

    private void parseDetails(String ticker) {
        String url = SERVER + "/api/details?ticker=" + ticker;

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject stock) {
                try {
                    String name = stock.getString("name");
                    Log.d(TAG, "onResponse: "+stock);
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