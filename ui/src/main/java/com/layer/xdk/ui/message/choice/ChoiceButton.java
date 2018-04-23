package com.layer.xdk.ui.message.choice;


import android.content.Context;
import android.support.v7.widget.AppCompatCheckBox;
import android.util.AttributeSet;

import com.layer.xdk.ui.R;

public class ChoiceButton extends AppCompatCheckBox {

    public ChoiceButton(Context context) {
        this(context, null);
    }

    public ChoiceButton(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.MessageChoiceButton);
    }

    public ChoiceButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }
}
