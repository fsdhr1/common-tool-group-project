package com.gykj.commontool.maptest;

import android.graphics.PointF;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.blankj.utilcode.util.LogUtils;
import com.grandtech.mapframe.core.GMap;
import com.grandtech.mapframe.core.editor.IToolInterceptor;
import com.grandtech.mapframe.core.editor.gc.GraphicSetting;
import com.grandtech.mapframe.core.enumeration.GeometryType;
import com.grandtech.mapframe.core.event.IMapEvent;
import com.grandtech.mapframe.core.maps.GMapView;
import com.grandtech.mapframe.core.maps.IOnMapReady;
import com.grandtech.mapframe.core.net.BoxHttpClient;
import com.grandtech.mapframe.core.select.SelectSetting;
import com.grandtech.mapframe.core.select.SelectionSet;
import com.grandtech.mapframe.core.sketch.SketchSetting;
import com.grandtech.mapframe.core.util.FeatureSet;
import com.gykj.commontool.R;
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
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.layers.CircleLayer;
import com.mapbox.mapboxsdk.style.layers.FillLayer;
import com.mapbox.mapboxsdk.style.layers.Layer;
import com.mapbox.mapboxsdk.style.layers.LineLayer;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @ClassName MapActivityEditorDemo
 * @Description TODO
 * @Author: fs
 * @Date: 2021/9/17 9:01
 * @Version 2.0
 */
public class MapActivityEditorDemo extends AppCompatActivity implements View.OnClickListener, IOnMapReady, IMapEvent, IToolInterceptor {

    private GMapView mGMapView;

    private Button btSelectLine,btSelectFill,btSketchPoint,btSketchLine,btSketchFill,btSketchScrawl,btnSave,btnSplit,btnUni,btnDig,btnDelete,btnClearSketch;

