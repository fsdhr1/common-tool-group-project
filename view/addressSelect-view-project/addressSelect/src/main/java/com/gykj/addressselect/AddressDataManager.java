package com.gykj.addressselect;


import android.content.Context;
import android.text.TextUtils;
import android.widget.Toast;

import com.blankj.utilcode.util.ToastUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.gykj.addressselect.bean.AddreBean;
import com.gykj.addressselect.net.RequestUtil;
import com.gykj.addressselect.service.AddressApi;
import com.gykj.addressselect.service.Constant;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;

public class AddressDataManager {
    private Context mContext;

    public AddressDataManager(Context context) {
        mContext = context;
    }

    public interface AddressCallBack<T> {
        void onAddressCallBack(List<T> t);
    }

    /**
     * 获取省市县乡
     *
     * @param mAddressCallBack
     */
    public void getAddressDataList(String qhdm, AddressCallBack mAddressCallBack) {
        // 得到所有省份的数据
        HashMap mHashMap = new HashMap();
        mHashMap.put("qhdm", qhdm);
        RequestUtil.request(AddressApi.class, mContext, new RequestUtil.NetWorkResult<String>() {
            @Override
            public void onSuccess(String mData) {
                try {
                    JSONObject jsonObject = new JSONObject(mData);
                    int code = jsonObject.getInt("code");
                    if (code != 0) {
                        showToast(jsonObject.getString("msg"));
                        if (mAddressCallBack != null) {
                            mAddressCallBack.onAddressCallBack(null);
                        }
                        return;
                    }
                    JSONArray data = jsonObject.getJSONArray("data");// 解析出省份和直辖市的数组
                    List<AddreBean> mList = new Gson().fromJson(data.toString(), new TypeToken<List<AddreBean>>() {
                    }.getType());
                    if (mAddressCallBack != null) {
                        mAddressCallBack.onAddressCallBack(mList);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    //
                    if (mAddressCallBack != null) {
                        mAddressCallBack.onAddressCallBack(null);
                    }
                }
            }
        }).getAddressByQhdm(mHashMap);
    }

    public void getVillageDataList(String townCode, AddressCallBack mAddressCallBack) {
        RequestUtil.request(AddressApi.class, mContext, new RequestUtil.NetWorkResult<String>() {
            @Override
            public void onSuccess(String mData) {
                try {
                    JSONObject jsonObject = new JSONObject(mData);
                    int code = jsonObject.getInt("code");
                    if (code != 0) {
                        showToast(jsonObject.getString("msg"));
                        if (mAddressCallBack != null) {
                            mAddressCallBack.onAddressCallBack(null);
                        }
                        return;
                    }
                    JSONArray data = jsonObject.getJSONArray("data");// 解析出省份和直辖市的数组
                    List<AddreBean> mList = new Gson().fromJson(data.toString(), new TypeToken<List<AddreBean>>() {
                    }.getType());
                    if (mAddressCallBack != null) {
                        mAddressCallBack.onAddressCallBack(mList);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    //
                    if (mAddressCallBack != null) {
                        mAddressCallBack.onAddressCallBack(null);
                    }
                }
            }
        }).httpVillages(townCode);
    }

    public void showToast(String msg) {
        if (!TextUtils.isEmpty(msg)) {
            Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();
        }
    }
}
