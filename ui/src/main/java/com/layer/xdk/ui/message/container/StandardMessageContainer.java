package com.layer.xdk.ui.message.container;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.databinding.Observable;
import android.graphics.drawable.GradientDrawable;
import android.support.annotation.AttrRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewStub;

import com.layer.xdk.ui.BR;
import com.layer.xdk.ui.R;
import com.layer.xdk.ui.databinding.XdkUiStandardMessageContainerBinding;
import com.layer.xdk.ui.message.model.MessageModel;

public class StandardMessageContainer extends MessageContainer {
    private XdkUiStandardMessageContainerBinding mBinding;

    public StandardMessageContainer(@NonNull Context context) {
        this(context, null, 0);
    }

    public StandardMessageContainer(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public StandardMessageContainer(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public View inflateMessageView(@LayoutRes int messageViewLayoutId) {
        ViewStub viewStub = getBinding().xdkUiStandardMessageContainerContentView.getViewStub();
        viewStub.setLayoutResource(messageViewLayoutId);
        return viewStub.inflate();
    }

    @Override
    protected View getMessageView() {
        return getBinding().xdkUiStandardMessageContainerContentView.getRoot();
    }

    @Override
    protected int getContainerMinimumWidth(boolean hasMetadata) {
        if (hasMetadata) {
            return getResources().getDimensionPixelSize(
                    R.dimen.xdk_ui_standard_message_container_min_width);
        }
        return getResources().getDimensionPixelSize(
                R.dimen.xdk_ui_standard_message_container_min_width_zero);
    }

    @Override
    public <T extends MessageModel> void setMessageModel(T model) {
        super.setMessageModel(model);

        getBinding().setMessageModel(model);
        if (model != null) {
            BottomPaddingCallback bottomPaddingCallback = new BottomPaddingCallback();
            model.addOnPropertyChangedCallback(bottomPaddingCallback);
            // Initiate the view properties as this will only be called if the model changes
            bottomPaddingCallback.onPropertyChanged(model, BR._all);
            setContentBackground(model);
        }

        getBinding().executePendingBindings();
    }

    @Override
    public <T extends MessageModel> void setContentBackground(@NonNull T model) {
        GradientDrawable background = (GradientDrawable) ContextCompat.getDrawable(getContext(), R.drawable.xdk_ui_standard_message_container_content_background);
        if (background != null) {
            background.setColor(ContextCompat.getColor(getContext(), model.getBackgroundColor()));
        }
        getBinding().xdkUiStandardMessageContainerContentView.getRoot().setBackgroundDrawable(background);
    }

    private XdkUiStandardMessageContainerBinding getBinding() {
        if (mBinding == null) {
            mBinding = DataBindingUtil.getBinding(this);
        }
        return mBinding;
    }

    private class BottomPaddingCallback extends Observable.OnPropertyChangedCallback {
        @Override
        public void onPropertyChanged(Observable sender, int propertyId) {
            MessageModel messageModel = (MessageModel) sender;
            if (propertyId == BR.hasMetadata || propertyId == BR._all) {
                int bottomPadding = 0;
                if (messageModel.getHasMetadata()) {
                    bottomPadding = getResources().getDimensionPixelOffset(
                            R.dimen.xdk_ui_margin_small);
                }
                setPadding(getPaddingLeft(), getPaddingTop(), getPaddingRight(), bottomPadding);
            }
        }
    }
}
