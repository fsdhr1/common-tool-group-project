<div>
<center>
![](http://gykj123.cn:4999/server/../Public/Uploads/2020-12-14/5fd700de652b6.png)
</center>
<center><h1>地图UI组件mapUI集成文档</h1></center>
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
  <td>0.0.1.1</td>
  <td>2021/07/14</td>
  <td>冯帅</td>
  <td>发布版本</td>
</tr>
</table>
</details>


<h4>目录</h4>

[TOC]

## 一、功能概述

		本文档是地图功能组件的使用说明文档，地图组件拆分成mapcore(功能组件)和mapUI(ui组件)，本文介绍项目中集成mapUI(ui组件)的方式，mapUI和maoCore是完全解耦的两个组件，互相之间没有依赖。


## 二、接入步骤
### 1.引入mapUI依赖配置
- Project工程下build.gradle添加maven私服配置
```
allprojects {
    repositories {
        maven { url "http://gykj123.cn:9032/maven/nexus/content/groups/public/" }
    }
}
```
- 项目的gradle下因为mapUI依赖
```
implementation ('com.grandtech.common:common_tool_mapui:0.0.1.1:@aar') { transitive = true }
```
### 2. 初始化配置

- 在引用地图的页面中的地图加载完成事件onStyleLoaded方法中执行
```
//初始化设置地图token
  mToolManager = ToolManager.newInstance().init(MapActivityDemo2.this,gMapView,false);
```
- ToolManager是mapUI的操作核心类，初始化之后，UI组件内部有默认的工具布局可以选择隐藏默认按钮，利用API进行自定义，具体使用详见API文档

##三、API文档
[地图功能组件API文档](https://www.showdoc.com.cn/GMap/7178604402319765)
##四、混淆说明
已在aar包的consumer里添加混淆，不再重复设置
