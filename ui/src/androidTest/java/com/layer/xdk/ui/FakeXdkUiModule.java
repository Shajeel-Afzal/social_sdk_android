package com.layer.xdk.ui;


import android.content.Context;
import android.support.annotation.NonNull;

import com.layer.sdk.LayerClient;
import com.layer.xdk.ui.conversation.ConversationItemFormatter;
import com.layer.xdk.ui.conversation.DefaultConversationItemFormatter;
import com.layer.xdk.ui.identity.IdentityFormatter;
import com.layer.xdk.ui.identity.DefaultIdentityFormatter;
import com.layer.xdk.ui.mock.MockLayerClient;
import com.layer.xdk.ui.util.DateFormatter;
import com.layer.xdk.ui.util.DefaultDateFormatter;
import com.layer.xdk.ui.message.image.cache.ImageCacheWrapper;
import com.layer.xdk.ui.message.image.cache.PicassoImageCacheWrapper;
import com.squareup.picasso.Picasso;

import javax.inject.Provider;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module(includes = XdkUiInternalModule.class)
public class FakeXdkUiModule implements XdkUiModule {

    private Context mContext;

    public FakeXdkUiModule(Context context) {
        mContext = context;
    }

    @Provides
    @Singleton
    @NonNull
    @Override
    public Context provideApplicationContext() {
        return mContext;
    }

    @Provides
    @Singleton
    @NonNull
    @Override
    public LayerClient provideLayerClient() {
        return new MockLayerClient();
    }

    @Provides
    @Singleton
    @NonNull
    @Override
    public ImageCacheWrapper provideImageCacheWrapper() {
        return new PicassoImageCacheWrapper(Picasso.with(mContext));
    }

    @Provides
    @Singleton
    @NonNull
    @Override
    public IdentityFormatter provideIdentityFormatter() {
        return new DefaultIdentityFormatter(mContext);
    }

    @Provides
    @Singleton
    @NonNull
    @Override
    public DateFormatter provideDateFormatter() {
        return new DefaultDateFormatter(mContext);
    }

    @Provides
    @Singleton
    @NonNull
    public ConversationItemFormatter provideConversationItemFormatter(
            Provider<DefaultConversationItemFormatter> provider) {
        return provider.get();
    }

    @SuppressWarnings("ConstantConditions")
    @NonNull
    @Override
    public ConversationItemFormatter provideConversationItemFormatter() {
        // This is unused so just return null
        return null;
    }
}
