package sumatodev.com.social.model;

/**
 * Created by Ali on 01/03/2018.
 */

public class UsersThread {

    private String id;
    private Long createdDate;

    public UsersThread() {
    }

    public UsersThread(String id, Long createdDate) {
        this.id = id;
        this.createdDate = createdDate;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Long getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Long createdDate) {
        this.createdDate = createdDate;
    }
}
