package com.layer.xdk.ui.message.image;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import com.layer.xdk.ui.message.view.MessageViewHelper;
import com.layer.xdk.ui.message.image.cache.ImageRequestParameters;

public class ImageMessageView extends AppCompatImageView {

    private MessageViewHelper mMessageViewHelper;

    public ImageMessageView(Context context) {
        this(context, null, 0);
    }

    public ImageMessageView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ImageMessageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mMessageViewHelper = new MessageViewHelper(context);
        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                mMessageViewHelper.performAction();
            }
        });
    }

    public void setMessageModel(@Nullable ImageMessageModel model) {
        mMessageViewHelper.setMessageModel(model);
        if (model != null) {
            if (model.getPreviewRequestParameters() != null) {
                setupImageViewDimensions(model.getPreviewRequestParameters());
            } else if (model.getSourceRequestParameters() != null) {
                setupImageViewDimensions(model.getSourceRequestParameters());
            }
        }
    }

    private void setupImageViewDimensions(ImageRequestParameters requestParams) {
        ViewGroup.LayoutParams layoutParams = getLayoutParams();
        int width = (requestParams.getTargetWidth() > 0 ? requestParams.getTargetWidth()
                : ViewGroup.LayoutParams.WRAP_CONTENT);
        int height = (requestParams.getTargetHeight() > 0 ? requestParams.getTargetHeight()
                : ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.width = width;
        layoutParams.height = height;
        setLayoutParams(layoutParams);
    }
}
