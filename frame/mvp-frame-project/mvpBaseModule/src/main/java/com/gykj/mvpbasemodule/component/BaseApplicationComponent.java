package com.gykj.mvpbasemodule.component;

import android.content.Context;

import com.gykj.mvpbasemodule.module.ApplicationModule;
import com.gykj.mvpbasemodule.scope.ContextLife;
import com.gykj.mvpbasemodule.scope.PerApp;
import dagger.Component;


@PerApp
@Component(modules = ApplicationModule.class)
public interface BaseApplicationComponent {
    @ContextLife("Application")
    Context getApplication();
}