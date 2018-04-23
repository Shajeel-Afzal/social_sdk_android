package com.layer.xdk.ui;


import android.content.Context;
import android.support.annotation.NonNull;

import com.layer.sdk.LayerClient;
import com.layer.xdk.ui.conversation.ConversationItemFormatter;
import com.layer.xdk.ui.identity.IdentityFormatter;
import com.layer.xdk.ui.util.DateFormatter;
import com.layer.xdk.ui.message.image.cache.ImageCacheWrapper;

/**
 * Module interface that Dagger modules should extend from. This provides dependencies for
 * instantiating classes via a {@link XdkUiComponent} component.
 *
 * Note that it is not strictly necessary to extend from this interface, or implement the methods
 * with the same signatures. It's possible that custom modules will use different method signatures
 * for the provided objects. In that case, use this interface as a guide as to <i>what</i> objects
 * need to be provided by this module.
 */
public interface XdkUiModule {

    /**
     * @return application context to use with the XDK UI
     */
    @NonNull
    Context provideApplicationContext();

    /**
     * @return LayerClient instance to use with the XDK UI
     */
    @NonNull
    LayerClient provideLayerClient();

    /**
     * @return image cache wrapper to use with the XDK UI
     */
    @NonNull
    ImageCacheWrapper provideImageCacheWrapper();

    /**
     * @return identity formatter to use with the XDK UI
     */
    @NonNull
    IdentityFormatter provideIdentityFormatter();

    /**
     * @return date formatter to use with the XDK UI
     */
    @NonNull
    DateFormatter provideDateFormatter();

    /**
     * @return conversation item formatter to use with the XDK UI
     */
    @NonNull
    ConversationItemFormatter provideConversationItemFormatter();
}
