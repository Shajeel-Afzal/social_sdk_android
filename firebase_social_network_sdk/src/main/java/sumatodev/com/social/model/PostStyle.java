package sumatodev.com.social.model;

import java.io.Serializable;

/**
 * Created by Ali on 05/04/2018.
 */

public class PostStyle implements Serializable {

    public int bg_color;

    public PostStyle() {
    }

    public PostStyle(int bg_color) {
        this.bg_color = bg_color;
    }
}
