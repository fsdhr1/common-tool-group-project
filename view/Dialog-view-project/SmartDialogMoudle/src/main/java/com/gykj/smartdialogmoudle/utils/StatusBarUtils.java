package com.gykj.smartdialogmoudle.utils;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

public class StatusBarUtils {
    public static int getHeight(Context context) {
        int statusBarHeight = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            statusBarHeight = context.getResources().getDimensionPixelSize(resourceId);
        }
        return statusBarHeight;
    }
}
