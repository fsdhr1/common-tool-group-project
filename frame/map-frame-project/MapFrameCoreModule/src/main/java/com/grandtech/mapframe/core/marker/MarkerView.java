package com.grandtech.mapframe.core.marker;

import android.graphics.PointF;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;

import com.grandtech.mapframe.core.rules.Rules;
import com.grandtech.mapframe.core.util.MeasureUtil;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.Projection;

/**
 * @ClassName MarkerView
 * @Description TODO MarkerView可以理解为更加灵活的marker可以自定义样式
 * @Author: fs
 * @Date: 2021/8/10 15:00
 * @Version 2.0
 */
public class MarkerView implements Rules {

    private String markerId;
    private final View view;
    private LatLng latLng;
    private Projection projection;
    private OnPositionUpdateListener onPositionUpdateListener;
    /**
     * 设置marker显示的x轴偏移量单位：px,默认左上角为原点
     */
    private int offsetX;
    /**
     * 设置marker显示的y轴偏移量单位：px,默认左上角为原点
     */
    private int offsetY;


    /**
     * Create a MarkerView
     *
     * @param markerId MarkerView的标识
     * @param latLng   latitude-longitude pair
     * @param view     an Android SDK View
     */
    public MarkerView(String markerId, @NonNull LatLng latLng, @NonNull View view) {
        this.markerId = markerId;
        this.latLng = latLng;
        this.view = view;
    }

    /**
     * Update the location of the MarkerView on the map.
     * <p>
     * Provided as a latitude-longitude pair.
     * </p>
     *
     * @param latLng latitude-longitude pair
     */
    public void setLatLng(@NonNull LatLng latLng) {
        this.latLng = latLng;
        update();
    }

    public String getMarkerId() {
        return markerId;
    }

    public void setMarkerId(String markerId) {
        this.markerId = markerId;
    }

    public int getOffsetX() {
        return offsetX;
    }

    /**
     * 设置marker显示的x轴偏移量单位：px,默认左上角为原点
     */
    public void setOffsetX(int offsetX) {
        this.offsetX = offsetX;
    }

    public int getOffsetY() {
        return offsetY;
    }

    /**
     * 设置marker显示的y轴偏移量单位：px,默认左上角为原点
     */
    public void setOffsetY(int offsetY) {
        this.offsetY = offsetY;
    }

    /**
     * Set a callback to be invoked when position placement is calculated.
     * <p>
     * Can be used to offset a MarkerView on screen.
     * </p>
     *
     * @param onPositionUpdateListener callback to be invoked when position placement is calculated
     */
    public void setOnPositionUpdateListener(OnPositionUpdateListener onPositionUpdateListener) {
        this.onPositionUpdateListener = onPositionUpdateListener;
    }

    /**
     * Callback definition that is invoked when position placement of a MarkerView is calculated.
     */
    public interface OnPositionUpdateListener {
        PointF onUpdate(PointF pointF);
    }

    void setProjection(Projection projection) {
        this.projection = projection;
    }

    View getView() {
        return view;
    }

    void update() {
        PointF point = projection.toScreenLocation(latLng);
        if (onPositionUpdateListener != null) {
            point = onPositionUpdateListener.onUpdate(point);
        }
        view.setX(point.x + offsetX);
        view.setY(point.y + offsetY);
    }
}
