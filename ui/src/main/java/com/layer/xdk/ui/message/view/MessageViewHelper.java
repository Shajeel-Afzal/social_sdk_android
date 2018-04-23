package com.layer.xdk.ui.message.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.google.gson.JsonObject;
import com.layer.xdk.ui.message.action.ActionHandlerRegistry;
import com.layer.xdk.ui.message.model.MessageModel;

public class MessageViewHelper {
    private Context mContext;
    private String mActionEvent;
    private JsonObject mActionData;

    public MessageViewHelper(Context context) {
        mContext = context;
    }

    public void performAction() {
        if (!TextUtils.isEmpty(mActionEvent)) {
            ActionHandlerRegistry.dispatchEvent(mContext, mActionEvent, mActionData);
        }
    }

    public void setMessageModel(@Nullable MessageModel model) {
        if (model == null) {
            mActionEvent = null;
            mActionData = null;
        } else {
            mActionEvent = model.getActionEvent();
            mActionData = model.getActionData();
        }
    }
}
