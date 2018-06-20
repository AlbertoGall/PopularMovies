package com.example.alberto.popularmovies;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.util.DisplayMetrics;

import com.example.alberto.popularmovies.favoritedb.FavoritesMoviesContract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Created by Alberto on 04/03/2018.
 */

class Utils {

    private final static String MOVIE_DB_BASE_URL = "http://api.themoviedb.org/3/movie";

    private final static String PARAM_API_KEY = "api_key";

    private final static String API_KEY = BuildConfig.IMDB_API_KEY;

    private final static String JSON_MOVIE_ARRAY = "results";
    private final static String JSON_TITLE_STRING = "title";
    private final static String JSON_DATE_STRING = "release_date";
    private final static String JSON_POSTER_STRING = "poster_path";
    private final static String JSON_VOTE_DOUBLE = "vote_average";
    private final static String JSON_OVERVIEW_STRING = "overview";
    private final static String JSON_ID_INT = "id";
    private final static String JSON_KEY_STRING = "key";
    private final static String JSON_CONTENT_STRING = "content";

    static URL buildURL(String id, String path) {
        Uri buildUri = Uri.parse(MOVIE_DB_BASE_URL).buildUpon()
                .appendPath(id)
                .appendPath(path)
                .appendQueryParameter(PARAM_API_KEY, API_KEY)
                .build();

        URL url = null;

        try {
            url = new URL (buildUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return url;
    }

    static URL buildURL (String path) {
        return buildURL("", path);
    }

    static String getJsonFromURL (URL url) throws IOException {
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        try {
            InputStream inputStream = urlConnection.getInputStream();

            Scanner scanner = new Scanner(inputStream);
            scanner.useDelimiter("\\A");

            if (scanner.hasNext()) {
                return scanner.next();
            } else {
                return null;
            }
        } finally {
            urlConnection.disconnect();
        }
    }

    static ArrayList<PopularMovie> getArrayFromJson (String json) {
        ArrayList<PopularMovie> listOfMovies = new ArrayList<PopularMovie>();
        try {
            JSONObject movieDB = new JSONObject(json);
            JSONArray movies = movieDB.getJSONArray(JSON_MOVIE_ARRAY);
            for (int i=0; i<movies.length(); i++) {
                JSONObject movie = movies.getJSONObject(i);
                int id = movie.optInt(JSON_ID_INT);
                String title = movie.optString(JSON_TITLE_STRING);
                String releaseDate = movie.optString(JSON_DATE_STRING);
                String poster = movie.optString(JSON_POSTER_STRING);
                double voteAverage = movie.optDouble(JSON_VOTE_DOUBLE);
                String overview = movie.optString(JSON_OVERVIEW_STRING);
                PopularMovie popularMovie = new PopularMovie(id,title,releaseDate,poster,voteAverage,overview);
                listOfMovies.add(popularMovie);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return listOfMovies;
    }

    static String[] getMovieTrailers (String json) {
        String[] trailersId = null;
        try{
            JSONObject trailerDB = new JSONObject(json);
            JSONArray trailers = trailerDB.getJSONArray(JSON_MOVIE_ARRAY);
            trailersId = new String[trailers.length()];
            for (int i=0; i<trailers.length(); i++) {
                JSONObject trailer = trailers.getJSONObject(i);
                String trailerKey = trailer.optString(JSON_KEY_STRING);
                trailersId[i] = trailerKey;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return trailersId;
    }

    static String[] getMovieReviews (String json) {
        String[] movieReviews = null;
        try{
            JSONObject reviewsDB = new JSONObject(json);
            JSONArray reviews = reviewsDB.getJSONArray(JSON_MOVIE_ARRAY);
            movieReviews = new String[reviews.length()];
            for (int i=0; i<reviews.length(); i++) {
                JSONObject review = reviews.getJSONObject(i);
                String reviewContent = review.optString(JSON_CONTENT_STRING);
                movieReviews[i] = reviewContent;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return movieReviews;
    }

    static int getColumnsWidth(Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        int width = displayMetrics.widthPixels;
        int height = displayMetrics.heightPixels;
        return Math.max(width/3, height/5);
    }

    static ContentValues contentValuesFromMovie (PopularMovie movie) {
        ContentValues contentValues = new ContentValues();

        contentValues.put(FavoritesMoviesContract.FavoritesMovies.COLUMN_MOVIE_ID, movie.getId());
        contentValues.put(FavoritesMoviesContract.FavoritesMovies.COLUMN_AVERAGE_VOTE, movie.getVoteAverage());
        contentValues.put(FavoritesMoviesContract.FavoritesMovies.COLUMN_PLOT, movie.getPlot());
        contentValues.put(FavoritesMoviesContract.FavoritesMovies.COLUMN_POSTER, movie.getPoster());
        contentValues.put(FavoritesMoviesContract.FavoritesMovies.COLUMN_RELEASE_DATE, movie.getReleaseDate());
        contentValues.put(FavoritesMoviesContract.FavoritesMovies.COLUMN_TITLE, movie.getTitle());

        return contentValues;
    }
}