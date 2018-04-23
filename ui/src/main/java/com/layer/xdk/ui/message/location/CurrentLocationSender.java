package com.layer.xdk.ui.message.location;

import static android.support.v4.content.ContextCompat.checkSelfPermission;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresPermission;
import android.support.annotation.StringRes;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.layer.sdk.LayerClient;
import com.layer.sdk.messaging.Identity;
import com.layer.sdk.messaging.Message;
import com.layer.sdk.messaging.MessageOptions;
import com.layer.sdk.messaging.MessagePart;
import com.layer.sdk.messaging.PushNotificationPayload;
import com.layer.xdk.ui.R;
import com.layer.xdk.ui.identity.IdentityFormatter;
import com.layer.xdk.ui.message.MessagePartUtils;
import com.layer.xdk.ui.message.sender.AttachmentSender;
import com.layer.xdk.ui.util.Log;
import com.layer.xdk.ui.util.AndroidFieldNamingStrategy;

import java.lang.ref.WeakReference;

/**
 * CurrentLocationSender creates a Location Message with the user's latest location.
 * Google's fused location API is used for gathering location at send time and may trigger a dialog
 * for updating Google Play Services.  Requires @{link Manifest.permission.ACCESS_FINE_LOCATION}
 * for getting device location.
 */
public class CurrentLocationSender extends AttachmentSender {
    private static final String PERMISSION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final int ACTIVITY_REQUEST_CODE = 30;
    private static final int PERMISSION_REQUEST_CODE = 31;

    private static final int NUMBER_OF_LOCATION_UPDATES = 1;
    private static final int MAX_WAIT_TIME = 10000;
    private static final int EXPIRATION_DURATION = 1000;

    private static GoogleApiClient sGoogleApiClient;

    private WeakReference<Activity> mActivity = new WeakReference<>(null);
    private IdentityFormatter mIdentityFormatter;
    private Gson mGson;

    public CurrentLocationSender(@StringRes int titleResId, @DrawableRes int iconResId,
                                 @NonNull Activity activity, @NonNull LayerClient layerClient,
                                 @NonNull IdentityFormatter identityFormatter) {
        this(activity.getString(titleResId), iconResId, activity, layerClient, identityFormatter);
    }

    @SuppressWarnings("WeakerAccess")
    public CurrentLocationSender(String title, @DrawableRes int iconResId, @NonNull Activity activity,
                                 @NonNull LayerClient layerClient,
                                 @NonNull IdentityFormatter identityFormatter) {
        super(activity.getApplicationContext(), layerClient, title, iconResId);
        mActivity = new WeakReference<>(activity);
        mIdentityFormatter = identityFormatter;
        mGson = new GsonBuilder().setFieldNamingStrategy(new AndroidFieldNamingStrategy()).create();

        init(activity);
    }

    private void init(final Activity activity) {
        // If the client has already been created, ensure connected and return.
        if (sGoogleApiClient != null) {
            if (!sGoogleApiClient.isConnected()) sGoogleApiClient.connect();
            return;
        }

        int errorCode = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(activity);

        // If the correct Google Play Services are available, connect and return.
        if (errorCode == ConnectionResult.SUCCESS) {
            GoogleApiCallbacks googleApiCallbacks = new GoogleApiCallbacks();
            sGoogleApiClient = new GoogleApiClient.Builder(activity.getApplicationContext())
                    .addConnectionCallbacks(googleApiCallbacks)
                    .addOnConnectionFailedListener(googleApiCallbacks)
                    .addApi(LocationServices.API)
                    .build();
            sGoogleApiClient.connect();
            return;
        }

        // If the correct Google Play Services are not available, redirect to proper solution.
        if (GoogleApiAvailability.getInstance().isUserResolvableError(errorCode)) {
            GoogleApiAvailability.getInstance()
                    .getErrorDialog(activity, errorCode, ACTIVITY_REQUEST_CODE)
                    .show();
            return;
        }

        if (Log.isLoggable(Log.ERROR)) Log.e("Cannot update Google Play Services: " + errorCode);
    }

