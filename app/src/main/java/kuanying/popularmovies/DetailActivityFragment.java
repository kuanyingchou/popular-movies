package kuanying.popularmovies;

import android.annotation.TargetApi;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
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

import java.util.List;

import kuanying.popularmovies.data.Movie;
import kuanying.popularmovies.data.Review;
import kuanying.popularmovies.data.ReviewResult;
import kuanying.popularmovies.data.Trailer;
import kuanying.popularmovies.data.TrailerResult;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class DetailActivityFragment extends Fragment {

    private Movie movie;
    private List<Trailer> trailers;
    private List<Review> reviews;

    public DetailActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        movie = Parcels.unwrap(getActivity().getIntent().getParcelableExtra("movie"));
        //Toast.makeText(getActivity(), movie.toString(), Toast.LENGTH_SHORT).show();

        final View view = inflater.inflate(R.layout.fragment_detail, container, false);
        updateView(view, inflater, container);

        return view;
    }

    private void updateView(View view, final LayoutInflater inflater, ViewGroup container) {

        getActivity().setTitle("Details");

        //setBackdrop(movie, view);

        ImageView posterView = (ImageView)view.findViewById(R.id.posterView);
        Picasso.with(getActivity()).load(movie.getPosterUrl()).into(posterView);

        TextView titleView = (TextView)view.findViewById(R.id.titleView);
        titleView.setText(movie.getTitle());

        TextView overviewView = (TextView)view.findViewById(R.id.overviewView);
        overviewView.setText(movie.getOverview());

        TextView ratingView = (TextView)view.findViewById(R.id.ratingView);
        ratingView.setText(getString(R.string.rating_prefix) + movie.getRating());

        TextView dateView = (TextView)view.findViewById(R.id.dateView);
        dateView.setText(getString(R.string.date_prefix) + movie.getReleaseDate());

        //TODO: show "No trailers" when empty
        //TODO: configuration change
        final LinearLayout trailerView = (LinearLayout)view.findViewById(R.id.trailerView);
        Utility.tmdbService.listTrailers(movie.getId(), Utility.MY_API_KEY, new Callback<TrailerResult>() {
            @Override
            public void success(TrailerResult trailerResult, Response response) {
                //Log.d("TEST", trailerResult.getTrailers().toString());
                trailers = trailerResult.getTrailers();
                for (int i = 0; i < trailers.size(); i++) { //TODO: upper limit
                    View itemView = createTrailerItem(inflater, i);
                    trailerView.addView(itemView);
                }
            }

            @Override
            public void failure(RetrofitError error) {

            }
        });

        final LinearLayout reviewView = (LinearLayout)view.findViewById(R.id.reviewView);
        Utility.tmdbService.listReviews(movie.getId(), Utility.MY_API_KEY, new Callback<ReviewResult>() {
            @Override
            public void success(ReviewResult reviewResult, Response response) {
                reviews = reviewResult.getReviews();
                for (int i = 0; i < reviews.size(); i++) { //TODO: upper limit
                    View itemView = createReviewItem(inflater, i);
                    reviewView.addView(itemView);
                }
            }

            @Override
            public void failure(RetrofitError error) {

            }
        });
    }

    private View createTrailerItem(LayoutInflater inflater, int position) {
        View trailerView = inflater.inflate(R.layout.trailer_item, null);
        trailerView.setId(position);
        final TextView textView = (TextView)trailerView.findViewById(R.id.trailerText);
        textView.setText(trailers.get(position).getName());
        final ImageView imageView = (ImageView)trailerView.findViewById(R.id.trailerImage);
        Picasso.with(getActivity()).load(trailers.get(position).getThumbnailLink()).into(imageView);

        trailerView.setOnClickListener(new TrailerClickListener());
        return trailerView;
    }

    private View createReviewItem(LayoutInflater inflater, int position) {
        View reviewView = inflater.inflate(R.layout.review_item, null);
        reviewView.setId(position);
        final TextView authorView = (TextView)reviewView.findViewById(R.id.reviewAuthor);
        authorView.setText(reviews.get(position).getAuthor());
        final TextView contentView = (TextView)reviewView.findViewById(R.id.reviewContent);
        contentView.setText(reviews.get(position).getContent());
        return reviewView;
    }

    //experimental
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

    private class TrailerClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            //ref: http://developer.android.com/training/basics/intents/sending.html#AppChooser
            final Uri uri = trailers.get(v.getId()).getVideoUri();
            final Intent intent = new Intent(Intent.ACTION_VIEW, uri);

            //TODO: Somehow Youtube is the only target on my phone
            //Log.d("TEST", ""+getActivity().getPackageManager().queryIntentActivities(intent, 0));

            final Intent chooser = Intent.createChooser(intent, "Choose an App");
            if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
                startActivity(chooser);
            }
            startActivity(intent);
        }
    }
}
