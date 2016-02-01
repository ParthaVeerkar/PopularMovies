package com.example.android.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {

    private static final String SORT_POPULAR = "popularity.desc";
    private static final String SORT_RATING = "vote_average.desc";

    private int mPosterWidth;
    private int mPosterHeight;

    private JSONArray mMovieList;
    private CustomArrayAdapter mCustomArrayAdapter;

    private static final String LOG_TAG = MainActivityFragment.class.getSimpleName();

    public MainActivityFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
       inflater.inflate(R.menu.menu_filter, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.action_filter) {
            startActivity(new Intent(getActivity(), FilterSettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateMoviesList() {
        // sending the sort preference to FetchMoviesTask and starting it
        new FetchMoviesTask().execute(PreferenceManager.getDefaultSharedPreferences(getActivity()).getString(getString(R.string.pref_sort_key), getString(R.string.pref_sort_values_default)));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        GridView posterGrid = (GridView) rootView.findViewById(R.id.moviePosterGrid);

        // getting size of the screen
        DisplayMetrics displaymetrics = new DisplayMetrics();
        getActivity().getWindowManager()
                .getDefaultDisplay()
                .getMetrics(displaymetrics);

        // setting poster size based on standard poster size and the screen size
        mPosterWidth = (int) (displaymetrics.widthPixels / 3) - 4;
        mPosterHeight = (int) (mPosterWidth/0.66);
        posterGrid.setColumnWidth(mPosterWidth);

        mCustomArrayAdapter = new CustomArrayAdapter(getActivity(),
                R.layout.grid_poster,
                R.id.moviePoster);
        posterGrid.setAdapter(mCustomArrayAdapter);

        posterGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                try {
                    Intent detailIntent = new Intent(getActivity(), DetailActivity.class).putExtra(Intent.EXTRA_TEXT, mMovieList.getJSONObject(position).toString());
                    startActivity(detailIntent);
                } catch (JSONException e) {
                    Log.e(LOG_TAG, e.getMessage());
                }
            }
        });
        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        updateMoviesList();
    }

    public class CustomArrayAdapter extends ArrayAdapter {

        public CustomArrayAdapter(Context context, int resource, int textViewResourceId, List objects) {
            super(context, resource, textViewResourceId, objects);
        }

        public CustomArrayAdapter(Context context, int resource) {
            super(context, resource);
        }

        public CustomArrayAdapter(Context context, int resource, int textViewResourceId) {
            super(context, resource, textViewResourceId);
        }

        public CustomArrayAdapter(Context context, int resource, Object[] objects) {
            super(context, resource, objects);
        }

        public CustomArrayAdapter(Context context, int resource, int textViewResourceId, Object[] objects) {
            super(context, resource, textViewResourceId, objects);
        }

        public CustomArrayAdapter(Context context, int resource, List objects) {
            super(context, resource, objects);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.grid_poster, parent, false);
                convertView.getLayoutParams().height = mPosterHeight;
                convertView.getLayoutParams().width = mPosterWidth;
            }
            String url = (String) getItem(position);
            Picasso.with(getContext()).load(url)
                    .into((ImageView) convertView);
            return convertView;
        }
    }

    public class FetchMoviesTask extends AsyncTask<String, Void, JSONArray> {

        private final String LOG_TAG = FetchMoviesTask.class.getSimpleName();
        private String moviesInformation;

        // INSERT API KEY HERE
        private static final String API_KEY = "";  // API KEY
        private static final String API_URL = "http://api.themoviedb.org/3/discover/movie";
        private static final String IMAGE_URL = "http://image.tmdb.org/t/p/w185";

        protected JSONArray doInBackground(String... params) {

            String sortOrder = SORT_POPULAR;

            if(params[0].equals(getString(R.string.pref_sort_values_rating))) {
                sortOrder = SORT_RATING;
            }

            Uri uri = Uri.parse(API_URL).buildUpon()
                    .appendQueryParameter("sort_by", sortOrder)
                    .appendQueryParameter("api_key", API_KEY).build();

            HttpURLConnection urlConnection;
            BufferedReader reader;
            try {
                URL url = new URL(uri.toString());

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line);
                }
                if (buffer.length() == 0) {
                    return null;
                }

                moviesInformation = buffer.toString();
                return getMoviesList(moviesInformation);
            } catch (Exception e) {
                Log.d(LOG_TAG, e.getMessage());
            }
            return null;
        }

        private JSONArray getMoviesList(String moviesInformation) {
            try {
                JSONObject moviesInformationList = new JSONObject(moviesInformation);
                return moviesInformationList.getJSONArray("results");
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(JSONArray results) {

            if (results == null) {
                return;
            }
            mMovieList = results;
            mCustomArrayAdapter.clear();
            try {
                for (int i = 0; i < results.length(); i++) {
                    mCustomArrayAdapter.add(IMAGE_URL + results.getJSONObject(i).get("poster_path"));
                }
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage());
            }
        }
    }
}