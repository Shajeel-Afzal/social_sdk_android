package sumatodev.com.social.enums;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static sumatodev.com.social.enums.Consts.FollowType.FOLLOWER;
import static sumatodev.com.social.enums.Consts.FollowType.FOLLOWING;
import static sumatodev.com.social.enums.Consts.FollowType.REQUESTED;


/**
 * Created by Ali on 01/03/2018.
 */

public class Consts {

    public static final String USER_KEY = "userKey";
    public static final String FRIENDS_REF = "friends";
    public static final String REQUEST_LIST_REF = "requestList";
    public static final String FOLLOWERS_LIST_REF = "followersList";
    public static final String FOLLOWING_LIST_REF = "followingList";
    public static final String FIREBASE_PUBLIC_USERS = "users_public";
    public static final String FIREBASE_LOCATION_USERS = "profiles";
    public static final String POSTS_REF = "posts";
    public static final String COMMENT_STATUS_REF = "commentStatus";
    public static final String POST_COMMENTS_REF = "post-comments";
    public static final String COMMENTS_LIKES_REF = "comment-likes";
    public static final String ACCOUNT_DISABLED = "disabled";
    public static final String ACCOUNT_ACTIVE = "active";
    public static final String ACCOUNT_STATUS = "accountStatus";
    public static final String FOLLOW_KEY = "follow";


    @IntDef({FOLLOWER, FOLLOWING, REQUESTED})
    @Retention(RetentionPolicy.SOURCE)
    public @interface FollowType {
        int FOLLOWER = 100;
        int FOLLOWING = 200;
        int REQUESTED = 300;
    }

}
