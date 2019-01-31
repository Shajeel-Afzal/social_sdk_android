package sumatodev.com.social.managers;

import android.content.Context;

import com.google.firebase.database.ValueEventListener;

import sumatodev.com.social.ApplicationHelper;
import sumatodev.com.social.managers.listeners.OnDataChangedListener;
import sumatodev.com.social.managers.listeners.OnFollowStatusChanged;
import sumatodev.com.social.managers.listeners.OnObjectExistListener;
import sumatodev.com.social.model.Friends;
import sumatodev.com.social.model.UsersPublic;

/**
 * Created by Ali on 11/04/2018.
 */

public class UsersManager extends FirebaseListenersManager {

    private static UsersManager instance;
    private Context context;

    public static UsersManager getInstance(Context context) {
        if (instance == null) {
            instance = new UsersManager(context);
        }
        return instance;
    }

    public UsersManager(Context context) {
        this.context = context;
    }

    public void getFriendsList(Context context, String userKey, String listType,
                               OnDataChangedListener<Friends> onDataChangedListener) {
        DatabaseHelper databaseHelper = ApplicationHelper.getDatabaseHelper();
        ValueEventListener eventListener = databaseHelper.getFriendsList(userKey, listType, onDataChangedListener);
        addListenerToMap(context, eventListener);
    }

    public void getUsersList(Context context, String searchString, OnDataChangedListener<UsersPublic> onDataChangedListener) {
        DatabaseHelper reference = ApplicationHelper.getDatabaseHelper();
        ValueEventListener valueEventListener = reference.getSearchList(searchString, onDataChangedListener);
        addListenerToMap(context, valueEventListener);
    }


    public void getFollowStatus(Context context, String userKey, OnFollowStatusChanged onFollowStatusChanged) {
        DatabaseHelper reference = ApplicationHelper.getDatabaseHelper();
        ValueEventListener eventListener = reference.checkFollowStatus(userKey, onFollowStatusChanged);
        addListenerToMap(context, eventListener);
    }

    public void getAllUsersList(Context context, OnDataChangedListener<UsersPublic> onDataChangedListener) {
        DatabaseHelper databaseHelper = ApplicationHelper.getDatabaseHelper();
        ValueEventListener eventListener = databaseHelper.getAllUsersList(onDataChangedListener);
        addListenerToMap(context, eventListener);
    }

    public void isUserFollowing(String authorId, String currentUid, OnObjectExistListener onObjectExistListener) {
        ApplicationHelper.getDatabaseHelper().isUserFollowing(authorId,currentUid, onObjectExistListener);
    }
}
