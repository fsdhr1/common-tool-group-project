package com.gradtech.mapframev10.core.sketch

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Color
import android.util.Log
import android.view.MotionEvent
import com.gradtech.mapframev10.R
import com.gradtech.mapframev10.core.enumeration.GeometryType
import com.gradtech.mapframev10.core.event.IMapEvent
import com.gradtech.mapframev10.core.maps.GMapView
import com.gradtech.mapframev10.core.util.MathUtil
import com.gradtech.mapframev10.core.util.Transformation
import com.gradtech.mapframev10.core.util.Transformation.boxGeometry2Points
import com.mapbox.geojson.*
import com.mapbox.maps.ScreenCoordinate
import com.mapbox.maps.extension.style.expressions.dsl.generated.number
import com.mapbox.maps.extension.style.expressions.generated.Expression
import com.mapbox.maps.extension.style.layers.addLayer
import com.mapbox.maps.extension.style.layers.generated.fillLayer
import com.mapbox.maps.extension.style.layers.generated.lineLayer
import com.mapbox.maps.extension.style.layers.generated.symbolLayer
import com.mapbox.maps.extension.style.layers.properties.generated.IconAnchor
import com.mapbox.maps.extension.style.layers.properties.generated.SymbolPlacement
import com.mapbox.maps.extension.style.layers.properties.generated.TextAnchor
import com.mapbox.maps.extension.style.sources.addSource
import com.mapbox.maps.extension.style.sources.generated.geoJsonSource
import com.mapbox.maps.extension.style.sources.getSource
import com.mapbox.maps.plugin.MapPlugin
import com.mapbox.turf.*
import org.jetbrains.annotations.NotNull
import java.util.*

/**
 * @ClassName SketchAnnotations
 * @Description TODO
 * @Author: fs
 * @Date: 2022/8/2 15:58
 * @Version 2.0
 */
class SketchAnnotations : MapPlugin, ISketch, IMapEvent {

    private var mContext: Context

    private var mGmapView: GMapView

    private var mIsStart = false

    private var geojsonFillSource = geoJsonSource("draw-source") 
    private var geojsonSymbolSource = geoJsonSource("draw-symbol-source") 
    private var geojsonLineSource = geoJsonSource("draw-line-source") 
    private var geojsonScrawlSource = geoJsonSource("scrawl-source") 
    //橡皮筋图层
    private var geojsonRubberLineSource = geoJsonSource("draw-Rubberline-source") 
    //标注线段长度
    private var geojsonTextLineSource = geoJsonSource("draw-text-line-source") 

    private lateinit var mSketchHelpView:SketchHelpView

    private var mSketchGeometry: SketchGeometry

    private lateinit var onepoint:Point
    private lateinit var twopoint:Point
    private lateinit var tagpoint:Point
    private lateinit var oldTagPoint: Point


    /**
     * 是否绘制橡皮筋
     */
    private var mDrawRubber:Boolean = false

    /**
     * 判断当前是否有拖拽事件，比如拖拽点等
     */
    private var mTouchAction:GeoTouchMode = GeoTouchMode.TouchOnNull

    enum class GeoTouchMode {
        TouchOnNull, TouchOnMidPoint, TouchOnPoint, TouchOnLine, TouchOnPolygon
    }

    /*
    *涂鸦线完成绘制回调监听
     */
    private var mIOnAfterSlideListener:IOnAfterSlideListener? = null

    fun setIOnAfterSlideListener(@NotNull iOnAfterSlideListener:IOnAfterSlideListener){
        mIOnAfterSlideListener = iOnAfterSlideListener
    }

    interface IOnAfterSlideListener {
        fun onAfterSlideListener(geometry: Geometry?)
    }

    @JvmOverloads
    constructor(@NotNull context: Context, @NotNull gmapView: GMapView, geometryType: GeometryType = GeometryType.polygon) {
        mContext = context
        mGmapView = gmapView
        mSketchGeometry = SketchGeometry(geometryType)
    }

    override fun setSketchType(geometryType: GeometryType) {
        mSketchGeometry.setGeometryType(geometryType)
    }

    override fun startSketch() {
        mIsStart = true
        mGmapView.addMapEvent(this)
        initStyleLayer()
    }

