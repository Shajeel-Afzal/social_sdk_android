package com.sumatodev.social_chat_sdk.main.model;

import com.sumatodev.social_chat_sdk.main.enums.ItemType;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/*
 * Created by troy379 on 04.04.17.
 */
public class Message implements Serializable, LazyLoading {

    private String id;
    private String text;
    private long createdAt;
    private String fromUserId;
    private ItemType itemType;


    public Message() {
        this.createdAt = new Date().getTime();
        itemType = ItemType.ITEM;
    }


    public Message(ItemType itemType) {
        this.itemType = itemType;
        setId(itemType.toString());

    }

    public void setText(String text) {
        this.text = text;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFromUserId() {
        return fromUserId;
    }

    public void setFromUserId(String fromUserId) {
        this.fromUserId = fromUserId;
    }


    public String getId() {
        return id;
    }

    public String getText() {
        return text;
    }

    public Object getCreatedAt() {
        return createdAt;
    }

    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();

        result.put("id", id);
        result.put("text", text);
        result.put("createdAt", createdAt);
        result.put("fromUserId", fromUserId);

        return result;
    }

    @Override
    public ItemType getItemType() {
        return itemType;
    }

    @Override
    public void setItemType(ItemType itemType) {

    }
}
