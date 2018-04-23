package com.layer.xdk.ui.message.image.cache;

import android.graphics.Bitmap;
import android.widget.ImageView;

public interface ImageCacheWrapper {

    /**
     * Asynchronously fetches a bitmap from any Image Caching Library. Sets the bitmap on the
     * {@link BitmapWrapper} in your implementation via {@link BitmapWrapper#setBitmap(Bitmap)}.
     *
     * @see PicassoImageCacheWrapper for sample implementation
     *
     * @param bitmapWrapper a {@link BitmapWrapper} instance to store the fetched Bitmap in
     * @param callback callback to monitor result
     */
    void fetchBitmap(BitmapWrapper bitmapWrapper, Callback callback);

    /**
     * Synchronously fetches a bitmap from any Image Caching Library. Sets the bitmap on the
     * {@link BitmapWrapper} in your implementation via {@link BitmapWrapper#setBitmap(Bitmap)}.
     * A null bitmap will be set on a failure.
     *
     * @see PicassoImageCacheWrapper for sample implementation
     *
     * @param bitmapWrapper a {@link BitmapWrapper} instance to store the fetched Bitmap in
     */
    void fetchBitmap(BitmapWrapper bitmapWrapper);

    /**
     * Cancels a bitmap request.
     *
     * @see PicassoImageCacheWrapper for sample implementation
     *
     * @param bitmapWrapper {@link BitmapWrapper} instance used to make the fetch request
     */
    void cancelBitmap(BitmapWrapper bitmapWrapper);

    @SuppressWarnings("unused")
    void pauseTag(String tag);

    @SuppressWarnings("unused")
    void resumeTag(String tag);

    void loadImage(ImageRequestParameters imageRequestParameters, ImageView imageView);

    void loadDefaultPlaceholder(ImageView imageView);

    /**
     * Callback when the Bitmap or Image is loaded from the ImageCache Library
     */
    interface Callback {
        void onSuccess();
        void onFailure();
    }
}
