package sumatodev.com.social.controllers;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.view.animation.BounceInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import sumatodev.com.social.ApplicationHelper;
import sumatodev.com.social.R;
import sumatodev.com.social.enums.ProfileStatus;
import sumatodev.com.social.managers.CommentManager;
import sumatodev.com.social.managers.PostManager;
import sumatodev.com.social.managers.ProfileManager;
import sumatodev.com.social.managers.listeners.OnObjectExistListener;
import sumatodev.com.social.model.Comment;
import sumatodev.com.social.model.Post;
import sumatodev.com.social.ui.activities.BaseActivity;
import sumatodev.com.social.ui.activities.MainActivity;
import sumatodev.com.social.ui.activities.PostDetailsActivity;

/**
 * Created by Ali on 02/04/2018.
 */

public class CommentLikeController {

    private static final int ANIMATION_DURATION = 300;

    public enum AnimationType {
        COLOR_ANIM, BOUNCE_ANIM
    }

    private Context context;
    private String postId;
    private String commentId;

    private AnimationType likeAnimationType = AnimationType.BOUNCE_ANIM;

    private TextView likeCounterTextView;
    private ImageView likesImageView;

    private boolean isListView = false;

    private boolean isLiked = false;
    private boolean updatingLikeCounter = true;

    public CommentLikeController(Context context, Comment comment, TextView likeCounterTextView,
                                 ImageView likesImageView, boolean isListView) {
        this.context = context;
        this.postId = comment.getPostId();
        this.commentId = comment.getId();
        this.likeCounterTextView = likeCounterTextView;
        this.likesImageView = likesImageView;
        this.isListView = isListView;
    }

    public void likeClickAction(long prevValue) {
        if (!updatingLikeCounter) {
            startAnimateLikeButton(likeAnimationType);

            if (!isLiked) {
                addLike(prevValue);
            } else {
                removeLike(prevValue);
            }
        }
    }

    public void likeClickActionLocal(Comment comment) {
        setUpdatingLikeCounter(false);
        likeClickAction(comment.getLikesCount());
        updateLocalPostLikeCounter(comment);
    }

    private void addLike(long prevValue) {
        updatingLikeCounter = true;
        isLiked = true;
        likeCounterTextView.setText(String.valueOf(prevValue + 1));
        ApplicationHelper.getDatabaseHelper().updateCommentLike(postId, commentId);
    }

    private void removeLike(long prevValue) {
        updatingLikeCounter = true;
        isLiked = false;
        likeCounterTextView.setText(String.valueOf(prevValue - 1));
        ApplicationHelper.getDatabaseHelper().removeCommentLike(postId,commentId);
    }

    private void startAnimateLikeButton(AnimationType animationType) {
        switch (animationType) {
            case BOUNCE_ANIM:
                bounceAnimateImageView();
                break;
            case COLOR_ANIM:
                colorAnimateImageView();
                break;
        }
    }

    private void bounceAnimateImageView() {
        AnimatorSet animatorSet = new AnimatorSet();

        ObjectAnimator bounceAnimX = ObjectAnimator.ofFloat(likesImageView, "scaleX", 0.2f, 1f);
        bounceAnimX.setDuration(ANIMATION_DURATION);
        bounceAnimX.setInterpolator(new BounceInterpolator());

        ObjectAnimator bounceAnimY = ObjectAnimator.ofFloat(likesImageView, "scaleY", 0.2f, 1f);
        bounceAnimY.setDuration(ANIMATION_DURATION);
        bounceAnimY.setInterpolator(new BounceInterpolator());
        bounceAnimY.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                likesImageView.setImageResource(!isLiked ? R.drawable.ic_like_active
                        : R.drawable.ic_like);
            }
        });

        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
            }
        });

        animatorSet.play(bounceAnimX).with(bounceAnimY);
        animatorSet.start();
    }

    private void colorAnimateImageView() {
        final int activatedColor = context.getResources().getColor(R.color.like_icon_activated);

        final ValueAnimator colorAnim = !isLiked ? ObjectAnimator.ofFloat(0f, 1f)
                : ObjectAnimator.ofFloat(1f, 0f);
        colorAnim.setDuration(ANIMATION_DURATION);
        colorAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float mul = (Float) animation.getAnimatedValue();
                int alpha = adjustAlpha(activatedColor, mul);
                likesImageView.setColorFilter(alpha, PorterDuff.Mode.SRC_ATOP);
                if (mul == 0.0) {
                    likesImageView.setColorFilter(null);
                }
            }
        });

        colorAnim.start();
    }

    private int adjustAlpha(int color, float factor) {
        int alpha = Math.round(Color.alpha(color) * factor);
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);
        return Color.argb(alpha, red, green, blue);
    }

    public AnimationType getLikeAnimationType() {
        return likeAnimationType;
    }

    public void setLikeAnimationType(AnimationType likeAnimationType) {
        this.likeAnimationType = likeAnimationType;
    }

    public boolean isLiked() {
        return isLiked;
    }

    public void setLiked(boolean liked) {
        isLiked = liked;
    }

    public boolean isUpdatingLikeCounter() {
        return updatingLikeCounter;
    }

    public void setUpdatingLikeCounter(boolean updatingLikeCounter) {
        this.updatingLikeCounter = updatingLikeCounter;
    }

    public void initLike(boolean isLiked) {
        likesImageView.setImageResource(isLiked ? R.drawable.ic_like_active : R.drawable.ic_like);
        this.isLiked = isLiked;
    }

    private void updateLocalPostLikeCounter(Comment comment) {
        if (isLiked) {
            comment.setLikesCount(comment.getLikesCount() + 1);
        } else {
            comment.setLikesCount(comment.getLikesCount() - 1);
        }
    }

    public void handleLikeClickAction(final BaseActivity baseActivity, final Comment comment) {

        CommentManager.getInstance(baseActivity.getApplicationContext())
                .isCommentExist(comment.getPostId(), comment.getId(), new OnObjectExistListener<Comment>() {
                    @Override
                    public void onDataChanged(boolean exist) {
                        if (exist) {
                            if (baseActivity.hasInternetConnection()) {
                                doHandleLikeClickAction(baseActivity, comment);
                            } else {
                                showWarningMessage(baseActivity, R.string.internet_connection_failed);
                            }
                        } else {
                            showWarningMessage(baseActivity, R.string.message_post_was_removed);
                        }
                    }
                });
    }

    private void showWarningMessage(BaseActivity baseActivity, int messageId) {
        if (baseActivity instanceof PostDetailsActivity) {
            ((PostDetailsActivity) baseActivity).showFloatButtonRelatedSnackBar(messageId);
        } else {
            baseActivity.showSnackBar(messageId);
        }
    }

    private void doHandleLikeClickAction(BaseActivity baseActivity, Comment comment) {
        ProfileStatus profileStatus = ProfileManager.getInstance(baseActivity).checkProfile();

        if (profileStatus.equals(ProfileStatus.PROFILE_CREATED)) {
            if (isListView) {
                likeClickActionLocal(comment);
            } else {
                likeClickAction(comment.getLikesCount());
            }
        } else {
            baseActivity.doAuthorization(profileStatus);
        }
    }
}
