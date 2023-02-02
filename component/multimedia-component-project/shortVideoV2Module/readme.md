<div>
<center><h1>小视频采集预览</h1></center>
</div>

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
  <td>2020/02/24</td>
  <td>贺鑫海</td>
  <td>1.试用初级版本</td>
</tr>
</table>
</details>


<h4>目录</h4>

## 一、功能概述
  #### 该组件主要实现了短视频的录制功能和预览功能，并提供了短视频的视频地址和视频截图地址

## 二、接入步骤
### 1. 引用arr

-  Project工程下build.gradle添加maven
```
allprojects {
    repositories {
        maven { url "http://gykj123.cn:9032/maven/nexus/content/groups/public/" }
    }
}
```
- moudle下的build.gradle添加arr引用
```
dependencies {
    implementation ('com.grandtech.common:common_tool_shortvideo_debug:0.0.0.1:@aar') { transitive = true }
}
```

### 2.接入说明
- #### 权限申请 接入组件需要动态获取存储读写相机麦克风等权限，如下：
```java
	<uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
    <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    <uses-permission android:name="android.permission.CAMERA" />
    <uses-permission android:name="android.permission.RECORD_AUDIO" />
    <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
    <uses-permission android:name="android.permission.READ_PHONE_STATE" />
```
- #### 示例代码
```java
//请求打开短视屏
Intent intent = new Intent();
ComponentName cn =new ComponentName(YourActivity.this,"com.gykj.shortvideov2module.VideoActivity");//YourActivity（全类名）
intent.setComponent(cn);
intent.putExtra("videoName","deamo");//短视频名称
startActivityForResult(intent,requesetVideoCode);//requesetVideoCode 请求码 int类型
```

```java
//返回参数接收
 @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==requesetVideoCode&&resultCode==-1){//requesetVideoCode 请求码 int类型
            String videoPath =data.getStringExtra("videoPath");//本地视频地址
            String thumbPicPath =data.getStringExtra("picPath");//缩略图地址

        }
    }
```
## 四、混淆说明
```java
-keep class com.gykj.shortvideov2module.** { *; }
##Glide
-dontwarn com.bumptech.glide.**
-keep class com.bumptech.glide.**{*;}
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public class * extends com.bumptech.glide.AppGlideModule
-keep public enum com.bumptech.glide.load.resource.bitmap.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}
```

## 五、其他说明
```java
	//接入的第三方框架及版本号
	implementation 'com.github.bumptech.glide:glide:4.12.0'//图片框架
    annotationProcessor 'com.github.bumptech.glide:compiler:4.12.0'
    api 'com.blankj:utilcodex:1.30.6'//工具类
    //甜美的对话框
    implementation 'com.github.cazaea:sweet-alert-dialog:1.0.0'
    //甜美的对话框
    implementation 'com.github.cazaea:materialish-progress:1.0.2'
```