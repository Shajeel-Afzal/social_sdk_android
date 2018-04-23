package com.layer.xdk.ui.message.receipt;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.gson.annotations.SerializedName;
import com.layer.xdk.ui.R;

import java.text.NumberFormat;
import java.util.Currency;
import java.util.List;
import java.util.Locale;

@SuppressWarnings("WeakerAccess") // For Gson serialization/de-serialization
public class ReceiptMessageMetadata {

    @SerializedName("created_at")
    public String mCreatedAt;

    @SerializedName("currency")
    public String mCurrency;

    @SerializedName("discounts")
    public List<DiscountMetadata> mDiscounts;

    @SerializedName("order")
    public OrderMetadata mOrder;

    @SerializedName("payment_method")
    public String mPaymentMethod;

    @SerializedName("summary")
    public SummaryMetadata mSummary;

    @SerializedName("title")
    public String mTitle;

    public String getCreatedAt() {
        return mCreatedAt;
    }

    @NonNull
    public String getCurrency(Context context) {
        return mCurrency != null ? mCurrency : context.getString(R.string.xdk_ui_product_message_model_default_currency);
    }

    @Nullable
    public String getTotalCostToDisplay(@NonNull Context context) {
        if (mSummary != null && mSummary.mTotalCost != null) {
            NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.getDefault());
            currencyFormat.setCurrency(Currency.getInstance(getCurrency(context)));
            return currencyFormat.format(mSummary.mTotalCost);
        }

        return null;
    }
}
