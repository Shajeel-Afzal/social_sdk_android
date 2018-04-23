package com.layer.xdk.ui.message.receipt;

import com.google.gson.annotations.SerializedName;

@SuppressWarnings("WeakerAccess") // For Gson serialization/de-serialization
public class SummaryMetadata {

    @SerializedName("shipping_cost")
    public Double mShippingCost;

    @SerializedName("subtotal")
    public Double mSubtotal;

    @SerializedName("total_cost")
    public Double mTotalCost;

    @SerializedName("total_tax")
    public Double mTotalTax;
}
