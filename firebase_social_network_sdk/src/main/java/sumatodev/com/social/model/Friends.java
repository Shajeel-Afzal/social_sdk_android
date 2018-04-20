package sumatodev.com.social.model;

import java.util.Date;

/**
 * Created by Ali on 01/03/2018.
 */

public class Friends {

    private String id;
    private Long createdDate;
    private String type;

    public Friends() {
    }

    public Friends(String id, Long createdDate, String type) {
        this.id = id;
        this.createdDate = createdDate;
        this.type = type;
    }

    public Friends(String id, String type) {
        this.createdDate = new Date().getTime();
        this.id = id;
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
