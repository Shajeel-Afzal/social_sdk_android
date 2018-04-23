package com.layer.xdk.ui.message.container;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.databinding.Observable;
import android.databinding.ViewDataBinding;
import android.graphics.Canvas;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.os.Build;
import android.support.annotation.AttrRes;
import android.support.annotation.CallSuper;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewOutlineProvider;

import com.layer.xdk.ui.BR;
import com.layer.xdk.ui.R;
import com.layer.xdk.ui.message.model.MessageModel;

public abstract class MessageContainer extends ConstraintLayout {
    private Path mCornerClippingPath = new Path();
    private final float mCornerRadius;
    private final boolean mUsingOutline;
    private final Paint mBorderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF mRect = new RectF();
    private boolean mDrawBorder = true;

    public MessageContainer(@NonNull Context context) {
        this(context, null);
    }

    public MessageContainer(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MessageContainer(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mCornerRadius = (context.getResources()
                .getDimension(R.dimen.xdk_ui_message_container_corner_radius));

        mBorderPaint.setColor(
                ContextCompat.getColor(context, R.color.xdk_ui_message_container_border_color));
        mBorderPaint.setStrokeWidth(getResources().getDimension(
                R.dimen.xdk_ui_message_container_border_stroke_width));
        mBorderPaint.setStyle(Paint.Style.STROKE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mUsingOutline = true;
            setClipToOutline(true);
            setOutlineProvider(new ClipOutlineProvider(mCornerRadius));
        } else {
            mUsingOutline = false;
        }
    }

    @Override
    public void dispatchDraw(Canvas canvas) {
        if (mUsingOutline) {
            super.dispatchDraw(canvas);
        } else {
            int save = canvas.save();
            canvas.clipPath(mCornerClippingPath);
            super.dispatchDraw(canvas);
            canvas.restoreToCount(save);
        }
        if (mDrawBorder) {
            canvas.drawRoundRect(mRect, mCornerRadius, mCornerRadius, mBorderPaint);
        }
    }

    @Override
    protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
        super.onSizeChanged(width, height, oldWidth, oldHeight);
        mRect.set(0, 0, width, height);
        if (!mUsingOutline) {
            Path path = new Path();
            path.addRoundRect(mRect, mCornerRadius, mCornerRadius, Path.Direction.CW);
            path.close();
            mCornerClippingPath = path;
        }
    }

    public void setDrawBorder(boolean drawBorder) {
        mDrawBorder = drawBorder;
    }

    public abstract View inflateMessageView(@LayoutRes int messageViewLayoutId);

    /**
     * @return view object that contains the message view
     */
    protected abstract View getMessageView();

    /**
     * Return the minimum width, in pixels, that this container should be.
     *
     * @param hasMetadata if the associated model has metadata or not
     * @return minimum width in pixels
     */
    protected abstract int getContainerMinimumWidth(boolean hasMetadata);

    public abstract <T extends MessageModel> void setContentBackground(T model);

    @CallSuper
    public <T extends MessageModel> void setMessageModel(T model) {
        ViewDataBinding messageBinding = DataBindingUtil.getBinding(getMessageView());
        messageBinding.setVariable(BR.messageModel, model);
        if (model != null) {
            HasContentOrMetadataCallback hasContentOrMetadataCallback =
                    new HasContentOrMetadataCallback();
            model.addOnPropertyChangedCallback(hasContentOrMetadataCallback);
            // Initiate the view properties as this will only be called if the model changes
            hasContentOrMetadataCallback.onPropertyChanged(model, BR._all);
            setContentBackground(model);
        }

        messageBinding.executePendingBindings();
    }

    private class HasContentOrMetadataCallback extends Observable.OnPropertyChangedCallback {
        @Override
        public void onPropertyChanged(Observable sender, int propertyId) {
            MessageModel messageModel = (MessageModel) sender;
            View messageView = getMessageView();
            if (propertyId == BR.hasContent || propertyId == BR._all) {
                messageView.setVisibility(messageModel.getHasContent() ? VISIBLE : GONE);
            }
            if (propertyId == BR.hasMetadata || propertyId == BR._all) {
                ConstraintSet set = new ConstraintSet();
                set.clone(MessageContainer.this);
                int minWidth = getContainerMinimumWidth(messageModel.getHasMetadata());
                set.constrainMinWidth(messageView.getId(), minWidth);
                set.applyTo(MessageContainer.this);
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private static class ClipOutlineProvider extends ViewOutlineProvider {

        private float mCornerRadius;

        ClipOutlineProvider(float cornerRadius) {
            mCornerRadius = cornerRadius;
        }

        @Override
        public void getOutline(View view, Outline outline) {
            outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(), mCornerRadius);
        }

    }
}
