package kuanying.popularmovies;

import com.google.gson.annotations.SerializedName;

import org.parceler.Parcel;

import java.util.List;

@Parcel
public class DiscoverResult {
    long page;
    @SerializedName("total_pages") long totalPages;
    @SerializedName("total_results") long totalResults;
    List<Movie> results;

    public DiscoverResult() {}

    public List<Movie> getMovies() {
        return results;
    }

    @Override
    public String toString() {
        return String.format("%d / %d, total: %d result", page, totalPages, totalResults);
    }
}
