package com.layer.xdk.ui.identity.adapter;


import android.arch.paging.PositionalDataSource;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.layer.sdk.LayerClient;
import com.layer.sdk.changes.LayerChange;
import com.layer.sdk.changes.LayerChangeEvent;
import com.layer.sdk.listeners.LayerChangeEventListener;
import com.layer.sdk.messaging.Identity;
import com.layer.sdk.query.Predicate;
import com.layer.sdk.query.Query;
import com.layer.sdk.query.SortDescriptor;
import com.layer.xdk.ui.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Creates a {@link android.arch.paging.DataSource} to use with the paging library that loads
 * Identities from a {@link LayerClient}. This will convert them to {@link IdentityItemModel}
 * objects before returning.
 */
public class IdentityDataSource extends PositionalDataSource<IdentityItemModel> {

    private static final Predicate DEFAULT_PREDICATE = new Predicate(
            Identity.Property.FOLLOWED, Predicate.Operator.EQUAL_TO, true);
    private static final SortDescriptor DEFAULT_SORT_DESCRIPTOR = new SortDescriptor(
            Identity.Property.DISPLAY_NAME, SortDescriptor.Order.DESCENDING);

    private final LayerClient mLayerClient;
    private final Predicate mPredicate;
    private final SortDescriptor mSortDescriptor;
    private final LayerChangeEventListener.BackgroundThread.Weak mListener;

    /**
     * Create a {@link android.arch.paging.DataSource} and registers a listener with the
     * {@link LayerClient} to listen for relevant change notifications to invalidate if necessary.
     *
     * @param layerClient client to use for the query
     * @param predicate custom predicate to use for the query, null to use the default predicate
     * @param sortDescriptor sort rules to use for the query, null to use the default sorting
     */
    @SuppressWarnings("WeakerAccess")
    public IdentityDataSource(@NonNull LayerClient layerClient,
            @Nullable Predicate predicate,
            @Nullable SortDescriptor sortDescriptor) {
        mLayerClient = layerClient;
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

        mListener = new LayerChangeEventListener.BackgroundThread.Weak() {
            @Override
            public void onChangeEvent(LayerChangeEvent layerChangeEvent) {
                List<LayerChange> changes = layerChangeEvent.getChanges();
                boolean needsInvalidation = false;
                for (LayerChange change : changes) {
                    switch (change.getObjectType()) {
                        case IDENTITY:
                            needsInvalidation = true;
                            break;
                    }

                    if (needsInvalidation) {
                        // Unregister this listener, invalidate the data source and return so no
                        // more changes are processed
                        mLayerClient.unregisterEventListener(mListener);
                        if (Log.isLoggable(Log.VERBOSE)) {
                            Log.d("Invalidating " + IdentityDataSource.class.getSimpleName()
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
            @NonNull LoadInitialCallback<IdentityItemModel> callback) {
        int count = (int) computeCount();
        if (count == 0) {
            callback.onResult(Collections.<IdentityItemModel>emptyList(), 0, 0);
        } else {
            int position = computeInitialLoadPosition(params, count);
            int size = computeInitialLoadSize(params, position, count);

            List<Identity> identities = loadRangeInternal(position, size);
            if (identities.size() == size) {
                callback.onResult(convertIdentitiesToItemModels(identities), position, count);
            } else {
                invalidate();
            }
        }
    }

    @Override
    public void loadRange(@NonNull LoadRangeParams params,
            @NonNull LoadRangeCallback<IdentityItemModel> callback) {
        List<Identity> identities = loadRangeInternal(params.startPosition, params.loadSize);
        callback.onResult(convertIdentitiesToItemModels(identities));
    }

    private long computeCount() {
        Long count = mLayerClient.executeQueryForCount(Query.builder(Identity.class)
                .predicate(mPredicate)
                .build());
        if (count == null) {
            return 0L;
        }
        return count;
    }

    @NonNull
    @SuppressWarnings("unchecked")
    private List<Identity> loadRangeInternal(int position, int requestedLoadSize) {
        return (List<Identity>) mLayerClient.executeQueryForObjects(Query.builder(
                Identity.class)
                .predicate(mPredicate)
                .sortDescriptor(mSortDescriptor)
                .offset(position)
                .limit(requestedLoadSize)
                .build());
    }

    @NonNull
    private List<IdentityItemModel> convertIdentitiesToItemModels(
            @NonNull List<Identity> identities) {
        List<IdentityItemModel> itemModels = new ArrayList<>(identities.size());
        for (Identity identity : identities) {
            itemModels.add(new IdentityItemModel(identity));
        }
        return itemModels;
    }
}
