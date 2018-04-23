package com.layer.xdk.ui.message.choice;

import android.content.Context;
import android.databinding.Bindable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.gson.stream.JsonReader;
import com.layer.sdk.LayerClient;
import com.layer.sdk.messaging.Message;
import com.layer.sdk.messaging.MessagePart;
import com.layer.xdk.ui.BR;
import com.layer.xdk.ui.R;
import com.layer.xdk.ui.message.model.MessageModel;
import com.layer.xdk.ui.message.response.ResponseSummaryMetadataV2;
import com.layer.xdk.ui.message.response.crdt.OrOperationResult;
import com.layer.xdk.ui.util.Log;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class ChoiceMessageModel extends MessageModel {
    public static final String MIME_TYPE = "application/vnd.layer.choice+json";

    private ChoiceMessageMetadata mMetadata;
    private ChoiceClickDelegate mChoiceClickDelegate;

    private ChoiceOrSetHelper mChoiceOrSetHelper;

    public ChoiceMessageModel(@NonNull Context context, @NonNull LayerClient layerClient,
                              @NonNull Message message) {
        super(context, layerClient, message);
    }

    @Override
    public int getViewLayoutId() {
        return R.layout.xdk_ui_choice_message_view;
    }

    @Override
    public int getContainerViewLayoutId() {
        return R.layout.xdk_ui_titled_message_container;
    }

    @Override
    protected void parse(@NonNull MessagePart messagePart) {
        InputStreamReader inputStreamReader = new InputStreamReader(messagePart.getDataStream());
        JsonReader reader = new JsonReader(inputStreamReader);
        mMetadata = getGson().fromJson(reader, ChoiceMessageMetadata.class);
        try {
            inputStreamReader.close();
        } catch (IOException e) {
            if (Log.isLoggable(Log.ERROR)) {
                Log.e("Failed to close input stream while parsing choice message", e);
            }
        }

        mMetadata.setEnabledForMe(getAuthenticatedUserId());

        mChoiceOrSetHelper = new ChoiceOrSetHelper(getGson(), getRootMessagePart(),
                Collections.<ChoiceConfigMetadata>singleton(mMetadata));

        notifyPropertyChanged(BR.choiceMessageMetadata);
        notifyPropertyChanged(BR.selectedChoices);
    }

    @Override
    protected void processResponseSummaryMetadata(@NonNull ResponseSummaryMetadataV2 metadata) {
        mChoiceOrSetHelper.processRemoteResponseSummary(metadata);
        notifyPropertyChanged(BR.selectedChoices);
    }

    @Override
    protected boolean shouldDownloadContentIfNotReady(@NonNull MessagePart messagePart) {
        return true;
    }

    @Nullable
    @Override
    public String getTitle() {
        String title = getAppContext().getString(R.string.xdk_ui_choice_message_model_default_title);
        if (mMetadata != null && mMetadata.mTitle != null) {
            title = mMetadata.mTitle;
        }

        return title;
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

        return null;
    }

    @Override
    public boolean getHasContent() {
        return mMetadata != null;
    }

    @Nullable
    @Override
    public String getPreviewText() {
        if (getHasContent() && mMetadata.mChoices.size() > 0) {
            return getTitle();
        }
        return null;
    }

    @Nullable
    @Bindable
    public ChoiceMessageMetadata getChoiceMessageMetadata() {
        return mMetadata;
    }

    @Bindable
    public String getLabel() {
        return mMetadata != null ? mMetadata.mLabel : null;
    }

    @Bindable
    public Set<String> getSelectedChoices() {
        return mChoiceOrSetHelper.getSelections(mMetadata.getResponseName());
    }

    @Bindable
    @SuppressWarnings("WeakerAccess")
    public boolean getIsEnabledForMe() {
        return mMetadata.mEnabledForMe;
    }

    void onChoiceClicked(@NonNull ChoiceMetadata choice, boolean selected) {
        Uri identityId = getAuthenticatedUserId();
        if (identityId == null) {
            if (Log.isLoggable(Log.WARN)) {
                Log.w("Unable to process a choice with no authenticated user");
            }
            return;
        }

        List<OrOperationResult> orOperationResults = mChoiceOrSetHelper.processLocalSelection(
                identityId, mMetadata.getResponseName(), selected, choice.mId);
        getChoiceClickDelegate().sendResponse(mMetadata, choice, selected, orOperationResults);
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
