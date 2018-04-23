package com.layer.xdk.ui.message.location;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.view.View;

import com.layer.xdk.ui.databinding.XdkUiLocationMessageViewBinding;
import com.layer.xdk.ui.message.view.MessageViewHelper;

public class LocationMessageView extends AppCompatImageView {
    private MessageViewHelper mMessageViewHelper;
    private XdkUiLocationMessageViewBinding mBinding;

    public LocationMessageView(Context context) {
        this(context, null, 0);
    }

    public LocationMessageView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LocationMessageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mMessageViewHelper = new MessageViewHelper(context);

        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                mMessageViewHelper.performAction();
            }
        });
    }

    public void setMessageModel(LocationMessageModel model) {
        mBinding = DataBindingUtil.getBinding(this);
        mMessageViewHelper.setMessageModel(model);
    }

    public void hideMap(boolean hideMap) {
        mBinding.setHideMap(hideMap);
    }

    public boolean isMapHidden() {
        return mBinding.getHideMap();
    }
}
