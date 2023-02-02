package com.grandtech.mapframe.core.maps;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;

import com.grandtech.mapframe.core.editor.BaseEditor;
import com.grandtech.mapframe.core.editor.gc.GraphicContainerImpl;
import com.grandtech.mapframe.core.editor.gc.GraphicSetting;
import com.grandtech.mapframe.core.editor.gc.IGraphicContainer;
import com.grandtech.mapframe.core.enumeration.GeometryType;
import com.grandtech.mapframe.core.event.IMapEvent;
import com.grandtech.mapframe.core.event.MapEventDispatch;
import com.grandtech.mapframe.core.layer.StyleLayerManager;
import com.grandtech.mapframe.core.marker.LocView;
import com.grandtech.mapframe.core.marker.MarkerManager;
import com.grandtech.mapframe.core.marker.MarkerViewManager;
import com.grandtech.mapframe.core.net.BoxHttpClient;
import com.grandtech.mapframe.core.offline.OfflineDataBaseHandler;
import com.grandtech.mapframe.core.rules.Rules;
import com.grandtech.mapframe.core.select.SelectSetting;
import com.grandtech.mapframe.core.select.SelectionSet;
import com.grandtech.mapframe.core.sketch.SketchAnnotations;
import com.grandtech.mapframe.core.sketch.SketchSlideAnnotations;
import com.grandtech.mapframe.core.util.FeatureSet;
import com.grandtech.mapframe.core.util.Transformation;
import com.mapbox.geojson.Geometry;
import com.mapbox.mapboxsdk.camera.CameraUpdate;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.MapboxMapOptions;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.UiSettings;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;


/**
 * @ClassName GMapView
 * @Description TODO  1
 * @Author: fs
 * @Date: 2021/4/20 10:08
 * @Version 2.0
 */
@SuppressLint("ViewConstructor")
public final class GMapView extends MapView implements OnMapReadyCallback, Rules {

    /**
     * iOnMapReady
     */
    private IOnMapReady mIOnMapReady;

    private MapEventDispatch mMapEventDispatch;

    /**
     * 地图盒子
     */
    private MapboxMap mMapBoxMap;

    private SketchAnnotations mSketchAnnotations;

    private SketchSlideAnnotations mSketchSlideAnnotations;

    private SelectionSet mSelectionSet;

    /**
     * 启动绘制工具之前的地图事件监听者，启动绘制功能后地图工具会占用地图事件，结束后还原回去
     */
    private IMapEvent mBeforeSketchEvent;

    /**
     * 临时数据增删改查，扣除合并等
     */
    private Map<GraphicSetting,BaseEditor> mBaseEditors = new HashMap<>();
    /**
     * 定位组件
     */
    private LocView mLocView;

    private StyleLayerManager mStyleLayerManager;

    /**
     * marker管理类实例
     */
    private MarkerManager mMarkerManager;

    private MarkerViewManager mMarkerViewManager;
    /**
     * 离线瓦片库操作类实例
     */
    private OfflineDataBaseHandler mOfflineDataBaseHandler;


    @UiThread
    public GMapView(@NonNull Context context, @Nullable IOnMapReady iOnMapReady) {
        super(context);
        init(iOnMapReady);
    }

    @UiThread
    public GMapView(@NonNull Context context, @Nullable MapboxMapOptions options, @Nullable IOnMapReady iOnMapReady) {
        super(context, options);
        init(iOnMapReady);

    }

    @UiThread
    public GMapView(@NonNull Context context) {
        super(context);
    }

    @UiThread
    public GMapView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    @UiThread
    public GMapView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @UiThread
    public GMapView(@NonNull Context context, @Nullable MapboxMapOptions options) {
        super(context, options);
    }

    /**
     * 地图初始化设置
     *
     * @param iOnMapReady
     */
    public GMapView init(@Nullable IOnMapReady iOnMapReady) {
        this.mIOnMapReady = iOnMapReady;
        mMapEventDispatch = new MapEventDispatch();
        this.getMapAsync(this);
        return this;
    }

