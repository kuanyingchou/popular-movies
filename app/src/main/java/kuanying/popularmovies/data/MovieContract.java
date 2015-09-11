package kuanying.popularmovies.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

public final class MovieContract {
    public static final String CONTENT_AUTHORITY = "kuanying.popularmovies";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_MOVIE = "movie";
    public static final String CONTENT_TYPE =
            ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MOVIE;
    public static final String CONTENT_ITEM_TYPE =
            ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MOVIE;

    public MovieContract() {}

    public static final class MovieEntry implements BaseColumns {
        public static final String TABLE_NAME = "movie";
        public static final String COLUMN_NAME_TITLE = "title";
        public static final String COLUMN_POSTER_PATH = "poster_path";
        public static final String COLUMN_RELEASE_DATE = "release_date";
        public static final String COLUMN_OVERVIEW = "overview";
        public static final String COLUMN_VOTE_AVERAGE = "vote_averate";
        public static final String COLUMN_POPULARITY = "popularity";
        public static final String COLUMN_FAVORITE = "favorite";

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_MOVIE).build();

        public static final Uri buildUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public final static String SQL_CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " (" +
                _ID + " INTEGER PRIMARY KEY," +
                COLUMN_NAME_TITLE + " TEXT UNIQUE NOT NULL, " +
                COLUMN_POSTER_PATH + " TEXT NOT NULL, " +
                COLUMN_RELEASE_DATE + " TEXT, " +
                COLUMN_OVERVIEW + " TEXT, " +
                COLUMN_VOTE_AVERAGE + " REAL NOT NULL, " +
                COLUMN_POPULARITY + " REAL NOT NULL, " +
                COLUMN_FAVORITE + " INTEGER NOT NULL" +
                " );";

        public static final String SQL_DELETE_TABLE =
                "DROP TABLE IF EXISTS " + TABLE_NAME;
    }

    public static final class TrailerEntry implements BaseColumns {
        public static final String TABLE_NAME = "trailer";
        public static final String COLUMN_KEY = "key";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_SITE = "site";
        public static final String COLUMN_MOVIE_ID = "movie_id";

        public final static String SQL_CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + " (" +
                _ID + " INTEGER PRIMARY KEY," +
                COLUMN_KEY + " TEXT UNIQUE NOT NULL, " +
                COLUMN_NAME + " TEXT, " +
                COLUMN_SITE + " TEXT, " +
                COLUMN_MOVIE_ID + " INTEGER NOT NULL, " +
                " FOREIGN KEY (" + COLUMN_MOVIE_ID + ") REFERENCES " +
                MovieEntry.TABLE_NAME + " (" + MovieEntry._ID + ") " +
                " );";

        public static final String SQL_DELETE_TABLE =
                "DROP TABLE IF EXISTS " + TABLE_NAME;
    }

    public static final class ReviewEntry implements BaseColumns {
        public static final String TABLE_NAME = "review";
        //TODO
    }
}
