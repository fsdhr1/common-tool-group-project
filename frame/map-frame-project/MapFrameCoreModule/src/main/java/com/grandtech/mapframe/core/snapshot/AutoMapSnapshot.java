package com.grandtech.mapframe.core.snapshot;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Rect;
import android.os.Environment;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.grandtech.mapframe.core.enumeration.GeometryType;
import com.grandtech.mapframe.core.offline.DbCommand;
import com.grandtech.mapframe.core.rules.Rules;
import com.grandtech.mapframe.core.snapshot.bean.SnapParam;
import com.grandtech.mapframe.core.util.BitmapUtil;
import com.grandtech.mapframe.core.util.FeatureSet;
import com.grandtech.mapframe.core.util.FileUtil;
import com.grandtech.mapframe.core.util.MeasureScreen;
import com.grandtech.mapframe.core.util.MeasureUtil;
import com.grandtech.mapframe.core.util.Pro4jUtil;
import com.grandtech.mapframe.core.util.StyleJsonBuilder;
import com.grandtech.mapframe.core.util.Transformation;
import com.grandtech.mapframe.core.util.TransformationForTurf;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdate;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.snapshotter.MapSnapshot;
import com.mapbox.mapboxsdk.snapshotter.MapSnapshotter;
import com.mapbox.turf.TurfMeasurement;
import com.mapbox.turf.TurfTransformation;
import com.vividsolutions.jts.geom.Coordinate;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * @ClassName AutoSnapshot
 * @Description TODO 自动生成地图快照，可批量
 * @Author: fs
 * @Date: 2021/7/29 13:20
 * @Version 2.0
 */
public class AutoMapSnapshot implements Rules {

    private ICallBack mICallBack;
    private Context context;

    private Rect snapRect;
    private Rect mapRect;
    private Rect padRect;
    //当设备的像素密度为2时，pading宽度默认为200
    private int padWidth = 200;
    //当设备屏幕密度为2时，pading宽度默认为200
    private float baseDensity = 2;

    private JsonObject mStyleJson;
    private int size ;
    private AtomicInteger index=new AtomicInteger(0); ;
    private List<SnapParam> mSnapParams;
    private  MapboxMap mMapboxMap;
    private List<MapSnapshotter> snapshotters = new ArrayList<>();
    //同时生成的数量，20代表同时生成20个，生成完成后会继续执行
    private int threshold = 2;

    public static AutoMapSnapshot create(@NonNull Context context , @NonNull String styleJson, @NonNull ICallBack iCallBack){
        return new AutoMapSnapshot(context,styleJson,iCallBack);
    }

    public AutoMapSnapshot( Context context ,String styleJson,ICallBack iCallBack) {
        this.mICallBack = iCallBack;
        this.context = context;
        //复制一个stylejson对象
        JsonParser jsonParser = new JsonParser();
        mStyleJson = jsonParser.parse(styleJson).getAsJsonObject();
        int width = MeasureScreen.getScreenWidth(context);
        int height = MeasureScreen.getAppDisplayHeight(context);

        int _width = MeasureScreen.px2dip(context, width);
        int _height = MeasureScreen.px2dip(context, height);

        float density = context.getResources().getDisplayMetrics().density;
        padWidth = Math.round(density / baseDensity * padWidth);

        this.mapRect = new Rect(0, 0, _width, _height);
        this.padRect = new Rect(0, (height - width) / 2, width, (height + width) / 2);
        this.snapRect = new Rect(0, (_height - _width) / 2, _width, (_height + _width) / 2);

    }

