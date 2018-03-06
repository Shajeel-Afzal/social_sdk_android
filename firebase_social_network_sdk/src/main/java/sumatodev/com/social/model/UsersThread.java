package sumatodev.com.social.model;

/**
 * Created by Ali on 01/03/2018.
 */

public class UsersThread {

    private String id;
    private Long createdDate;
    private String type;

    public UsersThread() {
    }

    public UsersThread(String id, Long createdDate, String type) {
        this.id = id;
        this.createdDate = createdDate;
        this.type = type;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