    override fun stopSketch() {
        mIsStart = false
        mGmapView.removeMapEvent(this)
        initStyleLayer()
    }

    /**
     * 初始化绘制的layer以及source
     */
    private fun initStyleLayer() {
        if(mIsStart){
            if(mSketchGeometry.getGeometryType() == GeometryType.slideLine){
                mGmapView.getMapboxMap().getStyle()?.addSource(geojsonScrawlSource)
                val lineLayer = lineLayer("scrawl-line-layer", geojsonScrawlSource.sourceId) {
                    lineColor(Color.parseColor("#94FDD6")).lineWidth(3.0).lineOpacity(1.0)
                }
                mGmapView.getMapboxMap().getStyle()?.addLayer(lineLayer)
            }else if(mSketchGeometry.getGeometryType() == GeometryType.slidePolygon){
                mGmapView.getMapboxMap().getStyle()?.addSource(geojsonScrawlSource)
                val fillLayer = fillLayer("scrawl-fill-layer", geojsonScrawlSource.sourceId) {
                    fillColor(Color.RED)
                            .fillOpacity(0.5).fillOutlineColor(Color.parseColor("#94FDD6"))
                            .fillColor(Color.parseColor("#94FDD6"))

                }
                mGmapView.getMapboxMap().getStyle()?.addLayer(fillLayer)
            }else{
                mGmapView.getMapboxMap().getStyle()?.addSource(geojsonFillSource)
                mGmapView.getMapboxMap().getStyle()?.addSource(geojsonSymbolSource)
                mGmapView.getMapboxMap().getStyle()?.addSource(geojsonLineSource)
                mGmapView.getMapboxMap().getStyle()?.addSource(geojsonRubberLineSource)
                mGmapView.getMapboxMap().getStyle()?.addSource(geojsonTextLineSource)
                val decodeResource = BitmapFactory.decodeResource(mContext.resources, R.mipmap.gmmfc_draw_shape)
                mGmapView.getMapboxMap().getStyle()?.addImage("draw-p", decodeResource)
                decodeResource.recycle()
                val fillLayer = fillLayer("draw-fill-layer", "draw-source") {
                    fillColor(Color.RED)
                            .fillOpacity(0.2)
                            .fillOutlineColor(Color.RED)
                }
                mGmapView.getMapboxMap().getStyle()?.addLayer(fillLayer)
                val lineLayer = lineLayer("draw-line-layer", "draw-line-source") {
                    lineColor(Color.RED).lineWidth(2.0)
                }
                mGmapView.getMapboxMap().getStyle()?.addLayer(lineLayer)

                val SymbolLayer = symbolLayer("draw-symbol-layer", "draw-symbol-source") {
                    iconImage("draw-p").iconAnchor(IconAnchor.CENTER).iconAllowOverlap(true)
                }
                mGmapView.getMapboxMap().getStyle()?.addLayer(SymbolLayer)
                //添加橡皮筋
                val rubberLineLayer = lineLayer("draw-Rubberline-layer", "draw-Rubberline-source") {
                    lineColor(Color.YELLOW).lineWidth(2.0)
                }
                mGmapView.getMapboxMap().getStyle()?.addLayer(rubberLineLayer)
                val testValue = listOf(1.0, 0.0)
                val expression = number {
                    get {
                        literal("number")
                    }
                }
                val textSymbolLayer = symbolLayer("draw-text_symbol-layer", "draw-text-line-source"){
                    textField(Expression.concat(Expression.get("length"), Expression.get("unit"))).textFont(listOf("FZHei-B01S Regular")).textColor(Color.WHITE).textSize(12.5)
                            .textAnchor(TextAnchor.TOP_LEFT)
                            .symbolPlacement(SymbolPlacement.LINE_CENTER).textOffset(testValue).textAllowOverlap(false).iconAllowOverlap(true)
                            .symbolAvoidEdges(false).textIgnorePlacement(true).symbolSpacing(100000000.0)
                }
                mGmapView.getMapboxMap().getStyle()?.addLayer(textSymbolLayer)

                //初始化扩展view
                mSketchHelpView = SketchHelpView(mContext)
                if(mSketchGeometry.getGeometryType() == GeometryType.multiPolygon||mSketchGeometry.getGeometryType()==GeometryType.polygon){
                    mSketchHelpView.drawArea(true)
                    mSketchHelpView.drawLength(true)
                } else if(mSketchGeometry.getGeometryType() == GeometryType.multiLine||mSketchGeometry.getGeometryType()==GeometryType.multiLine){
                    mSketchHelpView.drawLength(true)
                }
                mGmapView.addView(this.mSketchHelpView)
            }

        }else{
            clearSketch()
            if(mSketchGeometry.getGeometryType() == GeometryType.slideLine||mSketchGeometry.getGeometryType()  == GeometryType.slidePolygon){
                mGmapView.getMapboxMap().getStyle()?.removeStyleLayer("scrawl-line-layer")
                mGmapView.getMapboxMap().getStyle()?.removeStyleLayer("scrawl-fill-layer")
                mGmapView.getMapboxMap().getStyle()?.removeStyleSource(geojsonScrawlSource.sourceId)
            }else{
                mGmapView.getMapboxMap().getStyle()?.removeStyleLayer("draw-Rubberline-layer")
                mGmapView.getMapboxMap().getStyle()?.removeStyleLayer("draw-symbol-layer")
                mGmapView.getMapboxMap().getStyle()?.removeStyleLayer("draw-line-layer")
                mGmapView.getMapboxMap().getStyle()?.removeStyleLayer("draw-fill-layer")
                mGmapView.getMapboxMap().getStyle()?.removeStyleLayer("draw-text_symbol-layer")
                mGmapView.getMapboxMap().getStyle()?.removeStyleSource("draw-Rubberline-source")
                mGmapView.getMapboxMap().getStyle()?.removeStyleSource("draw-symbol-source")
                mGmapView.getMapboxMap().getStyle()?.removeStyleSource("draw-line-source")
                mGmapView.getMapboxMap().getStyle()?.removeStyleSource("draw-source")
                mGmapView.getMapboxMap().getStyle()?.removeStyleSource("draw-text-line-source")
                mGmapView.removeView(mSketchHelpView)
                cleanup()
            }

        }

    }




