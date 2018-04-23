package com.layer.xdk.ui.message.container;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.graphics.drawable.GradientDrawable;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintSet;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;

import com.layer.xdk.ui.R;
import com.layer.xdk.ui.message.model.MessageModel;
import com.layer.xdk.ui.util.Log;

public class EmptyMessageContainer extends MessageContainer {
    private LayoutInflater mInflater;

    public EmptyMessageContainer(@NonNull Context context) {
        this(context, null, 0);
    }

    public EmptyMessageContainer(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public EmptyMessageContainer(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public View inflateMessageView(@LayoutRes int messageViewLayoutId) {
        ViewDataBinding binding = DataBindingUtil.inflate(mInflater, messageViewLayoutId, this,
                true);
        View messageView = binding.getRoot();
        ConstraintSet set = new ConstraintSet();
        set.connect(messageView.getId(), ConstraintSet.START, getId(), ConstraintSet.START);
        set.connect(messageView.getId(), ConstraintSet.END, getId(), ConstraintSet.END);
        set.connect(messageView.getId(), ConstraintSet.TOP, getId(), ConstraintSet.TOP);
        set.connect(messageView.getId(), ConstraintSet.BOTTOM, getId(), ConstraintSet.BOTTOM);
        set.applyTo(this);
        return messageView;
    }

    @Override
    protected View getMessageView() {
        if (getChildCount() == 0) {
            if (Log.isLoggable(Log.ERROR)) {
                Log.w("No message view set on this container");
            }
            throw new IllegalStateException("No message view set on this container");
        }
        return getChildAt(0);
    }

    @Override
    protected int getContainerMinimumWidth(boolean hasMetadata) {
        // This container doesn't have a minimum width
        return 0;
    }

    @Override
    public <T extends MessageModel> void setContentBackground(@NonNull T model) {
        GradientDrawable background = (GradientDrawable) ContextCompat.getDrawable(getContext(), R.drawable.xdk_ui_standard_message_container_content_background);
        if (background != null) {
            background.setColor(ContextCompat.getColor(getContext(), model.getBackgroundColor()));
        }
        getChildAt(0).setBackgroundDrawable(background);
    }
}
