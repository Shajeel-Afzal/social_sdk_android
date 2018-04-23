/*
 * Copyright (c) 2015 Layer. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.layer.xdk.ui.util;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.layer.sdk.LayerClient;
import com.layer.sdk.exceptions.LayerException;
import com.layer.sdk.listeners.LayerAuthenticationListener;
import com.layer.sdk.messaging.Identity;
import com.layer.xdk.ui.BuildConfig;

import java.util.concurrent.atomic.AtomicBoolean;

public class Util {

    /**
     * Returns the app version name.
     *
     * @return The app version name.
     */
    public static String getXdkUiVersion() {
        return BuildConfig.VERSION_NAME;
    }

    @Deprecated
    @NonNull
    public static String getDisplayName(Identity identity) {
        if (TextUtils.isEmpty(identity.getDisplayName())) {
            String first = identity.getFirstName();
            String last = identity.getLastName();
            if (!TextUtils.isEmpty(first)) {
                if (!TextUtils.isEmpty(last)) {
                    return String.format("%s %s", first, last);
                }
                return first;
            } else if (!TextUtils.isEmpty(last)) {
                return last;
            } else {
                return identity.getUserId();
            }
        }
        return identity.getDisplayName();
    }

    /**
     * Asynchronously deauthenticates with Layer.
     *
     * @param layerClient LayerClient to deauthenticate.
     * @param callback    Callback to report deauthentication success and failure.
     */
    public static void deauthenticate(LayerClient layerClient, final DeauthenticationCallback callback) {
        final AtomicBoolean alerted = new AtomicBoolean(false);
        final LayerAuthenticationListener listener = new LayerAuthenticationListener.BackgroundThread.Weak() {
            @Override
            public void onAuthenticated(LayerClient layerClient, String s) {

            }

            @Override
            public void onDeauthenticated(LayerClient layerClient) {
                if (alerted.compareAndSet(false, true)) {
                    callback.onDeauthenticationSuccess(layerClient);
                }
                layerClient.unregisterAuthenticationListener(this);
            }

            @Override
            public void onAuthenticationChallenge(LayerClient layerClient, String s) {

            }

            @Override
            public void onAuthenticationError(LayerClient layerClient, LayerException e) {
                if (alerted.compareAndSet(false, true)) {
                    callback.onDeauthenticationFailed(layerClient, e.getMessage());
                }
                layerClient.unregisterAuthenticationListener(this);
            }
        };
        layerClient.registerAuthenticationListener(listener);
        if (!layerClient.isAuthenticated()) {
            layerClient.unregisterAuthenticationListener(listener);
            if (alerted.compareAndSet(false, true)) {
                callback.onDeauthenticationSuccess(layerClient);
            }
            return;
        }
        layerClient.deauthenticate();
    }

    public interface DeauthenticationCallback {
        void onDeauthenticationSuccess(LayerClient client);

        void onDeauthenticationFailed(LayerClient client, String reason);
    }
}