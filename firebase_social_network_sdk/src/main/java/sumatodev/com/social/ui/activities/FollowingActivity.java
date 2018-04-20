package sumatodev.com.social.ui.activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import sumatodev.com.social.R;
import sumatodev.com.social.ui.fragments.FollowFragment;

public class FollowingActivity extends AppCompatActivity {

    public static final String USER_ID_EXTRA_KEY = "FollowingActivity.USER_ID_EXTRA_KEY";
    public static final String REF_TYPE = "REF_TYPE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_frame);

        if (getIntent() != null) {
            String userId = getIntent().getStringExtra(USER_ID_EXTRA_KEY);
            String type = getIntent().getStringExtra(REF_TYPE);
            if (savedInstanceState == null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, FollowFragment.newInstance(userId, type))
                        .commit();
            }
        }
    }
}
