package com.layer.xdk.ui.message.product;

import android.content.Context;
import android.databinding.Bindable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;
import com.layer.sdk.LayerClient;
import com.layer.sdk.messaging.Message;
import com.layer.sdk.messaging.MessagePart;
import com.layer.xdk.ui.R;
import com.layer.xdk.ui.message.choice.ChoiceMessageModel;
import com.layer.xdk.ui.message.choice.ChoiceMetadata;
import com.layer.xdk.ui.message.image.cache.ImageRequestParameters;
import com.layer.xdk.ui.message.model.MessageModel;
import com.layer.xdk.ui.util.Log;

import java.io.IOException;
import java.io.InputStreamReader;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Currency;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

public class ProductMessageModel extends MessageModel {
    public static final String MIME_TYPE = "application/vnd.layer.product+json";
    private static final String DEFAULT_ACTION_EVENT = "open-url";
    private static final String DEFAULT_ACTION_DATA_KEY = "url";

    private List<ChoiceMessageModel> mOptions;
    private ProductMessageMetadata mMetadata;

    public ProductMessageModel(@NonNull Context context, @NonNull LayerClient layerClient,
                               @NonNull Message message) {
        super(context, layerClient, message);
        mOptions = new ArrayList<>();
    }

    @Override
    public int getViewLayoutId() {
        return R.layout.xdk_ui_product_message_view;
    }

    @Override
    public int getContainerViewLayoutId() {
        return R.layout.xdk_ui_empty_message_container;
    }

    @Override
    protected void parse(@NonNull MessagePart messagePart) {
        JsonReader reader;
        InputStreamReader inputStreamReader = new InputStreamReader(messagePart.getDataStream());
        reader = new JsonReader(inputStreamReader);
        mMetadata = getGson().fromJson(reader, ProductMessageMetadata.class);
        mOptions.clear();
        try {
            inputStreamReader.close();
        } catch (IOException e) {
            if (Log.isLoggable(Log.ERROR)) {
                Log.e("Failed to close input stream while parsing product message", e);
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
        return null;
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
    public String getActionEvent() {
        String actionEvent = super.getActionEvent();
        if (actionEvent == null && mMetadata != null) {
            if (mMetadata.mAction != null) {
                actionEvent = mMetadata.mAction.getEvent();
            } else if (mMetadata.mUrl != null) {
                actionEvent = DEFAULT_ACTION_EVENT;
            }
        }

        return actionEvent;
    }

    @NonNull
    @Override
    public JsonObject getActionData() {
        JsonObject data = super.getActionData();
        if (data.size() == 0 && mMetadata != null) {
            if (mMetadata.mAction != null) {
                data = mMetadata.mAction.getData();
            } else if (mMetadata.mUrl != null) {
                data.addProperty(DEFAULT_ACTION_DATA_KEY, mMetadata.mUrl);
            }
        }

        return data;
    }

    @Nullable
    @Bindable
    public String getBrand() {
        return mMetadata != null ? mMetadata.mBrand : null;
    }

    @Nullable
    @Bindable
    public String getName() {
        return mMetadata != null ? mMetadata.mName : null;
    }

    @Nullable
    @Bindable
    public String getProductDescription() {
        return mMetadata != null ? mMetadata.mDescription : null;
    }

    @Nullable
    @Override
    public String getPreviewText() {
        String name = getName();
        return name != null ? name : getAppContext().getString(R.string.xdk_ui_product_message_preview_text);
    }

    @Bindable
    @Nullable
    public String getPrice() {
        if (mMetadata != null && mMetadata.mPrice != null) {
            NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.getDefault());
            currencyFormat.setCurrency(Currency.getInstance(mMetadata.getCurrency(getAppContext())));
            return currencyFormat.format(mMetadata.mPrice);
        }

        return null;
    }

    @Bindable
    @Nullable
    public ImageRequestParameters getImageRequestParameters() {
        if (mMetadata != null) {
            ImageRequestParameters.Builder builder = new ImageRequestParameters.Builder();
            String url = mMetadata.mImageUrls != null && mMetadata.mImageUrls.size() > 0 ?
                    mMetadata.mImageUrls.get(0) : null;

            if (url != null) {
                builder.url(url);
            } else {
                return null;
            }

            builder.centerCrop(true)
                    .resize(getAppContext().getResources().getDimensionPixelSize(R.dimen.xdk_ui_product_message_image_width),
                            getAppContext().getResources().getDimensionPixelSize(R.dimen.xdk_ui_product_message_image_height))
                    .tag(getClass().getSimpleName());

            return builder.build();
        }

        return null;
    }

    @Nullable
    public ProductMessageMetadata getMetadata() {
        return mMetadata;
    }

    @NonNull
    @Bindable
    public List<ChoiceMessageModel> getOptions() {
        if (mOptions.isEmpty()) {
            List<MessageModel> childMessageModels = getChildMessageModels();
            if (childMessageModels != null) {
                for (MessageModel model : childMessageModels) {
                    if (model instanceof ChoiceMessageModel) {
                        mOptions.add((ChoiceMessageModel) model);
                    }
                }
            }
        }

        return mOptions;
    }

    @Nullable
    public String getSelectedOptionsAsCommaSeparatedList() {
        List<String> productTexts = new ArrayList<>();
        List<ChoiceMessageModel> options = getOptions();
        if (!options.isEmpty()) {
            for (ChoiceMessageModel option : options) {
                Iterator<String> iterator = option.getSelectedChoices() != null ? option.getSelectedChoices().iterator() : null;
                // Use just the first choice for now, the remaining will be displayed in an
                // expanded product message view, to be built later.
                String choiceId = iterator != null && iterator.hasNext() ? iterator.next() : null;
                List<ChoiceMetadata> choices = option.getChoiceMessageMetadata() != null ? option.getChoiceMessageMetadata().mChoices : null;

                if (choices != null && choices.size() > 0) {
                    for (ChoiceMetadata choice : choices) {
                        if (choice.mId.equals(choiceId)) {
                            productTexts.add(choice.mText);

                            break;
                        }
                    }
                }
            }
        }

        return !productTexts.isEmpty() ? TextUtils.join(", ", productTexts) : null;
    }
}
