package kuanying.popularmovies;

import kuanying.popularmovies.data.MovieResult;
import kuanying.popularmovies.data.ReviewResult;
import kuanying.popularmovies.data.TrailerResult;
import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.http.GET;
import retrofit.http.Path;
import retrofit.http.Query;

public interface TmdbService {

    RestAdapter REST_ADAPTER = new RestAdapter.Builder()
            .setEndpoint("http://api.themoviedb.org/3")
            .build();

    TmdbService INSTANCE = REST_ADAPTER.create(TmdbService.class);
    String MY_API_KEY = "553ac01e8b3b3cc0c17b6489fca129a5";

    @GET("/discover/movie")
    void listMovies(
            @Query("sort_by") String sortBy,
            @Query("page") int page,
            @Query("api_key") String apiKey,
            Callback<MovieResult> callback);

    @GET("/movie/{id}/videos")
    void listTrailers(
            @Path("id") long id,
            @Query("api_key") String apiKey,
            Callback<TrailerResult> callback);

    @GET("/movie/{id}/reviews")
    void listReviews(
            @Path("id") long id,
            @Query("api_key") String apiKey,
            Callback<ReviewResult> callback);
}
