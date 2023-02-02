package com.gradtech.mapframe.mapdemo;

import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.blankj.utilcode.util.LogUtils;
import com.gradtech.mapframev10.core.GMap;
import com.gradtech.mapframev10.core.enumeration.GeometryType;
import com.gradtech.mapframev10.core.event.IMapEvent;
import com.gradtech.mapframev10.core.maps.GMapView;
import com.gradtech.mapframev10.core.offline.editor.BaseEditor;
import com.gradtech.mapframev10.core.offline.editor.IToolInterceptor;
import com.gradtech.mapframev10.core.offline.gc.GraphicSetting;
import com.gradtech.mapframev10.core.select.ISelectionSet;
import com.gradtech.mapframev10.core.select.SelectSetting;
import com.gradtech.mapframev10.core.sketch.SketchAnnotations;
import com.gradtech.mapframev10.core.util.FeatureSet;
import com.mapbox.common.DownloadOptions;
import com.mapbox.common.HttpRequest;
import com.mapbox.common.HttpResponse;
import com.mapbox.common.HttpServiceFactory;
import com.mapbox.common.HttpServiceInterceptorInterface;
import com.mapbox.common.HttpServiceInterface;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.Geometry;
import com.mapbox.geojson.Point;
import com.mapbox.geojson.Polygon;
import com.mapbox.maps.ScreenCoordinate;
import com.mapbox.maps.Style;
import com.mapbox.maps.StylePropertyValue;
import com.mapbox.maps.extension.observable.eventdata.MapLoadingErrorEventData;
import com.mapbox.maps.extension.style.StyleExtensionImpl;
import com.mapbox.maps.extension.style.expressions.generated.Expression;
import com.mapbox.maps.extension.style.layers.generated.FillLayer;
import com.mapbox.maps.extension.style.layers.generated.LineLayer;
import com.mapbox.maps.plugin.Plugin;
import com.mapbox.maps.plugin.delegates.listeners.OnMapLoadErrorListener;
import com.mapbox.maps.plugin.gestures.GesturesPluginImpl;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

/**
 * @ClassName StyleManagerTestActivity
 * @Description TODO
 * @Author: fs
 * @Date: 2022/9/5 14:22
 * @Version 2.0
 */
public class SelectionTestActivity extends AppCompatActivity implements IMapEvent {
    GMapView gMapView;
    private Button splzzdk,sklzzdk,splzzdkfill,sklzzdkfill,hb;

