package com.layer.xdk.ui.message.image.popup;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.media.ExifInterface;
import android.support.v4.widget.ContentLoadingProgressBar;

import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.layer.sdk.LayerClient;
import com.layer.sdk.listeners.LayerProgressListener;
import com.layer.sdk.messaging.MessagePart;
import com.layer.xdk.ui.R;
import com.layer.xdk.ui.message.image.cache.ImageCacheWrapper;
import com.layer.xdk.ui.util.Log;

import java.io.Serializable;

/**
 * ImagePopupActivity implements a ful resolution image viewer Activity.  This Activity
 * registers with the LayerClient as a LayerProgressListener to monitor progress.
 */
public class ImagePopupActivity extends Activity implements LayerProgressListener.BackgroundThread.Weak, SubsamplingScaleImageView.OnImageEventListener {
    private static LayerClient sLayerClient;
    private static ImageCacheWrapper sImageCacheWrapper;

    public static final String EXTRA_PARAMS = "extra_params";

    private SubsamplingScaleImageView mImageView;
    private ContentLoadingProgressBar mProgressBar;
    private Uri mMessagePartId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setBackgroundDrawableResource(R.color.xdk_ui_image_popup_background);
        setContentView(R.layout.xdk_ui_image_popup);
        mImageView = findViewById(R.id.image_popup);
        mProgressBar = findViewById(R.id.image_popup_progress);

        mImageView.setPanEnabled(true);
        mImageView.setZoomEnabled(true);
        mImageView.setDoubleTapZoomDpi(160);
        mImageView.setMinimumDpi(80);
        mImageView.setBitmapDecoderClass(MessagePartDecoder.class);
        mImageView.setRegionDecoderClass(MessagePartRegionDecoder.class);

        Intent intent = getIntent();
        if (intent == null || intent.getExtras() == null) {
            return;
        }
        Parameters parameters = (Parameters) intent.getExtras().getSerializable(EXTRA_PARAMS);
        displayImage(parameters);
    }

    @Override
    protected void onResume() {
        super.onResume();
        sLayerClient.registerProgressListener(null, this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sLayerClient.unregisterProgressListener(null, this);
    }

    public static void init(LayerClient layerClient, ImageCacheWrapper imageCacheWrapper) {
        sLayerClient = layerClient;
        MessagePartDecoder.init(layerClient, imageCacheWrapper);
        MessagePartRegionDecoder.init(layerClient);
    }

    @SuppressWarnings("SuspiciousNameCombination")
    private void displayImage(Parameters parameters) {
        mMessagePartId = Uri.parse(parameters.mSourceUri);
        mProgressBar.show();
        int orientation;
        Uri uri = Uri.parse(parameters.mPreviewUri != null ? parameters.mPreviewUri : parameters.mSourceUri);
        int width;
        int height;
        switch (parameters.mOrientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                orientation = SubsamplingScaleImageView.ORIENTATION_90;
                width = parameters.mWidth;
                height = parameters.mHeight;
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                orientation = SubsamplingScaleImageView.ORIENTATION_180;
                width = parameters.mHeight;
                height = parameters.mWidth;
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                orientation = SubsamplingScaleImageView.ORIENTATION_270;
                width = parameters.mHeight;
                height = parameters.mWidth;
                break;
            case ExifInterface.ORIENTATION_NORMAL:
            default:
                orientation = SubsamplingScaleImageView.ORIENTATION_0;
                width = parameters.mWidth;
                height = parameters.mHeight;
                break;
        }

        ImageSource preview;
        ImageSource source;
        if (width == 0 || height == 0) {
            preview = null;
            source = ImageSource.uri(uri);
        } else {
            preview = ImageSource.uri(uri);
            source = ImageSource.uri(mMessagePartId).dimensions(width, height);
        }
        if (!"layer".equals(uri.getScheme())) {
            // Not a Layer MessagePart. Disable tiling as we don't know the size
            if (preview != null) {
                preview.tilingDisabled();
            }
            source.tilingDisabled();
        }
        mImageView.setImage(source, preview);

        mImageView.setOrientation(orientation);
        mImageView.setOnImageEventListener(this);
    }

    //==============================================================================================
    // SubsamplingScaleImageView.OnImageEventListener: hide progress bar when full part loaded
    //==============================================================================================

    @Override
    public void onReady() {

    }

    @Override
    public void onImageLoaded() {
        mProgressBar.hide();
    }

    @Override
    public void onPreviewLoadError(Exception e) {
        if (Log.isLoggable(Log.ERROR)) Log.e(e.getMessage(), e);
        mProgressBar.hide();
    }

    @Override
    public void onImageLoadError(Exception e) {
        if (Log.isLoggable(Log.ERROR)) Log.e(e.getMessage(), e);
        mProgressBar.hide();
    }

    @Override
    public void onTileLoadError(Exception e) {
        if (Log.isLoggable(Log.ERROR)) Log.e(e.getMessage(), e);
        mProgressBar.hide();
    }

    @Override
    public void onPreviewReleased() {

    }


    //==============================================================================================
    // LayerProgressListener: update progress bar while downloading
    //==============================================================================================

    @Override
    public void onProgressStart(MessagePart messagePart, Operation operation) {
        if (!messagePart.getId().equals(mMessagePartId)) return;
        mProgressBar.setProgress(0);
    }

    @Override
    public void onProgressUpdate(MessagePart messagePart, Operation operation, long bytes) {
        if (!messagePart.getId().equals(mMessagePartId)) return;
        double fraction = (double) bytes / (double) messagePart.getSize();
        int progress = (int) Math.round(fraction * mProgressBar.getMax());
        mProgressBar.setProgress(progress);
    }

    @Override
    public void onProgressComplete(MessagePart messagePart, Operation operation) {
        if (!messagePart.getId().equals(mMessagePartId)) return;
        mProgressBar.setProgress(mProgressBar.getMax());
    }

    @Override
    public void onProgressError(MessagePart messagePart, Operation operation, Throwable e) {
        if (!messagePart.getId().equals(mMessagePartId)) return;
        if (Log.isLoggable(Log.ERROR)) Log.e(e.getMessage(), e);
    }

    public static class Parameters implements Serializable {
        private String mSourceUri;
        private String mPreviewUri;
        private int mWidth;
        private int mHeight;
        private int mOrientation = SubsamplingScaleImageView.ORIENTATION_USE_EXIF;

        public Parameters() {
        }

        public Parameters source(Uri uri) {
            mSourceUri = uri.toString();

            return this;
        }

        public Parameters preview(Uri uri) {
            mPreviewUri = uri.toString();

            return this;
        }

        public Parameters source(String url) {
            mSourceUri = url;

            return this;
        }

        public Parameters preview(String url) {
            mPreviewUri = url;

            return this;
        }

        public Parameters dimensions(int height, int width) {
            mHeight = height;
            mWidth = width;

            return this;
        }

        public Parameters orientation(int orientation) {
            mOrientation = orientation;

            return this;
        }
    }
}
