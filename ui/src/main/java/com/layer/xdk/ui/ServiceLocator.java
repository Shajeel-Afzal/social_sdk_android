package com.layer.xdk.ui;


import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.layer.sdk.LayerClient;
import com.layer.xdk.ui.conversation.ConversationItemFormatter;
import com.layer.xdk.ui.conversation.DefaultConversationItemFormatter;
import com.layer.xdk.ui.identity.DefaultIdentityFormatter;
import com.layer.xdk.ui.identity.IdentityFormatter;
import com.layer.xdk.ui.util.DateFormatter;
import com.layer.xdk.ui.message.image.cache.ImageCacheWrapper;
import com.layer.xdk.ui.message.image.cache.PicassoImageCacheWrapper;
import com.layer.xdk.ui.message.image.cache.requesthandlers.MessagePartRequestHandler;
import com.layer.xdk.ui.util.DefaultDateFormatter;
import com.squareup.picasso.Picasso;

/**
 * A service locator that holds dependencies to supply to the XDK UI. In some cases, a default
 * dependency will be instantiated if the dependency is request and hasn't been set yet.
 *
 * If your app has an existing Dagger implementation and you'd like to supply a module and
 * component, you can bypass this class altogether.
 */
public class ServiceLocator {
    private Context mAppContext;
    private LayerClient mLayerClient;
    private ImageCacheWrapper mImageCacheWrapper;
    private IdentityFormatter mIdentityFormatter;
    private DateFormatter mDateFormatter;
    private ConversationItemFormatter mConversationItemFormatter;

    private enum LayerXdkComponentManager {
        INSTANCE;

        private DefaultXdkUiComponent mLayerXdkComponent;

        private DefaultXdkUiComponent getInstance(ServiceLocator serviceLocator) {
            if (mLayerXdkComponent == null) {
                DefaultXdkUiModule module = new DefaultXdkUiModule(serviceLocator);
                mLayerXdkComponent = DaggerDefaultXdkUiComponent.builder()
                        .defaultXdkUiModule(module)
                        .build();
            }
            return mLayerXdkComponent;
        }
    }

    /**
     * @return the context set on this locator
     */
    @Nullable
    public Context getAppContext() {
        return mAppContext;
    }

    /**
     * Set the application {@link Context} on this locator. This is a required call to use the XDK.
     * This enforces the use of an application context by calling
     * {@link Context#getApplicationContext()}.
     *
     * @param appContext context to use with the XDK UI
     */
    public void setAppContext(@NonNull Context appContext) {
        mAppContext = appContext.getApplicationContext();
    }

    /**
     * @return the LayerClient set on this locator
     */
    @Nullable
    public LayerClient getLayerClient() {
        return mLayerClient;
    }

    /**
     * Set the {@link LayerClient} on this locator. This is a required call to use the XDK.
     *
     * @param layerClient LayerClient instance to use with the XDK UI
     */
    public void setLayerClient(@Nullable LayerClient layerClient) {
        mLayerClient = layerClient;
    }

    /**
     * Return the {@link ImageCacheWrapper} set on this locator. If no {@link ImageCacheWrapper} is
     * set by the time this is called, a default {@link PicassoImageCacheWrapper} will be created.
     *
     * @return the image cache wrapper set on this locator or a default wrapper if none is supplied
     */
    @NonNull
    public ImageCacheWrapper getImageCacheWrapper() {
        if (mImageCacheWrapper == null) {
            mImageCacheWrapper = new PicassoImageCacheWrapper(new Picasso.Builder(mAppContext)
                    .addRequestHandler(new MessagePartRequestHandler(mLayerClient))
                    .build());
        }
        return mImageCacheWrapper;
    }

    /**
     * Set the {@link ImageCacheWrapper} on this locator. This is an optional call to use the XDK.
     *
     * @param imageCacheWrapper image cache wrapper to use with the XDK UI
     */
    public void setImageCacheWrapper(@Nullable ImageCacheWrapper imageCacheWrapper) {
        mImageCacheWrapper = imageCacheWrapper;
    }

    /**
     * Return the {@link IdentityFormatter} set on this locator. If no {@link IdentityFormatter} is
     * set by the time this is called, a {@link DefaultIdentityFormatter} will be created.
     *
     * @return identity formatter set on this locator or a default formatter if none is supplied
     */
    @NonNull
    public IdentityFormatter getIdentityFormatter() {
        if (mIdentityFormatter == null) {
            mIdentityFormatter = getXdkUiComponent().defaultIdentityFormatter();
        }
        return mIdentityFormatter;
    }

    /**
     * Set the {@link IdentityFormatter} on this locator. This is an optional call to use the XDK.
     *
     * @param formatter identity formatter to use with the XDK UI
     */
    public void setIdentityFormatter(@Nullable IdentityFormatter formatter) {
        mIdentityFormatter = formatter;
    }

    /**
     * Return the {@link ConversationItemFormatter} set on this locator. If no
     * {@link ConversationItemFormatter} is set by the time this is called, a default
     * {@link DefaultConversationItemFormatter} will be created.
     *
     * @return conversation item formatter set on this locator or a default formatter if none
     * is supplied
     */
    @SuppressWarnings("WeakerAccess")
    @NonNull
    public ConversationItemFormatter getConversationItemFormatter() {
        if (mConversationItemFormatter == null) {
            mConversationItemFormatter = getXdkUiComponent().defaultConversationItemFormatter();
        }
        return mConversationItemFormatter;
    }

    /**
     * Set the {@link ConversationItemFormatter} on this locator. This is an optional call to use
     * the XDK.
     *
     * @param formatter conversation item formatter to use with the XDK UI
     */
    @SuppressWarnings("unused")
    public void setConversationItemFormatter(@Nullable ConversationItemFormatter formatter) {
        mConversationItemFormatter = formatter;
    }

    /**
     * Return the {@link DateFormatter} set on this locator. If no {@link DateFormatter} is set by
     * the time this is called, a {@link DefaultDateFormatter} will be created.
     *
     * @return date formatter set on this locator or a default formatter if none is supplied
     */
    @NonNull
    public DateFormatter getDateFormatter() {
        if (mDateFormatter == null) {
            mDateFormatter = getXdkUiComponent().defaultDateFormatter();
        }
        return mDateFormatter;
    }

    /**
     * Set the {@link DateFormatter} on this locator. This is an optional call to use the XDK.
     *
     * @param formatter date formatter to use with the XDK UI
     */
    @SuppressWarnings("unused")
    public void setDateFormatter(@Nullable DateFormatter formatter) {
        mDateFormatter = formatter;
    }

    /**
     * Create a {@link DefaultXdkUiComponent} if one doesn't exist. Then return that singleton
     * instance. This {@link DefaultXdkUiComponent} will create objects used to work with the
     * Layer XDK using dependencies specified in this service locator.
     *
     * @return a component used to instantiate Layer XDK objects.
     */
    @NonNull
    public DefaultXdkUiComponent getXdkUiComponent() {
        return LayerXdkComponentManager.INSTANCE.getInstance(this);
    }
}
