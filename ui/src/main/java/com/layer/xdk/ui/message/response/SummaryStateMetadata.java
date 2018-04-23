package com.layer.xdk.ui.message.response;


import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * POJO that represents state JSON inside a response summary message part.
 */
public class SummaryStateMetadata {

    @SerializedName("adds")
    public List<SummaryAddOperationMetadata> mAddOperations;

    @SerializedName("removes")
    public List<String> mRemoveIds;
}
