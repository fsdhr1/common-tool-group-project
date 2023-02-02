package com.gradtech.mapframev10.core.marker

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.annotation.UiThread
import androidx.core.content.ContextCompat
import com.google.gson.GsonBuilder
import com.gradtech.mapframev10.R
import com.gradtech.mapframev10.core.maps.GMapView
import com.gradtech.mapframev10.core.rules.Rules
import com.gradtech.mapframev10.core.util.BitampUtil
import com.mapbox.geojson.Point
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.*
import com.mapbox.maps.viewannotation.ViewAnnotationManager

/**
 * @ClassName MarkerManager
 * @Description TODO 基于AnnotationManager的marker管理类，可以更轻松便捷的使用Annotation
 * @Author: fs
 * @Date: 2022/12/30 13:37
 * @Version 2.0
 */
class MarkerManager : Rules {

    private lateinit var mGMapView: GMapView

    lateinit var pointManager: PointAnnotationManager

    private var markers = linkedMapOf<String, PointAnnotation>()

    private var simpleMarkers = linkedMapOf<String, PointAnnotation>()

    private var icons = arrayListOf<Bitmap>()

    private var onAnnotationClickListeners = arrayListOf<OnPointAnnotationClickListener>()

    private var onPointAnnotationLongClickListeners = arrayListOf<OnPointAnnotationLongClickListener>()

    private lateinit var viewAnnotationManager: ViewAnnotationManager


    constructor(mapview: GMapView) {
        mGMapView = mapview;
        val annotations = mGMapView.annotations
        pointManager = annotations.createPointAnnotationManager().apply {
            // listen for click event
            addClickListener(
                    OnPointAnnotationClickListener {
                        if(intercept(it)){
                           return@OnPointAnnotationClickListener true
                        }
                        for (onAnnotationClickListener in onAnnotationClickListeners) {
                            if(onAnnotationClickListener.onAnnotationClick(it)){
                                return@OnPointAnnotationClickListener true
                            }
                        }
                        return@OnPointAnnotationClickListener false
                    }
            )
            addLongClickListener(OnPointAnnotationLongClickListener {
                for (onAnnotationClickListener in onPointAnnotationLongClickListeners) {
                    if(onAnnotationClickListener.onAnnotationLongClick(it)){
                        return@OnPointAnnotationLongClickListener  true
                    }
                }
                return@OnPointAnnotationLongClickListener  false
            })
        }

        viewAnnotationManager = mapview.viewAnnotationManager;
    }


    fun addClickListener(onAnnotationClickListener: OnPointAnnotationClickListener) {
        onAnnotationClickListeners.add(onAnnotationClickListener)
    }

    fun addLongClickListener(onPointAnnotationLongClickListener: OnPointAnnotationLongClickListener) {
        onPointAnnotationLongClickListeners.add(onPointAnnotationLongClickListener)
    }

    fun removeClickListener(onAnnotationClickListener: OnPointAnnotationClickListener){
        onAnnotationClickListeners.remove(onAnnotationClickListener);
    }

    fun removeLongClickListener(onPointAnnotationLongClickListener: OnPointAnnotationLongClickListener) {
        onPointAnnotationLongClickListeners.remove(onPointAnnotationLongClickListener)
    }

    fun addIcon(bitmapId: String, bitmap: Bitmap) {
        if (!icons.contains(bitmap)) {
            mGMapView.getMapboxMap().getStyle()?.addImage(bitmapId, bitmap)
            icons.add(bitmap)
        }

    }


    fun addMarker(markerId: String, point: Point, bitmap: Bitmap, data: Any): PointAnnotation {
        val gson = GsonBuilder().setDateFormat("yyyy-MM-dd' 'HH:mm:ss").create()
        val jsonElement = gson.toJsonTree(data)
        val circleOptions: PointAnnotationOptions = PointAnnotationOptions()
                .withPoint(point)
                .withIconImage(bitmap)
                .withData(jsonElement)


        val create: PointAnnotation = pointManager.create(circleOptions)

        markers.put(markerId, create);
        if (!icons.contains(bitmap)) {
            icons.add(bitmap)
        }

        return create;
    }


    fun addMarker(markerId: String, point: Point, bitmap: Bitmap, data: Any, text: String, iconOffset: List<Double>, textColor: String): PointAnnotation {
        val gson = GsonBuilder().setDateFormat("yyyy-MM-dd' 'HH:mm:ss").create()
        val jsonElement = gson.toJsonTree(data)
        val circleOptions: PointAnnotationOptions = PointAnnotationOptions()
                .withPoint(point)
                .withIconImage(bitmap)
                .withData(jsonElement)
                .withIconOffset(iconOffset)
                .withTextColor(textColor)
                .withTextField(text)
                .withTextSize(12.0)

        var fonts = arrayListOf<String>();
        fonts.add("FZHei-B01S Regular");
        pointManager.textFont = fonts
        val create: PointAnnotation = pointManager.create(circleOptions)
        markers.put(markerId, create);
        if (!icons.contains(bitmap)) {
            icons.add(bitmap)
        }

        return create;
    }


