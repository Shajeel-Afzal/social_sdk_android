package com.layer.xdk.ui.message.product;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.util.AttributeSet;
import android.view.View;

import com.layer.xdk.ui.message.view.MessageViewHelper;

public class ProductMessageLayout extends ConstraintLayout {
    private MessageViewHelper mMessageViewHelper;

    public ProductMessageLayout(Context context) {
        this(context, null, 0);
    }

    public ProductMessageLayout(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ProductMessageLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mMessageViewHelper = new MessageViewHelper(context);
        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mMessageViewHelper.performAction();
            }
        });
    }

    public void setMessageModel(final ProductMessageModel model) {
        mMessageViewHelper.setMessageModel(model);
    }
}
