package com.gradtech.mapframev10.core.maps

import android.content.Context
import android.util.AttributeSet
import android.widget.Toast
import com.gradtech.mapframev10.core.enumeration.GeometryType
import com.gradtech.mapframev10.core.event.IMapEvent
import com.gradtech.mapframev10.core.event.MapEventDispatch
import com.gradtech.mapframev10.core.marker.LocView
import com.gradtech.mapframev10.core.marker.MarkerManager
import com.gradtech.mapframev10.core.marker.MarkerViewManager
import com.gradtech.mapframev10.core.net.BoxHttpClient
import com.gradtech.mapframev10.core.offline.editor.BaseEditor
import com.gradtech.mapframev10.core.offline.gc.GraphicContainer
import com.gradtech.mapframev10.core.offline.gc.GraphicSetting
import com.gradtech.mapframev10.core.offline.gc.IGraphicContainer
import com.gradtech.mapframev10.core.rules.Rules
import com.gradtech.mapframev10.core.select.ISelectionSet
import com.gradtech.mapframev10.core.select.SelectSetting
import com.gradtech.mapframev10.core.select.SelectionSet
import com.gradtech.mapframev10.core.sketch.SketchAnnotations
import com.gradtech.mapframev10.core.style.StyleLayerManager
import com.gradtech.mapframev10.core.util.FeatureSet
import com.gradtech.mapframev10.core.util.Transformation
import com.mapbox.geojson.BoundingBox
import com.mapbox.geojson.Geometry
import com.mapbox.geojson.Point
import com.mapbox.maps.*
import com.mapbox.maps.plugin.Plugin
import com.mapbox.maps.plugin.animation.Cancelable
import com.mapbox.maps.plugin.animation.MapAnimationOptions
import com.mapbox.maps.plugin.animation.flyTo
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.gestures.GesturesPluginImpl
import org.jetbrains.annotations.NotNull

/**
 * @ClassName GMapView
 * @Description TODO 地图主要入口
 * @Author: fs
 * @Date: 2022/8/2 11:27
 * @Version 2.0
 */
class GMapView : MapView, Rules {

    companion object {
        const val LOG: String = "mapframev10"

    }

    private var mSketchAnnotations: SketchAnnotations? = null

    private var mMapEventDispatch: MapEventDispatch? = null

    private var mStyleManager: StyleLayerManager? = null

    private var mSelectionSet: SelectionSet? = null

    /**
     * 临时数据增删改查，扣除合并等
     */
    private var mBaseEditors: HashMap<GraphicSetting, BaseEditor> = hashMapOf()

    /**
     * 网络设置
     */
    private var mBoxHttpClient: BoxHttpClient? = null;

    /**
     * 定位组件
     */
    private var mLocView: LocView? = null

    private var mMarkerViewManager: MarkerViewManager? = null

    /**
     * marker管理类实例
     */
    private var mMarkerManager: MarkerManager? = null


    constructor(context: Context, mapInitOptions: MapInitOptions) : super(context, mapInitOptions) {
        initMap()
    }

    @JvmOverloads
    constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : super(context, attrs, defStyleAttr) {
        initMap()
    }

    private fun initMap() {
        //这个得放到设置 MapEvent前面，不然点击事件如果被地图消费掉，将失效
        mMarkerManager =MarkerManager(this)

        if (this.mMapEventDispatch == null) {
            MapEventDispatch().also { mMapEventDispatch = it }
        }
        mMapEventDispatch!!.let { setOnMapListener(it) }

        mBoxHttpClient = BoxHttpClient.instance();

    }

    /**
     * @param mapEventDispatch
     * @Description 地图事件
     */
    private fun setOnMapListener(mapEventDispatch: MapEventDispatch) {
        super.setOnTouchListener(mapEventDispatch)
        if (getMapboxMap() == null) {
            Throwable("_mapBoxMap为null")
        }
        val gesturesplugin = getPlugin<GesturesPluginImpl>(Plugin.MAPBOX_GESTURES_PLUGIN_ID)
        gesturesplugin?.addOnMapClickListener(mapEventDispatch)
        gesturesplugin?.addOnMoveListener(mapEventDispatch)
        gesturesplugin?.addOnMapLongClickListener(mapEventDispatch)
        gesturesplugin?.addOnFlingListener(mapEventDispatch)
        gesturesplugin?.addOnScaleListener(mapEventDispatch)
        super.setOnTouchListener(mapEventDispatch)
    }
    /***************************TODO 网络请求相关***********************************/
    /**
     * 获取网络设置对象
     */
    public fun getBoxHttpClient(): BoxHttpClient? {
        return mBoxHttpClient;
    }

