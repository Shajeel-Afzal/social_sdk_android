package sumatodev.com.social.managers.listeners;

import sumatodev.com.social.model.Comment;
import sumatodev.com.social.model.Post;

/**
 * Created by Ali on 06/04/2018.
 */

public interface OnCommentChangedListener {

    void onObjectChanged(Comment obj);

    void onError(String errorText);
}
