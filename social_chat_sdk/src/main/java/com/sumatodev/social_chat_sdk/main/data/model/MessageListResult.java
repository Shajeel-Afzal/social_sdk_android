package com.sumatodev.social_chat_sdk.main.data.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ali on 14/03/2018.
 */

public class MessageListResult {
    List<Message> messages = new ArrayList<>();
    long lastItemCreatedDate;

    public List<Message> getMessages() {
        return messages;
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }

    public long getLastItemCreatedDate() {
        return lastItemCreatedDate;
    }

    public void setLastItemCreatedDate(long lastItemCreatedDate) {
        this.lastItemCreatedDate = lastItemCreatedDate;
    }
}
