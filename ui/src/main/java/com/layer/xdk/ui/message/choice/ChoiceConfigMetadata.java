package com.layer.xdk.ui.message.choice;


import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

@SuppressWarnings("WeakerAccess") // For Gson serialization/de-serialization
public class ChoiceConfigMetadata {
    public static final String DEFAULT_RESPONSE_NAME = "selection";

    @SerializedName("response_name")
    public String mResponseName;

    @SerializedName("name")
    public String mName;

    @SerializedName("allow_reselect")
    public boolean mAllowReselect;

    @SerializedName("allow_deselect")
    public boolean mAllowDeselect;

    @SerializedName("allow_multiselect")
    public boolean mAllowMultiselect;

    @SerializedName("enabled_for")
    public String mEnabledFor;

    @SerializedName("custom_response_data")
    public JsonObject mCustomResponseData;

    public transient boolean mEnabledForMe;

    @NonNull
    public String getResponseName() {
        if (TextUtils.isEmpty(mResponseName)) {
            return DEFAULT_RESPONSE_NAME;
        } else {
            return mResponseName;
        }
    }

    public void setEnabledForMe(@Nullable Uri authenticatedUserId) {
        if (authenticatedUserId == null) {
            mEnabledForMe = false;
            return;
        }
        String myUserID = authenticatedUserId.toString();
        mEnabledForMe = mEnabledFor == null || mEnabledFor.equals(myUserID);
    }
}
