package kuanying.popularmovies;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {
    //examples:
    //  most popular movies: /discover/movie?sort_by=popularity.desc
    //  highest rated R movies:
    //    /discover/movie/?certification_country=US&certification=R&sort_by=vote_average.desc
    private final String LOG_TAG = MainActivityFragment.class.getSimpleName();
    private static final String MY_API_KEY = "553ac01e8b3b3cc0c17b6489fca129a5";
    private static final String IMAGE_BASE = "http://image.tmdb.org/t/p/w185/";

    private ImageView console;
    private MovieAdapter movieAdapter;

    public MainActivityFragment() {

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
                imageView.setLayoutParams(new GridView.LayoutParams(480, 480));
                //imageView.setScaleType(ImageView.ScaleType.);
                //imageView.setPadding(8, 8, 8, 8);
            }
            Log.d(LOG_TAG, "loading "+movies.get(position).getPosterUrl());
            Picasso.with(getActivity()).load(movies.get(position).getPosterUrl()).into(imageView);
            return imageView;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        movieAdapter = new MovieAdapter();
        //console = (ImageView) view.findViewById(R.id.consoleView);
        final GridView movieGrid = (GridView) view.findViewById(R.id.movie_grid);
        movieGrid.setAdapter(movieAdapter);

        new DataLoader().execute();

        return view;
    }

    class DataLoader extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;
            String jsonStr = null;

            try {

                final String BASE_URL =
                        "http://api.themoviedb.org/3/discover/movie";
                final String SORT_PARAM = "sort_by";
                final String KEY_PARAM = "api_key";

                Uri builtUri = Uri.parse(BASE_URL).buildUpon()
                        .appendQueryParameter(SORT_PARAM, "popularity.desc")
                        .appendQueryParameter(KEY_PARAM, MY_API_KEY)
                        .build();

                URL url = new URL(builtUri.toString());

                // Create the request to OpenWeatherMap, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuilder buffer = new StringBuilder();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line);
                    buffer.append("\n");
                    //Log.d(LOG_TAG, line);
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                jsonStr = buffer.toString();
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }

            return jsonStr;
        }

        @Override
        protected void onPostExecute(String s) {
            if(s == null) { return; }
            Log.d(LOG_TAG, "done loading!");
            //console.setText(s);
            try {
                JSONObject jsonObj = new JSONObject(s);
                JSONArray resultArray = jsonObj.getJSONArray("results");

                movieAdapter.clear();
                for(int i = 0; i<resultArray.length(); i++) {
                    JSONObject movieJsonObj = resultArray.getJSONObject(i);
                    Movie m = new Movie(
                        movieJsonObj.getLong("id"),
                        movieJsonObj.getString("title"),
                        IMAGE_BASE+movieJsonObj.getString("poster_path"));
                    movieAdapter.add(m);
                    Log.d(LOG_TAG, m.toString());

                }
                movieAdapter.notifyDataSetChanged();

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }
}
