<div>
<center>
![](http://gykj.com.cn:4999/server/../Public/Uploads/2020-12-14/5fd700de652b6.png)
</center>
<center><h1>网络请求文档</h1></center>
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
  <td>2020/03/01</td>
  <td>赵宇鹏</td>
  <td>初始导入</td>
</tr>
</table>
</details>


<h4>目录</h4>

[TOC]

## 一、功能概述

		本文档是网络请求组件的使用说明文档，本模块的主要功能是帮助开发者快速集成网络请求,方便对接接口。


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
    implementation ('com.grandtech.common:common_tool_network:0.0.0.1:@aar') { transitive = true }
}
```

### 2. 初始化配置

在Application的onCreate方法中进行初始化
```
@Override
    public void onCreate() {
        super.onCreate();
        NetworkHelper.init(this,"https://www.baidu.com");
    }
```
## 三、API说明
### 1.类NetworkHelper
 NetworkHelper是网络请求的核心类
- 方法摘要

|Type| Method  | Description  |
| ------------ | ------------ | ------------ |
| NetworkHelper | getInstance()  | 返回NetworkHelper实例  |
| 接口泛型C |request(Class<C> c, NetWorkResult<R> r)|发起请求|
| void |cancelAllRequest()|取消所有请求|
| void |setToken(Application hashObj,String token)|设置请求token|

- 方法详细信息

#### init
	/**
     * 初始化网络工具类
     * @param application 应用applicaion实例
     * @param baseUrl 网络请求基础url
     */
    public static void init(Application application, String baseUrl)


#### getInstance

```
 	/**
     * 返回网络请求工具实例
     * @return 网络请求实例
     */
    public static NetworkHelper getInstance()
```

#### request
	/**
     * 发起请求
     * @param c API接口类
     * @param r 网络请求返回类
     * @param <R> 返回数据类型
     * @return 返回Class对应的实体类
     */
    public <C, R> C request(Class<C> c, NetWorkResult<R> r)

#### cancelAllRequest
	/**
     * 取消所有请求
     */
    public synchronized void cancelAllRequest()

#### setToken
	/**
     * 设置请求的token
     * @param application 
     * @param token 网络请求的token
     */
    public static void setToken(Application application,String token)

### 2.接口NetworkResult
NetworkResult是网络请求的响应类

|Type| Method  | Description  |
| ------------ | ------------ | ------------ |
| void |onStart(Disposable disposable)|开始请求网络|
| void |onRequestInvoke(Observable observable)|开始执行请求|
| void |onRequestEnd()|请求返回结束|
| void |onSuccess(R data)|请求成功|
| void |onError(int err, String errMsg, Throwable t, R data)|请求失败|

#### onStart
		/**
         * 开始请求网络
         * @param disposable 网络请求handler
         */
        void onStart(Disposable disposable)
#### onRequestInvoke
		/**
         * 开始执行请求
         * @param observable 用于绑定网络请求生命周期
         */
        void onRequestInvoke(Observable observable);
#### onRequestEnd
		/**
         * 请求返回结束
         */
        void onRequestEnd();
#### onSuccess
		/**
         * 请求成功
         * @param data 返回的响应数据实体
         */
        void onSuccess(R data);
#### onError
/**
         * 请求失败
         * @param err 错误码
         * @param errMsg 错误信息
         * @param t 抛出的错误异常
         * @param data 返回的响应数据实体
         */
        default void onError(int err, String errMsg, Throwable t, R data)
## 四、混淆说明

