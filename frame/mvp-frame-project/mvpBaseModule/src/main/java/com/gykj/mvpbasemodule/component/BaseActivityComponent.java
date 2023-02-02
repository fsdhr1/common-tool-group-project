package com.gykj.mvpbasemodule.component;

import android.app.Activity;
import android.content.Context;
import com.gykj.mvpbasemodule.scope.ContextLife;


public interface BaseActivityComponent {
    @ContextLife("Activity")
    Context getActivityContext();

    @ContextLife("Application")
    Context getApplicationContext();

    Activity getActivity();
}
