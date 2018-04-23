package com.layer.xdk.ui.message.link;

import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import com.layer.xdk.ui.message.model.Action;

@SuppressWarnings("WeakerAccess") // For Gson serialization/de-serialization
public class LinkMessageMetadata {

    @SerializedName("author")
    public String mAuthor;
    @SerializedName("description")
    public String mDescription;
    @SerializedName("title")
    public String mTitle;
    @SerializedName("image_url")
    public String mImageUrl;
    @SerializedName("url")
    public String mUrl;
    @SerializedName("action")
    public Action mAction;
    @SerializedName("custom_data")
    public JsonObject mCustomData;
}
