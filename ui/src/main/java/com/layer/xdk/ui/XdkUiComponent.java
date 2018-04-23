package com.layer.xdk.ui;


import com.layer.xdk.ui.conversation.ConversationItemsListViewModel;
import com.layer.xdk.ui.conversation.ConversationViewModel;
import com.layer.xdk.ui.identity.IdentityItemsListViewModel;

/**
 * Component interface that Dagger components should extend from. Extend from this class if
 * providing a custom Dagger module. This provides objects whose dependencies are injected using
 * the supplied modules.
 */
public interface XdkUiComponent {

    ConversationItemsListViewModel conversationItemsListViewModel();
    ConversationViewModel conversationViewModel();
    IdentityItemsListViewModel identityItemsListViewModel();
}
