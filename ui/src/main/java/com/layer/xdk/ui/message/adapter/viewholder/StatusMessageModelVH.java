package com.layer.xdk.ui.message.adapter.viewholder;


import android.view.ViewGroup;

import com.layer.xdk.ui.R;
import com.layer.xdk.ui.databinding.XdkUiMessageModelVhStatusBinding;

public class StatusMessageModelVH extends
        MessageModelVH<StatusMessageModelVHModel, XdkUiMessageModelVhStatusBinding> {

    public StatusMessageModelVH(ViewGroup parent, StatusMessageModelVHModel model) {
        super(parent, R.layout.xdk_ui_message_model_vh_status, model);
        getBinding().setViewHolderModel(model);
        getBinding().executePendingBindings();
    }

    @Override
    void onBind() {
        getViewHolderModel().update();
    }
}
