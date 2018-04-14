package sumatodev.com.social.managers;

import android.content.Context;

import com.google.firebase.database.ValueEventListener;

import sumatodev.com.social.ApplicationHelper;
import sumatodev.com.social.managers.listeners.OnDataChangedListener;
import sumatodev.com.social.model.Friends;

/**
 * Created by Ali on 11/04/2018.
 */

public class FriendsManager extends FirebaseListenersManager {

    private static FriendsManager instance;
    private Context context;

    public static FriendsManager getInstance(Context context) {
        if (instance == null) {
            instance = new FriendsManager(context);
        }
        return instance;
    }

    public FriendsManager(Context context) {
        this.context = context;
    }

    public void getFriendsList(Context context, String userKey, String listType, OnDataChangedListener<Friends> onDataChangedListener) {

        DatabaseHelper databaseHelper = ApplicationHelper.getDatabaseHelper();
        ValueEventListener eventListener = databaseHelper.getFriendsList(userKey, listType, onDataChangedListener);
        addListenerToMap(context, eventListener);
    }
}
