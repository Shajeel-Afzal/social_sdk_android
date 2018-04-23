package com.layer.xdk.ui.message.adapter.viewholder;


import android.content.Context;
import android.databinding.Bindable;

import com.layer.sdk.LayerClient;
import com.layer.xdk.ui.identity.IdentityFormatter;
import com.layer.xdk.ui.message.model.MessageModel;
import com.layer.xdk.ui.message.response.ResponseMessageModel;
import com.layer.xdk.ui.message.status.StatusMessageModel;
import com.layer.xdk.ui.util.DateFormatter;
import com.layer.xdk.ui.util.Log;

import javax.inject.Inject;

@SuppressWarnings("WeakerAccess")
public class StatusMessageModelVHModel extends MessageModelVHModel {
    private boolean mEnableReadReceipts;
    private boolean mVisible;
    private CharSequence mText;

    @Inject
    public StatusMessageModelVHModel(Context context, LayerClient layerClient,
            IdentityFormatter identityFormatter,
            DateFormatter dateFormatter) {
        super(context, layerClient, identityFormatter, dateFormatter);
    }

    public void update() {
        if (getItem() instanceof ResponseMessageModel) {
            ResponseMessageModel responseModel = (ResponseMessageModel) getItem();
            mText = responseModel.getText();
        } else if (getItem() instanceof StatusMessageModel) {
            mText = ((StatusMessageModel) getItem()).getText();
        } else {
            if (Log.isLoggable(Log.ERROR)) {
                Log.e("Expecting either a Response or Status message model");
            }
            throw new IllegalStateException("Expecting either a Response or Status message model");
        }
        mVisible = getItem().getHasContent();

        if (!getItem().isMessageFromMe() && mEnableReadReceipts) {
            getItem().getMessage().markAsRead();
        }
        notifyChange();
    }

    public void setEnableReadReceipts(boolean enableReadReceipts) {
        mEnableReadReceipts = enableReadReceipts;
    }

    @Bindable
    public CharSequence getText() {
        return mText;
    }

    @Bindable
    public boolean isVisible() {
        return mVisible;
    }
}
