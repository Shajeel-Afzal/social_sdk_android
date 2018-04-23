package com.layer.xdk.ui.util;

import android.content.res.Resources;
import android.util.DisplayMetrics;

public class DisplayUtils {
    private static DisplayMetrics sDisplayMetrics = Resources.getSystem().getDisplayMetrics();

    public static int dpToPx(int dp) {
        return (int) (dp * sDisplayMetrics.density);
    }

    @SuppressWarnings("unused")
    public static int pxToDp(int px) {
        return (int) (px / sDisplayMetrics.density);
    }
}
