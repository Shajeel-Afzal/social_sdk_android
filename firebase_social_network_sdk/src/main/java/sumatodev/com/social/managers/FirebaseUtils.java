package sumatodev.com.social.managers;

import com.google.firebase.database.DatabaseReference;

import sumatodev.com.social.ApplicationHelper;
import sumatodev.com.social.enums.Consts;

/**
 * Created by Ali on 01/03/2018.
 */

public class FirebaseUtils {

    public static DatabaseReference getDatabaseRef() {
        return ApplicationHelper.getDatabaseHelper()
                .getDatabaseReference();
    }

    public static DatabaseReference getFriendsRef() {
        return getDatabaseRef().child(Consts.FRIENDS_REF);
    }

    public static DatabaseReference getUserPublicInfoRef() {
        return getDatabaseRef().child(Consts.FIREBASE_PUBLIC_USERS);
    }
}
