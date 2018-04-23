package com.layer.xdk.ui.message.adapter;


import android.arch.paging.DataSource;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.layer.sdk.LayerClient;
import com.layer.sdk.messaging.Conversation;
import com.layer.sdk.query.Predicate;
import com.layer.xdk.ui.message.model.MessageModel;
import com.layer.xdk.ui.message.model.MessageModelManager;

import javax.inject.Inject;

/**
 * Factory that handles creations of {@link MessageModelDataSource}. This contains the variables
 * used to create new instances of the DataSource when old ones become invalid.
 */
public class MessageModelDataSourceFactory implements DataSource.Factory<Integer, MessageModel> {

    private final LayerClient mLayerClient;
    private final MessageModelManager mMessageModelManager;
    private final GroupingCalculator mGroupingCalculator;
    private Conversation mConversation;
    private Predicate mPredicate;

    /**
     * Creates a Factory
     *
     * @param layerClient client to use for the query
     * @param messageModelManager registry that handles model creation
     * @param groupingCalculator calculator for calculating {@link MessageGrouping} on the resulting
     *                           models
     */
    @Inject
    public MessageModelDataSourceFactory(@NonNull LayerClient layerClient,
            @NonNull MessageModelManager messageModelManager,
            @NonNull GroupingCalculator groupingCalculator) {
        mLayerClient = layerClient;
        mMessageModelManager = messageModelManager;
        mGroupingCalculator = groupingCalculator;
    }

    /**
     * Sets the conversation and predicate to use for data source generation
     *
     * @param conversation conversation to fetch the messages for
     * @param predicate custom predicate to use for the query or null if default should be used
     */
    public void setConversation(@NonNull Conversation conversation, @Nullable Predicate predicate) {
        mConversation = conversation;
        mPredicate = predicate;
    }

    @Override
    public DataSource<Integer, MessageModel> create() {
        return new MessageModelDataSource(mLayerClient, mConversation, mPredicate,
                mMessageModelManager, mGroupingCalculator);
    }
}
