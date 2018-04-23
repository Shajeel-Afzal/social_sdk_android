package com.layer.xdk.ui.message.response.crdt;


import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * This is not a pure OR (Observe-remove) Set but rather a variation of one since the rules when
 * adding are a bit different. This is normally used with choice selections and subclasses define
 * more specific rules to change how add/removes work.
 *
 * This class and its subclasses should be able to be serialized/de-serialized via Gson.
 */
@SuppressWarnings("WeakerAccess") // For Gson access
public abstract class ORSet {

    @SerializedName("propertyName")
    String mPropertyName;
    @SerializedName("adds")
    LinkedHashSet<OrOperation> mAdds;
    @SerializedName("removes")
    LinkedHashSet<String> mRemoves;
    @SerializedName("type")
    String mType = getType();

    /**
     * @param propertyName name to use for {@link OrOperationResult} objects. Usually some key value
     * @param adds an ordered set of {@link OrOperation}s that denote additions to this set
     * @param removes an ordered set of {@link String}s that denote operation ID removals from this
     *                set
     */
    public ORSet(String propertyName, LinkedHashSet<OrOperation> adds,
            LinkedHashSet<String> removes) {
        mPropertyName = propertyName;
        mAdds = adds;
        mRemoves = removes;
    }

    /**
     * Get the type of this OR Set. This has two purposes. The first is used in an
     * {@link OrOperationResult} so the server knows how to handle operations. The second is to
     * instantiate the correct class when de-serializing from a JSON representation.
     *
     * @return the type of this set
     */
    @NonNull
    public abstract String getType();

    /**
     * @return an ordered set of the currently selected values
     */
    @NonNull
    public synchronized LinkedHashSet<String> getSelectedValues() {
        // Use a set that maintains insertion order when iterating
        LinkedHashSet<String> set = new LinkedHashSet<>();
        for (OrOperation addOp : getAdds()) {
            set.add(addOp.mValue);
        }
        return set;
    }

    /**
     * Perform an add operation to this set using the specified operation. A list of
     * {@link OrOperationResult} objects is returned so they can be sent to the server as a
     * response.
     *
     * @param operation operation to add to this set
     * @return a list with a single operation result if this operation ID was not already added.
     * Null will be returned if the operation ID has already been added
     */
    @Nullable
    public synchronized List<OrOperationResult> addOperation(@NonNull OrOperation operation) {
        if (containsOperation(operation.mOperationId)) {
            return null;
        }

        getAdds().add(operation);
        return Collections.singletonList(
                createOperationResult(operation.mOperationId, operation.mValue, "add"));
    }

    /**
     * Remove the operation with the specified ID from the set. A list of
     * {@link OrOperationResult} objects is returned so they can be sent to the server as a
     * response.
     *
     * @param operationId ID of the operation to remove from this set
     * @return a list with a single operation result if this operation ID was previously added, thus
     * being removed. Null will be returned if the operation ID is already in the removes set.
     */
    @Nullable
    public synchronized List<OrOperationResult> removeOperation(@NonNull String operationId) {
        String value = null;
        OrOperation operation = getOperation(operationId);
        if (operation != null) {
            getAdds().remove(operation);
            value = operation.mValue;
        }
        if (!getRemoves().contains(operationId)) {
            getRemoves().add(operationId);
            return Collections.singletonList(
                    createOperationResult(operationId, value, "remove"));
        }
        return null;
    }

    /**
     * Merge this set on top of another set, using the provided set as the base and applying
     * existing adds and removes on top of it.
     *
     * @param other set to synchronize with
     */
    public synchronized void synchronize(@Nullable ORSet other) {
        if (other == null) {
            return;
        }

        LinkedHashSet<OrOperation> oldAdds = getAdds();
        LinkedHashSet<String> oldRemoves = getRemoves();
        mAdds = new LinkedHashSet<>(other.getAdds());
        mRemoves = new LinkedHashSet<>(other.getRemoves());

        for (String operationId : oldRemoves) {
            OrOperation toRemove = getOperation(operationId);
            if (toRemove != null) {
                mAdds.remove(toRemove);
            }
            mRemoves.add(operationId);
        }

        for (OrOperation operation : oldAdds) {
            if (!mRemoves.contains(operation.mOperationId)) {
                if (!mAdds.contains(operation)) {
                    mAdds.add(operation);
                }
            }
        }
    }

    /**
     * Searches for an add operation that has the specified value.
     *
     * @param value value of the operation to search for
     * @return a list of operation IDs that have the specified value or an empty set if none do
     */
    @NonNull
    public synchronized List<String> findAddOperationsWithValue(@NonNull String value) {
        List<String> operationIds = new ArrayList<>();
        for (OrOperation operation : getAdds()) {
            if (value.equals(operation.mValue)) {
                operationIds.add(operation.mOperationId);
            }
        }
        return operationIds;
    }


    /**
     * @return the current set of add operations, creating a new set if none existed
     */
    @NonNull
    protected synchronized LinkedHashSet<OrOperation> getAdds() {
        if (mAdds == null) {
            mAdds = new LinkedHashSet<>();
        }
        return mAdds;
    }

    /**
     * @return the current set of removed operation IDs, creating a new set if none existed
     */
    @NonNull
    protected synchronized LinkedHashSet<String> getRemoves() {
        if (mRemoves == null) {
            mRemoves = new LinkedHashSet<>();
        }
        return mRemoves;
    }

    /**
     * Create a result object that represents the result of an add or remove operation. Usually
     * used to send to a remote server.
     *
     * @param operationId ID of the operation performed
     * @param value value which the operation was performed on
     * @param operation what operation was performed, either "add" or "remove"
     * @return a result distilling what the operation did
     */
    @NonNull
    protected final OrOperationResult createOperationResult(String operationId, String value,
            String operation) {
        OrOperationResult result = new OrOperationResult();
        result.mId = operationId;
        result.mValue = value;
        result.mName = mPropertyName;
        result.mOperation = operation;
        result.mType = getType();
        return result;
    }

    /**
     * @param operationId ID of an operation to search for
     * @return true if this operation ID is present in the operation adds set, false otherwise
     */
    protected synchronized final boolean containsOperation(@NonNull String operationId) {
        return getOperation(operationId) != null;
    }

    /**
     * @param operationId ID of an operation to search for
     * @return an Operation with the specified ID if present in the operation adds set, null
     * otherwise
     */
    @Nullable
    protected synchronized final OrOperation getOperation(@NonNull String operationId) {
        for (OrOperation operation : getAdds()) {
            if (operation.mOperationId.equals(operationId)) {
                return operation;
            }
        }
        return null;
    }
}
