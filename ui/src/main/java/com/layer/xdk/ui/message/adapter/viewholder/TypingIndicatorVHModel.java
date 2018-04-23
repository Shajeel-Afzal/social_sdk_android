package com.layer.xdk.ui.message.adapter.viewholder;


import android.content.Context;

import com.layer.sdk.LayerClient;
import com.layer.sdk.messaging.Identity;
import com.layer.xdk.ui.identity.IdentityFormatter;
import com.layer.xdk.ui.util.DateFormatter;
import com.layer.xdk.ui.message.image.cache.ImageCacheWrapper;

import java.util.Collections;
import java.util.Set;

import javax.inject.Inject;

public class TypingIndicatorVHModel extends MessageModelVHModel {

    private Set<Identity> mParticipants = Collections.emptySet();
    private boolean mAvatarViewVisible;
    private boolean mTypingIndicatorMessageVisible;
    private String mTypingIndicatorMessage;
    private ImageCacheWrapper mImageCacheWrapper;

    @Inject
    public TypingIndicatorVHModel(Context context, LayerClient layerClient,
            ImageCacheWrapper imageCacheWrapper, IdentityFormatter identityFormatter,
            DateFormatter dateFormatter) {
        super(context, layerClient, identityFormatter, dateFormatter);
        mImageCacheWrapper = imageCacheWrapper;
    }

    public Set<Identity> getParticipants() {
        return mParticipants;
    }

    public void setParticipants(Set<Identity> participants) {
        mParticipants = participants;
    }

    public boolean isAvatarViewVisible() {
        return mAvatarViewVisible;
    }

    @SuppressWarnings("WeakerAccess")
    public void setAvatarViewVisible(boolean avatarViewVisible) {
        mAvatarViewVisible = avatarViewVisible;
    }

    @SuppressWarnings("unused")
    public boolean isTypingIndicatorMessageVisible() {
        return mTypingIndicatorMessageVisible;
    }

    @SuppressWarnings("WeakerAccess")
    public void setTypingIndicatorMessageVisible(boolean typingIndicatorMessageVisible) {
        mTypingIndicatorMessageVisible = typingIndicatorMessageVisible;
    }

    public String getTypingIndicatorMessage() {
        return mTypingIndicatorMessage;
    }

    @SuppressWarnings("WeakerAccess")
    public void setTypingIndicatorMessage(String typingIndicatorMessage) {
        mTypingIndicatorMessage = typingIndicatorMessage;
    }

    public ImageCacheWrapper getImageCacheWrapper() {
        return mImageCacheWrapper;
    }
}
