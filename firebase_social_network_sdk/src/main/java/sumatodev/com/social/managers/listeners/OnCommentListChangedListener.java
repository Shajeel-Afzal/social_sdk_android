package sumatodev.com.social.managers.listeners;

import sumatodev.com.social.model.CommentListResult;

/**
 * Created by Ali on 06/04/2018.
 */

public interface OnCommentListChangedListener<Comment> {

    void onListChanged(CommentListResult result);

    void onCanceled(String message);
}
