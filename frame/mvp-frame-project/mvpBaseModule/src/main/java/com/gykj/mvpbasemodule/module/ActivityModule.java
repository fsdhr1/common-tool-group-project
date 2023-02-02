package com.gykj.mvpbasemodule.module;

import android.app.Activity;
import android.content.Context;
import com.gykj.mvpbasemodule.scope.ContextLife;
import com.gykj.mvpbasemodule.scope.PerActivity;
import dagger.Module;
import dagger.Provides;

/**
 * @author zyp
 * 2019-05-09
 */
@Module
public class ActivityModule {
    private Activity mActivity;

    public ActivityModule(Activity activity) {
        mActivity = activity;
    }
    @PerActivity
    @Provides
    @ContextLife("Activity")
    public Context provideActivityContext(){
        return mActivity;
    }
    @Provides
    @PerActivity
    public Activity provideActivity() {
        return mActivity;
    }
}
