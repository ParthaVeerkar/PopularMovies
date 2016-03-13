package com.example.android.popularmovies.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.Nullable;

/**
 * Created by partha.veerkar on 2/19/16.
 */
public class MoviesProvider extends ContentProvider {

    // The URI Matcher used by this content provider.
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private MoviesDbHelper mOpenHelper;

    static final int MOVIES = 0;
    static final int MOVIES_RATING = 100;
    static final int MOVIES_POPULARITY = 200;
    static final int BOOKMARKS = 300;
    static final int BOOKMARKS_WITH_ID = 400;
    static final int REVIEWS = 500;
    static final int REVIEWS_WITH_ID = 600;
    static final int VIDEOS = 700;
    static final int VIDEOS_WITH_ID = 800;

    private static final SQLiteQueryBuilder mMoviesQueryBuider = new SQLiteQueryBuilder();
    private static final SQLiteQueryBuilder mPopularMoviesQueryBuider = new SQLiteQueryBuilder();
    private static final SQLiteQueryBuilder mHighestRatedMoviesQueryBuider = new SQLiteQueryBuilder();
    private static final SQLiteQueryBuilder mBookmarksQueryBuider = new SQLiteQueryBuilder();
    private static final SQLiteQueryBuilder mReviewsQueryBuider = new SQLiteQueryBuilder();
    private static final SQLiteQueryBuilder mVideosQueryBuider = new SQLiteQueryBuilder();

