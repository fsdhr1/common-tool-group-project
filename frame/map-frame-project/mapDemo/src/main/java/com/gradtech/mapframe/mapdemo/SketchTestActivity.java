package com.gradtech.mapframe.mapdemo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.blankj.utilcode.util.ToastUtils;
import com.gradtech.mapframev10.core.GMap;
import com.gradtech.mapframev10.core.enumeration.GeometryType;
import com.gradtech.mapframev10.core.event.IMapEvent;
import com.gradtech.mapframev10.core.maps.GMapView;
import com.gradtech.mapframev10.core.offline.gc.GraphicContainer;
import com.gradtech.mapframev10.core.offline.gc.GraphicSetting;
import com.gradtech.mapframev10.core.offline.geosource.IBaseGeoSource;
import com.gradtech.mapframev10.core.sketch.SketchAnnotations;
import com.gradtech.mapframev10.core.util.GsonFactory;
import com.mapbox.common.DownloadOptions;
import com.mapbox.common.HttpRequest;
import com.mapbox.common.HttpResponse;
import com.mapbox.common.HttpServiceFactory;
import com.mapbox.common.HttpServiceInterceptorInterface;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.Geometry;
import com.mapbox.geojson.Point;
import com.mapbox.geojson.Polygon;
import com.mapbox.maps.Style;
import com.mapbox.maps.extension.observable.eventdata.MapLoadingErrorEventData;
import com.mapbox.maps.extension.style.StyleExtensionImpl;
import com.mapbox.maps.plugin.Plugin;
import com.mapbox.maps.plugin.delegates.listeners.OnMapLoadErrorListener;
import com.mapbox.maps.plugin.gestures.GesturesPluginImpl;
import com.mapbox.maps.plugin.logo.LogoViewPlugin;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SketchTestActivity extends AppCompatActivity implements IMapEvent {
    private FrameLayout flMainFrame;
    GMapView gMapView;
    private Button jd, qk, ks, js, undo, redo, addgeo, ksdhx, ksdhm, btnAdd2gc;

    private GraphicContainer graphicContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        GMap.getInstance(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        flMainFrame = findViewById(R.id.flMainFrame);
        jd = findViewById(R.id.jd);
        qk = findViewById(R.id.qk);
        ks = findViewById(R.id.ks);
        js = findViewById(R.id.js);
        undo = findViewById(R.id.undo);
        redo = findViewById(R.id.redo);
        addgeo = findViewById(R.id.addgeo);
        ksdhx = findViewById(R.id.ksdhx1);
        ksdhm = findViewById(R.id.ksdhm);
        btnAdd2gc = findViewById(R.id.btn_add2gc);

        //绘制点划线
        ksdhx.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gMapView.startSketch(GeometryType.slideLine);
                gMapView.setIOnAfterSlideListener(new SketchAnnotations.IOnAfterSlideListener() {
                    @Override
                    public void onAfterSlideListener(@Nullable Geometry geometry) {
                        Log.i(GMapView.LOG, GsonFactory.getFactory().getComMapBoxGson().toJson(geometry));
                    }
                });
            }
        });
        //绘制点划面
        ksdhm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gMapView.startSketch(GeometryType.slidePolygon);
            }
        });
        //绘制加点
        jd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (gMapView.isStartSketch()) {
                    gMapView.sketchAddScreenCenterPoint();
                }
            }
        });
        //清空
        qk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (gMapView.isStartSketch()) {
                    gMapView.clearSketch();
                }
            }
        });
        //启动绘制
        ks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gMapView.startSketch(GeometryType.polygon);
            }
        });
        //结束绘制
        js.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (gMapView.isStartSketch()) {
                    gMapView.stopSketch();
                }
            }
        });
        //回撤
        undo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (gMapView.isStartSketch()) {
                    gMapView.sketchUnDo();
                }
            }
        });
        //前撤
        redo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (gMapView.isStartSketch()) {
                    gMapView.sketchReDo();
                }
            }
        });
        //添加初始化图形
        addgeo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (gMapView.isStartSketch()) {
                    //
                    Polygon polygon = Polygon.fromJson("{\"coordinates\":[[[98.220338872,43.557277427],[107.340788804,40.903616209],[100.174721427,37.727794948],[98.220338872,43.557277427]]],\"type\":\"Polygon\"}");
                    gMapView.setSketchGeometry(polygon);
                }
            }
        });
        btnAdd2gc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Geometry sketchGeometry = gMapView.getSketchGeometry();
                if (sketchGeometry == null) {
                    return;
                }
                Feature feature = Feature.fromGeometry(sketchGeometry);
                List<Feature> features = new ArrayList<>();
                features.add(feature);
                graphicContainer.inserts(features, new IBaseGeoSource.IGcCallBack() {
                    @Override
                    public void onGcCallBack(@Nullable List<Feature> features) {
                        ToastUtils.showShort("");
                    }

                    @Override
                    public void onError(@NotNull Throwable e) {

                    }
                },true,false);
            }
        });
        HttpServiceFactory.getInstance().setInterceptor(new HttpServiceInterceptorInterface() {
            @NonNull
            @Override
            public HttpRequest onRequest(@NonNull HttpRequest request) {
                Log.i("mapboxv10","HttpRequest");
                String url = request.getUrl();
                if(url.contains("a.tiles.mapbox.com")){
                    HttpRequest build = request.toBuilder().url("https://t0.tianditu.gov.cn/DataServer?T=img_w&x={x}&y={y}&l={z}&tk=583e63953a6ed6bf304e68120db4c512").build();
                    return build;
                }
                return request;
            }

            @NonNull
            @Override
            public DownloadOptions onDownload(@NonNull DownloadOptions download) {
                Log.i("mapboxv10","download");
                return download;
            }

            @NonNull
            @Override
            public HttpResponse onResponse(@NonNull HttpResponse response) {
                Log.i("mapboxv10","response");
                return response;
            }
        });
        gMapView = new GMapView(this);
        String styel = "https://api.grandtechmap.com/mapeditor/styles/ASZhSsCC6?access_token=tiletoken.mtyynje0mtm4mjyzotzjztgzmmqzzdmzzjrmogjhndvjowqwowjjy2m2njrk";
        StyleExtensionImpl.Builder styleExtension = new StyleExtensionImpl.Builder(styel);
        flMainFrame.addView(gMapView, 0);
        gMapView.getMapboxMap().loadStyle(styleExtension.build(), new Style.OnStyleLoaded() {
            @Override
            public void onStyleLoaded(@NotNull Style style) {
                Log.i("getMapboxMap", "加载完成");
                Toast.makeText(SketchTestActivity.this, "加载完成", Toast.LENGTH_SHORT);
                gMapView.addMapEvent(SketchTestActivity.this);
                gMapView.startSketch(GeometryType.polygon);
                LogoViewPlugin plugin = gMapView.getPlugin(Plugin.MAPBOX_LOGO_PLUGIN_ID);
                plugin.setEnabled(false);
                GesturesPluginImpl plugin1 = gMapView.getPlugin(Plugin.MAPBOX_GESTURES_PLUGIN_ID);


       /* plugin1.addOnMapClickListener(new OnMapClickListener() {
            @Override
            public boolean onMapClick(@NotNull Point point) {
                Log.i("getMapboxMap", "onMapClick");
                return true;
            }
        });
        plugin1.addOnMapClickListener(new OnMapClickListener() {
            @Override
            public boolean onMapClick(@NotNull Point point) {
                Log.i("getMapboxMap", "onMapClick1");
                return false;
            }
        });*/
                plugin1.setRotateEnabled(false);
                initGc();
            }
        }, new OnMapLoadErrorListener() {
            @Override
            public void onMapLoadError(@NotNull MapLoadingErrorEventData mapLoadingErrorEventData) {
                Log.i("getMapboxMap", mapLoadingErrorEventData.toString());
            }
        });


        // SketchAnnotations sketchAnnotations = new SketchAnnotations(this,gMapView);
    }

    private void initGc() {
        String cachePath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "toolmapframeV10" + File.separator + "Cache" + File.separator + "frame" + File.separator + "data.json";
        graphicContainer = new GraphicContainer(gMapView, new GraphicSetting(GeometryType.polygon, "种植地块", cachePath));
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
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        gMapView.onLowMemory();
    }

    @Override
    public boolean onMapClick(@NonNull Point point) {
        return false;
    }
}