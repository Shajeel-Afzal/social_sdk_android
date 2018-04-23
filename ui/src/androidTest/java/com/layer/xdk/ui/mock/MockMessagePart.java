package com.layer.xdk.ui.mock;

import android.net.Uri;
import android.support.annotation.Nullable;

import com.layer.sdk.listeners.LayerProgressListener;
import com.layer.sdk.messaging.Message;
import com.layer.sdk.messaging.MessagePart;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Date;

public class MockMessagePart extends MessagePart {
    private static int sInstanceCount = 0;
    private final byte[] mContent;
    private final String mMimeType;
    private final Uri mId;

    public MockMessagePart(byte[] content, String mimeType) {
        sInstanceCount++;
        mId = Uri.parse("mockmessagepart:///" + sInstanceCount);
        mContent = content;
        mMimeType = mimeType;
    }

    @Override
    public Uri getId() {
        return mId;
    }

    @Override
    public Message getMessage() {
        return null;
    }

    @Override
    public String getMimeType() {
        return mMimeType;
    }

    @Override
    public byte[] getData() {
        return mContent;
    }

    @Override
    public InputStream getDataStream() {
        return new ByteArrayInputStream(getData());
    }

    @Override
    public long getSize() {
        return mContent.length;
    }

    @Override
    public TransferStatus getTransferStatus() {
        return TransferStatus.COMPLETE;
    }

    @Override
    public void download(LayerProgressListener layerProgressListener) {
        layerProgressListener.onProgressComplete(this, LayerProgressListener.Operation.DOWNLOAD);
    }

    @Override
    public boolean isContentReady() {
        return true;
    }

    @Override
    public void deleteLocalContent() {

    }

    @Nullable
    @Override
    public Date getUpdatedAt() {
        return null;
    }
}