    /***************************TODO 网络请求相关end***********************************/

    /***************************TODO 事件相关***********************************/
    /**
     * @param iMapEvent old setMapEvent
     * @Description 设置地图事件回调监听
     */
    fun addMapEvent(iMapEvent: IMapEvent) {
        mMapEventDispatch?.addIMapEvent(iMapEvent)
    }

    /**
     * @param iMapEvent old setMapEvent
     * @Description 移除地图事件回调监听
     */
    fun removeMapEvent(iMapEvent: IMapEvent) {
        mMapEventDispatch?.removeIMapEvent(iMapEvent)
    }
    /***************************TODO 事件相关 end***********************************/
    /***************************TODO Selection选择集操作相关begin***********************************/
    @JvmOverloads
    fun initSelection(@NotNull selectSetting: SelectSetting, customIds: LinkedHashMap<String, String>? = null) {
        if (mSelectionSet == null) {
            mSelectionSet = SelectionSet(this)
        }
        mSelectionSet!!.initQuery(customIds, selectSetting)
    }

    fun getSelectSetting(): SelectSetting? {
        return mSelectionSet?.getSelectSetting();
    }

    fun selectByScreenCoordinate(@NotNull screenCoordinate: ScreenCoordinate, selectedFeatures: MutableList<QueriedFeature>?, @NotNull callback: ISelectionSet.IQueryFeaturesCallback) {
        if (mSelectionSet != null) {
            mSelectionSet!!.selectByPoint(screenCoordinate, selectedFeatures, callback)
        }
    }

    fun selectByPoint(@NotNull point: Point, selectedFeatures: MutableList<QueriedFeature>?, @NotNull callback: ISelectionSet.IQueryFeaturesCallback) {
        if (mSelectionSet != null) {
            mSelectionSet!!.selectByPoint(point, selectedFeatures, callback)
        }
    }

    fun selectByPolygonOrLine(@NotNull geometry: Geometry, selectedFeatures: MutableList<QueriedFeature>?, @NotNull callback: ISelectionSet.IQueryFeaturesCallback) {
        if (mSelectionSet != null) {
            mSelectionSet!!.selectByPolygonOrLine(geometry, selectedFeatures, callback)
        }
    }

    /**
     * @Description 清空选择集和渲染
     */
    fun clearSelectSetAndRender() {
        if (mSelectionSet != null) {
            mSelectionSet!!.clearSelectSet()
            mSelectionSet!!.clearRender()
        }
    }

    /**
     * @return Map<String></String>, FeatureSet>
     * @Description 获取选择集
     */
    fun getSelectSet(): HashMap<String, FeatureSet>? {
        return if (mSelectionSet != null) {
            mSelectionSet!!.getSelectSet()
        } else null
    }
    /***************************TODO Selection选择集操作相关EDN***********************************/

    /***************************TODO 绘制相关***********************************/
    /**
     * 启动绘制 ，明确绘制类型，默认polygon
     */
    fun startSketch(geometryType: GeometryType) {
        if (mSketchAnnotations == null) {
            mSketchAnnotations = SketchAnnotations(context, this)
        }
        if (mSketchAnnotations!!.isStartSketch()) {
            stopSketch()
        }
        mSketchAnnotations!!.setSketchType(geometryType)
        mSketchAnnotations!!.startSketch()
    }

    /**
     * 结束绘制
     */
    fun stopSketch() {
        if (mSketchAnnotations != null && mSketchAnnotations!!.isStartSketch()) {
            mSketchAnnotations!!.stopSketch()
        }
    }

    /**
     * 获取当前绘制的类型
     * @return
     */
    fun getSketchType(): GeometryType? {
        return mSketchAnnotations?.getSketchGeometry()?.getGeometryType()
    }

    /**
     * 清空绘制
     */
    fun clearSketch() {
        if (!isStartSketch()) {
            return
        }
        mSketchAnnotations!!.clearSketch()
    }

