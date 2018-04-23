package com.layer.xdk.ui.message.image;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.media.ExifInterface;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.layer.sdk.LayerClient;
import com.layer.sdk.messaging.Message;
import com.layer.sdk.messaging.MessagePart;
import com.layer.xdk.ui.message.MessagePartUtils;
import com.layer.xdk.ui.util.Log;
import com.layer.xdk.ui.util.AndroidFieldNamingStrategy;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.UUID;

public class RichImageMessageComposer extends ImageMessageComposer {
    private static final String ROOT_MIME_TYPE = ImageMessageModel.ROOT_MIME_TYPE;
    private static final int PREVIEW_COMPRESSION_QUALITY = 75;

    private Gson mGson;

    public RichImageMessageComposer(Context context, LayerClient layerClient) {
        super(context, layerClient);
        mGson = new GsonBuilder().setFieldNamingStrategy(new AndroidFieldNamingStrategy()).create();
    }

    @Override
    public Message newImageMessage(@NonNull Uri imageUri) throws IOException {
        if (Log.isLoggable(Log.VERBOSE)) {
            Log.v("Creating Root part from " + imageUri.toString());
        }

        InputStream inputStream = getContext().getContentResolver().openInputStream(imageUri);
        BitmapFactory.Options bounds = getBounds(inputStream);

        inputStream = getContext().getContentResolver().openInputStream(imageUri);
        BitmapFactory.Options previewBounds = getPreviewBounds(inputStream);

        inputStream = getContext().getContentResolver().openInputStream(imageUri);
        MessagePart root = buildRootMessagePart(inputStream, bounds, previewBounds);
        UUID rootPartId = UUID.fromString(root.getId().getLastPathSegment());

        if (Log.isLoggable(Log.VERBOSE)) {
            Log.v("Creating Preview part from " + imageUri.toString());
        }
        inputStream = getContext().getContentResolver().openInputStream(imageUri);
        MessagePart preview = buildPreviewPart(inputStream, bounds, rootPartId);

        if (Log.isLoggable(Log.VERBOSE)) {
            Log.v("Creating Source part from " + imageUri.toString());
        }
        inputStream = getContext().getContentResolver().openInputStream(imageUri);
        MessagePart source = buildSourceMessagePart(inputStream, bounds,
                getFileSizeFromUri(getContext(), imageUri), rootPartId);

        if (Log.isLoggable(Log.VERBOSE)) {
            Log.v(String.format(Locale.US, "Source image bytes: %d, preview bytes: %d, root bytes: %d",
                    source.getSize(), preview.getSize(), root.getSize()));
        }

        return getLayerClient().newMessage(root, preview, source);
    }

    @Override
    public Message newImageMessage(@NonNull File file) throws IOException {
        if (file == null) throw new IllegalArgumentException("Null image file");
        if (!file.exists()) throw new FileNotFoundException("No image file");
        if (!file.canRead()) throw new IllegalArgumentException("Cannot read image file");

        BitmapFactory.Options bounds = getBounds(new FileInputStream(file.getAbsolutePath()));
        BitmapFactory.Options previewBounds = getPreviewBounds(new FileInputStream(file.getAbsolutePath()));

        if (Log.isLoggable(Log.VERBOSE)) {
            Log.v("Creating Root part from " + file.getAbsolutePath());
        }

        MessagePart root = buildRootMessagePart(new FileInputStream(file.getAbsolutePath()), bounds, previewBounds);
        UUID rootPartId = UUID.fromString(root.getId().getLastPathSegment());
        MessagePart preview = buildPreviewPart(new FileInputStream(file.getAbsolutePath()), bounds, rootPartId);
        MessagePart source = buildSourceMessagePart(new FileInputStream(file.getAbsolutePath()), bounds, file.length(), rootPartId);

        return getLayerClient().newMessage(root, preview, source);
    }

    private MessagePart buildRootMessagePart(InputStream inputStream, BitmapFactory.Options sourceBounds, BitmapFactory.Options previewBounds)
            throws IOException {
        ExifInterface exifData = getExifData(inputStream);

        ImageMessageMetadata metadata = new ImageMessageMetadata();
        metadata.mHeight = sourceBounds.outHeight;
        metadata.mWidth = sourceBounds.outWidth;
        metadata.mPreviewHeight = previewBounds.outHeight;
        metadata.mPreviewWidth = previewBounds.outWidth;
        metadata.mMimeType = sourceBounds.outMimeType;
        metadata.mOrientation = exifData.getAttributeInt(ExifInterface.TAG_ORIENTATION, 0);

        return getLayerClient().newMessagePart(MessagePartUtils.getAsRoleRoot(ROOT_MIME_TYPE),
                mGson.toJson(metadata).getBytes());
    }

    private MessagePart buildPreviewPart(InputStream inputStream, BitmapFactory.Options bounds,
            @NonNull UUID parentNodeId) throws IOException {
        Bitmap previewBitmap = getPreviewBitmap(bounds, inputStream);
        File temp = new File(getContext().getCacheDir(), getClass().getSimpleName() + "."
                + System.nanoTime() + ".jpg");
        FileOutputStream previewStream = new FileOutputStream(temp);

        if (Log.isLoggable(Log.VERBOSE)) {
            Log.v("Compressing preview to '" + temp.getAbsolutePath() + "'");
        }

        previewBitmap.compress(Bitmap.CompressFormat.JPEG, PREVIEW_COMPRESSION_QUALITY, previewStream);
        previewBitmap.recycle();
        previewStream.close();

        if (Log.isLoggable(Log.VERBOSE)) {
            Log.v("Exif orientation preserved in preview");
        }

        return getLayerClient().newMessagePart(MessagePartUtils.getAsRoleWithParentId("image/jpeg",
                "preview", parentNodeId.toString()), new FileInputStream(temp), temp.length());
    }

    private MessagePart buildSourceMessagePart(InputStream inputStream,
            BitmapFactory.Options bounds, long length, @NonNull UUID parentNodeId) {
        String mimeType = MessagePartUtils.getAsRoleWithParentId(bounds.outMimeType, "source", parentNodeId.toString());
        return getLayerClient().newMessagePart(mimeType, inputStream, length);
    }
}
