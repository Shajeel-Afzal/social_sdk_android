package com.sumatodev.social_chat_sdk.main.enums;

public enum MessageType {

    SENT(55), RECEIVE(66);

    private final int messageType;

    MessageType(int messageType) {
        this.messageType = messageType;
    }

    public int getMessageType() {
        return messageType;
    }
}
