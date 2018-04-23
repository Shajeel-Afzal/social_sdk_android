package com.layer.xdk.ui;

import android.content.Context;

import com.layer.sdk.LayerClient;
import com.layer.xdk.ui.conversation.ConversationItemFormatter;
import com.layer.xdk.ui.conversation.DefaultConversationItemFormatter;
import com.layer.xdk.ui.identity.IdentityFormatter;
import com.layer.xdk.ui.identity.DefaultIdentityFormatter;
import com.layer.xdk.ui.message.model.MessageModelManager;
import com.layer.xdk.ui.util.DateFormatter;
import com.layer.xdk.ui.util.DefaultDateFormatter;
import com.layer.xdk.ui.message.image.cache.ImageCacheWrapper;

import javax.inject.Singleton;

import dagger.Component;

/**
 * Default Dagger component that uses the default XDK Dagger module. This allows access to
 * additional, non-critical dependencies that were provided by the service locator or were created
 * due to the service locator not providing an implementation.
 */
@Singleton
@Component(modules = {DefaultXdkUiModule.class})
public interface DefaultXdkUiComponent extends XdkUiComponent {

    @SuppressWarnings("unused")
    Context applicationContext();
    LayerClient layerClient();
    ImageCacheWrapper imageCacheWrapper();
    IdentityFormatter identityFormatter();
    DefaultIdentityFormatter defaultIdentityFormatter();
    @SuppressWarnings("unused")
    DateFormatter dateFormatter();
    DefaultDateFormatter defaultDateFormatter();
    ConversationItemFormatter conversationItemFormatter();
    DefaultConversationItemFormatter defaultConversationItemFormatter();
    MessageModelManager messageModelManager();
}
