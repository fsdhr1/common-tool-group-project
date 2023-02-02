<div>
<center>
![](http://gykj123.cn:4999/server/../Public/Uploads/2020-12-14/5fd700de652b6.png)
</center>
<center><h1>地图框架接入使用文档</h1></center>
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
  <td>0.0.1.6</td>
  <td>2021/06/04</td>
  <td>冯帅</td>
  <td>bate版本</td>
</tr>
<tr>
  <td>0.0.5.7</td>
  <td>2021/08/04</td>
  <td>冯帅</td>
  <td>1.修复已知bug <br> 2.新增生成坐落图批量，手动功能</td>
</tr>
</table>
</details>


<h4>目录</h4>

[TOC]

## 一、功能概述

		本文档是地图功能组件的使用说明文档，地图组件拆分成mapcore(功能组件)和mapUI(ui组件)，本文介绍项目中单独集成功能组件的方式。


## 二、接入步骤
### 1. mapbox引配置

-  Project工程下gradle.properties文件中添加变量MAPBOX_DOWNLOADS_TOKEN，从mapbox官方申请
地址:https://account.mapbox.com/access-tokens/
```
# mapbox MAPBOX_DOWNLOADS_TOKEN
MAPBOX_DOWNLOADS_TOKEN=sk.eyJ1IjoiZnMxOTk1MDMwMSIsImEiOiJja2xhZ210czgzamVuMnNxb2dkcTN6ZXRyIn0.OjKkjFdRWrZBP432CSAGHw
```
-  Project工程下build.gradle添加mapbox的仓库配置
```
allprojects {
    repositories {
        maven {
            url 'https://api.mapbox.com/downloads/v2/releases/maven'
            authentication {
                basic(BasicAuthentication)
            }
            credentials {
                // Do not change the username below.
                // This should always be `mapbox` (not your username).
                username = 'mapbox'
                // Use the secret token you stored in gradle.properties as the password
                password = project.properties['MAPBOX_DOWNLOADS_TOKEN'] ?: ""
            }
        }
    }
}
```
### 2.引入mapCore依赖配置
- Project工程下build.gradle添加maven私服配置
```
allprojects {
    repositories {
        maven { url "http://gykj123.cn:9032/maven/nexus/content/groups/public/" }
    }
}
```
### 2. 初始化配置

- 在引用地图的页面中的onCreate方法首先执行
```
//初始化设置地图token
GMap.getInstance(this);
```
- 创建一个地图实例放入要呈现的布局中(注意mapview生命周期和Activity或fragment的绑定)
```
 @Override
    protected void onCreate(Bundle savedInstanceState) {
        GMap.getInstance(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        flMainFrame = findViewById(R.id.flMainFrame);
        MapboxMapOptions mapOptions = new MapboxMapOptions();
        mapOptions.setPrefetchesTiles(true);
        mapOptions.compassEnabled(true);
        mapOptions.rotateGesturesEnabled(false);
        mapOptions.tiltGesturesEnabled(false);
        mapOptions.logoEnabled(false);
        mapOptions.attributionEnabled(false);
        mapOptions.maxZoomPreference(16.99);
        gMapView = new GMapView(this,mapOptions,this);
        gMapView.onCreate(savedInstanceState);
        flMainFrame.addView(gMapView);
    }
}
@Override
    protected void onStart() {
        super.onStart();
        gMapView.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        gMapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        gMapView.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        gMapView.onStop();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        gMapView.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        gMapView.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        gMapView.onDestroy();
    }
```
- 地图style设置（因为地图加载是异步的，因此要在回调中进行下一步操作）
```
//地图Gmapview准备完成回调
@Override
public void onBoxMapReady(MapboxMap mapboxMap) {       BoxHttpClient.getSingleton().setToken("eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJneWtqcWZ4X3Rlc3QiLCJjb250ZXh0VXNlcklkIjoiMTIyMTkiLCJjb250ZXh0TmFtZSI6IuWbvea6kOenkeaKgOa4heS4sOWOv-a1i-ivlei0puaItyIsImNvbnRleHREZXB0SWQiOiIxMDE4IiwiY29udGV4dEFwcGxpY2F0aW9uSWQiOiI5Y2Q1YzQ4OTdkOTM0YjFmOTQyMjA4NDEwNjQ1NmRiOSIsImV4cCI6MTYyMDgwOTI1NH0.DFXUjKLgOupBo7iynBX-YIomaZ4bgET7kBcHOvG9X5M");
        String url ="http://gykj123.cn:9035/api/v1/styles/gykj/rJg3x0CN_7";
        mapboxMap.setStyle(url, new Style.OnStyleLoaded() {
            @Override
            public void onStyleLoaded(@NonNull Style style) {
                Log.i("mbgl","style加载完毕回调");
				//在此处可以进行业务需要的功能组件初始化设置和初始化，选择集、临时图层、地图事件注册等
				  //gMapView.setMapEvent(MapActivityDome1.this);
                 //  String cachePath = Environment.getExternalStorageDirectory().getAbsolutePath()+                                 //File.separator+"toolmapframe" + File.separator + "Cache" + File.separator                                        //+"frame"+File.separator+               //"data.json";
               // gMapView.setEditor(new GraphicSetting(GeometryType.polygon, "验标地块", cachePath));
                //如果选择集要操作临时图层gMapView.initSelection需要在 gMapView.setEditor执行之后执行
              //  gMapView.initSelection(new SelectSetting(true,true,gMapView.getGcQueryLayerId(), "验标地块","参考                //   地块", "大棚地块","种植地块", "基础地块"));
             //   gMapView.addSelectChangeListening(MapActivityDome1.this);
                // Map is set up and the style has loaded. Now you can add data or make other map adjustments
            }
        });

    }
```
##三、API文档
[地图功能组件API文档](https://www.showdoc.com.cn/GMap/7178604402319765)
##四、混淆说明
已在aar包的consumer里添加混淆，不再重复设置
