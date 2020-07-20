package com.example.newsapp;

import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
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
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.Manifest;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.IOException;
import java.security.GuardedObject;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HomeFragment extends Fragment implements LocationListener {

    private ArrayList<MainActivity.GuardianArticle> homeArticles = new ArrayList<>();
    private LocationManager locationManager;
    private String provider;
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    private View rootView;
    private RecyclerView recyclerView;
    private RecyclerViewAdapter adapter;
    private SwipeRefreshLayout homeSwipeRefreshLayout;
    private String cityName = "Los Angeles";
    private String stateName = "California";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.home_fragment, container, false);

        MainActivity.spinner.setVisibility(View.VISIBLE);
        MainActivity.spinnerText.setText(R.string.progress_text);
        homeSwipeRefreshLayout = rootView.findViewById(R.id.home_swiperefresh_items);
        recyclerView = rootView.findViewById(R.id.recycler_view_home);

        homeSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                homeArticles.clear();
                adapter = new RecyclerViewAdapter(getActivity(), homeArticles);
                recyclerView.setAdapter(adapter);
                recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
                getData();
                getWeatherData(cityName, stateName);

                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if(homeSwipeRefreshLayout.isRefreshing()) {
                            homeSwipeRefreshLayout.setRefreshing(false);
                        }
                    }
                }, 1000);
            }
        });

        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        provider = locationManager.getBestProvider(new Criteria(), false);
        checkLocationPermission();
        return rootView;
    }




    public boolean checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(getActivity())
                        .setTitle("Requesting Location Permission")
                        .setMessage("We need the permission to display the weather at your current location. Please grant us the permission.")
                        .setPositiveButton("Grant Permission", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_LOCATION);
                            }
                        })
                        .create()
                        .show();


            } else {
                // No explanation needed, we can request the permission.
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            getData();
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        provider = locationManager.getBestProvider(new Criteria(), false);

        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        //Request location updates:
                        locationManager.requestLocationUpdates(provider, 400, 1, this);
                    }

                } else {

                }
                getData();
                return;
            }

        }
    }


    private void getWeatherData(final String cityName, final String stateName) {
        RequestQueue queue = Volley.newRequestQueue(getActivity());
        String url ="https://api.openweathermap.org/data/2.5/weather?q="+ cityName + "&units=metric&appid=03e1381e020532aa1619fb94d00880cc";
        JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        String temperature = "";
                        String weatherSummary = "";
                        try {
                            if(response.has("main") && response.getJSONObject("main").has("temp")){
                                temperature = Integer.toString(Math.round(Float.parseFloat(response.getJSONObject("main").getString("temp"))));
                                temperature += " °C";
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        try {
                            if(response.has("weather")){
                                weatherSummary = response.getJSONArray("weather").getJSONObject(0).getString("main");
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        int image;

                        if(weatherSummary.equals("Clouds")) {
                            image = R.drawable.cloudy_weather;
                        }
                        else if (weatherSummary.equals("Clear")) {
                            image = R.drawable.clear_weather;
                        }
                        else if(weatherSummary.equals("Snow")) {
                            image = R.drawable.snowy_weather;
                        }
                        else if(weatherSummary.equals("Rain / Drizzle") || weatherSummary.equals("Rain") || weatherSummary.equals("Drizzle")) {
                            image = R.drawable.rainy_weather;
                        }
                        else if(weatherSummary.equals("Thunderstorm")) {
                            image = R.drawable.thunder_weather;
                        }
                        else {
                            image = R.drawable.sunny_weather;
                        }

                        TextView temperatureView = rootView.findViewById(R.id.temperature);
                        TextView cityView = rootView.findViewById(R.id.city);
                        TextView stateView = rootView.findViewById(R.id.state);
                        TextView summaryView = rootView.findViewById(R.id.weather_summary);
                        View weatherLayout = rootView.findViewById(R.id.weather_rel_layout);

                        weatherLayout.setBackgroundResource(image);
                        temperatureView.setText(temperature);
                        summaryView.setText(weatherSummary);
                        cityView.setText(cityName);
                        stateView.setText(stateName);

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                });
        queue.add(jsonRequest);
    }



    private void getData(){
        homeArticles.clear();
        RequestQueue queue = Volley.newRequestQueue(getActivity());
        String url ="https://newsandroidserver.wl.r.appspot.com/section_news/guardian/home";
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
                                    homeArticles.add(new MainActivity.GuardianArticle(getImageFromJson(article), article.getString("webTitle"),
                                            article.getString("webUrl"), getTime(article.getString("webPublicationDate")),
                                            article.getString("sectionName"),article.getString("id"), article.getString("webPublicationDate")));
                                }
                                adapter = new RecyclerViewAdapter(getActivity(), homeArticles);
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
            image = article.getJSONObject("fields").getString("thumbnail");
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
    public void onLocationChanged(Location location) {
        Geocoder geocoder = new Geocoder(getActivity(), Locale.getDefault());
        List<Address> addresses;
        try {
            addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            cityName = addresses.get(0).getLocality();
            stateName = addresses.get(0).getAdminArea();
            getWeatherData(cityName, stateName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onResume() {
        super.onResume();
        provider = locationManager.getBestProvider(new Criteria(), false);
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(provider, 400, 1, this);
        }
        try {
            adapter.notifyDataSetChanged();
        }
        catch(Exception ignored) {

        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.removeUpdates(this);
        }
    }

}

