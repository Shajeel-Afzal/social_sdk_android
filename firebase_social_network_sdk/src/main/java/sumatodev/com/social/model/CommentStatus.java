package sumatodev.com.social.model;

import java.io.Serializable;

/**
 * Created by Ali on 29/03/2018.
 */

public class CommentStatus implements Serializable {
    public boolean commentStatus = true;

    public CommentStatus() {
    }

    public CommentStatus(boolean commentStatus) {
        this.commentStatus = commentStatus;
    }
}
