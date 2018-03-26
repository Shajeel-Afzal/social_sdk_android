package com.sumatodev.social_chat_sdk.main.model;

import android.net.Uri;

/**
 * Created by Ali on 14/03/2018.
 */

public class InputMessage {

    private String text;
    private String uid;
    private Uri imageUrl;

    public InputMessage() {
    }

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

    public Uri getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(Uri imageUrl) {
        this.imageUrl = imageUrl;
    }
}
