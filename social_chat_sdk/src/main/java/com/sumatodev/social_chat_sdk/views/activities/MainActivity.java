package com.sumatodev.social_chat_sdk.views.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.sumatodev.social_chat_sdk.R;
import com.sumatodev.social_chat_sdk.views.fragments.ThreadsFragment;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, ThreadsFragment.newInstance())
                    .commit();
        }

    }
}