    static {

        mMoviesQueryBuider.setTables(MovieContract.MoviesEntry.TABLE_NAME);

        mPopularMoviesQueryBuider.setTables(
                MovieContract.MoviesEntry.TABLE_NAME + " INNER JOIN " +
                        MovieContract.PopularityEntry.TABLE_NAME +
                        " on " + MovieContract.MoviesEntry.TABLE_NAME +
                        "." +  MovieContract.MoviesEntry.COLUMN_ID +
                        " = " + MovieContract.PopularityEntry.TABLE_NAME +
                        "." + MovieContract.PopularityEntry.COLUMN_ID
        );

        mHighestRatedMoviesQueryBuider.setTables(
                MovieContract.MoviesEntry.TABLE_NAME + " INNER JOIN " +
                        MovieContract.RatingEntry.TABLE_NAME +
                        " on " + MovieContract.MoviesEntry.TABLE_NAME +
                        "." +  MovieContract.MoviesEntry.COLUMN_ID +
                        " = " + MovieContract.RatingEntry.TABLE_NAME +
                        "." + MovieContract.RatingEntry.COLUMN_ID
        );

        mBookmarksQueryBuider.setTables(
                MovieContract.MoviesEntry.TABLE_NAME + " INNER JOIN " +
                        MovieContract.BookmarksEntry.TABLE_NAME +
                        " on " + MovieContract.MoviesEntry.TABLE_NAME +
                        "." +  MovieContract.MoviesEntry.COLUMN_ID +
                        " = " + MovieContract.BookmarksEntry.TABLE_NAME +
                        "." + MovieContract.BookmarksEntry.COLUMN_ID
        );

        mReviewsQueryBuider.setTables(
                MovieContract.MoviesEntry.TABLE_NAME + " INNER JOIN " +
                        MovieContract.ReviewsEntry.TABLE_NAME +
                        " on " + MovieContract.MoviesEntry.TABLE_NAME +
                        "." +  MovieContract.MoviesEntry.COLUMN_ID +
                        " = " + MovieContract.ReviewsEntry.TABLE_NAME +
                        "." + MovieContract.ReviewsEntry.COLUMN_ID
        );

        mVideosQueryBuider.setTables(
                MovieContract.MoviesEntry.TABLE_NAME + " INNER JOIN " +
                        MovieContract.VideosEntry.TABLE_NAME +
                        " on " + MovieContract.MoviesEntry.TABLE_NAME +
                        "." +  MovieContract.MoviesEntry.COLUMN_ID +
                        " = " + MovieContract.VideosEntry.TABLE_NAME +
                        "." + MovieContract.VideosEntry.COLUMN_ID
        );
    }
    @Override
    public boolean onCreate() {
        mOpenHelper = new MoviesDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        Cursor cursor;
        switch (sUriMatcher.match(uri)) {

            case MOVIES:
            {
                int movieId = MovieContract.MoviesEntry.getMovieIdFromUri(uri);
                cursor = mMoviesQueryBuider.query(mOpenHelper.getReadableDatabase(),
                        null,
                        MovieContract.MoviesEntry.COLUMN_ID + " = ? ",
                        new String[] { Integer.toString(movieId) },
                        null,
                        null,
                        null
                );
                break;
            }

            case MOVIES_POPULARITY:
            {
                cursor = mPopularMoviesQueryBuider.query(mOpenHelper.getReadableDatabase(),
                        null,
                        null,
                        null,
                        null,
                        null,
                        null
                );
                break;
            }
            case MOVIES_RATING:
            {
                cursor = mHighestRatedMoviesQueryBuider.query(mOpenHelper.getReadableDatabase(),
                        null,
                        null,
                        null,
                        null,
                        null,
                        null
                );
                break;
            }
            case BOOKMARKS:
            {
                cursor = mBookmarksQueryBuider.query(mOpenHelper.getReadableDatabase(),
                        null,
                        null,
                        null,
                        null,
                        null,
                        null
                );
                break;
            }
            case BOOKMARKS_WITH_ID:
            {
                int movieId = MovieContract.BookmarksEntry.getMovieIdFromUri(uri);
                cursor = mBookmarksQueryBuider.query(mOpenHelper.getReadableDatabase(),
                        null,
                        MovieContract.MoviesEntry.TABLE_NAME + "." + MovieContract.MoviesEntry.COLUMN_ID + " = ? ",
                        new String[] { Integer.toString(movieId) },
                        null,
                        null,
                        null
                );
                break;
            }
            case REVIEWS_WITH_ID:
            {
                int movieId = MovieContract.ReviewsEntry.getMovieIdFromUri(uri);
                cursor = mReviewsQueryBuider.query(mOpenHelper.getReadableDatabase(),
                        null,
                        MovieContract.MoviesEntry.TABLE_NAME + "." + MovieContract.MoviesEntry.COLUMN_ID + " = ? ",
                        new String[] { Integer.toString(movieId) },
                        null,
                        null,
                        null
                );
                break;
            }

            case VIDEOS_WITH_ID:
            {
                int movieId = MovieContract.VideosEntry.getMovieIdFromUri(uri);
                cursor = mVideosQueryBuider.query(mOpenHelper.getReadableDatabase(),
                        null,
                        MovieContract.MoviesEntry.TABLE_NAME + "." + MovieContract.MoviesEntry.COLUMN_ID + " = ? ",
                        new String[]{Integer.toString(movieId)},
                        null,
                        null,
                        null
                );
                break;
            }

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }


    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int returnCount = 0;
        int moviesCount = 0;
        ContentValues rankingDbContent = new ContentValues();
        switch (match) {
            case MOVIES:
                db.beginTransaction();
                returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db.insertWithOnConflict(MovieContract.MoviesEntry.TABLE_NAME, null, value, SQLiteDatabase.CONFLICT_REPLACE);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);

                return returnCount;
            case MOVIES_POPULARITY:
                db.beginTransaction();
                moviesCount = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db.insertWithOnConflict(MovieContract.MoviesEntry.TABLE_NAME, null, value, SQLiteDatabase.CONFLICT_REPLACE);
                        if (_id != -1) {
                            moviesCount++;
                        }
                    }
                returnCount = 0;
                    db.delete(MovieContract.PopularityEntry.TABLE_NAME, null, null);
                    for (ContentValues value : values) {
                        rankingDbContent.put(MovieContract.PopularityEntry.COLUMN_ID, value.getAsString(MovieContract.MoviesEntry.COLUMN_ID));
                        long _id = db.insert(MovieContract.PopularityEntry.TABLE_NAME, null, rankingDbContent);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }

                    if(moviesCount > 0 && moviesCount == returnCount) {
                        db.setTransactionSuccessful();
                    }
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;

