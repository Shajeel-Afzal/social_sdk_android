package com.layer.xdk.ui.fourpartitem;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

import com.layer.xdk.ui.R;
import com.layer.xdk.ui.fourpartitem.adapter.FourPartItemRecyclerViewAdapter;

import java.util.List;

public abstract class FourPartItemsListView<ADAPTER extends FourPartItemRecyclerViewAdapter> extends ConstraintLayout {

    protected FourPartItemStyle mFourPartItemStyle;
    protected RecyclerView mItemsRecyclerView;

    public FourPartItemsListView(Context context) {
        this(context, null);
    }

    public FourPartItemsListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
        mFourPartItemStyle = new FourPartItemStyle(context, attrs, 0);
    }

    protected void init() {
        inflate(getContext(), R.layout.xdk_ui_four_part_items_list, this);
        mItemsRecyclerView = findViewById(R.id.xdk_ui_items_recycler);

        LinearLayoutManager manager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        manager.setStackFromEnd(false);
        mItemsRecyclerView.setLayoutManager(manager);

        DefaultItemAnimator noChangeAnimator = new DefaultItemAnimator() {
            @Override
            public boolean canReuseUpdatedViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, @NonNull List<Object> payloads) {
                return true;
            }
        };
        noChangeAnimator.setSupportsChangeAnimations(false);
        mItemsRecyclerView.setItemAnimator(noChangeAnimator);
    }

    @SuppressWarnings("unchecked")
    public void setAdapter(ADAPTER adapter) {
        adapter.setStyle(mFourPartItemStyle);
        mItemsRecyclerView.setAdapter(adapter);
    }
}