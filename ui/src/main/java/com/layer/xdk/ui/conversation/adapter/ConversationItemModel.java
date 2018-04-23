package com.layer.xdk.ui.conversation.adapter;


import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.layer.sdk.messaging.Conversation;
import com.layer.sdk.messaging.Identity;
import com.layer.sdk.messaging.Metadata;
import com.layer.xdk.ui.conversation.adapter.viewholder.ConversationItemVHModel;
import com.layer.xdk.ui.identity.adapter.IdentityItemModel;
import com.layer.xdk.ui.message.model.MessageModel;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Model class that wraps a {@link Conversation} and its last message's {@link MessageModel} object.
 * For use with {@link ConversationItemVHModel}s.
 */
public class ConversationItemModel {
    private final Conversation mConversation;
    private final MessageModel mLastMessageModel;
    private final Set<Identity> mParticipants;
    private final Set<Identity> mParticipantsMinusAuthenticatedUser;

    // Cached values for deep equals
    private final Metadata mMetadata;
    private final int mUnreadMessageCount;
    // Identities must be copied to separate variables to compare via deepEquals as only one
    // identity object will ever exist for that identity.
    private final Set<IdentityItemModel> mCachedParticipants;

    /**
     * Constructs a model object
     *
     * @param conversation the {@link Conversation} backing this model
     * @param lastMessageModel the {@link MessageModel} for this conversation's last message or
     *                         null if there is no last message
     * @param authenticatedUser the user currently authenticated with
     *                          the {@link com.layer.sdk.LayerClient}
     */
    public ConversationItemModel(@NonNull Conversation conversation,
            @Nullable MessageModel lastMessageModel,
            @NonNull Identity authenticatedUser) {
        mConversation = conversation;
        mLastMessageModel = lastMessageModel;
        mParticipants = mConversation.getParticipants();
        mMetadata = mConversation.getMetadata();
        mUnreadMessageCount = mConversation.getTotalUnreadMessageCount();
        mCachedParticipants = new HashSet<>(mParticipants.size());
        for (Identity participant : mParticipants) {
            mCachedParticipants.add(new IdentityItemModel(participant));
        }

        mParticipantsMinusAuthenticatedUser = new HashSet<>(mConversation.getParticipants());
        mParticipantsMinusAuthenticatedUser.remove(authenticatedUser);
    }

    /**
     * @return the {@link Conversation} object backing this model
     */
    @NonNull
    public Conversation getConversation() {
        return mConversation;
    }

    /**
     * @return the {@link MessageModel} that represents the last message in the conversation or null
     * if there is no last message
     */
    @Nullable
    public MessageModel getLastMessageModel() {
        return mLastMessageModel;
    }

    /**
     * @return the participants of this conversation
     */
    public Set<Identity> getParticipants() {
        return mParticipants;
    }

    /**
     * @return the participants of this conversation but without the current authenticated user
     */
    public Set<Identity> getParticipantsMinusAuthenticatedUser() {
        return mParticipantsMinusAuthenticatedUser;
    }

    /**
     * Perform an equals check on select properties.
     *
     * This is primarily used for calculations with {@link android.support.v7.util.DiffUtil}.
     *
     * @param other model to compare to
     * @return true if all properties are equal
     */
    @SuppressWarnings("WeakerAccess")
    public boolean deepEquals(ConversationItemModel other) {
        if (mUnreadMessageCount != other.mUnreadMessageCount) {
            return false;
        }

        if (mLastMessageModel == null) {
            if (other.mLastMessageModel != null) {
                return false;
            }
        } else if (other.mLastMessageModel == null) {
            // This last message model is always non null here
            return false;
        } else {
            if (!mLastMessageModel.getMessage().getId().equals(
                    other.mLastMessageModel.getMessage().getId())) {
                return false;
            }
            if (!mLastMessageModel.deepEquals(other.mLastMessageModel)) {
                return false;
            }
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

        if (!mParticipants.equals(other.mParticipants)) {
            return false;
        }
        // Check participants
        if (mCachedParticipants.size() != other.mCachedParticipants.size()) {
            return false;
        }
        Iterator<IdentityItemModel> participantIterator = mCachedParticipants.iterator();
        Iterator<IdentityItemModel> otherParticipantIterator = other.mCachedParticipants.iterator();
        while (participantIterator.hasNext() && otherParticipantIterator.hasNext()) {
            if (!participantIterator.next().deepEquals(otherParticipantIterator.next())) {
                return false;
            }
        }

        return true;
    }
}
