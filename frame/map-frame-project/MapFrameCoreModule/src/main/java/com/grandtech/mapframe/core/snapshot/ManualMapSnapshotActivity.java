package com.grandtech.mapframe.core.snapshot;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.grandtech.mapframe.core.R;
import com.grandtech.mapframe.core.maps.IOnMapReady;
import com.grandtech.mapframe.core.rules.Rules;
import com.grandtech.mapframe.core.snapshot.bean.SnapParam;
import com.grandtech.mapframe.core.snapshot.view.SquareMapView;
import com.grandtech.mapframe.core.util.BitmapUtil;
import com.grandtech.mapframe.core.util.FeatureSet;
import com.grandtech.mapframe.core.util.StyleJsonBuilder;
import com.grandtech.mapframe.core.util.Transformation;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.Geometry;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdate;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.snapshotter.MapSnapshotter;
import com.mapbox.mapboxsdk.style.expressions.Expression;
import com.mapbox.mapboxsdk.style.layers.PropertyFactory;
import com.mapbox.mapboxsdk.style.layers.RasterLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.mapboxsdk.style.sources.RasterSource;
import com.mapbox.mapboxsdk.style.sources.Source;
import com.mapbox.mapboxsdk.style.sources.TileSet;

import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName ManualMapSnapshotActivity
 * @Description TODO 手动截图
 * @Author: fs
 * @Date: 2021/8/3 15:27
 * @Version 2.0
 */
public class ManualMapSnapshotActivity extends AppCompatActivity implements OnMapReadyCallback, MapboxMap.SnapshotReadyCallback, Rules {

    public static final String SNAPPARAM = "SNAPPARAM";

    public static final String STYLEJSON = "STYLEJSON";

    public static final String RESULT_PATH = "RESULT_PATH";

    private SnapParam mSnapParam;

    private JsonObject mStyleJson;

    private SquareMapView mSquareMapView;

    private ImageButton mTakePic;

    private MapboxMap mMapboxMap;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gmmfc_snapshot_manal_activity);
        setEvent();
        initData();
        mSquareMapView.getMapAsync(this);
    }

    private void initData() {
        String json = getIntent().getStringExtra(SNAPPARAM);
        String styleJson = getIntent().getStringExtra(STYLEJSON);
        if(json == null||styleJson ==null ){
            Intent intent = new Intent();
            intent.putExtra("code", "参数错误");
            setResult(Activity.RESULT_CANCELED, intent);
            finish();
        }
        mSnapParam = SnapParam.fromJson(json);
        JsonParser jsonParser = new JsonParser();
        mStyleJson = jsonParser.parse(styleJson).getAsJsonObject();
        creatMapSnapshotter(mSnapParam);
    }

    private void setEvent() {
        mTakePic = findViewById(R.id.gmmfc_takePic);
        mSquareMapView = findViewById(R.id.gmmfc_squareBoxMapView);
        mTakePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mMapboxMap!= null) {
                    mMapboxMap.snapshot(ManualMapSnapshotActivity.this);
                }
            }
        });
    }

    @Override
    public void onMapReady(@NonNull MapboxMap mapboxMap) {
        mMapboxMap = mapboxMap;
        Style.Builder builder =new Style.Builder();
        builder.fromJson(mStyleJson.toString());
        mapboxMap.setStyle(builder, new Style.OnStyleLoaded() {
            @Override
            public void onStyleLoaded(@NonNull Style style) {
                mapboxMap.setMaxZoomPreference(17.4);// 地图最大放大级别
                LatLngBounds latLngBounds = mSnapParam.getLatLngBounds();
                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(latLngBounds,
                        12);

                mapboxMap.animateCamera(cameraUpdate, 300);
            }
        });
    }

    /**
     * 生成快照
     * @param snapParam
     */
    private void creatMapSnapshotter(SnapParam snapParam){
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

        mSquareMapView.setWarter(snapParam.getWaterMark());
    }

    /**
     * 装配Style
     * @param id dourceId
     * @param geojson
     * @return
     */
    private JsonObject assembleStyle(String id, String geojson) {
        try {
            JsonParser jsonParser = new JsonParser();
            JsonObject sources = mStyleJson.getAsJsonObject("sources");
            JsonObject dkSource = sources.getAsJsonObject(id);
            dkSource.add("data", jsonParser.parse(geojson));
            return mStyleJson;
        } catch (JsonParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void onSnapshotReady(@NonNull Bitmap snapshot) {
        String zltPath = mSnapParam.getFilePath();
        if (zltPath.equals("") || zltPath == null) {
            snapshot.recycle();
           return;
        }
        Bitmap squareBitmap = BitmapUtil.bitmapCropWithRect(snapshot, mSquareMapView.getSquareRect());
        squareBitmap = BitmapUtil.addMark2BitmapEx(squareBitmap, mSnapParam.getWaterMark(),15);
        BitmapUtil.bitmap2File(squareBitmap, zltPath,100);
        setResult(Activity.RESULT_OK, getIntent().putExtra(RESULT_PATH, zltPath));
        finish();
    }

    @Override
    public void onResume() {
        super.onResume();
        mSquareMapView.onResume();
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
    public void onPause() {
        super.onPause();
        mSquareMapView.onPause();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mSquareMapView.onSaveInstanceState(outState);
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
