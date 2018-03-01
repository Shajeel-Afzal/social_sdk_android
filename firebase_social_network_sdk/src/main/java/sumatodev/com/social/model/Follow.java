package sumatodev.com.social.model;

/**
 * Created by Ali on 01/03/2018.
 */

public class Follow {

    private String id;
    private String authorId;
    private long createdDate;

    public Follow() {
    }

    public Follow(String id, String authorId, long createdDate) {
        this.id = id;
        this.authorId = authorId;
        this.createdDate = createdDate;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAuthorId() {
        return authorId;
    }

    public void setAuthorId(String authorId) {
        this.authorId = authorId;
    }

    public long getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(long createdDate) {
        this.createdDate = createdDate;
    }
}
