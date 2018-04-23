package com.layer.xdk.ui.message.carousel;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;
import com.layer.sdk.LayerClient;
import com.layer.sdk.messaging.Message;
import com.layer.sdk.messaging.MessagePart;
import com.layer.xdk.ui.R;
import com.layer.xdk.ui.message.model.MessageModel;

import java.io.InputStreamReader;
import java.util.List;

public class CarouselMessageModel extends MessageModel {
    public static final String MIME_TYPE = "application/vnd.layer.carousel+json";
    private static final String ROLE_CAROUSEL_ITEM = "carousel-item";

    private CarouselMessageMetadata mMetadata;

    public CarouselMessageModel(@NonNull Context context, @NonNull LayerClient layerClient,
                                @NonNull Message message) {
        super(context, layerClient, message);
    }

    @Override
    public int getViewLayoutId() {
        return R.layout.xdk_ui_carousel_message_view;
    }

    @Override
    public int getContainerViewLayoutId() {
        return R.layout.xdk_ui_empty_message_container;
    }

    @Override
    protected void processChildParts() {
        super.processChildParts();
        if (mMetadata != null && mMetadata.mAction != null) {
            List<MessageModel> childModels = getChildMessageModels();
            if (childModels != null) {
                for (MessageModel model : childModels) {
                    model.setAction(mMetadata.mAction);
                }
            }
        }
    }

    @Override
    protected void parse(@NonNull MessagePart messagePart) {
        JsonReader reader = new JsonReader(new InputStreamReader(messagePart.getDataStream()));
        mMetadata = getGson().fromJson(reader, CarouselMessageMetadata.class);
    }

    @Override
    protected boolean shouldDownloadContentIfNotReady(@NonNull MessagePart messagePart) {
        return true;
    }

    @SuppressWarnings("WeakerAccess")
    public List<MessageModel> getCarouselItemModels() {
        return getChildMessageModels();
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
    public String getActionEvent() {
        if (super.getActionEvent() != null) {
            return super.getActionEvent();
        }

        if (mMetadata.mAction != null) {
            return mMetadata.mAction.getEvent();
        }
        return null;
    }

    @NonNull
    @Override
    public JsonObject getActionData() {
        if (super.getActionData().size() > 0) {
            return super.getActionData();
        }

        if (mMetadata.mAction != null) {
            return mMetadata.mAction.getData();
        }
        return new JsonObject();
    }

    @Nullable
    @Override
    public String getPreviewText() {
        List<MessageModel> childMessageModels = getChildMessageModelsWithRole(ROLE_CAROUSEL_ITEM);
        return getAppContext().getResources().getQuantityString(R.plurals.xdk_ui_carousel_message_preview_text, 0, childMessageModels.size());
    }

    @Override
    public boolean getHasContent() {
        return getChildMessageModels() != null && !getChildMessageModels().isEmpty();
    }
}
