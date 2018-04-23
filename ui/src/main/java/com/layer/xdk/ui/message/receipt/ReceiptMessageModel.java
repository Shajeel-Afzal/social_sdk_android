package com.layer.xdk.ui.message.receipt;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.gson.stream.JsonReader;
import com.layer.sdk.LayerClient;
import com.layer.sdk.messaging.Message;
import com.layer.sdk.messaging.MessagePart;
import com.layer.xdk.ui.R;
import com.layer.xdk.ui.message.location.LocationMessageModel;
import com.layer.xdk.ui.message.model.MessageModel;
import com.layer.xdk.ui.message.product.ProductMessageModel;
import com.layer.xdk.ui.util.Log;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class ReceiptMessageModel extends MessageModel {
    public static final String MIME_TYPE = "application/vnd.layer.receipt+json";
    private static final String ROLE_PRODUCT_ITEM = "product-item";
    private static final String ROLE_SHIPPING_ADDRESS = "shipping-address";
    private static final String ROLE_BILLING_ADDRESS = "billing-address";

    private ReceiptMessageMetadata mMetadata;

    public ReceiptMessageModel(@NonNull Context context, @NonNull LayerClient layerClient,
                               @NonNull Message message) {
        super(context, layerClient, message);
    }

    @Override
    public int getViewLayoutId() {
        return R.layout.xdk_ui_receipt_message_view;
    }

    @Override
    public int getContainerViewLayoutId() {
        return R.layout.xdk_ui_titled_message_container;
    }

    @Override
    protected void parse(@NonNull MessagePart messagePart) {
        InputStreamReader inputStreamReader = new InputStreamReader(messagePart.getDataStream());
        JsonReader reader = new JsonReader(inputStreamReader);
        mMetadata = getGson().fromJson(reader, ReceiptMessageMetadata.class);
        try {
            inputStreamReader.close();
        } catch (IOException e) {
            if (Log.isLoggable(Log.ERROR)) {
                Log.e("Failed to close input stream while parsing receipt message", e);
            }
        }
    }

    @Override
    protected boolean shouldDownloadContentIfNotReady(@NonNull MessagePart messagePart) {
        return true;
    }

    @Nullable
    @Override
    public String getTitle() {
        return mMetadata != null && mMetadata.mTitle != null ? mMetadata.mTitle : "Order Confirmation";
    }

    @Nullable
    @Override
    public String getDescription() {
        return null;
    }

    @Nullable
    @Override
    public String getFooter() {
        return null;
    }

    @Override
    public boolean getHasContent() {
        return mMetadata != null;
    }

    @Nullable
    @Override
    public String getPreviewText() {
        return getAppContext().getString(R.string.xdk_ui_receipt_message_preview_text);
    }

    @SuppressWarnings("WeakerAccess")
    @NonNull
    public List<ProductMessageModel> getProductItemModels() {
        List<ProductMessageModel> models = new ArrayList<>();
        for (MessageModel model : getChildMessageModelsWithRole(ROLE_PRODUCT_ITEM)) {
            models.add((ProductMessageModel) model);
        }

        return models;
    }

    @Nullable
    public LocationMessageModel getShippingAddressLocationModel() {
        List<MessageModel> models = getChildMessageModelsWithRole(ROLE_SHIPPING_ADDRESS);
        if (!models.isEmpty()) {
            return (LocationMessageModel) models.get(0);
        }

        return null;
    }

    @Nullable
    public LocationMessageModel getBillingAddressLocationModel() {
        List<MessageModel> models = getChildMessageModelsWithRole(ROLE_BILLING_ADDRESS);
        if (!models.isEmpty()) {
            return (LocationMessageModel) models.get(0);
        }

        return null;
    }

    @Nullable
    public String getPaymentMethod() {
        return mMetadata != null ? mMetadata.mPaymentMethod : null;
    }

    @NonNull
    public String getFormattedCost() {
        if (mMetadata != null) {
            String cost = mMetadata.getTotalCostToDisplay(getAppContext());
            if (cost != null) {
                return cost;
            }
        }
        return "";
    }
}
