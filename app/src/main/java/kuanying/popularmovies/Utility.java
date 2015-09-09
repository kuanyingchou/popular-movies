package kuanying.popularmovies;

import retrofit.RestAdapter;

public class Utility {
    private static final RestAdapter restAdapter = new RestAdapter.Builder()
            .setEndpoint("http://api.themoviedb.org/3")
            .build();

    public static final TmdbService tmdbService = restAdapter.create(TmdbService.class);

    public static final String MY_API_KEY = "553ac01e8b3b3cc0c17b6489fca129a5";
}
