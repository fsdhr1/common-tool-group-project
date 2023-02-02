package com.gradtech.mapframev10.core.marker;

import android.graphics.PointF;
import android.view.View;

import androidx.annotation.NonNull;

import com.gradtech.mapframev10.core.maps.GMapView;
import com.gradtech.mapframev10.core.rules.Rules;
import com.mapbox.geojson.Point;
import com.mapbox.maps.Projection;
import com.mapbox.maps.ScreenCoordinate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


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
    private Point latLng;
    private GMapView projection;
    private OnPositionUpdateListener onPositionUpdateListener;


    private Map<View,OnClickListener> hasClickViews = new HashMap<>();
    /**
     * 设置marker显示的x轴偏移量单位：px,默认左上角为原点
     */
    private int offsetX;
    /**
     * 设置marker显示的y轴偏移量单位：px,默认左上角为原点
     */
    private int offsetY;

    private boolean avoid = true;

    public boolean isAvoid() {
        return avoid;
    }

    public void setAvoid(boolean avoid) {
        this.avoid = avoid;
    }

    /**
     * Create a MarkerView
     *
     * @param markerId MarkerView的标识
     * @param latLng   latitude-longitude pair
     * @param view     an Android SDK View
     */
    public MarkerView(String markerId, @NonNull Point latLng, @NonNull View view) {
        this.markerId = markerId;
        this.latLng = latLng;
        this.view = view;
    }

    /**
     * @param onClickListener
     */
    public void setOnClickListener(int id, OnClickListener onClickListener) {
        View viewById = view.findViewById(id);
        if (viewById == null) {
            return;
        }
        if (!hasClickViews.containsKey(viewById)) {
            hasClickViews.put(viewById,onClickListener);
        }

        viewById.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OnClickListener onClickListener1 = hasClickViews.get(viewById);
                if(onClickListener1!=null){
                    onClickListener1.onclick(viewById,MarkerView.this);
                }
            }
        });
        viewById.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                OnClickListener onClickListener1 = hasClickViews.get(viewById);
                if(onClickListener1!=null){
                    onClickListener1.onLongCick(viewById,MarkerView.this);
                }
                return true;
            }
        });
    }

    /**
     *
     */
    public void removeOnClickListener(int id) {
        View viewById = view.findViewById(id);
        if (viewById == null) {
            return;
        }
        viewById.setOnClickListener(null);
        viewById.setOnLongClickListener(null);
        hasClickViews.remove(viewById);
    }


    public interface OnClickListener {
        void onclick(View view,MarkerView markerView);

        void onLongCick(View view,MarkerView markerView);
    }

    /**
     * Update the location of the MarkerView on the map.
     * <p>
     * Provided as a latitude-longitude pair.
     * </p>
     *
     * @param latLng latitude-longitude pair
     */
    public void setLatLng(@NonNull Point latLng) {
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
        ScreenCoordinate onUpdate(ScreenCoordinate coordinate);
    }

    void setProjection(GMapView projection) {
        this.projection = projection;
    }

    public View getView() {
        return view;
    }

    void update() {
        if(projection!=null){
            ScreenCoordinate point = projection.getMapboxMap().pixelForCoordinate(latLng);
            if (onPositionUpdateListener != null) {
                point = onPositionUpdateListener.onUpdate(point);
            }
            view.setX((float) point.getX() + offsetX);
            view.setY((float) point.getY() + offsetY);
        }

    }
}
