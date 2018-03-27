package com.sumatodev.social_chat_sdk;

import com.sumatodev.social_chat_sdk.main.manager.DatabaseHelper;

/**
 * Created by Kristina on 10/28/16.
 */

public class ChatApplicationHelper {

    private static final String TAG = ChatApplicationHelper.class.getSimpleName();
    private static DatabaseHelper databaseHelper;

    public static DatabaseHelper getDatabaseHelper() {
        return databaseHelper;
    }

    public static void initDatabaseHelper(android.app.Application application) {
        databaseHelper = DatabaseHelper.getInstance(application);
        databaseHelper.init();
    }
}
