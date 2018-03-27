package com.sumatodev.social_chat_sdk.main.model;

import java.util.Date;

/**
 * Created by Ali on 18/03/2018.
 */

public class Status {
    public boolean isOnline;
    public long lastSeen;

    public Status() {
    }

    public Status(boolean isOnline) {
        this.isOnline = isOnline;
        this.lastSeen = new Date().getTime();
    }

}
