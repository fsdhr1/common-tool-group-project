package com.gradtech.mapframe.mapdemo;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

/**
 * @ClassName Main
 * @Description TODO
 * @Author: fs
 * @Date: 2022/10/8 8:34
 * @Version 2.0
 */
public class Main extends AppCompatActivity {
    private Button btSketch,btSelection,btNet,btnMarker,btn_snapshot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_test);
        btSketch = findViewById(R.id.btn_sketch);
        btn_snapshot = findViewById(R.id.btn_snapshot);
        btn_snapshot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Main.this, MapScreenshotTestActivity.class);
                startActivity(intent);
            }
        });
        btSketch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Main.this, SketchTestActivity.class);
                startActivity(intent);
            }
        });
        btSelection = findViewById(R.id.btn_selection);
        btSelection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Main.this, SelectionTestActivity.class);
                startActivity(intent);
            }
        });
        btNet = findViewById(R.id.btn_net);
        btNet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Main.this, NetTestActivity.class);
                startActivity(intent);
            }
        });

        btnMarker = findViewById(R.id.btn_marker);
        btnMarker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Main.this, MarkerMangerTestActivity.class);
                startActivity(intent);
            }
        });
    }
}
