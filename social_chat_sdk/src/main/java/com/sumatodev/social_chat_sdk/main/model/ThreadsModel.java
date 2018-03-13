package com.sumatodev.social_chat_sdk.main.model;

import com.sumatodev.social_chat_sdk.main.enums.ItemType;

import java.io.Serializable;

/**
 * Created by Ali on 13/03/2018.
 */

public class ThreadsModel implements Serializable, LazyLoading {

    private String threadKey;
    private ItemType itemType;

    public ThreadsModel() {
        itemType = ItemType.ITEM;
    }

    public ThreadsModel(ItemType itemType) {
        this.itemType = itemType;
        setThreadKey(itemType.toString());
    }

    public String getThreadKey() {
        return threadKey;
    }

    public void setThreadKey(String threadKey) {
        this.threadKey = threadKey;
    }


    @Override
    public ItemType getItemType() {
        return itemType;
    }

    @Override
    public void setItemType(ItemType itemType) {

    }
}