    override fun isStartSketch(): Boolean {
        return mIsStart
    }

    override fun getSketchGeometry(): SketchGeometry? {
       return mSketchGeometry
    }


    override fun setSketchGeometry(geometry: Geometry) {
        if(!isStartSketch()){
            return
        }
        mSketchGeometry.setGeometry(geometry)
        drawGeometry(mSketchGeometry.getGeometry(), mSketchGeometry.getGeometryType())
    }


    override fun onMapClick(point: Point): Boolean {
        addPoint(point)
        //drawRubberLine()
        mDrawRubber =false
        drawRubberLine()
        return true
    }

    override fun onMapLongClick(point: Point): Boolean {
        val geometry = mSketchGeometry.getGeometry()
        if(geometry is Polygon){
            val boxGeometry2Points = boxGeometry2Points(geometry, true)
            var nearestPoint = TurfClassification.nearestPoint(point, boxGeometry2Points)
            val indexOf = boxGeometry2Points.indexOf(nearestPoint)
            if(indexOf == boxGeometry2Points.size-1){
                onepoint = boxGeometry2Points[0]
                twopoint = boxGeometry2Points[if (indexOf - 1 < 0) 0 else indexOf - 1]
                oldTagPoint = nearestPoint
            }else if(indexOf == 0){
                onepoint = boxGeometry2Points[indexOf + 1]
                twopoint = boxGeometry2Points[boxGeometry2Points.size - 1]
                oldTagPoint = nearestPoint
            }else{
                onepoint= boxGeometry2Points[indexOf + 1]
                twopoint = boxGeometry2Points[if (indexOf - 1 < 0) 0 else indexOf - 1]
                oldTagPoint = nearestPoint
            }
            val midpoint1 = TurfMeasurement.midpoint(onepoint, nearestPoint)
            val midpoint2 = TurfMeasurement.midpoint(twopoint, nearestPoint)
            var ps= listOf<Point>(midpoint1, midpoint2, nearestPoint)
            val nearestPointMid = TurfClassification.nearestPoint(point, ps)
            if(midpoint1.equals(nearestPointMid)){
                twopoint = nearestPoint
                mTouchAction = GeoTouchMode.TouchOnMidPoint
            }
            else if(midpoint2.equals(nearestPointMid)){
                onepoint = nearestPoint
                mTouchAction = GeoTouchMode.TouchOnMidPoint
            }else{
                mTouchAction = GeoTouchMode.TouchOnPoint
            }
            mDrawRubber = true
        }
        if(geometry is LineString){
            val boxGeometry2Points = boxGeometry2Points(geometry, true)
            var nearestPoint = TurfClassification.nearestPoint(point, boxGeometry2Points)
            val indexOf = boxGeometry2Points.indexOf(nearestPoint)
            if(indexOf == boxGeometry2Points.size-1){
                onepoint = boxGeometry2Points.get(if (indexOf - 1 < 0) 0 else indexOf - 1)
                twopoint = boxGeometry2Points.get(if (indexOf - 1 < 0) 0 else indexOf - 1)
                oldTagPoint = nearestPoint
            }else if(indexOf == 0){
                onepoint = boxGeometry2Points.get(indexOf + 1)
                twopoint = boxGeometry2Points.get(indexOf + 1)
                oldTagPoint = nearestPoint
            }else{
                onepoint= boxGeometry2Points[indexOf + 1]
                twopoint = boxGeometry2Points[if (indexOf - 1 < 0) 0 else indexOf - 1]
                oldTagPoint = nearestPoint
            }
            val midpoint1 = TurfMeasurement.midpoint(onepoint, nearestPoint)
            val midpoint2 = TurfMeasurement.midpoint(twopoint, nearestPoint)
            var ps= listOf<Point>(midpoint1, midpoint2, nearestPoint)
            val nearestPointMid = TurfClassification.nearestPoint(point, ps)
            if(midpoint1.equals(nearestPointMid)){
                twopoint = nearestPoint
                mTouchAction = GeoTouchMode.TouchOnMidPoint
            }
            else if(midpoint2.equals(nearestPointMid)){
                onepoint = nearestPoint
                mTouchAction = GeoTouchMode.TouchOnMidPoint
            }else{
                mTouchAction = GeoTouchMode.TouchOnPoint
            }
            mDrawRubber = true
        }
        if(geometry is Point||geometry is MultiPoint){
            mTouchAction = GeoTouchMode.TouchOnNull
            mDrawRubber = false
        }
        if(geometry is MultiPolygon){
            val polygons = (geometry as MultiPolygon).polygons()
            lateinit var nearestPointLast:Point
            lateinit var boxGeometry2PointsList:List<Point>
            var lastIndex = -1
            for (i in 0..polygons.size-1){
                val polygon = polygons.get(i)
                val boxGeometry2Points = boxGeometry2Points(polygon, true)
                var nearestPoint = TurfClassification.nearestPoint(point, boxGeometry2Points)
                if(lastIndex ==-1){
                    lastIndex = 0
                    nearestPoint.also { nearestPointLast = it }
                    boxGeometry2PointsList = boxGeometry2Points
                }else{
                    val ps= listOf<Point>(nearestPoint, nearestPointLast, nearestPoint)
                    val nearestPoint1 = TurfClassification.nearestPoint(point, ps)
                    if(nearestPoint1 == nearestPoint){
                        lastIndex = i
                        nearestPointLast = nearestPoint
                        boxGeometry2PointsList = boxGeometry2Points
                    }
                }
            }
            val indexOf = boxGeometry2PointsList.indexOf(nearestPointLast)
            if(indexOf == boxGeometry2PointsList.size-1){
                onepoint = boxGeometry2PointsList.get(0)
                twopoint = boxGeometry2PointsList.get(if (indexOf - 1 < 0) 0 else indexOf - 1)
                oldTagPoint = nearestPointLast
            }else if(indexOf == 0){
                onepoint = boxGeometry2PointsList.get(indexOf + 1)
                twopoint = boxGeometry2PointsList.get(boxGeometry2PointsList.size - 1)
                oldTagPoint = nearestPointLast
            }else{
                onepoint=boxGeometry2PointsList.get(indexOf + 1)
                twopoint = boxGeometry2PointsList.get(if (indexOf - 1 < 0) 0 else indexOf - 1)
                oldTagPoint = nearestPointLast
            }
            val midpoint1 = TurfMeasurement.midpoint(onepoint, nearestPointLast)
            val midpoint2 = TurfMeasurement.midpoint(twopoint, nearestPointLast)
            var ps= listOf<Point>(midpoint1, midpoint2, nearestPointLast)
            val nearestPointMid = TurfClassification.nearestPoint(point, ps)
            if(midpoint1.equals(nearestPointMid)){
                twopoint = nearestPointLast
                mTouchAction = GeoTouchMode.TouchOnMidPoint
            }
            else if(midpoint2.equals(nearestPointMid)){
                onepoint = nearestPointLast
                mTouchAction = GeoTouchMode.TouchOnMidPoint
            }else{
                mTouchAction = GeoTouchMode.TouchOnPoint
            }
            mDrawRubber = true
        }
        if(geometry is MultiLineString){
            val lineStrings = (geometry).lineStrings()
            lateinit var nearestPointLast:Point
            lateinit var boxGeometry2PointsList:List<Point>
            var lastIndex = -1
            lineStrings.forEachIndexed(){ index, lineString ->
                val boxGeometry2Points = boxGeometry2Points(geometry, true)
                var nearestPoint = TurfClassification.nearestPoint(point, boxGeometry2Points)
                if(lastIndex ==-1){
                    lastIndex = 0
                    nearestPointLast = nearestPoint
                    boxGeometry2Points.also { boxGeometry2PointsList = it }
                }else{
                    var ps= listOf<Point>(nearestPoint, nearestPointLast, nearestPoint)
                    val nearestPoint1 = TurfClassification.nearestPoint(point, ps)
                    if(nearestPoint1 == nearestPoint){
                        lastIndex = index
                        nearestPointLast = nearestPoint
                        boxGeometry2Points.also { boxGeometry2PointsList = it }
                    }
                }
            }
            val indexOf = boxGeometry2PointsList.indexOf(nearestPointLast)
            if(indexOf == boxGeometry2PointsList.size-1){
                onepoint = boxGeometry2PointsList.get(if (indexOf - 1 < 0) 0 else indexOf - 1)
                twopoint = boxGeometry2PointsList.get(if (indexOf - 1 < 0) 0 else indexOf - 1)
                oldTagPoint = nearestPointLast
            }else if(indexOf == 0){
                onepoint = boxGeometry2PointsList.get(indexOf + 1)
                twopoint = boxGeometry2PointsList.get(indexOf + 1)
                oldTagPoint = nearestPointLast
            }else{
                onepoint=boxGeometry2PointsList.get(indexOf + 1)
                twopoint = boxGeometry2PointsList.get(if (indexOf - 1 < 0) 0 else indexOf - 1)
                oldTagPoint = nearestPointLast
            }
            val midpoint1 = TurfMeasurement.midpoint(onepoint, nearestPointLast)
            val midpoint2 = TurfMeasurement.midpoint(twopoint, nearestPointLast)
            var ps= listOf<Point>(midpoint1, midpoint2, nearestPointLast)
            val nearestPointMid = TurfClassification.nearestPoint(point, ps)
            if(midpoint1.equals(nearestPointMid)){
                twopoint = nearestPointLast
                mTouchAction = GeoTouchMode.TouchOnMidPoint
            }
            else if(midpoint2.equals(nearestPointMid)){
                onepoint = nearestPointLast
                mTouchAction = GeoTouchMode.TouchOnMidPoint
            }else{
                mTouchAction = GeoTouchMode.TouchOnPoint
            }
            mDrawRubber = true
        }
        return true
    }

