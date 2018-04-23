package com.layer.xdk.ui.identity;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.paging.LivePagedListBuilder;
import android.arch.paging.PagedList;
import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.layer.sdk.messaging.Identity;
import com.layer.sdk.query.Predicate;
import com.layer.sdk.query.SortDescriptor;
import com.layer.xdk.ui.BR;
import com.layer.xdk.ui.identity.adapter.IdentityDataSourceFactory;
import com.layer.xdk.ui.identity.adapter.IdentityItemModel;
import com.layer.xdk.ui.identity.adapter.IdentityItemsAdapter;
import com.layer.xdk.ui.recyclerview.OnItemClickListener;

import java.util.List;

import javax.inject.Inject;

/**
 * A ViewModel to drive a list of {@link Identity} objects
 */
public class IdentityItemsListViewModel extends BaseObservable {
    private static final int DEFAULT_PAGE_SIZE = 50;
    private static final int DEFAULT_PREFETCH_DISTANCE = 150;

    private final IdentityItemsAdapter mAdapter;
    private final IdentityDataSourceFactory mDataSourceFactory;
    private LiveData<PagedList<IdentityItemModel>> mIdentityItemModelList;
    private Observer<PagedList<IdentityItemModel>> mIdentityItemModelListObserver;
    private boolean mInitialLoadComplete;

    @Inject
    public IdentityItemsListViewModel(@NonNull IdentityItemsAdapter adapter,
                                      @NonNull IdentityDataSourceFactory dataSourceFactory) {
        mAdapter = adapter;
        mDataSourceFactory = dataSourceFactory;
    }

    /**
     * Load a list of in memory identities into the adapter.
     *
     * @param identities identity list to populate the adapter with
     */
    public void useIdentities(List<Identity> identities) {
        mDataSourceFactory.useIdentities(identities);
        createAndObserveConversationItemModelList();
    }

    /**
     * Specify query arguments to use for the identity query when populating the adapter.
     *
     * @param predicate      An identity predicate to apply to the query
     * @param sortDescriptor An identity sort descriptor to apply to the query
     */
    @SuppressWarnings("unused")
    public void useQuery(Predicate predicate, SortDescriptor sortDescriptor) {
        mDataSourceFactory.useQuery(predicate, sortDescriptor);
        createAndObserveConversationItemModelList();
    }

    /**
     * Set an {@link OnItemClickListener} to be fired when items in the identity list are
     * clicked
     *
     * @param itemClickListener the {@link OnItemClickListener} to be used
     */
    public void setItemClickListener(OnItemClickListener<IdentityItemModel> itemClickListener) {
        mAdapter.setItemClickListener(itemClickListener);
        notifyChange();
    }

    /**
     * Gets the {@link IdentityItemsAdapter} instance set on this object
     *
     * @return the {@link IdentityItemsAdapter} instance set on this object
     */
    @Bindable
    public IdentityItemsAdapter getAdapter() {
        return mAdapter;
    }

    /**
     * Creates the {@link PagedList} and observes for changes so the adapter can be updated. If a
     * {@link PagedList} already exists then the observer will be removed before creating a new one.
     */
    private void createAndObserveConversationItemModelList() {
        // Remove observer if this is an update
        if (mIdentityItemModelList != null) {
            mIdentityItemModelList.removeObserver(mIdentityItemModelListObserver);
        }

        mIdentityItemModelList = new LivePagedListBuilder<>(mDataSourceFactory,
                new PagedList.Config.Builder()
                        .setEnablePlaceholders(false)
                        .setPageSize(DEFAULT_PAGE_SIZE)
                        .setPrefetchDistance(DEFAULT_PREFETCH_DISTANCE)
                        .build()
        ).build();

        mIdentityItemModelListObserver = new Observer<PagedList<IdentityItemModel>>() {
            @Override
            public void onChanged(@Nullable PagedList<IdentityItemModel> identities) {
                if (!mInitialLoadComplete) {
                    mInitialLoadComplete = true;
                    notifyPropertyChanged(BR.initialLoadComplete);
                }
                mAdapter.submitList(identities);
            }
        };
        mIdentityItemModelList.observeForever(mIdentityItemModelListObserver);
    }

    /**
     * @return true if the initial loading is complete
     */
    @Bindable
    public boolean isInitialLoadComplete() {
        return mInitialLoadComplete;
    }
}
