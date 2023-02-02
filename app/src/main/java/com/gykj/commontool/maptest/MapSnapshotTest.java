package com.gykj.commontool.maptest;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.PointF;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.blankj.utilcode.util.LogUtils;
import com.grandtech.mapframe.core.GMap;
import com.grandtech.mapframe.core.event.IMapEvent;
import com.grandtech.mapframe.core.maps.GMapView;
import com.grandtech.mapframe.core.maps.IOnMapReady;
import com.grandtech.mapframe.core.net.BoxHttpClient;
import com.grandtech.mapframe.core.select.SelectSetting;
import com.grandtech.mapframe.core.snapshot.AutoMapSnapshot;
import com.grandtech.mapframe.core.snapshot.ManualMapSnapshotActivity;
import com.grandtech.mapframe.core.snapshot.bean.SnapParam;
import com.grandtech.mapframe.core.snapshot.custom.MapScreenshotTool;
import com.grandtech.mapframe.core.snapshot.custom.ScreenshotsFeatureStye;
import com.grandtech.mapframe.core.snapshot.custom.ScreenshotsSetting;
import com.grandtech.mapframe.core.util.FeatureSet;
import com.grandtech.mapframe.core.util.StringEngine;
import com.grandtech.mapframe.core.util.StyleJsonBuilder;
import com.gykj.commontool.MyApplication;
import com.gykj.commontool.R;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.Polygon;
import com.mapbox.mapboxsdk.annotations.Polyline;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.expressions.Expression;
import com.mapbox.mapboxsdk.style.layers.FillLayer;
import com.mapbox.mapboxsdk.style.layers.PropertyFactory;
import com.mapbox.mapboxsdk.style.sources.VectorSource;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

import static com.gykj.commontool.maptest.AddRainFallStyleActivity.ID_LAYER;
import static com.gykj.commontool.maptest.AddRainFallStyleActivity.ID_SOURCE;
import static com.gykj.commontool.maptest.AddRainFallStyleActivity.SOURCE_URL;
import static com.mapbox.mapboxsdk.style.expressions.Expression.eq;
import static com.mapbox.mapboxsdk.style.expressions.Expression.get;
import static com.mapbox.mapboxsdk.style.expressions.Expression.interpolate;
import static com.mapbox.mapboxsdk.style.expressions.Expression.literal;
import static com.mapbox.mapboxsdk.style.expressions.Expression.stop;
import static com.mapbox.mapboxsdk.style.layers.Property.VISIBLE;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.fillColor;

/**
 * @ClassName MapSnapshotTest
 * @Description TODO
 * @Author: fs
 * @Date: 2021/7/30 11:12
 * @Version 2.0
 */
public class MapSnapshotTest  extends AppCompatActivity implements IOnMapReady, IMapEvent {

