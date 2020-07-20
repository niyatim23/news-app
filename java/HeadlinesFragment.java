package com.example.newsapp;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.tabs.TabLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;

public class HeadlinesFragment extends Fragment {

    View rootView;
    TabLayout headlinesTabs;
    private ArrayList<MainActivity.GuardianArticle> headlinesArticles = new ArrayList<>();
    private SwipeRefreshLayout headlinesSwipeRefreshLayout;
    private RecyclerView recyclerView;
    private RecyclerViewAdapter adapter;
    private String currentSection = "world";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.headlines_fragment, container, false);
        headlinesTabs = rootView.findViewById(R.id.headlines_tabs);

        headlinesSwipeRefreshLayout = rootView.findViewById(R.id.headlines_swiperefresh_items);

        recyclerView = rootView.findViewById(R.id.recycler_view_home);


        MainActivity.spinner.setVisibility(View.VISIBLE);
        MainActivity.spinnerText.setText(R.string.progress_text);
        getSectionData("world");
        initListener();

        headlinesSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                headlinesArticles.clear();
                adapter = new RecyclerViewAdapter(getActivity(), headlinesArticles);
                recyclerView.setAdapter(adapter);
                recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
                getSectionData(currentSection);

                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if(headlinesSwipeRefreshLayout.isRefreshing()) {
                            headlinesSwipeRefreshLayout.setRefreshing(false);
                        }
                    }
                }, 1000);
            }
        });

        return rootView;
    }

    private void initListener() {
        headlinesTabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {

                headlinesArticles.clear();
                MainActivity.spinner.setVisibility(View.VISIBLE);
                MainActivity.spinnerText.setText(R.string.progress_text);
                RecyclerView recyclerView = rootView.findViewById(R.id.recycler_view_headlines);
                RecyclerViewAdapter adapter = new RecyclerViewAdapter(getActivity(),headlinesArticles);
                recyclerView.setAdapter(adapter);
                recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

                if(tab.getPosition() == 1){
                    getSectionData("business");
                }
                else if(tab.getPosition() == 2){
                    getSectionData("politics");
                }
                else if(tab.getPosition() == 3){
                    getSectionData("sports");
                }
                else if(tab.getPosition() == 4){
                    getSectionData("technology");
                }
                else if(tab.getPosition() == 5){
                    getSectionData("science");
                }
                else {
                    getSectionData("world");
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    private void getSectionData(String section){
        currentSection = section;
        headlinesArticles.clear();
        RequestQueue queue = Volley.newRequestQueue(getActivity());
        String url ="https://newsandroidserver.wl.r.appspot.com/section_news/guardian/" + section;
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
                                    headlinesArticles.add(new MainActivity.GuardianArticle(getImageFromJson(article),
                                            article.getString("webTitle"),
                                            article.getString("webUrl"), getTime(article.getString("webPublicationDate")),
                                            article.getString("sectionName"),article.getString("id"), article.getString("webPublicationDate")));
                                }
                                recyclerView = rootView.findViewById(R.id.recycler_view_headlines);
                                adapter = new RecyclerViewAdapter(getActivity(),headlinesArticles);
                                recyclerView.setAdapter(adapter);
                                recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
                                MainActivity.spinner.setVisibility(View.GONE);
                                MainActivity.spinnerText.setText("");
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
            image = article.getJSONObject("blocks").getJSONObject("main")
                    .getJSONArray("elements").getJSONObject(0)
                    .getJSONArray("assets").getJSONObject(0).getString("file");
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
    public void onResume() {
        super.onResume();
        try {
            adapter.notifyDataSetChanged();
        }
        catch(Exception ignored) {

        }
    }
}
