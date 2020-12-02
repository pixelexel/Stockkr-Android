package com.akshay.stocks;

import android.content.Context;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

public class Search {
    private static Search mInstance;
    private RequestQueue mRequestQueue;
    private static Context mCtx;

    private static String SERVER = "http://9tqatssas.us-east-1.elasticbeanstalk.com";

    public Search(Context ctx) {
        mCtx = ctx;
        mRequestQueue = getRequestQueue();
    }
    public static synchronized Search getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new Search(context);
        }
        return mInstance;
    }
    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(mCtx.getApplicationContext());
        }
        return mRequestQueue;
    }
    public <T> void addToRequestQueue(Request<T> req) {
        getRequestQueue().add(req);
    }
    public static void make(Context ctx, String query, Response.Listener<String>
            listener, Response.ErrorListener errorListener) {
        String url = SERVER + "/api/autocomplete?search=" + query;
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                listener, errorListener);
        Search.getInstance(ctx).addToRequestQueue(stringRequest);
    }
}
