package com.layer.xdk.ui.message.response;


import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.layer.sdk.LayerClient;
import com.layer.sdk.messaging.Message;
import com.layer.sdk.messaging.MessagePart;
import com.layer.xdk.ui.message.model.MessageModel;
import com.layer.xdk.ui.message.status.StatusMessageModel;

public class ResponseMessageModel extends MessageModel {

    public static final String MIME_TYPE = "application/vnd.layer.response+json";
    public static final String MIME_TYPE_V2 = "application/vnd.layer.response-v2+json";

    public ResponseMessageModel(@NonNull Context context, @NonNull LayerClient layerClient,
                                @NonNull Message message) {
        super(context, layerClient, message);
    }

    @Override
    public int getViewLayoutId() {
        // No view layout since this is rendered inside a MessageItemStatusViewModel
        return 0;
    }

    @Override
    public int getContainerViewLayoutId() {
        // No container layout since this is rendered inside a MessageItemStatusViewModel
        return 0;
    }

    @Override
    protected void parse(@NonNull MessagePart messagePart) {
        // Nothing to do
    }

    @Override
    protected boolean shouldDownloadContentIfNotReady(@NonNull MessagePart messagePart) {
        return true;
    }

    @Nullable
    @Override
    public String getTitle() {
        return null;
    }

    @Nullable
    @Override
    public String getDescription() {
        return null;
    }

    @Nullable
    @Override
    public String getFooter() {
        return null;
    }

    @Override
    public boolean getHasContent() {
        if (getChildMessageModels() != null && !getChildMessageModels().isEmpty()) {
            return getChildMessageModels().get(0).getHasContent();
        }
        return false;
    }

    @Nullable
    @Override
    public String getPreviewText() {
        if (getChildMessageModels() != null && !getChildMessageModels().isEmpty()) {
            return getChildMessageModels().get(0).getPreviewText();
        }
        return null;
    }

    @Nullable
    public CharSequence getText() {
        if (getChildMessageModels() != null && !getChildMessageModels().isEmpty()) {
            return ((StatusMessageModel) getChildMessageModels().get(0)).getText();
        }
        return null;
    }
}
