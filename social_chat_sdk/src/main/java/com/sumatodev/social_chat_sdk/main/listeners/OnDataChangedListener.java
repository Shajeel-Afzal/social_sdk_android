package com.sumatodev.social_chat_sdk.main.listeners;

import java.util.List;

/**
 * Created by Ali on 16/03/2018.
 */

public interface OnDataChangedListener<T> {
    void onListChanged(List<T> list);
}
