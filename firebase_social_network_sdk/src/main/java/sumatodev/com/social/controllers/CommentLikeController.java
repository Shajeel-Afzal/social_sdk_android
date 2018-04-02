package sumatodev.com.social.controllers;

import android.content.Context;
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
import sumatodev.com.social.ui.activities.PostDetailsActivity;

/**
 * Created by Ali on 02/04/2018.
 */

public class CommentLikeController {

    private static final int ANIMATION_DURATION = 300;

    public static enum AnimationType {
        COLOR_ANIM, BOUNCE_ANIM
    }

    private Context context;
    private String postId;
    private String commentId;

    private AnimationType likeAnimationType = AnimationType.BOUNCE_ANIM;


    private TextView likes_count;
    private TextView like_action;


    private boolean isListView = false;

    private boolean isLiked = false;
    private boolean updatingLikeCounter = true;


    public CommentLikeController(Context context, Comment comment, TextView likes_count,
                                 TextView like_action, boolean isListView) {
        this.context = context;
        this.postId = comment.getPostId();
        this.commentId = comment.getId();
        this.likes_count = likes_count;
        this.like_action = like_action;
        this.isListView = isListView;
    }


    private void likeClickAction(long prevValue) {

        if (!updatingLikeCounter) {
            //startAnimateLikeButton(likeAnimationType);
            if (!isLiked) {
                addLike(prevValue);
            } else {
                removeLike(prevValue);
            }
        }
    }


    private void likeClickActionLocal(Comment comment) {
        setUpdatingLikeCounter(false);
        likeClickAction(comment.getLikesCount());
        updateLocalCommentLikeCounter(comment);
    }

    private void addLike(long prevValue) {
        updatingLikeCounter = true;
        isLiked = true;
        likes_count.setText(String.valueOf(prevValue + 1));
        ApplicationHelper.getDatabaseHelper().updateCommentLike(postId, commentId);
    }

    private void removeLike(long prevValue) {
        updatingLikeCounter = true;
        isLiked = false;
        likes_count.setText(String.valueOf(prevValue - 1));
        ApplicationHelper.getDatabaseHelper().removeCommentLike(postId, commentId);
    }

    public void setUpdatingLikeCounter(boolean updatingLikeCounter) {
        this.updatingLikeCounter = updatingLikeCounter;
    }

    private void updateLocalCommentLikeCounter(Comment comment) {
        if (isLiked) {
            comment.setLikesCount(comment.getLikesCount() + 1);
        } else {
            comment.setLikesCount(comment.getLikesCount() - 1);
        }
    }

    public void initLike(boolean isLiked) {
        int liked = context.getResources().getColor(R.color.primary);
        int likeDefault = context.getResources().getColor(R.color.secondary_text);
        like_action.setTextColor(isLiked ? liked : likeDefault);
        likes_count.setTextColor(isLiked ? liked : likeDefault);

        this.isLiked = isLiked;
    }

    public void handleLikeClickAction(final BaseActivity baseActivity, final Comment comment) {
        PostManager.getInstance(baseActivity.getApplicationContext()).isPostExistSingleValue(comment.getPostId(),
                new OnObjectExistListener<Post>() {
                    @Override
                    public void onDataChanged(boolean exist) {
                        if (exist) {
                            if (baseActivity.hasInternetConnection()) {
                                checkCommentExist(baseActivity, comment);
                            } else {
                                showWarningMessage(baseActivity, R.string.internet_connection_failed);
                            }
                        } else {
                            showWarningMessage(baseActivity, R.string.message_post_was_removed);
                        }
                    }
                });
    }

    private void checkCommentExist(final BaseActivity baseActivity, final Comment comment) {
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
}
