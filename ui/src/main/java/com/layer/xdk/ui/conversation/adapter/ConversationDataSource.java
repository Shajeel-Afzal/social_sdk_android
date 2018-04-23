package com.layer.xdk.ui.conversation.adapter;


import android.arch.paging.PositionalDataSource;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.layer.sdk.LayerClient;
import com.layer.sdk.changes.LayerChange;
import com.layer.sdk.changes.LayerChangeEvent;
import com.layer.sdk.listeners.LayerChangeEventListener;
import com.layer.sdk.messaging.Conversation;
import com.layer.sdk.messaging.Identity;
import com.layer.sdk.messaging.Message;
import com.layer.sdk.query.Predicate;
import com.layer.sdk.query.Query;
import com.layer.sdk.query.SortDescriptor;
import com.layer.xdk.ui.message.model.MessageModel;
import com.layer.xdk.ui.message.model.MessageModelManager;
import com.layer.xdk.ui.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Creates a {@link android.arch.paging.DataSource} to use with the paging library that loads
 * Conversations from a {@link LayerClient}. This will convert them to {@link ConversationItemModel}
 * objects before returning, performing some additional loads into memory to speed up item binding
 * with a recycler view.
 */
public class ConversationDataSource extends PositionalDataSource<ConversationItemModel> {

    private static final Predicate DEFAULT_PREDICATE = new Predicate(
            Conversation.Property.PARTICIPANT_COUNT, Predicate.Operator.GREATER_THAN, 1);
    private static final SortDescriptor DEFAULT_SORT_DESCRIPTOR = new SortDescriptor(
            Conversation.Property.LAST_MESSAGE_RECEIVED_AT, SortDescriptor.Order.DESCENDING);

    private final LayerClient mLayerClient;
    private final MessageModelManager mMessageModelManager;
    private final Predicate mPredicate;
    private final SortDescriptor mSortDescriptor;
    private final LayerChangeEventListener.BackgroundThread.Weak mListener;
    private final Identity mAuthenticatedUser;
    private int mInitialHistory = 0;

    /**
     * Create a {@link android.arch.paging.DataSource} and registers a listener with the
     * {@link LayerClient} to listen for relevant change notifications to invalidate if necessary.
     *
     * @param layerClient client to use for the query
     * @param messageModelManager message model manager that handles model creation
     * @param predicate custom predicate to use for the query, null to use the default predicate
     * @param sortDescriptor sort rules to use for the query, null to use the default sorting
     * @param initialHistory number of messages to fetch for the initial history
     */
    @SuppressWarnings("WeakerAccess")
    public ConversationDataSource(@NonNull LayerClient layerClient,
            @NonNull MessageModelManager messageModelManager,
            @Nullable Predicate predicate,
            @Nullable SortDescriptor sortDescriptor,
            int initialHistory) {
        mLayerClient = layerClient;
        mAuthenticatedUser = mLayerClient.getAuthenticatedUser();
        mMessageModelManager = messageModelManager;
        if (predicate == null) {
            mPredicate = DEFAULT_PREDICATE;
        } else {
            mPredicate = predicate;
        }
        if (sortDescriptor == null) {
            mSortDescriptor = DEFAULT_SORT_DESCRIPTOR;
        } else {
            mSortDescriptor = sortDescriptor;
        }
        mInitialHistory = initialHistory;

        mListener = new LayerChangeEventListener.BackgroundThread.Weak() {
            @Override
            public void onChangeEvent(LayerChangeEvent layerChangeEvent) {
                List<LayerChange> changes = layerChangeEvent.getChanges();
                boolean needsInvalidation = false;
                for (LayerChange change : changes) {
                    switch (change.getObjectType()) {
                        case CONVERSATION:
                            needsInvalidation = true;
                            break;
                        case IDENTITY:
                            needsInvalidation = true;
                            break;
                    }

                    if (needsInvalidation) {
                        // Unregister this listener, invalidate the data source and return so no
                        // more changes are processed
                        mLayerClient.unregisterEventListener(mListener);
                        if (Log.isLoggable(Log.VERBOSE)) {
                            Log.d("Invalidating " + ConversationDataSource.class.getSimpleName()
                                    + " due to change");
                        }
                        invalidate();
                        return;
                    }
                }

            }
        };

        mLayerClient.registerEventListener(mListener);
    }

    @Override
    public void loadInitial(@NonNull LoadInitialParams params,
            @NonNull LoadInitialCallback<ConversationItemModel> callback) {
        int count = (int) computeCount();
        if (count == 0) {
            callback.onResult(Collections.<ConversationItemModel>emptyList(), 0, 0);
        } else {
            int position = computeInitialLoadPosition(params, count);
            int size = computeInitialLoadSize(params, position, count);

            List<Conversation> conversations = loadRangeInternal(position, size);
            syncInitialMessages(conversations);
            if (conversations.size() == size) {
                callback.onResult(convertConversationsToItemModels(conversations), position, count);
            } else {
                invalidate();
            }
        }
    }

    @Override
    public void loadRange(@NonNull LoadRangeParams params,
            @NonNull LoadRangeCallback<ConversationItemModel> callback) {
        List<Conversation> conversations = loadRangeInternal(params.startPosition, params.loadSize);
        syncInitialMessages(conversations);
        callback.onResult(convertConversationsToItemModels(conversations));
    }

    private long computeCount() {
        Long count = mLayerClient.executeQueryForCount(Query.builder(Conversation.class)
                .predicate(mPredicate)
                .build());
        if (count == null) {
            return 0L;
        }
        return count;
    }

    @NonNull
    @SuppressWarnings("unchecked")
    private List<Conversation> loadRangeInternal(int position, int requestedLoadSize) {
        return (List<Conversation>) mLayerClient.executeQueryForObjects(Query.builder(
                Conversation.class)
                .predicate(mPredicate)
                .sortDescriptor(mSortDescriptor)
                .offset(position)
                .limit(requestedLoadSize)
                .build());
    }

    @NonNull
    private List<ConversationItemModel> convertConversationsToItemModels(
            @NonNull List<Conversation> conversations) {
        List<ConversationItemModel> itemModels = new ArrayList<>(conversations.size());
        for (Conversation conversation : conversations) {
            MessageModel lastMessageModel = null;
            Message lastMessage = conversation.getLastMessage();
            if (lastMessage != null) {
                lastMessageModel = mMessageModelManager.getNewModel(lastMessage);
                lastMessageModel.processPartsFromTreeRoot();
            }
            itemModels.add(new ConversationItemModel(conversation, lastMessageModel,
                    mAuthenticatedUser));
        }
        return itemModels;
    }

    private void syncInitialMessages(@NonNull List<Conversation> conversations) {
        if (mInitialHistory <= 0) return;
        for (Conversation conversation : conversations) {
            if (conversation == null || conversation.getHistoricSyncStatus() != Conversation
                    .HistoricSyncStatus.MORE_AVAILABLE) {
                continue;
            }
            Query<Message> localCountQuery = Query.builder(Message.class)
                    .predicate(new Predicate(Message.Property.CONVERSATION, Predicate.Operator.EQUAL_TO, conversation))
                    .build();
            long delta = mInitialHistory - mLayerClient.executeQueryForCount(localCountQuery);
            if (delta > 0) conversation.syncMoreHistoricMessages((int) delta);
        }
    }
}
