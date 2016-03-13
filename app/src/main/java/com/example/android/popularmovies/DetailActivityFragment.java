package com.example.android.popularmovies;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.android.popularmovies.data.MovieContract;
import com.squareup.picasso.Picasso;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{

    private static final String LOG_TAG = DetailActivityFragment.class.getSimpleName();
    private static final String IMAGE_URL = "http://image.tmdb.org/t/p/w185";

    public static String DETAIL_URI = "URI";
    private Uri mUri;

    private static final String[] MOVIE_COLUMNS = {
        MovieContract.MoviesEntry.COLUMN_ID,
        MovieContract.MoviesEntry.COLUMN_POSTER_PATH,
        MovieContract.MoviesEntry.COLUMN_OVERVIEW,
        MovieContract.MoviesEntry.COLUMN_RELEASE_DATE,
        MovieContract.MoviesEntry.COLUMN_TITLE,
        MovieContract.MoviesEntry.COLUMN_VOTE_AVERAGE,
        MovieContract.MoviesEntry.COLUMN_VIDEO
    };

    private static final int INDEX_ID = 1;
    private static final int INDEX_POSTER_PATH = 2;
    private static final int INDEX_OVERVIEW = 3;
    private static final int INDEX_RELEASE_DATE = 4;
    private static final int INDEX_TITLE = 5;
    private static final int INDEX_VOTE_AVERAGE = 6;

    private static final int MY_LOADER_ID = 0;

    private ImageView mPoster;
    private TextView mDescription;
    private TextView mTitle;
    private TextView mReleaseDate;
    private TextView mReleaseDateTitle;
    private TextView mVoteCount;
    private Button mBookmarkButton;
    private LinearLayout mReviewsList;
    private LinearLayout mVideosList;
    private TextView mTrailersTitle;
    private TextView mReviewsTitle;
    private int mMovieId;
    private boolean isBookmarked;

    public DetailActivityFragment()  {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        Bundle args = getArguments();
        if(args != null) {
            mUri = args.getParcelable(DETAIL_URI);
        }

        mPoster = (ImageView) rootView.findViewById(R.id.detailMoviePoster);
        mDescription = (TextView) rootView.findViewById(R.id.detailDescription);
        mTitle = (TextView) rootView.findViewById(R.id.detailTitle);
        mReleaseDate = (TextView) rootView.findViewById(R.id.detailReleaseDate);
        mReleaseDateTitle = (TextView) rootView.findViewById(R.id.releaseDateTitle);
        mVoteCount = (TextView) rootView.findViewById(R.id.detailVotes);
        mBookmarkButton = (Button) rootView.findViewById(R.id.bookmarkButton);
        mReviewsList = (LinearLayout) rootView.findViewById(R.id.reivewList);
        mVideosList = (LinearLayout) rootView.findViewById(R.id.videoList);
        mTrailersTitle = (TextView) rootView.findViewById(R.id.trailersTitle);
        mReviewsTitle = (TextView) rootView.findViewById(R.id.reviewTitle);

        mBookmarkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri bookmarkUri = MovieContract.BookmarksEntry.buildInsertBookmarksUriWithMovieId(mMovieId);
                ContentValues contentValues = new ContentValues();
                contentValues.put(MovieContract.BookmarksEntry.COLUMN_ID, mMovieId);
                    if(!isBookmarked) {
                        if(null != getActivity().getContentResolver().insert(bookmarkUri, contentValues)) {
                            isBookmarked = true;
                            mBookmarkButton.setText(R.string.is_bookmarked_text);
                        }
                    } else {
                        if(-1 != getActivity().getContentResolver().delete(bookmarkUri, null, null) ) {
                            isBookmarked = false;
                            mBookmarkButton.setText(R.string.is_not_bookmarked_text);
                        }
                    }
            }
        });
        return rootView;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if(null != mUri) {
            return new CursorLoader(getActivity(), mUri, MOVIE_COLUMNS, null, null, null);
        } else {
            getActivity().findViewById(R.id.moviePosterGrid).performClick();
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, final Cursor data) {

        if (!data.moveToFirst()) { return; }

        Picasso.with(getContext()).load(IMAGE_URL + data.getString(INDEX_POSTER_PATH))
                .placeholder(R.drawable.noimage)
                .into(mPoster);
        mDescription.setText(data.getString(INDEX_OVERVIEW));
        mTitle.setText(data.getString(INDEX_TITLE));
        mReleaseDateTitle.setText("Release date:");
        mReleaseDate.setText(data.getString(INDEX_RELEASE_DATE));
        mVoteCount.setText("User Rating: " + data.getString(INDEX_VOTE_AVERAGE));

        mMovieId = data.getInt(INDEX_ID);

        Uri isBookmarkedUri = MovieContract.BookmarksEntry.buildInsertBookmarksUriWithMovieId(mMovieId);
        final Cursor cursor = getActivity().getContentResolver().query(isBookmarkedUri, null, null, null, null);

        isBookmarked = cursor.moveToFirst();
        if(isBookmarked) {
            mBookmarkButton.setText(R.string.is_bookmarked_text);
        } else {
            mBookmarkButton.setText(R.string.is_not_bookmarked_text);
        }
        mBookmarkButton.setVisibility(View.VISIBLE);

        LayoutInflater inflater = LayoutInflater.from(getContext());
        View newView;

        // adding reviews
        Uri reviewUri = MovieContract.ReviewsEntry.buildReviewsUriWithMovieId(mMovieId);
        Cursor reviewsCursor = getActivity().getContentResolver().query(reviewUri, null, null, null, null);
        if(reviewsCursor.getCount() > 0) {
            mReviewsTitle.setVisibility(View.VISIBLE);
            mReviewsList.removeAllViews();
        } else {
            mReviewsTitle.setVisibility(View.GONE);
        }

            while (reviewsCursor.moveToNext()) {
                newView = inflater.inflate(R.layout.review, mReviewsList, false);
                ((TextView) newView.findViewById(R.id.review_author)).setText("Review by " + reviewsCursor.getString(reviewsCursor.getColumnIndex(MovieContract.ReviewsEntry.COLUMN_AUTHOR)));
                ((TextView) newView.findViewById(R.id.review_content)).setText(reviewsCursor.getString(reviewsCursor.getColumnIndex(MovieContract.ReviewsEntry.COLUMN_CONTENTS)));
                mReviewsList.addView(newView);
            }
        reviewsCursor.close();

        // adding videos
        Uri videoUri = MovieContract.VideosEntry.buildVideosUriWithMovieId(mMovieId);
        final Cursor videoCursor = getActivity().getContentResolver().query(videoUri, null, null, null, null);
        if(videoCursor.getCount() > 0) {
            mTrailersTitle.setVisibility(View.VISIBLE);
            mVideosList.removeAllViews();
        } else {
            mTrailersTitle.setVisibility(View.GONE);
        }
        Button shareButton;

        while (videoCursor.moveToNext()) {
            String videoUrl = videoCursor.getString(videoCursor.getColumnIndex(MovieContract.VideosEntry.COLUMN_URL));
            newView = inflater.inflate(R.layout.video, mVideosList, false);
            newView.setTag(videoUrl);
            ((TextView) newView.findViewById(R.id.videoTitle)).setText("Trailer " + (videoCursor.getPosition() + 1));
            newView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(v.getTag().toString())));
                }
            });

            shareButton = (Button) newView.findViewById(R.id.shareButton);
            shareButton.setTag(videoUrl);
            shareButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent shareIntent = new Intent(Intent.ACTION_SEND);
                    shareIntent.setType("text/plain");
                    shareIntent.putExtra(Intent.EXTRA_TEXT, "Check out this video: " + v.getTag().toString());
                    startActivity(Intent.createChooser(shareIntent, "Stuff"));
                        }
                    });
            mVideosList.addView(newView);
        }
        videoCursor.close();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(MY_LOADER_ID, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
