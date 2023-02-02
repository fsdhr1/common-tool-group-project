package com.gykj.mvpbasemodule.module;

import android.app.Activity;
import android.content.Context;
import androidx.fragment.app.Fragment;
import com.gykj.mvpbasemodule.scope.ContextLife;
import com.gykj.mvpbasemodule.scope.PerFragment;
import dagger.Module;
import dagger.Provides;

/**
 * @author zyp
 * 2019-05-09
 */
@Module
public class FragmentModule {
    private Fragment mFragment;

    public FragmentModule(Fragment fragment) {
        mFragment = fragment;
    }
    @PerFragment
    @Provides
    @ContextLife("Activity")
    public Context provideActivityContext(){
        return mFragment.getActivity();
    }
    @Provides
    @PerFragment
    public Activity provideActivity(){
        return mFragment.getActivity();
    }
    @Provides
    @PerFragment

    public Fragment provideFragment(){
        return mFragment;
    }
}
