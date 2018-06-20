package com.example.alberto.popularmovies.favoritedb;

import android.net.Uri;
import android.provider.BaseColumns;

public class FavoritesMoviesContract {

    public static final String AUTHORITY = "com.example.alberto.popularmovies";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);

    public static final String PATH_FAVORITES = "favorites";

    public static final class FavoritesMovies implements BaseColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(PATH_FAVORITES)
                .build();

        public static final String TABLE_NAME = "favorites";
        public static final String COLUMN_MOVIE_ID = "movieId";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_RELEASE_DATE = "releaseDate";
        public static final String COLUMN_POSTER = "poster";
        public static final String COLUMN_AVERAGE_VOTE = "vote";
        public static final String COLUMN_PLOT = "plot";
    }

}
