package kuanying.popularmovies;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
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
import kuanying.popularmovies.data.MovieResult;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class MainActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {


    private final String LOG_TAG = MainActivityFragment.class.getSimpleName();
    private static final String SORT_POPULARITY = "popularity.desc";
    private static final String SORT_RATING = "vote_average.desc";
    private static final String SORT_FAVORITE = "favorites.desc";
    private static final String KEY_DATA = "movie_data";
    private static final String KEY_SORTING_METHOD = "sorting_method";
    private static final String KEY_POSITION = "position";
    private static final String KEY_ERROR = "error";
    private static final String KEY_DATA_DISPLAYED= "updated";
    private static final int MOVIE_LOADER = 0;

    private GridView movieGrid;
    private MovieAdapter movieAdapter;
    private String sortingMethod;
    private int lastPosition = GridView.INVALID_POSITION;
    private TextView errorView;
    private View errorPanel;

    interface ItemClickListener {
        public void onItemClick(long id);
    }

    public MainActivityFragment() {}

    //TODO: update adapter after removing favorites: setNotificationUri
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        movieAdapter = new MovieAdapter(getActivity(), null, 0);
        movieGrid = (GridView) view.findViewById(R.id.movie_grid);
        movieGrid.setAdapter(movieAdapter);
        errorPanel = view.findViewById(R.id.error_panel);
        errorView = (TextView) view.findViewById(R.id.error_view);

        if(savedInstanceState!=null) {
            sortingMethod = savedInstanceState.getString(KEY_SORTING_METHOD);
            lastPosition = savedInstanceState.getInt(KEY_POSITION);
            String error = savedInstanceState.getString(KEY_ERROR);
            if(error != null && ! error.isEmpty()) {
                errorView.setText(error);
                errorPanel.setVisibility(View.VISIBLE);
            }
            boolean dataDisplayed = savedInstanceState.getBoolean(KEY_DATA_DISPLAYED);
            if(! dataDisplayed) {
                updateAndLoad();
            }
        } else {
            sortingMethod = SORT_POPULARITY;
            updateAndLoad();
        }
        load();

        setHasOptionsMenu(true);

        movieGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Cursor cursor = (Cursor) adapterView.getItemAtPosition(position);
                if(cursor == null) {
                    return;
                }

                ((ItemClickListener)getActivity()).onItemClick(cursor.getLong(
                        cursor.getColumnIndex(MovieContract.MovieEntry._ID)));


            }
        });

        Button reloadButton = (Button)view.findViewById(R.id.reload_button);
        reloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateAndLoad();
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
        updateAndLoad();
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(KEY_DATA_DISPLAYED, movieAdapter.getCursor() != null);
        outState.putInt(KEY_POSITION, movieGrid.getFirstVisiblePosition());
        outState.putString(KEY_SORTING_METHOD, sortingMethod);
//        outState.putParcelable(KEY_DATA, Parcels.wrap(movieResult));
        outState.putString(KEY_ERROR, (errorPanel.getVisibility() == View.VISIBLE) ?
                errorView.getText().toString() : "");
        super.onSaveInstanceState(outState);
    }

    //ref: http://stackoverflow.com/questions/4238921/detect-whether-there-is-an-internet-connection-available-on-android
    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void load() {
        getLoaderManager().initLoader(MOVIE_LOADER, null, this);
    }

    private void updateAndLoad() {
        if(isNetworkAvailable()) {
            Utility.tmdbService.listMovies(sortingMethod, 1,
                    Utility.MY_API_KEY, new Callback<MovieResult>() {
                @Override
                public void success(MovieResult result, Response response) {
                    errorPanel.setVisibility(View.GONE);
                    if (result == null) {
                        return;
                    }

                    ContentResolver resolver = getActivity().getContentResolver();
                    for(Movie m: result.getMovies()) {
                        //check if the movie is already in the db

                        Cursor c = resolver.query(MovieContract.MovieEntry.buildUri(m.getId()),
                                null, null, null, null);

                        if(c.moveToFirst()) {
                            //if it's in the db, update existing values
                            resolver.update(MovieContract.MovieEntry.buildUri(m.getId()),
                                    m.toContentValuesExcludeFavorite(), null, null);
                        } else {
                            //if it's not, insert the movie to the db
                            resolver.insert(MovieContract.MovieEntry.buildUri(m.getId()),
                                    m.toContentValues());
                        }
                        c.close();
                    }
                    load();

                }

                @Override
                public void failure(RetrofitError error) {
                    errorView.setText("Failed to Load Data");
                    errorPanel.setVisibility(View.VISIBLE);
                    load();
                }
            });

        } else {
            errorView.setText("No Network Connnection");
            errorPanel.setVisibility(View.VISIBLE);
            load();
        }

    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        final String[] columns = new String [] {
                MovieContract.MovieEntry._ID,
                MovieContract.MovieEntry.COLUMN_NAME_TITLE,
                MovieContract.MovieEntry.COLUMN_POSTER_PATH
        };

        String selection = null;
        String[] selectionArgs = null;
        String orderBy = null;
        if(sortingMethod == SORT_POPULARITY) {
            orderBy = MovieContract.MovieEntry.COLUMN_POPULARITY + " DESC";
        } else  if(sortingMethod == SORT_RATING) {
            orderBy = MovieContract.MovieEntry.COLUMN_VOTE_AVERAGE + " DESC";
        } else if(sortingMethod == SORT_FAVORITE) {
            selection = MovieContract.MovieEntry.COLUMN_FAVORITE + " = ?";
            selectionArgs = new String[] { String.valueOf(1) };
        }

        return new CursorLoader(
                getActivity(), MovieContract.MovieEntry.CONTENT_URI,
                null, selection, selectionArgs, orderBy);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        movieAdapter.swapCursor(data);
        movieGrid.smoothScrollToPosition(lastPosition);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        movieAdapter.swapCursor(null);
    }
}
