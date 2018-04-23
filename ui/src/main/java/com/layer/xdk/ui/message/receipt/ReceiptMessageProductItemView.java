package com.layer.xdk.ui.message.receipt;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;

import com.layer.xdk.ui.R;
import com.layer.xdk.ui.databinding.XdkUiReceiptMessageProductDetailBinding;
import com.layer.xdk.ui.message.product.ProductMessageModel;

public class ReceiptMessageProductItemView extends FrameLayout {
    private XdkUiReceiptMessageProductDetailBinding mBinding;

    public ReceiptMessageProductItemView(@NonNull Context context) {
        this(context, null, 0);
    }

    public ReceiptMessageProductItemView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ReceiptMessageProductItemView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        LayoutInflater inflater = LayoutInflater.from(context);
        mBinding = XdkUiReceiptMessageProductDetailBinding.inflate(inflater, this, true);
    }

    public void setProductModel(ProductMessageModel productModel) {
        mBinding.setMessageModel(productModel);
        String subtitle = productModel.getSelectedOptionsAsCommaSeparatedList();
        if (subtitle != null) {
            String text = getContext().getString(R.string.xdk_ui_receipt_product_detail_options, subtitle);
            mBinding.subtitle.setText(text);
        } else if (productModel.getMetadata() != null) {
            String quantity = productModel.getMetadata().getQuantity() > 0 ?
                    getContext().getString(R.string.xdk_ui_receipt_product_detail_quantity,
                            productModel.getMetadata().getQuantity()) : null;

            if (quantity != null) mBinding.subtitle.setText(quantity);
        }

        mBinding.executePendingBindings();
    }
}
