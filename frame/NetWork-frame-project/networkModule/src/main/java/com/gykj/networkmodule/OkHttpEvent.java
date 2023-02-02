package com.gykj.networkmodule;

/**
 * @author zyp
 * 2019-05-09
 */
 class OkHttpEvent {
    public long dnsStartTime;
    public long dnsEndTime;
    public long responseBodySize;
    //判断有没有成功
    public boolean apiSuccess;
    public String errReson;
}