    @RequiresPermission(PERMISSION)
    private static boolean getFreshLocation(LocationListener listener) {
        if (sGoogleApiClient == null) {
            if (Log.isLoggable(Log.ERROR)) Log.e("GoogleApiClient not initialized");
            return false;
        }
        if (Log.isLoggable(Log.VERBOSE)) Log.v("Getting fresh location");
        LocationRequest r = new LocationRequest()
                .setNumUpdates(NUMBER_OF_LOCATION_UPDATES)
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setExpirationDuration(EXPIRATION_DURATION)
                .setMaxWaitTime(MAX_WAIT_TIME);
        try {
            LocationServices.FusedLocationApi.requestLocationUpdates(sGoogleApiClient, r, listener);
            return true;
        } catch (IllegalStateException e) {
            if (Log.isLoggable(Log.ERROR)) {
                Log.e(e.getMessage(), e);
            }
        }
        return false;
    }

    @RequiresPermission(PERMISSION)
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode != PERMISSION_REQUEST_CODE) return;
        if (grantResults.length == 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
            if (Log.isLoggable(Log.VERBOSE)) Log.v("Location permission denied");
            return;
        }
        getFreshLocation(new SenderLocationListener(this));
    }

    /**
     * Asynchronously requests a fresh location and sends a location Message.
     * @return `true` if a send operation is started, or `false` otherwise.
     */
    @RequiresPermission(PERMISSION)
    @Override
    public boolean requestSend() {
        Activity activity = mActivity.get();
        if (activity == null) return false;
        if (Log.isLoggable(Log.VERBOSE)) Log.v("Sending location");
        if (checkSelfPermission(activity, PERMISSION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(activity, PERMISSION_REQUEST_CODE, PERMISSION);
            return true;
        }
        return getFreshLocation(new SenderLocationListener(this));
    }

    @Override
    public boolean onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
        if (requestCode != ACTIVITY_REQUEST_CODE) return false;
        init(activity);
        return true;
    }

    private static class SenderLocationListener implements LocationListener {
        private final WeakReference<CurrentLocationSender> mCurrentLocationSenderWeakReference;

        private SenderLocationListener(CurrentLocationSender locationSender) {
            mCurrentLocationSenderWeakReference = new WeakReference<>(locationSender);
        }

        @Override
        public void onLocationChanged(Location location) {
            if (Log.isLoggable(Log.VERBOSE)) Log.v("Got fresh location");

            if (Log.isPerfLoggable()) {
                Log.perf("LocationSender is attempting to send a message");
            }
            CurrentLocationSender sender = mCurrentLocationSenderWeakReference.get();
            if (sender == null) return;
            Context context = sender.getContext();
            LayerClient client = sender.getLayerClient();

            Identity me = client.getAuthenticatedUser();
            String myName = me == null ? "" : sender.mIdentityFormatter.getDisplayName(me);

            LocationMessageMetadata locationMessageMetadata = new LocationMessageMetadata();
            locationMessageMetadata.mLatitude = location.getLatitude();
            locationMessageMetadata.mLongitude = location.getLongitude();

            String notification = context.getString(R.string.xdk_ui_notification_location, myName);
            String mimeType = MessagePartUtils.getAsRoleRoot(LocationMessageModel.ROOT_MIME_TYPE);

            MessagePart part = client.newMessagePart(mimeType, sender.mGson.toJson(locationMessageMetadata).getBytes());
            PushNotificationPayload payload = new PushNotificationPayload.Builder()
                    .text(notification)
                    .build();
            Message message = client.newMessage(new MessageOptions().defaultPushNotificationPayload(payload), part);

            sender.send(message);
        }
    }

    private static class GoogleApiCallbacks implements GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks {
        @Override
        public void onConnected(Bundle bundle) {
            if (Log.isLoggable(Log.VERBOSE)) Log.v("GoogleApiClient connected");
        }

        @Override
        public void onConnectionSuspended(int i) {
            if (Log.isLoggable(Log.VERBOSE)) Log.v("GoogleApiClient suspended");
        }

        @Override
        public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
            if (Log.isLoggable(Log.VERBOSE)) Log.v("GoogleApiClient failed: " + connectionResult);
        }
    }
}
