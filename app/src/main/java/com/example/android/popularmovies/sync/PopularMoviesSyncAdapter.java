package com.example.android.popularmovies.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SyncRequest;
import android.content.SyncResult;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import com.example.android.popularmovies.BuildConfig;
import com.example.android.popularmovies.R;
import com.example.android.popularmovies.data.MovieContract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Vector;

/**
 * Created by partha.veerkar on 2/24/16.
 */
public class PopularMoviesSyncAdapter extends AbstractThreadedSyncAdapter {

    private static final String SORT_POPULAR = "popularity.desc";
    private static final String SORT_RATING = "vote_average.desc";
    private static final String MANUAL_SYNC = "immediate_sync";

    private static final String LOG_TAG = PopularMoviesSyncAdapter.class.getSimpleName();

    private static final String API_URL = "http://api.themoviedb.org/3/discover/movie";
    private static final String ADDITIONAL_DATA_PARAMETER = "append_to_response";
    private static final String ADDITIONAL_DATA_VALUE = "videos,reviews";
    private static final String MOVIE_API_URL = "http://api.themoviedb.org/3/movie";
    private static final String YOUTUBE_URL_PREFIX = "http://www.youtube.com/watch?v=";
    private static final String VOTE_COUNT_GREATER_THAN = "vote_count.gte";
    private static final String VOTE_COUNT_LIMIT = "1000";

    // sync intervals
    public static final int SYNC_INTERVAL = 60 * 180;
    public static final int SYNC_FLEXTIME = SYNC_INTERVAL/3;

    private String moviesInformation;
    private ArrayList<String> mInsertedMovies = new ArrayList<String>();

