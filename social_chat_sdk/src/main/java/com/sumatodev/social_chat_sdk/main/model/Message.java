package com.sumatodev.social_chat_sdk.main.model;

import com.google.firebase.database.Exclude;
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
    private String toUserId;
    private ItemType itemType;


    public Message() {
        this.createdAt = new Date().getTime();
        itemType = ItemType.ITEM;
    }

    public Message(String fromUserId, String toUserId, String text) {
        this(fromUserId, toUserId, text, new Date().getTime());
    }

    public Message(ItemType itemType) {
        this.itemType = itemType;
        setId(itemType.toString());

    }

    public Message(String fromUserId, String toUserId, String text, long createdAt) {
        this.text = text;
        this.fromUserId = fromUserId;
        this.toUserId = toUserId;
        this.createdAt = createdAt;
    }

    public String getStatus() {
        return "Sent";
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

    public String getToUserId() {
        return toUserId;
    }

    public void setToUserId(String toUserId) {
        this.toUserId = toUserId;
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

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();

        result.put("id", id);
        result.put("text", text);
        result.put("createdAt", createdAt);
        result.put("fromUserId", fromUserId);
        result.put("toUserId", toUserId);

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
