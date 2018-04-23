package com.layer.xdk.ui.message.image;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;
import com.layer.sdk.LayerClient;
import com.layer.sdk.messaging.Message;
import com.layer.sdk.messaging.MessagePart;
import com.layer.xdk.ui.R;
import com.layer.xdk.ui.message.LegacyMimeTypes;
import com.layer.xdk.ui.message.MessagePartUtils;
import com.layer.xdk.ui.message.image.cache.ImageRequestParameters;
import com.layer.xdk.ui.message.model.Action;
import com.layer.xdk.ui.message.model.MessageModel;
import com.layer.xdk.ui.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStreamReader;
import java.util.Set;

public class ImageMessageModel extends MessageModel {
    private static final String ROLE_SOURCE = "source";
    private static final String ROLE_PREVIEW = "preview";
    private static final int PLACEHOLDER = R.drawable.xdk_ui_image_message_model_placeholder;

    @SuppressWarnings("WeakerAccess")
    public static final String ACTION_EVENT_OPEN_URL = "open-url";

    public static final String ROOT_MIME_TYPE = "application/vnd.layer.image+json";

    private ImageMessageMetadata mMetadata;

    private ImageRequestParameters mPreviewRequestParameters;
    private ImageRequestParameters mSourceRequestParameters;

    public ImageMessageModel(@NonNull Context context, @NonNull LayerClient layerClient,
                             @NonNull Message message) {
        super(context, layerClient, message);
    }

    @Override
    public int getViewLayoutId() {
        return R.layout.xdk_ui_image_message_view;
    }

    @Override
    public int getContainerViewLayoutId() {
        return R.layout.xdk_ui_standard_message_container;
    }

    @Override
    protected void processLegacyParts() {
        LegacyImageMessageParts parts = new LegacyImageMessageParts(getMessage());

        try {
            mMetadata = new ImageMessageMetadata();
            if (parts.getInfoPart() != null) {
                JSONObject infoObject = new JSONObject(new String(parts.getInfoPart().getData()));
                mMetadata.mOrientation = infoObject.getInt("orientation");
                mMetadata.mWidth = infoObject.getInt("width");
                mMetadata.mHeight = infoObject.getInt("height");
            }
            if (parts.getPreviewPart() != null) {
                parsePreviewPart(parts.getPreviewPart());
            }
            parseSourcePart(parts.getFullPart());
        } catch (JSONException e) {
            if (Log.isLoggable(Log.ERROR)) {
                Log.e(e.getMessage(), e);
            }
        }
    }

    /**
     * Images can have a varying format in their mime type. Strip the type and just use the prefix
     * in the mime type tree.
     *
     * @return Mime type tree representing a legacy single or three part image
     */
    @Override
    protected String createLegacyMimeTypeTree() {
        StringBuilder sb = new StringBuilder();
        boolean prependComma = false;
        for (MessagePart part : getMessage().getMessageParts()) {
            if (prependComma) {
                sb.append(",");
            }
            if (part.getMimeType().startsWith(LegacyMimeTypes.LEGACY_IMAGE_MIME_TYPE_IMAGE_PREFIX)
                    && !part.getMimeType().equals(LegacyMimeTypes.LEGACY_IMAGE_MIME_TYPE_PREVIEW)) {
                sb.append(LegacyMimeTypes.LEGACY_IMAGE_MIME_TYPE_IMAGE_PREFIX);
            } else {
                sb.append(part.getMimeType());
            }
            sb.append("[]");
            prependComma = true;
        }
        return sb.toString();
    }

    @Override
    protected void parse(@NonNull MessagePart messagePart) {
        parseRootMessagePart(messagePart);
    }

    @Override
    protected boolean parseChildPart(@NonNull MessagePart childMessagePart) {
        if (MessagePartUtils.isRole(childMessagePart, ROLE_PREVIEW)) {
            parsePreviewPart(childMessagePart);
            return true;
        } else if (MessagePartUtils.isRole(childMessagePart, ROLE_SOURCE)) {
            parseSourcePart(childMessagePart);
            return true;
        }

        return false;
    }

