package com.layer.xdk.ui.message.adapter;


import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.layer.sdk.messaging.Conversation;
import com.layer.xdk.ui.message.model.MessageModel;
import com.layer.xdk.ui.message.response.ResponseMessageModel;
import com.layer.xdk.ui.message.status.StatusMessageModel;

import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

public class GroupingCalculator {

    private static final long GROUP_TIME_THRESHOLD = TimeUnit.MINUTES.toMillis(30);

    @Inject
    public GroupingCalculator() {
    }

    @SuppressWarnings("WeakerAccess")
    public void calculateGrouping(List<MessageModel> models) {
        MessageModel previousModel = null;
        MessageModel currentModel;
        MessageModel nextModel;
        for (int i = 0; i < models.size(); i++) {
            currentModel = models.get(i);
            if (i == models.size() - 1) {
                nextModel = null;
            } else {
                nextModel = models.get(i + 1);
            }

            currentModel.setGrouping(calculateForCurrent(nextModel, currentModel, previousModel));
            previousModel = currentModel;
        }
    }

    @NonNull
    private EnumSet<MessageGrouping> calculateForCurrent(@Nullable MessageModel older,
            @NonNull MessageModel current, @Nullable MessageModel newer) {
        EnumSet<MessageGrouping> groupings = EnumSet.noneOf(MessageGrouping.class);
        // Status messages are part of their own sub group
        if (current instanceof ResponseMessageModel || current instanceof StatusMessageModel) {
            groupings.add(MessageGrouping.SUB_GROUP_START);
            groupings.add(MessageGrouping.SUB_GROUP_END);
        }

        if (older == null) {
            // Current should be the first message. Check if it is the first in the conversation
            if (current.getMessage().getConversation().getHistoricSyncStatus() == Conversation
                    .HistoricSyncStatus.NO_MORE_AVAILABLE) {
                groupings.add(MessageGrouping.OLDEST_MESSAGE);
            }
            groupings.add(MessageGrouping.GROUP_START);
            groupings.add(MessageGrouping.SUB_GROUP_START);
        } else {
            // Get time difference to older message
            Date currentReceivedAt = current.getMessage().getReceivedAt();
            Date olderReceivedAt = older.getMessage().getReceivedAt();
            long timeDiff = Math.abs(currentReceivedAt.getTime() - olderReceivedAt.getTime());
            if (timeDiff > GROUP_TIME_THRESHOLD) {
                groupings.add(MessageGrouping.GROUP_START);
                groupings.add(MessageGrouping.SUB_GROUP_START);
            }

            // Check for different sender of older message
            if ((current.getSenderId() != null && !current.getSenderId().equals(older.getSenderId()))
                    || (current.getSenderId() == null && older.getSenderId() != null)) {
                groupings.add(MessageGrouping.SUB_GROUP_START);
            }

            // Check if older message is a status message
            if (older instanceof  ResponseMessageModel || older instanceof StatusMessageModel) {
                groupings.add(MessageGrouping.SUB_GROUP_START);
            }
        }

        if (newer == null) {
            // Current should be the newest message
            groupings.add(MessageGrouping.SUB_GROUP_END);
            groupings.add(MessageGrouping.NEWEST_MESSAGE);
        } else {
            // Get time difference to newer message
            Date currentReceivedAt = current.getMessage().getReceivedAt();
            Date newerReceivedAt = newer.getMessage().getReceivedAt();
            long timeDiff = Math.abs(newerReceivedAt.getTime() - currentReceivedAt.getTime());
            if (timeDiff > GROUP_TIME_THRESHOLD) {
                groupings.add(MessageGrouping.SUB_GROUP_END);
            }

            // Check if newer message is a status message
            if (newer instanceof  ResponseMessageModel || newer instanceof StatusMessageModel) {
                groupings.add(MessageGrouping.SUB_GROUP_END);
            }

            // Check for different sender of newer message
            if ((current.getSenderId() != null && !current.getSenderId().equals(newer.getSenderId()))
                    || (current.getSenderId() == null && newer.getSenderId() != null)) {
                groupings.add(MessageGrouping.SUB_GROUP_END);
            } else if (!groupings.contains(MessageGrouping.SUB_GROUP_END)
                    && !groupings.contains(MessageGrouping.SUB_GROUP_START)) {
                // Same sender. It will only be the middle if it's not already the end
                groupings.add(MessageGrouping.SUB_GROUP_MIDDLE);
            }
        }

        return groupings;
    }
}
