package kuanying.popularmovies;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import retrofit.RestAdapter;

public class Utility {
    private static final RestAdapter restAdapter = new RestAdapter.Builder()
            .setEndpoint("http://api.themoviedb.org/3")
            .build();

    //TODO: move to TmdbService
    public static final TmdbService tmdbService = restAdapter.create(TmdbService.class);

    public static final String MY_API_KEY = "553ac01e8b3b3cc0c17b6489fca129a5";

    //ref: http://stackoverflow.com/questions/4238921/detect-whether-there-is-an-internet-connection-available-on-android
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

}
