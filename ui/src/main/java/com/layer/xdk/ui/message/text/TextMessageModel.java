package com.layer.xdk.ui.message.text;

import android.content.Context;
import android.databinding.Bindable;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.util.Linkify;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.layer.sdk.LayerClient;
import com.layer.sdk.messaging.Message;
import com.layer.sdk.messaging.MessagePart;
import com.layer.xdk.ui.R;
import com.layer.xdk.ui.message.model.MessageModel;

public class TextMessageModel extends MessageModel {

    public final static String ROOT_MIME_TYPE = "application/vnd.layer.text+json";
    private final JsonParser mJsonParser;

    private CharSequence mText;
    private String mTitle;
    private String mSubtitle;
    private String mAuthor;
    private String mActionEvent;
    private JsonObject mCustomData;

    public TextMessageModel(@NonNull Context context, @NonNull LayerClient layerClient,
                            @NonNull Message message) {
        super(context, layerClient, message);
        mJsonParser = new JsonParser();
    }

    @Override
    public int getViewLayoutId() {
        return R.layout.xdk_ui_text_message_view;
    }

    @Override
    public int getContainerViewLayoutId() {
        return R.layout.xdk_ui_standard_message_container;
    }

    @Override
    protected boolean shouldDownloadContentIfNotReady(@NonNull MessagePart messagePart) {
        return true;
    }

    @Override
    protected void parse(@NonNull MessagePart messagePart) {
        String data = new String(messagePart.getData());
        JsonObject jsonObject = mJsonParser.parse(data).getAsJsonObject();
        mText = jsonObject.has("text") ? jsonObject.get("text").getAsString() : null;
        mSubtitle = jsonObject.has("subtitle") ? jsonObject.get("subtitle").getAsString().trim() : null;
        mTitle = jsonObject.has("title") ? jsonObject.get("title").getAsString().trim() : null;
        mAuthor = jsonObject.has("author") ? jsonObject.get("author").getAsString().trim() : null;
        if (jsonObject.has("action")) {
            JsonObject action = jsonObject.getAsJsonObject("action");
            mActionEvent = action.get("event").getAsString();
            mCustomData = action.get("data").getAsJsonObject();
        } else {
            mActionEvent = null;
            mCustomData = null;
        }

        linkifyText();
    }

    @Override
    protected void processLegacyParts() {
        MessagePart part = getMessage().getMessageParts().iterator().next();
        if (part.isContentReady()) {
            mText = new String(part.getData());
            linkifyText();
        } else if (shouldDownloadContentIfNotReady(part)) {
            part.download(null);
        }
    }

    private void linkifyText() {
        SpannableString spannableString = new SpannableString(mText);
        Linkify.addLinks(spannableString, Linkify.ALL);
        mText = spannableString;
    }

    @Bindable
    public CharSequence getText() {
        return mText;
    }

    @Override
    @Bindable
    public String getTitle() {
        return mTitle;
    }

    @Override
    @Bindable
    public String getDescription() {
        return mSubtitle;
    }

    @Override
    public String getActionEvent() {
        if (super.getActionEvent() != null) {
            return super.getActionEvent();
        }

        return mActionEvent;
    }

    @NonNull
    @Override
    public JsonObject getActionData() {
        if (super.getActionData().size() > 0) {
            return super.getActionData();
        }

        if (mCustomData != null) {
            return mCustomData;
        }

        return new JsonObject();
    }

    @Override
    public String getFooter() {
        return mAuthor;
    }

    @Override
    public boolean getHasContent() {
        return !TextUtils.isEmpty(mText);
    }

    @Nullable
    @Override
    public String getPreviewText() {
        if (getHasContent()) {
            return mTitle != null ? mTitle : String.valueOf(mText);
        } else {
            return getAppContext().getString(R.string.xdk_ui_text_message_preview_text);
        }
    }

    @Override
    public int getBackgroundColor() {
        return useSimpleColor() ? R.color.xdk_ui_text_message_view_background_simple : R.color.xdk_ui_text_message_view_background;
    }

    @Bindable
    @ColorInt
    public int getTextColor() {
        @ColorRes int colorRes = useSimpleColor() ? R.color.xdk_ui_text_message_view_body_simple : R.color.xdk_ui_text_message_view_body;
        return ContextCompat.getColor(getAppContext(), colorRes);
    }

    @Bindable
    @ColorInt
    public int getTextColorLink() {
        @ColorRes int colorRes = useSimpleColor() ? R.color.xdk_ui_text_message_view_body_simple_link : R.color.xdk_ui_text_message_view_body_link;
        return ContextCompat.getColor(getAppContext(), colorRes);
    }

    private boolean useSimpleColor() {
        return getParentMessageModel() == null && !getHasMetadata() && isMessageFromMe();
    }
}
