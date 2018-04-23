package com.layer.xdk.ui.message.button;

import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import com.layer.xdk.ui.message.choice.ChoiceConfigMetadata;
import com.layer.xdk.ui.message.choice.ChoiceMetadata;

import java.util.List;

@SuppressWarnings("WeakerAccess") // For Gson serialization/de-serialization
public class ButtonMetadata {
    public static final String TYPE_ACTION = "action";
    public static final String TYPE_CHOICE = "choice";

    @SerializedName("type")
    public String mType;

    @SerializedName("text")
    public String mText;

    @SerializedName("event")
    public String mEvent;

    @SerializedName("choices")
    public List<ChoiceMetadata> mChoices;

    @SerializedName("data")
    public JsonObject mData;

    public transient ChoiceConfigMetadata mChoiceConfigMetadata;
}
