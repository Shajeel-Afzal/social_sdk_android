package com.layer.xdk.ui.message.location;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;
import com.layer.sdk.LayerClient;
import com.layer.sdk.messaging.Message;
import com.layer.sdk.messaging.MessagePart;
import com.layer.xdk.ui.R;
import com.layer.xdk.ui.message.image.cache.ImageRequestParameters;
import com.layer.xdk.ui.message.model.MessageModel;
import com.layer.xdk.ui.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStreamReader;

public class LocationMessageModel extends MessageModel {
    public static final String ROOT_MIME_TYPE = "application/vnd.layer.location+json";
    private static final String LEGACY_KEY_LATITUDE = "lat";
    private static final String LEGACY_KEY_LONGITUDE = "lon";
    private static final String LEGACY_KEY_LABEL = "label";

    private static final String ACTION_EVENT_OPEN_MAP = "open-map";
    private static final double GOLDEN_RATIO = (1.0 + Math.sqrt(5.0)) / 2.0;

    private LocationMessageMetadata mMetadata;
    private boolean mLegacy;

    public LocationMessageModel(@NonNull Context context, @NonNull LayerClient layerClient,
                                @NonNull Message message) {
        super(context, layerClient, message);
    }

    @Override
    public int getViewLayoutId() {
        return R.layout.xdk_ui_location_message_view;
    }

    @Override
    public int getContainerViewLayoutId() {
        return R.layout.xdk_ui_standard_message_container;
    }

    @Override
    protected void parse(@NonNull MessagePart messagePart) {
        InputStreamReader inputStreamReader = new InputStreamReader(messagePart.getDataStream());
        JsonReader reader = new JsonReader(inputStreamReader);
        mMetadata = getGson().fromJson(reader, LocationMessageMetadata.class);
        try {
            inputStreamReader.close();
        } catch (IOException e) {
            if (Log.isLoggable(Log.ERROR)) {
                Log.e("Failed to close input stream while parsing location message", e);
            }
        }
    }

    @Override
    protected void processLegacyParts() {
        mLegacy = true;
        mMetadata = new LocationMessageMetadata();

        try {
            JSONObject json = new JSONObject(
                    new String(getMessage().getMessageParts().iterator().next().getData()));

            mMetadata.mLatitude = json.optDouble(LEGACY_KEY_LATITUDE, 0);
            mMetadata.mLongitude = json.optDouble(LEGACY_KEY_LONGITUDE, 0);
            mMetadata.mTitle = json.optString(LEGACY_KEY_LABEL, null);
        } catch (JSONException e) {
            if (Log.isLoggable(Log.ERROR)) {
                Log.e(e.getMessage(), e);
            }
        }
    }

    @Override
    protected boolean shouldDownloadContentIfNotReady(@NonNull MessagePart messagePart) {
        return true;
    }

    @Override
    public String getTitle() {
        if (mLegacy) {
            // Return null here since title is used for the marker name
            return null;
        }
        return mMetadata != null ? mMetadata.mTitle : null;
    }

    @Nullable
    @Override
    public String getDescription() {
        if (mMetadata != null) {
            if (mMetadata.mDescription != null) {
                return mMetadata.mDescription;
            } else {
                return mMetadata.getFormattedAddress();
            }
        }

        return null;
    }

    @Override
    public String getFooter() {
        return null;
    }

    @Override
    public String getActionEvent() {
        if (super.getActionEvent() != null) {
            return super.getActionEvent();
        }

        if (mMetadata != null) {
            return mMetadata.mAction != null ? mMetadata.mAction.getEvent() : ACTION_EVENT_OPEN_MAP;
        }

        return null;
    }

    @NonNull
    @Override
    public JsonObject getActionData() {
        if (super.getActionData().size() > 0) {
            return super.getActionData();
        }

        JsonObject actionData;

        if (mMetadata != null) {
            if (mMetadata.mAction != null) {
                actionData = mMetadata.mAction.getData();
            } else {
                actionData = new JsonObject();
                if (mMetadata.mLongitude != null && mMetadata.mLatitude != null) {
                    actionData.addProperty("latitude", mMetadata.mLatitude);
                    actionData.addProperty("longitude", mMetadata.mLongitude);
                } else if (mMetadata.getFormattedAddress() != null) {
                    actionData.addProperty("address", mMetadata.getFormattedAddress());
                }

                if (mMetadata.mTitle != null) {
                    actionData.addProperty("title", mMetadata.mTitle);
                }
            }
        } else {
            actionData = new JsonObject();
        }
        return actionData;
    }

    @Override
    public int getBackgroundColor() {
        return R.color.xdk_ui_location_message_background;
    }

    @Override
    public boolean getHasContent() {
        return mMetadata != null;
    }

    @Nullable
    @Override
    public String getPreviewText() {
        String title = getTitle();
        return title != null ? title : getAppContext().getString(R.string.xdk_ui_location_message_preview_text);
    }

    @Nullable
    public LocationMessageMetadata getMetadata() {
        return mMetadata;
    }

    @Nullable
    public String getFormattedAddress() {
        return mMetadata != null ? mMetadata.getFormattedAddress() : null;
    }

    @Nullable
    public ImageRequestParameters getMapImageRequestParameters() {
        if (mMetadata != null) {
            StringBuilder url = new StringBuilder("https://maps.googleapis.com/maps/api/staticmap?maptype=roadmap&scale=2");

            url.append("&zoom=")
                    .append(mMetadata.getZoom())
                    .append("&markers=");

            // Set location parameters or geocode
            if (mMetadata.mLatitude != null && mMetadata.mLongitude != null) {
                url.append(mMetadata.mLatitude).append(",").append(mMetadata.mLongitude);
            } else if (mMetadata.getFormattedAddress() != null) {
                // Encode this as there may be spaces
                url.append(Uri.encode(mMetadata.getFormattedAddress()));
            } else {
                return null;
            }

            // Set dimensions
            // Google Static Map API has max dimension 640
            int mapWidth = (int) getAppContext().getResources().getDimension(R.dimen.xdk_ui_location_message_map_width);
            int mapHeight = (int) Math.round((double) mapWidth / GOLDEN_RATIO);

            url.append("&size=").append(mapWidth).append("x").append(mapHeight);

            ImageRequestParameters.Builder paramsBuilder = new ImageRequestParameters.Builder(Uri.parse(url.toString()));
            paramsBuilder.resize(mapWidth, mapHeight);
            paramsBuilder.centerCrop(true);

            return paramsBuilder.build();
        }

        return null;
    }
}