    /**
     * 判断当前绘制工具是否启动
     */
    fun isStartSketch(): Boolean {
        if (mSketchAnnotations != null && mSketchAnnotations!!.isStartSketch()) {
            return true
        }
        return false
    }

    /**
     * 屏幕加点
     */
    fun sketchAddScreenCenterPoint() {
        mSketchAnnotations?.addScreenCenterPoint()
    }

    /**
     * 回撤
     */
    fun sketchUnDo() {
        if (mSketchAnnotations != null) {
            mSketchAnnotations!!.tryUnDo()
        }
    }

    /**
     * 前撤
     */
    fun sketchReDo() {
        if (mSketchAnnotations != null) {
            mSketchAnnotations!!.tryReDo()
        }
    }

    /**
     * @Description 获取图形
     */
    fun getSketchGeometry(): Geometry? {
        return mSketchAnnotations?.getSketchGeometry()?.getGeometry()
    }


    /**
     * @param geometry
     * @Description 给绘制工具添加初始图形
     */
    fun setSketchGeometry(geometry: Geometry) {
        mSketchAnnotations?.setSketchGeometry(geometry)
    }

    /**
     * 设置绘制SlideLine后回调
     *
     * @param iOnAfterSlideListener
     */
    fun setIOnAfterSlideListener(@NotNull iOnAfterSlideListener: SketchAnnotations.IOnAfterSlideListener) {
        mSketchAnnotations?.setIOnAfterSlideListener(iOnAfterSlideListener)
    }

    /***************************TODO 绘制相关EDN***********************************/
    /***************************TODO style操作相关begin***********************************/
    /**
     * 获取StyleManager
     */
    fun getStyleLayerManager(): StyleLayerManager {
        if (mStyleManager == null) mStyleManager = StyleLayerManager(getMapboxMap())
        return mStyleManager!!
    }

    /***************************TODO style操作相关EDN***********************************/

    /*************************** TODO 编辑器Editor  begin************************************/
    /**
     * @param graphicSetting
     * @Description 设置临时图层编辑器  编辑类型、目标样式图层、存储路径 要在onMapReady回调内或之后执行
     */
    fun addEditor(@NotNull graphicSetting: GraphicSetting) {
        val mIGraphicContainer: IGraphicContainer = GraphicContainer(this, graphicSetting)
        val baseEditor: BaseEditor = BaseEditor(mIGraphicContainer)
        mBaseEditors.put(graphicSetting, baseEditor)
    }

    /**
     * 获取Gc实例
     */
    fun getGc(@NotNull graphicSetting: GraphicSetting): IGraphicContainer? {
        val baseEditor: BaseEditor? = getBaseEditor(graphicSetting)
        return if (baseEditor != null) {
            baseEditor.getGraphicContainer()
        } else null
    }

    /**
     * 获取BaseEditor实例
     */
    fun getBaseEditor(@NotNull graphicSetting: GraphicSetting): BaseEditor? {
        return if (mBaseEditors != null) {
            mBaseEditors[graphicSetting]
        } else null

    }

    /**
     * 获取全部BaseEditor实例
     * @return
     */
    fun getBaseEditors(): HashMap<GraphicSetting, BaseEditor> {
        return mBaseEditors
    }

    /**
     * 获取当前操作图层id
     * 如果mBaseEditor没有初始化，会获取不到
     *
     * @return
     */
    fun getGcQueryLayerId(@NotNull graphicSetting: GraphicSetting): String? {
        val graphicContainer: IGraphicContainer? = getGc(graphicSetting)
        return if (graphicContainer != null) {
            graphicContainer.getQueryLayerId()
        } else null
    }


    /***************************TODO 编辑器EditorEND***********************************/

    /***************************TODO 定位相关***********************************/
    /**
     * 设置定位点
     *
     * @param latitude  纬度
     * @param longitude 经度
     */
    fun setLocation(latitude: Double, longitude: Double) {
        if (mLocView == null) {
            mLocView = LocView(this)
            val index = if (childCount - 1 < 0) 0 else childCount - 1
            addView(mLocView, index)
        }
        mLocView!!.setLocation(latitude, longitude)
    }

