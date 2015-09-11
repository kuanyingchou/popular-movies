package kuanying.popularmovies;

import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.parceler.Parcels;

import java.util.List;

import kuanying.popularmovies.data.Movie;
import kuanying.popularmovies.data.MovieContract;
import kuanying.popularmovies.data.Review;
import kuanying.popularmovies.data.ReviewResult;
import kuanying.popularmovies.data.Trailer;
import kuanying.popularmovies.data.TrailerResult;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class DetailActivityFragment extends Fragment {

    private static final String KEY_TRAILERS = "trailers";
    private static final String KEY_REVIEWS = "reviews";

    private Movie movie;
    private TrailerResult trailerResult;
    private ReviewResult reviewResult;
    private ShareActionProvider shareActionProvider;
    private Intent shareIntent = createShareIntent();

    public DetailActivityFragment() {
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        long id = getArguments().getLong("movie_id");

        ContentResolver resolver = getActivity().getContentResolver();
        Cursor c = resolver.query(MovieContract.MovieEntry.buildUri(id),
                null, null, null, null);
        if(c.moveToFirst()) {
            ContentValues values = new ContentValues();
            DatabaseUtils.cursorRowToContentValues(c, values);
            movie = Movie.fromContentValues(values);
        }

        //Toast.makeText(getActivity(), movie.toString(), Toast.LENGTH_SHORT).show();
        setHasOptionsMenu(true);

        final View view = inflater.inflate(R.layout.fragment_detail, container, false);
        if(movie == null) return view; //TODO: show empty msg?

        getActivity().setTitle(movie.getTitle()); //TODO: tablet?

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

        //TODO: show "No data" when empty
        final ViewGroup trailerView = (ViewGroup)view.findViewById(R.id.trailerView);
        final ViewGroup reviewView = (ViewGroup)view.findViewById(R.id.reviewView);

        if(savedInstanceState != null) {
            trailerResult = Parcels.unwrap(savedInstanceState.getParcelable(KEY_TRAILERS));
            updateTrailerView(trailerView, inflater);
            reviewResult = Parcels.unwrap(savedInstanceState.getParcelable(KEY_REVIEWS));
            updateReviewView(reviewView, inflater);
        } else {
            //TODO: upper limit
            TmdbService.INSTANCE.listTrailers(movie.getId(), TmdbService.MY_API_KEY,
                    new Callback<TrailerResult>() {
                @Override
                public void success(TrailerResult tr, Response response) {
                    //Log.d("TEST", trailerResult.getTrailers().toString());
                    trailerResult = tr;
                    updateTrailerView(trailerView, inflater);
                    updateShareIntent();
                }

                @Override
                public void failure(RetrofitError error) {

                }
            });
            TmdbService.INSTANCE.listReviews(movie.getId(), TmdbService.MY_API_KEY,
                    new Callback<ReviewResult>() {
                        @Override
                        public void success(ReviewResult rr, Response response) {
                            reviewResult = rr;
                            updateReviewView(reviewView, inflater);
                        }

                        @Override
                        public void failure(RetrofitError error) {

                        }
                    });
        }

        ToggleButton toggle = (ToggleButton)view.findViewById(R.id.favoriteButton);
        toggle.setChecked(movie.getIsFavorite());
        toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    addToFavorites();
                    Toast.makeText(getActivity(), "Added to Favorites", Toast.LENGTH_SHORT).show();
                } else {
                    removeFromFavorites();
                    Toast.makeText(getActivity(), "Removed from Favorites", Toast.LENGTH_SHORT).show();
                }
            }
        });

        return view;
    }

    private void addToFavorites() {
        movie.setIsFavorite(true);
        updateFavorites();
    }

    private void removeFromFavorites() {
        movie.setIsFavorite(false);
        updateFavorites();
    }

    private void updateFavorites() {
        ContentResolver resolver = getActivity().getContentResolver();

        ContentValues values = new ContentValues();
        values.put(MovieContract.MovieEntry.COLUMN_FAVORITE, movie.getIsFavorite());

        resolver.update(MovieContract.MovieEntry.buildUri(movie.getId()),
                values,
                null, null);
    }

    private void updateTrailerView(ViewGroup trailerView, LayoutInflater inflater) {
        if(trailerView==null) return;
        List<Trailer> trailers = trailerResult.getTrailers();
        for (int i = 0; i < trailers.size(); i++) {
            View itemView = createTrailerItem(inflater, i);
            trailerView.addView(itemView);
        }
    }

    private void updateReviewView(ViewGroup reviewView, LayoutInflater inflater) {
        if(reviewView==null) return;
        List<Review> reviews = reviewResult.getReviews();
        for (int i = 0; i < reviews.size(); i++) {
            View itemView = createReviewItem(inflater, i);
            reviewView.addView(itemView);
        }
    }

    private View createTrailerItem(LayoutInflater inflater, int position) {
        View trailerView = inflater.inflate(R.layout.trailer_item, null);
        trailerView.setId(position);
        final TextView textView = (TextView)trailerView.findViewById(R.id.trailerText);
        textView.setText(trailerResult.getTrailers().get(position).getName());
        final ImageView imageView = (ImageView)trailerView.findViewById(R.id.trailerImage);
        //TODO: ripple effect
        //TODO: play icon overlap
        Picasso.with(getActivity()).load(
                trailerResult.getTrailers().get(position).getThumbnailLink()).into(imageView);

        trailerView.setOnClickListener(new TrailerClickListener());
        return trailerView;
    }

    private View createReviewItem(LayoutInflater inflater, int position) {
        View reviewView = inflater.inflate(R.layout.review_item, null);
        reviewView.setId(position);
        final TextView authorView = (TextView)reviewView.findViewById(R.id.reviewAuthor);
        authorView.setText(reviewResult.getReviews().get(position).getAuthor());
        final TextView contentView = (TextView)reviewView.findViewById(R.id.reviewContent);
        contentView.setText(reviewResult.getReviews().get(position).getContent());
        return reviewView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(KEY_TRAILERS, Parcels.wrap(trailerResult));
        outState.putParcelable(KEY_REVIEWS, Parcels.wrap(reviewResult));
        super.onSaveInstanceState(outState);
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
            final Uri uri = trailerResult.getTrailers().get(v.getId()).getVideoUri();
            final Intent intent = new Intent(Intent.ACTION_VIEW, uri);

            //TODO: Somehow the chooser doesn't show up on my phone
            final Intent chooser = Intent.createChooser(intent, "Choose an App");
            if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
                startActivity(chooser);
            }
            startActivity(intent);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_detail, menu);
        MenuItem menuItem = menu.findItem(R.id.action_share);
        shareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(menuItem);
        shareActionProvider.setShareIntent(createShareIntent());
        super.onCreateOptionsMenu(menu, inflater);
    }

    //from Sunshine
    private Intent createShareIntent() {
        shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        return shareIntent;
    }
    private void updateShareIntent() {
        String msg = movie.getTitle() + " ";
        if(trailerResult!=null && trailerResult.getTrailers().size() > 0) {
            msg += trailerResult.getTrailers().get(0).getVideoUri();
        }
        shareIntent.putExtra(Intent.EXTRA_TEXT, msg);
    }
}
