package com.example.android.popularmovies;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.android.popularmovies.data.MovieContract;
import com.example.android.popularmovies.sync.PopularMoviesSyncAdapter;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int MY_LOADER_ID = 1;
    private static final String SELECTED_KEY = "selected_position";

    private GridView mPosterGrid;
    private int mPosition = GridView.INVALID_POSITION;
    private MoviesGridAdapter mMoviesGridAdapter;
    private TextView mNoMoviesMessage;

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
        PopularMoviesSyncAdapter.syncImmediately(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        mPosterGrid = (GridView) rootView.findViewById(R.id.moviePosterGrid);
        mNoMoviesMessage = (TextView) rootView.findViewById(R.id.noMoviesMessage);

        mMoviesGridAdapter = new MoviesGridAdapter(getActivity(), null, 0);

        mPosterGrid.setAdapter(mMoviesGridAdapter);

        mPosterGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView adapterView, View view, int position, long id) {
                try {
                    Cursor cursor = (Cursor) adapterView.getItemAtPosition(position);
                    if (cursor != null) {
                        ((Callback) getActivity()).onItemSelected(MovieContract.MoviesEntry.buildMoviesUriWithMovieId(cursor.getInt(1)));
                    }
                    mPosition = position;
                } catch (Exception e) {
                    Log.e(LOG_TAG, e.getMessage());
                }
            }
        });
        if (savedInstanceState != null && savedInstanceState.containsKey(SELECTED_KEY)) {
            mPosition = savedInstanceState.getInt(SELECTED_KEY);
        }
        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        updateMoviesList();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        String sortPreference = PreferenceManager.getDefaultSharedPreferences(getActivity()).getString(getString(R.string.pref_sort_key), getString(R.string.pref_sort_values_popularity));
        Uri requestUri;
        if (sortPreference.equals(getString(R.string.pref_sort_values_popularity))) {
            requestUri = MovieContract.PopularityEntry.CONTENT_URI;
        } else if (sortPreference.equals(getString(R.string.pref_sort_values_rating))) {
            requestUri = MovieContract.RatingEntry.CONTENT_URI;
        } else if (sortPreference.equals(getString(R.string.pref_sort_values_bookmarked))) {
            requestUri = MovieContract.BookmarksEntry.CONTENT_URI;
        } else {
            return null;
        }

        return new CursorLoader(getActivity(),
                requestUri,
                null,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        String sortPref = PreferenceManager.getDefaultSharedPreferences(getActivity()).getString(getString(R.string.pref_sort_key), getString(R.string.pref_sort_values_popularity));
        if(data.getCount() < 1) {
            if(sortPref.equals(getString(R.string.pref_sort_values_bookmarked))) {
                mNoMoviesMessage.setText(R.string.no_favorites_message);
            } else {
                mNoMoviesMessage.setText(R.string.no_movies_message);
            }
            mNoMoviesMessage.setVisibility(View.VISIBLE);
        } else {
            mNoMoviesMessage.setVisibility(View.GONE);
        }
        mMoviesGridAdapter.swapCursor(data);
        if (mPosition != ListView.INVALID_POSITION) {
            mPosterGrid.smoothScrollToPosition(mPosition);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mMoviesGridAdapter.swapCursor(null);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(MY_LOADER_ID, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    public void onSortingChanged() {
       updateMoviesList();
       getLoaderManager().restartLoader(MY_LOADER_ID, null, this);
    }

    public interface Callback {
        public void onItemSelected(Uri dataUri);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (mPosition != ListView.INVALID_POSITION) {
            outState.putInt(SELECTED_KEY, mPosition);
        }
        super.onSaveInstanceState(outState);
    }
}