    @Override
    protected boolean shouldDownloadContentIfNotReady(@NonNull MessagePart messagePart) {
        return true;
    }

    @Override
    public String getActionEvent() {
        if (super.getActionEvent() != null) {
            return super.getActionEvent();
        }

        if (mMetadata.mAction != null) {
            return mMetadata.mAction.getEvent();
        } else {
            return ACTION_EVENT_OPEN_URL;
        }
    }

    @NonNull
    @Override
    public JsonObject getActionData() {
        if (super.getActionData().size() > 0) {
            return super.getActionData();
        }

        if (mMetadata == null) {
            return new JsonObject();
        } else if (mMetadata.mAction != null) {
            return mMetadata.mAction.getData();
        } else {
            Action action = new Action(ACTION_EVENT_OPEN_URL);
            String url = null;
            int width, height;
            if (mMetadata.mPreviewUrl != null) {
                url = mMetadata.mPreviewUrl;
                width = mMetadata.getPreviewWidth();
                height = mMetadata.getPreviewHeight();
            } else if (mMetadata.mSourceUrl != null) {
                url = mMetadata.mSourceUrl;
                width = mMetadata.getWidth();
                height = mMetadata.getHeight();
            } else {
                if (mSourceRequestParameters != null && mSourceRequestParameters.getUri() != null) {
                    url = mSourceRequestParameters.getUri().toString();
                } else if (mPreviewRequestParameters != null && mPreviewRequestParameters.getUri() != null) {
                    url = mPreviewRequestParameters.getUri().toString();
                }
                width = mMetadata.getWidth();
                height = mMetadata.getHeight();
            }

            action.getData().addProperty("url", url);
            action.getData().addProperty("mime-type", mMetadata.mMimeType);
            action.getData().addProperty("width", width);
            action.getData().addProperty("height", height);
            action.getData().addProperty("orientation", mMetadata.mOrientation);
            return action.getData();
        }
    }

    @Nullable
    @Override
    public String getPreviewText() {
        String title = getTitle();
        return title != null ? title : getAppContext().getString(R.string.xdk_ui_image_message_preview_text);
    }

    /*
     * Private methods
     */

    private void parseRootMessagePart(MessagePart messagePart) {
        JsonReader reader = new JsonReader(new InputStreamReader(messagePart.getDataStream()));
        mMetadata = getGson().fromJson(reader, ImageMessageMetadata.class);

        Message message = getMessage();
        if (!MessagePartUtils.hasMessagePartWithRole(message, ROLE_PREVIEW, ROLE_SOURCE)) {
            ImageRequestParameters.Builder previewRequestBuilder = new ImageRequestParameters.Builder();
            ImageRequestParameters.Builder sourceRequestBuilder = new ImageRequestParameters.Builder();
            String previewUrl;
            int width = 0;
            int height = 0;
            if (mMetadata.mPreviewUrl != null) {
                previewUrl = mMetadata.mPreviewUrl;
                width = mMetadata.getPreviewWidth();
                height = mMetadata.getPreviewHeight();
            } else if (mMetadata.mSourceUrl != null) {
                previewUrl = mMetadata.mSourceUrl;
                width = mMetadata.getWidth();
                height = mMetadata.getHeight();

                sourceRequestBuilder.url(mMetadata.mSourceUrl);
                if (width > 0 && height > 0) {
                    sourceRequestBuilder.resize(width, height);
                }
                sourceRequestBuilder.exifOrientation(mMetadata.mOrientation)
                        .tag(getClass().getSimpleName());

                mSourceRequestParameters = sourceRequestBuilder.build();
            } else {
                previewUrl = null;
            }

            if (width > 0 && height > 0) {
                previewRequestBuilder.resize(width, height);
            }

            previewRequestBuilder.url(previewUrl)
                    .placeHolder(PLACEHOLDER)
                    .exifOrientation(mMetadata.mOrientation)
                    .tag(getClass().getSimpleName());

            mPreviewRequestParameters = previewRequestBuilder.build();
        }
    }

