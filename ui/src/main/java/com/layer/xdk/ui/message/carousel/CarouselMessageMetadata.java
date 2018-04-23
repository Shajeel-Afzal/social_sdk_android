package com.layer.xdk.ui.message.carousel;

import com.google.gson.annotations.SerializedName;
import com.layer.xdk.ui.message.model.Action;

@SuppressWarnings("WeakerAccess") // For Gson serialization/de-serialization
public class CarouselMessageMetadata {

    @SerializedName("action")
    public Action mAction;
}
