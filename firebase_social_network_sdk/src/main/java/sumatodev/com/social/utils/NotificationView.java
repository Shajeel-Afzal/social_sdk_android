package sumatodev.com.social.utils;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import sumatodev.com.social.R;
import sumatodev.com.social.ui.activities.MainActivity;

/**
 * Created by Ali on 28/02/2018.
 */

public class NotificationView {

    private final static String TAG = NotificationView.class.getName();
    private NotificationCompat.Builder mBuilder;
    private final Context mContext;
    private static final int mId = 58956;
    private NotificationManager mNotifyManager;
    private static NotificationView instance;

    public static NotificationView getInstance(Context context) {
        if (instance == null) {
            instance = new NotificationView(context);
        }
        return instance;
    }

    public NotificationView(Context mContext) {
        this.mContext = mContext;
    }

    public void setNotification(boolean value, String mTitle) {
        if (value) {
            initNotification();
            setProgressNotification(mTitle);
            updateProgressNotification();
        } else {
            setCompletedNotification(mTitle);
        }
    }

    private void initNotification() {
        mNotifyManager = (NotificationManager) mContext
                .getSystemService(Context.NOTIFICATION_SERVICE);
        mBuilder = new NotificationCompat.Builder(mContext, "notification");
    }


    private void setProgressNotification(String mTitle) {
        mBuilder.setContentTitle(mTitle)
                .setSmallIcon(R.drawable.ic_uploading)
                .setAutoCancel(false);
    }

    private void updateProgressNotification() {
        mBuilder.setProgress(0, 0, true);
        mNotifyManager.notify(mId, mBuilder.build());
    }


    /**
     * the last notification
     *
     * @param mTitle
     */
    private void setCompletedNotification(String mTitle) {
        mBuilder.setSmallIcon(R.drawable.ic_uploading_complete).setContentTitle(mTitle)
                .setAutoCancel(true);

        mBuilder.setProgress(0, 0, false);
        // Creates an explicit intent for an Activity in your app
        Intent resultIntent = new Intent(mContext, MainActivity.class);
        // The stack builder object will contain an artificial back stack for
        // the
        // started Activity.
        // This ensures that navigating backward from the Activity leads out of
        // your application to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(mContext);
        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(MainActivity.class);
        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0,
                PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(resultPendingIntent);

        mNotifyManager.notify(mId, mBuilder.build());
    }

}
