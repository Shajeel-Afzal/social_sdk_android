package sumatodev.com.social.enums;

public enum FollowStatus {

    FOLLOWING(0), FOLLOWERS(1), FOLLOW(2), REQUESTED(3);

    int status;

    FollowStatus(int status) {
        this.status = status;
    }

    public int getStatus() {
        return status;
    }
}
