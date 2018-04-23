package com.layer.xdk.ui.message.model;

import android.support.annotation.NonNull;

import com.google.gson.JsonObject;

/**
 * Represents an action that the user may perform on a {@link MessageModel}'s view
 */
public class Action {
    private String mEvent;
    private JsonObject mData;

    public Action(@NonNull String event) {
        mEvent = event;
        mData = new JsonObject();
    }

    /**
     * Provides the name for the action that is broadcast to any
     * {@link com.layer.xdk.ui.message.action.ActionHandler} that may be registered against the name
     * name
     *
     * @return a {@link String} containing the action name
     */
    @NonNull
    public String getEvent() {
        return mEvent;
    }

    /**
     * Set the name for the action that is broadcast to any
     * {@link com.layer.xdk.ui.message.action.ActionHandler} that may be registered against the name
     * name
     *
     * @param event a {@link String} containing the action name
     */
    public void setEvent(@NonNull String event) {
        mEvent = event;
    }

    /**
     * Provides data relevant to the action's execution
     *
     * @return a {@link JsonObject} containing relevant data, or an empty {@link JsonObject} if
     * no data is to be sent
     */
    @NonNull
    public JsonObject getData() {
        return mData;
    }

    /**
     * Set data relevant to the action's execution
     *
     * @param data a {@link JsonObject} containing relevant data, or an empty {@link JsonObject} if
     *             no data is to be sent
     */
    public void setData(@NonNull JsonObject data) {
        mData = data;
    }
}
