package com.gradtech.mapframe.mapdemo;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.blankj.utilcode.util.LogUtils;
import com.google.gson.JsonElement;
import com.gradtech.mapframev10.core.GMap;
import com.gradtech.mapframev10.core.enumeration.GeometryType;
import com.gradtech.mapframev10.core.event.IMapEvent;
import com.gradtech.mapframev10.core.maps.GMapView;
import com.gradtech.mapframev10.core.select.ISelectionSet;
import com.gradtech.mapframev10.core.select.SelectSetting;
import com.gradtech.mapframev10.core.snapshot.AutoMapSnapshot;
import com.gradtech.mapframev10.core.snapshot.ManualMapSnapshotActivity;
import com.gradtech.mapframev10.core.snapshot.bean.SnapParam;
import com.gradtech.mapframev10.core.snapshot.custom.MapScreenshotTool;
import com.gradtech.mapframev10.core.snapshot.custom.ScreenshotsFeatureStye;
import com.gradtech.mapframev10.core.snapshot.custom.ScreenshotsSetting;
import com.gradtech.mapframev10.core.util.FeatureSet;
import com.gradtech.mapframev10.core.util.StringEngine;
import com.gradtech.mapframev10.core.util.StyleJsonBuilder;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Geometry;
import com.mapbox.geojson.Point;
import com.mapbox.maps.ScreenCoordinate;
import com.mapbox.maps.Style;
import com.mapbox.maps.extension.observable.eventdata.MapLoadingErrorEventData;
import com.mapbox.maps.extension.style.StyleExtensionImpl;
import com.mapbox.maps.extension.style.layers.generated.FillLayer;
import com.mapbox.maps.plugin.Plugin;
import com.mapbox.maps.plugin.annotation.generated.OnPointAnnotationClickListener;
import com.mapbox.maps.plugin.annotation.generated.OnPointAnnotationLongClickListener;
import com.mapbox.maps.plugin.annotation.generated.PointAnnotation;
import com.mapbox.maps.plugin.delegates.listeners.OnMapLoadErrorListener;
import com.mapbox.maps.plugin.gestures.GesturesPluginImpl;
import com.mapbox.maps.plugin.logo.LogoViewPlugin;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * @ClassName MapScreenshotTestActivity
 * @Description TODO
 * @Author: fs
 * @Date: 2023/1/5 9:23
 * @Version 2.0
 */
public class MapScreenshotTestActivity extends AppCompatActivity implements IMapEvent {

    GMapView gMapView;
    private Button btn_auto_create,btn_custum_create,btn_auto_activity;

    private AutoMapSnapshot mAutoMapSnapshot;
    MapScreenshotTool mapScreenshotTool;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        GMap.getInstance(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_snapshot_test);
        gMapView = findViewById(R.id.mapView);
        btn_auto_create = findViewById(R.id.btn_auto_create);
        btn_custum_create  = findViewById(R.id.btn_custum_create);
        btn_auto_activity = findViewById(R.id.btn_auto_activity);
        btn_auto_activity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<String> waterMark = new ArrayList<>();
                waterMark.add("坐落图自定义样式");
                List<SnapParam> snapParams = new ArrayList<>();
                String cachePath = Environment.getExternalStorageDirectory().getAbsolutePath()+ File.separator+"toolmapframe" + File.separator +
                        "坐落图默认样式" + File.separator + StringEngine.get32UUID() +".png";
                List<FeatureSet> featureSets = FeatureSet.convert2FeatureSets(gMapView.getSelectSet());

