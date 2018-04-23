package com.layer.xdk.ui.message.receipt;

import com.google.gson.annotations.SerializedName;

@SuppressWarnings("WeakerAccess") // For Gson serialization/de-serialization
public class DiscountMetadata {

    @SerializedName("name")
    public String mName;
    @SerializedName("amount")
    public Double mAmount;
}
