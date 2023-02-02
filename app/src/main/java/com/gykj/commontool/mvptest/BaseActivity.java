package com.gykj.commontool.mvptest;

import com.gykj.commontool.R;
import com.gykj.mvpbasemodule.BaseContract;
import com.gykj.mvpbasemodule.BaseMvpActivity;
import com.gykj.mvpbasemodule.MvpBaseModule;
import com.gykj.mvpbasemodule.component.BaseApplicationComponent;
import com.gykj.mvpbasemodule.module.ActivityModule;

/**
 * @author zyp
 * 2021/2/26
 */
public abstract class BaseActivity<T extends BaseContract.BasePresenter> extends BaseMvpActivity<T,ActivityComponent> {
    
    @Override
    protected void initActivityComponent() {
       mActivityComponent = DaggerActivityComponent.builder()
                .baseApplicationComponent(MvpBaseModule.getInstance()
                        .getBaseApplicationComponent())
                .activityModule(new ActivityModule(this))
                .build();
    }
    
}