    override fun onTouchStart(motionEvent: MotionEvent?): Boolean {
        return super.onTouchStart(motionEvent)
    }

    override fun onTouchMoving(motionEvent: MotionEvent?): Boolean {
        if(mSketchGeometry.getGeometryType() == GeometryType.slidePolygon||mSketchGeometry.getGeometryType() == GeometryType.slideLine){
            val x = motionEvent?.getX()
            val y = motionEvent?.getY()
            val screenCoordinate = ScreenCoordinate(x!!.toDouble(), y!!.toDouble())
            tagpoint = mGmapView.getMapboxMap().coordinateForPixel(screenCoordinate)
            mSketchGeometry.addPoint(tagpoint)
            drawGeometry(mSketchGeometry.getGeometry(), mSketchGeometry.getGeometryType())
            return true
        }
        if(mDrawRubber){
            if(mTouchAction!=GeoTouchMode.TouchOnNull){
                val x = motionEvent?.getX()
                val y = motionEvent?.getY()
                val screenCoordinate = ScreenCoordinate(x!!.toDouble(), y!!.toDouble())
                tagpoint = mGmapView.getMapboxMap().coordinateForPixel(screenCoordinate)
            }else{
                val screenCenterPoint: ScreenCoordinate = mSketchHelpView.getScreenCenterPoint()
                val mapboxMap = mGmapView.getMapboxMap()
                tagpoint = mapboxMap.coordinateForPixel(screenCenterPoint)
            }
            drawRubberLine()
        }

        //simulate

        if(mTouchAction!=GeoTouchMode.TouchOnNull){
            return true
        }else{
            return super.onTouchMoving(motionEvent)
        }
    }

