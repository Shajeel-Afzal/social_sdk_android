package sumatodev.com.social.managers;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import sumatodev.com.social.enums.Consts;

/**
 * Created by Ali on 01/03/2018.
 */

public class FirebaseUtils {


    public static DatabaseReference getFriendsRef() {
        return FirebaseDatabase.getInstance()
                .getReference(Consts.FRIENDS_REF);
    }
}
