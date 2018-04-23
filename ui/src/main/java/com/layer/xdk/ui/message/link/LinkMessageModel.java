package com.layer.xdk.ui.message.link;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;
import com.layer.sdk.LayerClient;
import com.layer.sdk.messaging.Message;
import com.layer.sdk.messaging.MessagePart;
import com.layer.xdk.ui.R;
import com.layer.xdk.ui.message.image.cache.ImageRequestParameters;
import com.layer.xdk.ui.message.model.MessageModel;
import com.layer.xdk.ui.util.Log;

import java.io.IOException;
import java.io.InputStreamReader;

public class LinkMessageModel extends MessageModel {

    public static final String ROOT_MIME_TYPE = "application/vnd.layer.link+json";
    private static final String ACTION_OPEN_URL = "open-url";

    private LinkMessageMetadata mMetadata;

    public LinkMessageModel(@NonNull Context context, @NonNull LayerClient layerClient,
                            @NonNull Message message) {
        super(context, layerClient, message);
    }

    @Override
    public int getViewLayoutId() {
        return R.layout.xdk_ui_link_message_view;
    }

    @Override
    public int getContainerViewLayoutId() {
        return R.layout.xdk_ui_standard_message_container;
    }

    @Override
    protected void parse(@NonNull MessagePart messagePart) {
        InputStreamReader inputStreamReader = new InputStreamReader(messagePart.getDataStream());
        JsonReader reader = new JsonReader(inputStreamReader);
        mMetadata = getGson().fromJson(reader, LinkMessageMetadata.class);
        try {
            inputStreamReader.close();
        } catch (IOException e) {
            if (Log.isLoggable(Log.ERROR)) {
                Log.e("Failed to close input stream while parsing link message", e);
            }
        }
    }

    @Override
    protected boolean shouldDownloadContentIfNotReady(@NonNull MessagePart messagePart) {
        return true;
    }

    @Nullable
    @Override
    public String getTitle() {
        return mMetadata != null ? mMetadata.mTitle : null;
    }

    @Nullable
    @Override
    public String getDescription() {
        return mMetadata != null ? mMetadata.mDescription : null;
    }

    @Nullable
    @Override
    public String getFooter() {
        return mMetadata != null ? mMetadata.mAuthor : null;
    }

    @Override
    public String getActionEvent() {
        if (super.getActionEvent() != null) {
            return super.getActionEvent();
        }

        if (mMetadata != null && mMetadata.mAction != null) {
            return mMetadata.mAction.getEvent();
        }

        return ACTION_OPEN_URL;
    }

    @NonNull
    @Override
    public JsonObject getActionData() {
        if (super.getActionData().size() > 0) {
            return super.getActionData();
        }

        JsonObject actionData;
        if (mMetadata != null) {
            if (mMetadata.mAction != null) {
                actionData = mMetadata.mAction.getData();
            } else {
                actionData = new JsonObject();
                actionData.addProperty("url", mMetadata.mUrl);
            }
        } else {
            actionData = super.getActionData();
        }

        return actionData;
    }

    @Override
    public int getBackgroundColor() {
        return R.color.xdk_ui_link_message_view_background;
    }

    @Override
    public boolean getHasContent() {
        return mMetadata != null;
    }

    @Nullable
    @Override
    public String getPreviewText() {
        String title = getTitle();
        return title != null ? title : getAppContext().getString(R.string.xdk_ui_link_message_preview_text);
    }

    public String getImageUrl() {
        return mMetadata != null ? mMetadata.mImageUrl : null;
    }

    public String getUrl() {
        return mMetadata != null ? mMetadata.mUrl : null;
    }

    public LinkMessageMetadata getMetadata() {
        return mMetadata;
    }

    public ImageRequestParameters getImageRequestParameters() {
        if (getHasContent()) {
            ImageRequestParameters.Builder builder = new ImageRequestParameters.Builder();
            if (mMetadata.mImageUrl != null) {
                builder.url(mMetadata.mImageUrl);
            } else {
                return null;
            }

            builder.tag(getClass().getSimpleName());

            return builder.build();
        }

        return null;
    }
}
