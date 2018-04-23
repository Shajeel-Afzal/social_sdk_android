package com.layer.xdk.ui.message.image.popup;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import com.davemorrissey.labs.subscaleview.decoder.ImageDecoder;
import com.layer.sdk.LayerClient;
import com.layer.sdk.messaging.MessagePart;
import com.layer.xdk.ui.message.MessagePartUtils;
import com.layer.xdk.ui.message.image.cache.BitmapWrapper;
import com.layer.xdk.ui.message.image.cache.ImageCacheWrapper;
import com.layer.xdk.ui.util.Log;

import java.util.concurrent.TimeUnit;

public class MessagePartDecoder implements ImageDecoder {
    private static final int IMAGE_FETCH_TIMEOUT_MINUTES = 3;
    private static LayerClient sLayerClient;
    private static ImageCacheWrapper sImageCacheWrapper;

    public static void init(LayerClient layerClient, ImageCacheWrapper imageCacheWrapper) {
        sLayerClient = layerClient;
        sImageCacheWrapper = imageCacheWrapper;
    }

    @Override
    public Bitmap decode(Context context, Uri uri) throws Exception {
        if (!"layer".equals(uri.getScheme())) {
            // Not a Layer MessagePart. Attempt to load with the image cache
            BitmapWrapper bitmapWrapper = new BitmapWrapper(uri.toString(), 0, 0, false);
            sImageCacheWrapper.fetchBitmap(bitmapWrapper);
            return bitmapWrapper.getBitmap();
        }

        MessagePart part = (MessagePart) sLayerClient.get(uri);
        if (part == null) {
            if (Log.isLoggable(Log.ERROR)) {
                Log.e("No message part with ID: " + uri);
            }
            return null;
        }
        if (part.getMessage().isDeleted()) {
            if (Log.isLoggable(Log.ERROR)) {
                Log.e("Message part is deleted: " + uri);
            }
            return null;
        }
        if (!MessagePartUtils.downloadMessagePart(part, IMAGE_FETCH_TIMEOUT_MINUTES, TimeUnit.MINUTES)) {
            if (Log.isLoggable(Log.ERROR)) {
                Log.e("Timed out while downloading: " + uri);
            }
            return null;
        }

        return BitmapFactory.decodeStream(part.getDataStream());
    }
}
