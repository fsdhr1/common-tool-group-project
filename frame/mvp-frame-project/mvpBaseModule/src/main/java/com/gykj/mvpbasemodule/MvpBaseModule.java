package com.gykj.mvpbasemodule;

import java.lang.ref.SoftReference;

import android.app.Application;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.Utils;
import com.gykj.mvpbasemodule.component.BaseApplicationComponent;
import com.gykj.mvpbasemodule.component.DaggerBaseApplicationComponent;
import com.gykj.mvpbasemodule.module.ApplicationModule;

/**
 * @author zyp
 * 2021/2/26
 */
public class MvpBaseModule {
    private static SoftReference<Application> applicationSoftReference;

    private BaseApplicationComponent mBaseApplicationComponent;
    private static MvpBaseModule mMvpBaseModule;
    public static void init(Application application){
        applicationSoftReference = new SoftReference<>(application);
        if(mMvpBaseModule == null){
            mMvpBaseModule = new MvpBaseModule();
        }
        mMvpBaseModule.initApplicationComponent();
    }
    
    public static Application getApplication(){
        return applicationSoftReference.get();
    }

    private void initApplicationComponent() {
        mBaseApplicationComponent =
                DaggerBaseApplicationComponent.builder()
                        .applicationModule(new ApplicationModule(getApplication())).build();
        Utils.init(getApplication());
    }

    public BaseApplicationComponent getBaseApplicationComponent() {
        return mBaseApplicationComponent;
    }
    
    public static MvpBaseModule getInstance(){
        if (mMvpBaseModule == null){
            LogUtils.eTag("MvpBaseModule","请先初始化MvpBaseModule!");
        }
        return mMvpBaseModule;
    }
}
