
<center>
![](http://gykj.com.cn:4999/server/../Public/Uploads/2020-12-14/5fd700de652b6.png)
</center>
<center><h1>签字组件接入指南</h1></center>


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
  <td>2021/02/25</td>
  <td>景艳辉</td>
  <td>新增签字组件</td>
</tr>
</table>
</details>


<h4>目录</h4>

[TOC]

## 一、功能概述

### 该组件实现了签字功能，功能图如下
![](http://gykj.com.cn:4999/server/../Public/Uploads/2021-02-25/603715e3046c6.png)

## 二、接入步骤
### 1. 添加私有maven仓库地址并添加arr引用

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
    implementation ('com.grandtech.common:common_tool_signature:0.0.0.1:@aar') { transitive = true }// 签字组件
}
```

### 2. 权限申请 接入组件需要动态获取存储读写权限，如下：
```
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
```

## 三、接入示例代码
```
// 跳转到签字页面
Intent intent = new Intent();
ComponentName cn = new ComponentName(this,"com.gykj.signature.SignActivity");// this--所属页面Context
intent.setComponent(cn);
intent.putExtra("signPath", path + "QM.jpg");// 签字图片存储路径，必传
intent.putExtra("colorBtnNormal", "#13c287");// Button正常状态下的背景色，非必传 不传则采用默认颜色，通常设置为主题色
startActivityForResult(intent, RESULT_SIGN);// RESULT_SIGN--int类型请求码
```
```
/**
 * 返回参数接收
 */
@Override
protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_SIGN) {
            String signPath = data.getStringExtra("RES_SIGN");// 返回签字图片路径
            // 图片加载
            Glide.with(this)
                    .load(signPath)
                    .skipMemoryCache(true)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .error(getResources().getDrawable(R.mipmap.signature))
                    .into(civSign);
        }
    }
```

## 四、混淆说明
混淆已配置完成 暂时不需要额外混淆

## 五、其他说明
```
// 引用的第三方框架及推荐版本号
api 'com.github.bumptech.glide:glide:4.11.0'// GLIDE图片加载库
```