    override fun onTouchCancel(motionEvent: MotionEvent?): Boolean {
        if(mTouchAction!=GeoTouchMode.TouchOnNull){
            mDrawRubber = false
            if(mTouchAction == GeoTouchMode.TouchOnMidPoint){
                mSketchGeometry.updateMidPoint(onepoint, twopoint, tagpoint)
            }else if(mTouchAction == GeoTouchMode.TouchOnPoint){
                mSketchGeometry.updatePoint(oldTagPoint, tagpoint)
            }
            drawGeometry(mSketchGeometry.getGeometry(), mSketchGeometry.getGeometryType())
            drawRubberLine()
            mTouchAction = GeoTouchMode.TouchOnNull
        }
        if(mSketchGeometry.getGeometryType() == GeometryType.slidePolygon||mSketchGeometry.getGeometryType() == GeometryType.slideLine){
            mIOnAfterSlideListener?.onAfterSlideListener(mSketchGeometry.getGeometry())
        }
        return super.onTouchCancel(motionEvent)
    }

    /**
     * 加点 屏幕中心点
     */
    fun addScreenCenterPoint() {
        val screenCenterPoint: ScreenCoordinate = mSketchHelpView.getScreenCenterPoint()
        val mapboxMap = mGmapView.getMapboxMap()
        val coordinateForPixel = mapboxMap.coordinateForPixel(screenCenterPoint)
        if (coordinateForPixel != null) {
            addPoint(Point.fromLngLat(coordinateForPixel.longitude(), coordinateForPixel.latitude()))
        }
        val geometry = mSketchGeometry.getGeometry() ?: return
        tagpoint = mapboxMap.coordinateForPixel(screenCenterPoint)
        val points = boxGeometry2Points(geometry, true)
        if(points.size>0){
            onepoint = points.get(0)
        }
        twopoint = points.get(points.size - 1)
        mDrawRubber = true
        drawRubberLine()
    }

