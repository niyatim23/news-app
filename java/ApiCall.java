package com.example.newsapp;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ApiCall {
    private static ApiCall mInstance;
    private RequestQueue mRequestQueue;
    private static Context mCtx;
    public ApiCall(Context ctx) {
        mCtx = ctx;
        mRequestQueue = getRequestQueue();
    }
    public static synchronized ApiCall getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new ApiCall(context);
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

    public static void make(Context ctx, String query, Response.Listener<JSONObject> listener, Response.ErrorListener errorListener) {
        String url = "https://niyati-maheshwari235.cognitiveservices.azure.com/bing/v7.0/suggestions?q=" + query;
        JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.GET, url, null,listener, errorListener){
            @Override
            public Map<String, String> getHeaders(){
                Map<String, String>  params = new HashMap<>();
                params.put("Ocp-Apim-Subscription-Key", "29c023f5fb1b438f929aaa4b724a4bff");
                return params;
            }
        };
        ApiCall.getInstance(ctx).addToRequestQueue(jsonRequest);
    }
}
