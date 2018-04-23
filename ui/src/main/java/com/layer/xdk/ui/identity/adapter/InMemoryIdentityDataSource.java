package com.layer.xdk.ui.identity.adapter;


import android.arch.paging.PositionalDataSource;
import android.support.annotation.NonNull;

import com.layer.sdk.LayerClient;
import com.layer.sdk.changes.LayerChange;
import com.layer.sdk.changes.LayerChangeEvent;
import com.layer.sdk.listeners.LayerChangeEventListener;
import com.layer.sdk.messaging.Identity;
import com.layer.xdk.ui.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Creates a {@link android.arch.paging.DataSource} to use with the paging library that loads
 * Identities from an in-memory list. This will convert them to {@link IdentityItemModel}
 * objects before returning.
 */
public class InMemoryIdentityDataSource extends PositionalDataSource<IdentityItemModel> {

    private final LayerClient mLayerClient;
    private final LayerChangeEventListener.BackgroundThread.Weak mListener;
    private final List<Identity> mIdentities;

    /**
     * Create a {@link android.arch.paging.DataSource} and registers a listener with the
     * {@link LayerClient} to listen for relevant change notifications to invalidate if necessary.
     *
     * @param layerClient client to use to listen for changes
     * @param identities identities to populate this data source
     */
    @SuppressWarnings("WeakerAccess")
    public InMemoryIdentityDataSource(@NonNull LayerClient layerClient,
            @NonNull List<Identity> identities) {
        mLayerClient = layerClient;
        mIdentities = identities;

        mListener = new LayerChangeEventListener.BackgroundThread.Weak() {
            @Override
            public void onChangeEvent(LayerChangeEvent layerChangeEvent) {
                List<LayerChange> changes = layerChangeEvent.getChanges();
                boolean needsInvalidation = false;
                for (LayerChange change : changes) {
                    switch (change.getObjectType()) {
                        case IDENTITY:
                            Identity changedIdentity = (Identity) change.getObject();
                            if (mIdentities.contains(changedIdentity)) {
                                needsInvalidation = true;
                            }
                            break;
                    }

                    if (needsInvalidation) {
                        // Unregister this listener, invalidate the data source and return so no
                        // more changes are processed
                        mLayerClient.unregisterEventListener(mListener);
                        if (Log.isLoggable(Log.VERBOSE)) {
                            Log.d("Invalidating " + InMemoryIdentityDataSource.class.getSimpleName()
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
        return mIdentities.size();
    }

    @NonNull
    @SuppressWarnings("unchecked")
    private List<Identity> loadRangeInternal(int position, int requestedLoadSize) {
        List<Identity> partialIdentities = new ArrayList<>(requestedLoadSize);
        for (int i = position; i < position + requestedLoadSize && i < mIdentities.size(); i++) {
            partialIdentities.add(mIdentities.get(i));
        }
        return partialIdentities;
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
