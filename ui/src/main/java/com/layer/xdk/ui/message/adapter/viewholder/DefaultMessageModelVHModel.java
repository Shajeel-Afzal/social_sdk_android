package com.layer.xdk.ui.message.adapter.viewholder;

import android.content.Context;
import android.databinding.Bindable;
import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.StyleSpan;

import com.layer.sdk.LayerClient;
import com.layer.sdk.messaging.Conversation;
import com.layer.sdk.messaging.Identity;
import com.layer.sdk.messaging.Message;
import com.layer.xdk.ui.R;
import com.layer.xdk.ui.identity.IdentityFormatter;
import com.layer.xdk.ui.message.adapter.MessageGrouping;
import com.layer.xdk.ui.message.model.MessageModel;
import com.layer.xdk.ui.util.DateFormatter;
import com.layer.xdk.ui.message.image.cache.ImageCacheWrapper;

import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

public class DefaultMessageModelVHModel extends MessageModelVHModel {

    // Config
    private boolean mEnableReadReceipts;
    private boolean mShowAvatars;
    private boolean mShowPresence;
    private ImageCacheWrapper mImageCacheWrapper;

    // View related variables
    private boolean mShouldShowDisplayName;
    private boolean mShouldShowDateTimeForMessage;
    private float mMessageCellAlpha;
    private String mSenderName;
    private Set<Identity> mSender = Collections.emptySet();
    private String mReadReceipt;
    private CharSequence mDateTime;
    private boolean mIsReadReceiptVisible;
    private boolean mShouldDisplayAvatarSpace;
    private boolean mIsAvatarViewVisible;
    private boolean mIsPresenceVisible;
    private boolean mShouldShowAvatarForCurrentUser;
    private boolean mShouldCurrentUserAvatarBeVisible;
    private boolean mShouldCurrentUserPresenceBeVisible;
    private boolean mShouldShowPresenceForCurrentUser;

    @Inject
    public DefaultMessageModelVHModel(Context context,
            LayerClient layerClient,
            ImageCacheWrapper imageCacheWrapper,
            IdentityFormatter identityFormatter,
            DateFormatter dateFormatter) {
        super(context, layerClient, identityFormatter, dateFormatter);
        mEnableReadReceipts = true;
        mShowAvatars = true;
        mShowPresence = true;
        mImageCacheWrapper = imageCacheWrapper;
    }

    public void update() {
        Message message = getItem().getMessage();
        mSender = Collections.singleton(message.getSender());

        updateAvatar();
        updateReceivedAtDateAndTime();
        updateRecipientStatus();

        // Sender-dependent elements
        updateSenderDependentElements(getItem());
        notifyChange();
    }

    @SuppressWarnings("WeakerAccess")
    protected void updateAvatar() {
        EnumSet<MessageGrouping> grouping = getItem().getGrouping();
        if (grouping == null) {
            // This should never get hit since we're usually dealing with root models here
            mShouldCurrentUserAvatarBeVisible = false;
            mShouldCurrentUserPresenceBeVisible = false;
            return;
        }

        mShouldCurrentUserAvatarBeVisible = isMyMessage() && mShouldShowAvatarForCurrentUser &&
                grouping.contains(MessageGrouping.SUB_GROUP_END);
        mShouldCurrentUserPresenceBeVisible =
                mShouldCurrentUserAvatarBeVisible && mShouldShowPresenceForCurrentUser;
    }

    @SuppressWarnings("WeakerAccess")
    protected void updateReceivedAtDateAndTime() {
        EnumSet<MessageGrouping> grouping = getItem().getGrouping();
        if (grouping == null) {
            // This should never get hit since we're usually dealing with root models here
            mShouldShowDateTimeForMessage = false;
            return;
        }

        if (grouping.contains(MessageGrouping.OLDEST_MESSAGE)) {
            Conversation conversation = getItem().getMessage().getConversation();
            Date createdAt = conversation.getCreatedAt();
            if (createdAt == null) {
                createdAt = new Date();
            }

            String conversationStartTime = getDateFormatter().formatTime(createdAt);
            String conversationStartFormattedDate = getDateFormatter().formatTimeDay(createdAt);
            mDateTime = getContext().getString(R.string.xdk_ui_messages_list_header_conversation_start_time,
                        conversationStartFormattedDate, conversationStartTime);
            mShouldShowDateTimeForMessage = true;

        } else if (grouping.contains(MessageGrouping.GROUP_START)) {
            Date receivedAt = getItem().getMessage().getReceivedAt();
            if (receivedAt == null) receivedAt = new Date();

            String day = getDateFormatter().formatTimeDay(receivedAt);
            String time = getDateFormatter().formatTime(receivedAt);
            SpannableString dateTime = new SpannableString(String.format("%s %s", day, time));
            dateTime.setSpan(new StyleSpan(Typeface.BOLD), 0, day.length(),
                    Spanned.SPAN_INCLUSIVE_EXCLUSIVE);

            mDateTime = dateTime;
            mShouldShowDateTimeForMessage = true;
        } else {
            mShouldShowDateTimeForMessage = false;
        }
    }

