
<center>
![](http://gykj123.cn:4999/server/../Public/Uploads/2020-12-14/5fd700de652b6.png)
</center>
<center><h1>相机组件接入指南</h1></center>


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
  <td>2021/08/03</td>
  <td>景艳辉</td>
  <td>新增相机组件</td>
</tr>
<tr>
  <td>0.0.0.2</td>
  <td>2021/09/07</td>
  <td>景艳辉</td>
  <td>修复拍照完成后，点击'×'和‘√’两个按钮中间，还能切换前后摄像头，并且能拍照</td>
</tr>
</table>
</details>


<h4>目录</h4>

[TOC]

## 一、功能概述

### 该组件实现了相册选择及相机拍照功能，功能图如下
![](http://gykj123.cn:4999/server/index.php?s=/api/attachment/visitFile/sign/8e678fedbd8e98af1f70af3825295959)
![](http://gykj123.cn:4999/server/index.php?s=/api/attachment/visitFile/sign/f02ee8f88f3929f0732529e0cdb00517)

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
    implementation('com.grandtech.common:common_tool_cameramodule:0.0.0.2') { transitive = true }// 相机组件
}
```

### 2. 权限申请 接入组件需要动态获取存储读写权限、相机权限、定位权限，如下：
```
<!--允许程序写入外部存储,如SD卡上写文件-->
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
<!--读权限-->
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
 <!--允许程序访问摄像头进行拍照-->
<uses-permission android:name="android.permission.CAMERA" />
<!-- 这个权限用于访问GPS定位 -->
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<!-- 这个权限用于进行网络定位 -->
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
<!--允许程序振动-->
<uses-permission android:name="android.permission.VIBRATE" />
```

## 三、接入示例代码
```
// 跳转到相册页面，同时支持相机拍照
Intent intent = new Intent();
ComponentName cn = new ComponentName(this, "com.gykj.cameramodule.activity.image.ImgPickActivity");// this--所属页面Context
intent.setComponent(cn);
intent.putExtra("isBatch", false);// 是否批处理（连拍），非必传 不传默认false
intent.putExtra("filePath", path);// 拍摄的照片的存储路径，必传
intent.putExtra("basePath", path);// 选择相册中的图片存储的路径，必传
intent.putExtra("firstPath", localPath);// 判断优先显示的路径下有没有图片，有的话就优先显示，没有就不管他
intent.putExtra("extendName", "XCZP");// 拓展名，必传
intent.putExtra("areaName", "areaName");// 水印信息显示的地点名称，非必传
//intent.putExtra("areaCode", "null");// 地点代码，拼接在图片名中，非必传
intent.putExtra("isOrdinary", false);// 是否为普通相机，非必传 不传默认true，照片不带水印
//intent.putExtra("areaNameUrl", AREA_NAME_URL);// 获取区划全称的地址，非必传
intent.putExtra("isHideTitle", false);// 是否隐藏相册页面的标题头，非必传 不传默认false不隐藏
intent.putExtra("backgroundColorTitle", "#008577");// 标题头背景色，非必传 不传则采用默认颜色，通常设置为主题色
startActivityForResult(intent, RESULT_IMAGE);// RESULT_CAMERA--int类型请求码
```
```
// 直接跳转到相机拍照
Intent intent = new Intent();
ComponentName cn = new ComponentName(this, "com.gykj.cameramodule.activity.camera.CameraActivity");// this--所属页面Context
intent.setComponent(cn);
intent.putExtra("isBatch", false);// 是否批处理（连拍），非必传 不传默认false
intent.putExtra("filePath", path);// 拍摄的照片的存储路径，必传
intent.putExtra("extendName", "XCZP");// 拓展名，必传
intent.putExtra("areaName", "areaName");// 水印信息显示的地点名称，非必传
intent.putExtra("areaCode", "000000000000");// 地点代码，拼接在图片名中，非必传
intent.putExtra("isOrdinary", false);// 是否为普通相机，非必传 不传默认true，照片不带水印
//intent.putExtra("areaNameUrl", AREA_NAME_URL);// 获取区划全称的地址，非必传
//intent.putExtra("waterMarkerText", waterMarkerText);// 水印信息
startActivityForResult(intent, RESULT_CAMERA);
```
```
	/**
	* 返回参数接收
	* @param requestCode
	* @param resultCode
	* @param data
	*/
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // 相册返回
        if (requestCode == RESULT_IMAGE) {
            if (data.getStringExtra("flag").equals("nothing")) return;// 没有图片
            if (data.getStringExtra("flag").equals("camera")) {// 相机拍照
                List<PhotoFile> list = (List<PhotoFile>) data.getSerializableExtra("pathPhotoFile");
                // 是否批处理（连拍）
                if (list != null) {
                    int i = 0;
                    for (PhotoFile photoFile : list) {
                        i = i + 1;
                        photoFile.setDesc(i + "");
                        picFileAdapter.appendItem(photoFile);
                        // 拷贝到本地路径
                        FileUtil.copyFileAnyhow(photoFile.getPath(), localPath + System.currentTimeMillis() + "_照片.jpg");
                    }
                } else {
                    String path = data.getStringExtra("path");
                    PhotoFile photoFile = new PhotoFile("现场照片", path, data.getStringExtra("desc"));// desc 水印信息
                    picFileAdapter.appendItem(photoFile);
                    // 拷贝到本地路径
                    FileUtil.copyFileAnyhow(path, localPath + System.currentTimeMillis() + "_照片.jpg");
                }
            } else if (data.getStringExtra("flag").equals("pickImg")) {// 相册选择
                ArrayList<String> items = data.getStringArrayListExtra("items");// 选择相册中的图片存储的全路径
                int i = 0;
                for (String item : items) {
                    i = i + 1;
                    try {
                        PhotoFile photoFile = new PhotoFile("现场照片", item, data.getStringExtra("desc"));// desc 水印信息
                        photoFile.setDesc(i + "");
                        picFileAdapter.appendItem(photoFile);
                        String albumpath = data.getStringExtra("albumpath");
                        if (albumpath != null && albumpath.contains(localPath))
                            continue;//如果是从缓存列表选的就不要再复制了
                        // 拷贝到本地路径
                        FileUtil.copyFileAnyhow(item, localPath + System.currentTimeMillis() + "_照片.jpg");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        // 相机拍照返回
        if (requestCode == RESULT_CAMERA) {
            if (data == null) return;
            if (resultCode == SUCCESS) {
                List<PhotoFile> list = (List<PhotoFile>) data.getSerializableExtra("pathPhotoFile");
                // 是否批处理（连拍）
                if (list != null) {
                    int i = 0;
                    for (PhotoFile photoFile : list) {
                        i = i + 1;
                        photoFile.setDesc(i + "");
                        picFileAdapter.appendItem(photoFile);
                        // 拷贝到本地路径
                        FileUtil.copyFileAnyhow(photoFile.getPath(), localPath + System.currentTimeMillis() + "_照片.jpg");
                    }
                } else {
                    String path = data.getStringExtra("path");
                    PhotoFile photoFile = new PhotoFile("现场照片", path, data.getStringExtra("desc"));// desc 水印信息
                    picFileAdapter.appendItem(photoFile);
                    // 拷贝到本地路径
                    FileUtil.copyFileAnyhow(path, localPath + System.currentTimeMillis() + "_照片.jpg");
                }
            }
        }
    }
```

## 四、混淆说明
混淆已配置完成 暂时不需要额外混淆

## 五、其他说明
说明一：
```
APP主题设置为NoActionBar
<style name="AppTheme" parent="Theme.AppCompat.Light.NoActionBar" />
```
说明二：
```
implementation 'androidx.appcompat:appcompat:1.2.0'// 解决Cannot access androidx.activity.ComponentActivity

implementation ('com.grandtech.common:common_tool_locationservice:0.0.0.3:@aar') { transitive = true }// 定位服务
/*******************************以下为引用第三方*******************************/
// RxLifecycle
implementation "com.trello.rxlifecycle2:rxlifecycle:$rootProject.rxlifecycle"
implementation "com.trello.rxlifecycle2:rxlifecycle-components:$rootProject.rxlifecycle"
implementation "androidx.recyclerview:recyclerview:$rootProject.recyclerview"
// 图片加载
implementation "com.github.bumptech.glide:glide:$rootProject.glide"
// 消息总线
implementation "org.greenrobot:eventbus:$rootProject.eventbus"
// 甜美的对话框
implementation "com.github.cazaea:sweet-alert-dialog:$rootProject.sweetdialog"
implementation "io.reactivex.rxjava2:rxjava:$rootProject.rxjava"
// gson
implementation "com.google.code.gson:gson:$rootProject.gson"
implementation "com.squareup.retrofit2:adapter-rxjava:$rootProject.adapter_rxjava"

Project工程下build.gradle配置第三方框架版本号：
buildscript {
    ext {
		ext.rxlifecycle = '2.2.1'
		ext.recyclerview = '1.1.0'
		// GLIDE图片加载库
		ext.glide = '4.11.0'
		ext.glide_compiler = '4.12.0'
		ext.eventbus = '3.1.1'
		ext.sweetdialog = '1.0.0'
		ext.rxjava = '2.1.11'
		ext.gson = '2.8.6'
		ext.adapter_rxjava = '2.4.0'
    }
}
```