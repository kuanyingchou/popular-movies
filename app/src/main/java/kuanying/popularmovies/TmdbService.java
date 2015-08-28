package kuanying.popularmovies;

import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.Query;

public interface TmdbService {
    @GET("/3/discover/movie")
    void listMovies(
            @Query("sort_by") String sortBy,
            @Query("page") int page,
            @Query("api_key") String apiKey,
            Callback<DiscoverResult> callback);
}
