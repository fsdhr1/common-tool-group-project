package com.gykj.mvpbasemodule.component;

import android.app.Activity;
import android.content.Context;
import com.gykj.mvpbasemodule.scope.ContextLife;


public interface BaseFragmentComponent {
    @ContextLife("Activity")
    Context getAcitivtyContext();

    @ContextLife("Application")
    Context getApplicationContext();

    Activity getAcitivty();

}
