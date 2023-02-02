package com.gradtech.mapframev10.core.marker;

import android.graphics.Rect;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.UiThread;


import com.gradtech.mapframev10.core.maps.GMapView;
import com.gradtech.mapframev10.core.rules.Rules;
import com.mapbox.android.gestures.MoveGestureDetector;
import com.mapbox.maps.MapView;
import com.mapbox.maps.MapboxMap;
import com.mapbox.maps.plugin.Plugin;
import com.mapbox.maps.plugin.gestures.GesturesPluginImpl;
import com.mapbox.maps.plugin.gestures.OnMoveListener;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName MarkerViewManager
 * @Description TODO 用来管理MarkerView区别于Marker，MarkerView可以理解为更加灵活的marker可以自定义样式
 * @Author: fs
 * @Date: 2021/8/10 14:59
 * @Version 2.0
 */
public class MarkerViewManager implements OnMoveListener, Rules {

    private GMapView mapView;
    private List<MarkerView> markers = new ArrayList<>();
    private boolean initialised;

    /**
     * Create a MarkerViewManager.
     *
     * @param mapView the MapView used to synchronise views on
     */
    public MarkerViewManager(GMapView mapView) {
        this.mapView = mapView;
    }

    /**
     * Destroys the MarkerViewManager.
     * <p>
     * Should be called before MapView#onDestroy
     * </p>
     */
    @UiThread
    public void onDestroy() {
        markers.clear();
        GesturesPluginImpl gesturesplugin = mapView.getPlugin(Plugin.MAPBOX_GESTURES_PLUGIN_ID);
        gesturesplugin.removeOnMoveListener(this);
        initialised = false;
    }

    /**
     * Add a MarkerView to the map using MarkerView and LatLng.
     *
     * @param markerView the markerView to synchronise on the map
     */
    @UiThread
    public void addMarker(@NonNull MarkerView markerView) {
        if (markers.contains(markerView)) {
            return;
        }

        if (!initialised) {
            initialised = true;
            GesturesPluginImpl gesturesplugin = mapView.getPlugin(Plugin.MAPBOX_GESTURES_PLUGIN_ID);
            gesturesplugin.addOnMoveListener(this);
        }
        markerView.setProjection(mapView);
        mapView.addView(markerView.getView(), 1);
        markers.add(markerView);
        markerView.update();
    }

    /**
     * get a MarkerView from markers of ready Add by markerId.
     *
     * @param markerId the  markerId
     */
    @UiThread
    public MarkerView getMarker(@NonNull String markerId) {
        if (markerId == null) {
            return null;
        }
        for (MarkerView markerView : markers) {
            if (markerId.equals(markerView.getMarkerId())) {
                return markerView;
            }
        }
        return null;
    }

    /**
     * Remove an existing markerView from the map.
     *
     * @param markerView the markerView to be removed from the map
     */
    @UiThread
    public void removeMarker(@NonNull MarkerView markerView) {
        if (!markers.contains(markerView)) {
            return;
        }
        mapView.removeView(markerView.getView());
        markers.remove(markerView);
    }

    @UiThread
    public void removeAllMarker() {
        if (markers == null) {
            return;
        }
        for (MarkerView marker : markers) {
            mapView.removeView(marker.getView());
        }
        markers.clear();
    }

    /**
     * Remove an existing markerView from the map by markerId.
     *
     * @param markerId the markerView to be removed from the map by markerId
     */
    @UiThread
    public void removeMarker(@NonNull String markerId) {
        if (markerId == null) {
            return;
        }
        List<MarkerView> markerView = new ArrayList<>();
        for (MarkerView mv : markers) {
            if (markerId.equals(mv.getMarkerId())) {
                markerView.add(mv);
            }
        }
        if (markerView.size() > 0) {
            for (MarkerView view : markerView) {
                mapView.removeView(view.getView());
                markers.remove(view);
            }

        }
    }

    private void update() {
        for (MarkerView marker : markers) {
            marker.update();
        }
    }

    private void visible(int visible) {
        for (MarkerView marker : markers) {
            marker.getView().setVisibility(visible);
        }
    }

    /**
     * 避让
     */
    private void avoid() {
        List<MarkerView> markerViews = new ArrayList<>();
        for (MarkerView marker : markers) {
            if(marker.isAvoid()){
                markerViews.add(marker);
            }
        }
        for (int i = 0; i < markerViews.size(); i++) {
            boolean b = true;
            for (int j = i + 1; j < markerViews.size(); j++) {
                b = avoidMarkerView(markerViews.get(i), markerViews.get(j));
                if (b) {
                    markerViews.get(i).getView().setVisibility(View.GONE);
                    break;
                }
            }
            if (!b && markerViews.get(i).getView().getVisibility() == View.VISIBLE) {
                markerViews.get(i).getView().setVisibility(View.VISIBLE);
            }
        }
    }

    /**
     * 两个MarkerView避让
     *
     * @param mv1
     * @param mv2
     * @return
     */
    private boolean avoidMarkerView(MarkerView mv1, MarkerView mv2) {
        Rect rectMv1 = new Rect();
        View view1 = mv1.getView();
        view1.getGlobalVisibleRect(rectMv1);
        Rect rectMv2 = new Rect();
        View view2 = mv2.getView();
        view2.getGlobalVisibleRect(rectMv2);
        if (rectMv1.intersect(rectMv2)) {
            if (view1 instanceof ViewGroup || view2 instanceof ViewGroup) {
                List<View> views1 = getVisibleView(view1);
                List<View> views2 = getVisibleView(view2);
                for (View views11 : views1) {
                    for (View views22 : views2) {
                        if (avoidView(views11, views22)) {
                            return true;
                        }
                    }
                }
            } else {
                return true;
            }
        }
        return false;
    }

    private List<View> getVisibleView(View v) {
        List<View> views = new ArrayList<>();
        if (v instanceof ViewGroup) {
            int count = ((ViewGroup) v).getChildCount();
            for (int i = 0; i < count; i++) {
                View view = ((ViewGroup) v).getChildAt(i);
                if (view.getVisibility() == View.VISIBLE) {
                    views.add(view);
                    views.addAll(getVisibleView(view));
                }
            }
        }
        return views;
    }

    private boolean avoidView(View v1, View v2) {
        Rect rectMv1 = new Rect();
        v1.getGlobalVisibleRect(rectMv1);
        Rect rectMv2 = new Rect();
        v2.getGlobalVisibleRect(rectMv2);
        if (rectMv1.intersect(rectMv2)) {
            return true;
        }
        return false;
    }


    @Override
    public boolean onMove(@NotNull MoveGestureDetector moveGestureDetector) {
        Log.i("gmbgl-MarkerView", "onMove");
        //visible(View.VISIBLE);
        update();
        return false;
    }

    @Override
    public void onMoveBegin(@NotNull MoveGestureDetector moveGestureDetector) {
        Log.i("gmbgl-MarkerView", "onMoveBegin");
        // visible(View.GONE);
    }

    @Override
    public void onMoveEnd(@NotNull MoveGestureDetector moveGestureDetector) {
        Log.i("gmbgl-MarkerView", "onMoveEnd");
        //visible(View.VISIBLE);
        avoid();
    }
}
