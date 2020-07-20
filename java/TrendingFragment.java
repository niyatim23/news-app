package com.example.newsapp;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class TrendingFragment extends Fragment {

    private View rootView;
    private EditText editText;
    private LineChart lineChart;
    private List<Entry> plotCoordinates = new ArrayList<>();
    private Legend legend;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.trending_fragment , container, false);
        MainActivity.spinner.setVisibility(View.GONE);
        MainActivity.spinnerText.setText("");
        editText = rootView.findViewById(R.id.edittext);
        editText.setHint(R.string.hint);
        lineChart = rootView.findViewById(R.id.line_chart);
        legend = lineChart.getLegend();

        legend.setTextSize(15);
        legend.setFormSize(16);

        getChartData("Coronavirus");
        editText.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
                        (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    getChartData(editText.getText().toString().trim());
                    return true;
                }
                return false;
            }
        });
        return rootView;
    }

    public void getChartData(final String keyword){
        plotCoordinates.clear();
        final List<Integer> colors = new ArrayList<>();
        RequestQueue queue = Volley.newRequestQueue(getActivity());
        String url ="https://newsandroidserver.wl.r.appspot.com/get_trends/" + keyword.toLowerCase();
        JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @RequiresApi(api = Build.VERSION_CODES.O)
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            if (response.has("default") && response.getJSONObject("default").has("timelineData")) {
                                JSONArray jsonArray = response.getJSONObject("default").getJSONArray("timelineData");
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    plotCoordinates.add(new Entry(i, Integer.parseInt(jsonArray.getJSONObject(i).getJSONArray("value").getString(0))));
                                    colors.add(R.color.colorPrimary);
                                }
                                LineDataSet setComp1 = new LineDataSet(plotCoordinates, "Trending Chart for " + keyword);


                                setComp1.setCircleColor(getActivity().getColor(R.color.colorPrimary));
                                setComp1.setCircleHoleColor(getActivity().getColor(R.color.colorPrimary));
                                setComp1.setColor(getActivity().getColor(R.color.colorPrimary)); //plot line color

                                setComp1.setAxisDependency(YAxis.AxisDependency.LEFT);
                                List<ILineDataSet> dataSets = new ArrayList<>();
                                dataSets.add(setComp1);
                                LineData data = new LineData(dataSets);
                                lineChart.setData(data);
                                lineChart.getXAxis().setDrawGridLines(false);
                                lineChart.getAxisLeft().setDrawGridLines(false);
                                lineChart.getAxisLeft().setDrawAxisLine(false);
                                lineChart.getAxisRight().setDrawGridLines(false);
                                lineChart.invalidate();
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
}
