package com.layer.xdk.ui.message.button;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.databinding.Observable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.AppCompatButton;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.LinearLayout;

import com.layer.xdk.ui.R;
import com.layer.xdk.ui.databinding.XdkUiButtonMessageViewBinding;
import com.layer.xdk.ui.message.choice.ChoiceButtonSet;
import com.layer.xdk.ui.message.choice.ChoiceConfigMetadata;
import com.layer.xdk.ui.message.choice.ChoiceMetadata;
import com.layer.xdk.ui.message.container.MessageContainer;
import com.layer.xdk.ui.message.model.MessageModel;
import com.layer.xdk.ui.message.view.MessageViewHelper;
import com.layer.xdk.ui.message.view.ParentMessageView;
import com.layer.xdk.ui.util.Log;

import java.util.List;
import java.util.Set;

public class ButtonMessageLayout extends ConstraintLayout implements ParentMessageView {
    private static final String BUTTON_SET_TAG_PREFIX = "ChoiceButtonSet-";

    private XdkUiButtonMessageViewBinding mBinding;
    private MessageViewHelper mMessageViewHelper;

    public ButtonMessageLayout(Context context) {
        this(context, null, 0);
    }

    public ButtonMessageLayout(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ButtonMessageLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mMessageViewHelper = new MessageViewHelper(context);
    }

    @Override
    public <T extends MessageModel> void inflateChildLayouts(@NonNull T model,
            @NonNull OnLongClickListener longClickListener) {
        if (!(model instanceof ButtonMessageModel)) {
            // Nothing to do with a non button model
            return;
        }
        ButtonMessageModel buttonModel = (ButtonMessageModel) model;
        MessageModel contentModel = buttonModel.getContentModel();
        if (contentModel == null) {
            // Nothing to do
            return;
        }
        mBinding = DataBindingUtil.getBinding(this);
        ViewStub viewStub = mBinding.xdkUiButtonMessageViewContent.getViewStub();
        viewStub.setLayoutResource(contentModel.getContainerViewLayoutId());
        MessageContainer container = (MessageContainer) viewStub.inflate();
        container.setDrawBorder(false);
        View contentView = container.inflateMessageView(contentModel.getViewLayoutId());
        contentView.setOnLongClickListener(longClickListener);
        if (contentView instanceof ParentMessageView) {
            ((ParentMessageView) contentView).inflateChildLayouts(contentModel, longClickListener);
        }
    }

    public void setMessageModel(ButtonMessageModel model) {
        mBinding = DataBindingUtil.getBinding(this);
        mMessageViewHelper.setMessageModel(model);
        if (model == null) {
            return;
        }
        if (model.getContentModel() != null) {
            MessageContainer messageContainer =
                    (MessageContainer) mBinding.xdkUiButtonMessageViewContent.getRoot();
            if (messageContainer != null) {
                messageContainer.setMessageModel(model.getContentModel());
            }
        }

        // This is the easy but expensive way to do this. Improve with AND-1374
        mBinding.xdkUiButtonMessageViewButtonsContainer.removeAllViews();
        addOrUpdateButtonsFromModel();

        model.addOnPropertyChangedCallback(new Observable.OnPropertyChangedCallback() {
            @Override
            public void onPropertyChanged(Observable sender, int propertyId) {
                addOrUpdateButtonsFromModel();
            }
        });
        mBinding.executePendingBindings();
    }

    private void addOrUpdateButtonsFromModel() {
        ButtonMessageModel model = mBinding.getMessageModel();
        if (model == null) {
            return;
        }
        List<ButtonMetadata> buttonMetadata = model.getButtonMetadata();
        if (buttonMetadata != null) {
            for (ButtonMetadata metadata : buttonMetadata) {
                addOrUpdateButton(metadata);
            }
        }
    }

    private void addOrUpdateButton(ButtonMetadata metadata) {
        if (metadata.mType.equals(ButtonMetadata.TYPE_ACTION)) {
            addOrUpdateActionButton(metadata);
        } else if (metadata.mType.equals(ButtonMetadata.TYPE_CHOICE)) {
            addOrUpdateChoiceButtons(metadata);
        }
    }

    private void addOrUpdateActionButton(@NonNull final ButtonMetadata metadata) {
        //Instantiate
        AppCompatButton actionButton = findViewWithTag(metadata.mText);
        if (actionButton == null) {
            actionButton = new AppCompatButton(getContext(), null, R.attr.MessageButton);

            actionButton.setTag(metadata.mText);

            // Add it
            mBinding.xdkUiButtonMessageViewButtonsContainer.addView(actionButton,
                    new ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT));
        }

        // Bind data to it
        actionButton.setText(metadata.mText);
        actionButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                mMessageViewHelper.performAction();
            }
        });
    }

    private void addOrUpdateChoiceButtons(@NonNull ButtonMetadata metadata) {
        if (metadata.mChoices == null || metadata.mChoices.isEmpty()) return;
        final ChoiceConfigMetadata choiceConfig = metadata.mChoiceConfigMetadata;
        if (choiceConfig == null) {
            if (Log.isLoggable(Log.WARN)) {
                Log.w("No response name for this choice set, not adding choice buttons");
            }
            return;
        }

        // Prefix this tag in case buttons have the same response name
        String buttonSetTag = BUTTON_SET_TAG_PREFIX + choiceConfig.getResponseName();
        ChoiceButtonSet choiceButtonSet = findViewWithTag(buttonSetTag);
        if (choiceButtonSet == null) {
            choiceButtonSet = new ChoiceButtonSet(getContext());
            choiceButtonSet.setOrientation(LinearLayout.HORIZONTAL);
            choiceButtonSet.setTag(buttonSetTag);
            mBinding.xdkUiButtonMessageViewButtonsContainer.addView(choiceButtonSet,
                    new ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT));
        }

        choiceButtonSet.setupViewsForChoices(metadata.mChoices);
        choiceButtonSet.setEnabledForMe(choiceConfig.mEnabledForMe);
        choiceButtonSet.setAllowDeselect(choiceConfig.mAllowDeselect);
        choiceButtonSet.setAllowReselect(choiceConfig.mAllowReselect);
        choiceButtonSet.setAllowMultiSelect(choiceConfig.mAllowMultiselect);

        choiceButtonSet.setOnChoiceClickedListener(new ChoiceButtonSet.OnChoiceClickedListener() {
            @Override
            public void onChoiceClick(ChoiceMetadata choice, boolean selected) {
                ButtonMessageModel messageModel = mBinding.getMessageModel();
                if (messageModel != null) {
                    messageModel.onChoiceClicked(choiceConfig, choice, selected);
                }
            }
        });

        Set<String> selectedChoices = mBinding.getMessageModel().getSelectedChoices(
                choiceConfig.getResponseName());

        choiceButtonSet.setSelection(selectedChoices);
    }
}
