package com.gykj.commontool.selectlinearlayouttest;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.gykj.commontool.R;
import com.gykj.selectlinearlayout.SelectLinearLayout;
import com.gykj.selectlinearlayout.bean.Dict;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jyh on 2021-03-02
 */
public class SelectLinearLayoutTestActivity extends AppCompatActivity implements SelectLinearLayout.ISelectDict {

    SelectLinearLayout llSelect;
    private boolean includesAll = true;// 是否包括"全部"

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selectlinearlayout_test);

        llSelect = findViewById(R.id.llSelect);
        llSelect.setISelectDictListen(this);
        List<Dict> dicts = new ArrayList<>();
        String string[] = new String[]{"大豆", "冬小麦", "玉米", "苹果"};
        for (int i = 0; i < 4; i++) {
            Dict dict = new Dict();
            dict.setKey((i + 1) + "");
            dict.setValue(string[i]);
            dicts.add(dict);
        }
        llSelect.setDicts(dicts);// 设置字典集合（下拉列表数据）
        if (includesAll) {
            // 是否包括"全部"
            llSelect.setIncludesAll(true);// 设置下拉列表是否包括"全部"
            Dict dict = new Dict();
            dict.setValue("全部");
            dict.setKey("");
            llSelect.addDict(dict);// 追加字典实体
        }
        llSelect.setTvName("作物");// 设置字典名称

        llSelect.setOpenListener(true);// 设置下拉布局组件是否可点击，默认true

        Dict dict = new Dict();
        dict.setKey("07");
        dict.setValue("冬小麦");
        dicts.add(dict);
        llSelect.setDict(dict);// 设置要显示的字典实体

        Dict dict1 = llSelect.getOperationDict();// 获取当前选择的字典实体
    }

    /**
     * 选中回调
     *
     * @param dict
     */
    @Override
    public void selectDict(Dict dict) {
        llSelect.setColor(ContextCompat.getColor(this, R.color.colorPrimary));// 设置文本颜色
    }
}
