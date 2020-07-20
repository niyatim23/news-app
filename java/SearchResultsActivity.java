package com.example.newsapp;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
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

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;

public class SearchResultsActivity extends AppCompatActivity {

    private ArrayList <MainActivity.GuardianArticle> searchArticles = new ArrayList<>();
    Toolbar searchToolbar;
    private ProgressBar spinner;
    private TextView spinnerText;
    private SwipeRefreshLayout searchSwipeRefreshLayout;
    private Context context;
    private String query = "";
    private RecyclerViewAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_results);

        spinner = findViewById(R.id.progress_bar_search);
        spinnerText = findViewById(R.id.progress_bar_text_search);
        spinner.setVisibility(View.VISIBLE);
        spinnerText.setText(R.string.progress_text);

        context = this;

        searchToolbar = findViewById(R.id.search_toolbar);
        setSupportActionBar(searchToolbar);

        query = this.getIntent().getStringExtra("query");

        TextView textView = findViewById(R.id.search_toolbar_text);
        textView.setText("Search Results for "+ query);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        getSearchResults(query);

        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        searchSwipeRefreshLayout = findViewById(R.id.search_swiperefresh_items);

        searchSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                searchArticles.clear();
                RecyclerView recyclerView = findViewById(R.id.recycler_view_search);
                RecyclerViewAdapter adapter = new RecyclerViewAdapter(context, searchArticles);
                recyclerView.setAdapter(adapter);
                recyclerView.setLayoutManager(new LinearLayoutManager(context));
                getSearchResults(query);

                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if(searchSwipeRefreshLayout.isRefreshing()) {
                            searchSwipeRefreshLayout.setRefreshing(false);
                        }
                    }
                }, 1000);
            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }


    private void getSearchResults(String query){
        RequestQueue queue = Volley.newRequestQueue(this);
        final Context context = this;
        String url ="https://newsandroidserver.wl.r.appspot.com/search/guardian/" + query;

        searchArticles.clear();

        JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @RequiresApi(api = Build.VERSION_CODES.O)
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            if (response.has("response") && response.getJSONObject("response").has("results")) {
                                JSONArray jsonArray = response.getJSONObject("response").getJSONArray("results");
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject article = jsonArray.getJSONObject(i);
                                    searchArticles.add(new MainActivity.GuardianArticle(getImageFromJson(article), article.getString("webTitle"),
                                            article.getString("webUrl"), getTime(article.getString("webPublicationDate")),
                                            article.getString("sectionName"),article.getString("id"), article.getString("webPublicationDate")));
                                }
                                RecyclerView recyclerView = findViewById(R.id.recycler_view_search);
                                adapter = new RecyclerViewAdapter(context, searchArticles);
                                recyclerView.setAdapter(adapter);
                                recyclerView.setLayoutManager(new LinearLayoutManager(context));

                                spinner.setVisibility(View.GONE);
                                spinnerText.setText("");
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                });
        queue.add(jsonRequest);
    }

    private String getImageFromJson(JSONObject article) {
        String image;
        try {
            image = article.getJSONObject("blocks").getJSONObject("main").getJSONArray("elements").getJSONObject(0).getJSONArray("assets").getJSONObject(0).getString("file");
        }
        catch(JSONException e){
            image  = "https://assets.guim.co.uk/images/eada8aa27c12fe2d5afa3a89d3fbae0d/fallback-logo.png";
        }
        return image;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private String getTime(String publicationDate){
        ZoneId toTimeZone = ZoneId.of("America/Los_Angeles");
        ZonedDateTime currentDateTime = ZonedDateTime.now(toTimeZone);
        ZonedDateTime publicationDateTime = Instant.parse(publicationDate).atZone(toTimeZone);
        Duration duration = Duration.between(publicationDateTime, currentDateTime);
        if(duration.toMillis()/(1000 * 60 * 60) > 0) {
            int hours = (int)(duration.toMillis()/(1000 * 60 * 60));
            if (hours <= 24) {
                return (duration.toMillis()/(1000 * 60 * 60) + "h ago");
            }
            else {
                return ((hours / 24) + "d ago");
            }
        }
        if((duration.toMillis()/(1000 * 60) > 0)) {
            return (duration.toMillis()/(1000 * 60) + "m ago");
        }
        return (duration.toMillis()/(1000) + "s ago");
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            adapter.notifyDataSetChanged();
        }
        catch(Exception e) {

        }
    }
}



