package com.gykj.commontool.mvptest.testmodule;

import android.app.Activity;
import com.gykj.commontool.R;
import com.gykj.commontool.mvptest.BaseActivity;

/**
 * @author zyp
 * 2021/3/1
 */
public class MvpTestActivity extends BaseActivity<MvpTestPresenter> {
    @Override
    protected void initInjector() {
       mActivityComponent.inject(this);//在此进行注入
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_test;
    }

    @Override
    protected void initView() {
        super.initView();
        hideTitle();
        //setStatusBarColor(R.color.transparent);
    }

    @Override
    public int getPrimaryColor() {
        return getResources().getColor(R.color.colorPrimary);
    }

    /*@Override
    protected int setTheme() {
        return R.style.TranslucentTheme;
    }*/
    
    
}
