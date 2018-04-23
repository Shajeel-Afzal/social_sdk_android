package com.layer.xdk.ui.message.adapter.viewholder;


import android.view.View;
import android.view.ViewGroup;

import com.layer.sdk.messaging.Identity;
import com.layer.xdk.ui.R;
import com.layer.xdk.ui.databinding.XdkUiMessageModelVhTypingIndicatorBinding;

import java.util.Set;

/**
 * Special type of {@link MessageModelVH} that holds and binds a typing indicator.
 * <p>
 * Note that this doesn't have a {@link com.layer.xdk.ui.message.model.MessageModel}.
 */
public class TypingIndicatorVH extends
        MessageModelVH<TypingIndicatorVHModel, XdkUiMessageModelVhTypingIndicatorBinding> {

    public TypingIndicatorVH(ViewGroup parent, TypingIndicatorVHModel model) {
        super(parent, R.layout.xdk_ui_message_model_vh_typing_indicator, model);
        getBinding().setViewHolderModel(model);
        getBinding().executePendingBindings();
    }

    @Override
    void onBind() {
        // Unused
    }

    public void clear() {
        getBinding().root.removeAllViews();
    }

    public void bind(Set<Identity> users, View typingIndicatorLayout, boolean shouldAvatarBeVisible) {
        getBinding().root.addView(typingIndicatorLayout);

        getViewHolderModel().setParticipants(users);
        getViewHolderModel().setAvatarViewVisible(shouldAvatarBeVisible);
        getViewHolderModel().notifyChange();

        getBinding().executePendingBindings();
    }
}
