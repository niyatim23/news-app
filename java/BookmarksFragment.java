package com.example.newsapp;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Map;

public class BookmarksFragment extends Fragment {
    private View rootView;
    private ArrayList<MainActivity.GuardianArticle> bookmarkedArticles = new ArrayList<>();
    private SharedPreferences preferences;
    private TextView itemNoBookmarks;
    private RecyclerView recyclerView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.bookmarks_fragment, container, false);
        itemNoBookmarks = rootView.findViewById(R.id.no_bookmarks_text);
        recyclerView = rootView.findViewById(R.id.recycler_view_bookmarks);

        DividerItemDecoration Hdivider = new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL);
        Hdivider.setDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.divider));
        recyclerView.addItemDecoration(Hdivider);


        preferences = getActivity().getSharedPreferences("MODE_PRIVATE", 0);
        Gson gson = new Gson();
        Map<String, ?> keys = preferences.getAll();
        bookmarkedArticles.clear();
        for(Map.Entry<String,?> entry : keys.entrySet()) {
            bookmarkedArticles.add(gson.fromJson(preferences.getString(entry.getKey(), ""), MainActivity.GuardianArticle.class));
        }
        if(bookmarkedArticles.size() > 0) {

            RecyclerViewAdapterBookmark adapter = new RecyclerViewAdapterBookmark(getActivity(), bookmarkedArticles);
            recyclerView.setAdapter(adapter);
            recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 2));
            recyclerView.addItemDecoration(Hdivider);
            itemNoBookmarks.setText("");
        }
        else {
            itemNoBookmarks.setText("No Bookmarked Articles");
        }
        return rootView;
    }



    @Override
    public void onResume() {
        super.onResume();
        itemNoBookmarks = rootView.findViewById(R.id.no_bookmarks_text);
        preferences = getActivity().getSharedPreferences("MODE_PRIVATE", 0);
        Gson gson = new Gson();
        Map<String, ?> keys = preferences.getAll();
        bookmarkedArticles.clear();
        for(Map.Entry<String,?> entry : keys.entrySet()) {
            bookmarkedArticles.add(gson.fromJson(preferences.getString(entry.getKey(), ""), MainActivity.GuardianArticle.class));
        }
        if(bookmarkedArticles.size() > 0) {
            itemNoBookmarks.setText("");
            RecyclerViewAdapterBookmark adapter = new RecyclerViewAdapterBookmark(getActivity(), bookmarkedArticles);
            recyclerView.setAdapter(adapter);
        }
        else {
            itemNoBookmarks.setText("No Bookmarked Articles");
        }
    }
}