    @Override
    public void onMapReady(@NonNull MapboxMap mapboxMap) {
        mapboxMap.setMaxZoomPreference(16.99);
        mapboxMap.setMinZoomPreference(1);
        UiSettings uiSettings = mapboxMap.getUiSettings();
        uiSettings.setCompassEnabled(true);
        uiSettings.setRotateGesturesEnabled(false);
        uiSettings.setTiltGesturesEnabled(false);
        uiSettings.setLogoEnabled(false);
        uiSettings.setAttributionEnabled(false);
        mIOnMapReady.onBoxMapReady(mapboxMap);
        mMapBoxMap = mapboxMap;
        setOnTouchListener(mMapEventDispatch);

    }

    /**
     * 获取MapBoxMap
     *
     * @return mMapBoxMap
     */
    public MapboxMap getMapBoxMap() {
        return mMapBoxMap;
    }


    @Override
    public void onDestroy() {
        mSketchAnnotations = null;
        mSelectionSet = null;
        mSketchSlideAnnotations = null;
        mMarkerManager = null;
        if (mMarkerViewManager != null) {
            mMarkerViewManager.onDestroy();
        }
        mMarkerViewManager = null;
        if(mOfflineDataBaseHandler != null){
            mOfflineDataBaseHandler.onDestroy();
        }
        if(BoxHttpClient.isInit()){
            BoxHttpClient.getSingleton().clearInterceptor();
        }
        super.onDestroy();
    }


    /**
     * @param iOnMapReady TODO
     * @Description TODO 设置地图准备完成回调
     */
    public void onBoxMapReady(@NonNull IOnMapReady iOnMapReady) {
        this.mIOnMapReady = iOnMapReady;
    }


    /**
     * @param iMapEvent
     * @Description 设置地图事件回调监听
     */
    public void setMapEvent(IMapEvent iMapEvent) {
        if (mMapEventDispatch == null) {
            return;
        }
        mMapEventDispatch.setCurIMapEvent(iMapEvent);
    }


    /***************************TODO 绘制相关***********************************/

    /**
     * 启动绘制 ，明确绘制类型，默认polygon
     */
    public void startSketch(@NonNull GeometryType geometryType) {
        if (geometryType == null) {
            geometryType = GeometryType.polygon;
        }
        if(mMapEventDispatch != null){
            setOnTouchListener(mMapEventDispatch);
        }
        //判断走正常绘制还是涂鸦（划线）
        if (GeometryType.slideLine.equals(geometryType)||GeometryType.slidePolygon.equals(geometryType)) {
            if (mSketchSlideAnnotations == null) {
                mSketchSlideAnnotations = new SketchSlideAnnotations(this);
            }
            mSketchSlideAnnotations.setSketchType(geometryType);
            mSketchSlideAnnotations.startSketch();
            mBeforeSketchEvent = mMapEventDispatch.getCurIMapEvent();
            mMapEventDispatch.setSketchMapEvnet(mSketchSlideAnnotations);
        } else  {
            if (mSketchAnnotations == null) {
                mSketchAnnotations = new SketchAnnotations(this, getContext());
            }
            mSketchAnnotations.setSketchType(geometryType);
            mSketchAnnotations.startSketch();
            mBeforeSketchEvent = mMapEventDispatch.getCurIMapEvent();
            mMapEventDispatch.setSketchMapEvnet(mSketchAnnotations);
        }
    }

    /**
     * 结束绘制
     */
    public void stopSketch() {
        if (mSketchAnnotations != null && mSketchAnnotations.isStartSketch()) {
            mSketchAnnotations.stopSketch();
            mMapEventDispatch.setSketchMapEvnet(null);
        }
        if (mSketchSlideAnnotations != null && mSketchSlideAnnotations.isStartSketch()) {
            mSketchSlideAnnotations.stopSketch();
            mMapEventDispatch.setSketchMapEvnet(null);
        }
    }

    /**
     * 获取当前绘制的类型
     * @return
     */
    public GeometryType getSketchType(){
        if (mSketchAnnotations != null && mSketchAnnotations.isStartSketch()) {
            return mSketchAnnotations.getSketchSetting().getSketchType();
        }
        if (mSketchSlideAnnotations != null && mSketchSlideAnnotations.isStartSketch()) {
            mSketchSlideAnnotations.stopSketch();
            mMapEventDispatch.setSketchMapEvnet(null);
            return mSketchSlideAnnotations.getGeometryType();
        }
        return null;
    }


