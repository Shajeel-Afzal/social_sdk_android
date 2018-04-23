package com.layer.xdk.ui.message.action;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.gson.JsonObject;
import com.layer.sdk.LayerClient;
import com.layer.xdk.ui.R;

public class OpenFileActionHandler extends ActionHandler {
    private static final String ACTION_EVENT_OPEN_FILE = "open-file";
    private static final String ACTION_DATA_URI = "uri";
    private static final String ACTION_DATA_FILE_MIME_TYPE = "file_mime_type";

    public OpenFileActionHandler(LayerClient layerClient) {
        super(layerClient, ACTION_EVENT_OPEN_FILE);
    }

    @Override
    public void performAction(@NonNull Context context, @Nullable JsonObject data) {
        if (data == null || data.size() == 0) return;

        if (data.has(ACTION_DATA_URI)) {
            Uri uri = Uri.parse(data.get(ACTION_DATA_URI).getAsString());
            String mimeType = data.get(ACTION_DATA_FILE_MIME_TYPE).getAsString();

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, mimeType);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            Intent chooser = Intent.createChooser(intent, context.getString(R.string.xdk_ui_open_file_action_handler_activity_picker_title));
            context.startActivity(chooser);
        }
    }
}
