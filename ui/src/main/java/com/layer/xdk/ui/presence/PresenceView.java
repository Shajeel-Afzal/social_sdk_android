package com.layer.xdk.ui.presence;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;

import com.layer.sdk.messaging.Identity;
import com.layer.sdk.messaging.Presence;
import com.layer.xdk.ui.R;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;

public class PresenceView extends View {
    private Identity mIdentity;
    private int mAvailableColor;
    private int mBusyColor;
    private int mAwayColor;
    private int mInvisibleColor;
    private int mOfflineColor;
    private int mOuterStrokeColor;
    private int mOuterStrokeWidth;
    private boolean mShowOuterStroke;

    private LayerDrawable mPresenceDrawable;
    private GradientDrawable mOuterDrawable;
    private GradientDrawable mInnerDrawable;

    public PresenceView(Context context) {
        this(context, null, 0);
    }

    public PresenceView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PresenceView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray ta = context.getTheme().obtainStyledAttributes(attrs, R.styleable.PresenceView,
                R.attr.PresenceView, defStyleAttr);
        mAvailableColor = ta.getColor(R.styleable.PresenceView_presenceAvailableColor,
                ContextCompat.getColor(context, R.color.xdk_ui_presence_available));
        mBusyColor = ta.getColor(R.styleable.PresenceView_presenceBusyColor,
                getContext().getResources().getColor(R.color.xdk_ui_presence_busy));
        mAwayColor = ta.getColor(R.styleable.PresenceView_presenceAwayColor,
                getContext().getResources().getColor(R.color.xdk_ui_presence_away));
        mInvisibleColor = ta.getColor(R.styleable.PresenceView_presenceInvisibleColor,
                getContext().getResources().getColor(R.color.xdk_ui_presence_invisible));
        mOfflineColor = ta.getColor(R.styleable.PresenceView_presenceOfflineColor,
                getContext().getResources().getColor(R.color.xdk_ui_presence_offline));
        mOuterStrokeColor = ta.getColor(R.styleable.PresenceView_presenceOuterStrokeColor,
                ContextCompat.getColor(context, R.color.xdk_ui_presence_outer_stroke));
        mOuterStrokeWidth = (int) ta.getDimension(R.styleable.PresenceView_presenceOuterStrokeWidth,
                context.getResources().getDimension(R.dimen.xdk_ui_presence_outer_stroke));
        mShowOuterStroke = ta.getBoolean(R.styleable.PresenceView_presenceShowOuterStroke,
                true);
        ta.recycle();

        mOuterDrawable = new GradientDrawable();
        mOuterDrawable.setShape(GradientDrawable.OVAL);
        mInnerDrawable = new GradientDrawable();
        mInnerDrawable.setShape(GradientDrawable.OVAL);

        mPresenceDrawable = new LayerDrawable(new Drawable[]{mOuterDrawable, mInnerDrawable});

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            setBackground(mPresenceDrawable);
        } else {
            setBackgroundDrawable(mPresenceDrawable);
        }
    }

    public void setParticipants(Set<Identity> participants) {
        processSetParticipants(participants);
    }

    public void setParticipants(Identity... participants) {
        processSetParticipants(Arrays.asList(participants));
    }

    private void processSetParticipants(Collection<Identity> participants) {
        if (participants.size() == 1) {
            mIdentity = participants.iterator().next();
            setVisibility(VISIBLE);
            Presence.PresenceStatus currentStatus = mIdentity != null ? mIdentity.getPresenceStatus() : null;
            if (currentStatus == null) {
                return;
            }

            switch (currentStatus) {
                case AVAILABLE:
                    setupPresenceColors(mAvailableColor, false);
                    break;
                case AWAY:
                    setupPresenceColors(mAwayColor, false);
                    break;
                case OFFLINE:
                    setupPresenceColors(mOfflineColor, false);
                    break;
                case INVISIBLE:
                    setupPresenceColors(mInvisibleColor, true);
                    break;
                case BUSY:
                    setupPresenceColors(mBusyColor, false);
                    break;
            }
        } else {
            setVisibility(INVISIBLE);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = getMeasuredWidth() - (getPaddingLeft() + getPaddingRight());
        int height = getMeasuredHeight() - (getPaddingTop() + getPaddingBottom());

        mOuterDrawable.setSize(width, height);
        mInnerDrawable.setSize(width, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }

    private void setupPresenceColors(@ColorInt int presenceColor, boolean makeHollow) {
        mOuterDrawable.setColor(presenceColor);
        mOuterDrawable.setStroke(mShowOuterStroke ? mOuterStrokeWidth : 0, mOuterStrokeColor);

        mInnerDrawable.setColor(makeHollow ? mOuterStrokeColor : presenceColor);
        mInnerDrawable.setStroke(4 * mOuterStrokeWidth, Color.TRANSPARENT);
    }
}
