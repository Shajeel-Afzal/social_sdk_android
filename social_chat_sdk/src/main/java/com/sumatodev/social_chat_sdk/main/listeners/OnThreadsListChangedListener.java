package com.sumatodev.social_chat_sdk.main.listeners;

import com.sumatodev.social_chat_sdk.main.model.ThreadListResult;

/**
 * Created by Ali on 13/03/2018.
 */

public interface OnThreadsListChangedListener<ThreadsModel> {

    void onListChanged(ThreadListResult result);

    void onCanceled(String message);
}
