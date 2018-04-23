package com.layer.xdk.ui.message.action;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.gson.JsonObject;
import com.layer.sdk.LayerClient;
import com.layer.xdk.ui.message.image.cache.ImageCacheWrapper;
import com.layer.xdk.ui.message.image.popup.ImagePopupActivity;

public class OpenUrlActionHandler extends ActionHandler {

    private static final String KEY_URL = "url";

    private static final String KEY_MIME_TYPE = "mime-type";
    private static final String KEY_HEIGHT = "height";
    private static final String KEY_WIDTH = "width";
    private static final String KEY_ORIENTATION = "orientation";

    private Intent mBrowserIntent;

    public OpenUrlActionHandler(LayerClient layerClient, ImageCacheWrapper imageCacheWrapper) {
        super(layerClient, "open-url");
        mBrowserIntent = new Intent(Intent.ACTION_VIEW);
        ImagePopupActivity.init(layerClient, imageCacheWrapper);
    }

    @Override
    public void performAction(@NonNull Context context, @Nullable JsonObject data) {
        if (data == null || !data.has(KEY_URL)) {
            throw new IllegalStateException("Incorrect data. No url to open");
        }

        if (data.has(KEY_MIME_TYPE)) {
            openPopupImage(context, data);
        } else {
            String url = data.get(KEY_URL).getAsString();
            openUrl(context, url);
        }
    }

    private void openUrl(Context context, String url) {
        mBrowserIntent.setData(Uri.parse(url));
        context.startActivity(mBrowserIntent);
    }

    private void openPopupImage(Context context, JsonObject data) {
        Intent intent = new Intent(context, ImagePopupActivity.class);
        ImagePopupActivity.Parameters parameters = new ImagePopupActivity.Parameters();
        String url = data.get(KEY_URL).getAsString();
        parameters.source(url);

        int width = data.get(KEY_WIDTH).getAsInt();
        int height = data.get(KEY_HEIGHT).getAsInt();
        int orientation = data.get(KEY_ORIENTATION).getAsInt();

        parameters.dimensions(width, height);
        parameters.orientation(orientation);

        intent.putExtra(ImagePopupActivity.EXTRA_PARAMS, parameters);
        context.startActivity(intent);
    }
}
