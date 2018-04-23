package com.layer.xdk.ui.message.response.crdt;


import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.LinkedHashSet;
import java.util.List;

/**
 * A specialized OR Set that acts as a register (providing a single value). This represents a
 * single selection state that can only be selected once. Further selections are ignored.
 */
public class FirstWriterWinsRegister extends ORSet {
    static final String TYPE = "FWW";

    /**
     * @param propertyName name to use for {@link OrOperationResult} objects. Usually some key value
     * @param adds an ordered set of {@link OrOperation}s that denote additions to this register
     * @param removes an ordered set of {@link String}s that denote operation ID removals from this
     *                register
     */
    public FirstWriterWinsRegister(String propertyName,
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
     * Perform an add operation to this register, only allowing a single value to ever be set. A
     * list of {@link OrOperationResult} objects is returned so they can be sent to the server as a
     * response.
     *
     * @param operation operation to add to this register
     * @return a list with a single operation result if any operation has not yet been added,
     * null otherwise
     */
    @Nullable
    @Override
    public synchronized List<OrOperationResult> addOperation(@NonNull OrOperation operation) {
        if (containsOperation(operation.mOperationId)) {
            return null;
        }

        if (!getAdds().isEmpty()) {
            return null;
        }
        return super.addOperation(operation);
    }
}
