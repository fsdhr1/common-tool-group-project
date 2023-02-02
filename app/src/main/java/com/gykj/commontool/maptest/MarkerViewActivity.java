package com.gykj.commontool.maptest;

import android.animation.ValueAnimator;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.blankj.utilcode.util.SizeUtils;
import com.grandtech.mapframe.core.GMap;
import com.grandtech.mapframe.core.editor.gc.GraphicSetting;
import com.grandtech.mapframe.core.enumeration.GeometryType;
import com.grandtech.mapframe.core.event.IMapEvent;
import com.grandtech.mapframe.core.layer.IDoneListenerCallBack;
import com.grandtech.mapframe.core.maps.GMapView;
import com.grandtech.mapframe.core.maps.IOnMapReady;
import com.grandtech.mapframe.core.marker.MarkerView;

import com.grandtech.mapframe.core.util.StringEngine;
import com.gykj.commontool.R;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.Polygon;
import com.mapbox.mapboxsdk.annotations.Polyline;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.Style;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;


/**
 * @ClassName MarkerViewActivity
 * @Description TODO
 * @Author: fs
 * @Date: 2021/8/10 10:17
 * @Version 2.0
 */
public class MarkerViewActivity extends AppCompatActivity implements IOnMapReady, IMapEvent {
    private GMapView gMapView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //在项目中使用的时候这句话要放到applacation中初始化
        GMap.getInstance(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_marker);
        gMapView = findViewById(R.id.mapView);
        gMapView.onCreate(savedInstanceState);
        gMapView.init(this);
    }

    @Override
    public void onBoxMapReady(MapboxMap mapboxMap) {
        String url ="http://gykj123.cn:9035/api/v1/styles/gykj/rJg3x0CN_7";
        mapboxMap.setStyle(url, new Style.OnStyleLoaded() {
            @Override
            public void onStyleLoaded(@NonNull Style style) {
                gMapView.setMapEvent(MarkerViewActivity.this);
            }

        });
    }

    View createMarkerView(){
        View customView = LayoutInflater.from(this).inflate(R.layout.activity_map_marker_view, null);
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(WRAP_CONTENT,WRAP_CONTENT);
        customView.setLayoutParams(layoutParams);
        LinearLayout llhv = customView.findViewById(R.id.ll_hv);
        llhv.setVisibility(View.INVISIBLE);
        LinearLayout lltriangle = customView.findViewById(R.id.ll_triangle);
        lltriangle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                llhv.setVisibility(llhv.getVisibility() == View.INVISIBLE?View.VISIBLE:View.INVISIBLE);
            }
        });
      /*  ImageView icon = customView.findViewById(R.id.imageview);
        FrameLayout animationView = customView.findViewById(R.id.animation_layout);
        icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ValueAnimator anim = ValueAnimator.ofInt(animationView.getMeasuredWidth(), 350);
                anim.setInterpolator(new AccelerateDecelerateInterpolator() );
                anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        int val = (int)animation.getAnimatedValue();
                        ViewGroup.LayoutParams layoutParams1 = animationView.getLayoutParams();
                        layoutParams1.width  = val;
                        animationView.setLayoutParams(layoutParams1);
                    }
                });
                anim.setDuration(1250);
                anim.start();
            }
        });*/

       return customView;
    }

    @Override
    public boolean onMapClick(@NonNull LatLng point) {

        View view = createMarkerView();
      /*  ImageView imageView = new ImageView(this);
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(WRAP_CONTENT,WRAP_CONTENT);
        imageView.setImageResource(R.mipmap.camera_module_ic_back);
        imageView.setLayoutParams(layoutParams);*/
        MarkerView markerView =  new MarkerView(StringEngine.get32UUID(),point,view);
        markerView.setOffsetX(-SizeUtils.dp2px(220)/2);
        markerView.setOffsetY(-SizeUtils.dp2px(197));
        gMapView.getMarkerViewManager().addMarker(markerView);
        return false;
    }


    @Override
    public boolean onMapLongClick(@NonNull LatLng point) {
        gMapView.getMarkerViewManager().removeAllMarker();
        return false;
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


}