    private GraphicSetting graphicSettingPoint,graphicSettingLine,graphicSettingFill,graphicSettingScrawl,graphicSetting;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //在项目中使用的时候这句话要放到applacation中初始化
        GMap.getInstance(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_editor);
        initView(savedInstanceState);
    }

    private void initView(Bundle savedInstanceState) {
        mGMapView = findViewById(R.id.mapView);
        mGMapView.onCreate(savedInstanceState);
        mGMapView.init(this);
        btSelectLine = findViewById(R.id.btn_selectLine);
        btSelectFill = findViewById(R.id.btn_selectFill);
        btSketchPoint = findViewById(R.id.btn_sketchPoint);
        btSketchLine = findViewById(R.id.btn_sketchLine);
        btSketchFill = findViewById(R.id.btn_sketchFill);
        btSketchScrawl = findViewById(R.id.btn_sketchScrawl);
        btnSave = findViewById(R.id.btn_save);
        btnSplit = findViewById(R.id.btn_split);
        btnUni = findViewById(R.id.btn_uni);
        btnDig = findViewById(R.id.btn_dig);
        btnDelete = findViewById(R.id.btn_delete);
        btnClearSketch = findViewById(R.id.btn_clearSketch);

    }

    @Override
    public void onBoxMapReady(MapboxMap mapboxMap) {
       // BoxHttpClient.getSingleton().setToken("eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiLogL_kvbPolYrkuKrkuroiLCJjb250ZXh0VXNlcklkIjoicDlGQjJDQTM4RDREOTQ0MjA4QjAwNDI2MkMwMEM2Rjc4IiwiY29udGV4dE5hbWUiOiJnZW8iLCJjb250ZXh0RGVwdElkIjoiMSIsImNvbnRleHRBcHBsaWNhdGlvbklkIjoiMSIsImV4cCI6MjYxMzIxODk2NDd9.dM92KheaX7Yp7GsHB_iFubyzV8KClL9mFKDy0a1AwXM");
        String url ="http://222.143.33.110:10092/mapeditor/styles/QYFBN8Qtr";
        mapboxMap.setStyle(url, new Style.OnStyleLoaded() {
            @Override
            public void onStyleLoaded(@NonNull Style style) {
                initEditor();
                initEvent();
               // mGMapView.moveToLocation(34.05032451837759, 113.75082760425255 ,15.5);
            }
        });
    }

    private void initEvent() {
        btSelectLine.setOnClickListener(this);
        btSelectFill.setOnClickListener(this);
        btSketchPoint.setOnClickListener(this);
        btSketchLine.setOnClickListener(this);
        btSketchFill.setOnClickListener(this);
        btSketchScrawl.setOnClickListener(this);
        btnSave.setOnClickListener(this);
        btnSplit.setOnClickListener(this);
        btnUni.setOnClickListener(this);
        btnDig.setOnClickListener(this);
        btnDelete.setOnClickListener(this);
        btnClearSketch.setOnClickListener(this);
        mGMapView.setMapEvent(this);
    }

    /**
     * 初始化临时图层，点线面涂鸦
     */
    private void initEditor() {
        String cachePath = Environment.getExternalStorageDirectory().getAbsolutePath()+ File.separator+"toolmapframe" + File.separator + "Cache" + File.separator +"frame";
        String settingPointPath = cachePath+File.separator+ "point.json";
        graphicSettingPoint= new GraphicSetting(GeometryType.point,"point",settingPointPath);
        mGMapView.addEditor(graphicSettingPoint);
        String settingLinePath = cachePath+File.separator+ "line.json";
        graphicSettingLine= new GraphicSetting(GeometryType.line,"line",settingLinePath);
        mGMapView.addEditor(graphicSettingLine);
        String settingFillPath = cachePath+File.separator+ "fill.json";
        graphicSettingFill= new GraphicSetting(GeometryType.polygon,"fill",settingFillPath);
        mGMapView.addEditor(graphicSettingFill);
        String settingScrawlPath = cachePath+File.separator+ "Scrawl.json";
        graphicSettingScrawl= new GraphicSetting(GeometryType.line,"line",settingScrawlPath);
        mGMapView.addEditor(graphicSettingScrawl);
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == btSelectLine.getId()){
            List<Layer> layers = mGMapView.getMapBoxMap().getStyle().getLayers();
            List<String> selectAbles = new ArrayList<>();
            for (Layer layer : layers) {
                String id = layer.getId();
                if(layer instanceof LineLayer ){
                        if(!(id.contains("国") || id.contains("省") || id.contains("市") || id.contains("县") || id.contains("乡") || id.contains("镇") || id.contains("村") || id.contains("影像") )){
                            selectAbles.add(id);
                        }
                }
            }
            String layerId =
                    mGMapView.getBaseEditor(graphicSettingLine)
                            .getGraphicContainer().getQueryLayerId();
            LinkedHashMap customIds = new LinkedHashMap();
            customIds.put("种植地块", "id");
            mGMapView.initSelection(customIds,new SelectSetting(true,true,true,"省域"));
            graphicSetting = graphicSettingLine;
        }

        if(v.getId() == btSelectFill.getId()){
            List<Layer> layers = mGMapView.getMapBoxMap().getStyle().getLayers();
            List<String> selectAbles = new ArrayList<>();
            for (Layer layer : layers) {
                String id = layer.getId();
                Log.i("MapActivityEditorDemo",id);
                if(layer instanceof FillLayer ){
                    if(!(id.contains("国") || id.contains("省") || id.contains("市") || id.contains("县") || id.contains("乡") || id.contains("镇") || id.contains("村") || id.contains("影像") )){
                        selectAbles.add(id);
                    }
                }
            }
            mGMapView.initSelection(new SelectSetting(true,true,true,selectAbles));
            graphicSetting = graphicSettingFill;
        }

        if(v.getId() == btSketchPoint.getId()){
            graphicSetting= graphicSettingPoint;
        }

        if(v.getId() == btSketchLine.getId()){
            if(mGMapView.isStartSketch()){
                mGMapView.stopSketch();
            }else {
                mGMapView.startSketch(GeometryType.line);
            }
            graphicSetting= graphicSettingLine;
        }

        if(v.getId() == btSketchFill.getId()){
           if(mGMapView.isStartSketch()){
               mGMapView.stopSketch();
           }else {
               mGMapView.startSketch(GeometryType.polygon);
           }
            graphicSetting= graphicSettingFill;
        }
        if(v.getId() == btSketchScrawl.getId()){
            if(mGMapView.isStartSketch()){
                mGMapView.stopSketch();
            }else {
                mGMapView.startSketch(GeometryType.slideLine);
            }
            graphicSetting= graphicSettingScrawl;
        }
        if(v.getId() == btnSave.getId()){
            mGMapView.getBaseEditor(graphicSetting).drawFeature(btSketchFill.getTransitionName(),false, this,mGMapView.getSketchGeometry());
            mGMapView.clearSelectSetAndRender();
            mGMapView.stopSketch();
        }
        if(v.getId() == btnSplit.getId()){
            mGMapView.getBaseEditor(graphicSettingFill).splitFeatures("分割",false,this,mGMapView.getSelectSet(),mGMapView.getSketchGeometry());

        }
        if(v.getId() == btnUni.getId()){
            mGMapView.getBaseEditor(graphicSettingFill).unionFeatures(this,"合并",false,this,mGMapView.getSelectSet());

        }
        if(v.getId() == btnDig.getId()){
            mGMapView.getBaseEditor(graphicSettingFill).digFeatures("扣除",false,this,mGMapView.getSelectSet(),mGMapView.getSketchGeometry());
        }
        if(v.getId() == btnDelete.getId()){
            mGMapView.getBaseEditor(graphicSetting).delFeatures("删除",this,mGMapView.getSelectSet());
        }
        if(v.getId() == btnClearSketch.getId()){
            mGMapView.clearSketch();
        }
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
    }

    @Override
    public boolean onMapClick(@NonNull LatLng point) {
        if(graphicSetting!=null&&graphicSetting.equals(graphicSettingPoint)){
            mGMapView.getBaseEditor(graphicSetting).drawFeature(btSketchFill.getTransitionName(),false, this, Point.fromLngLat(point.getLongitude(),point.getLatitude()));
        }else {
            PointF pointF = mGMapView.getMapBoxMap().getProjection().toScreenLocation(point);
            mGMapView.selectFeatureByPoint(pointF,null);
        }

        return false;
    }

    @Override
    public boolean onMapLongClick(@NonNull LatLng point) {
        PointF pointF = mGMapView.getMapBoxMap().getProjection().toScreenLocation(point);
        //记录点命中的要素
        Map<String, FeatureSet> pointFeatures = new HashMap<>();
        SelectionSet.SelectStatus selectStatus =  mGMapView.selectFeatureByPoint(pointF, pointFeatures);
        if (pointFeatures.size() == 0) {
            return false;
        }
        ;
        //  VibratorUtil.Vibrate(context, 100);
        if (selectStatus == SelectionSet.SelectStatus.cancelSelected) {
            //激活编辑节点工具
            if (pointFeatures.size() != 1) {
                Toast.makeText(this, "只能编辑一个图形！", Toast.LENGTH_LONG).show();
                return false;
            }
            editFeature(pointFeatures);
        }
        return false;
    }
    private void editFeature(Map<String, FeatureSet> set){
        if (set == null) {
            return;
        }
        Feature feature = null;
        Map<String, FeatureSet> featuresMap = set;
        for (String _layerName : set.keySet()) {
            String editLayerName = _layerName;
            feature = FeatureSet.convert2Features(set).get(0);
        }
        if (feature == null) {
            return;
        }
        mGMapView.startSketch(GeometryType.polygon);
        mGMapView.clearSelectSetAndRender();
        Geometry geometry = feature.geometry();
        SketchSetting setting = null;
        if (geometry instanceof Point) {
            setting = new SketchSetting("test", GeometryType.point);
        }
        if (geometry instanceof MultiPoint) {
            setting = new SketchSetting("test", GeometryType.multiPoint);
        }
        if (geometry instanceof LineString) {
            setting = new SketchSetting("test", GeometryType.line);
        }
        if (geometry instanceof MultiLineString) {
            setting = new SketchSetting("test", GeometryType.multiLine);
        }
        if (geometry instanceof com.mapbox.geojson.Polygon) {
            setting = new SketchSetting("test", GeometryType.polygon);
        }
        if (geometry instanceof MultiPolygon) {
            setting = new SketchSetting("test", GeometryType.multiPolygon);
        }
        if (setting == null) {
            return;
        }
        mGMapView.addSketchGeometry(geometry);
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
    public boolean doHandle(Object o, String... flags) {
        LogUtils.i("doHandle"+flags[0]);
        return false;
    }

    @Override
    public void afterHandle(Object o, String... flags) {
        LogUtils.i("afterHandle"+flags[0]);
        mGMapView.clearSelectSetAndRender();
        mGMapView.stopSketch();
    }
}
