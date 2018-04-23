package com.layer.xdk.ui.message.image.cache;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;
import android.widget.ImageView;

import com.layer.xdk.ui.util.Log;
import com.layer.xdk.ui.message.image.cache.transformations.CircleTransform;
import com.layer.xdk.ui.message.image.cache.transformations.RoundedTransform;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;
import com.squareup.picasso.Target;

import static com.layer.xdk.ui.util.Log.ERROR;
import static com.layer.xdk.ui.util.Log.TAG;
import static com.layer.xdk.ui.util.Log.VERBOSE;

import java.io.IOException;

public class PicassoImageCacheWrapper implements ImageCacheWrapper {
    private final static CircleTransform CIRCLE_TRANSFORMATION = new CircleTransform(TAG + ".circle");

    private final Picasso mPicasso;

    public PicassoImageCacheWrapper(Picasso picasso) {
        mPicasso = picasso;
    }

    @Override
    public void fetchBitmap(final BitmapWrapper bitmapWrapper, final Callback callback) {
        Target target = createTarget(bitmapWrapper, callback);
        createBitmapFetchRequestCreator(bitmapWrapper)
                .into(target);
    }

    @Override
    public void fetchBitmap(final BitmapWrapper bitmapWrapper) {
        try {
            Bitmap bitmap = createBitmapFetchRequestCreator(bitmapWrapper)
                    .get();
            bitmapWrapper.setBitmap(bitmap);
        } catch (IOException e) {
            if (Log.isLoggable(ERROR)) {
                Log.e("Failed to fetch bitmap for: " + bitmapWrapper.getUrl());
            }
        }
    }

    private RequestCreator createBitmapFetchRequestCreator(BitmapWrapper bitmapWrapper) {
        RequestCreator creator = mPicasso.load(bitmapWrapper.getUrl())
                .tag(bitmapWrapper.getId())
                .noPlaceholder()
                .noFade();
        if (bitmapWrapper.useCircleTransformation()) {
            creator.transform(CIRCLE_TRANSFORMATION);
        }
        if (bitmapWrapper.getWidth() > 0 && bitmapWrapper.getHeight() > 0) {
            creator.resize(bitmapWrapper.getWidth(), bitmapWrapper.getHeight())
                .centerCrop();
        }

        return creator;
    }

    @VisibleForTesting
    public Target createTarget(final BitmapWrapper bitmapWrapper, final Callback callback) {
        return new Target() {

            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                bitmapWrapper.setBitmap(bitmap);
                callback.onSuccess();
            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {
                if (errorDrawable != null && Log.isLoggable(VERBOSE)) {
                    Log.v("onBitMapFailed :" + errorDrawable);
                }
                bitmapWrapper.setBitmap(null);
                callback.onFailure();
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {
            }
        };
    }

    public void cancelBitmap(BitmapWrapper bitmapWrapper) {
        if (bitmapWrapper != null) {
            mPicasso.cancelTag(bitmapWrapper.getId());
        }
    }

    @Override
    public void pauseTag(String picassoTag) {
        mPicasso.pauseTag(picassoTag);
    }

    @Override
    public void resumeTag(String tag) {
        mPicasso.resumeTag(tag);
    }

    @Override
    public void loadImage(final ImageRequestParameters imageRequestParameters, ImageView imageView) {
        RequestCreator requestCreator;

        if (imageRequestParameters.getUri() != null) {
            requestCreator = mPicasso.load(imageRequestParameters.getUri());
        } else if (imageRequestParameters.getUrl() != null) {
            requestCreator = mPicasso.load(imageRequestParameters.getUrl());
        } else {
            requestCreator = mPicasso.load(imageRequestParameters.getResourceId());
        }

        requestCreator.config(Bitmap.Config.RGB_565);

        if (imageRequestParameters.noFade()) {
            requestCreator.noFade();
        }

        if (imageRequestParameters.getTag() != null) {
            requestCreator.tag(imageRequestParameters.getTag());
        }

        if (imageRequestParameters.getPlaceholder() > 0) {
            requestCreator.placeholder(imageRequestParameters.getPlaceholder());
        }

        if (imageRequestParameters.centerCrop()) {
            requestCreator = requestCreator.centerCrop();
        } else if (imageRequestParameters.centerInside()) {
            requestCreator.centerInside();
        }

        if (imageRequestParameters.fit()) {
            requestCreator.fit();
        }

        if (imageRequestParameters.getTargetWidth() > 0 && imageRequestParameters.getTargetHeight() > 0) {
            requestCreator.resize(imageRequestParameters.getTargetWidth(), imageRequestParameters.getTargetHeight());
        }

        float rotationAngle = imageRequestParameters.getRotationDegrees() +
                imageRequestParameters.getExifRotationInDegrees(imageView.getContext());

        requestCreator.rotate(rotationAngle);

        if (imageRequestParameters.shouldScaleDown()) {
            requestCreator.onlyScaleDown();
        }

        if (imageRequestParameters.shouldApplyCirclularTransform()) {
            RoundedTransform transformation = new RoundedTransform();
            transformation.setCornerRadius(imageRequestParameters.getCornerRadius());
            transformation.setHasRoundTopCorners(imageRequestParameters.hasRoundedTopCorners());
            transformation.setHasRoundBottomCorners(imageRequestParameters.hasRoundedBottomCorners());

            requestCreator.transform(transformation);
        }

        final Callback callback = imageRequestParameters.getCallback();
        if (callback != null) {
            requestCreator.into(imageView, new com.squareup.picasso.Callback() {
                @Override
                public void onSuccess() {
                    callback.onSuccess();
                }

                @Override
                public void onError() {
                    callback.onFailure();
                }
            });
        } else {
            requestCreator.into(imageView);
        }
    }

    @Override
    public void loadDefaultPlaceholder(ImageView imageView) {
        String path = null;
        mPicasso.load(path).into(imageView);
    }

    private boolean isLocalContent(@NonNull Uri uri) {
        return uri != null && (uri.getScheme().equals("file") || uri.getScheme().equals("content"));
    }
}