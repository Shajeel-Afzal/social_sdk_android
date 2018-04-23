package com.layer.xdk.ui.message.choice;

import android.support.annotation.NonNull;

import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import com.layer.xdk.ui.message.model.Action;

import java.util.List;

/**
 * This metadata class represents a choice message. Since it contains a
 * {@link ChoiceConfigMetadata}, it merely subclasses that class and adds the necessary fields.
 */
@SuppressWarnings("WeakerAccess") // For Gson serialization/de-serialization
public class ChoiceMessageMetadata extends ChoiceConfigMetadata {
    public static final String CHOICE_TYPE_STANDARD = "standard";

    @SerializedName("title")
    public String mTitle;

    @SerializedName("label")
    public String mLabel;

    @SerializedName("choices")
    public List<ChoiceMetadata> mChoices;

    @SerializedName("type")
    public String mType;

    @SerializedName("expanded_type")
    public String mExpandedType;

    @SerializedName("action")
    public Action mAction;

    @SerializedName("custom_data")
    public JsonObject mCustomData;

    @NonNull
    public String getType() {
        if (mType != null) {
            return mType;
        } else {
            return CHOICE_TYPE_STANDARD;
        }
    }
}
