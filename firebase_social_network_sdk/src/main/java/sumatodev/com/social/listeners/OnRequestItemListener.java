package sumatodev.com.social.listeners;

import android.view.View;

/**
 * Created by Ali on 19/02/2018.
 */

public interface OnRequestItemListener {
    void onItemClick(View view, String userKey);

    void onAcceptClick(String userKey);

    void onRejectClick(String userKey);
}
