package sumatodev.com.social.model;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Ali on 16/02/2018.
 */

public class UsersPublic {

    private String id;
    private String username;
    private String photoUrl;

    public UsersPublic() {
    }

    public UsersPublic(String id, String username, String photoUrl) {
        this.id = id;
        this.username = username;
        this.photoUrl = photoUrl;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    @Exclude
    Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();

        result.put("username", username);
        result.put("photoUrl", photoUrl);
        result.put("id", id);

        return result;
    }
}
