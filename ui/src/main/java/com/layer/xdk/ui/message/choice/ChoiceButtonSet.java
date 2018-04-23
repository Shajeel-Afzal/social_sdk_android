package com.layer.xdk.ui.message.choice;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.layer.xdk.ui.R;
import com.layer.xdk.ui.util.Log;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ChoiceButtonSet extends LinearLayout {
    private boolean mAllowReselect;
    private boolean mAllowDeselect;
    private boolean mAllowMultiSelect;
    private boolean mEnabledForMe;

    private Map<String, ChoiceMetadata> mChoiceMetadata = new HashMap<>();
    private OnChoiceClickedListener mOnChoiceClickedListener;
    private Set<String> mSelectedChoiceIds = new HashSet<>();

    public ChoiceButtonSet(Context context) {
        this(context, null, 0);
    }

    public ChoiceButtonSet(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ChoiceButtonSet(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        // Default orientation
        setOrientation(VERTICAL);
        setShowDividers(SHOW_DIVIDER_MIDDLE);
        setDividerDrawable(ContextCompat.getDrawable(context, R.drawable.xdk_ui_linear_layout_divider_horizontal));
    }

    public void setOnChoiceClickedListener(OnChoiceClickedListener onChoiceClickedListener) {
        mOnChoiceClickedListener = onChoiceClickedListener;
    }

    public void setAllowReselect(boolean allowReselect) {
        mAllowReselect = allowReselect;
    }

    public void setAllowDeselect(boolean allowDeselect) {
        mAllowDeselect = allowDeselect;
    }

    public void setAllowMultiSelect(boolean allowMultiSelect) {
        mAllowMultiSelect = allowMultiSelect;
    }

    /**
     * Determine if reselection is allowed. Multi select implicitly allows for reselection
     *
     * @return true if reselection is allowed, false otherwise
     */
    private boolean isReselectAllowed() {
        return mAllowReselect || mAllowMultiSelect;
    }

    /**
     * Determine if deselection is allowed. Multi select implicitly allows for deselection
     *
     * @return true if deselection is allowed, false otherwise
     */
    private boolean isDeselectAllowed() {
        return mAllowDeselect || mAllowMultiSelect;
    }

    /**
     *@return true if multi selection is allowed, false otherwise
     */
    private boolean isMultiSelectAllowed() {
        return mAllowMultiSelect;
    }

    public void setEnabledForMe(boolean enabledForMe) {
        mEnabledForMe = enabledForMe;
    }

    public void setupViewsForChoices(List<ChoiceMetadata> metadata) {
        mChoiceMetadata.clear();
        // Ensure we have the right number of views
        // Add needed views
        for (int i = getChildCount(); i < metadata.size(); i++) {
            addButton();
        }

        // Remove extra views
        int childCount = getChildCount();
        for (int i = metadata.size(); i < childCount; i++) {
            removeViewAt(i);
        }

        // Set up buttons
        for (int i = 0; i < getChildCount(); i++) {
            ChoiceButton child = (ChoiceButton) getChildAt(i);
            // We have to request a layout here since the recycled view may be wider. This needs
            // optimization (AND-1372)
            child.requestLayout();
            updateChoice(child, metadata.get(i));
        }
    }

    private void addButton() {
        // Instantiate
        ChoiceButton choiceButton = new ChoiceButton((getContext()));

        // Add it
        LayoutParams layoutParams;
        if (getOrientation() == VERTICAL) {
            layoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup
                    .LayoutParams.WRAP_CONTENT);
        } else {
            layoutParams = new LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1.0f);
        }
        addView(choiceButton, layoutParams);
    }

    public void updateChoice(ChoiceButton choiceButton, final ChoiceMetadata choice) {
        choiceButton.setTag(choice.mId);
        choiceButton.setText(choice.mText);

        mChoiceMetadata.put(choice.mId, choice);

        choiceButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ChoiceButton button = (ChoiceButton) v;
                String choiceId = (String) button.getTag();
                toggleChoice(choiceId);

                ChoiceMetadata choice = mChoiceMetadata.get(choiceId);
                if (mOnChoiceClickedListener != null) {
                    mOnChoiceClickedListener.onChoiceClick(choice, button.isChecked());
                } else if (Log.isLoggable(Log.VERBOSE)) {
                    Log.v("Clicked choice but no OnChoiceClickedListener is registered. Choice: " + choiceId);
                }
            }
        });
    }

    private void toggleChoice(String choiceId) {
        if (mSelectedChoiceIds.contains(choiceId)) {
            // Currently selected, deselect if allowed
            if (isDeselectAllowed()) {
                mSelectedChoiceIds.remove(choiceId);
            }
        } else {
            // Un-select others if multi-select is not allowed
            if (!isMultiSelectAllowed()) {
                mSelectedChoiceIds.clear();
            }
            mSelectedChoiceIds.add(choiceId);

        }
        setSelection(mSelectedChoiceIds);
    }

    public void setSelection(@NonNull Set<String> choiceIds) {
        mSelectedChoiceIds = new HashSet<>(choiceIds);
        boolean somethingIsChecked = false;

        for (int i = 0; i < getChildCount(); i++) {
            ChoiceButton choiceButton = (ChoiceButton) getChildAt(i);
            String tag = (String) choiceButton.getTag();
            if (choiceIds.contains(tag)) {
                choiceButton.setChecked(true);
                somethingIsChecked = true;
            } else {
                choiceButton.setChecked(false);
            }
        }

        for (int i = 0; i < getChildCount(); i++) {
            ChoiceButton button = (ChoiceButton) getChildAt(i);
            if (!mEnabledForMe
                    || (somethingIsChecked && !isReselectAllowed())
                    || (button.isChecked() && !isDeselectAllowed())) {
                button.setEnabled(false);
            } else {
                button.setEnabled(true);
            }

        }
    }

    public interface OnChoiceClickedListener {
        /**
         * Called when a choice button is clicked.
         *
         * @param choice Metadata of the choice button that was clicked
         * @param selected true if button transitions from unselected -> selected state. False otherwise.
         */
        void onChoiceClick(ChoiceMetadata choice, boolean selected);
    }
}
