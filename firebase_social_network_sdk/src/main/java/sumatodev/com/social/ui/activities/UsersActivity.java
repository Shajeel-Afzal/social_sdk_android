package sumatodev.com.social.ui.activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import sumatodev.com.social.R;
import sumatodev.com.social.ui.fragments.RequestListFragment;
import sumatodev.com.social.ui.fragments.UsersFragment;

public class UsersActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_frame);


        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, UsersFragment.newInstance())
                    .commit();
        }
    }
}
