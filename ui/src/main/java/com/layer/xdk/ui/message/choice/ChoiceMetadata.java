package com.layer.xdk.ui.message.choice;

import com.google.gson.annotations.SerializedName;

@SuppressWarnings("WeakerAccess") // For Gson serialization/de-serialization
public class ChoiceMetadata {

    @SerializedName("id")
    public String mId;

    @SerializedName("text")
    public String mText;

    @SerializedName("tooltip")
    public String mTooltip;
}