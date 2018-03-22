package com.sumatodev.social_chat_sdk.main.model;

/**
 * Created by Ali on 15/03/2018.
 */

public class UsersPublic {

    private String id;
    private String username;
    private String photoUrl;
    private Status status;

    public UsersPublic() {
    }

    public UsersPublic(String id, String username, String photoUrl, Status status) {
        this.id = id;
        this.username = username;
        this.photoUrl = photoUrl;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
}