    /**
     * TODO 跳转到当前setLocation的定位点 默认放大到16级
     *
     * @param zoom 放大级别
     */
    fun moveToCurrentLocation(zoom: Double) {
        if (mLocView == null) {
            Toast.makeText(context, "未能获取当前位置信息！", Toast.LENGTH_LONG).show()
            return
        }
        moveToLocation(mLocView!!.getLat(), mLocView!!.getLon(), zoom)
    }

    /**
     * TODO 跳转到定位点 默认放大到16级
     *
     * @param latitude
     * @param longitude
     */
    fun moveToLocation(@NotNull latitude: Double, @NotNull longitude: Double, @NotNull zoom: Double): Cancelable? {
        val cameraOption = CameraOptions.Builder()
                .center(Point.fromLngLat(longitude, latitude))
                .zoom(zoom)
                .build()
        val flyTo: Cancelable = getMapboxMap().flyTo(cameraOption, MapAnimationOptions.mapAnimationOptions {
            duration(1500L)
        })
        return flyTo
    }


    /**
     * TODO 地图移动到当前范围
     *
     * @param bounds CoordinateBounds
     */
    fun moveToLocation(@NotNull bounds: CoordinateBounds): Cancelable? {
        if (bounds == null) {
            return null
        }
        val cameraForCoordinateBounds = getMapboxMap().cameraForCoordinateBounds(bounds, EdgeInsets(50.0, 50.0, 50.0, 50.0))
        val flyTo: Cancelable = getMapboxMap().flyTo(cameraForCoordinateBounds, MapAnimationOptions.mapAnimationOptions {
            duration(1500L)
        })
        return flyTo
    }

    /**
     * TODO 地图移动到当前范围
     *
     * @param geometry Geometry
     */
    fun moveToLocation(@NotNull geometry: Geometry): Cancelable? {
        if (geometry == null) {
            return null
        }

        val annotationPlugin = annotations
// Take Circle for example here
        /*  val circleManager = annotationPlugin.createPointAnnotationManager()
          val circleOptions: CircleOptions = CircleOptions()
                  .withPoint(Point.fromLngLat(0.381457, 6.687337))
                  .withCircleColor(ColorUtils.colorToRgbaString(Color.YELLOW))
                  .withCircleRadius(12.0)
                  .withDraggable(true)
          circleManager.create(circleOptions)*/
        var cameraForGeometry = getMapboxMap().cameraForGeometry(geometry, EdgeInsets(0.0, 50.0, 0.0, 50.0))

        val flyTo: Cancelable = getMapboxMap().flyTo(cameraForGeometry, MapAnimationOptions.mapAnimationOptions {
            duration(1500L)
        })
        return flyTo
    }

    /**
     * TODO 地图移动到当前范围
     *
     * @param wkt
     */
    fun moveToLocation(@NotNull wkt: String): Cancelable? {
        if (wkt == null) {
            return null
        }
        val latLngBounds: BoundingBox = Transformation.wkt2BoxLatLngBounds(wkt)
        return moveToLocation(CoordinateBounds(latLngBounds.southwest(), latLngBounds.northeast()))
    }

    /***************************TODO 定位相关EDN***********************************/

    /***************************TODO Marker相关***********************************/
    /**
     * 获取MarkerManager实例，管理marker
     *
     * @return
     */
       fun getMarkerManager(): MarkerManager {
           if (mMarkerManager == null) {
               mMarkerManager =MarkerManager(this)
           }
           return mMarkerManager!!
       }

    /**
     * 获取MarkerManager实例，管理marker
     *
     * @return
     */
    fun getMarkerViewManager(): MarkerViewManager {
        if (mMarkerViewManager == null) {
            mMarkerViewManager = MarkerViewManager(this)
        }
        return mMarkerViewManager!!
    }

    /***************************TODO Marker相关EDN***********************************/
    override fun onStart() {
        super.onStart()
        // mapView.onStart()
    }

    override fun onStop() {
        super.onStop()
        // mapView.onStop()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        //   mapView.onLowMemory()
    }

    override fun onDestroy() {
        super.onDestroy()
        mSketchAnnotations?.cleanup()
        mSketchAnnotations = null
        mStyleManager = null;
        mSelectionSet = null;
        mBaseEditors.clear();
        mBoxHttpClient = null;
        BoxHttpClient.cancel();
        mMarkerViewManager?.onDestroy()
        mMarkerManager?.onDestroy()
    }


}