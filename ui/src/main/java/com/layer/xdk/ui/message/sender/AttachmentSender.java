package com.layer.xdk.ui.message.sender;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Parcelable;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.layer.sdk.LayerClient;

import static android.support.v4.content.ContextCompat.checkSelfPermission;

/**
 * AttachmentSenders populate the ComposeBar attachment menu and handle message sending
 * requests.  AttachmentSenders can interact with the Activity lifecycle to preserve instance state
 * and receive activity results when needed.
 */
public abstract class AttachmentSender extends MessageSender {
    private final String mTitle;
    private final Integer mIcon;

    public AttachmentSender(Context context, LayerClient layerClient, String title, Integer icon) {
        super(context, layerClient);
        mTitle = title;
        mIcon = icon;
    }

    /**
     * Begins an attachment sending operation.  This might launch an Intent for selecting from a
     * gallery, taking a camera photo, or simply sending a message of the given type.  If an Intent
     * is generated for a result, consider overriding onActivityResult().
     *
     * @return `true` if a send operation is started, or `false` otherwise.
     * @see #onActivityResult(Activity, int, int, Intent)
     */
    public abstract boolean requestSend();

    /**
     * Override to save instance state.
     *
     * @return new saved instance state.
     * @see #onRestoreInstanceState(Parcelable)
     */
    public Parcelable onSaveInstanceState() {
        // Optional override
        return null;
    }

    /**
     * Override if saved instance state is required.
     *
     * @param state State previously created with onSaveInstanceState().
     * @see #onSaveInstanceState()
     */
    public void onRestoreInstanceState(Parcelable state) {
        // Optional override
    }

    /**
     * Override to handle results from onActivityResult.
     *
     * @return true if the result was handled, or false otherwise.
     */
    @MainThread
    public boolean onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
        // Optional override
        return false;
    }

    /**
     * Override to handle results from onRequestPermissionsResult.
     */
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        // Optional override
    }

    /**
     * Returns the mTitle for this AttachmentSender, typically for use in the ComposeBar
     * attachment menu.
     *
     * @return The mTitle for this AttachmentSender.
     */
    public String getTitle() {
        return mTitle;
    }

    /**
     * Returns the icon resource ID for this AttachmentSender, typically for use in the
     * ComposeBar attachment menu, or `null` for none.
     *
     * @return The icon resource ID for this AttachmentSender.
     */
    public Integer getIcon() {
        return mIcon;
    }


    /**
     *
     * Convenience method to check if a set of permissions have been granted
     */
    protected boolean hasPermissions(@NonNull Activity activity, String... permissions) {
        for (String permission: permissions) {
            if (TextUtils.isEmpty(permission)) continue;

            if (checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }

        return true;
    }

    protected void requestPermissions (@NonNull Activity activity, final int permissionsCode, String... permissions) {
        android.support.v4.app.ActivityCompat.requestPermissions(activity, permissions, permissionsCode);
    }
}
