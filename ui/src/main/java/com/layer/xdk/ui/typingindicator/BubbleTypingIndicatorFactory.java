package com.layer.xdk.ui.typingindicator;

import android.content.Context;
import android.content.res.Resources;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Interpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.layer.xdk.ui.R;
import com.layer.sdk.listeners.LayerTypingIndicatorListener;
import com.layer.sdk.messaging.Identity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BubbleTypingIndicatorFactory implements TypingIndicatorLayout.TypingIndicatorFactory<LinearLayout> {
    private static final String TAG = BubbleTypingIndicatorFactory.class.getSimpleName();

    private static final int DOT_RES_ID = R.drawable.xdk_ui_typing_indicator_dot;
    private static final float DOT_ON_ALPHA = 0.31f;
    private static final long ANIMATION_PERIOD = 600;
    private static final long ANIMATION_OFFSET = ANIMATION_PERIOD / 3;

    @Override
    public LinearLayout onCreateView(Context context) {
        Resources r = context.getResources();

        int minWidth = r.getDimensionPixelSize(R.dimen.xdk_ui_message_model_cell_min_width);
        int minHeight = r.getDimensionPixelSize(R.dimen.xdk_ui_message_model_cell_min_height);
        int dotSize = r.getDimensionPixelSize(R.dimen.xdk_ui_typing_indicator_dot_size);
        int dotSpace = r.getDimensionPixelSize(R.dimen.xdk_ui_typing_indicator_dot_space);

        LinearLayout l = new LinearLayout(context);
        l.setMinimumWidth(minWidth);
        l.setMinimumHeight(minHeight);
        l.setGravity(Gravity.CENTER);
        l.setOrientation(LinearLayout.HORIZONTAL);
        l.setLayoutParams(new TypingIndicatorLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        l.setBackgroundDrawable(r.getDrawable(R.drawable.xdk_ui_bubble_typing_indicator_background));

        ImageView v;
        LinearLayout.LayoutParams vp;

        List<View> dots = new ArrayList<View>(3);

        v = new ImageView(context);
        vp = new LinearLayout.LayoutParams(dotSize, dotSize);
        vp.setMargins(0, 0, dotSpace, 0);
        v.setLayoutParams(vp);
        v.setBackgroundDrawable(r.getDrawable(DOT_RES_ID));
        dots.add(v);
        l.addView(v);

        v = new ImageView(context);
        vp = new LinearLayout.LayoutParams(dotSize, dotSize);
        vp.setMargins(0, 0, dotSpace, 0);
        v.setLayoutParams(vp);
        v.setBackgroundDrawable(r.getDrawable(DOT_RES_ID));
        dots.add(v);
        l.addView(v);

        v = new ImageView(context);
        vp = new LinearLayout.LayoutParams(dotSize, dotSize);
        v.setLayoutParams(vp);
        v.setBackgroundDrawable(r.getDrawable(DOT_RES_ID));
        dots.add(v);
        l.addView(v);

        l.setTag(dots);
        return l;
    }

    @Override
    public void onBindView(LinearLayout l, Map<Identity, LayerTypingIndicatorListener.TypingIndicator> typingUserIds) {
        @SuppressWarnings("unchecked")
        List<View> dots = (List<View>) l.getTag();
        View dot1 = dots.get(0);
        View dot2 = dots.get(1);
        View dot3 = dots.get(2);

        Boolean animating = (Boolean) dot1.getTag();
        if (animating == null) animating = false;

        if (animating && typingUserIds.isEmpty()) {
            // Stop animating
            dot1.clearAnimation();
            dot2.clearAnimation();
            dot3.clearAnimation();
            dot1.setTag(true);
        } else if (!animating && !typingUserIds.isEmpty()) {
            // Start animating
            dot1.setAlpha(DOT_ON_ALPHA);
            dot2.setAlpha(DOT_ON_ALPHA);
            dot3.setAlpha(DOT_ON_ALPHA);
            startAnimation(dot1, ANIMATION_PERIOD, 0);
            startAnimation(dot2, ANIMATION_PERIOD, ANIMATION_OFFSET);
            startAnimation(dot3, ANIMATION_PERIOD, ANIMATION_OFFSET + ANIMATION_OFFSET);
            dot1.setTag(false);
        }
    }

    /**
     * Starts a repeating fade out / fade in with the given period and offset in milliseconds.
     *
     * @param view        View to start animating.
     * @param period      Length of time in milliseconds for the fade out / fade in period.
     * @param startOffset Length of time in milliseconds to delay the initial start.
     */
    private void startAnimation(final View view, long period, long startOffset) {
        final AlphaAnimation fadeOut = new AlphaAnimation(1.0f, 0.0f);
        fadeOut.setDuration(period / 2);
        fadeOut.setStartOffset(startOffset);
        fadeOut.setInterpolator(COSINE_INTERPOLATOR);

        final AlphaAnimation fadeIn = new AlphaAnimation(0.0f, 1.0f);
        fadeIn.setDuration(period / 2);
        fadeIn.setStartOffset(0);
        fadeIn.setInterpolator(COSINE_INTERPOLATOR);

        fadeOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                fadeIn.setStartOffset(0);
                fadeIn.reset();
                view.startAnimation(fadeIn);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });

        fadeIn.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                fadeOut.setStartOffset(0);
                fadeOut.reset();
                view.startAnimation(fadeOut);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });

        view.startAnimation(fadeOut);
    }

    /**
     * Ease in/out
     */
    private final Interpolator COSINE_INTERPOLATOR = new Interpolator() {
        @Override
        public float getInterpolation(float input) {
            return (float) (1.0 - Math.cos(input * Math.PI / 2.0));
        }
    };
}
