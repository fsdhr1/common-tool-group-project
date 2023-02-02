package com.grandtech.mapframe.orm;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.grandtech.mapframe.orm.data.SpatiaDatabaseSpTemp;
import com.grandtech.orm.spatia.Dao.SpBaseDao;
import com.mapbox.android.core.FileUtils;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Geometry;
import com.mapbox.geojson.MultiPolygon;
import com.mapbox.geojson.Polygon;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * @ClassName DemoActivity
 * @Description TODO java使用空间库示例
 * @Author: fs
 * @Date: 2021/8/17 13:57
 * @Version 2.0
 */
public class SptempTestActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView textView;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sp_demo_java);
        Button btnInsert = findViewById(R.id.btn_insert);
        textView = findViewById(R.id.textView2);
        btnInsert.setOnClickListener(this);
        Button btnQuery = findViewById(R.id.btn_query);
        Button btnUpdate = findViewById(R.id.btn_update);
        btnQuery.setOnClickListener(this);
        btnUpdate.setOnClickListener(this);
        Button btnDelete = findViewById(R.id.btn_delete);
        btnDelete.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.btn_insert){
            SpBaseDao spBaseDao = SpatiaDatabaseSpTemp.getDatabase(this).getSpBaseDao();
            String geo = "{\n" +
                    "      \"type\": \"MultiPolygon\",\n" +
                    "      \"coordinates\": [\n" +
                    "        [\n" +
                    "          [\n" +
                    "            [114.5711906, 37.3272566],\n" +
                    "            [114.571178, 37.3271534],\n" +
                    "            [114.5709235, 37.3271706],\n" +
                    "            [114.5709391, 37.3272738],\n" +
                    "            [114.5711906, 37.3272566]\n" +
                    "          ]\n" +
                    "        ]\n" +
                    "      ]\n" +
                    "    }";
            MultiPolygon polygon = MultiPolygon.fromJson(geo);
            Feature feature = Feature.fromGeometry(polygon);
            feature.addNumberProperty("objectid",1);
            feature.addStringProperty("bz","bz");
            feature.addNumberProperty("bz2",1.22);
            Observable<Long> tb_sptest = spBaseDao.insert("tb_sptest", feature, true);
            tb_sptest.subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<Long>() {
                        @Override
                        public void onSubscribe(@NonNull Disposable d) {

                        }

                        @Override
                        public void onNext(@NonNull Long aLong) {
                            textView.setText("保存成功");

                        }

                        @Override
                        public void onError(@NonNull Throwable e) {
                            textView.setText("保存失败");
                        }

                        @Override
                        public void onComplete() {
                            Log.i("插入","完成");

                        }
                    });

        }
        if(v.getId() == R.id.btn_query){
            SpBaseDao spBaseDao = SpatiaDatabaseSpTemp.getDatabase(this).getSpBaseDao();
            Observable<List<Feature>> listObservable = spBaseDao.queryByCondition("tb_sptest", "objectid = 1", "objectid");
           // Observable<List<Feature>> listObservable = spBaseDao.queryBySql("select * from tb_sptest");
            listObservable.subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<List<Feature>>() {
                        @Override
                        public void onSubscribe(@NonNull Disposable d) {

                        }

                        @Override
                        public void onNext(@NonNull List<Feature> features) {
                            FeatureCollection featureCollection = FeatureCollection.fromFeatures(features);
                            textView.setText(featureCollection.toJson());
                        }

                        @Override
                        public void onError(@NonNull Throwable e) {
                            textView.setText("查询");
                        }

                        @Override
                        public void onComplete() {
                            Log.i("查询","完成");
                        }
                    });

        }
        if(v.getId() == R.id.btn_update){
            SpBaseDao spBaseDao = SpatiaDatabaseSpTemp.getDatabase(this).getSpBaseDao();
            String geo = "{\n" +
                    "      \"type\": \"MultiPolygon\",\n" +
                    "      \"coordinates\": [\n" +
                    "        [\n" +
                    "          [\n" +
                    "            [114.5711906, 37.3272566],\n" +
                    "            [114.571178, 37.3271534],\n" +
                    "            [114.5709235, 37.3271706],\n" +
                    "            [114.5709391, 37.3272738],\n" +
                    "            [114.5711906, 37.3272566]\n" +
                    "          ]\n" +
                    "        ]\n" +
                    "      ]\n" +
                    "    }";
            MultiPolygon polygon = MultiPolygon.fromJson(geo);
            Feature feature = Feature.fromGeometry(polygon);
            feature.addNumberProperty("objectid",1);
            feature.addStringProperty("bz","bz修改");
            feature.addNumberProperty("bz2",1.22);
            List<String> conditions = new ArrayList<>();
            conditions.add("objectid");
            Observable<Long> tb_sptest = spBaseDao.update("tb_sptest", feature, conditions, null);
            tb_sptest.subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<Long>() {
                        @Override
                        public void onSubscribe(@NonNull Disposable d) {

                        }

                        @Override
                        public void onNext(@NonNull Long aLong) {
                            textView.setText("更新成功");
                        }

                        @Override
                        public void onError(@NonNull Throwable e) {

                        }

                        @Override
                        public void onComplete() {
                            Log.i("更新","完成");
                        }
                    });
        }
        if(v.getId() == R.id.btn_delete){
            SpBaseDao spBaseDao = SpatiaDatabaseSpTemp.getDatabase(this).getSpBaseDao();
            Observable<Long> tb_sptest = spBaseDao.delete("tb_sptest", "objectid = 1");
            tb_sptest.subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Consumer<Long>() {
                        @Override
                        public void accept(Long aLong) throws Exception {
                            textView.setText("删除成功！");
                            Log.i("删除","完成");
                        }
                    });
        }
    }
}
