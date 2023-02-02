package com.gykj.commontool.maptest;

import android.os.Bundle;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.grandtech.mapframe.core.GMap;
import com.grandtech.mapframe.core.maps.GMapView;
import com.grandtech.mapframe.core.maps.IOnMapReady;
import com.grandtech.mapframe.core.net.BoxHttpClient;
import com.grandtech.mapframe.ui.manager.ToolManager;
import com.grandtech.mapframe.ui.manager.WidgetGravityEnum;
import com.grandtech.mapframe.ui.view.IToolView;
import com.grandtech.mapframe.ui.view.ToolGroupLayout;
import com.grandtech.mapframe.ui.view.ToolView;
import com.gykj.commontool.R;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.MapboxMapOptions;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.module.http.HttpRequestUtil;

/**
 * @ClassName MapUIActivityDemo
 * @Description TODO mapUI测试用例
 * @Author: fs
 * @Date: 2021/12/29 14:10
 * @Version 2.0
 */
public class MapUIActivityDemo extends AppCompatActivity implements IOnMapReady {

    GMapView gMapView;
    ToolManager toolManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        //在项目中使用的时候这句话要放到applacation中初始化
        GMap.getInstance(this);
        super.onCreate(savedInstanceState);
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(LinearLayout.VERTICAL);

        MapboxMapOptions mapOptions = new MapboxMapOptions();
        mapOptions.setPrefetchesTiles(true);
        mapOptions.compassEnabled(true);
        mapOptions.rotateGesturesEnabled(false);
        mapOptions.tiltGesturesEnabled(false);
        mapOptions.logoEnabled(false);
        mapOptions.attributionEnabled(false);
        mapOptions.maxZoomPreference(16.99);
        gMapView = new GMapView(this,mapOptions,this);
        gMapView.onCreate(savedInstanceState);
        linearLayout.addView(gMapView);
        setContentView(linearLayout);
    }

    @Override
    public void onBoxMapReady(MapboxMap mapboxMap) {
        String url ="http://gykj123.cn:9035/api/v1/styles/gykj/rJg3x0CN_7";
        mapboxMap.setStyle(url, new Style.OnStyleLoaded() {
            @Override
            public void onStyleLoaded(@NonNull Style style) {
                initMapUI();
            }
        });
    }

    private void initMapUI() {
        toolManager = ToolManager.newInstance();
        toolManager.init(this,gMapView,true);
        ToolView toolView1 = new ToolView(this,"点击按钮", R.mipmap.ic_draw,R.mipmap.ic_draw,true);
        ToolView toolView2 = new ToolView(this,"按下按钮", R.mipmap.ic_draw,R.mipmap.ic_draw_pressed,false);
        ToolGroupLayout toolGroupLayout = new ToolGroupLayout(this);
        toolGroupLayout.addView(toolView1);
        toolGroupLayout.addView(toolView2);
        toolManager.addToolBarView(toolGroupLayout, false,WidgetGravityEnum.LEFT_CENTER);
        toolManager.setOnToolClickListener(new ToolManager.OnToolViewActivateListener() {
            @Override
            public void activate(IToolView toolView) {
                if("点击按钮".equals(toolView.getmToolName())){
                    IToolView toolview = toolManager.getToolViewByName("按下按钮");
                    toolManager.deactivateTool(toolview);
                }
            }

            @Override
            public void deactivate(IToolView toolView) {

            }
        });
    }
}
