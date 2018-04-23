package com.layer.xdk.ui.message.status;


import android.support.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.layer.sdk.LayerClient;
import com.layer.sdk.messaging.MessagePart;
import com.layer.xdk.ui.message.MessagePartUtils;
import com.layer.xdk.ui.util.AndroidFieldNamingStrategy;

import java.util.UUID;

/**
 * Creates a {@link MessagePart}s for sending a status message.
 */
public class StatusMessageComposer {
    private static final String MIME_TYPE_ROLE = "status";
    /**
     * Create a {@link MessagePart} to use for a status message. This is used to add the status to
     * another response message part, hence the 'parentNodeId'.
     *
     * @param layerClient used to create the MessagePart
     * @param status text to display
     * @param parentNodeId the root part's ID
     * @return a MessagePart built from the status string
     */
    public MessagePart buildStatusMessagePart(LayerClient layerClient, String status,
            @NonNull UUID parentNodeId) {
        Gson gson = new GsonBuilder()
                .setFieldNamingStrategy(new AndroidFieldNamingStrategy())
                .create();

        String statusMimeType = MessagePartUtils.getAsRoleWithParentId(
                StatusMessageModel.MIME_TYPE,
                MIME_TYPE_ROLE,
                parentNodeId.toString());
        StatusMessageMetadata statusMetadata = new StatusMessageMetadata();
        statusMetadata.mText = status;
        return layerClient.newMessagePart(statusMimeType, gson.toJson(statusMetadata).getBytes());
    }

    // TODO AND-1114 Add root part creation
}
