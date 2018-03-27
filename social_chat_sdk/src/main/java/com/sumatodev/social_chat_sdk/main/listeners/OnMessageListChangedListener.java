package com.sumatodev.social_chat_sdk.main.listeners;

import com.sumatodev.social_chat_sdk.main.model.MessageListResult;

/**
 * Created by Ali on 16/03/2018.
 */

public interface OnMessageListChangedListener<Message> {
    void onListChanged(MessageListResult result);

    void onCanceled(String message);
}
