package com.example.alberto.popularmovies;

import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.alberto.popularmovies.favoritedb.FavoritesMoviesContract;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements
        MoviePosterAdapter.MoviePosterAdapterOnClickHandler, LoaderManager.LoaderCallbacks<Cursor> {

    private final static String KEY_INSTANCE_STATE_RV_POSITION = "rv_state_position";
    private final static String KEY_POPULAR_MOVIE_ARRAY = "popular_movie_array";

    private final static String SORT_MOST_POPULAR = "popular";
    private final static String SORT_HIGHEST_RATED = "top_rated";

    private final static String SORT_BY = "sort_by";

    private final static int SPINNER_POSITION_POPULAR = 0;
    private final static int SPINNER_POSITION_HIGHEST_RATE = 1;
    private final static int SPINNER_POSITION_FAVORITE = 2;

    final static String MOVIE_SELECTED = "movie selected";

    private final static int ID_MAIN_LOADER = 985;

    private final static int RECYCLER_VIEW_DELAY = 400;

    Spinner mSortBySpinner;
    int mPosition;

    ArrayList<PopularMovie> mPopularMovieArray;
    MoviePosterAdapter mMoviePosterAdapter;
    RecyclerView recyclerView;
    Parcelable mLayoutManagerSavedState;
    RecyclerView.LayoutManager mLayoutManager;
    Boolean saved;
    AsyncTask mTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        saved = false;

        int gridWidth = getResources().getInteger(R.integer.grid_width);

        mLayoutManager = new GridLayoutManager(this, gridWidth);
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setHasFixedSize(true);
        mMoviePosterAdapter = new MoviePosterAdapter(this);
        recyclerView.setAdapter(mMoviePosterAdapter);
    }

    @Override
    public void onClick(PopularMovie movieSelected) {
        Intent detailIntent = new Intent(this, DetailActivity.class);
        detailIntent.putExtra(MOVIE_SELECTED, movieSelected);
        startActivity(detailIntent);
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        MenuItem item = menu.findItem(R.id.menu_spinner);
        mSortBySpinner = (Spinner) item.getActionView();
        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.spinner_options, android.R.layout.simple_spinner_item);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSortBySpinner.setAdapter(spinnerAdapter);
        mSortBySpinner.setSelection(mPosition);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSortBySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (mPosition == i && saved) {
                    updateRecyclerView();
                } else {
                    mPosition = i;
                    if (mPopularMovieArray == null) {
                        mPopularMovieArray = new ArrayList<>();
                    }
                    mPopularMovieArray.clear();
                    mLayoutManager.removeAllViews();
                    checkMenuItem(mPosition);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
        return true;
    }

    private void checkMenuItem(int i) {
        String sortBy = SORT_MOST_POPULAR;
        if (i == SPINNER_POSITION_POPULAR || i == SPINNER_POSITION_HIGHEST_RATE) {
            if (i == SPINNER_POSITION_HIGHEST_RATE) {
                sortBy = SORT_HIGHEST_RATED;
            }
            URL movieSearchUrl = Utils.buildURL(sortBy);
            if (isConnected()) {
                mTask = new MovieDbQueryTask().execute(movieSearchUrl);
            } else {
                mPopularMovieArray = new ArrayList<>();
                mPopularMovieArray.clear();
                mMoviePosterAdapter.setMoviesList(mPopularMovieArray);
                Toast.makeText(getApplicationContext(), "Not connected, please retry",
                        Toast.LENGTH_LONG).show();
            }
        } else if (i == SPINNER_POSITION_FAVORITE) {
            if (isConnected()) {
                getSupportLoaderManager().initLoader(ID_MAIN_LOADER, null,
                        MainActivity.this);
            } else {
                mPopularMovieArray = new ArrayList<>();
                mPopularMovieArray.clear();
                mMoviePosterAdapter.setMoviesList(mPopularMovieArray);
                Toast.makeText(getApplicationContext(), "Not connected, please retry",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        switch (id) {
            case ID_MAIN_LOADER:

                return new CursorLoader(this,
                        FavoritesMoviesContract.FavoritesMovies.CONTENT_URI,
                        null,
                        null,
                        null,
                        null);
            default:
                throw new RuntimeException("Loader Not Implemented: " + id);
        }
    }


    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        if (data.moveToFirst()) {
            ArrayList<PopularMovie> popularMovies = new ArrayList<>();
            do {
                String title = data.getString(data.getColumnIndex(FavoritesMoviesContract
                        .FavoritesMovies.COLUMN_TITLE));
                String plot = data.getString(data.getColumnIndex(FavoritesMoviesContract
                        .FavoritesMovies.COLUMN_PLOT));
                String poster = data.getString(data.getColumnIndex(FavoritesMoviesContract
                        .FavoritesMovies.COLUMN_POSTER));
                String date = data.getString(data.getColumnIndex(FavoritesMoviesContract
                        .FavoritesMovies.COLUMN_RELEASE_DATE));
                double vote = data.getDouble(data.getColumnIndex(FavoritesMoviesContract
                        .FavoritesMovies.COLUMN_AVERAGE_VOTE));
                int id = data.getInt(data.getColumnIndex(FavoritesMoviesContract
                        .FavoritesMovies.COLUMN_MOVIE_ID));

                popularMovies.add(new PopularMovie(id, title, date, poster, vote, plot));
            } while (data.moveToNext());
            if (mPopularMovieArray == null) {
                mPopularMovieArray = new ArrayList<>();
            }
            mPopularMovieArray.clear();
            mPopularMovieArray.addAll(popularMovies);
            mMoviePosterAdapter.setMoviesList(mPopularMovieArray);
        } else {
            mPopularMovieArray = new ArrayList<>();
            mPopularMovieArray.clear();
            mMoviePosterAdapter.setMoviesList(mPopularMovieArray);
            Toast.makeText(getApplicationContext(), "No favorites",
                    Toast.LENGTH_SHORT).show();
        }
        data.close();
        getSupportLoaderManager().destroyLoader(loader.getId());
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
    }

    public class MovieDbQueryTask extends AsyncTask<URL, Void, ArrayList<PopularMovie>> {
        @Override
        protected ArrayList<PopularMovie> doInBackground(URL... urls) {
            URL queryUrl = urls[0];
            String movieDbJson;
            ArrayList<PopularMovie> arrayDbJson = null;
            try {
                movieDbJson = Utils.getJsonFromURL(queryUrl);
                arrayDbJson = Utils.getArrayFromJson(movieDbJson);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return arrayDbJson;
        }

        @Override
        protected void onPostExecute(ArrayList<PopularMovie> arrayList) {
            super.onPostExecute(arrayList);
            if (mPopularMovieArray == null) {
                mPopularMovieArray = new ArrayList<>();
            }
            mPopularMovieArray.clear();
            mPopularMovieArray.addAll(arrayList);
            if (mPopularMovieArray != null) {
                mMoviePosterAdapter.setMoviesList(mPopularMovieArray);
            }
        }
    }

    private void updateRecyclerView() {
        mMoviePosterAdapter.setMoviesList(mPopularMovieArray);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mLayoutManagerSavedState != null) {
                    mLayoutManager.onRestoreInstanceState(mLayoutManagerSavedState);
                }
            }
        }, RECYCLER_VIEW_DELAY);
    }

    public boolean isConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager)
                getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnectedOrConnecting();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mSortBySpinner != null) {
            mPosition = mSortBySpinner.getSelectedItemPosition();
            outState.putInt(SORT_BY, mPosition);
        }
        outState.putParcelableArrayList(KEY_POPULAR_MOVIE_ARRAY, mPopularMovieArray);
        mLayoutManagerSavedState = mLayoutManager.onSaveInstanceState();
        outState.putParcelable(KEY_INSTANCE_STATE_RV_POSITION, mLayoutManagerSavedState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mPosition == SPINNER_POSITION_FAVORITE) {
            getSupportLoaderManager().restartLoader(ID_MAIN_LOADER, null,
                    MainActivity.this);
        }
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        if (savedInstanceState != null) {
            saved = true;
            if (savedInstanceState.containsKey(SORT_BY)) {
                mPosition = savedInstanceState.getInt(SORT_BY);
            }
            if (savedInstanceState.containsKey(KEY_INSTANCE_STATE_RV_POSITION)) {
                mLayoutManagerSavedState = savedInstanceState
                        .getParcelable(KEY_INSTANCE_STATE_RV_POSITION);
            }
            if (savedInstanceState.containsKey(KEY_POPULAR_MOVIE_ARRAY)) {
                mPopularMovieArray = savedInstanceState
                        .getParcelableArrayList(KEY_POPULAR_MOVIE_ARRAY);
            }
        }
    }
}


