package com.sumatodev.firebasesocial;

import android.app.Application;

import com.sumatodev.social_chat_sdk.ChatApplicationHelper;

import sumatodev.com.social.ApplicationHelper;
import sumatodev.com.social.listeners.LoginRequiredListener;
import sumatodev.com.social.managers.DatabaseHelper;

/**
 * Created by shajeelafzal on 27/03/2018.
 */

public class GlobalApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        ApplicationHelper.initDatabaseHelper(this, "gs://androidfirebasesocialnetwork.appspot.com",
                new LoginRequiredListener() {
                    @Override
                    public void loginRequired() {
                        SignInActivity.start(getApplicationContext());
                    }
                });
        DatabaseHelper.getInstance(this).subscribeToNewPosts();
        ChatApplicationHelper.initDatabaseHelper(this);
    }
}
