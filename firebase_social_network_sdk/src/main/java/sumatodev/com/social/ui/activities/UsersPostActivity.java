package sumatodev.com.social.ui.activities;

import android.os.Bundle;

import sumatodev.com.social.R;
import sumatodev.com.social.ui.fragments.UsersPostFragment;

public class UsersPostActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users_post);
        actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, UsersPostFragment.newInstance())
                    .commit();
        }

    }
}
