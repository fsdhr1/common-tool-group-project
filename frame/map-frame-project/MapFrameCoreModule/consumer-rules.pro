# Retrofit
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepattributes Signature
-keepattributes Exceptions

#-keep class org.geotools.geojson.geom.GeometryJSON{*;}

-keep class * implements org.json.simple.parser.ContentHandler{
    public <init>(...);
}

#不混淆某个包下的类
-keep class org.geotools.geojson.geom.** {*;}