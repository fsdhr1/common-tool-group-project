# 保留 @Subscribe 注解不被混淆
-keepclassmembers class * {
    @org.greenrobot.eventbus.Subscribe <methods>;
}