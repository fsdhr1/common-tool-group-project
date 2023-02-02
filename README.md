<div>
<center>
![](http://gykj123.cn:4999/server/../Public/Uploads/2020-12-14/5fd700de652b6.png)
</center>
<center><h1>通用组件项目</h1></center>
</div>
##本项目主要用于：
方便各个业务线快速集成公司前期开发的Android通用组件，优化资源利用，减少维护成本，节约项目开发时间。
##项目地址
http://gykj123.cn:8010/hexinhai/common-tool.git
##技术结构和方案：
- ####app启动项的命名方式：
项目主app为测试项目，主要为调用各个组件模块提供启动环境，测试参数，返回参数显示，成果验证等功能;
启动activity在清单文件自行调整，命名规则如下：
模块名称+TestActivity，并注册到清单文件中，例如cameraModule模块：CameraModuleTestActivity;
- ####组件模块的命名方式：
组件模块以library形式添加到项目中，命名规则不做强行限制，可参考：camerModule
- ####关于模块包名命名方式：
组件模块的包名不做强行限制，新增项目统一规则如下：com.gykj.+组件模块名称
- ####各模块的SDK版本
    compileSdkVersion 28
    buildToolsVersion "28.0.3"
    minSdkVersion 21
    targetSdkVersion 28
- ####各模块的打包上传
模块开发调试成功后，需要以aar或者jar包的形式上传maven私服，上传maven私服需要进行如下配置：
组件模块的gradle中添加maven插件：   apply plugin: 'maven'
在dependencies中添加如下代码
```java
   def MAVEN_LOCAL_PATH ='http://192.168.1.67:8081/nexus/content/repositories/releases/'
   //manven私服地址
    def ARTIFACT_ID = 'common_tool_camera_release'
	//组件id id命名规则"common_tool_"加上组件模块名称+debug/release(测试或者正式版本)
    def VERSION_NAME = '0.0.5.6'//组件版本名称，长度暂时统一为4位，从0.0.0.1开始
    def GROUP_ID = 'com.grandtech.common'//组件组id，固定分组暂定为com.grandtech.common
    def ACCOUNT = 'admin'//用户名
    def PASSWORD = 'admin123'//密码
    uploadArchives {
        repositories {
            mavenDeployer {
                repository(url:MAVEN_LOCAL_PATH ){
                    authentication(userName: ACCOUNT, password: PASSWORD)}
                pom.project {
                    groupId GROUP_ID
                    artifactId ARTIFACT_ID
                    version VERSION_NAME
                    packaging 'aar'
                }
            }
        }
    }
```

