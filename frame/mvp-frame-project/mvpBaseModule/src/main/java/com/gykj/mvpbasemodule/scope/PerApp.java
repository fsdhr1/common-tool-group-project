package com.gykj.mvpbasemodule.scope;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;

import javax.inject.Scope;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * @author zyp
 * 2019-05-09
 */
@Scope
@Documented
@Retention(RUNTIME)

public @interface PerApp {
}
