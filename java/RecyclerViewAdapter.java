package com.example.newsapp;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;

import java.util.ArrayList;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>{

    private ArrayList<MainActivity.GuardianArticle> newsArticles;

    private Context context;


    public RecyclerViewAdapter(Context context, ArrayList<MainActivity.GuardianArticle> newsArticles) {
        this.context = context;
        this.newsArticles = newsArticles;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {

        final SharedPreferences preferences = context.getSharedPreferences("MODE_PRIVATE", 0);
        final SharedPreferences.Editor editor = preferences.edit();

        Glide.with(context).load(newsArticles.get(position).image).into(holder.itemImage);
        holder.itemTitle.setText(newsArticles.get(position).title);
        holder.itemTimeElapsed.setText(newsArticles.get(position).timeElapsed);
        holder.itemSectionName.setText(newsArticles.get(position).sectionName);

        if(preferences.contains(newsArticles.get(position).identifier)){
            holder.itemBookmark.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_bookmark_filled));
        }
        else{
            holder.itemBookmark.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_bookmark_border_black_24dp));
        }

        holder.itemBookmark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(preferences.contains(newsArticles.get(position).identifier)){
                    editor.remove(newsArticles.get(position).identifier);
                    holder.itemBookmark.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_bookmark_border_black_24dp));
                    Toast.makeText(context, '"' + newsArticles.get(position).title + "\" was removed from bookmarks", Toast.LENGTH_SHORT).show();
                }
                else{
                    Gson gson = new Gson();
                    String json = gson.toJson(newsArticles.get(position));
                    editor.putString(newsArticles.get(position).identifier, json);
                    holder.itemBookmark.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_bookmark_filled));
                    Toast.makeText(context, '"' + newsArticles.get(position).title + "\" was added to bookmarks", Toast.LENGTH_SHORT).show();
                }
                editor.commit();
            }
        });

        holder.recyclerViewLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                final Dialog dialog = new Dialog(context);
                dialog.setContentView(R.layout.dialog_box);
                ImageView dialogImageView = dialog.findViewById(R.id.dialog_image);
                TextView dialogTextView = dialog.findViewById(R.id.dialog_title);
                ImageView dialogTwitter = dialog.findViewById(R.id.dialog_twitter);
                Glide.with(context).load(newsArticles.get(position).image).into(dialogImageView);
                dialogTextView.setText(newsArticles.get(position).title);
                final ImageView dialogBookmark = dialog.findViewById(R.id.dialog_bookmark);;

                if(preferences.contains(newsArticles.get(position).identifier)){
                    dialogBookmark.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_bookmark_filled));
                }
                else{
                    dialogBookmark.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_bookmark_border_black_24dp));
                }

                dialogTwitter.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://twitter.com/intent/tweet/?text=Check out this Link: " + newsArticles.get(position).webUrl + "\n&hashtags=CSCI571NewsSearch")));
                    }
                });

                dialogBookmark.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if(preferences.contains(newsArticles.get(position).identifier)){
                            editor.remove(newsArticles.get(position).identifier);
                            dialogBookmark.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_bookmark_border_black_24dp));
                            holder.itemBookmark.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_bookmark_border_black_24dp));
                            Toast.makeText(context, '"' + newsArticles.get(position).title + "\" was removed from bookmarks", Toast.LENGTH_SHORT).show();
                        }
                        else{
                            Gson gson = new Gson();
                            String json = gson.toJson(newsArticles.get(position));
                            editor.putString(newsArticles.get(position).identifier, json);
                            dialogBookmark.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_bookmark_filled));
                            holder.itemBookmark.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_bookmark_filled));
                            Toast.makeText(context, '"' + newsArticles.get(position).title + "\" was added to bookmarks", Toast.LENGTH_SHORT).show();
                        }
                        editor.commit();
                    }
                });

                dialog.show();
                return true;
            }
        });

        holder.recyclerViewLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, DetailsActivity.class);
                intent.putExtra("identifier", newsArticles.get(position).identifier);
                intent.putExtra("title", newsArticles.get(position).title);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return newsArticles.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageView itemImage;
        ImageView itemBookmark;
        TextView itemTitle;
        TextView itemTimeElapsed;
        TextView itemSectionName;
        TextView itemSeparator;
        RelativeLayout recyclerViewLayout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            itemImage = itemView.findViewById(R.id.item_image);
            itemBookmark = itemView.findViewById(R.id.item_bookmark);
            itemTitle = itemView.findViewById(R.id.item_title);
            itemTimeElapsed = itemView.findViewById(R.id.item_time_elapsed);
            itemSectionName = itemView.findViewById(R.id.item_section_name);
            recyclerViewLayout = itemView.findViewById(R.id.recycler_view_layout);
            itemSeparator = itemView.findViewById(R.id.item_separator);
        }
    }
}
