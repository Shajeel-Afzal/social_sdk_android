package com.layer.xdk.ui.message.image;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;
import android.support.media.ExifInterface;

import com.layer.sdk.LayerClient;
import com.layer.sdk.messaging.Message;
import com.layer.xdk.ui.util.Log;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public abstract class ImageMessageComposer {

    private static final int PREVIEW_MAX_WIDTH = 300;
    private static final int PREVIEW_MAX_HEIGHT = 300;

    private Context mContext;
    private LayerClient mLayerClient;

    @SuppressWarnings("WeakerAccess")
    public ImageMessageComposer(Context context, LayerClient layerClient) {
        mContext = context;
        mLayerClient = layerClient;
    }

    /**
     * Returns int[] {scaledWidth, scaledHeight} for dimensions that fit within the given maxWidth,
     * maxHeight at the given inWidth, inHeight aspect ratio.  If the in dimensions fit fully inside
     * the max dimensions, no scaling is applied.  Otherwise, at least one scaled dimension is set
     * to a max dimension, and the other scaled dimension is scaled to fit.
     *
     * @param inWidth input width
     * @param inHeight input height
     * @param maxWidth max width
     * @param maxHeight max height
     * @return an int[] representing the final scaled width and height
     */
    private int[] scaleDownInside(int inWidth, int inHeight, int maxWidth, int maxHeight) {
        int scaledWidth;
        int scaledHeight;
        if (inWidth <= maxWidth && inHeight <= maxHeight) {
            scaledWidth = inWidth;
            scaledHeight = inHeight;
        } else {
            double widthRatio = (double) inWidth / (double) maxWidth;
            double heightRatio = (double) inHeight / (double) maxHeight;
            if (widthRatio > heightRatio) {
                scaledWidth = maxWidth;
                scaledHeight = (int) Math.round((double) inHeight / widthRatio);
            } else {
                scaledHeight = maxHeight;
                scaledWidth = (int) Math.round((double) inWidth / heightRatio);
            }
        }
        return new int[]{scaledWidth, scaledHeight};
    }

    @WorkerThread
    public abstract Message newImageMessage(@NonNull Uri imageUri) throws IOException;

    @WorkerThread
    public abstract Message newImageMessage(@NonNull File file) throws IOException;

    protected Context getContext() {
        return mContext;
    }

    protected LayerClient getLayerClient() {
        return mLayerClient;
    }

    protected String getPath(Context context, Uri uri) {
        Cursor cursor = context.getContentResolver().query(uri, new String[]{MediaStore.Images.Media.DATA}, null, null, null);

        try {
            // Images in the MediaStore
            if (cursor != null) {
                int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                cursor.moveToFirst();
                return cursor.getString(columnIndex);
            } else {
                // Fallback to available path in the Uri
                return uri.getPath();
            }
        } finally {
            if (cursor != null) cursor.close();
        }
    }

    protected ExifInterface getExifData(File imageFile) throws IOException {
        if (imageFile == null) throw new IllegalArgumentException("Null image file");
        if (!imageFile.exists()) throw new IllegalArgumentException("Image file does not exist");
        if (!imageFile.canRead()) throw new IllegalArgumentException("Cannot read image file");
        if (imageFile.length() <= 0) throw new IllegalArgumentException("Image file is empty");

        try {
            ExifInterface exifInterface = new ExifInterface(imageFile.getAbsolutePath());
            return exifInterface;
        } catch (IOException e) {
            if (Log.isLoggable(Log.ERROR)) {
                Log.e(e.getMessage(), e);
            }
            throw e;
        }
    }

    @SuppressWarnings("WeakerAccess")
    protected ExifInterface getExifData(@NonNull InputStream inputStream) throws IOException {
        try {
            return new ExifInterface(inputStream);
        } catch (IOException e) {
            if (Log.isLoggable(Log.ERROR)) {
                Log.e(e.getMessage(), e);
            }
            throw e;
        }
    }

    @SuppressWarnings("WeakerAccess")
    protected BitmapFactory.Options getPreviewBounds(InputStream inputStream) {
        BitmapFactory.Options bounds = new BitmapFactory.Options();
        bounds.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(inputStream, null, bounds);
        int[] previewDimensions = scaleDownInside(bounds.outWidth, bounds.outHeight, PREVIEW_MAX_WIDTH, PREVIEW_MAX_HEIGHT);
        if (Log.isLoggable(Log.VERBOSE)) {
            Log.v("Preview size: " + previewDimensions[0] + "x" + previewDimensions[1]);
        }

        // Determine sample size for preview
        int sampleSize = 1;
        int sampleWidth = bounds.outWidth;
        int sampleHeight = bounds.outHeight;
        while (sampleWidth > previewDimensions[0] && sampleHeight > previewDimensions[1]) {
            sampleWidth >>= 1;
            sampleHeight >>= 1;
            sampleSize <<= 1;
        }
        if (sampleSize != 1) sampleSize >>= 1; // Back off 1 for scale-down instead of scale-up

        BitmapFactory.Options previewOptions = new BitmapFactory.Options();
        previewOptions.inSampleSize = sampleSize;

        if (Log.isLoggable(Log.VERBOSE)) {
            Log.v("Preview sampled size: " + (sampleWidth << 1) + "x" + (sampleHeight << 1));
        }

        if (previewDimensions[0] != sampleWidth && previewDimensions[1] != sampleHeight) {
            bounds.outWidth = previewDimensions[0];
            bounds.outHeight = previewDimensions[1];
        }


        return bounds;
    }

    @SuppressWarnings("WeakerAccess")
    protected BitmapFactory.Options getBounds(InputStream inputStream) {
        BitmapFactory.Options bounds = new BitmapFactory.Options();
        bounds.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(inputStream, null, bounds);

        return bounds;
    }

    @SuppressWarnings("WeakerAccess")
    protected Bitmap getPreviewBitmap(BitmapFactory.Options bounds, InputStream inputStream) {
        // Determine preview size
        int[] previewDimensions = scaleDownInside(bounds.outWidth, bounds.outHeight, PREVIEW_MAX_WIDTH, PREVIEW_MAX_HEIGHT);
        if (Log.isLoggable(Log.VERBOSE)) {
            Log.v("Preview size: " + previewDimensions[0] + "x" + previewDimensions[1]);
        }

        // Determine sample size for preview
        int sampleSize = 1;
        int sampleWidth = bounds.outWidth;
        int sampleHeight = bounds.outHeight;
        while (sampleWidth > previewDimensions[0] && sampleHeight > previewDimensions[1]) {
            sampleWidth >>= 1;
            sampleHeight >>= 1;
            sampleSize <<= 1;
        }
        if (sampleSize != 1) sampleSize >>= 1; // Back off 1 for scale-down instead of scale-up

        BitmapFactory.Options previewOptions = new BitmapFactory.Options();
        previewOptions.inSampleSize = sampleSize;

        if (Log.isLoggable(Log.VERBOSE)) {
            Log.v("Preview sampled size: " + (sampleWidth << 1) + "x" + (sampleHeight << 1));
        }

        // Create previewBitmap if sample size and preview size are different
        Bitmap sampledBitmap = BitmapFactory.decodeStream(inputStream, null, previewOptions);
        if (previewDimensions[0] != sampleWidth && previewDimensions[1] != sampleHeight) {
            Bitmap previewBitmap = Bitmap.createScaledBitmap(sampledBitmap, previewDimensions[0], previewDimensions[1], true);
            sampledBitmap.recycle();
            return previewBitmap;
        } else {
            return sampledBitmap;
        }
    }

    @SuppressWarnings("WeakerAccess")
    @Nullable
    protected Long getFileSizeFromUri(Context context, Uri uri) {
        Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);

        try {
            if (cursor != null) {
                cursor.moveToFirst();
                return cursor.getLong(cursor.getColumnIndex(OpenableColumns.SIZE));
            } else {
                return null;
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    protected void writeStreamToFile(String filePath, InputStream inputStream) throws IOException {
        OutputStream stream = new BufferedOutputStream(new FileOutputStream(filePath));
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];
        int len;
        try {
            while ((len = inputStream.read(buffer)) != -1) {
                stream.write(buffer, 0, len);
            }
        } finally {
            stream.close();
        }
    }
}
