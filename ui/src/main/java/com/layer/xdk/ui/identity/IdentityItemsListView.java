package com.layer.xdk.ui.identity;

import android.content.Context;
import android.util.AttributeSet;

import com.layer.xdk.ui.fourpartitem.FourPartItemsListView;
import com.layer.xdk.ui.identity.adapter.IdentityItemsAdapter;

public class IdentityItemsListView extends FourPartItemsListView<IdentityItemsAdapter> {
    public IdentityItemsListView(Context context) {
        super(context);
    }

    public IdentityItemsListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
}
