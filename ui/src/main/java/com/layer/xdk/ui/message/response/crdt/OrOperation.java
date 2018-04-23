package com.layer.xdk.ui.message.response.crdt;


import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

import java.util.Random;

/**
 * An operation performed on an OR set. This contains a value and an operation ID. The operation
 * ID is a 6 character randomly generated string.
 *
 * Object equality is determined by the operation ID.
 */
public class OrOperation {
    private static final String OPERATION_ID_CHARSET = "#$%&'()*+,-./0123456789:;"
            + "<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~";
    private static final int OPERATION_ID_LENGTH = 6;

    @SerializedName("value")
    String mValue;
    @SerializedName("operationId")
    String mOperationId;

    /**
     * Create an operation with a defined value and a randomly generated operation ID.
     *
     * @param value value for the operation
     */
    public OrOperation(@NonNull String value) {
        this(value, null);
    }

    /**
     * Create an operation with a defined value and operation ID.
     *
     * @param value value for the operation
     * @param operationId ID for the operation
     */
    public OrOperation(@NonNull String value, @Nullable String operationId) {
        mValue = value;
        if (operationId == null) {
            mOperationId = generateOperationId();
        } else {
            mOperationId = operationId;
        }
    }

    /**
     * Generate a random string to use for the operation ID. This uses the defined length and
     * charset for the generation.
     *
     * @return randomly generated operation ID
     */
    private String generateOperationId() {
        StringBuilder sb = new StringBuilder(OPERATION_ID_LENGTH);
        Random random = new Random();
        for (int i = 0; i < OPERATION_ID_LENGTH; i++) {
            sb.append(OPERATION_ID_CHARSET.charAt(random.nextInt(OPERATION_ID_CHARSET.length())));
        }
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        OrOperation that = (OrOperation) o;

        return mOperationId.equals(that.mOperationId);
    }

    @Override
    public int hashCode() {
        return mOperationId.hashCode();
    }
}
