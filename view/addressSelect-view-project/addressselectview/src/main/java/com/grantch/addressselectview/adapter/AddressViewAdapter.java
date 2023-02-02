package com.grantch.addressselectview.adapter;

import com.blankj.utilcode.util.SPUtils;
import com.grantch.addressselectview.data.AddressBean;
import com.grantch.addressselectview.view.AddressView;
import com.grantch.addressselectview.data.LevelType;

import java.util.List;

/**
 * ************************
 * 项目名称：commontool
 * 初始化view的一些数据类型
 * @Author hxh
 * 创建时间：2021/12/13   10:40
 * ************************
 */
public  class AddressViewAdapter {
    private LevelType mMaxLevel =LevelType.PROVINCE;
    private  LevelType mMinLevel =LevelType.VILLAGE;
    private  LevelType mCurrentLevel =mMaxLevel;
    private AddressView mAddressView;
    private List<AddressBean> mDatas;
    private ViewLevelSelectedListener mViewLevelSelectedListener;

    public AddressViewAdapter(AddressView addressView ){
        mAddressView =addressView;
    }

    public void setMaxLevel(LevelType maxLevel){
        mMaxLevel = maxLevel;
    }

    public LevelType getmMaxLevel() {
        return mMaxLevel;
    }

    public LevelType getmMinLevel() {
        return mMinLevel;
    }


    public void setMinLevel (LevelType minLevel){
        mMinLevel =minLevel;
    }

    public void setDefultSelectItem(LevelType type, AddressBean addreBean){
        if (addreBean!=null&&type!=null){
            mAddressView.delfultSelectedItem(type,addreBean);
        }
    }

    public void saveSelectedItem(AddressBean addreBean){
        mAddressView.saveSelectedItem(addreBean);
    }

    public void clearDefultSelected(){
        SPUtils.getInstance("selectview").clear();
    }

    public void recoverLevelData(LevelType levelType){
        mAddressView.clearLevelSelected(levelType);
    }

    public void currentLevelSelected(LevelType currentLevel, AddressBean addreBean){
        mCurrentLevel =currentLevel;
        if (mViewLevelSelectedListener!=null){
            mViewLevelSelectedListener.seleted(currentLevel,addreBean);
        }
    }

    public void setLevelDatas(LevelType level, List<AddressBean> datas ){
        if (level!=null&&level.getmLevel()>mMaxLevel.getmLevel())return;
        if (level!=null&&level.getmLevel()<mMinLevel.getmLevel())return;
        if (level==null){
            setNextLevelDatas(datas);
           return;
        }
        mDatas = datas;
        mAddressView.dataChanged(level,mDatas);
    }

    public void setmCurrentLevelDatas(List<AddressBean> datas){
        mDatas = datas;
        mAddressView.dataChanged(mCurrentLevel,datas);
    }

    public void setNextLevelDatas( List<AddressBean> datas){
        mDatas = datas;
        LevelType level = mCurrentLevel;
        switch (mCurrentLevel){
            case PROVINCE:
                level= LevelType.CITY;
                break;
            case CITY:
                level = LevelType.COUNTY;
                break;
            case COUNTY:
                level = LevelType.TOWN;
                break;
            case TOWN:
                level = LevelType.VILLAGE;
                break;
        }
        mAddressView.dataChanged(level,mDatas);
    }

    public void setViewLevelSelectedListener(ViewLevelSelectedListener listener){
        mViewLevelSelectedListener = listener;
    }

    public interface ViewLevelSelectedListener{
        void seleted(LevelType currentLevel, AddressBean addreBean);
    }
}
