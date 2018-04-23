package com.layer.xdk.ui.message.button;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;
import com.layer.sdk.LayerClient;
import com.layer.sdk.messaging.Message;
import com.layer.sdk.messaging.MessagePart;
import com.layer.xdk.ui.R;
import com.layer.xdk.ui.message.action.ActionHandlerRegistry;
import com.layer.xdk.ui.message.choice.ChoiceClickDelegate;
import com.layer.xdk.ui.message.choice.ChoiceConfigMetadata;
import com.layer.xdk.ui.message.choice.ChoiceMetadata;
import com.layer.xdk.ui.message.choice.ChoiceOrSetHelper;
import com.layer.xdk.ui.message.model.MessageModel;
import com.layer.xdk.ui.message.response.ResponseSummaryMetadataV2;
import com.layer.xdk.ui.message.response.crdt.OrOperationResult;
import com.layer.xdk.ui.util.Log;

import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ButtonMessageModel extends MessageModel {
    public static final String ROOT_MIME_TYPE = "application/vnd.layer.buttons+json";

    private ButtonMessageMetadata mMetadata;
    private ChoiceOrSetHelper mChoiceOrSetHelper;
    private ChoiceClickDelegate mChoiceClickDelegate;

    public ButtonMessageModel(@NonNull Context context, @NonNull LayerClient layerClient,
                              @NonNull Message message) {
        super(context, layerClient, message);
    }

    @Override
    public int getViewLayoutId() {
        return R.layout.xdk_ui_button_message_view;
    }

    @Override
    public int getContainerViewLayoutId() {
        return R.layout.xdk_ui_standard_message_container;
    }

    @Override
    protected void parse(@NonNull MessagePart messagePart) {
        JsonReader reader = new JsonReader(new InputStreamReader(messagePart.getDataStream()));
        mMetadata = getGson().fromJson(reader, ButtonMessageMetadata.class);

        // Populate choice data objects
        Set<ChoiceConfigMetadata> choiceConfigs = new HashSet<>();
        for (ButtonMetadata metadata : mMetadata.mButtonMetadata) {
            if (ButtonMetadata.TYPE_CHOICE.equals(metadata.mType)) {
                JsonObject data = metadata.mData;
                if (data != null) {
                    ChoiceConfigMetadata choiceConfig = getGson().fromJson(data,
                            ChoiceConfigMetadata.class);
                    metadata.mChoiceConfigMetadata = choiceConfig;
                    choiceConfig.setEnabledForMe(getAuthenticatedUserId());
                    choiceConfigs.add(choiceConfig);
                }
            }

        }
        mChoiceOrSetHelper = new ChoiceOrSetHelper(getGson(), getRootMessagePart(), choiceConfigs);
        notifyChange();
    }

    @Override
    protected void processResponseSummaryMetadata(@NonNull ResponseSummaryMetadataV2 metadata) {
        mChoiceOrSetHelper.processRemoteResponseSummary(metadata);
        notifyChange();
    }

    @NonNull
    Set<String> getSelectedChoices(@NonNull String responseName) {
        return mChoiceOrSetHelper.getSelections(responseName);
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

    @Nullable
    @Override
    public String getPreviewText() {
        if (getHasContent()) {
            String title = null;
            List<MessageModel> childMessageModels = getChildMessageModelsWithRole("content");
            if (childMessageModels.size() > 0) {
                title = childMessageModels.get(0).getPreviewText();
            }

            if (title != null) {
                return title;
            } else {
                return getAppContext().getResources().getQuantityString(R.plurals.xdk_ui_button_message_preview_text, 0, mMetadata.mButtonMetadata.size());
            }
        }

        return "";
    }

    @Override
    public String getActionEvent() {
        if (super.getActionEvent() != null) {
            return super.getActionEvent();
        }

        MessageModel contentModel = getContentModel();
        if (contentModel != null) {
            return contentModel.getActionEvent();
        }

        return null;
    }

    @NonNull
    @Override
    public JsonObject getActionData() {
        if (super.getActionData().size() > 0) {
            return super.getActionData();
        }

        MessageModel contentModel = getContentModel();
        if (contentModel != null) {
            return contentModel.getActionData();
        }

        return new JsonObject();
    }

    @Override
    public boolean getHasContent() {
        return getRootMessagePart().isContentReady();
    }

    @Nullable
    @SuppressWarnings("WeakerAccess")
    public MessageModel getContentModel() {
        if (getChildMessageModels() != null && getChildMessageModels().size() > 0) {
            return getChildMessageModels().get(0);
        }

        return null;
    }

    @Nullable
    @SuppressWarnings("WeakerAccess")
    public List<ButtonMetadata> getButtonMetadata() {
        return mMetadata != null ? mMetadata.mButtonMetadata : null;
    }

    @SuppressWarnings("WeakerAccess")
    protected void onChoiceClicked(ChoiceConfigMetadata choiceConfig, ChoiceMetadata choice,
            boolean selected) {
        Uri identityId = getAuthenticatedUserId();
        if (identityId == null) {
            if (Log.isLoggable(Log.WARN)) {
                Log.w("Unable to process a choice with no authenticated user");
            }
            return;
        }

        List<OrOperationResult> orOperationResults = mChoiceOrSetHelper.processLocalSelection(
                identityId, choiceConfig.getResponseName(), selected, choice.mId);
        getChoiceClickDelegate().sendResponse(choiceConfig, choice, selected, orOperationResults);

        ActionHandlerRegistry.dispatchChoiceSelection(getAppContext(), choice, this, getRootModelForTree());
    }

    private ChoiceClickDelegate getChoiceClickDelegate() {
        if (mChoiceClickDelegate == null) {
            String userName = getIdentityFormatter().getDisplayName(
                    getLayerClient().getAuthenticatedUser());
            mChoiceClickDelegate = new ChoiceClickDelegate(userName, getAppContext(),
                    getRootMessagePart().getId(), getMessageSenderRepository(), getMessage());
        }
        return mChoiceClickDelegate;
    }
}
