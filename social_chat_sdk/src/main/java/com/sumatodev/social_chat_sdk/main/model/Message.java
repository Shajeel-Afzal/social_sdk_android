package com.sumatodev.social_chat_sdk.main.model;

import com.google.firebase.database.ServerValue;
import com.sumatodev.social_chat_sdk.main.enums.ItemType;
import com.sumatodev.social_chat_sdk.main.enums.MessageType;

import java.io.Serializable;

/*
 * Created by troy379 on 04.04.17.
 */
public class Message implements Serializable, LazyLoading {

    private String id;
    private String text;
    private Object createdAt;
    private String fromUserId;
    private ItemType itemType;
    private String imageUrl;
    private MessageType messageType;


    public Message() {
        this.createdAt = ServerValue.TIMESTAMP;
    }


    public void setText(String text) {
        this.text = text;
        this.itemType = ItemType.TEXT;
    }

    public void setCreatedAt(Object createdAt) {
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


    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
        this.itemType = ItemType.IMAGE;
    }


    @Override
    public ItemType getItemType() {
        return itemType;
    }

    @Override
    public MessageType getMessageType() {
        return messageType;
    }

    @Override
    public void setItemType(ItemType itemType) {
        this.itemType = itemType;
    }

    @Override
    public void setMessageType(MessageType messageType) {
        this.messageType = messageType;
    }
}
