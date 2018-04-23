package com.layer.xdk.ui.message.response.crdt;


import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

import java.util.HashMap;
import java.util.Map;

/**
 * Collection of {@link ORSet} objects keyed by user ID then by a state string. Used for caching
 * a collection of {@link ORSet}s.
 */
public class ORSetSummary {

    // Suppressed for Gson access
    @SuppressWarnings("WeakerAccess")
    @SerializedName("userStateSets")
    Map<String, Map<String, ORSet>> mUserStateOrSets;

    /**
     * Attempts to find an {@link ORSet} matching the given arguments. Intermediate {@link Map}s
     * will be created and added to this object if none exist yet.
     *
     * @param identityId The full ID of the identity to search for
     * @param state the state key to search for
     * @return a set matching the arguments or null if none exists
     */
    @Nullable
    public ORSet getSet(@NonNull String identityId, @NonNull String state) {
        if (mUserStateOrSets == null) {
            mUserStateOrSets = new HashMap<>(1);
        }
        Map<String, ORSet> stateSets = mUserStateOrSets.get(identityId);
        if (stateSets == null) {
            stateSets = new HashMap<>(1);
            mUserStateOrSets.put(identityId, stateSets);
        }
        return stateSets.get(state);
    }

    /**
     * Adds an {@link ORSet} to the maps matching the given arguments. Intermediate {@link Map}s
     * will be created and added to this object if none exist yet.
     *
     * @param identityId the full ID of the identity to add the set for
     * @param state the state key to add the set for
     * @param orSet the set to add
     */
    public void addSet(@NonNull String identityId, @NonNull String state, @NonNull ORSet orSet) {
        if (mUserStateOrSets == null) {
            mUserStateOrSets = new HashMap<>(1);
        }
        Map<String, ORSet> stateSets = mUserStateOrSets.get(identityId);
        if (stateSets == null) {
            stateSets = new HashMap<>(1);
            mUserStateOrSets.put(identityId, stateSets);
        }
        stateSets.put(state, orSet);
    }
}
