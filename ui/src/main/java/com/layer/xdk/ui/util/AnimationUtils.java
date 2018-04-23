package com.layer.xdk.ui.util;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.view.View;

public class AnimationUtils {
    /**
     * Slides a view up from the bottom of its bounds into visibility
     * @param view to be animated in
     * @param duration of animation
     */
    public static void animateViewIn(final View view, int duration) {
        final int viewHeight = view.getHeight();
        view.setTranslationY(viewHeight);
        view.setVisibility(View.VISIBLE);
        ValueAnimator animator = new ValueAnimator();
        animator.setIntValues(viewHeight, 0);
        animator.setDuration(duration);

        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                int currentAnimatedIntValue = (int) animator.getAnimatedValue();
                view.setTranslationY(currentAnimatedIntValue);
            }
        });
        animator.start();
    }

    /**
     * Slides a view down to the bottom of its bounds with final visibility being {@link View#GONE}
     * @param view to be animated out
     * @param duration of animation
     */
    public static void animateViewOut(final View view, int duration) {
        final int viewHeight = view.getHeight();
        ValueAnimator animator = new ValueAnimator();
        animator.setIntValues(0, viewHeight);
        animator.setDuration(duration);

        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
                int currentAnimatedIntValue = (int) animator.getAnimatedValue();
                view.setTranslationY(currentAnimatedIntValue);
            }
        });

        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                view.setVisibility(View.GONE);
            }
        });

        animator.start();
    }
}
