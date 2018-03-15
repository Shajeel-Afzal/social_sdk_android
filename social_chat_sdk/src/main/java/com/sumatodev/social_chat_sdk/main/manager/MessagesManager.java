package com.sumatodev.social_chat_sdk.main.manager;

import android.content.Context;
import android.util.Log;

import com.google.firebase.database.ValueEventListener;
import com.sumatodev.social_chat_sdk.ApplicationHelper;
import com.sumatodev.social_chat_sdk.main.data.model.InputMessage;
import com.sumatodev.social_chat_sdk.main.data.model.Message;
import com.sumatodev.social_chat_sdk.main.listeners.OnMessageChangedListener;
import com.sumatodev.social_chat_sdk.main.listeners.OnMessageListChangedListener;
import com.sumatodev.social_chat_sdk.main.listeners.OnMessageSentListener;
import com.sumatodev.social_chat_sdk.main.listeners.OnObjectChangedListener;
import com.sumatodev.social_chat_sdk.main.model.Profile;
import com.sumatodev.social_chat_sdk.main.model.UsersPublic;


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

    /*
    public void sendNewMessage(Message message, OnMessageSentListener onMessageSentListener) {
        try {
            ApplicationHelper.getDatabaseHelper().sendNewMessage(message, onMessageSentListener);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }
    */


    public void sendNewMessage(InputMessage message, OnMessageSentListener onMessageSentListener) {
        try {
            ApplicationHelper.getDatabaseHelper().sendMessage(message, onMessageSentListener);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }


    public void getChatList(String key, OnMessageChangedListener<Message> listener) {
        try {
            ApplicationHelper.getDatabaseHelper().getChatList(key, listener);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    public void getProfileValue(Context activityContext, String id, final OnObjectChangedListener<Profile> listener) {
        ValueEventListener valueEventListener = databaseHelper.getProfile(id, listener);
        addListenerToMap(activityContext, valueEventListener);
    }

    public void getProfileSingleValue(String id, final OnObjectChangedListener<Profile> listener) {
        databaseHelper.getProfileSingleValue(id, listener);
    }

    public void getUsersPublicProfile(String userKey, OnObjectChangedListener<UsersPublic> listener) {
        databaseHelper.getUsersPublicProfile(userKey, listener);
    }

    public void getMessagesList(String userKey, OnMessageListChangedListener<Message> onMessageListChangedListener, long lastLoadedDate) {
        ApplicationHelper.getDatabaseHelper().getMessagesList(userKey,onMessageListChangedListener,lastLoadedDate);
    }

    public void getMessages(String userKey,OnMessageListChangedListener<Message> listChangedListener,long date) {
       ApplicationHelper.getDatabaseHelper().getMessages(userKey,listChangedListener,date);
    }
}
