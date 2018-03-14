package com.sumatodev.social_chat_sdk.main.listeners;

/**
 * Created by Ali on 14/03/2018.
 */

public interface OnMessageChangedListener<Message> {
    void OnListChanged(Message message);
    void onCancel(String message);
}
