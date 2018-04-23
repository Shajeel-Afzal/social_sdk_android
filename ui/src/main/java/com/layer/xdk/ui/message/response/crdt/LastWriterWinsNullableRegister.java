package com.layer.xdk.ui.message.response.crdt;


import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * A specialized OR Set that acts as a register (providing a single value). This represents a
 * single selection state with an ability to deselect the selection (i.e. to make null).
 */
public class LastWriterWinsNullableRegister extends ORSet {
    static final String TYPE = "LWWN";

    /**
     * @param propertyName name to use for {@link OrOperationResult} objects. Usually some key value
     * @param adds an ordered set of {@link OrOperation}s that denote additions to this register
     * @param removes an ordered set of {@link String}s that denote operation ID removals from this
     *                register
     */
    public LastWriterWinsNullableRegister(String propertyName,
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
     * Perform an add operation to this register, removing other values if needed. A list of
     * {@link OrOperationResult} objects is returned so they can be sent to the server as a
     * response.
     *
     * @param operation operation to add to this register
     * @return a list of operation results or null if this operation is already in the register
     */
    @Nullable
    @Override
    public synchronized List<OrOperationResult> addOperation(@NonNull OrOperation operation) {
        if (containsOperation(operation.mOperationId)) {
            return null;
        }

        List<OrOperationResult> results = new ArrayList<>();
        for (OrOperation addedOperation : getAdds()) {
            List<OrOperationResult> removalResults = super.removeOperation(addedOperation.mOperationId);
            if (removalResults != null) {
                results.addAll(removalResults);
            }
        }

        List<OrOperationResult> addResults = super.addOperation(operation);
        if (addResults != null) {
            results.addAll(addResults);
        }

        return results;
    }
}
