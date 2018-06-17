package sumatodev.com.social.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.messaging.RemoteMessage;

import sumatodev.com.social.Constants;
import sumatodev.com.social.R;
import sumatodev.com.social.managers.PostManager;
import sumatodev.com.social.ui.activities.MainActivity;
import sumatodev.com.social.ui.activities.PostDetailsActivity;

/**
 * Created by shajeelafzal on 17/06/2018.
 */

public class SocialNetworkNotificationUtils {

    private static final String TAG = SocialNetworkNotificationUtils.class.getSimpleName();
    private static final String NOTIFICATION_TITLE = "Title";
    private static final String NOTIFICATION_BODY = "Body";

    private static int notificationId = 0;

    private static final String POST_ID_KEY = "postId";
    private static final String AUTHOR_ID_KEY = "authorId";
    private static final String ACTION_TYPE_KEY = "actionType";
    private static final String TITLE_KEY = "title";
    private static final String BODY_KEY = "body";
    private static final String ICON_KEY = "icon";
    private static final String ACTION_TYPE_NEW_LIKE = "new_like";
    private static final String ACTION_TYPE_NEW_COMMENT = "new_comment";
    private static final String ACTION_TYPE_NEW_POST = "new_post";


    public static boolean handleNotification(Context context, RemoteMessage remoteMessage) {
        String receivedActionType = remoteMessage.getData().get(ACTION_TYPE_KEY);

        if (receivedActionType == null || receivedActionType.isEmpty())
            return false;

        LogUtil.logDebug(TAG, "Message Notification Action Type: " + receivedActionType);

        switch (receivedActionType) {
            case ACTION_TYPE_NEW_LIKE:
                parseCommentOrLike(context, remoteMessage);
                return true;
            case ACTION_TYPE_NEW_COMMENT:
                parseCommentOrLike(context, remoteMessage);
                return true;
            case ACTION_TYPE_NEW_POST:
                handleNewPostCreatedAction(context, remoteMessage);
                return true;
        }

        return false;
    }

    private static void parseCommentOrLike(Context context, RemoteMessage remoteMessage) {
        String notificationTitle = remoteMessage.getData().get(TITLE_KEY);
        String notificationBody = remoteMessage.getData().get(BODY_KEY);
        String notificationImageUrl = remoteMessage.getData().get(ICON_KEY);
        String postId = remoteMessage.getData().get(POST_ID_KEY);

        Intent backIntent = new Intent(context, MainActivity.class);
        Intent intent = new Intent(context, PostDetailsActivity.class);
        intent.putExtra(PostDetailsActivity.POST_ID_EXTRA_KEY, postId);

        Bitmap bitmap = getBitmapFromUrl(context, notificationImageUrl);

        sendNotification(context, notificationTitle, notificationBody, bitmap, intent, backIntent);

        LogUtil.logDebug(TAG, "Message Notification Body: " + remoteMessage.getData().get(BODY_KEY));
    }

    private static Bitmap getBitmapFromUrl(Context context, String imageUrl) {
        try {
            return Glide.with(context)
                    .load(imageUrl)
                    .asBitmap()
                    .centerCrop()
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .into(Constants.PushNotification.LARGE_ICONE_SIZE, Constants.PushNotification.LARGE_ICONE_SIZE)
                    .get();

        } catch (Exception e) {
            LogUtil.logError(TAG, "getBitmapfromUrl", e);
            return null;
        }
    }

    private static void sendNotification(Context context, String notificationTitle, String notificationBody, Bitmap bitmap, Intent intent, Intent backIntent) {
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        PendingIntent pendingIntent;

        if (backIntent != null) {
            backIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            Intent[] intents = new Intent[]{backIntent, intent};
            pendingIntent = PendingIntent.getActivities(context, notificationId++, intents, PendingIntent.FLAG_ONE_SHOT);
        } else {
            pendingIntent = PendingIntent.getActivity(context, notificationId++, intent, PendingIntent.FLAG_ONE_SHOT);
        }

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        String NOTIFICATION_CHANNEL_ID = "like_notifications_channel";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "Post or Comment Like Alerts", NotificationManager.IMPORTANCE_HIGH);

            // Configure the notification channel.
            notificationChannel.setDescription("Post Like!");
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.setVibrationPattern(new long[]{0, 1000, 500, 1000});
            notificationChannel.enableVibration(true);
            notificationManager.createNotificationChannel(notificationChannel);
        }

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = (NotificationCompat.Builder)
                new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
                        .setAutoCancel(true)   //Automatically delete the notification
                        .setWhen(System.currentTimeMillis())
                        .setSmallIcon(R.drawable.ic_push_notification_small) //Notification icon
                        .setContentIntent(pendingIntent)
                        .setContentTitle(notificationTitle)
                        .setContentText(notificationBody)
                        .setLargeIcon(bitmap)
                        .setSound(defaultSoundUri);

        notificationManager.notify(notificationId++ /* ID of notification */, notificationBuilder.build());
    }

    private static void handleNewPostCreatedAction(Context context, RemoteMessage remoteMessage) {
        String postAuthorId = remoteMessage.getData().get(AUTHOR_ID_KEY);
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        //Send notification for each users except author of post.
        if (firebaseUser != null && !firebaseUser.getUid().equals(postAuthorId)) {
            PostManager.getInstance(context.getApplicationContext()).incrementNewPostsCounter();
        }
    }

}
