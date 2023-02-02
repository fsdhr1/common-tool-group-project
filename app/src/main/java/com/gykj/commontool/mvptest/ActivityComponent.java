package com.gykj.commontool.mvptest;

import com.gykj.commontool.mvptest.testmodule.MvpTestActivity;
import com.gykj.mvpbasemodule.component.BaseActivityComponent;
import com.gykj.mvpbasemodule.component.BaseApplicationComponent;
import com.gykj.mvpbasemodule.module.ActivityModule;
import com.gykj.mvpbasemodule.scope.PerActivity;
import dagger.Component;

/**
 * @author zyp
 * 2021/3/1
 */
@PerActivity
@Component(dependencies = BaseApplicationComponent.class,modules = ActivityModule.class)
public interface ActivityComponent extends BaseActivityComponent {
    void inject(MvpTestActivity mvpTestActivity);//加入业务类的Activity
}
