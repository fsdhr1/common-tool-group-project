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
     * ????????????
     */
    private MapboxMap mMapBoxMap;

    private SketchAnnotations mSketchAnnotations;

    private SketchSlideAnnotations mSketchSlideAnnotations;

    private SelectionSet mSelectionSet;

    /**
     * ?????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
     */
    private IMapEvent mBeforeSketchEvent;

    /**
     * ??????????????????????????????????????????
     */
    private Map<GraphicSetting,BaseEditor> mBaseEditors = new HashMap<>();
    /**
     * ????????????
     */
    private LocView mLocView;

    private StyleLayerManager mStyleLayerManager;

    /**
     * marker???????????????
     */
    private MarkerManager mMarkerManager;

    private MarkerViewManager mMarkerViewManager;
    /**
     * ??????????????????????????????
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
     * ?????????????????????
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
     * ??????MapBoxMap
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
     * @Description TODO ??????????????????????????????
     */
    public void onBoxMapReady(@NonNull IOnMapReady iOnMapReady) {
        this.mIOnMapReady = iOnMapReady;
    }


    /**
     * @param iMapEvent
     * @Description ??????????????????????????????
     */
    public void setMapEvent(IMapEvent iMapEvent) {
        if (mMapEventDispatch == null) {
            return;
        }
        mMapEventDispatch.setCurIMapEvent(iMapEvent);
    }


    /***************************TODO ????????????***********************************/

    /**
     * ???????????? ??????????????????????????????polygon
     */
    public void startSketch(@NonNull GeometryType geometryType) {
        if (geometryType == null) {
            geometryType = GeometryType.polygon;
        }
        if(mMapEventDispatch != null){
            setOnTouchListener(mMapEventDispatch);
        }
        //?????????????????????????????????????????????
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
     * ????????????
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
     * ???????????????????????????
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
     * ????????????
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
     * ????????????????????????????????????
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
     * ????????????
     */
    public void sketchAddScreenCenterPoint() {
        if (mSketchAnnotations != null) {
            mSketchAnnotations.addScreenCenterPoint();
        }

    }

    /**
     * ??????
     */
    public void sketchUnDo() {
        if (mSketchAnnotations != null) {
            mSketchAnnotations.tryUnDo();
        }
    }

    /**
     * ??????
     */
    public void sketchReDo() {
        if (mSketchAnnotations != null) {
            mSketchAnnotations.tryReDo();
        }
    }

    /**
     * @Description ????????????
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
     * @Description ?????????????????????????????????
     */
    public void addSketchGeometry(@NonNull Geometry geometry) {
        if (mSketchAnnotations == null) {
            return;
        }
        mSketchAnnotations.addGeometry(geometry);
    }

    /**
     * ????????????SlideLine?????????
     *
     * @param iOnAfterSlideListener
     */
    public void setIOnAfterSlideListener(SketchSlideAnnotations.IOnAfterSlideListener iOnAfterSlideListener) {
        if (mSketchSlideAnnotations != null && mSketchSlideAnnotations.isStartSketch()) {
            mSketchSlideAnnotations.setIOnAfterSlideListener(iOnAfterSlideListener);
        }
    }

    /***************************TODO ????????????EDN***********************************/


    /***************************TODO ???????????????***********************************/
    /**
     * ??????????????????
     */
    public SelectionSet initSelection() {
        if (mSelectionSet == null) {
            mSelectionSet = new SelectionSet(this);
        }
        return mSelectionSet;
    }

    /**
     * ??????????????????
     */
    public SelectionSet initSelection(SelectSetting selectSetting) {
        if (mSelectionSet == null) {
            mSelectionSet = new SelectionSet(this);
        }
        mSelectionSet.initQuery(selectSetting);
        return mSelectionSet;
    }
    /**
     * @param customIds      ???????????????????????????????????????????????????uuid??????objectid??????????????????????????????????????????customId???key???layerid,value????????????
     *                      ??????????????????????????? customId???uuid???objectid
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
     * ?????? SelectSetting
     *
     * @param selectSetting
     */
    public void setSelectSetting(SelectSetting selectSetting) {
        if (mSelectionSet != null) {
            mSelectionSet.initQuery(selectSetting);
        }

    }
    /**
     * @param customIds     ???????????????????????????????????????????????????uuid??????objectid??????????????????????????????????????????customId???
     *                       ??????????????????????????? customId???uuid???objectid
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
     * @param pointSelFeatures ?????????????????????????????????????????????????????????
     * @return SelectionSet.SelectStatus
     * @Description ??????
     */
    public SelectionSet.SelectStatus selectFeatureByPoint(PointF pointF, Map<String, FeatureSet> pointSelFeatures) {
        if (mSelectionSet != null) {
            return mSelectionSet.selectByPoint(pointF, pointSelFeatures);
        }
        return SelectionSet.SelectStatus.selectedNull;
    }

    /**
     * @param iSelectSetChange
     * @Description ???????????????????????????
     */
    public void addSelectChangeListening(SelectionSet.ISelectSetChange iSelectSetChange) {
        if (mSelectionSet != null) {
            mSelectionSet.addChangeListening(iSelectSetChange);
        }
    }

    /**
     * @param iSelectSetChange
     * @Description ???????????????????????????
     */
    public void removeChangeListening(SelectionSet.ISelectSetChange iSelectSetChange) {
        if (mSelectionSet != null) {
            mSelectionSet.removeChangeListening(iSelectSetChange);
        }
    }

    /**
     * @Description ????????????????????????
     */
    public void clearSelectSetAndRender() {
        if (mSelectionSet != null) {
            mSelectionSet.clearSelectSet().clearRender();
        }
    }

    /**
     * @return Map<String, FeatureSet>
     * @Description ???????????????
     */
    public Map<String, FeatureSet> getSelectSet() {
        if (mSelectionSet != null) {
            return mSelectionSet.getSelectSet();
        }
        return null;
    }

    /*************************** TODO ???????????????EDN ***********************************/

    /*************************** TODO ?????????Editor ***********************************/
    /**
     * @param graphicSetting
     * @Description ???????????????????????????  ???????????????????????????????????????????????? ??????onMapReady????????????????????????
     */
    public void addEditor(GraphicSetting graphicSetting) {
        IGraphicContainer mIGraphicContainer = new GraphicContainerImpl(this, graphicSetting);
        BaseEditor baseEditor = new BaseEditor(mIGraphicContainer);
        mBaseEditors.put(graphicSetting,baseEditor);
    }

    /**
     * ??????Gc??????
     */
    public IGraphicContainer getGc(GraphicSetting graphicSetting) {
        BaseEditor baseEditor = getBaseEditor(graphicSetting);
        if (baseEditor != null) {
            return baseEditor.getGraphicContainer();
        }
        return null;
    }

    /**
     * ??????BaseEditor??????
     */
    public BaseEditor getBaseEditor(GraphicSetting graphicSetting) {
        if (mBaseEditors != null) {
            BaseEditor baseEditor = mBaseEditors.get(graphicSetting);
            return baseEditor;
        }
        return null;
    }

    /**
     * ????????????BaseEditor??????
     * @return
     */
    public Map<GraphicSetting, BaseEditor> getBaseEditors() {
        return mBaseEditors;
    }

    /**
     * ????????????????????????id
     * ??????mBaseEditor?????????????????????????????????
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
     * @Description ?????? ????????????????????????
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
     * @Description ?????? ??????????????????????????????
     *//*
    public void editorAddFeature(String flag, Boolean updateTargetSource, IToolInterceptor iToolInterceptor, Map<String, FeatureSet> selectSet) {
        if (mBaseEditor != null) {
            mBaseEditor.addFeatures(flag, updateTargetSource, iToolInterceptor, selectSet);
        }
    }

    *//**
     * ?????? ????????????
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
     * ?????? ????????????
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
     * ?????? ????????????
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
     * ?????? ??????
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

    /***************************TODO ?????????EditorEND***********************************/


    /***************************TODO ????????????***********************************/
    /**
     * ????????????style????????????????????????????????????
     *
     * @return
     */
    public StyleLayerManager getStyleLayerManager() {
        if (mStyleLayerManager == null) {
            mStyleLayerManager = new StyleLayerManager(mMapBoxMap);
        }
        return mStyleLayerManager;
    }
    /***************************TODO ????????????EDN***********************************/

    /***************************TODO source?????????????????????StyleLayerManager***********************************/

    //???????????????StyleLayerManager
    /***************************TODO source??????EDN***********************************/


    /***************************TODO ????????????***********************************/
    /**
     * ???????????????
     *
     * @param latitude  ??????
     * @param longitude ??????
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
     * TODO ???????????????setLocation???????????? ???????????????16???
     *
     * @param zoom ????????????
     */
    public void moveToCurrentLocation(Double zoom,MapboxMap.CancelableCallback cancelableCallback) {
        if (mLocView == null) {
            Toast.makeText(getContext(), "?????????????????????????????????", Toast.LENGTH_LONG).show();
            return;
        }
        moveToLocation(mLocView.getLat(), mLocView.getLon(), zoom,cancelableCallback);
    }

    /**
     * TODO ?????????????????? ???????????????16???
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
     * TODO ???????????????????????????
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
     * TODO ???????????????????????????
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
     * TODO ???????????????????????????
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

    /***************************TODO ????????????EDN***********************************/

    /***************************TODO Marker??????***********************************/
    /**
     * ??????MarkerManager???????????????marker
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
     * ??????MarkerManager???????????????marker
     *
     * @return
     */
    public MarkerViewManager getMarkerViewManager() {
        if (mMarkerViewManager == null) {
            mMarkerViewManager = new MarkerViewManager(this);
        }
        return mMarkerViewManager;
    }
    /***************************TODO Marker??????EDN***********************************/



    /***************************TODO offine??????begin***********************************/

    public OfflineDataBaseHandler getOfflineDataBaseHandler() {
        if(mOfflineDataBaseHandler == null){
            mOfflineDataBaseHandler = new OfflineDataBaseHandler(getContext());
        }
        return mOfflineDataBaseHandler;
    }

    /***************************TODO offine??????EDN***********************************/

    /**
     * @param mapEventDispatch
     * @Description ????????????
     */
    @SuppressLint("ClickableViewAccessibility")
    private void setOnTouchListener(MapEventDispatch mapEventDispatch) {
        super.setOnTouchListener(mapEventDispatch);
        if (mMapBoxMap == null) {
            new Throwable("_mapBoxMap???null");
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
