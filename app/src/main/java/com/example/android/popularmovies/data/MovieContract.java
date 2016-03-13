package com.example.android.popularmovies.data;

import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;

/**
 * Created by partha.veerkar on 2/19/16.
 */
public class MovieContract {

    public static final String CONTENT_AUTHORITY = "com.example.android.popularmovies.app";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_MOVIES = "movies";
    public static final String PATH_POPULARITY = "popularity";
    public static final String PATH_RATING = "rating";
    public static final String PATH_BOOKMARKS = "bookmarks";
    public static final String PATH_REVIEWS = "reviews";
    public static final String PATH_VIDEOS = "videos";

    public static final class MoviesEntry implements BaseColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_MOVIES).build();

        public static final String TABLE_NAME = "movies";

        //Columns
        public static final String COLUMN_ID = "id";
        public static final String COLUMN_POSTER_PATH = "poster_path";
        public static final String COLUMN_OVERVIEW = "overview";
        public static final String COLUMN_RELEASE_DATE = "release_date";
        public static final String COLUMN_TITLE = "original_title";
        public static final String COLUMN_VOTE_AVERAGE = "vote_average";
        public static final String COLUMN_VIDEO = "video";

        public static Uri buildMoviesUriWithMovieId(Integer movieId) {
            return CONTENT_URI.buildUpon().appendPath(movieId.toString()).build();
        }

        public static int getMovieIdFromUri(Uri uri) {
            Log.d("test2", uri.toString());
            return Integer.parseInt(uri.getPathSegments().get(1));
        }
    }

    public static final class PopularityEntry implements BaseColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_POPULARITY).build();

        public static final String TABLE_NAME = "popularity";

        //Columns
        public static final String COLUMN_ID = "id";
    }

    public static final class RatingEntry implements BaseColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_RATING).build();

        public static final String TABLE_NAME = "rating";

        //Columns
        public static final String COLUMN_ID = "id";
    }

    public static final class BookmarksEntry implements BaseColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_BOOKMARKS).build();

        public static final String TABLE_NAME = "bookmarks";

        //Columns
        public static final String COLUMN_ID = "id";

        public static Uri buildInsertBookmarksUriWithMovieId(int movieId) {
            return CONTENT_URI.buildUpon().appendPath(Integer.toString(movieId)).build();
        }

        public static int getMovieIdFromUri(Uri uri) {
            return Integer.parseInt(uri.getPathSegments().get(1));
        }
    }

    public static final class ReviewsEntry implements BaseColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_REVIEWS).build();

        public static final String TABLE_NAME = "reviews";

        //Columns
        public static final String COLUMN_REVIEW_ID = "review_id";
        public static final String COLUMN_ID = "id";
        public static final String COLUMN_AUTHOR = "author";
        public static final String COLUMN_CONTENTS = "contents";

        public static Uri buildReviewsUriWithMovieId(int movieId) {
            return CONTENT_URI.buildUpon().appendPath(Integer.toString(movieId)).build();
        }

        public static int getMovieIdFromUri(Uri uri) {
            return Integer.parseInt(uri.getPathSegments().get(1));
        }
    }

    public static final class VideosEntry implements BaseColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_VIDEOS).build();

        public static final String TABLE_NAME = "videos";

        //Columns
        public static final String COLUMN_ID = "id";
        public static final String COLUMN_URL = "url";

        public static Uri buildVideosUriWithMovieId(int movieId) {
            return CONTENT_URI.buildUpon().appendPath(Integer.toString(movieId)).build();
        }

        public static int getMovieIdFromUri(Uri uri) {
            return Integer.parseInt(uri.getPathSegments().get(1));
        }
    }
}
