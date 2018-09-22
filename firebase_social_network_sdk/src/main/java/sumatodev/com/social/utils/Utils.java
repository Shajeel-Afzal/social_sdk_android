package sumatodev.com.social.utils;

import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.view.Display;
import android.view.WindowManager;
import android.widget.Toast;

import sumatodev.com.social.ApplicationHelper;
import sumatodev.com.social.R;
import sumatodev.com.social.enums.ProfileStatus;
import sumatodev.com.social.ui.activities.BaseActivity;
import sumatodev.com.social.ui.activities.LinkActivity;


public class Utils {

    public static int getDisplayWidth(Context context) {
        return getSize(context).x;
    }

    public static int getDisplayHeight(Context context) {
        return getSize(context).y;
    }

    private static Point getSize(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size;
    }

    public static boolean hasInternetConnection(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = null;
        if (cm != null) {
            activeNetwork = cm.getActiveNetworkInfo();
        }
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    public static void openUrlActivity(String linkUrl, Context context) {
        Intent intent = new Intent(context, LinkActivity.class);
        intent.putExtra(LinkActivity.URL_REF, linkUrl);
        context.startActivity(intent);
    }

    public static void doAuthorization(ProfileStatus status, Context context) {
        if (status.equals(ProfileStatus.NOT_AUTHORIZED) || status.equals(ProfileStatus.NO_PROFILE)) {
            startLoginActivity(context);
        }
    }

    private static void startLoginActivity(Context context) {
        if (ApplicationHelper.mLoginRequiredListener != null) {
            Toast.makeText(context, R.string.login_required_for_this_action, Toast.LENGTH_SHORT).show();
            ApplicationHelper.mLoginRequiredListener.loginRequired();
        }
    }

    public static void openYoutubeLink(String link, Context context){
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
        context.startActivity(intent);
    }
}
