package com.layer.xdk.ui.fourpartitem.adapter.viewholder;

import android.support.annotation.CallSuper;
import android.support.v7.widget.RecyclerView;

import com.layer.xdk.ui.databinding.XdkUiFourPartItemBinding;
import com.layer.xdk.ui.fourpartitem.FourPartItemStyle;

public class FourPartItemVH<ITEM, VIEW_HOLDER_MODEL extends FourPartItemVHModel<ITEM>>
        extends RecyclerView.ViewHolder {

    private VIEW_HOLDER_MODEL mViewHolderModel;
    private FourPartItemStyle mStyle;
    protected XdkUiFourPartItemBinding mBinding;

    public FourPartItemVH(XdkUiFourPartItemBinding binding, VIEW_HOLDER_MODEL viewHolderModel,
                                  FourPartItemStyle itemStyle) {
        super(binding.getRoot());
        mBinding = binding;
        mViewHolderModel = viewHolderModel;
        mStyle = itemStyle;

        binding.setViewHolderModel(viewHolderModel);
        binding.setStyle(itemStyle);
        binding.getRoot().setOnClickListener(viewHolderModel.getOnClickListener());
        binding.getRoot().setOnLongClickListener(viewHolderModel.getOnLongClickListener());
    }

    public XdkUiFourPartItemBinding getBinding() {
        return mBinding;
    }

    @CallSuper
    public void setItem(ITEM item) {
        mViewHolderModel.setItem(item);
    }

    public ITEM getItem() {
        return mViewHolderModel.getItem();
    }

    public FourPartItemStyle getStyle() {
        return mStyle;
    }

    @CallSuper
    public void setStyle(FourPartItemStyle style) {
        mStyle = style;
    }

    public VIEW_HOLDER_MODEL getViewHolderModel() {
        return mViewHolderModel;
    }
}
