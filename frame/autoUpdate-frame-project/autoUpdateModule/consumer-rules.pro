-keep public class com.gykj.autoupdate.bean.**{
*;
}
-keep public class com.gykj.autoupdate.utils.UpdateAppUtil{
public *;
public static <fields>;
}
-keep public class com.gykj.autoupdate.utils.PermisionUtils{
public *;
public static <fields>;
}
-keep class com.gykj.autoupdate.utils.UpdateAppUtil$hotFixCallBack{*;}

-keep class com.gykj.autoupdate.utils.UpdateAppUtil$CheckVersionCallBack{*;}