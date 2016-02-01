package com.example.android.popularmovies;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailActivityFragment extends Fragment {

    private static final String LOG_TAG = DetailActivityFragment.class.getSimpleName();
    private static final String IMAGE_URL = "http://image.tmdb.org/t/p/w500";

    public DetailActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

        try {
            JSONObject movieDetails = new JSONObject(getActivity().getIntent().getStringExtra(Intent.EXTRA_TEXT));
            ImageView moviePoster = (ImageView) rootView.findViewById(R.id.detailMoviePoster);
            Log.d(LOG_TAG, movieDetails.get("poster_path").toString());
            Picasso.with(getContext()).load(IMAGE_URL + movieDetails.getString("poster_path")).into(moviePoster);

            //description
            ((TextView) rootView.findViewById(R.id.detailDescription)).setText(movieDetails.getString("overview"));

            //title
            ((TextView) rootView.findViewById(R.id.detailTitle)).setText(movieDetails.getString("original_title"));

            //release date
            ((TextView) rootView.findViewById(R.id.detailReleaseDate)).setText(movieDetails.getString("release_date"));

            //vote count
            ((TextView) rootView.findViewById(R.id.detailVotes)).setText("User Rating: " + movieDetails.getString("vote_average"));

        }catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage());
        }
        return rootView;
    }
}
