
<center>
![](http://gykj.com.cn:4999/server/../Public/Uploads/2020-12-14/5fd700de652b6.png)
</center>
<center><h1>时间选择器控件接入指南</h1></center>


<h4>修订历史</h4>
<details>
<table>
<tr>
  <th style="background-color:#409EFF;color:#FFFFFF;">版本</th>
  <th style="background-color:#409EFF;color:#FFFFFF">修订时间</th>
  <th style="background-color:#409EFF;color:#FFFFFF">修订人员</th>
  <th style="background-color:#409EFF;color:#FFFFFF">修订内容</th>
</tr>
<tr>
  <td>0.0.0.1</td>
  <td>2021/03/15</td>
  <td>景艳辉</td>
  <td>新增时间选择器控件</td>
</tr>
</table>
</details>


<h4>目录</h4>

[TOC]

## 一、功能概述

### 该组件提供了时间选择功能，功能图如下
![](http://gykj.com.cn:4999/server/../Public/Uploads/2021-03-15/604ec1df2d229.png)

## 二、接入步骤
### 添加私有maven仓库地址并添加arr引用

- Project工程下build.gradle添加maven
```
allprojects {
    repositories {
        maven { url "http://gykj123.cn:9032/maven/nexus/content/groups/public/" }
    }
}
```
- 主项目app下的build.gradle添加arr引用
```
dependencies {
    implementation ('com.grandtech.common:common_tool_timepickerview:0.0.0.1:@aar') { transitive = true }// 时间选择器控件
```

## 三、接入示例代码
```
/**
 * 初始化时间选择器
 */
private void initDatePicker() {
    Date date = new Date();
    SimpleDateFormat mDateFormat = new SimpleDateFormat("yyyy-MM-dd");
    GregorianCalendar mGc = new GregorianCalendar();
    mGc.setTime(date);

    Calendar startDate = Calendar.getInstance();
    startDate.add(Calendar.YEAR, -5);// -5 指当前年份前五年
    Calendar endDate = Calendar.getInstance();
    endDate.add(Calendar.YEAR, 5);// 5 指当前年份后五年
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
            .setDividerColor(getResources().getColor(R.color.colorPrimary))// 设置分割线的颜色
            .setContentSize(21)// 内容字体大小
            .setCancelText("取消")// 取消按钮文字
            .setCancelColor(getResources().getColor(R.color.colorPrimary))// 取消按钮颜色
            .setSubmitText("确定")// 确定按钮文字
            .setSubmitColor(getResources().getColor(R.color.colorPrimary))// 确定按钮颜色
            .setDate(mGc)// 当前选中时间
            .setRangDate(startDate, endDate)// 设置起始时间
            .setBackgroundId(0x00FFFFFF) // 显示时的外部背景色颜色，默认是灰色
            .setDecorView(null)
            .setSubCalSize(16)// 确定取消按钮大小
            .setTitleBgColor(0xFFDADADA)// 标题背景颜色
            .isDialog(true)// 是否是对话框模式
            .build();
    }
```
```
// 弹时间选择对话框
mTimePickerView.show();
```

## 四、混淆说明
混淆已配置完成 暂时不需要额外混淆

## 五、其他说明
暂无特殊说明