    fun addMarker(point: Point, bitmap: Bitmap, data: Any): PointAnnotation {
        val gson = GsonBuilder().setDateFormat("yyyy-MM-dd' 'HH:mm:ss").create()
        val jsonElement = gson.toJsonTree(data)
        val circleOptions: PointAnnotationOptions = PointAnnotationOptions()
                .withPoint(point)
                .withIconImage(bitmap)
                .withData(jsonElement)

        val create: PointAnnotation = pointManager.create(circleOptions)
        if (!icons.contains(bitmap)) {
            icons.add(bitmap)
        }

        return create;
    }

    fun addMarker(markerId: String, point: Point, bitmapId: String, data: Any): PointAnnotation {
        val gson = GsonBuilder().setDateFormat("yyyy-MM-dd' 'HH:mm:ss").create()
        val jsonElement = gson.toJsonTree(data)
        val circleOptions: PointAnnotationOptions = PointAnnotationOptions()
                .withPoint(point)
                .withIconImage(bitmapId)
                .withData(jsonElement)
        val create: PointAnnotation = pointManager.create(circleOptions)
        markers.put(markerId, create);
        return create;
    }


    fun removeMarker(pointAnnotation: PointAnnotation) {
        pointManager.delete(pointAnnotation)
        val entries = markers.entries
        for (entry in entries) {
            if (entry.value.equals(pointAnnotation)) {
                markers.remove(entry.key)
                mGMapView.getMarkerViewManager().removeMarker(entry.key)
                simpleMarkers.remove(entry.key)
                break
            }
        }
    }

    fun removeMarker(markerId: String) {
        val get: PointAnnotation? = markers.get(markerId)
        if (get != null) {
            pointManager.delete(get)
            markers.remove(markerId)
            if (simpleMarkers.containsKey(markerId)) {
                mGMapView.getMarkerViewManager().removeMarker(markerId)
                simpleMarkers.remove(markerId)
            }
        }
    }

    /**
     * 删除所有marker
     */
    fun removeAllMarker() {
        pointManager.deleteAll()
        val entries = simpleMarkers.entries
        for (entry in entries) {
            mGMapView.getMarkerViewManager().removeMarker(entry.key)
        }
        markers.clear()
        simpleMarkers.clear()
    }

    /**
     * 模拟9.x版本marker的默认样式
     */
    fun addSimpleAnnotation(sumpleMarkerId: String, point: Point, title: String, content: String, openMsg: Boolean, data: Any): PointAnnotation {
        if (!icons.contains(ICON_DEFUNT)) {
            val bitmapDrawable6: Drawable = ContextCompat.getDrawable(mGMapView.context, R.mipmap.gmmfc_v10_ic_location)!!
            val bitmap: Bitmap = BitampUtil.drawableToBitamp(bitmapDrawable6, 28f, 28f)
            addIcon(ICON_DEFUNT, bitmap);
        }
        val pointAnnotation = addMarker(sumpleMarkerId, point, ICON_DEFUNT, data)
        simpleMarkers.put(sumpleMarkerId, pointAnnotation)
        val get = simpleMarkers.get(sumpleMarkerId)
        val view: View = createMarkerView()
        val titleTv = view.findViewById<TextView>(R.id.title)
        titleTv.text = title
        titleTv.paint.isFakeBoldText = true
        val contentTv = view.findViewById<TextView>(R.id.content)
        contentTv.text = content
        val markerView = MarkerView(sumpleMarkerId, get!!.point, view)

        markerView.isAvoid = false;

        Log.i("mbglV10", "" + view.height + "ddd" + view.measuredHeight)
        markerView.offsetX = BitampUtil.dp2px(-75f)
        markerView.offsetY = (BitampUtil.dp2px(-140f))


        mGMapView.getMarkerViewManager().addMarker(markerView);

        if (openMsg) {
            view.visibility = View.VISIBLE
        } else {
            view.visibility = View.GONE
        }
        return pointAnnotation
    }

    private fun intercept(it: PointAnnotation):Boolean {
        val entries = simpleMarkers.entries
        var markerid = "";
        for (entry in entries) {
            if (entry.value.equals(it)) {
                markerid = entry.key
                break
            }
        }
        if (markerid.equals("")) {
            return false
        }
        val marker = mGMapView.getMarkerViewManager().getMarker(markerid)
        if (marker == null) {
            return false
        }
        marker.view.visibility = if (marker.view.visibility == View.GONE) View.VISIBLE else View.GONE
        return true
    }

    private fun createMarkerView(): View {

        val customView: View = LayoutInflater.from(mGMapView.context).inflate(R.layout.annotation_view, null)
        val layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        customView.layoutParams = layoutParams
        return customView
    }

    @UiThread
    fun onDestroy() {
        pointManager.deleteAll()
        markers.clear()
        for (icon in icons) {
            icon.recycle()
        }
        icons.clear()
    }

    companion object {
        private const val ICON_DEFUNT = "gmmfc_location_icon"
    }

}