    private fun drawRubberLine() {
        if(mGmapView.getMapboxMap().getStyle()?.getSource(geojsonRubberLineSource.sourceId) == null)return
        if(!mDrawRubber){
            geojsonRubberLineSource.featureCollection(FeatureCollection.fromJson("{\n" +
                    "  \"type\":\"FeatureCollection\",\n" +
                    "  \"features\":[\n" +
                    "    \n" +
                    "    ]\n" +
                    "  }"))
            return
        }
        if(mSketchGeometry.getGeometryType() == GeometryType.point||mSketchGeometry.getGeometryType() == GeometryType.multiPoint){
            return
        }
        val geometry = mSketchGeometry.getGeometry() ?: return
        val points = boxGeometry2Points(geometry, true)
        if(points.size>0){
            var ps = mutableListOf<Point>()
            if(mSketchGeometry.getGeometryType() == GeometryType.polygon||mTouchAction != GeoTouchMode.TouchOnNull){
                ps.add(onepoint)
            }
            ps.add(tagpoint)
            ps.add(twopoint)
            val lineString = LineString.fromLngLats(ps)
            geojsonRubberLineSource.data(lineString.toJson())
            var geo :Geometry? = null
            if(mTouchAction == GeoTouchMode.TouchOnMidPoint){
                geo = mSketchGeometry.updateMidPoint(onepoint, twopoint, tagpoint, true)
            }else if(mTouchAction == GeoTouchMode.TouchOnPoint){
                geo =  mSketchGeometry.updatePoint(oldTagPoint, tagpoint, true)
            }else if(mTouchAction == GeoTouchMode.TouchOnNull){
                geo = mSketchGeometry.addPoint(tagpoint, true)
            }
            if(geo!=null){
                val geometryType = mSketchGeometry.getGeometryType()
                if(geometryType == GeometryType.multiPolygon||geometryType == GeometryType.polygon){
                    val area = if(points.size>=3)Transformation.compute84GeoJsonArea(geo)else 0.00
                    val length = if(points.size>=2)Transformation.compute84GeoJsonLen(geo)else 0.00
                    mSketchHelpView.setLengthMsg(MathUtil.double2HALF_UP(length, 2))
                    mSketchHelpView.setAreaMsg(MathUtil.double2HALF_UP(area, 2))
                }else if(geometryType == GeometryType.multiLine||geometryType == GeometryType.line){
                    val length = if(points.size>=2)Transformation.compute84GeoJsonLen(geo)else 0.00
                    mSketchHelpView.setLengthMsg(MathUtil.double2HALF_UP(length, 2))
                }
                mSketchHelpView.postInvalidate()
                drawTextLine(geo)
            }
        }else{
            geojsonRubberLineSource.featureCollection(FeatureCollection.fromJson("{\n" +
                    "  \"type\":\"FeatureCollection\",\n" +
                    "  \"features\":[\n" +
                    "    \n" +
                    "    ]\n" +
                    "  }"))
        }

    }