            case MOVIES_RATING:
                db.beginTransaction();
                moviesCount = 0;
                try {
                    // inserting into movies table
                    for (ContentValues value : values) {
                        long _id = db.insertWithOnConflict(MovieContract.MoviesEntry.TABLE_NAME, null, value, SQLiteDatabase.CONFLICT_REPLACE);
                        if (_id != -1) {
                            moviesCount++;
                        }
                    }
                    returnCount = 0;

                    // inserting into highest rating table
                    db.delete(MovieContract.RatingEntry.TABLE_NAME, null, null);
                    for (ContentValues value : values) {
                        rankingDbContent.put(MovieContract.RatingEntry.COLUMN_ID, value.getAsString(MovieContract.MoviesEntry.COLUMN_ID));
                        long _id = db.insert(MovieContract.RatingEntry.TABLE_NAME, null, rankingDbContent);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }

                    // cleaning db to save precious memory
                    // can't perform outer join in sqlite so doing the inefficient way
                    db.rawQuery("DELETE FROM " + MovieContract.MoviesEntry.TABLE_NAME + " WHERE " +
                            MovieContract.MoviesEntry.TABLE_NAME + "." + MovieContract.MoviesEntry.COLUMN_ID + " NOT IN ( SELECT " +
                            MovieContract.PopularityEntry.COLUMN_ID + " FROM " + MovieContract.PopularityEntry.TABLE_NAME +
                            " ) AND " + MovieContract.MoviesEntry.TABLE_NAME + "." + MovieContract.MoviesEntry.COLUMN_ID +
                            " NOT IN ( SELECT " +  MovieContract.RatingEntry.COLUMN_ID + " FROM " + MovieContract.RatingEntry.TABLE_NAME + ");", null);

                    if(moviesCount > 0 && moviesCount == returnCount) {
                        db.setTransactionSuccessful();
                    }
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;

            case REVIEWS:
                db.beginTransaction();
                returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db.insertWithOnConflict(MovieContract.ReviewsEntry.TABLE_NAME, null, value, SQLiteDatabase.CONFLICT_REPLACE);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;

            case VIDEOS:
                db.beginTransaction();
                returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db.insertWithOnConflict(MovieContract.VideosEntry.TABLE_NAME, null, value, SQLiteDatabase.CONFLICT_REPLACE);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;

            default:
                return super.bulkInsert(uri, values);
        }
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {

        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);

        switch (match) {
            case BOOKMARKS_WITH_ID:
                long rowId = db.insert(MovieContract.BookmarksEntry.TABLE_NAME, null, values);
                if(rowId != -1) {
                    getContext().getContentResolver().notifyChange(uri, null);
                    return uri;
                }
        }
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        long rows;

        switch (match) {
            case BOOKMARKS_WITH_ID:
                int movieId = MovieContract.BookmarksEntry.getMovieIdFromUri(uri);
                rows = db.delete(MovieContract.BookmarksEntry.TABLE_NAME,
                        MovieContract.BookmarksEntry.COLUMN_ID + " = ? ",
                        new String[]{Integer.toString(movieId)});
                if(rows != 0) {
                    getContext().getContentResolver().notifyChange(uri, null);
                    return (int) rows;
                }
            case MOVIES:
                rows =  db.delete(MovieContract.MoviesEntry.TABLE_NAME,
                    selection,
                    selectionArgs);
                if(rows != 0) {
                    getContext().getContentResolver().notifyChange(uri, null);
                    return (int) rows;
                }
        }
        return -1;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }

    private static UriMatcher buildUriMatcher() {
        final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(MovieContract.CONTENT_AUTHORITY, MovieContract.PATH_MOVIES + "/*", MOVIES);
        uriMatcher.addURI(MovieContract.CONTENT_AUTHORITY, MovieContract.PATH_POPULARITY, MOVIES_POPULARITY);
        uriMatcher.addURI(MovieContract.CONTENT_AUTHORITY, MovieContract.PATH_RATING, MOVIES_RATING);
        uriMatcher.addURI(MovieContract.CONTENT_AUTHORITY, MovieContract.PATH_BOOKMARKS, BOOKMARKS);
        uriMatcher.addURI(MovieContract.CONTENT_AUTHORITY, MovieContract.PATH_BOOKMARKS + "/*", BOOKMARKS_WITH_ID);
        uriMatcher.addURI(MovieContract.CONTENT_AUTHORITY, MovieContract.PATH_REVIEWS, REVIEWS);
        uriMatcher.addURI(MovieContract.CONTENT_AUTHORITY, MovieContract.PATH_REVIEWS + "/*", REVIEWS_WITH_ID);
        uriMatcher.addURI(MovieContract.CONTENT_AUTHORITY, MovieContract.PATH_VIDEOS, VIDEOS);
        uriMatcher.addURI(MovieContract.CONTENT_AUTHORITY, MovieContract.PATH_VIDEOS + "/*", VIDEOS_WITH_ID);

        return uriMatcher;
    }
}
