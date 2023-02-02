package com.gradtech.mapframev10.core.snapshot;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;

import com.gradtech.mapframev10.R;
import com.gradtech.mapframev10.core.rules.Rules;
import com.gradtech.mapframev10.core.select.SelectSetting;
import com.gradtech.mapframev10.core.snapshot.bean.SnapParam;
import com.gradtech.mapframev10.core.snapshot.view.SquareMapView;
import com.gradtech.mapframev10.core.util.BitampUtil;
import com.gradtech.mapframev10.core.util.FeatureSet;
import com.gradtech.mapframev10.core.util.StyleJsonBuilder;
import com.gradtech.mapframev10.core.util.Transformation;
import com.mapbox.bindgen.Value;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.Point;
import com.mapbox.maps.CameraOptions;
import com.mapbox.maps.CoordinateBounds;
import com.mapbox.maps.EdgeInsets;
import com.mapbox.maps.MapView;
import com.mapbox.maps.MapboxMap;
import com.mapbox.maps.Style;
import com.mapbox.maps.extension.observable.eventdata.MapLoadingErrorEventData;
import com.mapbox.maps.extension.style.StyleExtensionImpl;
import com.mapbox.maps.extension.style.layers.LayerUtils;
import com.mapbox.maps.extension.style.layers.generated.FillLayer;
import com.mapbox.maps.extension.style.layers.generated.LineLayer;
import com.mapbox.maps.extension.style.sources.Source;
import com.mapbox.maps.extension.style.sources.SourceUtils;
import com.mapbox.maps.extension.style.sources.generated.GeoJsonSource;
import com.mapbox.maps.extension.style.sources.generated.VectorSource;
import com.mapbox.maps.plugin.Plugin;
import com.mapbox.maps.plugin.delegates.listeners.OnMapLoadErrorListener;
import com.mapbox.maps.plugin.gestures.GesturesPluginImpl;
import com.mapbox.maps.plugin.logo.LogoViewPlugin;


import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Vector;

/**
 * @ClassName ManualMapSnapshotActivity
 * @Description TODO 手动截图
 * @Author: fs
 * @Date: 2021/8/3 15:27
 * @Version 2.0
 */
public class ManualMapSnapshotActivity extends AppCompatActivity implements  Rules {

    public static final String SNAPPARAM = "SNAPPARAM";

    public static final String STYLEJSON_URI = "STYLEJSONURI";

    public static final String RESULT_PATH = "RESULT_PATH";

    private SnapParam mSnapParam;

    private SquareMapView mSquareMapView;

    private ImageButton mTakePic;

    private MapboxMap mMapboxMap;

