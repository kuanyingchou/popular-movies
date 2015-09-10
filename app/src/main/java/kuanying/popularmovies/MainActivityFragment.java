package kuanying.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
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

import kuanying.popularmovies.data.Movie;
import kuanying.popularmovies.data.MovieContract;
import kuanying.popularmovies.data.MovieDbHelper;
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
    private TextView errorView;
    private View errorPanel;

    private MovieDbHelper dbHelper;

    public MainActivityFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        movieAdapter = new MovieAdapter(getActivity(), null, 0);
        movieGrid = (GridView) view.findViewById(R.id.movie_grid);
        movieGrid.setAdapter(movieAdapter);
        errorPanel = view.findViewById(R.id.error_panel);
        errorView = (TextView) view.findViewById(R.id.error_view);

        dbHelper = new MovieDbHelper(getActivity());

        if(savedInstanceState!=null) {
            sortingMethod = savedInstanceState.getString(KEY_SORTING_METHOD);
            lastPosition = savedInstanceState.getInt(KEY_POSITION);
            //movieResult = Parcels.unwrap(savedInstanceState.getParcelable(KEY_DATA));
//            if(movieResult != null) {
//                //Log.d(LOG_TAG, movieResult.toString());
//                movieAdapter.setData(movieResult.getMovies());
//            }
            String error = savedInstanceState.getString(KEY_ERROR);
            if(error != null && ! error.isEmpty()) {
                errorView.setText(error);
                errorPanel.setVisibility(View.VISIBLE);
            }

        } else {
            sortingMethod = SORT_POPULARITY;
        }
        loadData(); //TODO: don't use network on configuration change

        setHasOptionsMenu(true);

        movieGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Cursor cursor = (Cursor) adapterView.getItemAtPosition(position);
                if(cursor == null) {
                    return;
                }

                Intent intent = new Intent(
                        getActivity(), DetailActivity.class)
                        .putExtra("movie_id", cursor.getLong(
                                cursor.getColumnIndex(MovieContract.MovieEntry._ID)));

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
        } else if(id == R.id.action_sort_by_favorites) {
            sortingMethod = SORT_FAVORITE;
        }
        loadData();
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(KEY_POSITION, movieGrid.getFirstVisiblePosition());
        outState.putString(KEY_SORTING_METHOD, sortingMethod);
//        outState.putParcelable(KEY_DATA, Parcels.wrap(movieResult));
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
        final String[] columns = new String [] {
                MovieContract.MovieEntry._ID,
                MovieContract.MovieEntry.COLUMN_NAME_TITLE,
                MovieContract.MovieEntry.COLUMN_POSTER_PATH
        };
        if(sortingMethod == SORT_FAVORITE) {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            String selection = MovieContract.MovieEntry.COLUMN_FAVORITE + " = ?";
            String[] selectionArgs = { String.valueOf(1) };
            String orderBy = MovieContract.MovieEntry.COLUMN_POPULARITY + " DESC"; //TODO: sorting
            Cursor c = db.query(MovieContract.MovieEntry.TABLE_NAME,
                    columns, selection, selectionArgs, null, null, orderBy); //TODO: limit
            movieAdapter.swapCursor(c);
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

                        SQLiteDatabase db = dbHelper.getWritableDatabase();
                        for(Movie m: result.getMovies()) {
                            //check if the movie is already in the db
                            String selection = MovieContract.MovieEntry._ID + " = ?";
                            String[] selectionArgs = { String.valueOf(m.getId()) };
                            Cursor c = db.query(MovieContract.MovieEntry.TABLE_NAME,
                                    null, selection, selectionArgs, null, null, null);

                            if(c.moveToFirst()) {
                                //if it's in the db, update existing values
                                db.update(MovieContract.MovieEntry.TABLE_NAME,
                                        m.toContentValuesExcludeFavorite(),
                                        selection, selectionArgs);
                            } else {
                                //if it's not, insert the movie to the db
                                db.insert(MovieContract.MovieEntry.TABLE_NAME, null,
                                        m.toContentValues());
                            }
                            c.close();
                        }
                        String orderBy = MovieContract.MovieEntry.COLUMN_POPULARITY + " DESC"; //TODO: sorting
                        Cursor c = db.query(MovieContract.MovieEntry.TABLE_NAME,
                                columns, null, null, null, null, orderBy, "20"); //TODO: limit
                        movieAdapter.swapCursor(c);
                        //movieResult = result;
                        //movieAdapter.setData(result.getMovies());
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
