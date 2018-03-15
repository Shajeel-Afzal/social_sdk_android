package com.sumatodev.social_chat_sdk.main.listeners;

import com.sumatodev.social_chat_sdk.main.data.model.MessageListResult;

/**
 * Created by Ali on 15/03/2018.
 */

public interface OnMessageListChangedListener<Message> {
    void onListChanged(MessageListResult result);

    void onCancel(String message);
}
