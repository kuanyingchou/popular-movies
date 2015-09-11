package kuanying.popularmovies.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

public class MovieProvider extends ContentProvider {
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private static final int MOVIE = 1;
    private static final int MOVIE_ID = 2;
    private MovieDbHelper dbHelper;

    private static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = MovieContract.CONTENT_AUTHORITY;
        matcher.addURI(authority, MovieContract.PATH_MOVIE, MOVIE);
        matcher.addURI(authority, MovieContract.PATH_MOVIE + "/#", MOVIE_ID);
        return matcher;
    }

    @Override
    public boolean onCreate() {
        dbHelper = new MovieDbHelper(getContext());
        return true;
    }

    @Override
    public String getType(Uri uri) {
        int match = sUriMatcher.match(uri);
        switch(match) {
            case MOVIE:
                return MovieContract.CONTENT_TYPE;
            case MOVIE_ID:
                return MovieContract.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        int match = sUriMatcher.match(uri);
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        switch(match) {
            case MOVIE:
                return db.query(MovieContract.MovieEntry.TABLE_NAME, projection,
                        selection, selectionArgs, null, null, sortOrder);
            case MOVIE_ID:
                long id = Long.valueOf(uri.getPathSegments().get(1));
                String s = MovieContract.MovieEntry._ID + " = ?";
                String[] sArgs = { String.valueOf(id) };
                return db.query(MovieContract.MovieEntry.TABLE_NAME, projection,
                        s, sArgs, null, null, null);
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int match = sUriMatcher.match(uri);
        switch(match) {
            case MOVIE:
                throw new UnsupportedOperationException("wrong uri: " + uri);
            case MOVIE_ID:
                long id = db.insert(MovieContract.MovieEntry.TABLE_NAME, null, values);
                return ContentUris.withAppendedId(MovieContract.MovieEntry.CONTENT_URI, id);
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int rowsUpdated = 0;
        int match = sUriMatcher.match(uri);
        switch(match) {
            case MOVIE:
                throw new UnsupportedOperationException("wrong uri: " + uri);
            case MOVIE_ID:
                long id = Long.valueOf(uri.getPathSegments().get(1));
                String s = MovieContract.MovieEntry._ID + " = ?";
                String[] sArgs = { String.valueOf(id) };

                rowsUpdated = db.update(MovieContract.MovieEntry.TABLE_NAME,
                        values, s, sArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }
}
