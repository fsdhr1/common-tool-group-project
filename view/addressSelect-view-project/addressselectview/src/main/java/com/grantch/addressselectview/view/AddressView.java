package com.grantch.addressselectview.view;

import com.grantch.addressselectview.data.LevelType;
import com.grantch.addressselectview.data.AddressBean;

import java.util.List;

/**
 * ************************
 * 项目名称：commontool
 *
 * @Author hxh
 * 创建时间：2021/12/13   11:26
 * ************************
 */
public interface AddressView {

    void dataChanged(LevelType levelType, List<AddressBean> datas);

    void showLoading();

    void closeLoading();

    void delfultSelectedItem(LevelType levelType, AddressBean addreBean);

    void saveSelectedItem(AddressBean addreBean);

    void clearLevelSelected(LevelType levelType);
}
