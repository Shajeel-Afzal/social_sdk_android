package com.layer.xdk.ui.message.image.cache;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;

import java.util.UUID;

/**
 * This class wraps everything needed in a Bitmap
 * The Bitmap is used in {@link ImageCacheWrapper#fetchBitmap(BitmapWrapper, ImageCacheWrapper.Callback)}
 * and also in {@link PicassoImageCacheWrapper}
 * Properties can be added to BitmapWrapper to suit other ImageCache library
 * We use Picasso in the implementation
 */
public class BitmapWrapper {

    private final UUID mId;
    private Bitmap mBitmap;
    private String mUrl;
    private int mWidth, mHeight;
    private boolean mCircleTransformation;

    public BitmapWrapper(@NonNull String url, int width, int height, boolean useCircleTransformation) {
        mId = UUID.randomUUID();
        mUrl = url;
        mWidth = width;
        mHeight = height;
        mCircleTransformation = useCircleTransformation;
    }

    public String getUrl() {
        return mUrl;
    }

    public void setUrl(String url) {
        mUrl = url;
    }

    public Bitmap getBitmap() {
        return mBitmap;
    }

    public BitmapWrapper setBitmap(Bitmap bitmap) {
        mBitmap = bitmap;
        return this;
    }

    public void setWidth(int width) {
        mWidth = width;
    }

    public int getWidth() {
        return mWidth;
    }

    public void setHeight(int height) {
        mHeight = height;
    }

    public int getHeight() {
        return mHeight;
    }

    public UUID getId() {
        return mId;
    }

    public boolean useCircleTransformation() {
        return mCircleTransformation;
    }

    public void setCircleTransformation(boolean circleTransformation) {
        mCircleTransformation = circleTransformation;
    }
}
