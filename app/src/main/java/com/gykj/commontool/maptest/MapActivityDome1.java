package com.gykj.commontool.maptest;

import android.graphics.Color;
import android.graphics.PointF;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.grandtech.mapframe.core.GMap;
import com.grandtech.mapframe.core.editor.IToolInterceptor;
import com.grandtech.mapframe.core.editor.gc.GraphicSetting;
import com.grandtech.mapframe.core.enumeration.GeometryType;
import com.grandtech.mapframe.core.event.IMapEvent;
import com.grandtech.mapframe.core.layer.IDoneListenerCallBack;
import com.grandtech.mapframe.core.layer.StyleLayerManager;
import com.grandtech.mapframe.core.maps.GMapView;
import com.grandtech.mapframe.core.maps.IOnMapReady;
import com.grandtech.mapframe.core.net.BoxHttpClient;
import com.grandtech.mapframe.core.select.SelectSetting;
import com.grandtech.mapframe.core.select.SelectionSet;
import com.grandtech.mapframe.core.sketch.SketchSetting;
import com.grandtech.mapframe.core.util.FeatureSet;
import com.grandtech.mapframe.core.util.Transformation;
import com.gykj.commontool.R;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.geojson.BoundingBox;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.Geometry;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.MultiLineString;
import com.mapbox.geojson.MultiPoint;
import com.mapbox.geojson.MultiPolygon;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.Polygon;
import com.mapbox.mapboxsdk.annotations.Polyline;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions;
import com.mapbox.mapboxsdk.location.LocationComponentOptions;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.MapboxMapOptions;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.module.http.HttpRequestUtil;
import com.mapbox.mapboxsdk.offline.OfflineManager;
import com.mapbox.mapboxsdk.style.expressions.Expression;
import com.mapbox.mapboxsdk.style.layers.CircleLayer;
import com.mapbox.mapboxsdk.style.layers.FillLayer;
import com.mapbox.mapboxsdk.style.layers.Layer;
import com.mapbox.mapboxsdk.style.layers.LineLayer;
import com.mapbox.mapboxsdk.style.layers.PropertyFactory;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.turf.TurfConstants;
import com.mapbox.turf.TurfMeasurement;
import com.mapbox.turf.TurfTransformation;
import com.vividsolutions.jts.geom.Envelope;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

import static com.mapbox.mapboxsdk.style.expressions.Expression.get;
import static com.mapbox.mapboxsdk.style.expressions.Expression.has;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textIgnorePlacement;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textOffset;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textSize;

/**
 * @ClassName MapActivityDome1
 * @Description TODO mapcore功能基本操作
 * @Author: fs
 * @Date: 2021/5/13 10:15
 * @Version 2.0
 */
@SuppressWarnings("ALL")
public class MapActivityDome1 extends AppCompatActivity implements IOnMapReady, IMapEvent, SelectionSet.ISelectSetChange, IToolInterceptor, PermissionsListener {
    public final static String TAG  = "com.grandtech.mapframe.ui.MapActivityDome1";

    private  PermissionsManager permissionsManager;
    private GMapView gMapView;

    private FrameLayout flMainFrame ;

    private Button hz,jd,undo,redo,add,deletes,uni,split,dig,gl,qxgl,dtmvt,qxdtmvt;
    private GraphicSetting mGraphicSetting;

