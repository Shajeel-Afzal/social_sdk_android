package com.layer.xdk.ui.message.action;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.gson.JsonObject;
import com.layer.sdk.LayerClient;

public abstract class ActionHandler {
    private String mEvent;
    private LayerClient mLayerClient;

    @SuppressWarnings("WeakerAccess")
    public ActionHandler(LayerClient layerClient, String event) {
        mLayerClient = layerClient;
        mEvent = event;
    }

    public String getEvent() {
        return mEvent;
    }

    public LayerClient getLayerClient() {
        return mLayerClient;
    }

    public abstract void performAction(@NonNull Context context, @Nullable JsonObject data);
}
