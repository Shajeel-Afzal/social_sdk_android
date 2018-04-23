package com.layer.xdk.ui.message.button;

import com.google.gson.annotations.SerializedName;

import java.util.List;

@SuppressWarnings("WeakerAccess") // For Gson serialization/de-serialization
public class ButtonMessageMetadata {

    @SerializedName("buttons")
    public List<ButtonMetadata> mButtonMetadata;
}
