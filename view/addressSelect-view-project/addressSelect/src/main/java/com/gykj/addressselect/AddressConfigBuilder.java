package com.gykj.addressselect;

/**
 * 地址选择器配置类
 * Created by ZhaiJiaChang.
 * Date: 2021/10/22
 */
public class AddressConfigBuilder {

    //
    private Boolean canceledOnTouchOutside;


    public Boolean getCanceledOnTouchOutside() {
        return canceledOnTouchOutside;
    }

    public void setCanceledOnTouchOutside(Boolean mCanceledOnTouchOutside) {
        canceledOnTouchOutside = mCanceledOnTouchOutside;
    }
}
