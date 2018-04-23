package com.layer.xdk.ui.repository;


import android.content.Context;

import com.layer.sdk.LayerClient;
import com.layer.sdk.messaging.Conversation;
import com.layer.sdk.messaging.Message;
import com.layer.xdk.ui.message.response.ResponseSender;
import com.layer.xdk.ui.message.response.ChoiceResponseMetadata;

/**
 * Manages the sending of different message types using a {@link LayerClient}.
 */
@SuppressWarnings("UnusedReturnValue")
public class MessageSenderRepository {

    private LayerClient mLayerClient;
    private Context mContext;

    public MessageSenderRepository(Context context, LayerClient layerClient) {
        mContext = context;
        mLayerClient = layerClient;
    }

    /**
     * Send a choice response message on a given conversation.
     *
     * @param conversation conversation to send the message on
     * @param choiceResponse metadata to populate the response message
     * @return result of {@link com.layer.xdk.ui.message.sender.MessageSender#send(Message)}
     */
    public boolean sendChoiceResponse(Conversation conversation,
            ChoiceResponseMetadata choiceResponse) {
        ResponseSender responseSender = new ResponseSender(mContext, mLayerClient);
        responseSender.setConversation(conversation);
        return responseSender.sendChoiceResponse(choiceResponse);
    }
}
