package kuanying.popularmovies;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import kuanying.popularmovies.data.MovieContract;

class MovieAdapter extends CursorAdapter {
    public MovieAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(context);
        return inflater.inflate(R.layout.movie_item, null);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        String posterPath = cursor.getString(
                cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_POSTER_PATH));
        Picasso.with(context).load(posterPath).into((ImageView) view);
    }

}
