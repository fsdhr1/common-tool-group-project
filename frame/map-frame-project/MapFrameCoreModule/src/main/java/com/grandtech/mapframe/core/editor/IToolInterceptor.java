package com.grandtech.mapframe.core.editor;

/**
 * Created by zy on 2019/2/24.
 */

public interface IToolInterceptor {


    /**
     * 存入临时层前执行
     *
     * @param
     * @param flags
     * @return
     */
    public boolean doHandle(Object o, String... flags);

    /**
     * 存入临时层后执行
     *
     * @param
     * @param flags
     * @return false不拦截，继续执行下面的代码,true 拦截，所有逻辑交给内部方法执行
     */
    public void afterHandle(Object o, String... flags);
}
