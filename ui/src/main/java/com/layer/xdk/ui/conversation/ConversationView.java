package com.layer.xdk.ui.conversation;

import android.content.Context;
import android.databinding.BindingAdapter;
import android.support.constraint.ConstraintLayout;
import android.util.AttributeSet;
import android.view.LayoutInflater;

import com.layer.sdk.LayerClient;
import com.layer.sdk.messaging.Conversation;
import com.layer.sdk.messaging.Identity;
import com.layer.xdk.ui.composebar.ComposeBar;
import com.layer.xdk.ui.databinding.XdkUiConversationViewBinding;
import com.layer.xdk.ui.message.MessageItemsListView;
import com.layer.xdk.ui.message.MessageItemsListViewModel;
import com.layer.xdk.ui.typingindicator.BubbleTypingIndicatorFactory;
import com.layer.xdk.ui.typingindicator.TypingIndicatorLayout;
import com.layer.xdk.ui.typingindicator.TypingIndicatorMode;

import java.util.Set;

public class ConversationView extends ConstraintLayout {

    private MessageItemsListView mMessageItemListView;
    private ComposeBar mComposeBar;
    private TypingIndicatorLayout mTypingIndicator;

    private XdkUiConversationViewBinding mBinding;

    public ConversationView(Context context) {
        this(context, null);
    }

    public ConversationView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ConversationView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mBinding = XdkUiConversationViewBinding.inflate(LayoutInflater.from(context), this, true);

        mMessageItemListView = mBinding.messagesList;
        mComposeBar = mBinding.composeBar;

        TypingIndicatorLayout typingIndicator = new TypingIndicatorLayout(context);
        typingIndicator.setTypingIndicatorFactory(new BubbleTypingIndicatorFactory());
        setTypingIndicatorLayout(typingIndicator);
    }

    @BindingAdapter(value = {"app:conversation", "app:layerClient", "app:messageItemsListViewModel"})
    public static void setConversation(ConversationView view, Conversation conversation,
                                       LayerClient layerClient, MessageItemsListViewModel viewModel) {
        view.mBinding.setViewModel(viewModel);
        view.mBinding.executePendingBindings();
        view.mMessageItemListView.setConversation(layerClient, conversation);

        view.mComposeBar.setConversation(layerClient, conversation);
        view.mTypingIndicator.setConversation(layerClient, conversation);
    }

    @SuppressWarnings("unused")
    public MessageItemsListView getMessageItemListView() {
        return mMessageItemListView;
    }

    public ComposeBar getComposeBar() {
        return mComposeBar;
    }

    @SuppressWarnings("unused")
    public TypingIndicatorLayout getTypingIndicator() {
        return mTypingIndicator;
    }

    /**
     * Set a {@link TypingIndicatorLayout} to be shown when participants are typing
     * Defaults to {@link TypingIndicatorMode#BOTH}
     *
     * @param typingIndicatorLayout a {@link TypingIndicatorLayout} instance
     */
    public void setTypingIndicatorLayout(TypingIndicatorLayout typingIndicatorLayout) {
        setTypingIndicatorLayout(typingIndicatorLayout, TypingIndicatorMode.BOTH);
    }

    /**
     * Set a {@link TypingIndicatorLayout} to be shown when participants are typing
     *
     * @param typingIndicatorLayout a {@link TypingIndicatorLayout} instance
     * @param mode the {@link TypingIndicatorMode} to be used.
     */
    public void setTypingIndicatorLayout(TypingIndicatorLayout typingIndicatorLayout, final TypingIndicatorMode mode) {
        mTypingIndicator = typingIndicatorLayout;
        mTypingIndicator.setTypingActivityListener(new TypingIndicatorLayout.TypingActivityListener() {
            @Override
            public void onTypingActivityChange(TypingIndicatorLayout typingIndicator,
                                               boolean active, Set<Identity> users) {
                mMessageItemListView.setTypingIndicatorLayout(active ? typingIndicator : null,
                        users, mode);
            }
        });
    }
}