    @SuppressWarnings("WeakerAccess")
    protected void updateSenderDependentElements(MessageModel model) {
        Message message = model.getMessage();
        EnumSet<MessageGrouping> grouping = model.getGrouping();
        if (isMyMessage()) {
            mMessageCellAlpha = message.isSent() ? 1.0f : 0.5f;
            mShouldShowDisplayName = false;
        } else {
            mMessageCellAlpha = 1.0f;

            if (mEnableReadReceipts) {
                message.markAsRead();
            }

            // Sender name, only for first message in sub group
            if (!isInAOneOnOneConversation() && grouping != null &&
                    grouping.contains(MessageGrouping.SUB_GROUP_START)) {
                Identity sender = model.getMessage().getSender();
                if (sender != null) {
                    mSenderName = getIdentityFormatter().getDisplayName(sender);
                } else {
                    mSenderName = getIdentityFormatter().getUnknownNameString();
                }
                mShouldShowDisplayName = true;
            } else {
                mShouldShowDisplayName = false;
            }
        }

        // Avatars
        if (isInAOneOnOneConversation()) {
            if (mShowAvatars) {
                mIsAvatarViewVisible = !isMyMessage();
                mShouldDisplayAvatarSpace = true;
                mIsPresenceVisible = mIsAvatarViewVisible && mShowPresence;
            } else {
                mIsAvatarViewVisible = false;
                mShouldDisplayAvatarSpace = false;
                mIsPresenceVisible = false;
            }
        } else if (grouping != null && grouping.contains(MessageGrouping.SUB_GROUP_END)) {
            // Last message in sub group
            mIsAvatarViewVisible = !isMyMessage();
            mShouldDisplayAvatarSpace = true;
            mIsPresenceVisible = mIsAvatarViewVisible && mShowPresence;
        } else {
            // Invisible for grouped messages to preserve proper spacing
            mIsAvatarViewVisible = false;
            mShouldDisplayAvatarSpace = true;
            mIsPresenceVisible = false;
        }
    }

    @SuppressWarnings("WeakerAccess")
    protected void updateRecipientStatus() {
        if (getItem().isMyNewestMessage()) {
            int readCount = 0;
            boolean delivered = false;
            Map<Identity, Message.RecipientStatus> statuses = getItem().getMessage().getRecipientStatus();
            for (Map.Entry<Identity, Message.RecipientStatus> entry : statuses.entrySet()) {
                // Only show receipts for other members
                if (entry.getKey().equals(getLayerClient().getAuthenticatedUser())) continue;
                // Skip receipts for members no longer in the conversation
                if (entry.getValue() == null) continue;

                switch (entry.getValue()) {
                    case READ:
                        readCount++;
                        break;
                    case DELIVERED:
                        delivered = true;
                        break;
                }
            }

            if (readCount > 0) {
                mIsReadReceiptVisible = true;

                // Use 2 to include one other participant plus the current user
                if (statuses.size() > 2) {
                    mReadReceipt = getContext().getResources()
                            .getQuantityString(R.plurals.xdk_ui_message_status_read_multiple_participants, readCount, readCount);
                } else {
                    mReadReceipt = getContext().getString(R.string.xdk_ui_message_status_read);
                }
            } else if (delivered) {
                mIsReadReceiptVisible = true;
                mReadReceipt = getContext().getString(R.string.xdk_ui_message_status_delivered);
            } else {
                mIsReadReceiptVisible = false;
            }
        } else {
            mIsReadReceiptVisible = false;
        }
    }

    @SuppressWarnings("WeakerAccess")
    protected boolean isInAOneOnOneConversation() {
        return getItem().getParticipantCount() == 2;
    }

    // Setters

    public void setShowAvatars(boolean showAvatars) {
        mShowAvatars = showAvatars;
    }

    public void setShouldShowAvatarForCurrentUser(boolean shouldShowAvatarForCurrentUser) {
        mShouldShowAvatarForCurrentUser = shouldShowAvatarForCurrentUser;
    }

    public void setShowPresence(boolean showPresence) {
        mShowPresence = showPresence;
    }

    public void setEnableReadReceipts(boolean enableReadReceipts) {
        mEnableReadReceipts = enableReadReceipts;
    }

    public void setShouldShowPresenceForCurrentUser(boolean shouldShowPresenceForCurrentUser) {
        mShouldShowPresenceForCurrentUser = shouldShowPresenceForCurrentUser;
    }

    // Getters

    public ImageCacheWrapper getImageCacheWrapper() {
        return mImageCacheWrapper;
    }

    // Bindable properties

    @Bindable
    public boolean getAvatarVisibility() {
        return mIsAvatarViewVisible;
    }

    @Bindable
    public boolean isPresenceVisible() {
        return mIsPresenceVisible;
    }

    @Bindable
    public Set<Identity> getParticipants() {
        return mSender;
    }

    @Bindable
    public boolean getShouldShowDateTimeForMessage() {
        return mShouldShowDateTimeForMessage;
    }

    @Bindable
    public String getReadReceipt() {
        return mReadReceipt;
    }

    @Bindable
    public float getMessageCellAlpha() {
        return mMessageCellAlpha;
    }

    @Bindable
    public String getSenderName() {
        return mSenderName;
    }

    @Bindable
    public boolean getShouldShowDisplayName() {
        return mShouldShowDisplayName;
    }

    @Bindable
    public CharSequence getDateTime() {
        return mDateTime;
    }

    @Bindable
    public boolean isReadReceiptVisible() {
        return mIsReadReceiptVisible;
    }

    @Bindable
    @SuppressWarnings("WeakerAccess")
    public boolean isMyMessage() {
        return getItem() != null && getItem().isMessageFromMe();
    }

    @Bindable
    public boolean getShouldDisplayAvatarSpace() {
        return mShouldDisplayAvatarSpace;
    }

    @Bindable
    public boolean getShouldShowAvatarForCurrentUser() {
        return mShouldShowAvatarForCurrentUser;
    }

    @Bindable
    public boolean getShouldCurrentUserAvatarBeVisible() {
        return mShouldCurrentUserAvatarBeVisible;
    }

    @Bindable
    public boolean getShouldCurrentUserPresenceBeVisible() {
        return mShouldCurrentUserPresenceBeVisible;
    }
}
