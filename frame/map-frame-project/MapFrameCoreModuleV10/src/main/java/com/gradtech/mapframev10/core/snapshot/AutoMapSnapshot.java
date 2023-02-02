package com.gradtech.mapframev10.core.snapshot;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.gradtech.mapframev10.core.rules.Rules;
import com.gradtech.mapframev10.core.snapshot.bean.SnapParam;
import com.gradtech.mapframev10.core.snapshot.custom.DbCommand;
import com.gradtech.mapframev10.core.util.BitampUtil;
import com.gradtech.mapframev10.core.util.FeatureSet;
import com.gradtech.mapframev10.core.util.MeasureScreen;
import com.gradtech.mapframev10.core.util.StyleJsonBuilder;
import com.gradtech.mapframev10.core.util.Transformation;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.Point;
import com.mapbox.geojson.Polygon;
import com.mapbox.maps.CameraOptions;
import com.mapbox.maps.CoordinateBounds;
import com.mapbox.maps.EdgeInsets;
import com.mapbox.maps.Image;
import com.mapbox.maps.MapInitOptions;
import com.mapbox.maps.MapSnapshotInterface;
import com.mapbox.maps.MapSnapshotOptions;
import com.mapbox.maps.MapboxMap;
import com.mapbox.maps.Size;
import com.mapbox.maps.Snapshot;
import com.mapbox.maps.SnapshotCreatedListener;
import com.mapbox.maps.Snapshotter;
import com.mapbox.turf.TurfMeasurement;
import com.mapbox.turf.TurfMeta;

import org.jetbrains.annotations.Nullable;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

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

    private Rect padRect;
    //当设备的像素密度为2时，pading宽度默认为200
    private int padWidth = 200;
    //当设备屏幕密度为2时，pading宽度默认为200
    private float baseDensity = 2;

    private JsonObject mStyleJson;
    private int size ;
    private AtomicInteger index=new AtomicInteger(0); ;
    private List<SnapParam> mSnapParams;
    private MapboxMap mMapboxMap;
    private List<Snapshotter> snapshotters = new ArrayList<>();
    //同时生成的数量，20代表同时生成20个，生成完成后会继续执行
    private int threshold = 2;

    private MapSnapshotOptions mSnapshotMapOptions;

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

       /* int _width = MeasureScreen.px2dip(context, width);
        int _height = MeasureScreen.px2dip(context, height);*/

        float density = context.getResources().getDisplayMetrics().density;
        padWidth = Math.round(density / baseDensity * padWidth);

        //this.mapRect = new Rect(0, 0, _width, _height);
        this.padRect = new Rect(0, (height - width) / 2, width, (height + width) / 2);
       // this.snapRect = new Rect(0, (_height - _width) / 2, _width, (_height + _width) / 2);

        mSnapshotMapOptions = new MapSnapshotOptions.Builder()
                .size(new Size(320.0f, 320.0f))

                .resourceOptions(MapInitOptions.Companion.getDefaultResourceOptions(context)).build();


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
        CoordinateBounds latLngBounds = snapParam.getLatLngBounds();
        double wd = latLngBounds.longitudeSpan();
        double ht = latLngBounds.latitudeSpan();
        int paddingLeft = (wd > ht) ? padWidth : 0;
        int paddingTop = (wd > ht) ? 0 : padRect.top + padWidth;
        int paddingRight = (wd > ht) ? padWidth : 0;
        int paddingBottom = (wd > ht) ? 0 : padRect.top + padWidth;
        // 获取当前相机位置

        Snapshotter snapshotter = new Snapshotter(context,mSnapshotMapOptions);
        snapshotter.setStyleJson(mStyleJson.toString());
     /*   ArrayList<Point> objects = new ArrayList<>();
        objects.add(latLngBounds.center());
        TurfMeta.coordAll(feature,true);*/
        CameraOptions cameraOptions = snapshotter.cameraForCoordinates( TurfMeta.coordAll(feature,true), new EdgeInsets(paddingTop, paddingLeft, paddingBottom, paddingRight), 0.0, 0.0);
        CameraOptions build = cameraOptions.toBuilder().zoom(14.0).build();
        snapshotter.setCamera(build);
     //  snapshotter.cameraForCoordinates(objects,new EdgeInsets(paddingTop,paddingLeft,paddingBottom,paddingRight),0.0,0.0);
        snapshotter.start(new ScreenShot(snapParam));
        snapshotters.add(snapshotter);

    }

    public void cancel() {
        for (Snapshotter snapshotter : snapshotters) {
            snapshotter.destroy();

        }

        snapshotters.clear();
    }

    public class ScreenShot implements SnapshotCreatedListener {

       private SnapParam mSnapParam;

        public ScreenShot(SnapParam snapParam) {
            this.mSnapParam = snapParam;
        }

        @Override
        public void onSnapshotResult(@Nullable MapSnapshotInterface mapSnapshotInterface) {

            new DbCommand<String>() {
                @Override
                protected String doInBackground() {
                    Bitmap.Config configBmp  = Bitmap.Config.ARGB_8888;
                    Image image = mapSnapshotInterface.image();
                    if(image ==null||image.getWidth() ==0||image.getHeight()==0){
                        if (mICallBack != null) {
                            mICallBack.onError(null);
                            return "失败";
                        }
                    }
                    Bitmap bitmap = Bitmap.createBitmap(image.getWidth(), image.getHeight(), configBmp);
                    try {

                        ByteBuffer buffer  = ByteBuffer.wrap(image.getData());
                        bitmap.copyPixelsFromBuffer(buffer);

                        Bitmap markBitmap = BitampUtil.addMark2BitmapEx(bitmap, mSnapParam.getWaterMark(), 12);
                        if(mSnapParam.getFilePath()!=null&&!mSnapParam.getFilePath().equals("")){
                            BitampUtil.bitmap2File(markBitmap, mSnapParam.getFilePath(), 100);
                        }else {
                            mSnapParam.setBase64(BitampUtil.bitmapToBase64(markBitmap));
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
