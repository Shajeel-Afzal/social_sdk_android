package com.layer.xdk.ui.identity.adapter;

import android.support.annotation.NonNull;
import android.support.v7.util.DiffUtil;
import android.view.ViewGroup;

import com.layer.sdk.LayerClient;
import com.layer.xdk.ui.databinding.XdkUiFourPartItemBinding;
import com.layer.xdk.ui.fourpartitem.adapter.FourPartItemRecyclerViewAdapter;
import com.layer.xdk.ui.fourpartitem.adapter.viewholder.FourPartItemVH;
import com.layer.xdk.ui.identity.adapter.viewholder.IdentityItemVHModel;
import com.layer.xdk.ui.fourpartitem.FourPartItemStyle;

import javax.inject.Inject;

import dagger.internal.Factory;

public class IdentityItemsAdapter extends FourPartItemRecyclerViewAdapter<IdentityItemModel, IdentityItemVHModel,
        XdkUiFourPartItemBinding, FourPartItemStyle, FourPartItemVH<IdentityItemModel, IdentityItemVHModel>> {

    private final Factory<IdentityItemVHModel> mItemViewModelFactory;

    @Inject
    public IdentityItemsAdapter(LayerClient layerClient,
            Factory<IdentityItemVHModel> itemViewModelFactory) {
        super(layerClient, new DiffCallback());
        mItemViewModelFactory = itemViewModelFactory;
    }

    @NonNull
    @Override
    public FourPartItemVH<IdentityItemModel, IdentityItemVHModel> onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType) {
        XdkUiFourPartItemBinding binding = XdkUiFourPartItemBinding.inflate(
                getLayoutInflater(parent.getContext()), parent, false);
        IdentityItemVHModel viewModel = mItemViewModelFactory.get();

        viewModel.setItemClickListener(getItemClickListener());

        FourPartItemVH<IdentityItemModel, IdentityItemVHModel>
                viewHolder = new FourPartItemVH<>(binding, viewModel, getStyle());

        binding.addOnRebindCallback(getOnRebindCallback());

        return viewHolder;
    }

    private static class DiffCallback extends DiffUtil.ItemCallback<IdentityItemModel> {
        @Override
        public boolean areItemsTheSame(@NonNull IdentityItemModel oldItem,
                @NonNull IdentityItemModel newItem) {
            return oldItem.getIdentity().getId().equals(newItem.getIdentity().getId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull IdentityItemModel oldItem,
                @NonNull IdentityItemModel newItem) {
            return oldItem.deepEquals(newItem);
        }
    }
}
