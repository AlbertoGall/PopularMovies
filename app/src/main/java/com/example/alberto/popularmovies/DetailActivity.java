package com.example.alberto.popularmovies;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.alberto.popularmovies.favoritedb.FavoritesMoviesContract;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

public class DetailActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private final static String POSTER_LINK = "http://image.tmdb.org/t/p/w500/";
    private final static String TRAILER_THUMBNAIL_LINK = "https://img.youtube.com/vi/";
    private final static String TRAILER_THUMBNAIL_FILE = "/0.jpg";
    private final static String PARAM_VIDEOS = "videos";
    private final static String PARAM_REVIEWS = "reviews";

    private final static int ID_DETAIL_LOADER = 793;

    private final static String[] DETAIL_PROJECTION = {FavoritesMoviesContract.FavoritesMovies.COLUMN_MOVIE_ID};

    ImageView mTrailer1ImageView;
    ImageView mTrailer2ImageView;
    TextView mReviewTextView;
    TextView mTrailerLabelTextView;
    TextView mReviewLabelTextView;

    PopularMovie mMovie;

    String[] mTrailersKeys = new String[2];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        ImageView posterImageView = findViewById(R.id.detail_poster_iw);
        TextView titleTextView = findViewById(R.id.detail_title_tw);
        TextView releaseDateTextView = findViewById(R.id.detail_release_date_tw);
        TextView averageRateTextView = findViewById(R.id.detail_average_vote_tw);
        TextView plotTextView = findViewById(R.id.detail_plot_tw);
        mTrailer1ImageView = findViewById(R.id.detail_trailer_1_iw);
        mTrailer2ImageView = findViewById(R.id.detail_trailer_2_iw);
        mReviewTextView = findViewById(R.id.detail_review_tw);
        mTrailerLabelTextView = findViewById(R.id.label_trailer);
        mReviewLabelTextView = findViewById(R.id.label_review);

        Intent intent = getIntent();

        mMovie = intent.getParcelableExtra(MainActivity.MOVIE_SELECTED);

        URL[] trailerAndReviewURL = {Utils.buildURL(String.valueOf(mMovie.getId()), PARAM_VIDEOS),
                Utils.buildURL(String.valueOf(mMovie.getId()), PARAM_REVIEWS)};
        new MovieDbQueryTask().execute(trailerAndReviewURL);

        String posterLink = POSTER_LINK + mMovie.getPoster();
        Picasso.with(this).load(posterLink).into(posterImageView);

        titleTextView.setText(mMovie.getTitle());
        releaseDateTextView.setText(mMovie.getReleaseDate());
        averageRateTextView.setText(String.valueOf(mMovie.getVoteAverage()));
        plotTextView.setText(mMovie.getPlot());

        getSupportLoaderManager().initLoader(ID_DETAIL_LOADER, null, this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.detail_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_favorite) {
            if (!mMovie.getFavorite()) {
                ContentValues newFavoriteMovie = Utils.contentValuesFromMovie(mMovie);
                getContentResolver().insert(FavoritesMoviesContract.FavoritesMovies.CONTENT_URI,
                        newFavoriteMovie);
                mMovie.setFavorite(true);
                invalidateOptionsMenu();
                return true;
            } else {
                Uri uri = FavoritesMoviesContract.FavoritesMovies.CONTENT_URI;
                uri = uri.buildUpon().appendPath(String.valueOf(mMovie.getId())).build();

                getContentResolver().delete(uri, null, null);
                mMovie.setFavorite(false);
                invalidateOptionsMenu();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (mMovie.getFavorite()) {
            menu.findItem(R.id.action_favorite).setIcon(R.drawable.ic_star_full_white);
        } else {
            menu.findItem(R.id.action_favorite).setIcon(R.drawable.ic_star_border_white);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {

        switch (i) {
            case ID_DETAIL_LOADER:

                return new CursorLoader(this,
                        FavoritesMoviesContract.FavoritesMovies.CONTENT_URI,
                        DETAIL_PROJECTION,
                        FavoritesMoviesContract.FavoritesMovies.COLUMN_MOVIE_ID + " == " + mMovie.getId(),
                        null,
                        null);
            default:
                throw new RuntimeException("Loader Not Implemented: " + i);
        }
    }

    @Override
    public void onLoadFinished(@NonNull android.support.v4.content.Loader<Cursor> loader, Cursor data) {
        mMovie.setFavorite(data.getCount() > 0);
        data.close();
        invalidateOptionsMenu();
        getSupportLoaderManager().destroyLoader(ID_DETAIL_LOADER);
    }

    @Override
    public void onLoaderReset(@NonNull android.support.v4.content.Loader<Cursor> loader) {

    }

    public class MovieDbQueryTask extends AsyncTask<URL, Void, String[]> {
        @Override
        protected String[] doInBackground(URL... urls) {
            URL trailerQueryUrl = urls[0];
            URL reviewQueryUrl = urls[1];
            String[] movieDbJson = new String[2];
            try {
                movieDbJson[0] = Utils.getJsonFromURL(trailerQueryUrl);
                movieDbJson[1] = Utils.getJsonFromURL(reviewQueryUrl);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return movieDbJson;
        }

        @Override
        protected void onPostExecute(String[] s) {
            super.onPostExecute(s);
            final Context context = DetailActivity.this;
            String[] reviews = Utils.getMovieReviews(s[1]);
            final String[] trailerKeys = Utils.getMovieTrailers(s[0]);
            if (trailerKeys.length > 0) {
                mTrailerLabelTextView.setVisibility(View.VISIBLE);
                mTrailersKeys[0] = TRAILER_THUMBNAIL_LINK + trailerKeys[0] + TRAILER_THUMBNAIL_FILE;
                Picasso.with(context).load(mTrailersKeys[0]).into(mTrailer1ImageView);
                mTrailer1ImageView.setOnClickListener(new View.OnClickListener(){

                    @Override
                    public void onClick(View v) {
                        Intent youtubeIntent = new Intent(Intent.ACTION_VIEW,
                                Uri.parse("vnd.youtube:" + trailerKeys[0]));
                        Intent webIntent = new Intent(Intent.ACTION_VIEW,
                                Uri.parse("http://www.youtube.com/watch?v=" + trailerKeys[0]));
                        if (youtubeIntent.resolveActivity(getPackageManager()) != null) {
                            context.startActivity(youtubeIntent);
                        } else {
                            context.startActivity(webIntent);
                        }
                    }
                });
                if (trailerKeys.length > 1) {
                    mTrailersKeys[1] = TRAILER_THUMBNAIL_LINK + trailerKeys[1] + TRAILER_THUMBNAIL_FILE;
                    Picasso.with(context).load(mTrailersKeys[1]).into(mTrailer2ImageView);
                    mTrailer2ImageView.setOnClickListener(new View.OnClickListener(){

                        @Override
                        public void onClick(View v) {
                            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:" + trailerKeys[1])));
                        }
                    });
                }
            }
            if (reviews.length > 0 && !reviews[0].equals("")) {
                mReviewLabelTextView.setVisibility(View.VISIBLE);
                mReviewTextView.setText(reviews[0]);
            }
        }
    }
}
