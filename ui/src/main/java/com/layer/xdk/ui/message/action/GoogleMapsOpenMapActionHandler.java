package com.layer.xdk.ui.message.action;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.google.gson.JsonObject;
import com.layer.sdk.LayerClient;

import java.util.Locale;

public class GoogleMapsOpenMapActionHandler extends ActionHandler {

    private static final String ACTION_EVENT = "open-map";

    private static final String KEY_ADDRESS = "address";
    private static final String KEY_LATITUDE = "latitude";
    private static final String KEY_LONGITUDE = "longitude";
    private static final String KEY_TITLE = "title";

    public GoogleMapsOpenMapActionHandler(LayerClient layerClient) {
        super(layerClient, ACTION_EVENT);
    }

    @Override
    public void performAction(@NonNull Context context, @Nullable JsonObject data) {
        if (data != null && data.size() > 0) {
            Uri googleMapsUri;

            double latitude = 0.0f;
            double longitude = 0.0f;

            if (data.has(KEY_LATITUDE) && data.has(KEY_LONGITUDE)) {
                latitude = data.get(KEY_LATITUDE).getAsDouble();
                longitude = data.get(KEY_LONGITUDE).getAsDouble();
            }

            String markerTitle = "";
            if (data.has(KEY_TITLE)) {
                markerTitle = data.get(KEY_TITLE).getAsString();
            }

            if (data.has(KEY_ADDRESS)) {
                googleMapsUri = constructGoogleMapsUri(data.get(KEY_ADDRESS).getAsString());
            } else {
                googleMapsUri = constructGoogleMapsUri(latitude, longitude, markerTitle);
            }

            Intent openMapsIntent = new Intent(Intent.ACTION_VIEW, googleMapsUri);

            if (openMapsIntent.resolveActivity(context.getPackageManager()) != null) {
                context.startActivity(openMapsIntent);
            }
        }
    }

    /**
     * Constructs a URI for an address per the scheme
     * <a href="https://developer.android.com/guide/components/intents-common.html#Maps">here </a>.
     *
     * @param address the address to send for the maps intent
     * @return a Uri to use with the {@link Intent#ACTION_VIEW} intent
     */
    private Uri constructGoogleMapsUri(String address) {
        String queryString = String.format(Locale.US, "geo:0,0?q=%s", Uri.encode(address));
        return Uri.parse(queryString);
    }

    /**
     * Constructs a URI for a lat/long with option label per the scheme
     * <a href="https://developer.android.com/guide/components/intents-common.html#Maps">here </a>.
     *
     * @param latitude latitude to use for the maps intent
     * @param longitude longitude to use for the maps intent
     * @param markerTitle optional label to set on the location
     * @return a Uri to use with the {@link Intent#ACTION_VIEW} intent
     */
    private Uri constructGoogleMapsUri(double latitude, double longitude,
            @Nullable String markerTitle) {
        String queryString;
        if (TextUtils.isEmpty(markerTitle)) {
            queryString = String.format(Locale.US, "geo:0,0?q=%f,%f", latitude,
                longitude);
        } else {
            queryString = String.format(Locale.US, "geo:0,0?q=%f,%f(%s)", latitude,
                    longitude, markerTitle);
        }
        return Uri.parse(queryString);
    }
}
