package com.layer.xdk.ui.message.generic;


import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.layer.sdk.LayerClient;
import com.layer.sdk.messaging.Message;
import com.layer.sdk.messaging.MessagePart;
import com.layer.xdk.ui.R;
import com.layer.xdk.ui.message.MessagePartUtils;
import com.layer.xdk.ui.message.text.TextMessageModel;

import java.util.HashSet;
import java.util.Set;

public class UnhandledMessageModel extends TextMessageModel {

    private String mText;

    public UnhandledMessageModel(@NonNull Context context, @NonNull LayerClient layerClient,
            @NonNull Message message) {
        super(context, layerClient, message);
    }

    @Override
    protected void processLegacyParts() {
        mText = generateLegacyMimeTypeText();
    }

    @Override
    protected void processParts(@NonNull MessagePart rootMessagePart) {
        super.processParts(rootMessagePart);
        mText = generateNormalMimeTypeText(rootMessagePart);
    }

    @Override
    protected void processChildParts() {
        // No-op as if we don't know how to handle this we shouldn't bother with its children
    }

    private String generateLegacyMimeTypeText() {
        Set<MessagePart> parts = getMessage().getMessageParts();
        Set<String> mimeTypes = new HashSet<>(parts.size());
        for (MessagePart part : parts) {
            mimeTypes.add(part.getMimeType());
        }
        String joinedMimeTypes = TextUtils.join(", ", mimeTypes);
        return getAppContext().getString(R.string.xdk_ui_unhandled_message_text_legacy, joinedMimeTypes);
    }

    private String generateNormalMimeTypeText(@NonNull MessagePart rootMessagePart) {
        String mimeType = MessagePartUtils.getMimeType(rootMessagePart);
        return getAppContext().getString(R.string.xdk_ui_unhandled_message_text, mimeType);
    }

    @Override
    protected void parse(@NonNull MessagePart messagePart) {
        // Never called since we just need the mime type
    }

    @Override
    public int getContainerViewLayoutId() {
        return R.layout.xdk_ui_empty_message_container;
    }

    @Override
    protected boolean shouldDownloadContentIfNotReady(@NonNull MessagePart messagePart) {
        return false;
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
        return true;
    }

    @Nullable
    @Override
    public String getPreviewText() {
        return getText();
    }

    public String getText() {
        return mText;
    }
}
