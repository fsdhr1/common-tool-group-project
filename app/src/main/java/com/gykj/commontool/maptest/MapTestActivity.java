package com.gykj.commontool.maptest;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.grandtech.mapframe.core.GMap;
import com.grandtech.mapframe.core.maps.GMapView;
import com.gykj.commontool.R;
import com.gykj.commontool.ocrModuletest.OcrModuleTestActivity;
import com.mapbox.mapboxsdk.maps.MapboxMapOptions;

/**
 * @ClassName MapTestActivity
 * @Description TODO
 * @Author: fs
 * @Date: 2021/5/19 10:09
 * @Version 2.0
 */
public class MapTestActivity extends AppCompatActivity {

    private Button btnDemo1,btnDemo2,btnDemo3,btnDemo4,btn_demo5,btn_Editor,btn_offline,btn_mapui;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_test);
        btnDemo1 = findViewById(R.id.btn_demo1);
        btnDemo2 = findViewById(R.id.btn_demo2);
        btnDemo3 = findViewById(R.id.btn_demo3);
        btnDemo4 = findViewById(R.id.btn_demo4);
        btn_demo5 = findViewById(R.id.btn_demo5);
        btn_Editor = findViewById(R.id.btn_Editor);
        btn_offline = findViewById(R.id.btn_offline);
        btn_mapui = findViewById(R.id.btn_mapui);
        btnDemo1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MapTestActivity.this, MapActivityDome1.class);
                startActivity(intent);
            }
        });
        btnDemo2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MapTestActivity.this, MapActivityDemo2.class);
                startActivity(intent);
            }
        });
        btnDemo3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MapTestActivity.this, MapSnapshotTest.class);
                startActivity(intent);
            }
        });
        btnDemo4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MapTestActivity.this, AddRainFallStyleActivity.class);
                startActivity(intent);
            }
        });
        btn_demo5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MapTestActivity.this, MarkerViewActivity.class);
                startActivity(intent);
            }
        });
        btn_Editor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MapTestActivity.this, MapActivityEditorDemo.class);
                startActivity(intent);
            }
        });
        btn_offline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MapTestActivity.this, OffLineDemoActivity.class);
                startActivity(intent);
            }
        });
        btn_mapui.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MapTestActivity.this, MapUIActivityDemo.class);
                startActivity(intent);
            }
        });
    }

}