    /**
     * 清空绘制
     */
    public void clearSketch() {
        if (mSketchAnnotations != null && mSketchAnnotations.isStartSketch()) {
            mSketchAnnotations.clearSketch();
        }
        if (mSketchSlideAnnotations != null && mSketchSlideAnnotations.isStartSketch()) {
            mSketchSlideAnnotations.clearSketch();
        }
    }

    /**
     * 判断当前绘制工具是否启动
     */
    public Boolean isStartSketch() {
        if (mSketchAnnotations != null && mSketchAnnotations.isStartSketch()) {
            return true;
        }
        if (mSketchSlideAnnotations != null && mSketchSlideAnnotations.isStartSketch()) {
            return true;
        }
        return false;
    }

    /**
     * 屏幕加点
     */
    public void sketchAddScreenCenterPoint() {
        if (mSketchAnnotations != null) {
            mSketchAnnotations.addScreenCenterPoint();
        }

    }

    /**
     * 回撤
     */
    public void sketchUnDo() {
        if (mSketchAnnotations != null) {
            mSketchAnnotations.tryUnDo();
        }
    }

    /**
     * 前撤
     */
    public void sketchReDo() {
        if (mSketchAnnotations != null) {
            mSketchAnnotations.tryReDo();
        }
    }

    /**
     * @Description 获取图形
     */
    public Geometry getSketchGeometry() {
        if (isStartSketch()) {
            if (mSketchAnnotations != null && mSketchAnnotations.isStartSketch()) {
                return mSketchAnnotations.getSketchGeometry();
            }
            if (mSketchSlideAnnotations != null && mSketchSlideAnnotations.isStartSketch()) {
                return mSketchSlideAnnotations.getSketchGeometry();
            }
        }
        return null;
    }


    /**
     * @param geometry
     * @Description 给绘制工具添加初始图形
     */
    public void addSketchGeometry(@NonNull Geometry geometry) {
        if (mSketchAnnotations == null) {
            return;
        }
        mSketchAnnotations.addGeometry(geometry);
    }

    /**
     * 设置绘制SlideLine后回调
     *
     * @param iOnAfterSlideListener
     */
    public void setIOnAfterSlideListener(SketchSlideAnnotations.IOnAfterSlideListener iOnAfterSlideListener) {
        if (mSketchSlideAnnotations != null && mSketchSlideAnnotations.isStartSketch()) {
            mSketchSlideAnnotations.setIOnAfterSlideListener(iOnAfterSlideListener);
        }
    }

    /***************************TODO 绘制相关EDN***********************************/


    /***************************TODO 选择集相关***********************************/
    /**
     * 初始化选择集
     */
    public SelectionSet initSelection() {
        if (mSelectionSet == null) {
            mSelectionSet = new SelectionSet(this);
        }
        return mSelectionSet;
    }

    /**
     * 初始化选择集
     */
    public SelectionSet initSelection(SelectSetting selectSetting) {
        if (mSelectionSet == null) {
            mSelectionSet = new SelectionSet(this);
        }
        mSelectionSet.initQuery(selectSetting);
        return mSelectionSet;
    }
    /**
     * @param customIds      如果当前需要选中的图层中没有默认的uuid或者objectid属性则允许用户设置一个默认的customId，key是layerid,value是字段名
     *                      选中渲染的优先级为 customId》uuid》objectid
     * @param selectSetting
     * @return
     */
    public SelectionSet initSelection(@NonNull  LinkedHashMap customIds, SelectSetting selectSetting) {
        if (mSelectionSet == null) {
            mSelectionSet = new SelectionSet(this);
        }
        mSelectionSet.initQuery(customIds,selectSetting);
        return mSelectionSet;
    }

