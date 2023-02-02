#此文件将打包至aar中 当主项目中开启混淆时将自动启用
-keep public class * extends com.gykj.networkmodule.beans.DataResponse

# Retrofit
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepattributes Signature
-keepattributes Exceptions

#通过apt生成的类不能够被混淆
-keep class * implements com.gykj.networkmodule.IBaseApi{*;}