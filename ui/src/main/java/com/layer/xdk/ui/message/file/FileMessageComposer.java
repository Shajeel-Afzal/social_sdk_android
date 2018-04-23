package com.layer.xdk.ui.message.file;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.OpenableColumns;
import android.support.annotation.NonNull;
import android.webkit.MimeTypeMap;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.layer.sdk.LayerClient;
import com.layer.sdk.messaging.Message;
import com.layer.sdk.messaging.MessagePart;
import com.layer.xdk.ui.message.MessagePartUtils;
import com.layer.xdk.ui.util.AndroidFieldNamingStrategy;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.UUID;

public class FileMessageComposer {

    public Message buildFileMessage(Context context, LayerClient layerClient, Uri uri) throws FileNotFoundException {
        Gson gson = new GsonBuilder().setFieldNamingStrategy(new AndroidFieldNamingStrategy()).create();

        FileMessageMetadata metadata = getFileMetadata(context, uri);
        MessagePart rootMessagePart = layerClient.newMessagePart(MessagePartUtils.getAsRoleRoot(FileMessageModel.ROOT_MIME_TYPE),
                gson.toJson(metadata).getBytes());
        UUID rootPartId = UUID.fromString(rootMessagePart.getId().getLastPathSegment());

        InputStream inputStream = context.getContentResolver().openInputStream(uri);
        MessagePart sourceMessagePart = getSourceMessagePart(layerClient, inputStream, metadata,
                rootPartId);

        return layerClient.newMessage(rootMessagePart, sourceMessagePart);
    }

    private String getMimeType(Context context, Uri uri) {
        String mimeType;
        if (uri.getScheme().equals(ContentResolver.SCHEME_CONTENT)) {
            ContentResolver cr = context.getContentResolver();
            mimeType = cr.getType(uri);
        } else {
            String fileExtension = MimeTypeMap.getFileExtensionFromUrl(uri
                    .toString());
            mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(
                    fileExtension.toLowerCase());
        }
        return mimeType;
    }

    @NonNull
    private FileMessageMetadata getFileMetadata(@NonNull Context context, @NonNull Uri uri) throws FileNotFoundException {
        FileMessageMetadata metadata = new FileMessageMetadata();
        String fileName = null;
        Long fileSize = null;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            File file = new File(uri.getPath());
            fileName = file.getName();
            fileSize = file.length();
        } else {
            Cursor cursor = context.getContentResolver().query(uri, null, null,
                    null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    fileName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));

                    int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
                    // If the size is unknown, the value stored is null.  But since an
                    // int can't be null in Java, the behavior is implementation-specific,
                    // which is just a fancy term for "unpredictable".  So as
                    // a rule, check if it's null before assigning to an int.  This will
                    // happen often:  The storage API allows for remote files, whose
                    // size might not be locally known.
                    if (!cursor.isNull(sizeIndex)) {
                        // Technically the column stores an int, but cursor.getString()
                        // will do the conversion automatically.
                        String size = cursor.getString(sizeIndex);
                        fileSize = Long.parseLong(size);
                    }
                }
            } finally {
                cursor.close();
            }

        }

        metadata.mTitle = fileName;
        metadata.mSize = fileSize;
        metadata.mMimeType = getMimeType(context, uri);
        return metadata;
    }

    private MessagePart getSourceMessagePart(LayerClient layerClient, InputStream inputStream,
            FileMessageMetadata metadata, @NonNull UUID parentNodeId) {
        String mimeType = MessagePartUtils.getAsRoleWithParentId(metadata.mMimeType, "source",
                parentNodeId.toString());
        return layerClient.newMessagePart(mimeType, inputStream, metadata.mSize);
    }
}
