package com.sumatodev.social_chat_sdk.main.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ali on 13/03/2018.
 */

public class ThreadListResult {

    boolean isMoreDataAvailable;
    List<ThreadsModel> threads = new ArrayList<>();
    long lastItemCreatedDate;

    public boolean isMoreDataAvailable() {
        return isMoreDataAvailable;
    }

    public void setMoreDataAvailable(boolean moreDataAvailable) {
        isMoreDataAvailable = moreDataAvailable;
    }

    public List<ThreadsModel> getThreads() {
        return threads;
    }

    public void setThreads(List<ThreadsModel> threads) {
        this.threads = threads;
    }

    public long getLastItemCreatedDate() {
        return lastItemCreatedDate;
    }

    public void setLastItemCreatedDate(long lastItemCreatedDate) {
        this.lastItemCreatedDate = lastItemCreatedDate;
    }
}
