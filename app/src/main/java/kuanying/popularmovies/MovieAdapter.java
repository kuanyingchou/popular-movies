package kuanying.popularmovies;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import kuanying.popularmovies.data.MovieContract;

class MovieAdapter extends CursorAdapter {
    public MovieAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        ImageView imageView = new ImageView(context);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setLayoutParams(new GridView.LayoutParams(
                (int) context.getResources().getDimension(R.dimen.poster_width),
                (int) context.getResources().getDimension(R.dimen.poster_height)));
        return imageView;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        String posterPath = cursor.getString(
                cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_POSTER_PATH));
        Picasso.with(context).load(posterPath).into((ImageView)view);
    }

}