                SnapParam snapParam = new SnapParam(featureSets,cachePath,waterMark);
                snapParam.setFeature(featureSets.get(0).features().get(0));
                //snapParam.setFeatureSets(featureSets);
                Intent intent = new Intent(MapScreenshotTestActivity.this, ManualMapSnapshotActivity.class);
                intent.putExtra(ManualMapSnapshotActivity.STYLEJSON_URI,"asset://snapshotstyle.json");
                intent.putExtra(ManualMapSnapshotActivity.SNAPPARAM,snapParam.toJson());
                startActivityForResult(intent,100);

            }
        });
        btn_custum_create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<Feature> features = FeatureSet.convert2Features(gMapView.getSelectSet());
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
        btn_auto_create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<Feature> features = FeatureSet.convert2Features(gMapView.getSelectSet());
                Feature feature1 = features.get(0);
               /* for (int i = 0; i< 100;i++ ) {
                    features.add(feature1);
                }*/
                //水印
                List<String> waterMark = new ArrayList<>();
                waterMark.add("坐落图默认样式");
                waterMark.add("坐落图默认样式");
                waterMark.add("坐落图默认样式");
                waterMark.add("坐落图默认样式");
                List<SnapParam> snapParams = new ArrayList<>();
                for (Feature feature : features) {
                    String cachePath = Environment.getExternalStorageDirectory().getAbsolutePath()+ File.separator+"toolmapframe" + File.separator +
                            "坐落图默认样式" + File.separator + StringEngine.get32UUID() +".png";
                    SnapParam snapParam = new SnapParam(feature,cachePath,waterMark);
                    snapParams.add(snapParam);
                }
                String styleJosn = StyleJsonBuilder.create().builder();
                 mAutoMapSnapshot = AutoMapSnapshot.create(MapScreenshotTestActivity.this, styleJosn, new AutoMapSnapshot.ICallBack() {
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
                        Toast.makeText(MapScreenshotTestActivity.this, "坐落图生成完毕！", Toast.LENGTH_LONG).show();
                        mAutoMapSnapshot.cancel();
                    }
                }).batchSnapShot(snapParams,gMapView.getMapboxMap());

            }
        });

        String MAP_OFFLINE_STYLE_JSON ="asset://style.json";
        // String MAP_OFFLINE_STYLE_JSON ="http://gykj123.cn:9035/api/v1/styles/gykj/rJg3x0CN_7";
        StyleExtensionImpl.Builder styleExtension = new StyleExtensionImpl.Builder(MAP_OFFLINE_STYLE_JSON);
        gMapView.getMapboxMap().loadStyle(styleExtension.build(), new Style.OnStyleLoaded() {
            @Override
            public void onStyleLoaded(@NotNull Style style) {
                LogoViewPlugin plugin = gMapView.getPlugin(Plugin.MAPBOX_LOGO_PLUGIN_ID);
                plugin.setEnabled(false);
                GesturesPluginImpl plugin1 = gMapView.getPlugin(Plugin.MAPBOX_GESTURES_PLUGIN_ID);

                plugin1.setRotateEnabled(false);
                gMapView.addMapEvent(MapScreenshotTestActivity.this);

                FillLayer selectLayer = gMapView.getStyleLayerManager().layerClone2TransparentFillLayer("种植地块");
                if(!gMapView.getStyleLayerManager().hasLayer(selectLayer)){
                    gMapView.getStyleLayerManager().addLayer(selectLayer);
                }
                gMapView.initSelection(new SelectSetting(true,true,true,selectLayer.getLayerId()));
                gMapView.moveToLocation(34.45794227238672,113.87665450572968,15.0);
            }

        }, new OnMapLoadErrorListener() {
            @Override
            public void onMapLoadError(@NotNull MapLoadingErrorEventData mapLoadingErrorEventData) {
                Log.i("getMapboxMap", mapLoadingErrorEventData.toString());
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        gMapView.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        gMapView.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        gMapView.onDestroy();

        if(mapScreenshotTool!=null){
            mapScreenshotTool.stopCreateMapScreenshotsTask();
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        gMapView.onLowMemory();
    }

    @Override
    public boolean onMapClick(@NonNull Point point) {
        ScreenCoordinate pixelForCoordinate = gMapView.getMapboxMap().pixelForCoordinate(point);
        gMapView.selectByScreenCoordinate(pixelForCoordinate, null, new ISelectionSet.IQueryFeaturesCallback() {
            @Override
            public void runCallBack(@NotNull ISelectionSet.SelectStatus selectStatus, @NotNull HashMap<String, FeatureSet> selectSet) {
                Log.i("selectCallbak",selectStatus.name());
                Set<String> strings = selectSet.keySet();
                for (String string : strings) {
                    Log.i("selectCallbak",string+","+selectSet.get(string).features().size());
                }
            }
        });
        return true;
    }
}
