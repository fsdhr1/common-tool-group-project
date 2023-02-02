package com.gykj.commontool.timepickertest;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.blankj.utilcode.util.ToastUtils;
import com.gykj.commontool.R;
import com.gykj.timepickerview.OptionsPickerView;
import com.gykj.timepickerview.TimePickerView;

/**
 * Created by jyh on 2021-03-11
 * 时间选择器控件
 */
public class TimePickerTestActivity extends AppCompatActivity {

    LinearLayout llDate,llOption;
    TextView tvDate;
    
    private TimePickerView mTimePickerView;
    private OptionsPickerView mOptionsPickerView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timepicker_test);

        llDate = findViewById(R.id.llDate);
        llOption = findViewById(R.id.llOption);
        tvDate = findViewById(R.id.tvDate);

        // 初始化时间选择器
        initDatePicker();
        initOptionsPicker();
        llDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mTimePickerView.show();
            }
        });
        llOption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOptionsPickerView.show();
            }
        });
    }

    /**
     * 初始化时间选择器
     */
    private void initDatePicker() {
        Date date = new Date();
        SimpleDateFormat mDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        GregorianCalendar mGc = new GregorianCalendar();
        mGc.setTime(date);

        Calendar startDate = Calendar.getInstance();
        startDate.add(Calendar.YEAR, -5);
        Calendar endDate = Calendar.getInstance();
        endDate.add(Calendar.YEAR, 5);
        // 时间选择器
        mTimePickerView = new TimePickerView.Builder(this, new TimePickerView.OnTimeSelectListener() {
            /**
             * 选中事件回调
             * @param date
             * @param v
             */
            @Override
            public void onTimeSelect(Date date, View v) {
                String data = mDateFormat.format(date);
                tvDate.setText(data);

            }
        })
                .setType(new boolean[]{true, true, true, false, false, false})// 显示类型 年月日时分秒 的显示与否，不设置则默认全部显示
                .setLabel("年", "月", "日", "", "", "")// 单位
                .isCenterLabel(false)// 是否只显示中间的label，默认true
                .setBackgroundId(Color.parseColor("#FFFFFF")) // 显示时的外部背景色颜色，默认是灰色
                .setDividerColor(Color.parseColor("#EAEDF0"))// 设置分割线的颜色
                .setContentSize(15)// 内容字体大小
                .setTitleHeight(dp2px(this, 39))// 标题高度
                .setTitleBgColor(Color.parseColor("#FFFFFF"))// 标题背景颜色
                .setShowTitleDivider(true)// 是否显示标题下方分割线
                .setCancelText("取消")// 取消按钮文字
                .setCancelColor(Color.parseColor("#666666"))// 取消按钮颜色
                .setSubmitText("确定")// 确定按钮文字
                .setSubmitColor(getResources().getColor(R.color.colorPrimary))// 确定按钮颜色
                .setSubCalSize(15)// 确定取消按钮字体大小
                .setDate(mGc)// 当前选中时间
                .setRangDate(startDate, endDate)// 设置起始时间
                .setDecorView(null)
                .isDialog(true)// 是否是对话框模式
                .build();
    }
    private void initOptionsPicker(){
        mOptionsPickerView = new OptionsPickerView.Builder(this, new OptionsPickerView.OnOptionsSelectListener() {
            @Override
            public void onOptionsSelect(int i, int i1, int i2, View view) {
                ToastUtils.showShort(i);
            }
        }).build();
        String[] strings = {"aa","bb","cc","dd"};
        mOptionsPickerView.setPicker(Arrays.asList(strings));
        
    }
    public static int dp2px(Context context, float value) {
        return (int) (TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, context.getResources().getDisplayMetrics()) + 0.5f);
    }
}
