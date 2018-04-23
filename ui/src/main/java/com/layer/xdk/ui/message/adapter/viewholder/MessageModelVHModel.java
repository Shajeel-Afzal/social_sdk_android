package com.layer.xdk.ui.message.adapter.viewholder;

import android.content.Context;
import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.view.View;

import com.layer.sdk.LayerClient;
import com.layer.xdk.ui.identity.IdentityFormatter;
import com.layer.xdk.ui.message.model.MessageModel;
import com.layer.xdk.ui.recyclerview.OnItemLongClickListener;
import com.layer.xdk.ui.util.DateFormatter;

public abstract class MessageModelVHModel extends BaseObservable {
    private Context mContext;
    private LayerClient mLayerClient;

    private MessageModel mItem;
    private OnItemLongClickListener<MessageModel> mItemLongClickListener;
    private IdentityFormatter mIdentityFormatter;
    private DateFormatter mDateFormatter;

    private View.OnLongClickListener mOnLongClickListener;

    @SuppressWarnings("WeakerAccess")
    public MessageModelVHModel(Context context, LayerClient layerClient, IdentityFormatter identityFormatter, DateFormatter dateFormatter) {
        mContext = context;
        mLayerClient = layerClient;

        mIdentityFormatter = identityFormatter;
        mDateFormatter = dateFormatter;


        mOnLongClickListener = new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                return mItemLongClickListener != null
                        && mItemLongClickListener.onItemLongClick(mItem);
            }
        };
    }

    @Bindable
    public MessageModel getItem() {
        return mItem;
    }

    public void setItem(MessageModel item) {
        mItem = item;
    }

    public void setItemLongClickListener(OnItemLongClickListener<MessageModel> listener) {
        mItemLongClickListener = listener;
    }

    @SuppressWarnings("WeakerAccess")
    public View.OnLongClickListener getOnLongClickListener() {
        return mOnLongClickListener;
    }

    public Context getContext() {
        return mContext;
    }

    public LayerClient getLayerClient() {
        return mLayerClient;
    }

    public IdentityFormatter getIdentityFormatter() {
        return mIdentityFormatter;
    }

    @SuppressWarnings("WeakerAccess")
    public DateFormatter getDateFormatter() {
        return mDateFormatter;
    }
}
