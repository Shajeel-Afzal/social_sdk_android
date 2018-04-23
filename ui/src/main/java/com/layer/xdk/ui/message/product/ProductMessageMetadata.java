package com.layer.xdk.ui.message.product;

import android.content.Context;
import android.support.annotation.NonNull;

import com.google.gson.annotations.SerializedName;
import com.layer.xdk.ui.R;
import com.layer.xdk.ui.message.model.Action;

import java.util.List;

@SuppressWarnings("WeakerAccess") // For Gson serialization/de-serialization
public class ProductMessageMetadata {

    @SerializedName("brand")
    public String mBrand;

    @SerializedName("name")
    public String mName;

    @SerializedName("image_urls")
    public List<String> mImageUrls;

    @SerializedName("price")
    public Float mPrice;

    @SerializedName("quantity")
    public Integer mQuantity;

    @SerializedName("currency")
    public String mCurrency;

    @SerializedName("description")
    public String mDescription;

    @SerializedName("url")
    public String mUrl;

    @SerializedName("action")
    public Action mAction;

    public int getQuantity() {
        return mQuantity != null ? mQuantity : 1;
    }

    @NonNull
    public String getCurrency(Context context) {
        return mCurrency != null ? mCurrency : context.getString(R.string.xdk_ui_product_message_model_default_currency);
    }
}
