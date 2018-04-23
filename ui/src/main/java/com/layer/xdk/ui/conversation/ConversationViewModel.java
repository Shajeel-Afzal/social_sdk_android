package com.layer.xdk.ui.conversation;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.support.annotation.NonNull;

import com.layer.sdk.LayerClient;
import com.layer.sdk.messaging.Conversation;
import com.layer.xdk.ui.message.MessageItemsListViewModel;

import javax.inject.Inject;

/**
 * A ViewModel to drive the display of a {@link Conversation}
 */
public class ConversationViewModel extends BaseObservable {
    private Conversation mConversation;
    private MessageItemsListViewModel mMessageItemsListViewModel;
    private LayerClient mLayerClient;

    /**
     * Create a new ConversationViewModel instance
     *
     * @param layerClient               the {@link LayerClient} instance to be used
     * @param messageItemsListViewModel a {@link MessageItemsListViewModel} instance to drive a
     *                                  {@link com.layer.xdk.ui.message.MessageItemsListView}
     */
    @Inject
    public ConversationViewModel(@NonNull LayerClient layerClient,
                                 @NonNull MessageItemsListViewModel messageItemsListViewModel) {
        mMessageItemsListViewModel = messageItemsListViewModel;
        mLayerClient = layerClient;
    }

    /**
     * Sets the conversation to be displayed
     *
     * @param conversation the {@link Conversation} instance from which the
     *                     {@link com.layer.sdk.messaging.Message}s are to be displayed
     */
    public void setConversation(Conversation conversation) {
        mConversation = conversation;
        mMessageItemsListViewModel.setConversation(conversation);
        notifyChange();
    }

    /**
     * Gets the {@link LayerClient} instance set on this object
     *
     * @return the {@link LayerClient} instance set on this object
     */
    @NonNull
    @Bindable
    public LayerClient getLayerClient() {
        return mLayerClient;
    }

    /**
     * Gets the {@link Conversation} instance set on this object
     *
     * @return the {@link Conversation} instance set on this object
     */
    @NonNull
    @Bindable
    public Conversation getConversation() {
        return mConversation;
    }

    /**
     * Gets the {@link MessageItemsListViewModel} instance set on this object that can drives a
     * {@link com.layer.xdk.ui.message.MessageItemsListView}
     *
     * @return Gets the {@link MessageItemsListViewModel} instance set on this object
     */
    @NonNull
    public MessageItemsListViewModel getMessageItemsListViewModel() {
        return mMessageItemsListViewModel;
    }
}
