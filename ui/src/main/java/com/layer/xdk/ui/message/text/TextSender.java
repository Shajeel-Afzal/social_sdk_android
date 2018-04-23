package com.layer.xdk.ui.message.text;

import android.content.Context;

import com.layer.sdk.LayerClient;
import com.layer.sdk.messaging.Identity;
import com.layer.xdk.ui.R;
import com.layer.xdk.ui.identity.IdentityFormatter;
import com.layer.xdk.ui.identity.DefaultIdentityFormatter;
import com.layer.xdk.ui.message.sender.MessageSender;

public abstract class TextSender extends MessageSender {
    private int mMaxNotificationLength;
    private IdentityFormatter mIdentityFormatter;

    @SuppressWarnings("WeakerAccess")
    public TextSender(Context context, LayerClient layerClient) {
        this(context, layerClient, 200);
    }

    @SuppressWarnings("WeakerAccess")
    public TextSender(Context context, LayerClient layerClient, int maxNotificationLength) {
        this(context, layerClient, maxNotificationLength, new DefaultIdentityFormatter(context));
    }

    @SuppressWarnings("WeakerAccess")
    public TextSender(Context context, LayerClient layerClient, int maxNotificationLength, IdentityFormatter identityFormatter) {
        super(context, layerClient);
        mMaxNotificationLength = maxNotificationLength;
        mIdentityFormatter = identityFormatter;
    }

    public abstract boolean requestSend(String text);

    public String getNotificationString(String text) {
        Identity me = getLayerClient().getAuthenticatedUser();
        String myName = me == null ? "" : mIdentityFormatter.getDisplayName(me);
        return getContext().getString(R.string.xdk_ui_notification_text, myName,
                (text.length() < mMaxNotificationLength) ?
                        text : (text.substring(0, mMaxNotificationLength) + "…"));
    }

    public int getMaxNotificationLength() {
        return mMaxNotificationLength;
    }

    public void setMaxNotificationLength(int maxNotificationLength) {
        mMaxNotificationLength = maxNotificationLength;
    }

    public void setIdentityFormatter(IdentityFormatter identityFormatter) {
        mIdentityFormatter = identityFormatter;
    }
}
