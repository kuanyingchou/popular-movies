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
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class MainActivityFragment extends Fragment {


    private final String LOG_TAG = MainActivityFragment.class.getSimpleName();
    private static final String SORT_POPULARITY = "popularity.desc";
    private static final String SORT_RATING = "vote_average.desc";
    private static final String KEY_DATA = "movie_data";
    private static final String KEY_SORTING_METHOD = "sorting_method";
    private static final String KEY_POSITION = "position";
    private static final String KEY_ERROR = "error";

    private GridView movieGrid;
    private MovieAdapter movieAdapter;
    private String sortingMethod;
    private int lastPosition = 0;
    private DiscoverResult discoverResult;
    private TextView errorView;
    private View errorPanel;

    public MainActivityFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        movieAdapter = new MovieAdapter();
        movieGrid = (GridView) view.findViewById(R.id.movie_grid);
        movieGrid.setAdapter(movieAdapter);
        errorPanel = view.findViewById(R.id.error_panel);
        errorView = (TextView) view.findViewById(R.id.error_view);

        if(savedInstanceState!=null) {
            sortingMethod = savedInstanceState.getString(KEY_SORTING_METHOD);
            lastPosition = savedInstanceState.getInt(KEY_POSITION);
            discoverResult = Parcels.unwrap(savedInstanceState.getParcelable(KEY_DATA));
            if(discoverResult != null) {
                //Log.d(LOG_TAG, discoverResult.toString());
                movieAdapter.setData(discoverResult.getMovies());
            }
            String error = savedInstanceState.getString(KEY_ERROR);
            if(error != null && ! error.isEmpty()) {
                errorView.setText(error);
                errorPanel.setVisibility(View.VISIBLE);
            }

        } else {
            sortingMethod = SORT_POPULARITY;
            updateData();
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
                updateData();
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
        updateData();
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(KEY_POSITION, movieGrid.getFirstVisiblePosition());
        outState.putString(KEY_SORTING_METHOD, sortingMethod);
        outState.putParcelable(KEY_DATA, Parcels.wrap(discoverResult));
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
    private void updateData() {
        if(isNetworkAvailable()) {
            errorPanel.setVisibility(View.GONE);

            Utility.tmdbService.listMovies(sortingMethod, 1, Utility.MY_API_KEY, new Callback<DiscoverResult>() {
                @Override
                public void success(DiscoverResult result, Response response) {
                    if (result == null) {
                        return;
                    }
                    Log.d(LOG_TAG, "done loading!");
                    discoverResult = result;
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

    class MovieAdapter extends BaseAdapter {
        private List<Movie> movies = new ArrayList<>();

        public MovieAdapter() {
            super();
        }

        public void clear() { movies.clear(); }
        public void add(Movie m) { movies.add(m); }

        @Override
        public int getCount() {
            return movies.size();
        }

        @Override
        public Object getItem(int position) {
            return movies.get(position);
        }

        @Override
        public long getItemId(int position) {
            return movies.get(position).getId();
        }

        @Override
        public View getView (int position, View convertView, ViewGroup parent) {
            ImageView imageView;
            if(convertView != null) {
                imageView = (ImageView) convertView;
            } else {
                imageView = new ImageView(getActivity());
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

                imageView.setLayoutParams(new GridView.LayoutParams(
                        (int) getResources().getDimension(R.dimen.poster_width),
                        (int) getResources().getDimension(R.dimen.poster_height)));

            }
            //Log.d(LOG_TAG, "loading "+movies.get(position).getPosterUrl());
            Picasso.with(getActivity()).load(movies.get(position).getPosterUrl()).into(imageView);
            return imageView;
        }

        public void setData(List<Movie> movies) {
            this.movies = movies;
            movieAdapter.notifyDataSetChanged();
        }
    }



}
