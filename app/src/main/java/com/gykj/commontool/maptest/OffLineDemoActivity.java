package com.gykj.commontool.maptest;

import android.content.res.Resources;
import android.graphics.PointF;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcel;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.esri.arcgisruntime.internal.jni.CoreByteArray;
import com.grandtech.mapframe.core.GMap;
import com.grandtech.mapframe.core.editor.IToolInterceptor;
import com.grandtech.mapframe.core.editor.gc.GraphicSetting;
import com.grandtech.mapframe.core.enumeration.GeometryType;
import com.grandtech.mapframe.core.event.IMapEvent;
import com.grandtech.mapframe.core.layer.IDoneListenerCallBack;
import com.grandtech.mapframe.core.maps.GMapView;
import com.grandtech.mapframe.core.maps.IOnMapReady;
import com.grandtech.mapframe.core.select.SelectSetting;
import com.grandtech.mapframe.core.select.SelectionSet;
import com.grandtech.mapframe.core.util.FeatureSet;
import com.grandtech.mapframe.core.util.Transformation;
import com.grandtech.mapframe.core.util.TransformationForTurf;
import com.gykj.commontool.R;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.geojson.Feature;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.Polygon;
import com.mapbox.mapboxsdk.annotations.Polyline;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.MapboxMapOptions;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.offline.OfflineManager;
import com.mapbox.mapboxsdk.offline.OfflineRegion;
import com.mapbox.mapboxsdk.offline.OfflineRegionDefinition;
import com.mapbox.mapboxsdk.offline.OfflineRegionError;
import com.mapbox.mapboxsdk.offline.OfflineRegionStatus;
import com.mapbox.mapboxsdk.offline.OfflineTilePyramidRegionDefinition;
import com.mapbox.mapboxsdk.style.layers.CircleLayer;
import com.mapbox.mapboxsdk.style.layers.FillLayer;
import com.mapbox.mapboxsdk.style.layers.Layer;
import com.mapbox.mapboxsdk.style.layers.LineLayer;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @ClassName OffLineDemoActivity
 * @Description TODO
 * @Author: fs
 * @Date: 2021/11/16 8:29
 * @Version 2.0
 */
public class OffLineDemoActivity extends AppCompatActivity implements IOnMapReady, IMapEvent, SelectionSet.ISelectSetChange, IToolInterceptor, PermissionsListener {
    private static final String TAG = "OfflineMapFragment";
    private FrameLayout flMainFrame ;
    private GMapView gMapView;
    private Button btn_start;
    private OfflineManager offlineManager;
    private OfflineRegion offlineRegionDownloaded;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        //在项目中使用的时候这句话要放到applacation中初始化
        GMap.getInstance(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_offline);
        flMainFrame = findViewById(R.id.flMainFrame);
        btn_start = findViewById(R.id.btn_start);
        btn_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Map<String, FeatureSet> selectSet = gMapView.getSelectSet();
                List<Feature> features = FeatureSet.convert2Features(selectSet);
                LatLngBounds latLngBounds = TransformationForTurf.features2BoxLatLngBounds(features);
                offlineManager = OfflineManager.getInstance(OffLineDemoActivity.this);
                String url ="http://gykj123.cn:9035/api/v1/styles/gykj/rk0f0ag_K";
                OfflineTilePyramidRegionDefinition offlineTilePyramidRegionDefinition= new OfflineTilePyramidRegionDefinition(url,latLngBounds,0,16, Resources.getSystem().getDisplayMetrics().density);
                String matedate = "测试1";
                offlineManager.setOfflineMapboxTileCountLimit(Long.MAX_VALUE);
                offlineManager.createOfflineRegion(offlineTilePyramidRegionDefinition, matedate.getBytes(), new OfflineManager.CreateOfflineRegionCallback() {
                    @Override
                    public void onCreate(OfflineRegion offlineRegion) {
                        offlineRegionDownloaded = offlineRegion;
                        launchDownload();
                    }

                    @Override
                    public void onError(String error) {
                        Log.e(TAG, "onError message: " + error);
                    }
                });
            }
        });
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
        flMainFrame.addView(gMapView);
    }
    @Override
    public void onBoxMapReady(MapboxMap mapboxMap) {
        String url ="http://gykj123.cn:9035/api/v1/styles/gykj/rk0f0ag_K";
        mapboxMap.setStyle(url, new Style.OnStyleLoaded() {
            @Override
            public void onStyleLoaded(@NonNull Style style) {
                gMapView.setMapEvent(OffLineDemoActivity.this);
                gMapView.initSelection(new SelectSetting(false,false,"县域"));
            }
        });
    }
    @Override
    public boolean doHandle(Object o, String... flags) {
        return false;
    }

    @Override
    public void afterHandle(Object o, String... flags) {

    }
    private void launchDownload() {
        // Set up an observer to handle download progress and
        // notify the user when the region is finished downloading
        offlineRegionDownloaded.setObserver(new OfflineRegion.OfflineRegionObserver() {
            @Override
            public void onStatusChanged(OfflineRegionStatus status) {
                // Compute a percentage
                double percentage = status.getRequiredResourceCount() >= 0
                        ? (100.0 * status.getCompletedResourceCount() / status.getRequiredResourceCount()) :
                        0.0;

                if (status.isComplete()) {
                    // Download complete
                   // endProgress("Region downloaded successfully.");
                    return;
                } else if (status.isRequiredResourceCountPrecise()) {
                    // Switch to determinate state
                   // setPercentage((int) Math.round(percentage));
                }

                // Log what is being currently downloaded
                Log.i(TAG, String.format("%s/%s resources; %s bytes downloaded.",
                        String.valueOf(status.getCompletedResourceCount()),
                        String.valueOf(status.getRequiredResourceCount()),
                        String.valueOf(status.getCompletedResourceSize())));
            }

            @Override
            public void onError(OfflineRegionError error) {
                Log.e(TAG, "onError reason: " + error.getReason());
                Log.e(TAG, "onError message: " + error.getMessage());
            }

            @Override
            public void mapboxTileCountLimitExceeded(long limit) {
                Log.e(TAG, "Mapbox tile count limit exceeded: " + limit);
            }
        });

        // Change the region state
        offlineRegionDownloaded.setDownloadState(OfflineRegion.STATE_ACTIVE);
    }

    @Override
    public boolean onMapClick(@NonNull LatLng point) {
        PointF pointF = gMapView.getMapBoxMap().getProjection().toScreenLocation(point);
        gMapView.selectFeatureByPoint(pointF,null);
        return false;
    }

    @Override
    public boolean onMapLongClick(@NonNull LatLng point) {
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



    @Override
    public boolean beforeSelectSetChange() {
        return false;
    }

    @Override
    public boolean selectSetChangeIng(Feature feature, SelectionSet.SelectStatus selectStatus) {
        return false;
    }

    @Override
    public void selectSetChanged() {

    }

    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {

    }

    @Override
    public void onPermissionResult(boolean granted) {

    }
}
