package com.layer.xdk.ui.message.image;

import android.support.annotation.Dimension;

import com.google.gson.annotations.SerializedName;
import com.layer.xdk.ui.message.model.Action;
import com.layer.xdk.ui.util.DisplayUtils;

@SuppressWarnings("WeakerAccess") // For Gson serialization/de-serialization
public class ImageMessageMetadata {
    @SerializedName("title")
    public String mTitle;

    @SerializedName("artist")
    public String mArtist;

    @SerializedName("subtitle")
    public String mSubtitle;

    @SerializedName("file_name")
    public String mFileName;

    @SerializedName("mime_type")
    public String mMimeType;

    @Dimension
    @SerializedName("width")
    public int mWidth;

    @Dimension
    @SerializedName("height")
    public int mHeight;

    @Dimension
    @SerializedName("preview_width")
    public int mPreviewWidth;

    @Dimension
    @SerializedName("preview_height")
    public int mPreviewHeight;

    @SerializedName("source_url")
    public String mSourceUrl;

    @SerializedName("preview_url")
    public String mPreviewUrl;

    @SerializedName("orientation")
    public int mOrientation;

    @SerializedName("action")
    public Action mAction;

    @Dimension
    public int getWidth() {
        return DisplayUtils.dpToPx(mWidth);
    }

    @Dimension
    public int getHeight() {
        return DisplayUtils.dpToPx(mHeight);
    }

    public int getPreviewWidth() {
        return DisplayUtils.dpToPx(mPreviewWidth);
    }

    @Dimension
    public int getPreviewHeight() {
        return DisplayUtils.dpToPx(mPreviewHeight);
    }
}
