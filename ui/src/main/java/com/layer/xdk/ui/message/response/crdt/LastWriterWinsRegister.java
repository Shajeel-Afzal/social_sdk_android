package com.layer.xdk.ui.message.response.crdt;


import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.layer.xdk.ui.util.Log;

import java.util.LinkedHashSet;
import java.util.List;

/**
 * A specialized OR Set that acts as a register (providing a single value). This represents a
 * single selection state with an ability to only have one value selected at a time. It is not
 * possible to remove an added value from this set as that is done automatically during the add.
 * This ensures only one value is ever selected.
 */
public class LastWriterWinsRegister extends LastWriterWinsNullableRegister {
    static final String TYPE = "LWW";

    /**
     * @param propertyName name to use for {@link OrOperationResult} objects. Usually some key value
     * @param adds an ordered set of {@link OrOperation}s that denote additions to this register
     * @param removes an ordered set of {@link String}s that denote operation ID removals from this
     *                register
     */
    public LastWriterWinsRegister(String propertyName,
            LinkedHashSet<OrOperation> adds,
            LinkedHashSet<String> removes) {
        super(propertyName, adds, removes);
    }

    @NonNull
    @Override
    public String getType() {
        return TYPE;
    }

    /**
     * Remove the operation with the specified operation ID from the register. This is normally
     * not called on this type of register.
     *
     * @param operationId ID of the operation to remove from this set
     * @return a list of operations if this ID is not already added to the removes set,
     * null otherwise
     * @throws IllegalArgumentException if this register contains this operation ID as values cannot
     * be explicitly un-set.
     */
    @Nullable
    @Override
    public synchronized List<OrOperationResult> removeOperation(@NonNull String operationId) {
        if (containsOperation(operationId)) {
            if (Log.isLoggable(Log.ERROR)) {
                Log.e("Cannot remove added value from LWW Register. Operation ID: " + operationId);
            }
            throw new IllegalArgumentException("Cannot remove added value from LWW Register. "
                    + "Operation ID: " + operationId);
        }

        return super.removeOperation(operationId);
    }
}
