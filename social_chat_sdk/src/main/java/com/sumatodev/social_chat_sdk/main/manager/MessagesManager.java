package com.sumatodev.social_chat_sdk.main.manager;

import android.content.Context;
import android.util.Log;

import com.google.firebase.database.ValueEventListener;
import com.sumatodev.social_chat_sdk.ApplicationHelper;
import com.sumatodev.social_chat_sdk.main.listeners.OnMessageSentListener;
import com.sumatodev.social_chat_sdk.main.listeners.OnObjectChangedListener;
import com.sumatodev.social_chat_sdk.main.model.Message;
import com.sumatodev.social_chat_sdk.main.model.Profile;


/**
 * Created by Ali on 09/03/2018.
 */

public class MessagesManager extends FirebaseListenersManager {

    private static final String TAG = MessagesManager.class.getSimpleName();
    private static MessagesManager instance;
    private DatabaseHelper databaseHelper;
    private Context context;

    public static MessagesManager getInstance(Context context) {
        if (instance == null) {
            instance = new MessagesManager(context);
        }
        return instance;
    }

    public MessagesManager(Context context) {
        this.context = context;
        databaseHelper = ApplicationHelper.getDatabaseHelper();
    }

    public void sendNewMessage(Message message, OnMessageSentListener onMessageSentListener) {
        try {
            ApplicationHelper.getDatabaseHelper().sendNewMessage(message, onMessageSentListener);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    public void getMessageList(String userKey, OnObjectChangedListener<Message> listener) {
        ApplicationHelper.getDatabaseHelper().getMessages(userKey, listener);
    }


    public void getProfileValue(Context activityContext, String id, final OnObjectChangedListener<Profile> listener) {
        ValueEventListener valueEventListener = databaseHelper.getProfile(id, listener);
        addListenerToMap(activityContext, valueEventListener);
    }

    public void getProfileSingleValue(String id, final OnObjectChangedListener<Profile> listener) {
        databaseHelper.getProfileSingleValue(id, listener);
    }

}
