package com.layer.xdk.ui.message.response;


import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * POJO that represents JSON for an add operation inside a response summary message part.
 */
public class SummaryAddOperationMetadata {

    @SerializedName("ids")
    public List<String> mIds;

    @SerializedName("value")
    public String mValue;

}
