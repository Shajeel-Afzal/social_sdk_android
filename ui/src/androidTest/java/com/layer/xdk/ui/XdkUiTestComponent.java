package com.layer.xdk.ui;


import com.layer.xdk.ui.conversation.adapter.viewholder.ConversationItemVHModel;
import com.layer.xdk.ui.identity.adapter.viewholder.IdentityItemVHModel;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = FakeXdkUiModule.class)
public interface XdkUiTestComponent extends DefaultXdkUiComponent {

    ConversationItemVHModel conversationItemViewModel();
    IdentityItemVHModel identityItemViewModel();
}
