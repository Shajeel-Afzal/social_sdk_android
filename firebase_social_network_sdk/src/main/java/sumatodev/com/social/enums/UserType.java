package sumatodev.com.social.enums;

/**
 * Created by Ali on 08/03/2018.
 */

public enum UserType {

    CURRENT(0), OTHERS(1);

    int status;

    UserType(int status) {
        this.status = status;
    }

    public int getStatus() {
        return status;
    }
}
