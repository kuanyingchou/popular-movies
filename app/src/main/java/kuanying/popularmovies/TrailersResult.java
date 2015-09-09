package kuanying.popularmovies;

import org.parceler.Parcel;

import java.util.List;

@Parcel
public class TrailersResult {
    long id;
    List<Trailer> results;

    List<Trailer> getTrailers() {
        return results;
    }
}