    /**
     * 设置 SelectSetting
     *
     * @param selectSetting
     */
    public void setSelectSetting(SelectSetting selectSetting) {
        if (mSelectionSet != null) {
            mSelectionSet.initQuery(selectSetting);
        }

    }
    /**
     * @param customIds     如果当前需要选中的图层中没有默认的uuid或者objectid属性则允许用户设置一个默认的customId，
     *                       选中渲染的优先级为 customId》uuid》objectid
     * @param selectSetting
     * @return
     */
    public void setSelectSetting(@NonNull  LinkedHashMap customIds, SelectSetting selectSetting) {
        if (mSelectionSet != null) {
            mSelectionSet.initQuery(customIds,selectSetting);
        }

    }
    /**
     * @param pointF
     * @param pointSelFeatures 如果点击处有已选中地块择放到这个集合里
     * @return SelectionSet.SelectStatus
     * @Description 点选
     */
    public SelectionSet.SelectStatus selectFeatureByPoint(PointF pointF, Map<String, FeatureSet> pointSelFeatures) {
        if (mSelectionSet != null) {
            return mSelectionSet.selectByPoint(pointF, pointSelFeatures);
        }
        return SelectionSet.SelectStatus.selectedNull;
    }

    /**
     * @param iSelectSetChange
     * @Description 添加选择集变化监听
     */
    public void addSelectChangeListening(SelectionSet.ISelectSetChange iSelectSetChange) {
        if (mSelectionSet != null) {
            mSelectionSet.addChangeListening(iSelectSetChange);
        }
    }

    /**
     * @param iSelectSetChange
     * @Description 移除选择集变化监听
     */
    public void removeChangeListening(SelectionSet.ISelectSetChange iSelectSetChange) {
        if (mSelectionSet != null) {
            mSelectionSet.removeChangeListening(iSelectSetChange);
        }
    }

    /**
     * @Description 清空选择集和渲染
     */
    public void clearSelectSetAndRender() {
        if (mSelectionSet != null) {
            mSelectionSet.clearSelectSet().clearRender();
        }
    }

    /**
     * @return Map<String, FeatureSet>
     * @Description 获取选择集
     */
    public Map<String, FeatureSet> getSelectSet() {
        if (mSelectionSet != null) {
            return mSelectionSet.getSelectSet();
        }
        return null;
    }

    /*************************** TODO 选择集相关EDN ***********************************/

    /*************************** TODO 编辑器Editor ***********************************/
    /**
     * @param graphicSetting
     * @Description 设置临时图层编辑器  编辑类型、目标样式图层、存储路径 要在onMapReady回调内或之后执行
     */
    public void addEditor(GraphicSetting graphicSetting) {
        IGraphicContainer mIGraphicContainer = new GraphicContainerImpl(this, graphicSetting);
        BaseEditor baseEditor = new BaseEditor(mIGraphicContainer);
        mBaseEditors.put(graphicSetting,baseEditor);
    }

    /**
     * 获取Gc实例
     */
    public IGraphicContainer getGc(GraphicSetting graphicSetting) {
        BaseEditor baseEditor = getBaseEditor(graphicSetting);
        if (baseEditor != null) {
            return baseEditor.getGraphicContainer();
        }
        return null;
    }

    /**
     * 获取BaseEditor实例
     */
    public BaseEditor getBaseEditor(GraphicSetting graphicSetting) {
        if (mBaseEditors != null) {
            BaseEditor baseEditor = mBaseEditors.get(graphicSetting);
            return baseEditor;
        }
        return null;
    }

    /**
     * 获取全部BaseEditor实例
     * @return
     */
    public Map<GraphicSetting, BaseEditor> getBaseEditors() {
        return mBaseEditors;
    }

    /**
     * 获取当前操作图层id
     * 如果mBaseEditor没有初始化，会获取不到
     *
     * @return
     */
    public String getGcQueryLayerId(GraphicSetting graphicSetting) {
        IGraphicContainer graphicContainer = getGc(graphicSetting);
        if(graphicContainer!=null){
            return graphicContainer.getQueryLayerId();
        }
        return null;
    }