    GraphicSetting graphicSetting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        GMap.getInstance(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_1);
        gMapView = findViewById(R.id.mapView);
        splzzdk = findViewById(R.id.splzzdk);
        sklzzdk = findViewById(R.id.sklzzdk);
        splzzdkfill = findViewById(R.id.splzzdkfill);
        sklzzdkfill = findViewById(R.id.sklzzdkfill);
        hb = findViewById(R.id.hb);
        splzzdk.setOnClickListener(v -> {
            clearEvent();
            gMapView.addMapEvent(SelectionTestActivity.this);
            gMapView.initSelection(new SelectSetting(false,true,true,"种植地块"));
        });
        splzzdkfill.setOnClickListener(v -> {
            clearEvent();
            gMapView.addMapEvent(SelectionTestActivity.this);
            FillLayer selectLayer = gMapView.getStyleLayerManager().layerClone2TransparentFillLayer("种植地块");
            if(!gMapView.getStyleLayerManager().hasLayer(selectLayer)){
                gMapView.getStyleLayerManager().addLayer(selectLayer);
            }
            gMapView.initSelection(new SelectSetting(true,true,true,selectLayer.getLayerId()));
        });
        hb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BaseEditor baseEditor = gMapView.getBaseEditor(graphicSetting);
                baseEditor.unionFeatures(SelectionTestActivity.this, "合并", false, new IToolInterceptor() {
                    @Override
                    public boolean doHandle(@Nullable Object o, @Nullable String... flags) {
                        return false;
                    }

                    @Override
                    public void afterHandle(@Nullable Object o, @Nullable String... flags) {

                    }
                },gMapView.getSelectSet());
            }
        });
        sklzzdk.setOnClickListener(v -> {
            clearEvent();
            LinkedHashMap linkedHashMap = new LinkedHashMap();
            linkedHashMap.put("种植地块","dkbm");
            gMapView.initSelection(new SelectSetting(true,true,true,"种植地块"),linkedHashMap);
            gMapView.startSketch(GeometryType.slideLine);
            gMapView.setIOnAfterSlideListener(new SketchAnnotations.IOnAfterSlideListener() {
                @Override
                public void onAfterSlideListener(@Nullable Geometry geometry) {
                    gMapView.clearSketch();
                    gMapView.selectByPolygonOrLine(geometry, null, new ISelectionSet.IQueryFeaturesCallback() {
                        @Override
                        public void runCallBack(@NotNull ISelectionSet.SelectStatus selectStatus, @NotNull HashMap<String, FeatureSet> selectSet) {
                            Log.i("selectCallbak",selectStatus.name());
                            Set<String> strings = selectSet.keySet();
                            for (String string : strings) {
                                Log.i("selectCallbak",string+","+selectSet.get(string).features().size());
                            }
                        }
                    });
                }
            });
        });
        sklzzdkfill.setOnClickListener(v -> {
            clearEvent();
            LinkedHashMap linkedHashMap = new LinkedHashMap();
            linkedHashMap.put("种植地块","dkbm");
            FillLayer selectLayer = gMapView.getStyleLayerManager().layerClone2TransparentFillLayer("种植地块");
            if(!gMapView.getStyleLayerManager().hasLayer(selectLayer)){
                gMapView.getStyleLayerManager().addLayer(selectLayer);
            }
            gMapView.initSelection(new SelectSetting(true,true,true,false,selectLayer.getLayerId()),linkedHashMap);
            gMapView.startSketch(GeometryType.slideLine);
            gMapView.setIOnAfterSlideListener(new SketchAnnotations.IOnAfterSlideListener() {
                @Override
                public void onAfterSlideListener(@Nullable Geometry geometry) {
                    gMapView.clearSketch();
                    gMapView.selectByPolygonOrLine(geometry, null, new ISelectionSet.IQueryFeaturesCallback() {
                        @Override
                        public void runCallBack(@NotNull ISelectionSet.SelectStatus selectStatus, @NotNull HashMap<String, FeatureSet> selectSet) {
                            Log.i("selectCallbak",selectStatus.name());
                            Set<String> strings = selectSet.keySet();
                            for (String string : strings) {
                                Log.i("selectCallbak",string+","+selectSet.get(string).features().size());
                            }
                        }
                    });
                }
            });
        });

        gMapView.getBoxHttpClient().addInterceptors(new HttpServiceInterceptorInterface() {
            @NonNull
            @Override
            public HttpRequest onRequest(@NonNull HttpRequest request) {
                Log.i("mapboxv10","HttpRequest");
                String url = request.getUrl();
                if(url.contains("a.tiles.mapbox.com")){
                    url = url.replace("sk.eyJ1IjoiZnMxOTk1MDMwMSIsImEiOiJja2xhZ210czgzamVuMnNxb2dkcTN6ZXRyIn0.OjKkjFdRWrZBP432CSAGHw1111","sk.eyJ1IjoiZnMxOTk1MDMwMSIsImEiOiJja2xhZ210czgzamVuMnNxb2dkcTN6ZXRyIn0.OjKkjFdRWrZBP432CSAGHw");
                    HttpRequest build = request.toBuilder().url(url).build();
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
        } );
        String MAP_OFFLINE_STYLE_JSON ="asset://style.json";
        String styel = "https://api.grandtechmap.com/mapeditor/styles/ASZhSsCC6?access_token=tiletoken.mtyynje0mtm4mjyzotzjztgzmmqzzdmzzjrmogjhndvjowqwowjjy2m2njrk";
        StyleExtensionImpl.Builder styleExtension = new StyleExtensionImpl.Builder(MAP_OFFLINE_STYLE_JSON);

        gMapView.getMapboxMap().loadStyle(styleExtension.build(), new Style.OnStyleLoaded() {
            @Override
            public void onStyleLoaded(@NotNull Style style) {
                Log.i("getMapboxMap", "加载完成");
                Toast.makeText(SelectionTestActivity.this, "加载完成", Toast.LENGTH_SHORT);


                GesturesPluginImpl plugin1 = gMapView.getPlugin(Plugin.MAPBOX_GESTURES_PLUGIN_ID);
                plugin1.setRotateEnabled(false);

                String cachePath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "toolmapframeV10" + File.separator + "Cache" + File.separator + "frame" + File.separator + "data.json";
                graphicSetting = new GraphicSetting(GeometryType.polygon, "种植地块", cachePath);
                gMapView.addEditor(graphicSetting);

               /* HttpServiceFactory.getInstance().setInterceptor(new HttpServiceInterceptorInterface() {
                    @NonNull
                    @Override
                    public HttpRequest onRequest(@NonNull HttpRequest request) {
                        Log.i("mapboxv10","HttpRequest");
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
                });*/

                gMapView.moveToLocation(32.4868574154374,112.926891084656, 15.0);
                LogUtils.i("dt_sy",gMapView.getStyleLayerManager().getSourceUri("dt_sy"));
                gMapView.getStyleLayerManager().highLightRegion("POLYGON((112.926891084656 32.4868574154374,112.926946393861 32.4867183622626,112.92693113956 32.4866900587992,112.926837917636 32.4866530597909,112.926799357404 32.4866537522689,112.926689010589 32.4866848841002,112.926562600983 32.4867151345958,112.926399989168 32.4867409019711,112.926237005034 32.486736819049,112.926106399191 32.4867016798387,112.926088369583 32.4867153594263,112.926107854294 32.4869572114062,112.926045040246 32.4870055490669,112.926095347422 32.4872266815661,112.926118639863 32.4872599304015,112.92616078839 32.4873539554208,112.926183419829 32.487554800114,112.926241331672 32.487583029833,112.926268496594 32.4875724232288,112.926350471597 32.4874808938283,112.926646290994 32.4873106602588,112.926775441733 32.4872578565649,112.926901871125 32.4872417317206,112.92690963767 32.4872288938984,112.926862839648 32.4871497283773,112.926848993686 32.4870129181118,112.926855102781 32.4869258475499,112.926891084656 32.4868574154374))",null);
                Expression any = Expression.any(Expression.eq(Expression.get("objectid"),Expression.literal(350702687)),Expression.eq(Expression.get("objectid"),Expression.literal(350344017)));
                ((LineLayer)gMapView.getStyleLayerManager().getLayer("种植地块")).filter(any);
                // Expression.any()
            }
        }, new OnMapLoadErrorListener() {
            @Override
            public void onMapLoadError(@NotNull MapLoadingErrorEventData mapLoadingErrorEventData) {
                Log.i("getMapboxMap", mapLoadingErrorEventData.toString());
            }
        });


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
                    List<Feature> features = selectSet.get(string).features();
                    for (Feature feature : features) {
                        Log.i("selectCallbak111",feature.getStringProperty("objectid"));
                    }
                }
            }
        });
        return true;
    }

    private void clearEvent(){
        gMapView.removeMapEvent(SelectionTestActivity.this);
        gMapView.stopSketch();
        gMapView.clearSelectSetAndRender();
    }
}
