package com.gykj.mvpbasemodule.module;

import android.app.Application;
import android.content.Context;

import com.gykj.mvpbasemodule.MvpBaseModule;
import com.gykj.mvpbasemodule.scope.ContextLife;
import com.gykj.mvpbasemodule.scope.PerApp;
import dagger.Module;
import dagger.Provides;

/**
 * @author zyp
 * 2019-05-09
 */
@Module
public class ApplicationModule {
    private Application mApplication;

    public ApplicationModule(Application application) {
        mApplication = application;
    }
    @Provides
    @PerApp
    @ContextLife("Application")
    public Context provideApplicationContext(){
        return MvpBaseModule.getApplication();
    }
}
