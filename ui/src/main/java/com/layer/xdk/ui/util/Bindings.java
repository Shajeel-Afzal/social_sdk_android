package com.layer.xdk.ui.util;

import android.databinding.BindingAdapter;
import android.graphics.Typeface;
import android.support.annotation.DrawableRes;
import android.support.constraint.ConstraintLayout;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.layer.xdk.ui.message.image.cache.ImageCacheWrapper;
import com.layer.xdk.ui.message.image.cache.ImageRequestParameters;

public class Bindings {

    @BindingAdapter({"bind:typeface"})
    public static void setTypeface(TextView textView, Typeface typeface) {
        textView.setTypeface(typeface);
    }

    @BindingAdapter("android:layout_marginRight")
    public static void setRightMargin(View view, float rightMargin) {
        ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
        layoutParams.setMargins(layoutParams.leftMargin, layoutParams.topMargin,
                Math.round(rightMargin), layoutParams.bottomMargin);
        view.setLayoutParams(layoutParams);
    }

    @BindingAdapter("android:layout_marginLeft")
    public static void setLeftMargin(View view, float leftMargin) {
        ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
        layoutParams.setMargins(Math.round(leftMargin), layoutParams.topMargin,
                layoutParams.rightMargin, layoutParams.bottomMargin);
        view.setLayoutParams(layoutParams);
    }

    @BindingAdapter("android:layout_marginStart")
    public static void setStartMargin(View view, float startMargin) {
        setLeftMargin(view, startMargin);
    }

    @BindingAdapter("android:layout_marginEnd")
    public static void setEndMargin(View view, float endMargin) {
        setRightMargin(view, endMargin);
    }

    @BindingAdapter("android:layout_width")
    public static void setLayoutWidth(View view, float width) {
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.width = (int) width;
        view.setLayoutParams(layoutParams);
    }

    @BindingAdapter("android:layout_height")
    public static void setLayoutHeight(View view, float height) {
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.height = (int) height;
        view.setLayoutParams(layoutParams);
    }

    @BindingAdapter("app:layout_constraintVertical_bias")
    public static void setVerticalBias(View view, float bias) {
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) view.getLayoutParams();
        params.verticalBias = bias;

    }

    @BindingAdapter({"app:loadFrom", "app:usingImageLoader"})
    public static void loadFrom(ImageView imageView, ImageRequestParameters parameters, ImageCacheWrapper imageCacheWrapper) {
        if (imageCacheWrapper == null) {
            // No model is set yet
            return;
        }
        if (parameters != null) {
            imageCacheWrapper.loadImage(parameters, imageView);
        } else {
            imageCacheWrapper.loadDefaultPlaceholder(imageView);
        }
    }

    @BindingAdapter("app:visibleOrGone")
    public static void visibleOrGone(View view, boolean visible) {
        view.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    @BindingAdapter("app:imageResource")
    public static void imageResource(ImageView imageView, @DrawableRes int resource) {
        imageView.setImageResource(resource);
    }
}
