package kuanying.popularmovies.data;

import org.parceler.Parcel;

import java.util.List;

@Parcel
public class ReviewResult {
    long id;
    int page;
    List<Review> results;
    public List<Review> getReviews() {
        return results;
    }
}
