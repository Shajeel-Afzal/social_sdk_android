package com.layer.xdk.ui.fourpartitem.adapter.viewholder;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.view.View;

import com.layer.sdk.messaging.Identity;
import com.layer.xdk.ui.identity.IdentityFormatter;
import com.layer.xdk.ui.recyclerview.OnItemClickListener;
import com.layer.xdk.ui.recyclerview.OnItemLongClickListener;
import com.layer.xdk.ui.message.image.cache.ImageCacheWrapper;

import java.util.Set;

public abstract class FourPartItemVHModel<ITEM> extends BaseObservable {

    private ITEM mItem;
    private OnItemClickListener<ITEM> mItemClickListener;
    private OnItemLongClickListener<ITEM> mItemLongClickListener;
    private IdentityFormatter mIdentityFormatter;
    private ImageCacheWrapper mImageCacheWrapper;

    private View.OnClickListener mOnClickListener;
    private View.OnLongClickListener mOnLongClickListener;

    public FourPartItemVHModel(IdentityFormatter identityFormatter, ImageCacheWrapper imageCacheWrapper) {
        mIdentityFormatter = identityFormatter;
        mImageCacheWrapper = imageCacheWrapper;

        mOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mItemClickListener != null) {
                    mItemClickListener.onItemClick(mItem);
                }
            }
        };

        mOnLongClickListener = new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (mItemLongClickListener != null) {
                    return mItemLongClickListener.onItemLongClick(mItem);
                } else {
                    return false;
                }
            }
        };
    }

    @Bindable
    public ITEM getItem() {
        return mItem;
    }

    public void setItem(ITEM item) {
        mItem = item;
        notifyChange();
    }

    public void setEmpty() {
        mItem = null;
    }

    public OnItemClickListener<ITEM> getItemClickListener() {
        return mItemClickListener;
    }

    public void setItemClickListener(OnItemClickListener<ITEM> itemClickListener) {
        mItemClickListener = itemClickListener;
    }

    public OnItemLongClickListener<ITEM> getItemLongClickListener() {
        return mItemLongClickListener;
    }

    public void setItemLongClickListener(OnItemLongClickListener<ITEM> itemLongClickListener) {
        mItemLongClickListener = itemLongClickListener;
    }

    @SuppressWarnings("WeakerAccess")
    public View.OnClickListener getOnClickListener() {
        return mOnClickListener;
    }

    @SuppressWarnings("WeakerAccess")
    public View.OnLongClickListener getOnLongClickListener() {
        return mOnLongClickListener;
    }

    @Bindable
    public IdentityFormatter getIdentityFormatter() {
        return mIdentityFormatter;
    }

    @Bindable
    public ImageCacheWrapper getImageCacheWrapper() {
        return mImageCacheWrapper;
    }

    @Bindable
    public abstract String getTitle();

    @Bindable
    public abstract String getSubtitle();

    @Bindable
    public abstract String getAccessoryText();

    @Bindable
    public abstract boolean isSecondaryState();

    @Bindable
    public abstract Set<Identity> getIdentities();
}
