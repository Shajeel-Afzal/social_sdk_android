package com.layer.xdk.ui.message;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.paging.LivePagedListBuilder;
import android.arch.paging.PagedList;
import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.layer.sdk.LayerClient;
import com.layer.sdk.messaging.Conversation;
import com.layer.sdk.query.Predicate;
import com.layer.xdk.ui.BR;
import com.layer.xdk.ui.identity.IdentityFormatter;
import com.layer.xdk.ui.message.action.ActionHandlerRegistry;
import com.layer.xdk.ui.message.action.GoogleMapsOpenMapActionHandler;
import com.layer.xdk.ui.message.action.OpenFileActionHandler;
import com.layer.xdk.ui.message.action.OpenUrlActionHandler;
import com.layer.xdk.ui.message.adapter.MessageModelAdapter;
import com.layer.xdk.ui.message.adapter.MessageModelDataSourceFactory;
import com.layer.xdk.ui.message.image.cache.ImageCacheWrapper;
import com.layer.xdk.ui.message.model.MessageModel;
import com.layer.xdk.ui.recyclerview.OnItemLongClickListener;

import javax.inject.Inject;

/**
 * A ViewModel to drive a list of {@link com.layer.sdk.messaging.Message} objects
 */
public class MessageItemsListViewModel extends BaseObservable {
    private static final int DEFAULT_PAGE_SIZE = 30;
    private static final int DEFAULT_PREFETCH_DISTANCE = 60;

    private MessageModelAdapter mAdapter;
    private Conversation mConversation;
    private Predicate mQueryPredicate;
    private LiveData<PagedList<MessageModel>> mMessageModelList;
    private Observer<PagedList<MessageModel>> mMessageModelListObserver;
    private MessageModelDataSourceFactory mDataSourceFactory;
    private IdentityFormatter mIdentityFormatter;
    private boolean mInitialLoadComplete;

    @Inject
    public MessageItemsListViewModel(@NonNull LayerClient layerClient,
            @NonNull MessageModelAdapter messageModelAdapter,
            @NonNull MessageModelDataSourceFactory dataSourceFactory,
            @NonNull IdentityFormatter identityFormatter,
            @NonNull ImageCacheWrapper imageCacheWrapper) {
        mAdapter = messageModelAdapter;
        mDataSourceFactory = dataSourceFactory;
        mIdentityFormatter = identityFormatter;

        ActionHandlerRegistry.registerHandler(new OpenUrlActionHandler(layerClient, imageCacheWrapper));
        ActionHandlerRegistry.registerHandler(new GoogleMapsOpenMapActionHandler(layerClient));
        ActionHandlerRegistry.registerHandler(new OpenFileActionHandler(layerClient));
    }

    /**
     * Sets the {@link Conversation} from which {@link com.layer.sdk.messaging.Message}s are
     * displayed
     *
     * @param conversation a {@link Conversation} instance to drive this list
     */
    public void setConversation(Conversation conversation) {
        mConversation = conversation;
        if (conversation != null) {
            mAdapter.setOneOnOneConversation(conversation.getParticipants().size() == 2);
            mAdapter.setReadReceiptsEnabled(conversation.isReadReceiptsEnabled());
            createAndObserveMessageModelList();
        }
        notifyChange();
    }

    /**
     * Gets the {@link MessageModelAdapter} instance set on this object
     *
     * @return the {@link MessageModelAdapter} instance set on this object
     */
    @Bindable
    public MessageModelAdapter getAdapter() {
        return mAdapter;
    }

    @Bindable
    public IdentityFormatter getIdentityFormatter() {
        return mIdentityFormatter;
    }

    /**
     * Set an {@link OnItemLongClickListener} to be fired when items in the conversation list are
     * long clicked
     *
     * @param listener the {@link OnItemLongClickListener} instance to be used
     */
    public void setItemLongClickListener(OnItemLongClickListener<MessageModel> listener) {
        mAdapter.setItemLongClickListener(listener);
    }

    /**
     * Set a custom predicate to use during the query for messages instead of the default.
     *
     * @param queryPredicate predicate to use for message query
     */
    @SuppressWarnings("unused")
    public void setQueryPredicate(@Nullable Predicate queryPredicate) {
        mQueryPredicate = queryPredicate;
        // Only re-create the list if the conversation has already been set. Else just rely on the
        // initial creation to happen when the conversation is set.
        if (mConversation != null) {
            createAndObserveMessageModelList();
            notifyChange();
        }
    }

    /**
     * Creates the {@link PagedList} and observes for changes so the adapter can be updated. If a
     * {@link PagedList} already exists then the observer will be removed before creating a new one.
     */
    private void createAndObserveMessageModelList() {
        // Remove observer if this is an update
        if (mMessageModelList != null) {
            mMessageModelList.removeObserver(mMessageModelListObserver);
        }
        mDataSourceFactory.setConversation(mConversation, mQueryPredicate);

        mMessageModelList = new LivePagedListBuilder<>(mDataSourceFactory,
                new PagedList.Config.Builder()
                        .setEnablePlaceholders(false)
                        .setPageSize(DEFAULT_PAGE_SIZE)
                        .setPrefetchDistance(DEFAULT_PREFETCH_DISTANCE)
                        .build()
        ).build();

        mMessageModelListObserver = new Observer<PagedList<MessageModel>>() {
            @Override
            public void onChanged(@Nullable PagedList<MessageModel> messages) {
                if (!mInitialLoadComplete) {
                    mInitialLoadComplete = true;
                    notifyPropertyChanged(BR.initialLoadComplete);
                }
                mAdapter.submitList(messages);
            }
        };
        mMessageModelList.observeForever(mMessageModelListObserver);
    }

    /**
     * @return true if the initial loading is complete
     */
    @Bindable
    public boolean isInitialLoadComplete() {
        return mInitialLoadComplete;
    }
}