    /**
     * 批量生成快照
     * @param snapParams
     * @param mapboxMap
     */
    public AutoMapSnapshot batchSnapShot(List<SnapParam> snapParams, MapboxMap mapboxMap) {
        if(snapParams == null||mapboxMap == null){
            return this;
        }
        size = snapParams.size();
        mSnapParams= snapParams;
        mMapboxMap = mapboxMap;
        int total = index.get() == 0?0:index.get();
        for(int i = total;i<total+threshold&&i<size;i++){
            creatMapSnapshotter(snapParams.get(i));
        }
        return this;
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
        LatLngBounds latLngBounds = snapParam.getLatLngBounds();
        double wd = latLngBounds.getLongitudeSpan();
        double ht = latLngBounds.getLatitudeSpan();
        int paddingLeft = (wd > ht) ? padWidth : 0;
        int paddingTop = (wd > ht) ? 0 : padRect.top + padWidth;
        int paddingRight = (wd > ht) ? padWidth : 0;
        int paddingBottom = (wd > ht) ? 0 : padRect.top + padWidth;
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(latLngBounds,
        paddingLeft,
                paddingTop,
                paddingRight,
                paddingBottom);
        // 获取当前相机位置
        CameraPosition cameraPosition = cameraUpdate.getCameraPosition(mMapboxMap);
        if (cameraPosition != null && cameraPosition.zoom >= 17) {
            CameraPosition.Builder builder = new CameraPosition.Builder(cameraPosition);
            builder.zoom(16.9);
            cameraPosition = builder.build();
         }
        MapSnapshotter.Options options = new MapSnapshotter.Options(mapRect.width(), mapRect.height())
                .withLogo(false)
                 .withCameraPosition(cameraPosition)
                .withStyleJson(mStyleJson.toString());
        MapSnapshotter snapShotter = new MapSnapshotter(context, options);
        snapshotters.add(snapShotter);
        snapShotter.start(new ScreenShot(snapParam), new MapSnapshotter.ErrorHandler() {
            @Override
            public void onError(String error) {
                if(mICallBack!=null){
                    mICallBack.onError(null);
                }
            }
        });
    }

    public void cancel() {
        for (MapSnapshotter snapshotter : snapshotters) {
            snapshotter.cancel();
        }
        snapshotters.clear();
    }

    public class ScreenShot implements MapSnapshotter.SnapshotReadyCallback {

       private SnapParam mSnapParam;

        public ScreenShot(SnapParam snapParam) {
            this.mSnapParam = snapParam;
        }

        @Override
        public void onSnapshotReady(MapSnapshot snapshot) {
            new DbCommand<String>() {
                @Override
                protected String doInBackground() {
                    Bitmap bitmap = snapshot.getBitmap();
                    try {
                        Bitmap squareBitmap = BitmapUtil.bitmapCropWithRect(bitmap, snapRect);
                        Bitmap markBitmap = BitmapUtil.addMark2BitmapEx(squareBitmap, mSnapParam.getWaterMark(), 15);
                        if(mSnapParam.getFilePath()!=null&&!mSnapParam.getFilePath().equals("")){
                            BitmapUtil.bitmap2File(markBitmap, mSnapParam.getFilePath(), 100);
                        }else {
                            mSnapParam.setBase64(BitmapUtil.bitmapToBase64(markBitmap));
                        }

                        if (markBitmap != null && !markBitmap.isRecycled()) {
                            markBitmap.recycle();
                        }
                        if (mICallBack != null) {

                            mICallBack.onNext(mSnapParam);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.e("gmbgl-AutoMapSnapshot","错误");
                        bitmap.recycle();
                        if (mICallBack != null) {
                            mICallBack.onError(e);
                        }
                    }
                    index.getAndIncrement();
                    Log.i("gmbgl-AutoMapSnapshot","完成"+index);
                   return "完成";
                }

                @Override
                protected void onPostExecute(String result) {
                    super.onPostExecute(result);
                    if(index.get()%threshold == 0){
                        cancel();
                        batchSnapShot(mSnapParams,mMapboxMap);
                    }
                    if (index.get() == size&&mICallBack != null) {
                        mICallBack.complete();
                        Log.i("gmbgl-AutoMapSnapshot","完成");
                    }
                }
            }.execute();
        }
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
           // Log.i(index+"gmbgl_test2",mStyleJson.toString());
            return mStyleJson;
        } catch (JsonParseException e) {
            if (mICallBack != null) {
                mICallBack.onError(e);
            }
            e.printStackTrace();
        //    Log.i("gmbgl_test2",mStyleJson.toString());
            return null;
        }
    }

    public interface ICallBack {
        /**
         * 每张快照生成后回调
         * @param snapParam
         */
        void onNext(SnapParam snapParam);

        /**
         * 产生异常回调
         * @param e
         */
        void onError(Throwable e);

        /***
         * 所有快照生产完后回调
         */
        void complete();

    }
}
