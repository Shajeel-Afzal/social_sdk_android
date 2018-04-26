package com.sumatodev.social_chat_sdk.main.model;

import com.google.firebase.database.ServerValue;

/**
 * Created by Ali on 18/03/2018.
 */

public class Status {
    public boolean isOnline;
    public Object lastSeen;

    public Status() {
    }

    public Status(boolean isOnline) {
        this.isOnline = isOnline;
        this.lastSeen = ServerValue.TIMESTAMP;
    }

}
