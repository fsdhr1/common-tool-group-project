
<center>
![](http://gykj123.cn:4999/server/../Public/Uploads/2020-12-14/5fd700de652b6.png)
</center>
<center><h1>定位服务接入指南</h1></center>


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
  <td>2021/03/17</td>
  <td>景艳辉</td>
  <td>新增定位服务</td>
</tr>
<tr>
  <td>0.0.0.2</td>
  <td>2021/08/03</td>
  <td>景艳辉</td>
  <td>内部混淆规则修改，防止模块之间包名冲突；<br/>异常处理，判断服务是否注册</td>
</tr>
<tr>
  <td>0.0.0.3</td>
  <td>2021/09/07</td>
  <td>景艳辉</td>
  <td>空指针异常处理</td>
</tr>
</table>
</details>


<h4>目录</h4>

[TOC]

## 一、功能概述
### 该组件提供了定位服务，返回当前位置信息，包括经纬度等

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
    implementation ('com.grandtech.common:common_tool_locationservice:0.0.0.3:@aar') { transitive = true }// 定位服务
}
```

## 三、接入示例代码
```
// 定位权限
protected String[] MANIFEST_LOCATION = new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.VIBRATE};


@Override
protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    /**
     * 公交车 注册
     */
    EventBus.getDefault().register(this);
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        // 请求权限
        requestPermissions(MANIFEST_LOCATION, 1000);
    }
}

/**
 * 请求权限的结果的回调
 * @param requestCode
 * @param permissions
 * @param grantResults
 */
@Override
public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);

    /**
     * 启动定位服务
     * Context var1
     * boolean var2  是否启动百度定位
     * boolean var3  是否常驻
     */
    LocationClient.getInstance().startLoc(this, false, false);
}

@Override
protected void onDestroy() {
    super.onDestroy();

    // 停止定位服务
    LocationClient.getInstance().stopLoc(this);
}

@Override
protected void onStop() {
    super.onStop();
    if (isFinishing()) {
        /**
         * 公交车 取消注册
         */
        EventBus.getDefault().unregister(this);
        System.out.println("注销公交车");
    }
}
```
```
/**
 * 接收定位服务信息
 *
 * @param serviceLocation
 */
@Subscribe(threadMode = ThreadMode.MAIN)
public void onEventMainThread(ServiceLocation serviceLocation) {
    if (serviceLocation != null)
        Toast.makeText(this, serviceLocation.getLatitude() + ", " + serviceLocation.getLongitude(), Toast.LENGTH_SHORT)
                .show();
}
```
** ServiceLocation实体说明 **

|参数名                 |类型      |说明|
|:----                  |:-----    |-----   |
|eSignal                |ESignal   |信号、强中弱 |
|location               |Location  |位置 |
|satelliteCount         |Integer   |卫星数 |
|validSatelliteCount    |Integer   |有效的卫星数，卫星信噪比 > 30 |
|latitude               |Double    |纬度 |
|longitude              |Double    |经度 |

## 四、混淆说明
混淆已配置完成 暂时不需要额外混淆

## 五、其他说明
暂无特殊说明