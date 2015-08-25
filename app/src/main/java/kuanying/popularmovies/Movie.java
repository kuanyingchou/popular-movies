package kuanying.popularmovies;

import com.google.gson.annotations.SerializedName;

import org.parceler.Parcel;

@Parcel
public class Movie {
    private static final String IMAGE_BASE = "http://image.tmdb.org/t/p/w185/";
    private static final String BIG_IMAGE_BASE = "http://image.tmdb.org/t/p/w500/";

    long id;
    @SerializedName("original_title") String title;
    @SerializedName("poster_path") String posterUrl;
    @SerializedName("primary_release_date") String releaseDate;
    @SerializedName("overview") String overview;
    @SerializedName("vote_average") double rating;

    public Movie() {}

//    public Movie(long id, String title, String poster, String date, String overview, double rating) {
//        this.id = id;
//        this.title = title;
//        this.posterUrl = poster;
//        this.releaseDate = date;
//        this.overview = overview;
//        this.rating = rating;
//    }

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



}