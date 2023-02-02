package com.grantch.addressselectview.data;

import android.content.Context;
import android.text.TextUtils;

import com.github.promeg.pinyinhelper.Pinyin;
import com.github.promeg.pinyinhelper.PinyinMapDict;
import com.github.promeg.tinypinyin.lexicons.android.cncity.CnCityDict;
import com.grantch.addressselectview.adapter.AddreassSelectorAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ************************
 * 项目名称：commontool
 *
 * @Author hxh
 * 创建时间：2021/12/10   9:35
 * ************************
 */
public class DataManager {
    private Context mContext;
    private List<String> heads;
    private static volatile DataManager singleton = null;
    private List<AddressBean> newDatas;

    private DataManager(Context context){
        this.mContext = context;
        Pinyin.init(Pinyin.newConfig().with(CnCityDict.getInstance(mContext)));
        addMorePinYinCovert();
        heads = new ArrayList<>();
    }

    public static DataManager getInstance(Context context) {
        if (context == null) {
            throw new IllegalArgumentException("context == null");
        }
        if (singleton == null) {
            synchronized (DataManager.class) {
                if (singleton == null) {
                    singleton = new DataManager(context);
                }
            }
        }
        return singleton;
    }


    /**
     * activity destory() 调用
     */
    public void destory(){
        mContext =null;
        singleton = null;
        heads =null;
        newDatas=null;
    }


    public List<String> getHeadList(){
        return heads;
    }

    public List<AddressBean> getDataList(){
        return newDatas;
    }

    private void addMorePinYinCovert() {
        Pinyin.init(Pinyin.newConfig()
                .with(new PinyinMapDict() {
                    @Override
                    public Map<String, String[]> mapping() {
                        HashMap<String, String[]> map = new HashMap<String, String[]>();
                        map.put("长江",  new String[]{"CHANG", "JIANG"});
                        map.put("长春",  new String[]{"CHANG", "CHUN"});
                        map.put("长白山",  new String[]{"CHANG", "BAI","SHAN"});
                        return map;
                    }
                }));
    }


    public void setData(List<AddressBean> datas){
        Map<String, List<AddressBean>> map = new HashMap<>();
        if (newDatas==null){
            newDatas = new ArrayList<>();
        }else {
            newDatas.clear();
        }
        //数据整理
        for (AddressBean a :datas){
           String head = covertToPinyin(a.getQhmc());
           if (map.keySet().contains(head)){
               List<AddressBean> headDatas =   map.get(head);
               headDatas.add(a);
           }else {
               List<AddressBean> newHeadDatas = new ArrayList<>();
               newHeadDatas.add(new AddressBean(head, AddreassSelectorAdapter.VIEW_TYPE_ITEM_TITLE));
               newHeadDatas.add(a);
               map.put(head,newHeadDatas);
           }
        }
        //排序,对map keyset进行排序
        List<String> list = new ArrayList(map.keySet());
        Collections.sort(list);
        heads.clear();
        heads.addAll(list);
        for (String key:list){
            newDatas.addAll(map.get(key));
        }
    }

    private String covertToPinyin(String ch){
       if (!TextUtils.isEmpty(ch)){
           String py = Pinyin.toPinyin(ch, "");
           return py.substring(0,1);
       }
       return ch;
    }
}