    /* *//**
     * @param flag
     * @param iToolInterceptor
     * @param geometry
     * @Description 工具 绘制插入临时图层
     *//*
    public void editorDrawFeature(String flag, Boolean updateTargetSource, IToolInterceptor iToolInterceptor, Geometry geometry) {
        if (mBaseEditor != null) {
            mBaseEditor.drawFeature(flag, updateTargetSource, iToolInterceptor, geometry);
        }
    }

    *//**
     * @param flag
     * @param iToolInterceptor
     * @param selectSet
     * @Description 工具 选中添加插入临时图层
     *//*
    public void editorAddFeature(String flag, Boolean updateTargetSource, IToolInterceptor iToolInterceptor, Map<String, FeatureSet> selectSet) {
        if (mBaseEditor != null) {
            mBaseEditor.addFeatures(flag, updateTargetSource, iToolInterceptor, selectSet);
        }
    }

    *//**
     * 工具 选中合并
     *
     * @param flag
     * @param iToolInterceptor
     * @param selectSet
     *//*
    public void editorUnionFeature(String flag, Boolean updateTargetSource, IToolInterceptor iToolInterceptor, Map<String, FeatureSet> selectSet) {
        if (mBaseEditor != null) {
            mBaseEditor.unionFeatures(getContext(), flag, updateTargetSource, iToolInterceptor, selectSet);
        }
    }

    *//**
     * 工具 选中合并
     *
     * @param flag
     * @param iToolInterceptor
     * @param selectSet
     *//*
    public void editorsplitFeatures(String flag, Boolean updateTargetSource, IToolInterceptor iToolInterceptor, Map<String, FeatureSet> selectSet, Geometry geometry) {
        if (mBaseEditor != null) {
            mBaseEditor.splitFeatures(flag, updateTargetSource, iToolInterceptor, selectSet, geometry);
        }
    }

    *//**
     * 工具 选中扣除
     *
     * @param flag
     * @param iToolInterceptor
     * @param selectSet
     *//*
    public void editorDigFeatures(String flag, Boolean updateTargetSource, IToolInterceptor iToolInterceptor, Map<String, FeatureSet> selectSet, Geometry geometry) {
        if (mBaseEditor != null) {
            mBaseEditor.digFeatures(flag, updateTargetSource, iToolInterceptor, selectSet, geometry);
        }
    }

    *//**
     * 工具 删除
     *
     * @param flag
     * @param iToolInterceptor
     * @param selectSet
     *//*
    public void editorDeleteFeature(String flag, IToolInterceptor iToolInterceptor, Map<String, FeatureSet> selectSet) {
        if (mBaseEditor != null) {
            mBaseEditor.delFeatures(flag, iToolInterceptor, selectSet);
        }
    }*/

    /***************************TODO 编辑器EditorEND***********************************/


    /***************************TODO 图层相关***********************************/
    /**
     * 获取地图style解析以及图层数据源过滤类
     *
     * @return
     */
    public StyleLayerManager getStyleLayerManager() {
        if (mStyleLayerManager == null) {
            mStyleLayerManager = new StyleLayerManager(mMapBoxMap);
        }
        return mStyleLayerManager;
    }
    /***************************TODO 图层相关EDN***********************************/

    /***************************TODO source相关全部移植到StyleLayerManager***********************************/

    //全部移植到StyleLayerManager
    /***************************TODO source相关EDN***********************************/


    /***************************TODO 定位相关***********************************/
    /**
     * 设置定位点
     *
     * @param latitude  纬度
     * @param longitude 经度
     */
    public void setLocation(double latitude, double longitude) {
        if (mLocView == null) {
            mLocView = new LocView(this);
            int index = getChildCount() - 1 < 0 ? 0 : getChildCount() - 1;
            addView(mLocView, index);
        }
        mLocView.setLocation(latitude, longitude);

    }

    /**
     * TODO 跳转到当前setLocation的定位点 默认放大到16级
     *
     * @param zoom 放大级别
     */
    public void moveToCurrentLocation(Double zoom,MapboxMap.CancelableCallback cancelableCallback) {
        if (mLocView == null) {
            Toast.makeText(getContext(), "未能获取当前位置信息！", Toast.LENGTH_LONG).show();
            return;
        }
        moveToLocation(mLocView.getLat(), mLocView.getLon(), zoom,cancelableCallback);
    }

