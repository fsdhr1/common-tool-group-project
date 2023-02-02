package com.gykj.commontool.maptest;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.grandtech.mapframe.core.GMap;
import com.grandtech.mapframe.core.maps.GMapView;
import com.grandtech.mapframe.core.snapshot.AutoMapSnapshot;
import com.grandtech.mapframe.core.snapshot.bean.SnapParam;
import com.grandtech.mapframe.core.util.FeatureSet;
import com.grandtech.mapframe.core.util.StyleJsonBuilder;
import com.gykj.commontool.MyApplication;
import com.gykj.commontool.R;
import com.mapbox.geojson.Feature;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName Tett
 * @Description TODO
 * @Author: fs
 * @Date: 2021/7/30 17:17
 * @Version 2.0
 */
public class Tett extends AppCompatActivity {

    private GMapView mGMapView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //在项目中使用的时候这句话要放到applacation中初始化
        GMap.getInstance(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_snapshot1);
        mGMapView = ((MyApplication)Tett.this.getApplication()).gMapView ;
        List<Feature> features = FeatureSet.convert2Features(mGMapView.getSelectSet());
        //水印
        List<String> waterMark = new ArrayList<>();
        waterMark.add("坐落图默认样式");
        List<SnapParam> snapParams = new ArrayList<>();
        for (Feature feature : features) {
            String cachePath = Environment.getExternalStorageDirectory().getAbsolutePath()+ File.separator+"toolmapframe" + File.separator +
                    "坐落图默认样式" + File.separator +feature.getStringProperty("objectid")+".png";
            SnapParam snapParam = new SnapParam(feature,cachePath,waterMark);
            snapParams.add(snapParam);
        }
        String styleJosn = StyleJsonBuilder.create().builder();
        AutoMapSnapshot.create(Tett.this, styleJosn, new AutoMapSnapshot.ICallBack() {
            @Override
            public void onNext(SnapParam snapParam) {
                Log.i("gmbgl_test",snapParam.toJson());
            }

            @Override
            public void onError(Throwable e) {
                e.printStackTrace();
                Log.e("gmbgl_test",e.getMessage());
            }

            @Override
            public void complete() {
                Log.i("gmbgl_test","坐落图生成完毕");
                Toast.makeText(Tett.this, "坐落图生成完毕！", Toast.LENGTH_LONG).show();
            }
        }).batchSnapShot(snapParams,mGMapView.getMapBoxMap());
    }
}
