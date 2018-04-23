package com.layer.xdk.ui.message.location;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.gson.annotations.SerializedName;
import com.layer.xdk.ui.message.model.Action;

import org.json.JSONObject;

@SuppressWarnings("WeakerAccess") // For Gson serialization/de-serialization
public class LocationMessageMetadata {

    @SerializedName("accuracy")
    public Double mAccuracy;

    @SerializedName("heading")
    public Double mHeading;
    @SerializedName("altitude")
    public Double mAltitude;
    @SerializedName("latitude")
    public Double mLatitude;
    @SerializedName("longitude")
    public Double mLongitude;

    @SerializedName("street1")
    public String mStreet1;
    @SerializedName("street2")
    public String mStreet2;
    @SerializedName("city")
    public String mCity;
    @SerializedName("administrative_area")
    public String mAdministrativeArea;
    @SerializedName("country")
    public String mCountry;
    @SerializedName("postal_code")
    public String mPostalCode;

    @SerializedName("zoom")
    public Integer mZoom;

    @SerializedName("created_at")
    public String mCreatedAt;
    @SerializedName("title")
    public String mTitle;
    @SerializedName("description")
    public String mDescription;

    @SerializedName("custom_data")
    public JSONObject mCustomData;

    @SerializedName("action")
    public Action mAction;

    @NonNull
    public Double getAltitude() {
        return mAltitude != null ? mAltitude : 0.0f;
    }

    @NonNull
    public Integer getZoom() {
        return mZoom != null ? mZoom : 17;
    }

    @Nullable
    public String getFormattedAddress() {
        StringBuilder formattedAddress = new StringBuilder();

        if (mStreet1 != null) {
            formattedAddress.append(mStreet1).append(" ");
        }

        if (mStreet2 != null) {
            formattedAddress.append(mStreet2).append("\n");
        }

        if (mCity != null) {
            if (formattedAddress.length() > 0) formattedAddress.append("\n");
            formattedAddress.append(mCity).append(" ");
        }

        if (mAdministrativeArea != null) {
            formattedAddress.append(mAdministrativeArea).append(" ");
        }

        if (mPostalCode != null) {
            formattedAddress.append(mPostalCode).append(" ");
        }

        if (mCountry != null) {
            if (formattedAddress.length() > 0) formattedAddress.append("\n");
            formattedAddress.append("\n").append(mCountry);
        }

        return formattedAddress.length() > 0 ? formattedAddress.toString() : null;
    }
}
