package kuanying.popularmovies;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.parceler.Parcels;

public class DetailActivityFragment extends Fragment {

    public DetailActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Movie m = Parcels.unwrap(getActivity().getIntent().getParcelableExtra("movie"));
        //Toast.makeText(getActivity(), m.toString(), Toast.LENGTH_SHORT).show();

        View view = inflater.inflate(R.layout.fragment_detail, container, false);
        updateView(m, view);

        return view;
    }

    private void updateView(Movie m, View view) {
        getActivity().setTitle("Details");

        //setBackdrop(m, view); //experimental

        ImageView posterView = (ImageView)view.findViewById(R.id.posterView);
        Picasso.with(getActivity()).load(m.getBigPosterUrl()).into(posterView);

        TextView titleView = (TextView)view.findViewById(R.id.titleView);
        titleView.setText(m.getTitle());

        TextView overviewView = (TextView)view.findViewById(R.id.overviewView);
        overviewView.setText(m.getOverview());

        TextView ratingView = (TextView)view.findViewById(R.id.ratingView);
        ratingView.setText(getString(R.string.rating_prefix) + m.getRating());

        TextView dateView = (TextView)view.findViewById(R.id.dateView);
        dateView.setText(getString(R.string.date_prefix) + m.getReleaseDate());
    }

    private void setBackdrop(Movie m, View view) {
        //experimental backdrop
        final LinearLayout ll = (LinearLayout)view.findViewById(R.id.detailView);
        Picasso.with(getActivity()).load(m.getBigPosterUrl()).into(new Target() {

            @Override
            @TargetApi(16)
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                ll.setBackground(new BitmapDrawable(getResources(), bitmap));
            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {

            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {

            }
        });
    }
}
