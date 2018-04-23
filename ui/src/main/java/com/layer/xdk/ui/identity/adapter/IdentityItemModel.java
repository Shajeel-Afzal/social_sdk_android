package com.layer.xdk.ui.identity.adapter;


import android.support.annotation.NonNull;

import com.layer.sdk.messaging.Identity;
import com.layer.sdk.messaging.Metadata;
import com.layer.sdk.messaging.Presence;
import com.layer.xdk.ui.identity.adapter.viewholder.IdentityItemVHModel;

import java.util.Date;
import java.util.Set;

/**
 * Model class that wraps an {@link Identity} object.
 * For use with {@link IdentityItemVHModel}s.
 */
public class IdentityItemModel {
    private final Identity mIdentity;

    // Cached values for deep equals
    private final String mDisplayName;
    private final String mFirstName;
    private final String mLastName;
    private final String mPhoneNumber;
    private final String mEmailAddress;
    private final String mAvatarUrl;
    private final String mPublicKey;
    private final Metadata mMetadata;
    private final boolean mFollowed;
    private final Presence.PresenceStatus mPresenceStatus;
    private final Date mLastSeenAt;

    /**
     * Constructs a model object
     *
     * @param identity the {@link Identity} backing this model
     */
    public IdentityItemModel(@NonNull Identity identity) {
        mIdentity = identity;

        mDisplayName = identity.getDisplayName();
        mFirstName = identity.getFirstName();
        mLastName = identity.getLastName();
        mPhoneNumber = identity.getPhoneNumber();
        mEmailAddress = identity.getEmailAddress();
        mAvatarUrl = identity.getAvatarImageUrl();
        mPublicKey = identity.getPublicKey();
        mMetadata = identity.getMetadata();
        mFollowed = identity.isFollowed();
        mPresenceStatus = identity.getPresenceStatus();
        mLastSeenAt = identity.getLastSeenAt();
    }

    /**
     * @return the {@link Identity} object backing this model
     */
    @NonNull
    public Identity getIdentity() {
        return mIdentity;
    }

    /**
     * Perform an equals check on all properties.
     *
     * This is primarily used for calculations with {@link android.support.v7.util.DiffUtil}.
     *
     * @param other model to compare to
     * @return true if all properties are equal
     */
    public boolean deepEquals(IdentityItemModel other) {
        if (mDisplayName == null ? other.mDisplayName != null
                : !mDisplayName.equals(other.mDisplayName)) {
            return false;
        }
        if (mFirstName == null ? other.mFirstName != null
                : !mFirstName.equals(other.mFirstName)) {
            return false;
        }
        if (mLastName == null ? other.mLastName != null
                : !mLastName.equals(other.mLastName)) {
            return false;
        }
        if (mAvatarUrl == null ? other.mAvatarUrl != null
                : !mAvatarUrl.equals(other.mAvatarUrl)) {
            return false;
        }
        if (mEmailAddress == null ? other.mEmailAddress != null
                : !mEmailAddress.equals(other.mEmailAddress)) {
            return false;
        }
        if (mPhoneNumber == null ? other.mPhoneNumber != null
                : !mPhoneNumber.equals(other.mPhoneNumber)) {
            return false;
        }
        if (mPublicKey == null ? other.mPublicKey != null
                : !mPublicKey.equals(other.mPublicKey)) {
            return false;
        }
        if (mPresenceStatus != other.mPresenceStatus) {
            return false;
        }
        if (mLastSeenAt == null ? other.mLastSeenAt != null
                : !mLastSeenAt.equals(other.mLastSeenAt)) {
            return false;
        }
        if (mFollowed != other.mFollowed) {
            return false;
        }

        if (mMetadata == null) {
            if (other.mMetadata != null) {
                return false;
            }
        } else if (other.mMetadata == null) {
            // This metadata is always non null here
            return false;
        } else {
            Set<String> keySet = mMetadata.keySet();
            if (!keySet.equals(other.mMetadata.keySet())) {
                return false;
            }
            for (String key : keySet) {
                if (!mMetadata.get(key).equals(other.mMetadata.get(key))) {
                    return false;
                }
            }
        }

        return true;
    }
}
