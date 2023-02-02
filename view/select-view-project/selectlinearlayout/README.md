
<center>
![](http://gykj.com.cn:4999/server/../Public/Uploads/2020-12-14/5fd700de652b6.png)
</center>
<center><h1>下拉布局组件接入指南</h1></center>


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
  <td>2021/03/09</td>
  <td>景艳辉</td>
  <td>新增下拉布局组件</td>
</tr>
</table>
</details>


<h4>目录</h4>

[TOC]

## 一、功能概述

### 该组件实现了下拉列表选择功能，支持自定义下拉列表选择的类型，功能图如下
![](http://gykj.com.cn:4999/server/../Public/Uploads/2021-03-11/6049706c975ea.png)

## 二、接入步骤
### 添加私有maven仓库地址并添加arr引用

-  Project工程下build.gradle添加maven
```
allprojects {
    repositories {
        maven { url "http://gykj123.cn:9032/maven/nexus/content/groups/public/" }
    }
}
```
-  主项目app下的build.gradle添加arr引用
```
dependencies {
	implementation ('com.grandtech.common:common_tool_selectlinearlayout:0.0.0.1:@aar') { transitive = true }// 下拉布局组件
}
```

## 三、接入示例代码
布局中引用
```
<LinearLayout
    android:layout_width="match_parent"
    android:layout_height="40dp">

    <com.gykj.selectlinearlayout.SelectLinearLayout
        android:id="@+id/llSelect"
        android:layout_width="wrap_content"
        android:layout_height="match_parent"
        android:layout_weight="1.5"
        android:gravity="center"
        android:orientation="horizontal" />
</LinearLayout>
```
Java代码
```
SelectLinearLayout llSelect;
private boolean includesAll = true;// 是否包括"全部"

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
llSelect.setDicts(dicts);// 设置字典集合（下拉列表数据），必传
if (includesAll) {
	// 是否包括"全部"
	llSelect.setIncludesAll(true);// 设置下拉列表是否包括"全部"
	Dict dict = new Dict();
	dict.setValue("全部");
	dict.setKey("");
	llSelect.addDict(dict);// 追加字典实体
}
llSelect.setTvName("作物");// 设置字典名称，必传

llSelect.setOpenListener(true);// 设置下拉布局组件是否可点击，默认true

Dict dict = new Dict();
dict.setKey("07");
dict.setValue("冬小麦");
dicts.add(dict);
llSelect.setDict(dict);// 设置要显示的字典实体

Dict dict1 = llSelect.getOperationDict();// 获取当前选择的字典实体
```
```
/**
 * 选中回调
 *
 * @param dict
 */
@Override
public void selectDict(Dict dict) {
    llSelect.setColor(ContextCompat.getColor(this, R.color.colorPrimary));// 设置文本颜色
}
```

## 四、混淆说明
混淆已配置完成 暂时不需要额外混淆

## 五、其他说明
暂无特殊说明