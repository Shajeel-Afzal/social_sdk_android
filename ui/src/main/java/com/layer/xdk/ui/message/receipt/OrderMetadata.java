package com.layer.xdk.ui.message.receipt;

import com.google.gson.annotations.SerializedName;

@SuppressWarnings("WeakerAccess") // For Gson serialization/de-serialization
public class OrderMetadata {

    @SerializedName("number")
    public String mNumber;

    @SerializedName("url")
    public String mUrl;
}