    /**
     * 清空绘制集合（重绘）
     */
    override fun clearSketch() {
        mSketchGeometry.setGeometry(null)
        val geometryType = mSketchGeometry.getGeometryType()
        drawGeometry(mSketchGeometry.getGeometry(), geometryType)
        mDrawRubber = false
        drawRubberLine()
    }


    private fun addPoint(point: Point){
        this.mSketchGeometry.addPoint(point)
        val geometryType = mSketchGeometry.getGeometryType()
        drawGeometry(mSketchGeometry.getGeometry(), geometryType)
    }

    /**
     * 回撤
     */
    override fun tryUnDo() {
        this.mSketchGeometry.undo()
        val geometryType = mSketchGeometry.getGeometryType()
        drawGeometry(mSketchGeometry.getGeometry(), geometryType)
    }
    /**
     * 前撤
     */
    override fun tryReDo() {
        this.mSketchGeometry.redo()
        val geometryType = mSketchGeometry.getGeometryType()
        drawGeometry(mSketchGeometry.getGeometry(), geometryType)
    }

    private fun drawGeometry(mGeometry: Geometry?, geometryType: GeometryType) {
        if(geometryType == GeometryType.slideLine){
            drawSlideLine(mGeometry)
        }
        else if(geometryType == GeometryType.slidePolygon){
            drawSlidePolygon(mGeometry)
        }else{
            if(geometryType == GeometryType.polygon||geometryType == GeometryType.multiPolygon){
                drawPolygon(mGeometry)
                drawLine(mGeometry)
                drawPoint(mGeometry)
                drawTextLine(mGeometry)
            }
            if(geometryType == GeometryType.line||geometryType == GeometryType.multiLine){
                drawLine(mGeometry)
                drawPoint(mGeometry)
                drawTextLine(mGeometry)
            }
            if(geometryType == GeometryType.point||geometryType == GeometryType.multiPoint){
                drawPoint(mGeometry)
            }
            mSketchHelpView.postInvalidate()
        }
    }

    private fun drawSlideLine(mGeometry: Geometry?){
        geojsonScrawlSource.featureCollection(FeatureCollection.fromJson("{\n" +
                "  \"type\":\"FeatureCollection\",\n" +
                "  \"features\":[\n" +
                "    \n" +
                "    ]\n" +
                "  }"))
        if(mGeometry != null){
            val boxGeometry2Points = Transformation.boxGeometry2Points(mGeometry, false)
            if(boxGeometry2Points.size<4){
                return
            }

            geojsonScrawlSource.data(mGeometry.toJson())

        }
    }

