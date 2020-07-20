package com.example.newsapp;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class DetailsActivity extends AppCompatActivity {

    private Context context;
    private String identifier;

    private ImageView imageDetailed;
    private TextView titleDetailed;
    private TextView dateDetailed;
    private TextView sectionNameDetailed;
    private TextView content;
    private MenuItem tweetItem;
    private MenuItem bookmarkItem;
    private TextView fullArticleLink;
    private String webUrl;
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    private MainActivity.GuardianArticle currentArticle;
    private ProgressBar spinner;
    private TextView spinnerText;
    private String title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        spinner = findViewById(R.id.progress_bar);
        spinnerText = findViewById(R.id.progress_bar_text);

        spinner.setVisibility(View.VISIBLE);
        spinnerText.setText(R.string.progress_text);

        context = this;
        preferences = context.getSharedPreferences("MODE_PRIVATE", 0);
        editor = preferences.edit();

        Toolbar detailedToolbar = findViewById(R.id.detailed_toolbar);
        setSupportActionBar(detailedToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        Intent myIntent = getIntent();
        identifier = myIntent.getStringExtra("identifier");

        TextView textView = findViewById(R.id.detailed_toolbar_title);
        title = myIntent.getStringExtra("title");
        textView.setText(title);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        imageDetailed = findViewById(R.id.image_detailed);
        titleDetailed = findViewById(R.id.title_detailed);
        dateDetailed = findViewById(R.id.date_detailed);
        sectionNameDetailed = findViewById(R.id.section_name_detailed);
        content = findViewById(R.id.content);
        fullArticleLink = findViewById(R.id.full_article_link);
        getData();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        else if(item.getItemId() == R.id.detailed_toolbar_twitter){
            context.startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://twitter.com/intent/tweet/?text=Check out this Link: " + webUrl + "\n&hashtags=CSCI571NewsSearch")));
        }
        else {
            if(preferences.contains(identifier)){
                editor.remove(identifier);
                bookmarkItem.setIcon(context.getResources().getDrawable(R.drawable.ic_bookmark_border_black_24dp));
                Toast.makeText(context, '"' + title + "\" was removed from bookmarks", Toast.LENGTH_SHORT).show();
            }
            else{
                Gson gson = new Gson();
                String json = gson.toJson(currentArticle);
                editor.putString(currentArticle.identifier, json);
                bookmarkItem.setIcon(context.getResources().getDrawable(R.drawable.ic_bookmark_filled));
                Toast.makeText(context, '"' + title + "\" was added to bookmarks", Toast.LENGTH_SHORT).show();
            }
            editor.commit();

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.detailed_toolbar, menu);
        tweetItem = menu.findItem(R.id.detailed_toolbar_twitter);
        bookmarkItem = menu.findItem(R.id.detailed_toolbar_bookmark);

        if(preferences.contains(identifier)){
            bookmarkItem.setIcon(context.getResources().getDrawable(R.drawable.ic_bookmark_filled));
        }
        else{
            bookmarkItem.setIcon(context.getResources().getDrawable(R.drawable.ic_bookmark_border_black_24dp));
        }

        return true;
    }

    private void getData(){
        RequestQueue queue = Volley.newRequestQueue(this);
        String url ="https://newsandroidserver.wl.r.appspot.com/get_article/guardian/?identifier=" + identifier;
        JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @RequiresApi(api = Build.VERSION_CODES.O)
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            if (response.has("response")) {
                                JSONObject article = response.getJSONObject("response").getJSONObject("content");
                                Glide.with(context).load(getImageFromJson(article)).into(imageDetailed);
                                titleDetailed.setText(article.getString("webTitle"));
                                dateDetailed.setText(getDate(article.getString("webPublicationDate")));
                                sectionNameDetailed.setText(article.getString("sectionName"));
                                content.setText(getContent(article));
                                webUrl = article.getString("webUrl");
                                currentArticle = new MainActivity.GuardianArticle(getImageFromJson(article),
                                        article.getString("webTitle"), webUrl, getTime(article.getString("webPublicationDate")),
                                        article.getString("sectionName"), identifier, article.getString("webPublicationDate"));
                                fullArticleLink.setText(R.string.view_full_article);
                                fullArticleLink.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(webUrl)));
                                    }
                                });
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

    @RequiresApi(api = Build.VERSION_CODES.N)
    private String getContent(JSONObject article) throws JSONException {
        String content = "";
        JSONObject currBody;
        JSONArray bodyArray = article.getJSONObject("blocks").getJSONArray("body");
        for( int i = 0 ; i < bodyArray.length() ; i++) {
            currBody = bodyArray.getJSONObject(i);
            content += Html.fromHtml(currBody.getString("bodyHtml"), Html.FROM_HTML_MODE_LEGACY);
        }
        return content;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private String getDate(String publicationDate) {
        ZoneId toTimeZone = ZoneId.of("America/Los_Angeles");
        ZonedDateTime publicationDateTime = Instant.parse(publicationDate).atZone(toTimeZone);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM yyyy");
        return publicationDateTime.format(formatter);
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
}
