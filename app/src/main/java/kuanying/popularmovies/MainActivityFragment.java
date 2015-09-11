package kuanying.popularmovies;

import android.content.ContentResolver;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.TextUtils;
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
    //private static final String KEY_DATA_DISPLAYED= "updated";
    private static final int MOVIE_LOADER = 0;

    private GridView movieGrid;
    private MovieAdapter movieAdapter;
    private String sortingMethod;
    private int lastPosition = GridView.INVALID_POSITION;
    private TextView errorView;
    private View errorPanel;
    private View progressView;
    private View emptyView;

    interface ItemClickListener {
        public void onItemClick(long id);
    }

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

        if(savedInstanceState!=null) {
            onRestoreInstanceState(savedInstanceState);
        } else {
            sortingMethod = SORT_POPULARITY;
            update();
        }
        load();

        setHasOptionsMenu(true);

        movieGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Cursor cursor = (Cursor) adapterView.getItemAtPosition(position);
                if (cursor == null) {
                    return;
                }

                ((ItemClickListener) getActivity()).onItemClick(cursor.getLong(
                        cursor.getColumnIndex(MovieContract.MovieEntry._ID)));

            }
        });

        Button reloadButton = (Button)view.findViewById(R.id.reload_button);
        reloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                update();
            }
        });

        progressView = view.findViewById(R.id.movieGridProgress);
        emptyView = view.findViewById(R.id.movieGridEmpty);

        movieGrid.setEmptyView(emptyView);
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
        //TODO: back to top
        load();
        update();
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        //outState.putBoolean(KEY_DATA_DISPLAYED, movieAdapter.getCursor() != null);
        outState.putInt(KEY_POSITION, movieGrid.getFirstVisiblePosition());
        outState.putString(KEY_SORTING_METHOD, sortingMethod);
//        outState.putParcelable(KEY_DATA, Parcels.wrap(movieResult));
        outState.putString(KEY_ERROR, errorView.getText().toString());
        super.onSaveInstanceState(outState);
    }
    private void onRestoreInstanceState(Bundle inState) {
        sortingMethod = inState.getString(KEY_SORTING_METHOD);
        lastPosition = inState.getInt(KEY_POSITION);
        String error = inState.getString(KEY_ERROR);
        showError(error);
        load();
    }

    private void load() {
        Loader<?> loader = getLoaderManager().getLoader(MOVIE_LOADER);
        if(loader != null) {
            getLoaderManager().restartLoader(MOVIE_LOADER, null, this);
        } else {
            getLoaderManager().initLoader(MOVIE_LOADER, null, this);
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

        movieGrid.post(new Runnable() {
            @Override
            public void run() {
                //smoothScrollToPosition() didn't work
                //Somehow, this line has no effect without post()
                if(lastPosition != GridView.INVALID_POSITION) {
                    movieGrid.setSelection(lastPosition);
                }
            }
        });

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        movieAdapter.swapCursor(null);
    }


    private void update() {
//        movieAdapter.swapCursor(null);
        if(Utility.isNetworkAvailable(getActivity())) {
            movieGrid.setEmptyView(progressView); //TODO: progress bar
            Utility.tmdbService.listMovies(sortingMethod, 1,
                    Utility.MY_API_KEY, new Callback<MovieResult>() {
                        @Override
                        public void success(MovieResult result, Response response) {
                            showError(null);
                            if (result == null) {
                                return;
                            }

                            ContentResolver resolver = getActivity().getContentResolver();
                            for (Movie m : result.getMovies()) {
                                //check if the movie is already in the db

                                Cursor c = resolver.query(MovieContract.MovieEntry.buildUri(m.getId()),
                                        null, null, null, null);

                                if (c.moveToFirst()) {
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

                        }

                        @Override
                        public void failure(RetrofitError error) {
                            showError("Failed to Load Data");
                        }
                    });

            movieGrid.setEmptyView(null);
        } else {
            showError("No Network Connnection");
        }

    }

    private void showError(String message) {
        if(TextUtils.isEmpty(message)) {
            errorPanel.setVisibility(View.GONE);
        } else {
            errorView.setText(message);
            errorPanel.setVisibility(View.VISIBLE);
        }
    }
}
