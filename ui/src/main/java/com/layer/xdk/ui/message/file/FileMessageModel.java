package com.layer.xdk.ui.message.file;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.databinding.Bindable;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;

import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;
import com.layer.sdk.LayerClient;
import com.layer.sdk.messaging.Message;
import com.layer.sdk.messaging.MessagePart;
import com.layer.xdk.ui.R;
import com.layer.xdk.ui.message.MessagePartUtils;
import com.layer.xdk.ui.message.model.MessageModel;
import com.layer.xdk.ui.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class FileMessageModel extends MessageModel {
    public static final String ROOT_MIME_TYPE = "application/vnd.layer.file+json";
    private static final String ROLE_SOURCE = "source";

    private static final String ACTION_EVENT_OPEN_FILE = "open-file";
    private static final String ACTION_DATA_URI = "uri";
    private static final String ACTION_DATA_FILE_MIME_TYPE = "file_mime_type";

    private static final List<String> PDF_MIME_TYPES = Collections.singletonList("application/pdf");
    private static final List<String> AUDIO_MIME_TYPES = Arrays.asList("application/ogg", "audio/mpeg",
            "audio/wav", "audio/aac", "audio/mp3", "audio/mp4");
    private static final List<String> TEXT_MIME_TYPES = Arrays.asList("text/plain", "application/msword", "text/html");
    private static final List<String> ZIP_MIME_TYPES = Arrays.asList("application-zip",
            "application/x-tar", "application/x-bzip2", "application/gzip", "application/x-apple-diskimage");
    private static final List<String> IMAGE_MIME_TYPES = Arrays.asList("image/png", "image/jpeg",
            "image/jpg", "image/gif", "image/svg", "image/tiff", "image/bmp");

    private FileMessageMetadata mMetadata;
    @DrawableRes
    private int mFileIconDrawable;

    private String mFileProviderAuthority;

    public FileMessageModel(@NonNull Context context, @NonNull LayerClient layerClient,
                            @NonNull Message message) {
        super(context, layerClient, message);
        mFileProviderAuthority = context.getPackageName() + ".file_provider";
    }

    @Override
    public int getViewLayoutId() {
        return R.layout.xdk_ui_file_message_view;
    }

    @Override
    public int getContainerViewLayoutId() {
        return R.layout.xdk_ui_standard_message_container;
    }

    @Override
    protected void parse(@NonNull MessagePart messagePart) {
        JsonReader reader;
        InputStreamReader inputStreamReader = new InputStreamReader(messagePart.getDataStream());
        reader = new JsonReader(inputStreamReader);
        mMetadata = getGson().fromJson(reader, FileMessageMetadata.class);
        setupFileIconDrawable(mMetadata.mMimeType);
        try {
            inputStreamReader.close();
        } catch (IOException e) {
            if (Log.isLoggable(Log.ERROR)) {
                Log.e("Failed to close input stream while parsing file message", e);
            }
        }
    }

    @Override
    protected boolean shouldDownloadContentIfNotReady(@NonNull MessagePart messagePart) {
        return true;
    }

    @Nullable
    @Override
    public String getTitle() {
        return mMetadata != null ? mMetadata.mTitle : null;
    }

    @Nullable
    @Override
    public String getDescription() {
        return mMetadata != null ? mMetadata.mAuthor : null;
    }

    @Nullable
    @Override
    public String getFooter() {
        if (mMetadata != null && mMetadata.mSize > 0) {
            return "" + (mMetadata.mSize / 1024) + " KB";
        }
        return null;
    }

    @Override
    public String getActionEvent() {
        if (super.getActionEvent() != null) {
            return super.getActionEvent();
        }

        if (mMetadata != null && mMetadata.mAction != null) {
            return mMetadata.mAction.getEvent();
        }

        return ACTION_EVENT_OPEN_FILE;
    }

    @NonNull
    @Override
    public JsonObject getActionData() {
        if (super.getActionData().size() > 0) {
            return super.getActionData();
        }

        JsonObject jsonObject = new JsonObject();
        if (mMetadata == null) {
            return jsonObject;
        }
        jsonObject.addProperty(ACTION_DATA_FILE_MIME_TYPE, mMetadata.mMimeType);
        if (getHasSourceMessagePart()) {
            MessagePart sourcePart = MessagePartUtils.getMessagePartWithRole(getMessage(), ROLE_SOURCE);

            if (sourcePart == null || sourcePart.getDataStream() == null) {
                return new JsonObject();
            }

            String filePath;
            try {
                filePath = writeDataToFile(sourcePart.getDataStream());
            } catch (IOException e) {
                // TODO : AND-1235 How should this error be exposed?
                return new JsonObject();
            }

            jsonObject.addProperty(ACTION_DATA_URI, filePath);
        } else {
            jsonObject.addProperty(ACTION_DATA_URI, mMetadata.mSourceUrl);
        }

        return jsonObject;
    }

    @Nullable
    @Override
    public String getPreviewText() {
        if (getHasContent()) {
            if (mMetadata.mTitle != null) {
                return mMetadata.mTitle;
            }
        }
        return getAppContext().getString(R.string.xdk_ui_file_message_preview_text);
    }

    @Override
    public int getBackgroundColor() {
        return R.color.xdk_ui_color_primary_gray;
    }

    @Override
    public boolean getHasContent() {
        return mMetadata != null;
    }

    @Bindable
    @DrawableRes
    public int getFileIconDrawable() {
        return mFileIconDrawable;
    }

    private void setupFileIconDrawable(@Nullable String sourceMimeType) {
        mFileIconDrawable = R.drawable.xdk_ui_file_generic;

        if (!TextUtils.isEmpty(sourceMimeType)) {
            if (TEXT_MIME_TYPES.contains(sourceMimeType)) {
                mFileIconDrawable = R.drawable.xdk_ui_file_text;
            } else if (PDF_MIME_TYPES.contains(sourceMimeType)) {
                mFileIconDrawable = R.drawable.xdk_ui_file_pdf;
            } else if (AUDIO_MIME_TYPES.contains(sourceMimeType)) {
                mFileIconDrawable = R.drawable.xdk_ui_file_audio;
            } else if (IMAGE_MIME_TYPES.contains(sourceMimeType)) {
                mFileIconDrawable = R.drawable.xdk_ui_file_image;
            } else if (ZIP_MIME_TYPES.contains(sourceMimeType)) {
                mFileIconDrawable = R.drawable.xdk_ui_file_zip;
            }
        }
    }

    private boolean getHasSourceMessagePart() {
        return getHasContent() && MessagePartUtils.hasMessagePartWithRole(getMessage(), ROLE_SOURCE);
    }

    private String writeDataToFile(@NonNull InputStream inputStream) throws IOException {
        String appName = getApplicationName(getAppContext());
        File storageDirectory = new File(getPublicStorageDirectoryForFileDownload(mMetadata.mMimeType), appName);
        if (!storageDirectory.exists() && !storageDirectory.mkdirs()) {
            throw new IllegalStateException("Unable to write to storage directory");
        }

        // Accounting for the possibility that no title is present, try and provide a unique file name
        String fileName = mMetadata.mTitle != null ? mMetadata.mTitle : (appName + "_file_" + UUID.randomUUID());

        File file = new File(storageDirectory, fileName);
        OutputStream output = new FileOutputStream(file);
        byte[] buffer = new byte[4 * 1024];
        int read;

        try {
            while ((read = inputStream.read(buffer)) != -1) {
                output.write(buffer, 0, read);
            }

            output.flush();
        } finally {
            output.close();
            inputStream.close();
        }

        return FileProvider.getUriForFile(getAppContext(), mFileProviderAuthority, file).toString();
    }

    private File getPublicStorageDirectoryForFileDownload(@Nullable String sourceMimeType) {
        String directory = Environment.DIRECTORY_DOWNLOADS;

        if (!TextUtils.isEmpty(sourceMimeType)) {
            if (TEXT_MIME_TYPES.contains(sourceMimeType) || PDF_MIME_TYPES.contains(sourceMimeType)) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    directory = Environment.DIRECTORY_DOCUMENTS;
                }
            } else if (AUDIO_MIME_TYPES.contains(sourceMimeType)) {
                directory = Environment.DIRECTORY_MUSIC;
            } else if (IMAGE_MIME_TYPES.contains(sourceMimeType)) {
                directory = Environment.DIRECTORY_PICTURES;
            }
        }

        return getAppContext().getExternalFilesDir(directory);
    }

    private String getApplicationName(Context context) {
        ApplicationInfo applicationInfo = context.getApplicationInfo();
        int stringId = applicationInfo.labelRes;
        return stringId == 0 ? applicationInfo.nonLocalizedLabel.toString() : context.getString(stringId);
    }
}