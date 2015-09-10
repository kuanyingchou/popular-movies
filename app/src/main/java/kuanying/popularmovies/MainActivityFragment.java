package kuanying.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;

import org.parceler.Parcels;

import kuanying.popularmovies.data.MovieResult;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class MainActivityFragment extends Fragment {


    private final String LOG_TAG = MainActivityFragment.class.getSimpleName();
    private static final String SORT_POPULARITY = "popularity.desc";
    private static final String SORT_RATING = "vote_average.desc";
    private static final String SORT_FAVORITE = "favorites.desc";
    private static final String KEY_DATA = "movie_data";
    private static final String KEY_SORTING_METHOD = "sorting_method";
    private static final String KEY_POSITION = "position";
    private static final String KEY_ERROR = "error";

    private GridView movieGrid;
    private MovieAdapter movieAdapter;
    private String sortingMethod;
    private int lastPosition = 0;
    private MovieResult movieResult;
    private TextView errorView;
    private View errorPanel;

    public MainActivityFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        movieAdapter = new MovieAdapter(getActivity());
        movieGrid = (GridView) view.findViewById(R.id.movie_grid);
        movieGrid.setAdapter(movieAdapter);
        errorPanel = view.findViewById(R.id.error_panel);
        errorView = (TextView) view.findViewById(R.id.error_view);

        if(savedInstanceState!=null) {
            sortingMethod = savedInstanceState.getString(KEY_SORTING_METHOD);
            lastPosition = savedInstanceState.getInt(KEY_POSITION);
            movieResult = Parcels.unwrap(savedInstanceState.getParcelable(KEY_DATA));
            if(movieResult != null) {
                //Log.d(LOG_TAG, movieResult.toString());
                movieAdapter.setData(movieResult.getMovies());
            }
            String error = savedInstanceState.getString(KEY_ERROR);
            if(error != null && ! error.isEmpty()) {
                errorView.setText(error);
                errorPanel.setVisibility(View.VISIBLE);
            }

        } else {
            sortingMethod = SORT_POPULARITY;
            loadData();
        }

        setHasOptionsMenu(true);

        movieGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Intent intent = new Intent(
                        getActivity(), DetailActivity.class)
                        .putExtra("movie",
                                Parcels.wrap(movieAdapter.getItem(position)));

                startActivity(intent);
            }
        });

        Button reloadButton = (Button)view.findViewById(R.id.reload_button);
        reloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadData();
            }
        });

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_fragment, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_sort_by_popularity) {
            sortingMethod = SORT_POPULARITY;
        } else if(id == R.id.action_sort_by_rating) {
            sortingMethod = SORT_RATING;
        }
        loadData();
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(KEY_POSITION, movieGrid.getFirstVisiblePosition());
        outState.putString(KEY_SORTING_METHOD, sortingMethod);
        outState.putParcelable(KEY_DATA, Parcels.wrap(movieResult));
        outState.putString(KEY_ERROR, (errorPanel.getVisibility() == View.VISIBLE)?
                errorView.getText().toString():"");
        super.onSaveInstanceState(outState);
    }

    //ref: http://stackoverflow.com/questions/4238921/detect-whether-there-is-an-internet-connection-available-on-android
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
    private void loadData() {
        if(sortingMethod == SORT_FAVORITE) {

        } else {
            if(isNetworkAvailable()) {
                errorPanel.setVisibility(View.GONE);

                Utility.tmdbService.listMovies(sortingMethod, 1, Utility.MY_API_KEY, new Callback<MovieResult>() {
                    @Override
                    public void success(MovieResult result, Response response) {
                        if (result == null) {
                            return;
                        }
                        Log.d(LOG_TAG, "done loading!");
                        movieResult = result;
                        movieAdapter.setData(result.getMovies());
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        errorView.setText("Connection Error");
                        errorPanel.setVisibility(View.VISIBLE);
                    }
                });

            } else {
                errorView.setText("No Network Connnection");
                errorPanel.setVisibility(View.VISIBLE);
            }
        }

    }


}
