package com.sumatodev.social_chat_sdk.main.manager;

import android.content.Context;
import android.util.Log;

import com.sumatodev.social_chat_sdk.ApplicationHelper;
import com.sumatodev.social_chat_sdk.main.model.Message;


/**
 * Created by Ali on 09/03/2018.
 */

public class MessagesManager extends FirebaseListenersManager {

    private static final String TAG = MessagesManager.class.getSimpleName();
    private static MessagesManager instance;

    private Context context;

    public static MessagesManager getInstance(Context context) {
        if (instance == null) {
            instance = new MessagesManager(context);
        }
        return instance;
    }

    public MessagesManager(Context context) {
        this.context = context;
    }

    public void sendNewMessage(Message message) {
        try {
            ApplicationHelper.getDatabaseHelper().sendNewMessage(message);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

}
