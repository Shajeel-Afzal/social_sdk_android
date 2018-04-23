package com.layer.xdk.ui.avatar;

import android.graphics.Typeface;

public final class AvatarStyle {

    private int mAvatarBackgroundColor;
    private int mAvatarBorderColor;
    private int mAvatarTextColor;
    private Typeface mAvatarTextTypeface;
    private float mWidth;
    private float mHeight;
    private float mPresenceRadius;

    private AvatarStyle(Builder builder) {
        mAvatarBackgroundColor = builder.avatarBackgroundColor;
        mAvatarTextColor = builder.avatarTextColor;
        mAvatarTextTypeface = builder.avatarTextTypeface;
        mAvatarBorderColor = builder.avatarBorderColor;
        mWidth = builder.width;
        mHeight = builder.height;
        mPresenceRadius = builder.presenceRadius;
    }

    public void setAvatarTextTypeface(Typeface avatarTextTypeface) {
        this.mAvatarTextTypeface = avatarTextTypeface;
    }

    @SuppressWarnings("WeakerAccess")
    public int getAvatarBackgroundColor() {
        return mAvatarBackgroundColor;
    }

    @SuppressWarnings("WeakerAccess")
    public int getAvatarTextColor() {
        return mAvatarTextColor;
    }

    @SuppressWarnings("WeakerAccess")
    public Typeface getAvatarTextTypeface() {
        return mAvatarTextTypeface;
    }

    @SuppressWarnings("WeakerAccess")
    public int getAvatarBorderColor() {
        return mAvatarBorderColor;
    }

    public float getWidth() {
        return mWidth;
    }

    public float getHeight() {
        return mHeight;
    }

    public float getPresenceRadius() {
        return mPresenceRadius;
    }

    public static final class Builder {
        private int avatarBorderColor;
        private int avatarBackgroundColor;
        private int avatarTextColor;
        private Typeface avatarTextTypeface;
        private float width;
        private float height;
        private float presenceRadius;

        public Builder() {
        }

        public Builder avatarBackgroundColor(int val) {
            avatarBackgroundColor = val;
            return this;
        }

        public Builder avatarTextColor(int val) {
            avatarTextColor = val;
            return this;
        }

        public Builder avatarTextTypeface(Typeface val) {
            avatarTextTypeface = val;
            return this;
        }

        public Builder avatarBorderColor(int val) {
            avatarBorderColor = val;
            return this;
        }

        public Builder width(float width) {
            this.width = width;
            return this;
        }

        public Builder height(float height) {
            this.height = height;
            return this;
        }

        public Builder presenceRadius(float presenceRadius) {
            this.presenceRadius = presenceRadius;
            return this;
        }

        public AvatarStyle build() {
            return new AvatarStyle(this);
        }
    }
}
