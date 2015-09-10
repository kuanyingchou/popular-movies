package kuanying.popularmovies.data;

import android.content.ContentValues;

import com.google.gson.annotations.SerializedName;

import org.parceler.Parcel;

@Parcel
public class Movie {
    private static final String IMAGE_BASE = "http://image.tmdb.org/t/p/w300/";
    private static final String BIG_IMAGE_BASE = "http://image.tmdb.org/t/p/w500/";

    long id;
    @SerializedName("original_title") String title;
    @SerializedName("poster_path") String posterUrl;
    @SerializedName("release_date") String releaseDate;
    @SerializedName("overview") String overview;
    @SerializedName("vote_average") double rating;
    double popularity;
    boolean isFavorite; 

    public Movie() {}

    public String getPosterUrl() { return IMAGE_BASE+posterUrl; }
    public String getBigPosterUrl() {
        return BIG_IMAGE_BASE+posterUrl;
    }
    public long getId() {
        return id;
    }
    public String getTitle() {
        return title;
    }
    public String getReleaseDate() {
        return releaseDate;
    }
    public String getOverview() {
        return overview;
    }
    public double getRating() {
        return rating;
    }
    public double getPopularity() { return popularity; }
    public boolean getIsFavorite() { return isFavorite; }

    @Override
    public String toString() {
        return title +"("+id+")";
    }

    public ContentValues toContentValues() {
        ContentValues values = new ContentValues();
        values.put(MovieContract.MovieEntry._ID, this.getId());
        values.put(MovieContract.MovieEntry.COLUMN_NAME_TITLE, this.getTitle());
        values.put(MovieContract.MovieEntry.COLUMN_OVERVIEW, this.getOverview());
        values.put(MovieContract.MovieEntry.COLUMN_POSTER_PATH, this.getPosterUrl());
        values.put(MovieContract.MovieEntry.COLUMN_RELEASE_DATE, this.getReleaseDate());
        values.put(MovieContract.MovieEntry.COLUMN_VOTE_AVERAGE, this.getRating());
        values.put(MovieContract.MovieEntry.COLUMN_POPULARITY, this.getPopularity());
        values.put(MovieContract.MovieEntry.COLUMN_FAVORITE, this.getIsFavorite());
        return values;
    }

}