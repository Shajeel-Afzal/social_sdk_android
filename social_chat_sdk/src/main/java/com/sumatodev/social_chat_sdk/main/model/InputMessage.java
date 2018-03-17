package com.sumatodev.social_chat_sdk.main.model;

/**
 * Created by Ali on 14/03/2018.
 */

public class InputMessage {

    private String text;
    private String uid;

    public InputMessage(String text, String uid) {
        this.text = text;
        this.uid = uid;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}
