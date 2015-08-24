package kuanying.popularmovies;

import android.os.Parcel;
import android.os.Parcelable;

public class Movie implements Parcelable {
    private static final String IMAGE_BASE = "http://image.tmdb.org/t/p/w185/";
    private static final String BIG_IMAGE_BASE = "http://image.tmdb.org/t/p/w500/";

    private long id;
    private String title;
    private String posterUrl;
    private String releaseDate;
    private String overview;
    private double rating;

    public Movie(long id, String title, String poster, String date, String overview, double rating) {
        this.id = id;
        this.title = title;
        this.posterUrl = poster;
        this.releaseDate = date;
        this.overview = overview;
        this.rating = rating;
    }

    public String getPosterUrl() {
        return IMAGE_BASE+posterUrl;
    }
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


    @Override
    public String toString() {
        return title +"("+id+")";
    }

    // Parcelable methods
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeLong(id);
        out.writeString(title);
        out.writeString(posterUrl);
        out.writeString(releaseDate);
        out.writeString(overview);
        out.writeDouble(rating);
    }

    public static final Parcelable.Creator<Movie> CREATOR
            = new Parcelable.Creator<Movie>() {
        public Movie createFromParcel(Parcel in) {
            return new Movie(in);
        }

        public Movie[] newArray(int size) {
            return new Movie[size];
        }
    };

    private Movie(Parcel in) {
        id = in.readLong();
        title = in.readString();
        posterUrl = in.readString();
        releaseDate = in.readString();
        overview = in.readString();
        rating = in.readDouble();
    }
}