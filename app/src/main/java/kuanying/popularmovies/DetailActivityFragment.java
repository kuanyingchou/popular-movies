package kuanying.popularmovies;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailActivityFragment extends Fragment {

    public DetailActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Movie m = getActivity().getIntent().getParcelableExtra("movie");

        getActivity().setTitle("Details");

        Toast.makeText(getActivity(), m.toString(), Toast.LENGTH_SHORT).show();

        View view = inflater.inflate(R.layout.fragment_detail, container, false);

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

        return view;
    }
}
