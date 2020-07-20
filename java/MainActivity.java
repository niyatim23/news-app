package com.example.newsapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ProgressBar;
import android.widget.TextView;


import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import java.util.List;

public class MainActivity extends AppCompatActivity {


    private static final int TRIGGER_AUTO_COMPLETE = 100;
    private static final long AUTO_COMPLETE_DELAY = 300;
    private Handler handler;
    private AutoSuggestAdapter autoSuggestAdapter;
    private SearchView searchView;
    private Context context;
    public static ProgressBar spinner;
    public static TextView spinnerText;

    public static class GuardianArticle {
        public String image;
        public String title;
        public String webUrl;
        public String timeElapsed;
        public String sectionName;
        public String identifier;
        public String rawDate;

        public GuardianArticle(String image, String title, String webUrl, String timeElapsed, String sectionName, String identifier, String rawDate) {
            this.image = image;
            this.title = title;
            this.webUrl = webUrl;
            this.timeElapsed = timeElapsed;
            this.sectionName = sectionName;
            this.identifier = identifier;
            this.rawDate = rawDate;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar homeToolbar = findViewById(R.id.home_toolbar);
        setSupportActionBar(homeToolbar);
        spinner = findViewById(R.id.progress_bar);
        spinnerText = findViewById(R.id.progress_bar_text);
        spinner.setVisibility(View.GONE);
        spinnerText.setText("");
        context = this;

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnNavigationItemSelectedListener(bottomNavListener);

        getSupportFragmentManager().beginTransaction().replace(R.id.bottom_navigation_fragments, new HomeFragment()).commit();
    }

    @SuppressLint("RestrictedApi")
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_toolbar, menu);

        MenuItem searchItem = menu.findItem(R.id.toolbar_search);

        searchView = (SearchView) searchItem.getActionView();
        searchView.setQueryHint("");

        final SearchView.SearchAutoComplete searchAutoComplete = searchView.findViewById(androidx.appcompat.R.id.search_src_text);
        autoSuggestAdapter = new AutoSuggestAdapter(this, android.R.layout.simple_dropdown_item_1line);
        searchAutoComplete.setThreshold(3);
        searchAutoComplete.setAdapter(autoSuggestAdapter);

        searchAutoComplete.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        searchAutoComplete.setText(autoSuggestAdapter.getObject(position));
                        //searchView.setQuery(autoSuggestAdapter.getObject(position), true);

                    }
                });
        searchAutoComplete.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                handler.removeMessages(TRIGGER_AUTO_COMPLETE);
                handler.sendEmptyMessageDelayed(TRIGGER_AUTO_COMPLETE, AUTO_COMPLETE_DELAY);
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
                        makeApiCall(searchAutoComplete.getText().toString());
                    }
                }
                return false;
            }
        });

        searchView.setOnQueryTextListener(
                new SearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String query) {
                        Intent intent = new Intent(context, SearchResultsActivity.class);
                        intent.putExtra("query", query);
                        context.startActivity(intent);
                        return false;
                    }

                    @Override
                    public boolean onQueryTextChange(String newText) {
                        return false;
                    }
                }
        );

        return true;
    }


    private void makeApiCall(String text) {
        ApiCall.make(this, text, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                List<String> stringList = new ArrayList<>();
                try {
                    if(response.has("suggestionGroups")){
                        JSONArray suggestions = response.getJSONArray("suggestionGroups").getJSONObject(0).getJSONArray("searchSuggestions");
                        for (int i = 0; i < suggestions.length(); i++) {
                            JSONObject word = suggestions.getJSONObject(i);
                            stringList.add(word.getString("displayText"));
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                autoSuggestAdapter.setData(stringList.size() > 4 ? stringList.subList(0, 5) : stringList);
                autoSuggestAdapter.notifyDataSetChanged();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        });
    }

    private BottomNavigationView.OnNavigationItemSelectedListener bottomNavListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                    Fragment currentFragment;
                    if(menuItem.getItemId() == R.id.home) {
                        currentFragment = new HomeFragment();
                    }
                    else if(menuItem.getItemId() == R.id.headlines) {
                        currentFragment = new HeadlinesFragment();
                    }
                    else if(menuItem.getItemId() == R.id.trending) {
                        currentFragment = new TrendingFragment();
                    }
                    else{
                        currentFragment = new BookmarksFragment();
                    }
                    getSupportFragmentManager().beginTransaction().replace(R.id.bottom_navigation_fragments, currentFragment).commit();
                    return true;
                }
            };

}
