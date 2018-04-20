package sumatodev.com.social.controllers;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.Button;

import com.percolate.caffeine.DialogUtils;

import sumatodev.com.social.ApplicationHelper;
import sumatodev.com.social.R;
import sumatodev.com.social.enums.Consts;
import sumatodev.com.social.enums.ProfileStatus;
import sumatodev.com.social.managers.ProfileManager;
import sumatodev.com.social.managers.UsersManager;
import sumatodev.com.social.managers.listeners.OnFollowStatusChanged;
import sumatodev.com.social.managers.listeners.OnTaskCompleteListener;
import sumatodev.com.social.ui.activities.BaseActivity;

public class FollowController {

    private static final String TAG = FollowController.class.getSimpleName();
    private Context context;
    private String userKey;
    private Button followBtn;

    private boolean isRequested = false;
    private boolean isFollowing = false;

    public FollowController(Context context, String userKey, Button followBtn) {
        this.context = context;
        this.userKey = userKey;
        this.followBtn = followBtn;

    }

    public void checkFollowStatus() {
        UsersManager.getInstance(context).getFollowStatus(context, userKey, new OnFollowStatusChanged() {
            @Override
            public void isStatus(String status) {
                Log.d(TAG, "" + status);
                switch (status) {
                    case Consts.FOLLOWERS_LIST_REF:
                        currentStatusIs(R.string.following);
                        isFollowing = true;
                        break;
                    case Consts.REQUEST_LIST_REF:
                        currentStatusIs(R.string.requested);
                        isRequested = true;
                        break;
                    case Consts.FOLLOW_KEY:
                        currentStatusIs(R.string.follow);
                        break;

                }
            }
        });
    }


    public void handleFollowAction(BaseActivity activity, String userKey) {

        ProfileStatus profileStatus = ProfileManager.getInstance(activity).checkProfile();
        if (profileStatus.equals(ProfileStatus.PROFILE_CREATED)) {
            doHandleClickAction(activity, userKey);
        } else {
            activity.doAuthorization(profileStatus);
        }
    }

    private void doHandleClickAction(BaseActivity activity, final String userKey) {
        if (isRequested) {
            ApplicationHelper.getDatabaseHelper().removeFollowRequest(userKey, new OnTaskCompleteListener() {
                @Override
                public void onTaskComplete(boolean success) {
                    isRequested = false;
                    currentStatusIs(R.string.follow);
                }
            });
        } else if (isFollowing) {

            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setMessage("Remove Following?")
                    .setNegativeButton(R.string.button_title_cancel, null)
                    .setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                            ApplicationHelper.getDatabaseHelper().removeFromFollowing(userKey, new OnTaskCompleteListener() {
                                @Override
                                public void onTaskComplete(boolean success) {
                                    isFollowing = false;
                                    currentStatusIs(R.string.follow);
                                }
                            });

                        }
                    });

            builder.create().show();

        } else {
            ApplicationHelper.getDatabaseHelper().updateFollowRequest(userKey, new OnTaskCompleteListener() {
                @Override
                public void onTaskComplete(boolean success) {
                    isRequested = true;
                    currentStatusIs(R.string.requested);
                }
            });
        }
    }


    private void currentStatusIs(int status) {
        followBtn.setText(status);
    }
}
