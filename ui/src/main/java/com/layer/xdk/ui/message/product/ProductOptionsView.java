package com.layer.xdk.ui.message.product;

import android.content.Context;
import android.databinding.BindingAdapter;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

import com.layer.xdk.ui.databinding.XdkUiProductOptionViewItemBinding;
import com.layer.xdk.ui.message.choice.ChoiceMessageModel;
import com.layer.xdk.ui.message.choice.ChoiceMetadata;

import java.util.Iterator;
import java.util.List;

public class ProductOptionsView extends LinearLayout {
    private List<ChoiceMessageModel> mOptions;

    public ProductOptionsView(Context context) {
        this(context, null, 0);
    }

    public ProductOptionsView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ProductOptionsView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setOrientation(VERTICAL);
    }

    public List<ChoiceMessageModel> getOptions() {
        return mOptions;
    }

    @BindingAdapter("app:setOptions")
    public static void setOptions(ProductOptionsView view, @NonNull List<ChoiceMessageModel> options) {
        view.mOptions = options;
        view.renderOptions();
    }

    private void renderOptions() {
        removeAllViews();
        if (mOptions != null && !mOptions.isEmpty()) {
            for (ChoiceMessageModel option : mOptions) {
                Iterator<String> selectedChoicesIterator = option.getSelectedChoices() != null
                        ? option.getSelectedChoices().iterator() : null;
                String choiceId = selectedChoicesIterator != null &&
                        selectedChoicesIterator.hasNext() ? selectedChoicesIterator.next() : null;
                List<ChoiceMetadata> choices = option.getChoiceMessageMetadata() != null
                        ? option.getChoiceMessageMetadata().mChoices : null;

                if (choices != null && choices.size() > 0 && choiceId != null) {
                    for (ChoiceMetadata choice : choices) {
                        if (choice.mId.equals(choiceId)) {
                            // Instantiate and add view
                            XdkUiProductOptionViewItemBinding binding = XdkUiProductOptionViewItemBinding.inflate(LayoutInflater.from(getContext()), this, true);

                            // Set data on it
                            binding.optionTitle.setText(option.getChoiceMessageMetadata().mLabel);
                            binding.optionText.setText(choice.mText);

                            break;
                        }
                    }
                }
            }
        }

        if (getChildCount() == 0) {
            setVisibility(GONE);
        }
    }
}
