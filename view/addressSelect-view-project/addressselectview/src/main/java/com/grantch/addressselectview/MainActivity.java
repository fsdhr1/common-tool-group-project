package com.grantch.addressselectview;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.blankj.utilcode.util.ToastUtils;
import com.grantch.addressselectview.adapter.AddressViewAdapter;
import com.grantch.addressselectview.data.AddressBean;
import com.grantch.addressselectview.data.LevelType;
import com.grantch.addressselectview.view.AddressSelectView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements AddressViewAdapter.ViewLevelSelectedListener {

    private AddressViewAdapter addressViewAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AddressSelectView as =findViewById(R.id.as);
        addressViewAdapter = new AddressViewAdapter(as);
        addressViewAdapter.setmCurrentLevelDatas(obtainData());
        as.setAdapter(addressViewAdapter);
        addressViewAdapter.setViewLevelSelectedListener(this);
    }

    private List<AddressBean> obtainData2(){
        List<AddressBean> list = new ArrayList<>();

        list.add(new AddressBean("辽宁"));
        list.add(new AddressBean("沈阳"));
        list.add(new AddressBean("江西"));
        list.add(new AddressBean("南昌"));

        list.add(new AddressBean("内蒙"));
        list.add(new AddressBean("西藏"));
        list.add(new AddressBean("四川"));
        list.add(new AddressBean("青海"));

        list.add(new AddressBean("福建"));
        list.add(new AddressBean("沈阳"));
        list.add(new AddressBean("广州"));
        list.add(new AddressBean("广西"));

        list.add(new AddressBean("辽宁"));
        list.add(new AddressBean("沈阳"));
        list.add(new AddressBean("深圳"));
        list.add(new AddressBean("南昌"));

        list.add(new AddressBean("辽宁"));
        list.add(new AddressBean("沈阳"));
        list.add(new AddressBean("湖北"));
        list.add(new AddressBean("南昌"));

        list.add(new AddressBean("河北"));
        list.add(new AddressBean("山东"));
        list.add(new AddressBean("苏州"));
        list.add(new AddressBean("南京"));

        list.add(new AddressBean("河南"));
        list.add(new AddressBean("沈阳"));
        list.add(new AddressBean("湖南"));
        list.add(new AddressBean("南昌"));
        return list;
    }

    private List<AddressBean> obtainData() {
        List<AddressBean> list = new ArrayList<>();

        list.add(new AddressBean("萍乡"));
        list.add(new AddressBean("高安"));
        list.add(new AddressBean("江西"));
        list.add(new AddressBean("天津"));


        list.add(new AddressBean("南昌"));
        list.add(new AddressBean("江西"));
        list.add(new AddressBean("南昌"));

        list.add(new AddressBean("长春"));
        list.add(new AddressBean("北京"));
        list.add(new AddressBean("江西"));
        list.add(new AddressBean("武汉"));

        list.add(new AddressBean("辽宁"));
        list.add(new AddressBean("沈阳"));
        list.add(new AddressBean("江西"));
        list.add(new AddressBean("乌鲁木齐"));

        list.add(new AddressBean("吉林"));
        list.add(new AddressBean("沈阳"));
        list.add(new AddressBean("长白山"));
        list.add(new AddressBean("黑龙江"));

        return list;
    }

    @Override
    public void seleted(LevelType currentLevel, AddressBean addreBean) {
        ToastUtils.showShort(addreBean.toString());
        addressViewAdapter.setNextLevelDatas(obtainData2());
    }
}