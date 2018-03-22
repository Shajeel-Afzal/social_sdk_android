package com.sumatodev.social_chat_sdk.main.model;

/**
 * Created by Ali on 13/03/2018.
 */

public class ThreadsModel {

    private String threadKey;

    public ThreadsModel() {
    }

    public ThreadsModel(String threadKey) {
        this.threadKey = threadKey;
    }

    public String getThreadKey() {
        return threadKey;
    }

    public void setThreadKey(String threadKey) {
        this.threadKey = threadKey;
    }
}
