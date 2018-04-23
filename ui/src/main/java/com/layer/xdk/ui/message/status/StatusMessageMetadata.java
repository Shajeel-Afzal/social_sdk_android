package com.layer.xdk.ui.message.status;


import com.google.gson.annotations.SerializedName;
import com.layer.xdk.ui.message.model.Action;

/**
 * Metadata for a status message
 */
public class StatusMessageMetadata {

    @SerializedName("text")
    public String mText;

    @SerializedName("action")
    public Action mAction;
}
