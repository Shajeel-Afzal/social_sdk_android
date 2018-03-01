package sumatodev.com.social.managers;

import android.content.Context;

/**
 * Created by Ali on 01/03/2018.
 */

public class FollowManager {

    private static final String TAG = FollowManager.class.getSimpleName();
    private static FollowManager instance;


    private Context context;

    public static FollowManager getInstance(Context context) {
        if (instance == null) {
            instance = new FollowManager(context);
        }

        return instance;
    }

    private FollowManager(Context context) {
        this.context = context;
    }
}
