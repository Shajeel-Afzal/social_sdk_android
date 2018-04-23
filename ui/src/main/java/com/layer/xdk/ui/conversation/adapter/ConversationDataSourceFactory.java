package com.layer.xdk.ui.conversation.adapter;


import android.arch.paging.DataSource;
import android.support.annotation.Nullable;

import com.layer.sdk.LayerClient;
import com.layer.sdk.query.Predicate;
import com.layer.sdk.query.SortDescriptor;
import com.layer.xdk.ui.message.model.MessageModelManager;

import javax.inject.Inject;

/**
 * Factory that handles creations of {@link ConversationDataSource}. This contains the variables
 * used to create new instances of the DataSource when old ones become invalid.
 */
public class ConversationDataSourceFactory implements DataSource.Factory<Integer, ConversationItemModel> {

    private LayerClient mLayerClient;
    private MessageModelManager mMessageModelManager;
    private Predicate mPredicate;
    private SortDescriptor mSortDescriptor;
    private int mInitialHistoricMessagesToFetch;

    /**
     * Creates a Factory
     *
     * @param layerClient client to use for the query
     * @param messageModelManager registry that handles model creation
     */
    @Inject
    public ConversationDataSourceFactory(LayerClient layerClient,
            MessageModelManager messageModelManager) {
        mLayerClient = layerClient;
        mMessageModelManager = messageModelManager;
    }

    /**
     * Create {@link ConversationDataSource} instances using the supplied query.
     *
     * @param predicate conversation predicate to use for the query
     * @param sortDescriptor conversation sorting to use for the query
     */
    public void setQuery(@Nullable Predicate predicate, @Nullable SortDescriptor sortDescriptor) {
        mPredicate = predicate;
        mSortDescriptor = sortDescriptor;
    }

    /**
     * Set the number of messages to fetch for each conversation. Up to this number of messages
     * are fetched if they have not yet been synced to the device.
     *
     * @param initialHistoricMessagesToFetch number of messages each conversation should fetch
     */
    public void setInitialHistoricMessagesToFetch(int initialHistoricMessagesToFetch) {
        mInitialHistoricMessagesToFetch = initialHistoricMessagesToFetch;
    }

    @Override
    public DataSource<Integer, ConversationItemModel> create() {
        return new ConversationDataSource(mLayerClient, mMessageModelManager,
                mPredicate, mSortDescriptor, mInitialHistoricMessagesToFetch);
    }
}
