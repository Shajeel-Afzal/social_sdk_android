package com.layer.xdk.ui.mock;

import android.net.Uri;
import android.support.annotation.Nullable;

import com.layer.sdk.LayerClient;
import com.layer.sdk.messaging.Conversation;
import com.layer.sdk.messaging.Identity;
import com.layer.sdk.messaging.Message;
import com.layer.sdk.messaging.MessageOptions;
import com.layer.sdk.messaging.MessagePart;

import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class MockMessageImpl implements Message {

    private Set<MessagePart> mMessageParts = new HashSet<>();
    private Date mReceivedAtDate;
    private Conversation mConversation;


    public MockMessageImpl(Set<MessagePart> messageParts) {
        mMessageParts = messageParts;
        mReceivedAtDate = new Date();
    }

    public MockMessageImpl(MessagePart... messageParts) {
        mMessageParts = new HashSet<>();
        Collections.addAll(mMessageParts, messageParts);
        mReceivedAtDate = new Date();
    }

    @Override
    public void delete(LayerClient.DeletionMode deletionMode) {

    }

    @Override
    public void markAsRead() {

    }

    @Override
    public Uri getId() {
        return null;
    }

    @Override
    public long getPosition() {
        return 0;
    }

    @Override
    public Conversation getConversation() {
        return mConversation;
    }

    @Override
    public Set<MessagePart> getMessageParts() {
        return mMessageParts;
    }

    @Override
    public boolean isSent() {
        return true;
    }

    @Override
    public boolean isDeleted() {
        return false;
    }

    @Override
    public Date getSentAt() {
        return null;
    }

    @Override
    public Date getReceivedAt() {
        return mReceivedAtDate;
    }

    @Nullable
    @Override
    public Identity getSender() {
        return null;
    }

    @Override
    public Map<Identity, RecipientStatus> getRecipientStatus() {
        return null;
    }

    @Override
    public RecipientStatus getRecipientStatus(Identity identity) {
        return null;
    }

    @Override
    public MessageOptions getOptions() {
        return null;
    }

    @Nullable
    @Override
    public Date getUpdatedAt() {
        return null;
    }

    public void setConversation(Conversation conversation) {
        mConversation = conversation;
    }

    @Override
    public void putLocalData(@Nullable byte[] bytes) {

    }

    @Nullable
    @Override
    public byte[] getLocalData() {
        return null;
    }
}
