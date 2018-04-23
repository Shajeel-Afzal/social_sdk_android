package com.layer.xdk.ui.message.adapter.viewholder;


import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.support.annotation.CallSuper;
import android.support.annotation.LayoutRes;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.layer.xdk.ui.message.model.MessageModel;

public abstract class MessageModelVH<VIEW_MODEL extends MessageModelVHModel, BINDING extends ViewDataBinding> extends
        RecyclerView.ViewHolder {

    private final VIEW_MODEL mViewModel;
    protected final BINDING mBinding;

    private MessageModelVH(BINDING binding, VIEW_MODEL viewModel) {
        super(binding.getRoot());
        mBinding = binding;
        mViewModel = viewModel;
    }

    @SuppressWarnings("WeakerAccess")
    public MessageModelVH(ViewGroup parent, @LayoutRes int layoutId, VIEW_MODEL viewModel) {
        this(DataBindingUtil.<BINDING>inflate(LayoutInflater.from(parent.getContext()), layoutId, parent, false), viewModel);
    }

    public BINDING getBinding() {
        return mBinding;
    }

    @CallSuper
    public void setItem(MessageModel item) {
        mViewModel.setItem(item);
    }

    public MessageModel getItem() {
        return mViewModel.getItem();
    }

    public VIEW_MODEL getViewHolderModel() {
        return mViewModel;
    }

    abstract void onBind();

    public void bindItem(MessageModel item) {
        mViewModel.setItem(item);
        onBind();
        mBinding.executePendingBindings();
    }
}
