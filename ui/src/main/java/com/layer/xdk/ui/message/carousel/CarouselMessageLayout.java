package com.layer.xdk.ui.message.carousel;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;

import com.layer.xdk.ui.R;
import com.layer.xdk.ui.message.container.MessageContainer;
import com.layer.xdk.ui.message.model.MessageModel;
import com.layer.xdk.ui.message.view.ParentMessageView;

import java.util.List;

public class CarouselMessageLayout extends FrameLayout implements ParentMessageView {
    private HorizontalScrollView mScrollView;
    private LinearLayout mLinearLayout;
    private LayoutInflater mInflater;

    private int mItemVerticalMargins;
    private int mItemHorizontalMargins;

    public CarouselMessageLayout(Context context) {
        this(context, null, 0);
    }

    public CarouselMessageLayout(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CarouselMessageLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mInflater = LayoutInflater.from(context);
        mItemVerticalMargins = Math.round(context.getResources().getDimension(R.dimen.xdk_ui_carousel_message_item_vertical_margins));
        mItemHorizontalMargins = Math.round(context.getResources().getDimension(R.dimen.xdk_ui_carousel_message_item_horizontal_margins));

        mScrollView = new HorizontalScrollView(context, attrs, defStyleAttr);
        mScrollView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        mLinearLayout = new LinearLayout(context, attrs, defStyleAttr);
        mLinearLayout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        mLinearLayout.setOrientation(LinearLayout.HORIZONTAL);

        mScrollView.addView(mLinearLayout);
        addView(mScrollView);
    }

    @Override
    public <T extends MessageModel> void inflateChildLayouts(@NonNull T model,
            @NonNull OnLongClickListener longClickListener) {
        if (!(model instanceof CarouselMessageModel)) {
            // Nothing to do with a non carousel model
            return;
        }
        // Don't draw a border on the container for this layout
        ((MessageContainer) getParent()).setDrawBorder(false);

        CarouselMessageModel carouselModel = (CarouselMessageModel) model;

        mLinearLayout.removeAllViews();
        List<MessageModel> models = carouselModel.getCarouselItemModels();

        for (int i = 0; i < models.size(); i++) {
            MessageModel itemModel = models.get(i);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            if (i == 0) {
                params.setMargins(0, mItemVerticalMargins, mItemHorizontalMargins,
                        mItemVerticalMargins);
            } else if (i == models.size() - 1) {
                params.setMargins(mItemHorizontalMargins, mItemVerticalMargins, 0,
                        mItemVerticalMargins);
            } else {
                params.setMargins(mItemHorizontalMargins, mItemVerticalMargins,
                        mItemHorizontalMargins, mItemVerticalMargins);
            }

            ViewDataBinding containerBinding = DataBindingUtil.inflate(mInflater,
                    itemModel.getContainerViewLayoutId(), mLinearLayout, false);

            MessageContainer container = (MessageContainer) containerBinding.getRoot();
            container.setLayoutParams(params);
            mLinearLayout.addView(container);

            View contentView = container.inflateMessageView(itemModel.getViewLayoutId());
            contentView.setOnLongClickListener(longClickListener);
            if (contentView instanceof ParentMessageView) {
                ((ParentMessageView) contentView).inflateChildLayouts(itemModel, longClickListener);
            }
        }
    }

    public void setMessageModel(CarouselMessageModel model) {
        if (model != null) {
            for (int i = 0; i < mLinearLayout.getChildCount(); i++) {
                MessageContainer container = (MessageContainer) mLinearLayout.getChildAt(i);
                container.setMessageModel(model.getCarouselItemModels().get(i));
            }
        }

    }
}
