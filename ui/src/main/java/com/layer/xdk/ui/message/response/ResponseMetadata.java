package com.layer.xdk.ui.message.response;


import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.gson.annotations.SerializedName;
import com.layer.xdk.ui.message.response.crdt.OrOperationResult;

import java.util.List;
import java.util.UUID;

/**
 * Metadata for a response message
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class ResponseMetadata {

    @SerializedName("response_to")
    public String mMessageIdToRespondTo;

    @SerializedName("response_to_node_id")
    public String mPartIdToRespondTo;

    @SerializedName("changes")
    public List<OrOperationResult> mChanges;

    public ResponseMetadata(@Nullable Uri messageIdToRespondTo, @Nullable UUID partIdToRespondTo,
            @NonNull List<OrOperationResult> changes) {
        mMessageIdToRespondTo = messageIdToRespondTo == null ? null : messageIdToRespondTo.toString();
        mPartIdToRespondTo = partIdToRespondTo == null ? null : partIdToRespondTo.toString();
        mChanges = changes;
    }
}
