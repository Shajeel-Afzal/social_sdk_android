package com.layer.xdk.ui.conversation.adapter;

import android.support.annotation.NonNull;
import android.support.v7.util.DiffUtil;
import android.view.ViewGroup;

import com.layer.sdk.LayerClient;
import com.layer.xdk.ui.conversation.adapter.viewholder.ConversationItemVHModel;
import com.layer.xdk.ui.databinding.XdkUiFourPartItemBinding;
import com.layer.xdk.ui.fourpartitem.adapter.FourPartItemRecyclerViewAdapter;
import com.layer.xdk.ui.fourpartitem.adapter.viewholder.FourPartItemVH;
import com.layer.xdk.ui.fourpartitem.FourPartItemStyle;

import javax.inject.Inject;

import dagger.internal.Factory;

public class ConversationItemsAdapter extends FourPartItemRecyclerViewAdapter<ConversationItemModel,
        ConversationItemVHModel, XdkUiFourPartItemBinding, FourPartItemStyle,
        FourPartItemVH<ConversationItemModel, ConversationItemVHModel>> {

    private Factory<ConversationItemVHModel> mItemViewModelFactory;

    @Inject
    public ConversationItemsAdapter(LayerClient layerClient,
                                    Factory<ConversationItemVHModel> itemViewModelFactory) {
        super(layerClient, new DiffCallback());
        mItemViewModelFactory = itemViewModelFactory;
    }

    @NonNull
    @Override
    public FourPartItemVH<ConversationItemModel, ConversationItemVHModel> onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType) {
        XdkUiFourPartItemBinding binding = XdkUiFourPartItemBinding.inflate(
                getLayoutInflater(parent.getContext()), parent, false);
        ConversationItemVHModel viewModel = mItemViewModelFactory.get();

        viewModel.setItemClickListener(getItemClickListener());
        viewModel.setItemLongClickListener(getItemLongClickListener());

        FourPartItemVH<ConversationItemModel, ConversationItemVHModel> itemViewHolder =
                new FourPartItemVH<>(binding, viewModel, getStyle());

        binding.addOnRebindCallback(getOnRebindCallback());

        return itemViewHolder;
    }

    private static class DiffCallback extends DiffUtil.ItemCallback<ConversationItemModel> {
        @Override
        public boolean areItemsTheSame(@NonNull ConversationItemModel oldItem, @NonNull ConversationItemModel newItem) {
            return oldItem.getConversation().getId().equals(newItem.getConversation().getId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull ConversationItemModel oldItem, @NonNull ConversationItemModel newItem) {
            return oldItem.deepEquals(newItem);
        }
    }
}
