package com.layer.xdk.ui.message.action;


import android.content.Context;
import android.support.annotation.NonNull;

import com.layer.sdk.LayerClient;
import com.layer.xdk.ui.message.choice.ChoiceMetadata;
import com.layer.xdk.ui.message.model.MessageModel;

public abstract class ChoiceHandler {
    private String mChoiceId;
    private LayerClient mLayerClient;

    public ChoiceHandler(LayerClient layerClient, String choiceId) {
        mLayerClient = layerClient;
        mChoiceId = choiceId;
    }

    @SuppressWarnings("WeakerAccess")
    public String getChoiceId() {
        return mChoiceId;
    }

    public LayerClient getLayerClient() {
        return mLayerClient;
    }

    @SuppressWarnings("WeakerAccess")
    public abstract void onChoiceSelect(@NonNull Context context, ChoiceMetadata choice,
            MessageModel model, MessageModel rootModel);
}
