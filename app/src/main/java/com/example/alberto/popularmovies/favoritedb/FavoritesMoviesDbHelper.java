package com.example.alberto.popularmovies.favoritedb;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.example.alberto.popularmovies.favoritedb.FavoritesMoviesContract.FavoritesMovies;

public class FavoritesMoviesDbHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "favorites.db";
    private static final int DATABASE_VERSION = 2;

    public FavoritesMoviesDbHelper(Context context) {
        super (context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        final String SQL_CREATE_FAVORITES_TABLE =
                "CREATE TABLE " + FavoritesMovies.TABLE_NAME + " (" +
                FavoritesMovies._ID + " INTEGER PRIMARY KEY, " +
                FavoritesMovies.COLUMN_MOVIE_ID + " INTEGER NOT NULL, " +
                FavoritesMovies.COLUMN_TITLE + " TEXT NOT NULL, " +
                FavoritesMovies.COLUMN_POSTER + " TEXT NOT NULL, " +
                FavoritesMovies.COLUMN_PLOT + " TEXT NOT NULL, " +
                FavoritesMovies.COLUMN_RELEASE_DATE+ " TEXT NOT NULL, " +
                FavoritesMovies.COLUMN_AVERAGE_VOTE + " REAL NOT NULL);";

        sqLiteDatabase.execSQL(SQL_CREATE_FAVORITES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + FavoritesMovies.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