    public PopularMoviesSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
         SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
         String preference = prefs.getString(getContext().getString(R.string.pref_sort_key),
                 getContext().getString(R.string.pref_sort_values_popularity));
        Boolean manualSync = extras.getBoolean(MANUAL_SYNC);
        if(preference.equals(getContext().getString(R.string.pref_sort_values_popularity))) {
            updateDatabase(account, extras, authority, provider, syncResult, getContext().getString(R.string.pref_sort_values_popularity));
            if(!manualSync) {
                updateDatabase(account, extras, authority, provider, syncResult, getContext().getString(R.string.pref_sort_values_rating));
                updateBookmarks();
                cleanUp();
            }
        } else if(preference.equals(getContext().getString(R.string.pref_sort_values_rating))) {
            updateDatabase(account, extras, authority, provider, syncResult, getContext().getString(R.string.pref_sort_values_rating));
            if(!manualSync) {
                updateDatabase(account, extras, authority, provider, syncResult, getContext().getString(R.string.pref_sort_values_popularity));
                updateBookmarks();
                cleanUp();
            }
        } else {
            updateBookmarks();
            if(!manualSync) {
                updateDatabase(account, extras, authority, provider, syncResult, getContext().getString(R.string.pref_sort_values_rating));
                updateDatabase(account, extras, authority, provider, syncResult, getContext().getString(R.string.pref_sort_values_popularity));
                cleanUp();
            }
        }
    }

    private void cleanUp() {
        if(mInsertedMovies == null) {
            return;
        }
        Uri requestUri = MovieContract.MoviesEntry.CONTENT_URI;
        int deletedRows = getContext().getContentResolver().delete(requestUri,
                MovieContract.MoviesEntry.COLUMN_ID + " != ? ",
                mInsertedMovies.toArray(new String[mInsertedMovies.size()])
                );
    }

    private void updateBookmarks() {
        Uri requestUri = MovieContract.BookmarksEntry.CONTENT_URI;
        Cursor movieCursor = getContext().getContentResolver().query(requestUri, null, null, null, null);
        int idColumn = movieCursor.getColumnIndex(MovieContract.BookmarksEntry.COLUMN_ID);
        HttpURLConnection urlConnection;
        BufferedReader reader;
        Uri movieUri;
        URL url;
        String id;
        JSONObject movie;
        Vector<ContentValues> cVVectorMovies = new Vector<ContentValues>(movieCursor.getCount());

        while (movieCursor.moveToNext()) {

            movieUri = Uri.parse(MOVIE_API_URL).buildUpon()
                    .appendPath(movieCursor.getString(idColumn))
                    .appendQueryParameter("api_key", BuildConfig.API_KEY).build();

            try {
                url = new URL(movieUri.toString());

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    return;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line);
                }
                if (buffer.length() == 0) {
                    return;
                }

                movie = new JSONObject(buffer.toString());
                id = movie.getString("id");

                mInsertedMovies.add(id);
                ContentValues movieContent = new ContentValues();

                movieContent.put(MovieContract.MoviesEntry.COLUMN_ID, id);
                movieContent.put(MovieContract.MoviesEntry.COLUMN_POSTER_PATH , movie.getString("poster_path"));
                movieContent.put(MovieContract.MoviesEntry.COLUMN_OVERVIEW, movie.getString("overview"));
                movieContent.put(MovieContract.MoviesEntry.COLUMN_RELEASE_DATE, movie.getString("release_date"));
                movieContent.put(MovieContract.MoviesEntry.COLUMN_TITLE, movie.getString("original_title"));
                movieContent.put(MovieContract.MoviesEntry.COLUMN_VOTE_AVERAGE, movie.getString("vote_average"));

                cVVectorMovies.add(movieContent);
                updateAdditionalInformation(id);
            } catch (Exception e) {
                Log.d(LOG_TAG, e.getMessage());
            }

        }
        movieCursor.close();

        // add movies to database
        if ( cVVectorMovies.size() > 0) {
            ContentValues[] values = new ContentValues[cVVectorMovies.size()];
            cVVectorMovies.toArray(values);
            int results = getContext().getContentResolver().bulkInsert(
                    MovieContract.MoviesEntry.CONTENT_URI,
                    values
            );
        }
    }

    private void updateDatabase(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult, String sortOption) {
        Uri.Builder uriBuilder = Uri.parse(API_URL).buildUpon()
                .appendQueryParameter("api_key", BuildConfig.API_KEY);

        String sortOrder = SORT_POPULAR;
        Uri rankingUri = MovieContract.PopularityEntry.CONTENT_URI;

        if(sortOption.equals(getContext().getString(R.string.pref_sort_values_rating))) {
            sortOrder = SORT_RATING;
            uriBuilder.appendQueryParameter(VOTE_COUNT_GREATER_THAN, VOTE_COUNT_LIMIT);
            rankingUri = MovieContract.RatingEntry.CONTENT_URI;
        }
        Uri uri = uriBuilder.appendQueryParameter("sort_by", sortOrder).build();

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
                return;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line);
            }
            if (buffer.length() == 0) {
                return;
            }

            moviesInformation = buffer.toString();
            JSONArray moviesInformationList =  getMoviesList(moviesInformation);
            String id;
            String poster_path;
            String overview;
            String release_date;
            String original_title;
            String vote_average;
            String video;
            String trailer;

            Vector<ContentValues> cVVectorMovies = new Vector<ContentValues>(moviesInformationList.length());

            for(int i = 0; i < moviesInformationList.length(); i++) {

                JSONObject movie = moviesInformationList.getJSONObject(i);
                id = movie.getString("id");
                poster_path = movie.getString("poster_path");
                overview = movie.getString("overview");
                release_date = movie.getString("release_date");
                original_title = movie.getString("original_title");
                vote_average = movie.getString("vote_average");
                video = movie.getString("video");

                ContentValues movieContent = new ContentValues();
                mInsertedMovies.add(id);

                movieContent.put(MovieContract.MoviesEntry.COLUMN_ID, id);
                movieContent.put(MovieContract.MoviesEntry.COLUMN_POSTER_PATH , poster_path);
                movieContent.put(MovieContract.MoviesEntry.COLUMN_OVERVIEW, overview);
                movieContent.put(MovieContract.MoviesEntry.COLUMN_RELEASE_DATE, release_date);
                movieContent.put(MovieContract.MoviesEntry.COLUMN_TITLE, original_title);
                movieContent.put(MovieContract.MoviesEntry.COLUMN_VOTE_AVERAGE, vote_average);

                movieContent.put(MovieContract.MoviesEntry.COLUMN_VIDEO, video);

                cVVectorMovies.add(movieContent);

                updateAdditionalInformation(id);
            }

            // add movies to database
            if ( cVVectorMovies.size() > 0) {
                ContentValues[] values = new ContentValues[cVVectorMovies.size()];
                cVVectorMovies.toArray(values);
                int results = getContext().getContentResolver().bulkInsert(
                        rankingUri,
                        values
                );
            }

            //return moviesInformationList;
        } catch (Exception e) {
            Log.d(LOG_TAG, e.getMessage());
        }
        return;
    }

    private void updateAdditionalInformation(String movieId) {

        Uri uri = Uri.parse(MOVIE_API_URL).buildUpon()
                .appendPath(movieId)
                .appendQueryParameter("api_key", BuildConfig.API_KEY)
                .appendQueryParameter(ADDITIONAL_DATA_PARAMETER, ADDITIONAL_DATA_VALUE)
                .build();

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
                return;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line);
            }
            if (buffer.length() == 0) {
                return;
            }

            JSONObject movieInformation = new JSONObject(buffer.toString());

            // inserting reviews in db
            insertReviewIntoDatabase(movieInformation.getJSONObject("reviews").getJSONArray("results"), movieId);

            // inserting videos in db
            insertVideosIntoDatabase(movieInformation.getJSONObject("videos").getJSONArray("results"), movieId);

        } catch (Exception e) {
            Log.d(LOG_TAG, e.getMessage());
        }
    }

    private void insertVideosIntoDatabase(JSONArray videos, String movieId) throws JSONException{

        Vector<ContentValues> cVVectorReviews = new Vector<ContentValues>(videos.length());
        JSONObject trailer;

        for(int i = 0; i < videos.length(); i++) {
            trailer = videos.getJSONObject(i);
            ContentValues videoContent = new ContentValues();

            if(null != trailer && trailer.getString("site").equals("YouTube")) {
                videoContent.put(MovieContract.VideosEntry.COLUMN_ID, movieId);
                videoContent.put(MovieContract.VideosEntry.COLUMN_URL, YOUTUBE_URL_PREFIX + trailer.getString("key"));
                cVVectorReviews.add(videoContent);
            }
        }

        // add to database
        if ( cVVectorReviews.size() > 0) {
            ContentValues[] values = new ContentValues[cVVectorReviews.size()];
            cVVectorReviews.toArray(values);
            int results = getContext().getContentResolver().bulkInsert(
                    MovieContract.VideosEntry.CONTENT_URI,
                    values
            );
        }
    }

    private void insertReviewIntoDatabase(JSONArray reviews, String movieId) throws JSONException{

        Vector<ContentValues> cVVectorReviews = new Vector<ContentValues>(reviews.length());
        String author;
        String content;
        String review_id;

        for(int i = 0; i < reviews.length(); i++) {

            JSONObject review = reviews.getJSONObject(i);
            review_id = review.getString("id");
            author = review.getString("author");
            content = review.getString("content");

            ContentValues reviewContent = new ContentValues();

            reviewContent.put(MovieContract.ReviewsEntry.COLUMN_REVIEW_ID, review_id);
            reviewContent.put(MovieContract.ReviewsEntry.COLUMN_ID, movieId);
            reviewContent.put(MovieContract.ReviewsEntry.COLUMN_AUTHOR , author);
            reviewContent.put(MovieContract.ReviewsEntry.COLUMN_CONTENTS, content);

            cVVectorReviews.add(reviewContent);
        }

        // add to database
        if ( cVVectorReviews.size() > 0) {
            ContentValues[] values = new ContentValues[cVVectorReviews.size()];
            cVVectorReviews.toArray(values);
            int results = getContext().getContentResolver().bulkInsert(
                    MovieContract.ReviewsEntry.CONTENT_URI,
                    values
            );
        }
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

    /**
     * Helper method to have the sync adapter sync immediately
     * @param context The context used to access the account service
     */
    public static void syncImmediately(Context context) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        bundle.putBoolean(MANUAL_SYNC, true);
        ContentResolver.requestSync(getSyncAccount(context),
                context.getString(R.string.content_authority), bundle);
    }

    /**
     * Helper method to get the fake account to be used with SyncAdapter, or make a new one
     * if the fake account doesn't exist yet.  If we make a new account, we call the
     * onAccountCreated method so we can initialize things.
     *
     * @param context The context used to access the account service
     * @return a fake account.
     */
    public static Account getSyncAccount(Context context) {
        // Get an instance of the Android account manager
        AccountManager accountManager =
                (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);

        // Create the account type and default account
        Account newAccount = new Account(
                context.getString(R.string.app_name), context.getString(R.string.sync_account_type));

        // If the password doesn't exist, the account doesn't exist
        if ( null == accountManager.getPassword(newAccount) ) {
            if (!accountManager.addAccountExplicitly(newAccount, "", null)) {
                return null;
            }
            onAccountCreated(newAccount, context);
        }
        return newAccount;
    }

    /**
     * Helper method to schedule the sync adapter periodic execution
     */
    public static void configurePeriodicSync(Context context, int syncInterval, int flexTime) {
        Account account = getSyncAccount(context);
        String authority = context.getString(R.string.content_authority);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // we can enable inexact timers in our periodic sync
            SyncRequest request = new SyncRequest.Builder().
                    syncPeriodic(syncInterval, flexTime).
                    setSyncAdapter(account, authority).
                    setExtras(new Bundle()).build();
            ContentResolver.requestSync(request);
        } else {
            ContentResolver.addPeriodicSync(account,
                    authority, new Bundle(), syncInterval);
        }
    }

    private static void onAccountCreated(Account newAccount, Context context) {
        PopularMoviesSyncAdapter.configurePeriodicSync(context, SYNC_INTERVAL, SYNC_FLEXTIME);
        ContentResolver.setSyncAutomatically(newAccount, context.getString(R.string.content_authority), true);
        syncImmediately(context);
    }

    public static void initializeSyncAdapter(Context context) {
        getSyncAccount(context);
    }
}
