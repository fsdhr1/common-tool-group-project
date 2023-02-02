package com.gykj.addressselect.interfaces;


import com.gykj.addressselect.bean.AddreBean;

public interface OnAddressSelectedListener {
    void onAddressSelected(int mIndex, AddreBean mProvince, AddreBean mCity, AddreBean mCounty, AddreBean mTown, AddreBean mVillage);
}
