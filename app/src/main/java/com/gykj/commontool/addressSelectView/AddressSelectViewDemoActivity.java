package com.gykj.commontool.addressSelectView;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.grantch.addressselectview.adapter.AddressViewAdapter;
import com.grantch.addressselectview.data.AddressBean;
import com.grantch.addressselectview.data.LevelType;
import com.grantch.addressselectview.view.AddressSelectView;
import com.gykj.commontool.R;
import java.util.ArrayList;
import java.util.List;


public class AddressSelectViewDemoActivity extends AppCompatActivity implements AddressViewAdapter.ViewLevelSelectedListener{

    private AddressViewAdapter addressViewAdapter;
    private AddressBean mSelected;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_address_select_view_demo);
        AddressSelectView as =findViewById(R.id.as);
        as.setShowSlideBar(false);
        Button btSave =findViewById(R.id.bt_save);
        as.setSelectedColor(R.color.color_2A70FF);
        as.setUnSelectedColor(R.color.text_color_4d4d4d);
        addressViewAdapter = new AddressViewAdapter(as);
        Intent intent = getIntent();
        AddressBean addressBean = (AddressBean) intent.getSerializableExtra("selected");
        if (addressBean!=null){
            addressViewAdapter.setDefultSelectItem(LevelType.CITY,addressBean);
        }else {
            addressViewAdapter.setLevelDatas(LevelType.PROVINCE,obtainData());
        }


        as.setAdapter(addressViewAdapter);
        addressViewAdapter.setViewLevelSelectedListener(new AddressViewAdapter.ViewLevelSelectedListener() {
            @Override
            public void seleted(LevelType levelType, AddressBean addressBean) {
                if (levelType==LevelType.CITY){
                    mSelected = addressBean;
                }else {
                    addressViewAdapter.setNextLevelDatas(obtainData2());
                }
            }
        });

        btSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addressViewAdapter.saveSelectedItem(mSelected);
                Intent intent = new Intent();
                intent.putExtra("selected",mSelected);
                setResult(-1,intent);
                finish();
            }
        });
    }


    private List<AddressBean> obtainData2(){
        List<AddressBean> list = new ArrayList<>();

        list.add(new AddressBean("辽宁","2100"));
        list.add(new AddressBean("沈阳","2101"));
        list.add(new AddressBean("江西","2102"));
        list.add(new AddressBean("南昌","2103"));

        list.add(new AddressBean("内蒙","2104"));
        list.add(new AddressBean("西藏","2105"));
        list.add(new AddressBean("四川","2106"));
        list.add(new AddressBean("青海","2107"));

        list.add(new AddressBean("福建","2108"));
        list.add(new AddressBean("沈阳","2109"));
        list.add(new AddressBean("广州","2110"));
        list.add(new AddressBean("广西","2111"));

        list.add(new AddressBean("辽宁","2112"));
        list.add(new AddressBean("沈阳","2113"));
        list.add(new AddressBean("深圳","2114"));
        list.add(new AddressBean("南昌","2115"));

        list.add(new AddressBean("辽宁","2116"));
        list.add(new AddressBean("沈阳","2117"));
        list.add(new AddressBean("湖北","2118"));
        list.add(new AddressBean("南昌","2119"));

        list.add(new AddressBean("河北","2120"));
        list.add(new AddressBean("山东","2121"));
        list.add(new AddressBean("苏州","2122"));
        list.add(new AddressBean("南京","2123"));

        list.add(new AddressBean("河南","2124"));
        list.add(new AddressBean("沈阳","2125"));
        list.add(new AddressBean("湖南","2126"));
        list.add(new AddressBean("南昌","2127"));
        return list;
    }

    private List<AddressBean> obtainData() {
        List<AddressBean> list = new ArrayList<>();

        list.add(new AddressBean("萍乡","21"));
        list.add(new AddressBean("高安","22"));
        list.add(new AddressBean("江西","23"));
        list.add(new AddressBean("天津","24"));


        list.add(new AddressBean("南昌","25"));
        list.add(new AddressBean("江西","26"));
        list.add(new AddressBean("南昌","27"));

        list.add(new AddressBean("长春","28"));
        list.add(new AddressBean("北京","29"));
        list.add(new AddressBean("江西","30"));
        list.add(new AddressBean("武汉","31"));

        list.add(new AddressBean("辽宁","32"));
        list.add(new AddressBean("沈阳","33"));
        list.add(new AddressBean("江西","34"));
        list.add(new AddressBean("乌鲁木齐","35"));

        list.add(new AddressBean("吉林","36"));
        list.add(new AddressBean("沈阳","37"));
        list.add(new AddressBean("长白山","38"));
        list.add(new AddressBean("黑龙江","39"));

        return list;
    }

    @Override
    public void seleted(LevelType currentLevel, AddressBean addreBean) {
        ToastUtils.showShort(addreBean.toString());
        addressViewAdapter.setNextLevelDatas(obtainData2());
    }

}