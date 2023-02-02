package com.gykj.commontool.maptest;

import android.graphics.Color;
import android.graphics.PointF;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.blankj.utilcode.util.LogUtils;
import com.google.gson.JsonArray;
import com.grandtech.mapframe.core.GMap;
import com.grandtech.mapframe.core.editor.IToolInterceptor;
import com.grandtech.mapframe.core.editor.gc.GraphicSetting;
import com.grandtech.mapframe.core.enumeration.GeometryType;
import com.grandtech.mapframe.core.event.IMapEvent;
import com.grandtech.mapframe.core.layer.StyleLayerManager;
import com.grandtech.mapframe.core.maps.GMapView;
import com.grandtech.mapframe.core.maps.IOnMapReady;
import com.grandtech.mapframe.core.net.BoxHttpClient;
import com.grandtech.mapframe.core.select.SelectSetting;
import com.grandtech.mapframe.core.select.SelectionSet;
import com.grandtech.mapframe.core.sketch.SketchSetting;
import com.grandtech.mapframe.core.util.FeatureSet;
import com.grandtech.mapframe.ui.manager.ToolManager;
import com.grandtech.mapframe.ui.manager.WidgetGravityEnum;
import com.grandtech.mapframe.ui.view.ToolGroupLayout;
import com.grandtech.mapframe.ui.view.bottom.SlidingUpPanelLayout;
import com.gykj.commontool.R;
import com.gykj.location.LocationClient;
import com.gykj.location.LocationType;
import com.gykj.location.ServiceLocation;
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
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.MapboxMapOptions;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.expressions.Expression;
import com.mapbox.mapboxsdk.style.layers.CircleLayer;
import com.mapbox.mapboxsdk.style.layers.FillLayer;
import com.mapbox.mapboxsdk.style.layers.Layer;
import com.mapbox.mapboxsdk.style.layers.LineLayer;
import com.mapbox.mapboxsdk.style.layers.PropertyFactory;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
/**
 * @ClassName MapActivityDome2
 * @Description TODO mapcUI功能基本操作,布局模板参考验标模式左滑抽屉+工具组+底部滑动
 * @Author: fs
 * @Date: 2021/5/18 10:15
 * @Version 2.0
 */
@SuppressWarnings("ALL")
public class MapActivityDemo2 extends AppCompatActivity implements IOnMapReady , IMapEvent, SelectionSet.ISelectSetChange, IToolInterceptor {
    public final static String TAG  = "com.grandtech.mapframe.ui.MapActivityDemo2";


    private GMapView gMapView;

    private FrameLayout flMainFrame ;

    private ToolManager mToolManager;

    private LinearLayout mapRegion;

    private SlidingUpPanelLayout mapSlidingUpPanelLayout;

