package kuanying.popularmovies;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import kuanying.popularmovies.data.Movie;

class MovieAdapter extends BaseAdapter {
    private List<Movie> movies = new ArrayList<>();
    private Context context;

    public MovieAdapter(Context c)  {
        super();
        context = c;
    }

    public void clear() {
        movies.clear();
    }

    public void add(Movie m) {
        movies.add(m);
    }

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
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        if (convertView != null) {
            imageView = (ImageView) convertView;
        } else {
            imageView = new ImageView(context);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

            imageView.setLayoutParams(new GridView.LayoutParams(
                    (int) context.getResources().getDimension(R.dimen.poster_width),
                    (int) context.getResources().getDimension(R.dimen.poster_height)));

        }
        //Log.d(LOG_TAG, "loading "+movies.get(position).getPosterUrl());
        Picasso.with(context).load(movies.get(position).getPosterUrl()).into(imageView);
        return imageView;
    }

    public void setData(List<Movie> movies) {
        this.movies = movies;
        notifyDataSetChanged();
    }
}
