package sumatodev.com.social.ui.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import sumatodev.com.social.R;
import sumatodev.com.social.ui.fragments.FollowersFragment;

public class FollowersActivity extends AppCompatActivity {

    public static final String USER_ID_EXTRA_KEY = "FollowersActivity.USER_ID_EXTRA_KEY";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_frame);

        if (getIntent() != null) {
            String userId = getIntent().getStringExtra(USER_ID_EXTRA_KEY);
            if (savedInstanceState == null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, FollowersFragment.newInstance(userId))
                        .commit();
            }
        }
    }

}
