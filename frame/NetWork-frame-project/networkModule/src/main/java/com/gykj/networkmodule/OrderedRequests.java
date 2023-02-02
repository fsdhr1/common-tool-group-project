package com.gykj.networkmodule;

import com.blankj.utilcode.util.ToastUtils;

/**
 * @author zyp
 * 2021/5/27
 */
public class OrderedRequests {
    private RequestBuilder[] mRequestBuilders;
     OrderedRequests(RequestBuilder ... requestBuilders){
        mRequestBuilders = requestBuilders;
    }
    
    public void request(){
         if(mRequestBuilders != null && mRequestBuilders.length > 1){ 
             RequestBuilder nextRequest = mRequestBuilders[0];
             nextRequest.postChainRequest(0,mRequestBuilders);
         }else {
             ToastUtils.showShort("请求队列不能为空");
         }
    }
}
