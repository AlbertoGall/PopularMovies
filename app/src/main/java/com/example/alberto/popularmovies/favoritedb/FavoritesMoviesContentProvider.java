package com.example.alberto.popularmovies.favoritedb;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import static com.example.alberto.popularmovies.favoritedb.FavoritesMoviesContract.FavoritesMovies.TABLE_NAME;

public class FavoritesMoviesContentProvider extends ContentProvider {

    public static final int MOVIES = 300;
    public static final int MOVIES_ID = 301;

    private static final UriMatcher uriMatcher = buildUriMatcher();

    public static UriMatcher buildUriMatcher() {

        UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);

        matcher.addURI(FavoritesMoviesContract.AUTHORITY,
                FavoritesMoviesContract.PATH_FAVORITES, MOVIES);
        matcher.addURI(FavoritesMoviesContract.AUTHORITY,
                FavoritesMoviesContract.PATH_FAVORITES + "/#", MOVIES_ID);

        return matcher;
    }

    private FavoritesMoviesDbHelper moviesDbHelper;

    @Override
    public boolean onCreate() {
        moviesDbHelper = new FavoritesMoviesDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {

        final SQLiteDatabase db = moviesDbHelper.getWritableDatabase();

        int uriMatch = uriMatcher.match(uri);
        Uri insertUri;

        switch (uriMatch) {
            case MOVIES:
                long id = db.insert(TABLE_NAME, null, contentValues);

                if (id > 0) {
                    insertUri = ContentUris.withAppendedId(
                            FavoritesMoviesContract.FavoritesMovies.CONTENT_URI, id);
                } else {
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }
                break;
            // Set the value for the returnedUri and write the default case for unknown URI's
            // Default case throws an UnsupportedOperationException
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);

        return insertUri;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] strings, @Nullable String s,
                        @Nullable String[] strings1, @Nullable String s1) {

        final SQLiteDatabase db = moviesDbHelper.getReadableDatabase();

        int uriMatch = uriMatcher.match(uri);
        Cursor queryCursor;

        switch (uriMatch) {
            case MOVIES:
                queryCursor = db.query(TABLE_NAME, strings, s, strings1, null,
                        null, s1);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        queryCursor.setNotificationUri(getContext().getContentResolver(), uri);

        return queryCursor;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String s, @Nullable String[] strings) {
        final SQLiteDatabase db = moviesDbHelper.getWritableDatabase();

        int uriMatch = uriMatcher.match(uri);

        int moviesDeleted;

        switch (uriMatch) {

            case MOVIES_ID:

                String id = uri.getPathSegments().get(1);

                moviesDeleted = db.delete(TABLE_NAME, "movieId=?", new String[]{id});
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        if (moviesDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return moviesDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String s,
                      @Nullable String[] strings) {
        throw new UnsupportedOperationException("Function not implemented");
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }
}
