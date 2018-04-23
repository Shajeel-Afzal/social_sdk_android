package com.layer.xdk.ui.message.adapter.decoration;


import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.layer.xdk.ui.message.adapter.MessageGrouping;
import com.layer.xdk.ui.message.adapter.viewholder.MessageModelVH;
import com.layer.xdk.ui.message.model.MessageModel;
import com.layer.xdk.ui.util.Log;

import java.util.EnumSet;

public abstract class MessageGroupingItemDecoration extends RecyclerView.ItemDecoration {
    private final int mOffsetSize;

    @SuppressWarnings("WeakerAccess")
    public MessageGroupingItemDecoration(int offsetSize) {
        mOffsetSize = offsetSize;
    }

    abstract boolean shouldDraw(EnumSet<MessageGrouping> groupings);

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        MessageModel model = getMessageModel(view, parent);
        // Null check for typing indicator
        if (model != null && shouldDraw(model.getGrouping())) {
            outRect.top = mOffsetSize;
        }
    }

    protected MessageModel getMessageModel(View view, RecyclerView parent) {
        RecyclerView.ViewHolder childViewHolder = parent.getChildViewHolder(view);
        if (!(childViewHolder instanceof MessageModelVH)) {
            if (Log.isLoggable(Log.ERROR)) {
                Log.e("This decoration can only be used with holders of type "
                        + MessageModelVH.class.getSimpleName());
            }
            throw new IllegalStateException("This decoration can only be used with holders of type "
                    + MessageModelVH.class.getSimpleName());
        }
        return ((MessageModelVH) childViewHolder).getItem();
    }
}
