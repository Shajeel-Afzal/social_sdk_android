package com.layer.xdk.ui.message.receipt;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.layer.xdk.ui.databinding.XdkUiReceiptMessageViewBinding;
import com.layer.xdk.ui.message.view.MessageViewHelper;
import com.layer.xdk.ui.message.location.LocationMessageModel;
import com.layer.xdk.ui.message.product.ProductMessageModel;

import java.util.List;

public class ReceiptMessageLayout extends ConstraintLayout {
    private XdkUiReceiptMessageViewBinding mBinding;
    private MessageViewHelper mMessageViewHelper;

    public ReceiptMessageLayout(Context context) {
        this(context, null, 0);
    }

    public ReceiptMessageLayout(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ReceiptMessageLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mMessageViewHelper = new MessageViewHelper(context);
    }

    public void setMessageModel(ReceiptMessageModel model) {
        if (mBinding == null) {
            initializeBinding();
        }
        if (model != null) {
            List<ProductMessageModel> products = model.getProductItemModels();
            if (!products.isEmpty()) {
                mBinding.productsLayout.setVisibility(VISIBLE);
                mBinding.productsLayout.removeAllViews();

                for (ProductMessageModel product : products) {
                    ReceiptMessageProductItemView view = new ReceiptMessageProductItemView(
                            getContext());
                    view.setLayoutParams(new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT));
                    mBinding.productsLayout.addView(view);
                    view.setProductModel(product);
                }

            } else {
                mBinding.productsLayout.setVisibility(View.GONE);
            }
        }
    }

    private void initializeBinding() {
        mBinding = DataBindingUtil.getBinding(this);
        if (mBinding != null) {
            mBinding.shippingAddressValue.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    onClickShippingAddress();
                }
            });

            mBinding.billingAddressValue.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    onClickBillingAddress();
                }
            });
        }
    }

    public void onClickBillingAddress() {
        LocationMessageModel model = mBinding.getMessageModel().getBillingAddressLocationModel();
        if (model != null) {
            mMessageViewHelper.setMessageModel(model);
            mMessageViewHelper.performAction();
        }
    }

    public void onClickShippingAddress() {
        LocationMessageModel model = mBinding.getMessageModel().getShippingAddressLocationModel();
        if (model != null) {
            mMessageViewHelper.setMessageModel(model);
            mMessageViewHelper.performAction();
        }
    }
}
