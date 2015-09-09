package kuanying.popularmovies;

import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.Path;
import retrofit.http.Query;

public interface TmdbService {

    @GET("/discover/movie")
    void listMovies(
            @Query("sort_by") String sortBy,
            @Query("page") int page,
            @Query("api_key") String apiKey,
            Callback<DiscoverResult> callback);

    @GET("/movie/{id}/videos")
    void listTrailers(
            @Path("id") long id,
            @Query("api_key") String apiKey,
            Callback<TrailersResult> callback);

    @GET("/movie/{id}/reviews")
    void listReviews(
            @Path("id") long id,
            @Query("api_key") String apiKey,
            Callback<ReviewResult> callback);
}
