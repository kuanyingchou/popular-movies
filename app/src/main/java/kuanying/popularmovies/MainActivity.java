package kuanying.popularmovies;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.ViewGroup;

public class MainActivity extends AppCompatActivity implements MainActivityFragment.ItemClickListener {
    private static final String DETAILFRAGMENT_TAG = "detail";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }

    @Override
    public void onItemClick(long id) {
        ViewGroup detailPane = (ViewGroup)findViewById(R.id.detailFragmentContainer);
        if(detailPane != null) {
            Bundle args = new Bundle();
            args.putLong("movie_id", id);
            DetailActivityFragment fragment = new DetailActivityFragment();
            fragment.setArguments(args);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.detailFragmentContainer, fragment, DETAILFRAGMENT_TAG)
                    .commit();
        } else {
            Intent intent = new Intent(this, DetailActivity.class).putExtra("movie_id", id);
            startActivity(intent);
        }
    }
}
