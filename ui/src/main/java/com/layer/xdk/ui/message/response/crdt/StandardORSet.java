package com.layer.xdk.ui.message.response.crdt;


import android.support.annotation.NonNull;

import java.util.LinkedHashSet;

/**
 * A standard set implementation that allows for multiple selections.
 */
public class StandardORSet extends ORSet {
    static final String TYPE = "Set";

    /**
     * @param propertyName name to use for {@link OrOperationResult} objects. Usually some key value
     * @param adds an ordered set of {@link OrOperation}s that denote additions to this set
     * @param removes an ordered set of {@link String}s that denote operation ID removals from this
     *                set
     */
    public StandardORSet(String propertyName,
            LinkedHashSet<OrOperation> adds,
            LinkedHashSet<String> removes) {
        super(propertyName, adds, removes);
    }

    @NonNull
    public String getType() {
        return TYPE;
    }
}
