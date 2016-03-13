package com.example.android.popularmovies;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.android.popularmovies.data.MovieContract;
import com.squareup.picasso.Picasso;

/**
 * Created by partha.veerkar on 2/22/16.
 */
public class MoviesGridAdapter extends CursorAdapter {

    private static final String IMAGE_URL = "http://image.tmdb.org/t/p/w185";

    public MoviesGridAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.grid_poster, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        ImageView imageView = (ImageView) view.findViewById(R.id.moviePoster);
        Picasso.with(context).load(IMAGE_URL + cursor.getString(cursor.getColumnIndex(MovieContract.MoviesEntry.COLUMN_POSTER_PATH)))
                .placeholder(R.drawable.noimage)
                .into(imageView);
    }
}
