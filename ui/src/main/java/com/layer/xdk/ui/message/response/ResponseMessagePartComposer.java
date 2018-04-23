package com.layer.xdk.ui.message.response;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.layer.sdk.LayerClient;
import com.layer.sdk.messaging.MessagePart;
import com.layer.xdk.ui.message.MessagePartUtils;
import com.layer.xdk.ui.util.AndroidFieldNamingStrategy;

/**
 * Converts {@link ResponseMetadata} objects to {@link MessagePart}s.
 */
@SuppressWarnings("WeakerAccess")
public class ResponseMessagePartComposer {

    /**
     * Create a {@link MessagePart} from a {@link ResponseMetadata}
     *
     * @param layerClient used to create the MessagePart
     * @param responseMetadata metadata to use when populating the part
     * @return a MessagePart built from the {@link ResponseMetadata} data
     */
    public MessagePart buildResponseMessagePart(LayerClient layerClient,
            ResponseMetadata responseMetadata) {
        Gson gson = new GsonBuilder().setFieldNamingStrategy(new AndroidFieldNamingStrategy()).create();

        String rootMimeTpe = MessagePartUtils.getAsRoleRoot(ResponseMessageModel.MIME_TYPE_V2);

        return layerClient.newMessagePart(rootMimeTpe, gson.toJson(responseMetadata).getBytes());
    }
}