    private void parsePreviewPart(MessagePart messagePart) {
        ImageRequestParameters.Builder builder = new ImageRequestParameters.Builder();
        if (messagePart.getId() != null) {
            builder.uri(messagePart.getId());
        } else {
            builder.url(mMetadata.mPreviewUrl);
        }

        builder.placeHolder(PLACEHOLDER)
                .exifOrientation(mMetadata.mOrientation)
                .tag(getClass().getSimpleName());

        if (mMetadata.getPreviewWidth() > 0 && mMetadata.getPreviewHeight() > 0) {
            builder.resize(mMetadata.getPreviewWidth(), mMetadata.getPreviewHeight());
        }
        mPreviewRequestParameters = builder.build();
    }

    private void parseSourcePart(MessagePart messagePart) {
        ImageRequestParameters.Builder builder = new ImageRequestParameters.Builder();
        if (messagePart.getId() != null) {
            builder.uri(messagePart.getId());
        } else {
            builder.url(mMetadata.mSourceUrl);
        }

        builder.placeHolder(PLACEHOLDER)
                .resize(mMetadata.getWidth(), mMetadata.getHeight())
                .exifOrientation(mMetadata.mOrientation)
                .tag(getClass().getSimpleName());

        mSourceRequestParameters = builder.build();
    }

    @Nullable
    public ImageMessageMetadata getMetadata() {
        return mMetadata;
    }

    /*
     * Setters, getters, bindings
     */

    public ImageRequestParameters getPreviewRequestParameters() {
        return mPreviewRequestParameters;
    }

    public ImageRequestParameters getSourceRequestParameters() {
        return mSourceRequestParameters;
    }

    @Override
    public String getTitle() {
        return mMetadata != null ? mMetadata.mTitle : null;
    }

    @Override
    public String getDescription() {
        return mMetadata != null ? mMetadata.mSubtitle : null;
    }

    @Override
    public String getFooter() {
        return mMetadata != null ? mMetadata.mSubtitle : null;
    }

    @Override
    public boolean getHasContent() {
        return mMetadata != null && (mPreviewRequestParameters != null || mSourceRequestParameters != null);
    }

    private static class LegacyImageMessageParts {
        private MessagePart mInfoPart;
        private MessagePart mPreviewPart;
        private MessagePart mFullPart;

        LegacyImageMessageParts(Message message) {
            Set<MessagePart> messageParts = message.getMessageParts();

            for (MessagePart part : messageParts) {
                if (part.getMimeType().equals(LegacyMimeTypes.LEGACY_IMAGE_MIME_TYPE_INFO)) {
                    mInfoPart = part;
                } else if (part.getMimeType().equals(LegacyMimeTypes.LEGACY_IMAGE_MIME_TYPE_PREVIEW)) {
                    mPreviewPart = part;
                } else if (part.getMimeType().startsWith(LegacyMimeTypes.LEGACY_IMAGE_MIME_TYPE_IMAGE_PREFIX)) {
                    mFullPart = part;
                }
            }

            if (messageParts.size() == 3
                    && (mInfoPart == null || mPreviewPart == null || mFullPart == null)) {
                if (Log.isLoggable(Log.ERROR)) {
                    Log.e("Incorrect parts for a three part image: " + messageParts);
                }
                throw new IllegalArgumentException("Incorrect parts for a three part image: " + messageParts);
            }
        }

        @Nullable
        MessagePart getInfoPart() {
            return mInfoPart;
        }

        @Nullable
        MessagePart getPreviewPart() {
            return mPreviewPart;
        }

        @NonNull
        MessagePart getFullPart() {
            return mFullPart;
        }
    }
}
