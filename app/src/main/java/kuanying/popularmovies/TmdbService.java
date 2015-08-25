package kuanying.popularmovies;

import retrofit.http.GET;
import retrofit.http.Query;

public interface TmdbService {
    @GET("/3/discover/movie")
    DiscoverResult listMovies(
            @Query("sort_by") String sortBy,
            @Query("page") int page,
            @Query("api_key") String apiKey);
}
