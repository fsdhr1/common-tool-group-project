package com.gradtech.mapframev10.core.offline.editor

/**
 * @ClassName IToolInterceptor
 * @Description TODO
 * @Author: fs
 * @Date: 2022/12/20 14:50
 * @Version 2.0
 */
interface IToolInterceptor {
    /**
     * 存入临时层前执行
     *
     * @param
     * @param flags
     * @return
     */
    fun doHandle(o: Any?, vararg flags: String?): Boolean

    /**
     * 存入临时层后执行
     *
     * @param
     * @param flags
     * @return false不拦截，继续执行下面的代码,true 拦截，所有逻辑交给内部方法执行
     */
    fun afterHandle(o: Any?, vararg flags: String?)
}