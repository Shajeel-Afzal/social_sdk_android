package com.layer.xdk.ui.message.container;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewStub;

import com.layer.xdk.ui.R;
import com.layer.xdk.ui.databinding.XdkUiTitledMessageContainerBinding;
import com.layer.xdk.ui.message.model.MessageModel;
import com.layer.xdk.ui.message.view.IconProvider;

public class TitledMessageContainer extends MessageContainer {
    private XdkUiTitledMessageContainerBinding mBinding;

    public TitledMessageContainer(@NonNull Context context) {
        this(context, null, 0);
    }

    public TitledMessageContainer(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TitledMessageContainer(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public View inflateMessageView(@LayoutRes int messageViewLayoutId) {
        ViewStub viewStub = getBinding().xdkUiTitledMessageContainerContentView.getViewStub();
        viewStub.setLayoutResource(messageViewLayoutId);
        View inflatedView = viewStub.inflate();

        if (inflatedView instanceof IconProvider) {
            IconProvider iconProvider = (IconProvider) inflatedView;
            Drawable icon = iconProvider.getIconDrawable();
            getBinding().xdkUiTitledMessageContainerTitle.setCompoundDrawables(icon, null, null, null);
        } else {
            getBinding().xdkUiTitledMessageContainerTitle.setCompoundDrawables(null, null, null, null);
        }

        return inflatedView;
    }

    @Override
    protected View getMessageView() {
        return getBinding().xdkUiTitledMessageContainerContentView.getRoot();
    }

    @Override
    protected int getContainerMinimumWidth(boolean hasMetadata) {
        if (hasMetadata) {
            return getResources().getDimensionPixelSize(
                    R.dimen.xdk_ui_titled_message_container_min_width);
        }
        return getResources().getDimensionPixelSize(
                R.dimen.xdk_ui_titled_message_container_min_width_zero);
    }

    @Override
    public <T extends MessageModel> void setMessageModel(T model) {
        super.setMessageModel(model);
        getBinding().setMessageModel(model);
        getBinding().executePendingBindings();
    }

    @Override
    public <T extends MessageModel> void setContentBackground(T model) {
        GradientDrawable background = (GradientDrawable) ContextCompat.getDrawable(getContext(), R.drawable.xdk_ui_titled_message_container_background);
        if (background != null) {
            background.setColor(ContextCompat.getColor(getContext(), model.getBackgroundColor()));
        }
        getBinding().xdkUiTitledMessageContainerContentView.getRoot().setBackgroundDrawable(background);
    }

    private XdkUiTitledMessageContainerBinding getBinding() {
        if (mBinding == null) {
            mBinding = DataBindingUtil.getBinding(this);
        }
        return mBinding;
    }
}