    private fun drawSlidePolygon(mGeometry: Geometry?){
        geojsonScrawlSource.featureCollection(FeatureCollection.fromJson("{\n" +
                "  \"type\":\"FeatureCollection\",\n" +
                "  \"features\":[\n" +
                "    \n" +
                "    ]\n" +
                "  }"))
        if(mGeometry != null){
            val boxGeometry2Points = Transformation.boxGeometry2Points(mGeometry, false)
            if(boxGeometry2Points.size<4){
                return
            }
            geojsonScrawlSource.data(mGeometry.toJson())

        }
    }

    private fun drawPolygon(mGeometry: Geometry?){
        geojsonFillSource.featureCollection(FeatureCollection.fromJson("{\n" +
                "  \"type\":\"FeatureCollection\",\n" +
                "  \"features\":[\n" +
                "    \n" +
                "    ]\n" +
                "  }"))
        mSketchHelpView.setAreaMsg(0.00)
        if(mGeometry != null){
            val boxGeometry2Points = Transformation.boxGeometry2Points(mGeometry, false)
            if(boxGeometry2Points.size<4){
                return
            }
            val compute84GeoJsonArea = Transformation.compute84GeoJsonArea(mGeometry)
            mSketchHelpView.setAreaMsg(MathUtil.double2HALF_UP(compute84GeoJsonArea, 2))
            geojsonFillSource.data(mGeometry.toJson())

        }
    }

    private fun drawLine(mGeometry: Geometry?){
        geojsonLineSource.featureCollection(FeatureCollection.fromJson("{\n" +
                "  \"type\":\"FeatureCollection\",\n" +
                "  \"features\":[\n" +
                "    \n" +
                "    ]\n" +
                "  }"))
        if(mGeometry != null){
            val boxGeometry2Points = Transformation.boxGeometry2Points(mGeometry, false)
            if(boxGeometry2Points.size<2){
                return
            }

            if(mGeometry is Polygon){
                val polygonToLine = TurfConversion.polygonToLine(mGeometry as Polygon)
                geojsonLineSource.data(polygonToLine.toJson())
            }
            if(mGeometry is MultiPolygon){
                geojsonLineSource.data(TurfConversion.polygonToLine(mGeometry as MultiPolygon).toJson())
            }
            if(mGeometry is LineString||mGeometry is MultiLineString){
                geojsonLineSource.data(mGeometry.toJson())
            }
        }

    }

    private fun drawPoint(mGeometry: Geometry?){
        if(mGeometry == null){
            geojsonSymbolSource.featureCollection(FeatureCollection.fromJson("{\n" +
                    "  \"type\":\"FeatureCollection\",\n" +
                    "  \"features\":[\n" +
                    "    \n" +
                    "    ]\n" +
                    "  }"))
            return
        }
        val featureCollection = Transformation.boxGeometry2PointFeatureCollection(mGeometry, true)
        geojsonSymbolSource.featureCollection(featureCollection)
    }

    /**
     * 绘制线标注
     */
    private fun drawTextLine(mGeometry: Geometry?){
        var lengthTotal = 0.00
        if(mGeometry == null){
            geojsonTextLineSource.featureCollection(FeatureCollection.fromJson("{\n" +
                    "  \"type\":\"FeatureCollection\",\n" +
                    "  \"features\":[\n" +
                    "    \n" +
                    "    ]\n" +
                    "  }"))
            mSketchHelpView.setLengthMsg(lengthTotal)
            return
        }
        var lineStrings:ArrayList<LineString> = Transformation.boxGeometry2TwoPointLineString(mGeometry)
        var features = arrayListOf<Feature>()

        lineStrings.forEachIndexed(){ index, lineString ->
            val fromGeometry = Feature.fromGeometry(lineString)
            var length = TurfMeasurement.length(lineString, TurfConstants.UNIT_METERS)
            length = MathUtil.double2HALF_UP(length, 2)
            lengthTotal = lengthTotal+length
            fromGeometry.addNumberProperty("length", length)
            fromGeometry.addStringProperty("unit", "m")

            features.add(fromGeometry)


        }
        mSketchHelpView.setLengthMsg(MathUtil.double2HALF_UP(lengthTotal, 2))
        val fromFeatures = FeatureCollection.fromFeatures(features)
        geojsonTextLineSource.featureCollection(fromFeatures)
    }
    override fun cleanup() {
        super.cleanup()
        mSketchHelpView?.cleanup()
    }



}