    private Button auto,auto1,custom,manual,customAuto,customAutoTotal;
    private GMapView mGMapView;
    private AutoMapSnapshot mAutoMapSnapshot;
    private FillLayer layer;
    private int index = 1;
    private RefreshGeoJsonRunnable refreshGeoJsonRunnable;
    private Handler handler;
    private MapScreenshotTool mapScreenshotTool;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        GMap.getInstance(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_snapshot);
        auto = findViewById(R.id.auto);
        auto1 = findViewById(R.id.auto1);
        custom = findViewById(R.id.custom);
        manual = findViewById(R.id.manual);
        mGMapView = findViewById(R.id.mapView);
        customAuto = findViewById(R.id.custom_auto);
        customAutoTotal = findViewById(R.id.custom_auto_total);
        handler = new Handler();
        mGMapView.init(this);
        auto1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<Feature> features = FeatureSet.convert2Features(mGMapView.getSelectSet());
                Feature feature1 = features.get(0);
                for (int i = 0; i< 100;i++ ) {
                    features.add(feature1);
                }
                //水印
                List<String> waterMark = new ArrayList<>();
                waterMark.add("坐落图默认样式");
                List<SnapParam> snapParams = new ArrayList<>();
                for (Feature feature : features) {
                    String cachePath = Environment.getExternalStorageDirectory().getAbsolutePath()+ File.separator+"toolmapframe" + File.separator +
                            "坐落图默认样式" + File.separator + StringEngine.get32UUID() +".png";
                    SnapParam snapParam = new SnapParam(feature,cachePath,waterMark);
                    snapParams.add(snapParam);
                }
                String styleJosn = StyleJsonBuilder.create().builder();
                mAutoMapSnapshot = AutoMapSnapshot.create(MapSnapshotTest.this, styleJosn, new AutoMapSnapshot.ICallBack() {
                    @Override
                    public void onNext(SnapParam snapParam) {
                        Log.i("gmbgl_test",snapParam.toJson());
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        Log.e("gmbgl_test",e.getMessage());
                    }

                    @Override
                    public void complete() {
                        Log.i("gmbgl_test2","坐落图生成完毕");
                        Toast.makeText(MapSnapshotTest.this, "坐落图生成完毕！", Toast.LENGTH_LONG).show();
                        mAutoMapSnapshot.cancel();
                    }
                }).batchSnapShot(snapParams,mGMapView.getMapBoxMap());
            }
        });
        auto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MapSnapshotTest.this, Tett.class);
                startActivity(intent);
            }
        });
        custom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              //  List<Feature> features = FeatureSet.convert2Features(mGMapView.getSelectSet());
                //水印
                List<String> waterMark = new ArrayList<>();
                waterMark.add("坐落图自定义样式");
                List<SnapParam> snapParams = new ArrayList<>();
                String cachePath = Environment.getExternalStorageDirectory().getAbsolutePath()+ File.separator+"toolmapframe" + File.separator +
                        "坐落图默认样式" + File.separator + StringEngine.get32UUID() +".png";
                List<FeatureSet> featureSets = FeatureSet.convert2FeatureSets(mGMapView.getSelectSet());
                SnapParam snapParam = new SnapParam(featureSets,cachePath,waterMark);
               // snapParam.setFeatureSets(featureSets);
                snapParams.add(snapParam);
                String styleJosn = StyleJsonBuilder.create().builder();
                for (FeatureSet featureSet : featureSets) {
                    String dkLineLayerStr = String.format("{\n" +
                            "      \"id\": \"%s\",\n" +
                            "      \"source\": \"%s\",\n" +
                            "      \"type\": \"line\",\n" +
                            "      \"layout\": {\n" +
                            "        \"visibility\": \"visible\"\n" +
                            "      },\n" +
                            "      \"paint\": {\n" +
                            "        \"line-color\": \"yellow\",\n" +
                            "        \"line-opacity\": 1,\n" +
                            "        \"line-width\": 2\n" +
                            "      }\n" +
                            "    }",featureSet.getLayerId(),featureSet.getSourceId());
                    styleJosn = StyleJsonBuilder.create(styleJosn)
                            .buildSourceJson(featureSet.getSourceId())
                            .buildLayerJson(dkLineLayerStr)
                            .builder();
                }

                mAutoMapSnapshot = AutoMapSnapshot.create(MapSnapshotTest.this, styleJosn, new AutoMapSnapshot.ICallBack() {
                    @Override
                    public void onNext(SnapParam snapParam) {
                        Log.i("gmbgl_test",snapParam.toJson());
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        Log.e("gmbgl_test",e.getMessage());
                    }

                    @Override
                    public void complete() {
                        Log.i("gmbgl_test2","坐落图生成完毕");
                        Toast.makeText(MapSnapshotTest.this, "坐落图生成完毕！", Toast.LENGTH_LONG).show();
                        mAutoMapSnapshot.cancel();
                    }
                }).batchSnapShot(snapParams,mGMapView.getMapBoxMap());
            }
        });
        manual.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<String> waterMark = new ArrayList<>();
                waterMark.add("坐落图自定义样式");
                List<SnapParam> snapParams = new ArrayList<>();
                String cachePath = Environment.getExternalStorageDirectory().getAbsolutePath()+ File.separator+"toolmapframe" + File.separator +
                        "坐落图默认样式" + File.separator + StringEngine.get32UUID() +".png";
                List<FeatureSet> featureSets = FeatureSet.convert2FeatureSets(mGMapView.getSelectSet());

                // snapParam.setFeatureSets(featureSets);
              //  snapParams.add(snapParam);
                String styleJosn = StyleJsonBuilder.create().builder();
                for (FeatureSet featureSet : featureSets) {
                    String dkLineLayerStr = String.format("{\n" +
                            "      \"id\": \"%s\",\n" +
                            "      \"source\": \"%s\",\n" +
                            "      \"type\": \"line\",\n" +
                            "      \"layout\": {\n" +
                            "        \"visibility\": \"visible\"\n" +
                            "      },\n" +
                            "      \"paint\": {\n" +
                            "        \"line-color\": \"yellow\",\n" +
                            "        \"line-opacity\": 1,\n" +
                            "        \"line-width\": 2\n" +
                            "      }\n" +
                            "    }",featureSet.getLayerId(),featureSet.getSourceId());
                    styleJosn = StyleJsonBuilder.create(styleJosn)
                            .buildSourceJson(featureSet.getSourceId())
                            .buildLayerJson(dkLineLayerStr)
                            .builder();
                    featureSet.jsonSerializable();
                }
                SnapParam snapParam = new SnapParam(featureSets,cachePath,waterMark);
                snapParam.setFeature(featureSets.get(0).features().get(0));
                Intent intent = new Intent(MapSnapshotTest.this, ManualMapSnapshotActivity.class);
                intent.putExtra(ManualMapSnapshotActivity.STYLEJSON,styleJosn);
                intent.putExtra(ManualMapSnapshotActivity.SNAPPARAM,snapParam.toJson());
                startActivityForResult(intent,100);
            }
        });
        customAuto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String s = readLocalJson(MapSnapshotTest.this, "geojson.json");
                FeatureCollection featureCollection = FeatureCollection.fromJson(s);

                List<Feature> features = featureCollection.features();
                mapScreenshotTool = new MapScreenshotTool();
                LogUtils.i("自定义方式生成",features.size());
                for (Feature feature : features) {
                    ScreenshotsSetting screenshotsSetting= new ScreenshotsSetting();
                    screenshotsSetting.setImageUrl("https://t0.tianditu.gov.cn/DataServer?T=img_w&x={x}&y={y}&l={z}&tk=583e63953a6ed6bf304e68120db4c512");
                    ScreenshotsFeatureStye screenshotsFeatureStye = new ScreenshotsFeatureStye();
                    screenshotsFeatureStye.setFeature(feature);
                    screenshotsSetting.addScreenshotsFeatureStye(screenshotsFeatureStye);
                   // String[] split = snapParam.getWaterMark().split(";");
                    //screenshotsSetting.setWaters(Arrays.asList(split));
                    String cachePath = Environment.getExternalStorageDirectory().getAbsolutePath()+ File.separator+"toolmapframe" + File.separator +
                            "自定义" + File.separator + StringEngine.get32UUID() +".png";
                    mapScreenshotTool.createMapScreenshotsAsync(screenshotsSetting, cachePath,  new MapScreenshotTool.ICallBack() {
                        @Override
                        public void onError() {
                            // iCallBack.onError(e);
                            LogUtils.i("自定义方式","自定义方式生成失败");
                        }

                        @Override
                        public void complete(ScreenshotsSetting screenshotsSetting, String path) {
                            LogUtils.i("自定义方式","自定义方式生成成功："+path);
                        }
                    });
                }


            }
        });

        customAutoTotal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String s = readLocalJson(MapSnapshotTest.this, "geojson.json");
                FeatureCollection featureCollection = FeatureCollection.fromJson(s);
                List<Feature> features = featureCollection.features();
                mapScreenshotTool = new MapScreenshotTool();
                LogUtils.i("自定义方式生成总图",features.size());
                ScreenshotsSetting screenshotsSetting= new ScreenshotsSetting();
                screenshotsSetting.setDrawRect(true);
                screenshotsSetting.setImageUrl("https://t0.tianditu.gov.cn/DataServer?T=img_w&x={x}&y={y}&l={z}&tk=583e63953a6ed6bf304e68120db4c512");
                for (Feature feature : features) {
                    ScreenshotsFeatureStye screenshotsFeatureStye = new ScreenshotsFeatureStye();
                    screenshotsFeatureStye.setFeature(feature);
                    screenshotsFeatureStye.setDrawPolygonMark(false);
                    screenshotsSetting.addScreenshotsFeatureStye(screenshotsFeatureStye);
                }
                String cachePath = Environment.getExternalStorageDirectory().getAbsolutePath()+ File.separator+"toolmapframe" + File.separator +
                        "自定义总图" + File.separator + StringEngine.get32UUID() +".png";
                mapScreenshotTool.createMapScreenshotsAsync(screenshotsSetting, cachePath,  new MapScreenshotTool.ICallBack() {
                    @Override
                    public void onError() {
                        // iCallBack.onError(e);
                        LogUtils.i("自定义方式","自定义方式生成失败");
                    }

                    @Override
                    public void complete(ScreenshotsSetting screenshotsSetting, String path) {
                        LogUtils.i("自定义方式","自定义方式生成成功："+path);
                    }
                });
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGMapView.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mGMapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mGMapView.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mGMapView.onStop();

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mGMapView.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mGMapView.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mGMapView.onDestroy();
        if(mapScreenshotTool!=null){
            mapScreenshotTool.stopCreateMapScreenshotsTask();
        }

        //生成坐落图要关闭，避免在未生成完时关闭页面导致内存无法释放
        if(mAutoMapSnapshot!=null){
            mAutoMapSnapshot.cancel();
        }

    }


    @Override
    public void onBoxMapReady(MapboxMap mapboxMap) {
       /* BoxHttpClient.getSingleton().addInterceptor(new Interceptor() {
            @Override
            public Response intercept(Interceptor.Chain chain) throws IOException {
                Log.i("gmbgl","requestUrl");
                Request request = chain.request();
                String requestUrl = chain.request().url().toString();
                requestUrl = requestUrl.replace("172.20.60.12","10.244.105.113");
                requestUrl = requestUrl.replace("59.227.152.115","10.244.105.113");
                Log.i("gmbgl",requestUrl);
                Request.Builder builder = request.newBuilder();
                builder.url(requestUrl);
                Response response = chain.proceed(builder.build());
                return response;
            }
        }).setToken("eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiLogL_kvbPolYrkuKrkuroiLCJjb250ZXh0VXNlcklkIjoicDlGQjJDQTM4RDREOTQ0MjA4QjAwNDI2MkMwMEM2Rjc4IiwiY29udGV4dE5hbWUiOiJnZW8iLCJjb250ZXh0RGVwdElkIjoiMSIsImNvbnRleHRBcHBsaWNhdGlvbklkIjoiMSIsImV4cCI6MjYxMzIxODk2NDd9.dM92KheaX7Yp7GsHB_iFubyzV8KClL9mFKDy0a1AwXM");*/
        String url ="http://gykj123.cn:9035/api/v1/styles/gykj/rJg3x0CN_7";
      //  String MAP_OFFLINE_STYLE_JSON ="asset://style.json";
     //   ConnectivityReceiver.instance(this).setConnected(true);
        mapboxMap.setStyle(url, new Style.OnStyleLoaded() {
            @Override
            public void onStyleLoaded(@NonNull Style style) {

               // addRadarData(style);
               /* refreshGeoJsonRunnable = new RefreshGeoJsonRunnable();
                do {
                    handler.postDelayed(refreshGeoJsonRunnable, 1000);
                }
                while (index == 37);*/

                mGMapView.initSelection();
                mGMapView.setSelectSetting(new SelectSetting(true,true,"种植地块"));
                mGMapView.setMapEvent(MapSnapshotTest.this);
                mGMapView.moveToLocation(34.09768527002947,114.46451392126596, 16.0,null);
               // ((MyApplication)MapSnapshotTest.this.getApplication()).gMapView =  mGMapView;
            }
        });
    }
    private void addRadarData(@NonNull Style loadedMapStyle) {
        VectorSource vectorSource = new VectorSource(
                ID_SOURCE,
                SOURCE_URL
        );
        loadedMapStyle.addSource(vectorSource);
        layer = loadedMapStyle.getLayerAs(ID_LAYER);
        if (layer == null) {
            layer = new FillLayer(ID_LAYER, ID_SOURCE);
            layer.withSourceLayer("201806261518");
            //layer.setFilter(eq((get("idx")), literal(0)));
            layer.setProperties(PropertyFactory.visibility(VISIBLE),
                    fillColor(interpolate(Expression.exponential(1f),
                            get("value"),
                            stop(8, Expression.rgb(20, 160, 240)),
                            stop(18, Expression.rgb(20, 190, 240)),
                            stop(36, Expression.rgb(20, 220, 240)),
                            stop(54, Expression.rgb(20, 250, 240)),
                            stop(72, Expression.rgb(20, 250, 160)),
                            stop(90, Expression.rgb(135, 250, 80)),
                            stop(108, Expression.rgb(250, 250, 0)),
                            stop(126, Expression.rgb(250, 180, 0)),
                            stop(144, Expression.rgb(250, 110, 0)),
                            stop(162, Expression.rgb(250, 40, 0)),
                            stop(180, Expression.rgb(180, 40, 40)),
                            stop(198, Expression.rgb(110, 40, 80)),
                            stop(216, Expression.rgb(80, 40, 110)),
                            stop(234, Expression.rgb(50, 40, 140)),
                            stop(252, Expression.rgb(20, 40, 170))
                            )
                    ),
                    PropertyFactory.fillOpacity(0.7f));
            loadedMapStyle.addLayer(layer);
        }
    }
    private class RefreshGeoJsonRunnable implements Runnable {
        @Override
        public void run() {
           // layer.setFilter(eq((Expression.get("idx")), literal(index)));
            index++;
            if (index == 37) {
                index = 0;
            }
            handler.postDelayed(this, 1000);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 100&&resultCode== Activity.RESULT_OK){
            Log.i("gmbgl_test2","坐落图生成完毕");
            Toast.makeText(MapSnapshotTest.this, "坐落图生成完毕！"+data.getStringExtra(ManualMapSnapshotActivity.RESULT_PATH), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public boolean onMapClick(@NonNull LatLng point) {
        PointF pointF = mGMapView.getMapBoxMap().getProjection().toScreenLocation(point);
        mGMapView.selectFeatureByPoint(pointF,null);
        return false;
    }

    @Override
    public boolean onMapLongClick(@NonNull LatLng point) {
        return false;
    }

    @Override
    public void onCameraIdle() {

    }

    @Override
    public void onCameraMoveCanceled() {

    }

    @Override
    public void onCameraMove() {

    }

    @Override
    public void onCameraMoveStarted(int reason) {

    }

    @Override
    public void onCompassAnimation() {

    }

    @Override
    public void onCompassAnimationFinished() {

    }

    @Override
    public void onFling() {

    }

    @Override
    public void onFpsChanged(double fps) {

    }

    @Override
    public void onScroll() {

    }

    @Override
    public boolean onMarkerClick(@NonNull Marker marker) {
        return false;
    }

    @Override
    public void onPolygonClick(@NonNull Polygon polygon) {

    }

    @Override
    public void onPolylineClick(@NonNull Polyline polyline) {

    }

    @Override
    public boolean onTouchStart(MotionEvent motionEvent) {
        return false;
    }

    @Override
    public boolean onTouchMoving(MotionEvent motionEvent) {
        return false;
    }

    @Override
    public boolean onTouchCancel(MotionEvent motionEvent) {
        return false;
    }

    private   String readLocalJson(Context context, String fileName){
        String jsonString="";
        String resultString="";
        try {
            BufferedReader bufferedReader=new BufferedReader(new InputStreamReader(
                    context.getResources().getAssets().open(fileName)));
            while ((jsonString=bufferedReader.readLine())!=null) {
                resultString+=jsonString;
            }
        } catch (Exception e) {
            // TODO: handle exception
        }
        return resultString;
    }
}
