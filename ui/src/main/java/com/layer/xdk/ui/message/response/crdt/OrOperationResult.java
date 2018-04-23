package com.layer.xdk.ui.message.response.crdt;


import com.google.gson.annotations.SerializedName;

/**
 * A POJO that represents the result of an operation on an {@link ORSet}. This is normally used for
 * converting to JSON.
 */
public class OrOperationResult {

    /**
     * The operation that was performed. Usually "add" or "remove".
     */
    @SerializedName("operation")
    String mOperation;

    /**
     * The type of the {@link ORSet} that was used for the operation.
     */
    @SerializedName("type")
    String mType;

    /**
     * The name of the property that was operated on. Usually a response name or other key.
     */
    @SerializedName("name")
    String mName;

    /**
     * The ID of this operation
     */
    @SerializedName("id")
    String mId;

    /**
     * The value that was mutated by this operation.
     */
    @SerializedName("value")
    String mValue;
}
