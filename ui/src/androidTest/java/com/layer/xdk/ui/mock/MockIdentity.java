package com.layer.xdk.ui.mock;

import android.net.Uri;
import android.support.annotation.NonNull;

import com.layer.sdk.messaging.Identity;
import com.layer.sdk.messaging.Metadata;
import com.layer.sdk.messaging.Presence;

import java.util.Date;
import java.util.UUID;

public class MockIdentity implements Identity {

    private static final int MOCK_AVAILABLE = 1;
    private static final int MOCK_AWAY = 2;
    private static final int MOCK_OFFLINE = 3;
    private static final int MOCK_INVISIBLE = 4;
    private static final int MOCK_BUSY = 5;

    private Date mLastSeenAt;
    private String mUserId;

    public MockIdentity() {
        mLastSeenAt = new Date();
        mUserId = UUID.randomUUID().toString();
    }

    @NonNull
    @Override
    public Uri getId() {
        return null;
    }

    @NonNull
    @Override
    public String getUserId() {
        return mUserId;
    }

    @Override
    public String getDisplayName() {
        return "Mock Test";
    }

    @Override
    public String getFirstName() {
        return "Mock";
    }

    @Override
    public String getLastName() {
        return "Test";
    }

    @Override
    public String getPhoneNumber() {
        return null;
    }

    @Override
    public String getEmailAddress() {
        return null;
    }

    @Override
    public String getAvatarImageUrl() {
        return null;
    }

    @NonNull
    @Override
    public Metadata getMetadata() {
        return null;
    }

    @Override
    public String getPublicKey() {
        return null;
    }

    @Override
    public boolean isFollowed() {
        return false;
    }

    @Override
    public void follow() {

    }

    @Override
    public void unFollow() {

    }

    @Override
    public Presence.PresenceStatus getPresenceStatus() {
        return Presence.PresenceStatus.BUSY;
    }

    public Presence.PresenceStatus getPresenceStatus(int i) {
        switch (i) {
            case MOCK_AVAILABLE:
                return Presence.PresenceStatus.AVAILABLE;
            case MOCK_AWAY:
                return Presence.PresenceStatus.AWAY;
            case MOCK_OFFLINE:
                return Presence.PresenceStatus.OFFLINE;
            case MOCK_INVISIBLE:
                return Presence.PresenceStatus.INVISIBLE;
            case MOCK_BUSY:
                return Presence.PresenceStatus.BUSY;
            default:
                return Presence.PresenceStatus.AVAILABLE;
        }
    }

    @Override
    public Date getLastSeenAt() {
        return mLastSeenAt;
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof Identity) {
            Identity otherIdentity = (Identity) other;
            return this.getUserId().equals(otherIdentity.getUserId());
        }

        return false;
    }
}
