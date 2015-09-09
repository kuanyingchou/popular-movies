package kuanying.popularmovies.data;

import org.parceler.Parcel;

import java.util.List;

@Parcel
public class TrailerResult {
    long id;
    List<Trailer> results;

    public List<Trailer> getTrailers() {
        return results;
    }
}