    private String uri = "asset://snapshotstyle.json";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gmmfc_snapshot_manal_activity);
        setEvent();
        initData();
        String MAP_OFFLINE_STYLE_JSON ="asset://snapshotstyle.json";
        // String MAP_OFFLINE_STYLE_JSON ="http://gykj123.cn:9035/api/v1/styles/gykj/rJg3x0CN_7";
        StyleExtensionImpl.Builder styleExtension = new StyleExtensionImpl.Builder(MAP_OFFLINE_STYLE_JSON);
        mSquareMapView.getMapboxMap().loadStyle(styleExtension.build(), new Style.OnStyleLoaded() {
            @Override
            public void onStyleLoaded(@NotNull Style style) {
                mMapboxMap = mSquareMapView.getMapboxMap();
                creatMapSnapshotter(mSnapParam);
                LogoViewPlugin plugin = mSquareMapView.getPlugin(Plugin.MAPBOX_LOGO_PLUGIN_ID);
                plugin.setEnabled(false);
                GesturesPluginImpl plugin1 = mSquareMapView.getPlugin(Plugin.MAPBOX_GESTURES_PLUGIN_ID);

                plugin1.setRotateEnabled(false);
                CoordinateBounds latLngBounds = mSnapParam.getLatLngBounds();
                CameraOptions cameraOptions = mMapboxMap.cameraForCoordinateBounds(latLngBounds, new EdgeInsets(50.0, 50.0, 50.0, 50.0), 0.0, 0.0);
                mMapboxMap.setCamera(cameraOptions.toBuilder().zoom(16.0).build());

            }

        }, new OnMapLoadErrorListener() {
            @Override
            public void onMapLoadError(@NotNull MapLoadingErrorEventData mapLoadingErrorEventData) {
                Log.i("getMapboxMap", mapLoadingErrorEventData.toString());
            }
        });
    }

    protected void initData() {
        String json = getIntent().getStringExtra(SNAPPARAM);
        String urii = getIntent().getStringExtra(STYLEJSON_URI);
        if(json == null){
            Intent intent = new Intent();
            intent.putExtra("code", "参数错误");
            setResult(Activity.RESULT_CANCELED, intent);
            finish();
        }
        if(urii!=null){
            uri = urii;
        }
        mSnapParam = SnapParam.fromJson(json);

    }

    protected void setEvent() {
        mTakePic = findViewById(R.id.gmmfc_takePic);
        mSquareMapView = findViewById(R.id.gmmfc_squareBoxMapView);
        mTakePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mMapboxMap!= null) {
                    mSquareMapView.snapshot(new MapView.OnSnapshotReady() {
                        @Override
                        public void onSnapshotReady(@Nullable Bitmap bitmap) {
                            String zltPath = mSnapParam.getFilePath();
                            if (zltPath.equals("") || zltPath == null) {
                                bitmap.recycle();
                                return;
                            }
                            Bitmap squareBitmap = BitampUtil.bitmapCropWithRect(bitmap, mSquareMapView.getSquareRect());
                            squareBitmap = BitampUtil.addMark2BitmapEx(squareBitmap, mSnapParam.getWaterMark(),12);
                            BitampUtil.bitmap2File(squareBitmap, zltPath,100);
                            setResult(Activity.RESULT_OK, getIntent().putExtra(RESULT_PATH, zltPath));
                            finish();
                        }
                    });
                }
            }
        });
    }



    /**
     * 生成快照
     * @param snapParam
     */
    protected void creatMapSnapshotter(SnapParam snapParam){
      /*  Feature feature = snapParam.getFeature();
        if(feature!=null){
            String symbolCollect = Transformation.geoJson2NodeSymbol(feature, "");
            assembleStyle(StyleJsonBuilder.DK_SOURCE,feature.toJson());
            assembleStyle(StyleJsonBuilder.SYM_SOURCE,symbolCollect);
        }
        List<FeatureSet> featureSets = snapParam.getFeatureSets();
        if(featureSets != null){
            for (FeatureSet featureSet : featureSets) {
                assembleStyle(featureSet.getSourceId(),featureSet.getGeoJson());
            }
        }

        mSquareMapView.setWarter(snapParam.getWaterMark());*/
        Feature feature = snapParam.getFeature();
        if(feature!=null){
            String symbolCollect = Transformation.geoJson2NodeSymbol(feature, "");
            assembleStyle(StyleJsonBuilder.DK_SOURCE,feature.toJson());
            assembleStyle(StyleJsonBuilder.SYM_SOURCE,symbolCollect);
        }
        List<FeatureSet> featureSets = snapParam.getFeatureSets();
        if(featureSets != null){
            for (FeatureSet featureSet : featureSets) {
                assembleStyle(featureSet.getSourceId(),featureSet.getGeoJson());
            }
        }

        /*LineLayer lineLayer = new LineLayer("qwewqe",StyleJsonBuilder.DK_SOURCE);
        lineLayer.lineColor(Color.RED);
        lineLayer.lineOpacity(1.0);
        lineLayer.lineWidth(3.0);
        LayerUtils.addLayer(mMapboxMap.getStyle(),lineLayer);*/
    }

    /**
     * 装配Style
     * @param id dourceId
     * @param geojson
     * @return
     */
    protected void assembleStyle(String id, String geojson) {
        try {
            GeoJsonSource source = new GeoJsonSource.Builder(id).feature(Feature.fromJson(geojson)).build();
            source.data(geojson);
            SourceUtils.addSource(mMapboxMap.getStyle(),source);
        } catch (JsonParseException e) {
            e.printStackTrace();

        }
    }



    @Override
    protected void onStart() {
        super.onStart();
        mSquareMapView.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mSquareMapView.onStop();
    }



    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        mSquareMapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mSquareMapView.onLowMemory();
    }


}
