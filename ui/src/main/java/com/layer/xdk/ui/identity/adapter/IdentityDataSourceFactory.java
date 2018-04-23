package com.layer.xdk.ui.identity.adapter;


import android.arch.paging.DataSource;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.layer.sdk.LayerClient;
import com.layer.sdk.messaging.Identity;
import com.layer.sdk.query.Predicate;
import com.layer.sdk.query.SortDescriptor;
import com.layer.xdk.ui.util.Log;

import java.util.List;

import javax.inject.Inject;

/**
 * Factory that handles creations of {@link IdentityDataSource} and
 * {@link InMemoryIdentityDataSource} objects. This contains the variables used to create new
 * instances of these data sources when the old one becomes invalid. Only one type of data source
 * can ever be instantiated in this factory. Call either
 * {@link IdentityDataSourceFactory#useQuery(Predicate, SortDescriptor)} or
 * {@link IdentityDataSourceFactory#useIdentities(List)} to initialize.
 */
public class IdentityDataSourceFactory implements DataSource.Factory<Integer, IdentityItemModel> {

    private LayerClient mLayerClient;
    private Predicate mPredicate;
    private SortDescriptor mSortDescriptor;
    private List<Identity> mIdentities;

    /**
     * Creates a factory.
     *
     * @param layerClient client to use for change notifications and a query if using.
     */
    @Inject
    public IdentityDataSourceFactory(LayerClient layerClient) {
        mLayerClient = layerClient;
    }

    /**
     * Create {@link IdentityDataSource} instances using the supplied query.
     *
     * @param predicate identity predicate to use for the query
     * @param sortDescriptor identity sorting to use for the query
     */
    public void useQuery(@Nullable Predicate predicate, @Nullable SortDescriptor sortDescriptor) {
        if (mIdentities != null) {
            if (Log.isLoggable(Log.ERROR)) {
                Log.e("Cannot use both query and identities in an identity data source");
            }
            throw new IllegalStateException("Cannot use both query and identities in an identity"
                    + " data source");
        }
        mPredicate = predicate;
        mSortDescriptor = sortDescriptor;
    }

    /**
     * Create {@link InMemoryIdentityDataSource} instances using the supplied list of identities.
     *
     * @param identities identity list to populate the data source with
     */
    public void useIdentities(@NonNull List<Identity> identities) {
        if (mPredicate != null || mSortDescriptor != null) {
            if (Log.isLoggable(Log.ERROR)) {
                Log.e("Cannot use both query and identities in an identity data source");
            }
            throw new IllegalStateException("Cannot use both query and identities in an identity"
                    + " data source");
        }
        mIdentities = identities;
    }

    @Override
    public DataSource<Integer, IdentityItemModel> create() {
        if (mIdentities != null) {
            return new InMemoryIdentityDataSource(mLayerClient, mIdentities);
        } else {
            return new IdentityDataSource(mLayerClient, mPredicate, mSortDescriptor);
        }
    }
}