    private void clearAmbientCache() {
        OfflineManager fileSource = OfflineManager.getInstance(this);
        fileSource.clearAmbientCache(new OfflineManager.FileSourceCallback() {
            @Override
            public void onSuccess() {
                //showToast(getString(R.string.clear_ambient_cache_size_toast_confirmation));
            }

            @Override
            public void onError(@NonNull String message) {
               // showToast(String.format(getString(
                   //     R.string.clear_ambient_cache_size_toast_error), message));
            }
        });
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //在项目中使用的时候这句话要放到applacation中初始化
        GMap.getInstance(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        flMainFrame = findViewById(R.id.flMainFrame);
        hz = findViewById(R.id.hz);
        jd = findViewById(R.id.jd);
        undo = findViewById(R.id.undo);
        redo = findViewById(R.id.redo);
        add = findViewById(R.id.add);
        uni = findViewById(R.id.uni);
        split = findViewById(R.id.split);
        dig = findViewById(R.id.dig);
        deletes = findViewById(R.id.deletes);
        gl = findViewById(R.id.gl);
        qxgl = findViewById(R.id.qxgl);
        dtmvt = findViewById(R.id.dtmvt);
        qxdtmvt = findViewById(R.id.qxdtmvt);
        hz.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clearAmbientCache();
                gMapView.moveToLocation(25.445684, 114.438901 ,15.5,null);//lot=114.4352&lat=25.44723http://gykj123.cn:8849/grandtech-middleground-maptile/api/v1/wms/jbnt_pic/?lot=114.4352&lat=25.44723&z=16.46&width=1276&height=1276&ratio=2
                if(gMapView.isStartSketch()){
                    gMapView.getBaseEditor(mGraphicSetting).drawFeature(add.getTransitionName(),false, MapActivityDome1.this,gMapView.getSketchGeometry());
                    gMapView.clearSelectSetAndRender();
                    gMapView.stopSketch();
                    hz.setText("绘制");
                }else {
                    gMapView.startSketch(GeometryType.polygon);
                    gMapView.clearSelectSetAndRender();
                    hz.setText("结束");
                }
            }
        });
        jd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(gMapView.isStartSketch()){
                    gMapView.sketchAddScreenCenterPoint();
                }else {
                    gMapView.startSketch(GeometryType.polygon);
                }
            }
        });
        undo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(gMapView.isStartSketch()){
                    gMapView.sketchUnDo();
                }else {
                    gMapView.startSketch(GeometryType.polygon);
                }
            }
        });
        redo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(gMapView.isStartSketch()){
                    gMapView.sketchReDo();
                }else {
                    gMapView.startSketch(GeometryType.polygon);
                }
            }
        });
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(gMapView.getSelectSet()!=null) {
                    gMapView.getBaseEditor(mGraphicSetting).addFeatures(add.getTransitionName(),false, MapActivityDome1.this,gMapView.getSelectSet());
                    gMapView.clearSelectSetAndRender();
                }

            }
        });
        uni.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(gMapView.getSelectSet()!=null) {
                    gMapView.getBaseEditor(mGraphicSetting).unionFeatures(MapActivityDome1.this,add.getTransitionName(),false, MapActivityDome1.this,gMapView.getSelectSet());
                    gMapView.clearSelectSetAndRender();
                }

            }
        });
        split.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(gMapView.getSelectSet()!=null) {
                    if(gMapView.isStartSketch()){
                        gMapView.getBaseEditor(mGraphicSetting).splitFeatures(add.getTransitionName(),false, MapActivityDome1.this,gMapView.getSelectSet(),gMapView.getSketchGeometry());
                        gMapView.clearSelectSetAndRender();
                        gMapView.stopSketch();
                    }else {
                        gMapView.startSketch(GeometryType.line);
                    }
                }
            }
        });
        dig.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(gMapView.getSelectSet()!=null) {
                    if(gMapView.isStartSketch()){
                        gMapView.getBaseEditor(mGraphicSetting).digFeatures(add.getTransitionName(),false, MapActivityDome1.this,gMapView.getSelectSet(),gMapView.getSketchGeometry());
                        gMapView.clearSelectSetAndRender();
                        gMapView.stopSketch();
                    }else {
                        gMapView.startSketch(GeometryType.polygon);
                    }
                }
            }
        });
        deletes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                gMapView.getGc(mGraphicSetting).deleteAll();

            }
        });
        gl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String regionWkt = "POLYGON((115.415514265 39.44213985,115.415514265 41.059078212,117.505956876 41.059078212,117.505956876 39.44213985,115.415514265 39.44213985))";

                gMapView.getStyleLayerManager().highLightRegion(regionWkt,null);
                gMapView.moveToLocation(regionWkt,null);
            }
        });
        qxgl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                gMapView.getStyleLayerManager().cancelHighLightRegion();

            }
        });
        dtmvt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    gMapView.getStyleLayerManager().showSimpleVector("dt_jjd","http://gykj123.cn:8849/grandtech-service-snyzt/api/v1/dynamictile/get_tile?layername=dt_jjd&x={x}&y={y}&z={z}",GeometryType.point);
                    //gMapView.getStyleLayerManager().showSimpleVector("dasdsa","https://mse.agribigdata.com.cn/vmap-server-bigdata/api/v1/tilesets/gykj/dt_zzdk/{z}/{x}/{y}.pbf",GeometryType.line);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });
        qxdtmvt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                gMapView.getStyleLayerManager().hideSimpleVector("dt_jjd");

            }
        });
        MapboxMapOptions mapOptions = new MapboxMapOptions();
        mapOptions.setPrefetchesTiles(true);
        mapOptions.compassEnabled(true);
        mapOptions.rotateGesturesEnabled(false);
        mapOptions.tiltGesturesEnabled(false);
        mapOptions.logoEnabled(false);
        mapOptions.attributionEnabled(false);
        mapOptions.maxZoomPreference(16.99);
        gMapView = new GMapView(this,mapOptions,this);
        gMapView.onCreate(savedInstanceState);
        flMainFrame.addView(gMapView);

    }

    @Override
    public void onBoxMapReady(MapboxMap mapboxMap) {
        BoxHttpClient.getSingleton().addInterceptor(new Interceptor() {
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
        }).setToken("Bearer eyJhbGciOiJSUzI1NiJ9.eyJ1c2VyX2lkIjo2ODQ0NjkyMDcxNjM1NywidXNlcl9uYW1lIjoiZ3lraiIsImF1dGhvcml0aWVzIjpbeyJhdXRob3JpdHkiOiJST0xFX2J1c2luZXNzX2RldmVsb3BlciJ9XSwianRpIjoiYjJmNDcwNTMtMDgzNC00M2I4LThjMDItMWJjYzNmMGUwNTg0IiwiaWF0IjoxNjI5MDczMDE0LCJleHAiOjE2MjkxNTk0MTR9.rSNbPWdqEeXsjQEd5_NUYz0Pk2-LIPXGJvfCdlHmEvmRwmtROJFlgovIWw0_RDjVgIPTIljHZ858QL_A8VnoYLuJ44WHJr3Yde6sxSs4YiHRXTE-QK26XMMy8QaUxQmaj1QeqHI1AfvdYZ_IptQIhojrIxWYY1vn_teygFXFL-HnkM0PxP6BvYkhL0icucZHLtoTvCU-K8TV3q5r5HYtwYw5sj6eNw_WIiXFKbkVO6gi_g6iau5Xk4tYFFHOMxvKnBliKi9_l354EzyzvqRp923c7Ii631rTg1kalGmn4SgwQxzQbvsOgs0NJBZkmFBvxx1SkroBbMaBs4AxK9AGNw");
        String url ="http://gykj123.cn:9035/api/v1/styles/gykj/rJg3x0CN_7";
        HttpRequestUtil.setOkHttpClient(BoxHttpClient.getSingleton().getClient());
        //String url ="http://gykj123.cn:8849/grandtech-middleground-vectile-wgs/api/v1/styles/getstyle/gykj/SJlgA8E2SN";
       // String url = "https://api.agribigdata.com.cn/vmap-server-bigdata/api/v1/styles/adminnybx/rJg3x0CN_7";
        String MAP_OFFLINE_STYLE_JSON ="asset://style.json";
        mapboxMap.setStyle(MAP_OFFLINE_STYLE_JSON, new Style.OnStyleLoaded() {
            @Override
            public void onStyleLoaded(@NonNull Style style) {
                Log.i("ddd","fff");
                gMapView.getStyleLayerManager().getStyleLayerGroupsAsync(new IDoneListenerCallBack<LinkedHashMap<String, List<Layer>>>() {
                    @Override
                    public void onDone(LinkedHashMap<String, List<Layer>> result) {
                        Log.i("gmbgl","getStyleLayerGroupsAsync:"+result.toString());
                    }
                });
//                Log.i("gmbgl","uri:"+gMapView.getStyleLayerManager().getSourceUri("dt_gj"));

               // String layerJson = gMapView.getStyleLayerManager().getLayerJson("园地");
                //Log.i("gmbgl","园地:"+layerJson);
                gMapView.setMapEvent(MapActivityDome1.this);
                String cachePath = Environment.getExternalStorageDirectory().getAbsolutePath()+ File.separator+"toolmapframe" + File.separator + "Cache" + File.separator +"frame"+File.separator+ "data.json";
                mGraphicSetting = new GraphicSetting(GeometryType.point, "验标地块", cachePath);
                gMapView.addEditor(mGraphicSetting);

                //如果选择集要操作临时图层gMapView.initSelection需要在 gMapView.setEditor执行之后执行
                List<Layer> layers = gMapView.getMapBoxMap().getStyle().getLayers();
                List<String> selectAbles = new ArrayList<>();
                for (Layer layer : layers) {
                    String id = layer.getId();
                    if(layer instanceof FillLayer || layer instanceof LineLayer|| layer instanceof CircleLayer){
                       /* if(!(id.contains("国") || id.contains("省") || id.contains("市") || id.contains("县") || id.contains("乡") || id.contains("镇") || id.contains("村") || id.contains("影像") || id.contains("_fill"))){
                            selectAbles.add(id);
                        }*/
                        selectAbles.add(id);
                    }
                }
                gMapView.initSelection(new SelectSetting(false,true,false,selectAbles));
                gMapView.addSelectChangeListening(MapActivityDome1.this);
               // gMapView.moveToLocation(34.972398,113.610671,14.2);
                Log.i("dddd","fff");
               // layerIds = initQueryRegionMsg();
               /* try {

                    gMapView.getStyleLayerManager().showSimpleVector("dasdsa","https://mse.agribigdata.com.cn/vmap-server-bigdata/api/v1/tilesets/gykj/dt_zzdk",GeometryType.line);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }*/
                // Map is set up and the style has loaded. Now you can add data or make other map adjustments
            }
        });

    }

    public String[] initQueryRegionMsg(){
        String[] layerIds = new String[5];
        StyleLayerManager styleLayerManager = gMapView.getStyleLayerManager();
        Layer layerS = styleLayerManager.getLayer("省域");
        Layer layerDS = styleLayerManager.getLayer("地市域");
        Layer layerXY = styleLayerManager.getLayer("县域");
        Layer layerXZ = styleLayerManager.getLayer("乡镇域");
        Layer layerCY = styleLayerManager.getLayer("村域");
        layerS = styleLayerManager.layerClone2TransparentFillLayer(layerS);
        layerDS = styleLayerManager.layerClone2TransparentFillLayer(layerDS);
        layerXY = styleLayerManager.layerClone2TransparentFillLayer(layerXY);
        layerXZ = styleLayerManager.layerClone2TransparentFillLayer(layerXZ);
        layerCY = styleLayerManager.layerClone2TransparentFillLayer(layerCY);
        styleLayerManager.addLayer(layerS);
        styleLayerManager.addLayer(layerDS);
        styleLayerManager.addLayer(layerXY);
        styleLayerManager.addLayer(layerXZ);
        styleLayerManager.addLayer(layerCY);
        layerIds[0] = layerS.getId();
        layerIds[1] = layerDS.getId();
        layerIds[2] = layerXY.getId();
        layerIds[3] = layerXZ.getId();
        layerIds[4] = layerCY.getId();
        return layerIds;
    }
    String[] layerIds;
    public List<Feature> queryRegionMsg(PointF coordinates){
        return gMapView.getMapBoxMap().queryRenderedFeatures(coordinates,layerIds);
    }
    @Override
    protected void onStart() {
        super.onStart();
        gMapView.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        gMapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        gMapView.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        gMapView.onStop();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        gMapView.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        gMapView.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        gMapView.onDestroy();
    }


    @Override
    public boolean onMapClick(@NonNull LatLng point) {
        try {
     /*   if(1==1){
            PointF pointF = gMapView.getMapBoxMap().getProjection().toScreenLocation(point);
            gMapView.selectFeatureByPoint(pointF,null);
            return;
        }*/
        PointF pointF = gMapView.getMapBoxMap().getProjection().toScreenLocation(point);
        gMapView.selectFeatureByPoint(pointF,null);

        //point.setLongitude(114.43863942901703);
        //point.setLatitude(25.44886534426287);
        Log.i("TAG", "onMapClick: ssss");
    //  114.43863942901703, 25.44886534426287  http://gykj123.cn:8849/grandtech-middleground-maptile/api/v1/wms/jbnt_pic/?lot=114.4409&lat=25.44742&z=15.31&width=575&height=575&ratio=2

       /* PointF pointF = gMapView.getMapBoxMap().getProjection().toScreenLocation(point);
        gMapView.selectFeatureByPoint(pointF,null);*/
      //  PointF pointF = gMapView.getMapBoxMap().getProjection().toScreenLocation(point);
      //  Double ss = Transformation.wktTransfWidthHeightPx("POINT("+point.getLongitude()+" "+point.getLatitude()+")",gMapView.getMapBoxMap().getCameraPosition().zoom,500);
       // String url = "http://gykj123.cn:8849/grandtech-middleground-maptile/api/v1/wms/jbnt_pic/?lot=114.4352&lat=25.44723&z="+gMapView.getMapBoxMap().getCameraPosition().zoom+"&width="+ss+"&height="+ss+"&ratio=2";
     /*   try {
            com.vividsolutions.jts.geom.Geometry geometry = Transformation.wkt2JtsGeometry("POINT("+point.getLongitude()+" "+point.getLatitude()+")");
            geometry = Transformation.buffer(geometry,500);
            Envelope envelope = geometry.getEnvelopeInternal();
            PointF pointmin = gMapView.getMapBoxMap().getProjection().toScreenLocation(new LatLng(envelope.getMinY(),envelope.getMinX()));
          //  PointF pointmax = gMapView.getMapBoxMap().getProjection().toScreenLocation(new LatLng(envelope.getMaxY(),envelope.getMaxX()));
            com.mapbox.geojson.Polygon polygon = TurfTransformation.circle(Point.fromLngLat(point.getLongitude(),point.getLatitude()),500,TurfConstants.UNIT_METRES);
            double[] doubles = TurfMeasurement.bbox(polygon);
            double metersPerPixel = gMapView.getMapBoxMap().getProjection().getMetersPerPixelAtLatitude(point.getLatitude());
            double  ss = 500/metersPerPixel*2;
            BoundingBox boundingBox =  BoundingBox.fromLngLats(doubles[0],doubles[1],doubles[2],doubles[3]);
            String url = " http://gykj123.cn:8849/grandtech-middleground-maptile/api/v1/wms/zjd_pic/?lot="+point.getLongitude()+"&lat="+point.getLatitude()+"&z="+(gMapView.getMapBoxMap().getCameraPosition().zoom)+"&width="+ss+"&height="+ss+"&ratio=2";
            gMapView.getStyleLayerManager().showSimpleImage("imageSHowSource",boundingBox,url);
            GeoJsonSource geoJsonSource = gMapView.getStyleLayerManager().createSimpleGeoJsonSource(System.currentTimeMillis()+"");
           // GeoJsonSource geoJsonSource2 = gMapView.getStyleLayerManager().createSimpleGeoJsonSource(System.currentTimeMillis()+"11");
            FillLayer layer = new FillLayer(geoJsonSource.getId()+"layer",geoJsonSource.getId())
                    .withProperties(PropertyFactory.fillColor(Color.parseColor("#94FDD6")),
                            PropertyFactory.fillOpacity(0.6f),
                            PropertyFactory.fillOutlineColor(Color.parseColor("#94FDD6"))
                            );

          *//*  FillLayer layer2 = new FillLayer(geoJsonSource2.getId()+"layer",geoJsonSource2.getId())
                    .withProperties(PropertyFactory.fillColor(Color.RED),
                            PropertyFactory.fillOpacity(0.6f),
                            PropertyFactory.fillOutlineColor(Color.parseColor("#94FDD6"))
                    );*//*
           // Feature feature = Feature.fromGeometry((Geometry) Transformation.jstGeometry2GeoJson(geometry ));
           // feature.addStringProperty("po","POINT("+point.getLongitude()+" "+point.getLatitude()+")");
            geoJsonSource.setGeoJson(TurfMeasurement.bboxPolygon(doubles));
          //  geoJsonSource2.setGeoJson(feature);
            SymbolLayer symbolLayer =  new SymbolLayer(geoJsonSource.getId()+"SymbolLayer", geoJsonSource.getId()).withProperties(
                    PropertyFactory.textField(Expression.toString(has("po"))),
                    textSize(12f),
                    textColor(Color.BLACK),
                    textIgnorePlacement(true),
                    // The .5f offset moves the data numbers down a little bit so that they're
                    // in the middle of the triangle cluster image
                    textOffset(new Float[] {0f, .5f}),textAllowOverlap(true));
            gMapView.getStyleLayerManager().addSource(geoJsonSource);*/
           // gMapView.getStyleLayerManager().addSource(geoJsonSource2);
           // gMapView.getStyleLayerManager().addLayer(symbolLayer);
            //gMapView.getStyleLayerManager().addLayer(layer);
            //gMapView.getStyleLayerManager().addLayer(layer2);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean onMapLongClick(@NonNull LatLng point) {
        PointF pointF = gMapView.getMapBoxMap().getProjection().toScreenLocation(point);
        //记录点命中的要素
        Map<String, FeatureSet> pointFeatures = new HashMap<>();
        SelectionSet.SelectStatus selectStatus =  gMapView.selectFeatureByPoint(pointF, pointFeatures);
        if (pointFeatures.size() == 0) return false; ;
          //  VibratorUtil.Vibrate(context, 100);
        if (selectStatus == SelectionSet.SelectStatus.cancelSelected) {
            //激活编辑节点工具
            if (pointFeatures.size() != 1) {
                Toast.makeText(this, "只能编辑一个图形！", Toast.LENGTH_LONG).show();
                return false;
            }
            editFeature(pointFeatures);
        }
        if (selectStatus == SelectionSet.SelectStatus.addSelected) {
            //直接删除临时图层直接这个
            //gMapView.getGc().deletes(FeatureSet.convert2Features(pointFeatures));
            //如果想带回调 走下面这个
            gMapView.getBaseEditor(mGraphicSetting).delFeatures("deleteTool",this,pointFeatures);
            gMapView.clearSelectSetAndRender();//清空选择集
        }
        return false;
    }

    private void editFeature(Map<String, FeatureSet> set){
        if (set == null) return;
        Feature feature = null;
        Map<String, FeatureSet> featuresMap = set;
        for (String _layerName : set.keySet()) {
            String editLayerName = _layerName;
            feature = FeatureSet.convert2Features(set).get(0);
        }
        if (feature == null) return;
        gMapView.startSketch(GeometryType.polygon);
        gMapView.clearSelectSetAndRender();
        hz.setText("结束");
        Geometry geometry = feature.geometry();
        SketchSetting setting = null;
        if (geometry instanceof Point)
            setting = new SketchSetting("test", GeometryType.point);
        if (geometry instanceof MultiPoint)
            setting = new SketchSetting("test", GeometryType.multiPoint);
        if (geometry instanceof LineString)
            setting = new SketchSetting("test", GeometryType.line);
        if (geometry instanceof MultiLineString)
            setting = new SketchSetting("test", GeometryType.multiLine);
        if (geometry instanceof com.mapbox.geojson.Polygon)
            setting = new SketchSetting("test", GeometryType.polygon);
        if (geometry instanceof MultiPolygon)
            setting = new SketchSetting("test", GeometryType.multiPolygon);
        if (setting == null) return;
        gMapView.addSketchGeometry(geometry);
    }

    @Override
    public void onCameraIdle() {

    }

    @Override
    public void onCameraMoveCanceled() {

    }

    @Override
    public void onCameraMove() {
      //  LatLng latLng = gMapView.getMapBoxMap().getCameraPosition().target;

      //  List<Feature> features = queryRegionMsg( gMapView.getMapBoxMap().getProjection().toScreenLocation(latLng));
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



    @Override
    public boolean beforeSelectSetChange() {
        return true;
    }

    @Override
    public boolean selectSetChangeIng(Feature feature, SelectionSet.SelectStatus selectStatus) {
        return true;
    }


    @Override
    public void selectSetChanged() {
        Toast.makeText(this, "selectSetChanged！", Toast.LENGTH_LONG).show();
    }


    @Override
    public boolean doHandle(Object o, String... flags) {
        return false;
    }

    @Override
    public void afterHandle(Object o, String... flags) {
      //  return true;
    }

    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {
        Toast.makeText(this, "R.string.user_location_permission_explanation", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onPermissionResult(boolean granted) {
        if (granted) {
            gMapView.getMapBoxMap().getStyle(new Style.OnStyleLoaded() {
                @Override
                public void onStyleLoaded(@NonNull Style style) {

                }
            });
        } else {
            Toast.makeText(this, "R.string.user_location_permission_explanation", Toast.LENGTH_LONG).show();
            finish();
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}