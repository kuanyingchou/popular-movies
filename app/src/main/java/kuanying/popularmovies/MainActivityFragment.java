package kuanying.popularmovies;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
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
import android.widget.GridView;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

import retrofit.RestAdapter;

public class MainActivityFragment extends Fragment {


    private final String LOG_TAG = MainActivityFragment.class.getSimpleName();
    private static final String MY_API_KEY = "553ac01e8b3b3cc0c17b6489fca129a5";
    private static final String SORT_POPULARITY = "popularity.desc";
    private static final String SORT_RATING = "vote_average.desc";
    private static final String KEY_DATA = "movie_data";
    private static final String KEY_SORTING_METHOD = "sorting_method";
    private static final String KEY_POSITION = "position";

    private GridView movieGrid;
    private MovieAdapter movieAdapter;
    private String sortingMethod;
    private int lastPosition = 0;
    private DiscoverResult discoverResult;

    public MainActivityFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        movieAdapter = new MovieAdapter();

        movieGrid = (GridView) view.findViewById(R.id.movie_grid);
        movieGrid.setAdapter(movieAdapter);

        if(savedInstanceState!=null) {
            sortingMethod = savedInstanceState.getString(KEY_SORTING_METHOD);
            lastPosition = savedInstanceState.getInt(KEY_POSITION);
            discoverResult = Parcels.unwrap(savedInstanceState.getParcelable(KEY_DATA));
            movieAdapter.setData(discoverResult.getMovies());
            Log.d(LOG_TAG, discoverResult.toString());
        } else {
            sortingMethod = SORT_POPULARITY;
            new DataLoader().execute(sortingMethod);
        }

        setHasOptionsMenu(true);

        movieGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Intent intent = new Intent(
                        getActivity(), DetailActivity.class)
                        .putExtra("movie", (Parcelable) movieAdapter.getItem(position));

                startActivity(intent);
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
        new DataLoader().execute(sortingMethod);
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(KEY_POSITION, movieGrid.getFirstVisiblePosition());
        outState.putString(KEY_SORTING_METHOD, sortingMethod);
        outState.putParcelable(KEY_DATA, Parcels.wrap(discoverResult));
        super.onSaveInstanceState(outState);
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
                imageView.setLayoutParams(new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT));
                imageView.setLayoutParams(new GridView.LayoutParams(480, 480)); //TODO

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

    class DataLoader extends AsyncTask<String, Void, DiscoverResult> {

        @Override
        protected DiscoverResult doInBackground(String... params) {

            return fetchDataRetrofit(params[0]);
        }

        private DiscoverResult fetchDataRetrofit(String sortingMethod) {
            RestAdapter restAdapter = new RestAdapter.Builder()
                    .setEndpoint("http://api.themoviedb.org")
                    .build();

            TmdbService service = restAdapter.create(TmdbService.class);
            return service.listMovies(sortingMethod, 1, MY_API_KEY);
        }

        @Override
        protected void onPostExecute(DiscoverResult s) {
            if(s == null) { return; }
            Log.d(LOG_TAG, "done loading!");
            discoverResult = s;
            movieAdapter.setData(s.getMovies());
            movieGrid.setSelection(lastPosition);
        }
    }

}