    /**
     * TODO 跳转到定位点 默认放大到16级
     *
     * @param latitude
     * @param longitude
     */
    public void moveToLocation(Double latitude, Double longitude, Double zoom,MapboxMap.CancelableCallback cancelableCallback) {
        if (mMapBoxMap == null) {
            return;
        }
        if (latitude == null || latitude == null) {
            return;
        }
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), zoom == null ? 16 : zoom);
        mMapBoxMap.animateCamera(cameraUpdate, 1000,cancelableCallback);
    }

    /**
     * TODO 地图移动到当前范围
     *
     * @param bounds LatLngBounds
     */
    public void moveToLocation(LatLngBounds bounds,MapboxMap.CancelableCallback cancelableCallback) {
        if (mMapBoxMap == null) {
            return;
        }
        if (bounds == null) {
            return;
        }
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, 10);
        mMapBoxMap.animateCamera(cameraUpdate, 1000,cancelableCallback);
    }

    /**
     * TODO 地图移动到当前范围
     *
     * @param geometry Geometry
     */
    public void moveToLocation(Geometry geometry,MapboxMap.CancelableCallback cancelableCallback) {
        if (mMapBoxMap == null) {
            return;
        }
        if (geometry == null) {
            return;
        }
        LatLngBounds latLngBounds = Transformation.geometry2BoxLatLngBounds(geometry);
        moveToLocation(latLngBounds,cancelableCallback);
    }

    /**
     * TODO 地图移动到当前范围
     *
     * @param wkt
     */
    public void moveToLocation(String wkt,MapboxMap.CancelableCallback cancelableCallback) {
        if (mMapBoxMap == null) {
            return;
        }
        if (wkt == null) {
            return;
        }
        LatLngBounds latLngBounds = Transformation.wkt2BoxLatLngBounds(wkt);
        moveToLocation(latLngBounds,cancelableCallback);
    }

    /***************************TODO 定位相关EDN***********************************/

    /***************************TODO Marker相关***********************************/
    /**
     * 获取MarkerManager实例，管理marker
     *
     * @return
     */
    public MarkerManager getMarkerManager() {
        if (mMarkerManager == null) {
            mMarkerManager = new MarkerManager(this);
        }
        return mMarkerManager;
    }

    /**
     * 获取MarkerManager实例，管理marker
     *
     * @return
     */
    public MarkerViewManager getMarkerViewManager() {
        if (mMarkerViewManager == null) {
            mMarkerViewManager = new MarkerViewManager(this);
        }
        return mMarkerViewManager;
    }
    /***************************TODO Marker相关EDN***********************************/



    /***************************TODO offine相关begin***********************************/

    public OfflineDataBaseHandler getOfflineDataBaseHandler() {
        if(mOfflineDataBaseHandler == null){
            mOfflineDataBaseHandler = new OfflineDataBaseHandler(getContext());
        }
        return mOfflineDataBaseHandler;
    }

    /***************************TODO offine相关EDN***********************************/

    /**
     * @param mapEventDispatch
     * @Description 地图事件
     */
    @SuppressLint("ClickableViewAccessibility")
    private void setOnTouchListener(MapEventDispatch mapEventDispatch) {
        super.setOnTouchListener(mapEventDispatch);
        if (mMapBoxMap == null) {
            new Throwable("_mapBoxMap为null");
        }
        mMapBoxMap.removeOnMapClickListener(mMapEventDispatch);
        mMapBoxMap.addOnMapClickListener(mapEventDispatch);
        mMapBoxMap.removeOnMapLongClickListener(mMapEventDispatch);
        mMapBoxMap.addOnMapLongClickListener(mapEventDispatch);
        mMapBoxMap.removeOnCameraMoveListener(mMapEventDispatch);
        mMapBoxMap.addOnCameraMoveListener(mapEventDispatch);
        mMapBoxMap.removeOnCameraMoveCancelListener(mMapEventDispatch);
        mMapBoxMap.addOnCameraMoveCancelListener(mapEventDispatch);
        mMapBoxMap.removeOnCameraMoveStartedListener(mMapEventDispatch);
        mMapBoxMap.addOnCameraMoveStartedListener(mapEventDispatch);
        mMapBoxMap.removeOnCameraIdleListener(mMapEventDispatch);
        mMapBoxMap.addOnCameraIdleListener(mapEventDispatch);
        mMapBoxMap.removeOnCameraMoveCancelListener(mMapEventDispatch);
        mMapBoxMap.addOnCameraMoveCancelListener(mapEventDispatch);
    }

    public void addOnMapTouchListener(OnTouchListener onTouchListener) {
        this.mMapEventDispatch.addOnTouchListener(onTouchListener);
    }

    public void removeOnMapTouchListener(OnTouchListener onTouchListener) {
        this.mMapEventDispatch.removeOnTouchListener(onTouchListener);
    }
}
