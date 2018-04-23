package com.layer.xdk.ui.conversation;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.layer.sdk.messaging.Conversation;
import com.layer.sdk.messaging.Identity;
import com.layer.xdk.ui.message.model.MessageModel;

import java.util.Set;

/**
 * A formatter for displaying conversation items in the UI
 */
public interface ConversationItemFormatter {
    /**
     * Set a title in the conversation metadata. Set a null or empty title to remove
     *
     * @param conversation upon which the title is to be set
     * @param title        title to be set
     */
    void setConversationMetadataTitle(@NonNull Conversation conversation, @Nullable String title);

    /**
     * Get the title that has been set in the conversation metadata
     *
     * @param conversation from which the title is to be fetched
     * @return title from conversation metadata. null if none exists
     */
    @Nullable
    String getConversationMetadataTitle(@NonNull Conversation conversation);

    /**
     * Get a title for the supplied conversation
     *
     * @param conversation to generate the title from
     * @param participants pre-fetched set of participants on the conversation
     * @return a title String for conversation
     */
    String getConversationTitle(@NonNull Conversation conversation,
            @NonNull Set<Identity> participants);

    /**
     * Get a title for the supplied conversation
     *
     * @param conversation to generate the title from
     * @return a title String for conversation
     */
    String getConversationTitle(@NonNull Conversation conversation);

    /**
     * Get a timestamp to display for the conversation
     *
     * @param conversation from which a timestamp is to be generated
     * @return a timestamp string suitable for display
     */
    @NonNull
    String getTimeStamp(@NonNull Conversation conversation);

    /**
     * Generate a suitable preview for this conversation
     *
     * @param conversation from which the preview is to be generated
     * @param lastMessageModel model for the last message in the conversation. Null if there is no
     *                         last message.
     * @return a string suitable to display as a preview for this conversation
     */
    @NonNull
    String getLastMessagePreview(@NonNull Conversation conversation,
            @Nullable MessageModel lastMessageModel);
}