    private TextView tv_layerfiltertest;
    private GraphicSetting mGraphicSetting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //在项目中使用的时候这句话要放到applacation中初始化
        GMap.getInstance(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_ui1);
        MapboxMapOptions mapOptions = new MapboxMapOptions();
        mapOptions.setPrefetchesTiles(true);
        mapOptions.compassEnabled(true);
        mapOptions.rotateGesturesEnabled(false);
        mapOptions.tiltGesturesEnabled(false);
        mapOptions.logoEnabled(false);
        mapOptions.attributionEnabled(false);
        mapOptions.maxZoomPreference(16.99);
        flMainFrame = findViewById(R.id.flMainFrame);
        mapRegion = findViewById(R.id.mapVisibleRegion);
        tv_layerfiltertest = findViewById(R.id.tv_layerfiltertest);
        mapSlidingUpPanelLayout = findViewById(R.id.mapSlidingUpPanelLayout);
        gMapView = new GMapView(this,mapOptions,this);
        gMapView.onCreate(savedInstanceState);
        mapRegion.addView(gMapView);
        /**
         * 公交车 注册
         */
        EventBus.getDefault().register(this);
        initViewEvent();
    }

    /**
     * 注册事件
     */
    protected void initViewEvent(){
        mapSlidingUpPanelLayout.addPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {

            }

            @Override
            public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {
                if (newState == SlidingUpPanelLayout.PanelState.COLLAPSED) {
                    mapSlidingUpPanelLayout.getDragView().setAlpha(0);
                }
                if (newState == SlidingUpPanelLayout.PanelState.DRAGGING) {
                    mapSlidingUpPanelLayout.getDragView().setAlpha(1);
                }
            }
        });
        tv_layerfiltertest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Expression expression = gMapView.getStyleLayerManager().getLayerFilter("验标地块");


                JsonArray jsonElements = new JsonArray();
                jsonElements.add("==");
                jsonElements.add("dkbm");
                jsonElements.add(3.0);
                Expression exp = Expression.Converter.convert(jsonElements);
                gMapView.getStyleLayerManager().addorReplace2filterByLayer("验标地块",exp);
                //gMapView.getStyleLayerManager().replaceExpression()

                Expression expression1 = gMapView.getStyleLayerManager().getLayerFilter("验标地块"); ;
            }
        });
    }

    @Override
    public void onBoxMapReady(MapboxMap mapboxMap) {
        BoxHttpClient.getSingleton().setToken("eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiLogL_kvbPolYrkuKrkuroiLCJjb250ZXh0VXNlcklkIjoicDlGQjJDQTM4RDREOTQ0MjA4QjAwNDI2MkMwMEM2Rjc4IiwiY29udGV4dE5hbWUiOiJnZW8iLCJjb250ZXh0RGVwdElkIjoiMSIsImNvbnRleHRBcHBsaWNhdGlvbklkIjoiMSIsImV4cCI6MjYxMzIxODk2NDd9.dM92KheaX7Yp7GsHB_iFubyzV8KClL9mFKDy0a1AwXM");
        String url ="https://api.agribigdata.com.cn/vmap-server-bigdata/api/v1/styles/adminnybx/rJg3x0CN_7";
        mapboxMap.setStyle(url, new Style.OnStyleLoaded() {
            @Override
            public void onStyleLoaded(@NonNull Style style) {
                //gMapView.getMapBoxMap().getStyle().getLayer("验标地块").setProperties(PropertyFactory.visibility(Property.VISIBLE));
                gMapView.getStyleLayerManager().getStyleLayerGroups();
                gMapView.setMapEvent(MapActivityDemo2.this);
                String cachePath = Environment.getExternalStorageDirectory().getAbsolutePath()+ File.separator+"toolmapframe" + File.separator + "Cache" + File.separator +"frame"+File.separator+ "data.json";
                mGraphicSetting = new GraphicSetting(GeometryType.point, "验标地块", cachePath);
                gMapView.addEditor(mGraphicSetting);
                //如果选择集要操作临时图层gMapView.initSelection需要在 gMapView.setEditor执行之后执行
                gMapView.initSelection(new SelectSetting(true,true,gMapView.getGcQueryLayerId(mGraphicSetting), "验标地块","参考地块", "大棚地块","种植地块", "基础地块"));
                gMapView.addSelectChangeListening(MapActivityDemo2.this);
                mToolManager = ToolManager.newInstance().init(MapActivityDemo2.this,gMapView,false);
               // mToolManager.setOnToolClickListener(new DefaultToolClickListener(mToolManager,gMapView));
                View v = View.inflate(MapActivityDemo2.this, R.layout.activity_map_selectset_res, null);
                mToolManager.addToolBarView(v, false,WidgetGravityEnum.TOP_CENTER);
                LocationClient.getInstance().startLocation(MapActivityDemo2.this,LocationType.COMMON,null);
               /* mToolManager.setOnToolClickListener(new ToolManager.OnToolClickListener() {
                    @Override
                    public void activateClick(ToolView toolView) {
                        Log.i("activateClick",toolView.getmToolName());
                        if(ToolNameEnum.NAV.getName().equals(toolView.getmToolName())){
                            mToolManager.toolBarNavigate(toolView);
                        }
                        if(ToolNameEnum.LOC.getName().equals(toolView.getmToolName())){
                            gMapView.moveToCurrentLocation(16.0);
                        }
                    }

                    @Override
                    public void deactivateClick(ToolView toolView) {
                        Log.i("deactivateClick",toolView.getmToolName());
                        if(ToolNameEnum.NAV.getName().equals(toolView.getmToolName())) {
                            mToolManager.toolBarNavigate(toolView);
                        }
                    }
                });*/
//               mToolManager.setOnToolClickListener(new DefaultToolClickListener(mToolManager,gMapView));
            }
        });

    }
    /**
     * 接收定位服务信息
     *
     * @param serviceLocation
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(ServiceLocation serviceLocation) {
       gMapView.setLocation(serviceLocation.getLatitude(),serviceLocation.getLongitude());
        //gMapView.setLocation(35.925176,115.096676);
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
        EventBus.getDefault().unregister(this);
        System.out.println("注销公交车");
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
        // 停止定位服务
        LocationClient.getInstance().stopLocation(this, LocationType.COMMON);
    }


    @Override
    public boolean onMapClick(@NonNull LatLng point) {
        Log.i("TAG", "onMapClick: ssss");

      /*  List<MarkerSetting> markerSettings = new ArrayList<>();
        MarkerSetting markerSetting = new MarkerSetting();
        markerSetting.setLatLng(point);
        markerSetting.setMarkerId("1111");
        markerSetting.setSnippet("cccc");
        markerSetting.setTitle(point.toString());
        markerSettings.add(markerSetting);
        markerSetting = new MarkerSetting();
        markerSetting.setLatLng(point);
        markerSetting.setMarkerId("222");
        markerSetting.setSnippet("cccc");
        markerSetting.setTitle(point.toString());
        markerSettings.add(markerSetting);
        gMapView.getMarkerManager().addMarkers("dadsadas",markerSettings);
        PointF pointF = gMapView.getMapBoxMap().getProjection().toScreenLocation(point);
        gMapView.selectFeatureByPoint(pointF,null);*/

        addGraphic(Point.fromLngLat(point.getLongitude(),point.getLatitude()),"dadas");
        return false;
    }

    private void addGraphic(Geometry geometry,String sourceId){
        GeoJsonSource geoJsonSource = null;
        String layerId = sourceId+"_Graphiclayer";
        StyleLayerManager styleLayerManager = gMapView.getStyleLayerManager();
        Layer layer = null;
        if(gMapView.getStyleLayerManager().hasSource(sourceId)){
            geoJsonSource = (GeoJsonSource)styleLayerManager.getSource(sourceId);
            layer = styleLayerManager.getLayer(layerId);
        }else {
            geoJsonSource = gMapView.getStyleLayerManager().createSimpleGeoJsonSource(sourceId);
            if(geometry instanceof Point){
                layer = new CircleLayer(layerId ,sourceId)
                        .withProperties(
                                PropertyFactory.circleColor("#94FDD6"),
                                PropertyFactory.circleRadius(5f),
                                PropertyFactory.circleOpacity(1f));
            }
            if(geometry instanceof com.mapbox.geojson.Polygon){
                layer = new FillLayer(layerId, sourceId)
                        .withSourceLayer(sourceId) .withProperties(PropertyFactory.lineColor(Color.parseColor("#94FDD6")),
                                PropertyFactory.lineWidth(3f),
                                PropertyFactory.lineOpacity(1f));
            }
            if(geometry instanceof LineString){
                layer = new LineLayer(layerId,sourceId)
                        .withProperties(PropertyFactory.lineColor(Color.parseColor("#94FDD6")),
                                PropertyFactory.lineWidth(3f),
                                PropertyFactory.lineOpacity(1f));
            }
            if(geoJsonSource!= null && layer!=null){
                styleLayerManager.addSource(geoJsonSource);
                styleLayerManager.addLayer(layer);
            }
        }
        geoJsonSource.setGeoJson(geometry);
    }

    @Override
    public boolean onMapLongClick(@NonNull LatLng point) {
        ToolGroupLayout toolGroupLayout = mToolManager.getToolGroupViewByName("默认工具");
        if(toolGroupLayout != null){
            Toast.makeText(this, toolGroupLayout.getToolGroupName(), Toast.LENGTH_LONG).show();
        }

        PointF pointF = gMapView.getMapBoxMap().getProjection().toScreenLocation(point);
        //记录点命中的要素
        Map<String, FeatureSet> pointFeatures = new HashMap<>();
        SelectionSet.SelectStatus selectStatus =  gMapView.selectFeatureByPoint(pointF, pointFeatures);
        if (pointFeatures.size() == 0) return false;
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

    }


    @Override
    public boolean doHandle(Object o, String... flags) {
        LogUtils.i("doHandle"+flags[0]);
        return false;
    }

    @Override
    public void afterHandle(Object o, String... flags) {
        LogUtils.i("afterHandle"+flags[0]);
    }
}