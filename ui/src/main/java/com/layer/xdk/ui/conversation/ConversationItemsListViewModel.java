package com.layer.xdk.ui.conversation;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.paging.LivePagedListBuilder;
import android.arch.paging.PagedList;
import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.layer.sdk.query.Predicate;
import com.layer.sdk.query.SortDescriptor;
import com.layer.xdk.ui.BR;
import com.layer.xdk.ui.conversation.adapter.ConversationDataSourceFactory;
import com.layer.xdk.ui.conversation.adapter.ConversationItemModel;
import com.layer.xdk.ui.conversation.adapter.ConversationItemsAdapter;
import com.layer.xdk.ui.recyclerview.OnItemClickListener;
import com.layer.xdk.ui.recyclerview.OnItemLongClickListener;

import javax.inject.Inject;

/**
 * A ViewModel to drive a list of {@link com.layer.sdk.messaging.Conversation} objects
 */
public class ConversationItemsListViewModel extends BaseObservable {
    private static final int DEFAULT_PAGE_SIZE = 50;
    private static final int DEFAULT_PREFETCH_DISTANCE = 150;

    private final static int INITIAL_HISTORIC_MESSAGES_TO_SYNC = 20;

    private final ConversationItemsAdapter mAdapter;
    private final ConversationDataSourceFactory mDataSourceFactory;
    private LiveData<PagedList<ConversationItemModel>> mConversationItemModelList;
    private Observer<PagedList<ConversationItemModel>> mConversationItemModelListObserver;
    private Predicate mQueryPredicate;
    private SortDescriptor mSortDescriptor;
    private boolean mInitialLoadComplete;

    @Inject
    public ConversationItemsListViewModel(@NonNull ConversationItemsAdapter adapter,
                                          @NonNull ConversationDataSourceFactory dataSourceFactory) {
        mAdapter = adapter;
        mDataSourceFactory = dataSourceFactory;
        setInitialHistoricMessagesToFetch(INITIAL_HISTORIC_MESSAGES_TO_SYNC);
    }

    /**
     * Use the default query for the conversation query. This only shows conversations we're still
     * a member of and are sorted by the last message's receivedAt time
     */
    public void useDefaultQuery() {
        useQuery(null, null);
    }

    /**
     * Specify query arguments to use for the conversation query.
     *
     * @param predicate      A conversation predicate to apply to the query
     * @param sortDescriptor A conversation sort descriptor to apply to the query
     */
    @SuppressWarnings({"SameParameterValue", "WeakerAccess"})
    public void useQuery(@Nullable Predicate predicate, @Nullable SortDescriptor sortDescriptor) {
        mQueryPredicate = predicate;
        mSortDescriptor = sortDescriptor;
        createAndObserveConversationItemModelList();
    }

    /**
     * Sets number of messages to fetch for the initial history
     *
     * @param numberOfMessagesToFetch number of messages to fetch for the initial history
     */
    @SuppressWarnings("WeakerAccess")
    public void setInitialHistoricMessagesToFetch(int numberOfMessagesToFetch) {
        mDataSourceFactory.setInitialHistoricMessagesToFetch(numberOfMessagesToFetch);
    }

    /**
     * Gets the {@link ConversationItemsAdapter} instance set on this object
     *
     * @return the {@link ConversationItemsAdapter} instance set on this object
     */
    @NonNull
    @Bindable
    public ConversationItemsAdapter getConversationItemsAdapter() {
        return mAdapter;
    }

    /**
     * Set an {@link OnItemClickListener} to be fired when items in the conversation list are
     * clicked
     *
     * @param listener the {@link OnItemClickListener} to be used
     */
    public void setItemClickListener(OnItemClickListener<ConversationItemModel> listener) {
        mAdapter.setItemClickListener(listener);
    }

    /**
     * Set an {@link OnItemLongClickListener} to be fired when items in the conversation list are
     * long clicked
     *
     * @param listener the {@link OnItemLongClickListener} instance to be used
     */
    public void setItemLongClickListener(OnItemLongClickListener<ConversationItemModel> listener) {
        mAdapter.setItemLongClickListener(listener);
    }

    /**
     * Creates the {@link PagedList} and observes for changes so the adapter can be updated. If a
     * {@link PagedList} already exists then the observer will be removed before creating a new one.
     */
    private void createAndObserveConversationItemModelList() {
        // Remove observer if this is an update
        if (mConversationItemModelList != null) {
            mConversationItemModelList.removeObserver(mConversationItemModelListObserver);
        }
        mDataSourceFactory.setQuery(mQueryPredicate, mSortDescriptor);

        mConversationItemModelList = new LivePagedListBuilder<>(mDataSourceFactory,
                new PagedList.Config.Builder()
                        .setEnablePlaceholders(false)
                        .setPageSize(DEFAULT_PAGE_SIZE)
                        .setPrefetchDistance(DEFAULT_PREFETCH_DISTANCE)
                        .build()
        ).build();

        mConversationItemModelListObserver = new Observer<PagedList<ConversationItemModel>>() {
            @Override
            public void onChanged(@Nullable PagedList<ConversationItemModel> conversations) {
                if (!mInitialLoadComplete) {
                    mInitialLoadComplete = true;
                    notifyPropertyChanged(BR.initialLoadComplete);
                }
                mAdapter.submitList(conversations);
            }
        };
        mConversationItemModelList.observeForever(mConversationItemModelListObserver);
    }

    /**
     * @return true if the initial loading is complete
     */
    @Bindable
    public boolean isInitialLoadComplete() {
        return mInitialLoadComplete;
    }
}
