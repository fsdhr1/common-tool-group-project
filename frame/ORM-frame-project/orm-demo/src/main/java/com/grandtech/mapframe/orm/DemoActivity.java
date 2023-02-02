package com.grandtech.mapframe.orm;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.grandtech.mapframe.orm.data.SpatiaEncryptionDatabase;

/**
 * @ClassName DemoActivity
 * @Description TODO
 * @Author: fs
 * @Date: 2021/8/17 13:57
 * @Version 2.0
 */
public class DemoActivity extends AppCompatActivity {


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo);
        Button button1 = findViewById(R.id.button1);
        Button button2 = findViewById(R.id.button2);
        Button button3 = findViewById(R.id.button3);
        Button button4 = findViewById(R.id.button4);
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DemoActivity.this, SpatiaActivity.class);
                intent.putExtra("flag",0);
                startActivity(intent);
            }
        });
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DemoActivity.this, SpatiaActivity.class);
                intent.putExtra("flag",1);
                startActivity(intent);
            }
        });
        button4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DemoActivity.this, SptempTestActivity.class);
                intent.putExtra("flag",2);
                startActivity(intent);
            }
        });
    }
}
