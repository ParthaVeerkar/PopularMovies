package com.example.android.popularmovies.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.example.android.popularmovies.data.MovieContract.MoviesEntry;

/**
 * Created by partha.veerkar on 2/19/16.
 */

public class MoviesDbHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "movies.db";

    public MoviesDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        final String SQL_CREATE_MOVIES_TABLE = "CREATE TABLE " + MoviesEntry.TABLE_NAME + " (" +
                MoviesEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                MoviesEntry.COLUMN_ID + " INTEGER UNIQUE NOT NULL, " +
                MoviesEntry.COLUMN_POSTER_PATH + " TEXT NOT NULL, " +
                MoviesEntry.COLUMN_OVERVIEW + " TEXT NOT NULL, " +
                MoviesEntry.COLUMN_RELEASE_DATE + " TEXT NOT NULL, " +
                MoviesEntry.COLUMN_TITLE + " TEXT NOT NULL, " +
                MoviesEntry.COLUMN_VOTE_AVERAGE + " REAL NOT NULL, " +
                MoviesEntry.COLUMN_VIDEO + " TEXT " +
                " );";

        db.execSQL(SQL_CREATE_MOVIES_TABLE);

        final String SQL_CREATE_POPULARITY_TABLE = "CREATE TABLE " + MovieContract.PopularityEntry.TABLE_NAME + " (" +
                MoviesEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                MoviesEntry.COLUMN_ID + " INTEGER UNIQUE NOT NULL " +
                " );";

        db.execSQL(SQL_CREATE_POPULARITY_TABLE);

        final String SQL_CREATE_RATING_TABLE = "CREATE TABLE " + MovieContract.RatingEntry.TABLE_NAME + " (" +
                MovieContract.RatingEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                MovieContract.RatingEntry.COLUMN_ID + " INTEGER UNIQUE NOT NULL " +
                " );";

        db.execSQL(SQL_CREATE_RATING_TABLE);

        final String SQL_CREATE_BOOKMARKS_TABLE = "CREATE TABLE " + MovieContract.BookmarksEntry.TABLE_NAME + " (" +
                MovieContract.BookmarksEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                MovieContract.BookmarksEntry.COLUMN_ID + " INTEGER UNIQUE NOT NULL " +
                " );";

        db.execSQL(SQL_CREATE_BOOKMARKS_TABLE);

        final String SQL_CREATE_REVIEWS_TABLE = "CREATE TABLE " + MovieContract.ReviewsEntry.TABLE_NAME + " (" +
                MovieContract.ReviewsEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                MovieContract.ReviewsEntry.COLUMN_REVIEW_ID + " TEXT UNIQUE NOT NULL, " +
                MovieContract.ReviewsEntry.COLUMN_ID + " INTEGER NOT NULL, " +
                MovieContract.ReviewsEntry.COLUMN_AUTHOR + " TEXT NOT NULL, " +
                MovieContract.ReviewsEntry.COLUMN_CONTENTS + " TEXT NOT NULL " +
                " );";

        db.execSQL(SQL_CREATE_REVIEWS_TABLE);

        final String SQL_CREATE_VIDEOS_TABLE = "CREATE TABLE " + MovieContract.VideosEntry.TABLE_NAME + " (" +
                MovieContract.VideosEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                MovieContract.VideosEntry.COLUMN_ID + " INTEGER NOT NULL, " +
                MovieContract.VideosEntry.COLUMN_URL + " TEXT UNIQUE NOT NULL " +
                " );";

        db.execSQL(SQL_CREATE_VIDEOS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + MoviesEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + MovieContract.PopularityEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + MovieContract.RatingEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + MovieContract.BookmarksEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + MovieContract.ReviewsEntry.TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + MovieContract.VideosEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }


}
