package com.layer.xdk.ui.message.file;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;

import com.layer.sdk.LayerClient;
import com.layer.sdk.messaging.Identity;
import com.layer.sdk.messaging.Message;
import com.layer.sdk.messaging.PushNotificationPayload;
import com.layer.xdk.ui.R;
import com.layer.xdk.ui.message.sender.AttachmentSender;
import com.layer.xdk.ui.util.Log;
import com.layer.xdk.ui.util.Util;

import java.io.FileNotFoundException;
import java.lang.ref.WeakReference;

public class FileSender extends AttachmentSender {

    private static final String PERMISSION_READ = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) ? Manifest.permission.READ_EXTERNAL_STORAGE : null;
    private static final int ACTIVITY_REQUEST_CODE = 40;
    private static final int PERMISSION_REQUEST_CODE = 111;

    private WeakReference<Activity> mActivity;
    private FileMessageComposer mFileMessageComposer = new FileMessageComposer();

    public FileSender(Activity activity, LayerClient layerClient, @StringRes int titleId) {
        this(activity, layerClient, activity.getString(titleId), R.drawable.ic_insert_drive_file_black_24dp);
    }

    @SuppressWarnings("WeakerAccess")
    public FileSender(Activity activity, LayerClient layerClient, String title, @DrawableRes Integer icon) {
        super(activity.getApplicationContext(), layerClient, title, icon);
        mActivity = new WeakReference<>(activity);
    }

    @Override
    public boolean requestSend() {
        Activity activity = mActivity.get();
        if (activity == null) return false;

        if (Log.isLoggable(Log.VERBOSE)) Log.v("Checking permissions");

        if (!hasPermissions(activity, PERMISSION_READ)) {
            requestPermissions(activity, PERMISSION_REQUEST_CODE, PERMISSION_READ);
        } else {
            startFilePickerIntent(activity);
        }

        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode != PERMISSION_REQUEST_CODE) return;

        if (grantResults.length != 1) {
            if (Log.isLoggable(Log.VERBOSE)) Log.v("One or more required permissions denied");
            return;
        }

        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (Log.isLoggable(Log.VERBOSE)) Log.v("Starting file picker");
            Activity activity = mActivity.get();
            if (activity == null) return;
            startFilePickerIntent(activity);
        }
    }

    private void startFilePickerIntent(Activity activity) {
        Intent intent = new Intent();

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            intent.setAction(Intent.ACTION_GET_CONTENT);
        } else {
            intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
        }

        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        activity.startActivityForResult(intent, ACTIVITY_REQUEST_CODE);
    }

    @Override
    public boolean onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
        if (requestCode != ACTIVITY_REQUEST_CODE) return false;
        if (resultCode != Activity.RESULT_OK) {
            if (Log.isLoggable(Log.ERROR)) Log.e("Result: " + requestCode + ", data: " + data);
            return true;
        }
        if (Log.isLoggable(Log.VERBOSE)) Log.v("Received file picker response");

        if (data == null || data.getData() == null) {
            if (Log.isLoggable(Log.DEBUG)) Log.d("Received null data");
            return true;
        } else {
            Uri uri;
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
                String filePath = data.getData().getPath();
                uri = Uri.parse(filePath);
            } else {
                uri = data.getData();
            }

            if (Log.isLoggable(Log.VERBOSE)) Log.v("Uri: " + uri.toString());

            Identity me = getLayerClient().getAuthenticatedUser();
            String myName = me == null ? "" : Util.getDisplayName(me);
            PushNotificationPayload payload = new PushNotificationPayload.Builder()
                    .text(getContext().getString(R.string.xdk_ui_notification_image, myName))
                    .build();

            Message message;
            try {
                message = mFileMessageComposer.buildFileMessage(getContext(), getLayerClient(), uri);
                message.getOptions().defaultPushNotificationPayload(payload);
                send(message);
            } catch (FileNotFoundException e) {
                throw new IllegalStateException("File not found: " + e.getMessage());
            }

            return true;
        }
    }
}
