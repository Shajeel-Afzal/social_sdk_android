package com.sumatodev.social_chat_sdk.main.manager;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ValueEventListener;
import com.sumatodev.social_chat_sdk.ApplicationHelper;
import com.sumatodev.social_chat_sdk.main.listeners.OnDataChangedListener;
import com.sumatodev.social_chat_sdk.main.listeners.OnMessageListChangedListener;
import com.sumatodev.social_chat_sdk.main.listeners.OnMessageSentListener;
import com.sumatodev.social_chat_sdk.main.listeners.OnObjectChangedListener;
import com.sumatodev.social_chat_sdk.main.listeners.OnTaskCompleteListener;
import com.sumatodev.social_chat_sdk.main.model.InputMessage;
import com.sumatodev.social_chat_sdk.main.model.Message;
import com.sumatodev.social_chat_sdk.main.model.Profile;
import com.sumatodev.social_chat_sdk.main.model.UsersPublic;
import com.sumatodev.social_chat_sdk.main.utils.LogUtil;


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


    public void sendNewMessage(InputMessage message, OnMessageSentListener onMessageSentListener) {
        try {
            ApplicationHelper.getDatabaseHelper().sendNewMessage(message, onMessageSentListener);
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

    public void getChatList(Context context, String userKey, OnDataChangedListener<Message> onDataChangedListener) {
        ValueEventListener valueEventListener = ApplicationHelper.getDatabaseHelper().getChatList(userKey, onDataChangedListener);
        addListenerToMap(context, valueEventListener);
    }

    public void getMessageList(Context context, String userKey, OnMessageListChangedListener<Message> listener, long date) {
        ValueEventListener valueEventListener = ApplicationHelper.getDatabaseHelper().getMessageList(userKey,
                listener, date);
        addListenerToMap(context, valueEventListener);
    }

    public void removeMessage(String messageId, String userKey, final OnTaskCompleteListener onTaskCompleteListener) {

        DatabaseHelper helper = ApplicationHelper.getDatabaseHelper();
        helper.removeMessage(messageId, userKey).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                onTaskCompleteListener.onTaskComplete(true);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                onTaskCompleteListener.onTaskComplete(false);
                LogUtil.logError(TAG, "removeMessage()", e);
            }
        });
    }
}
