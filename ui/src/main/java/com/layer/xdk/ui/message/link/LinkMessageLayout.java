package com.layer.xdk.ui.message.link;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.util.AttributeSet;
import android.view.View;

import com.layer.xdk.ui.message.view.MessageViewHelper;

public class LinkMessageLayout extends ConstraintLayout {
    private MessageViewHelper mMessageViewHelper;

    public LinkMessageLayout(Context context) {
        this(context, null, 0);
    }

    public LinkMessageLayout(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LinkMessageLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mMessageViewHelper = new MessageViewHelper(context);
        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mMessageViewHelper.performAction();
            }
        });
    }

    public void setMessageModel(LinkMessageModel model) {
        mMessageViewHelper.setMessageModel(model);
    }
}
