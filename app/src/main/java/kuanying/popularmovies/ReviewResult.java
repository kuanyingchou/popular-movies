package kuanying.popularmovies;

import org.parceler.Parcel;

import java.util.List;

@Parcel
public class ReviewResult {
    long id;
    int page;
    List<Review> results;
}
