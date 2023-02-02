package com.gykj.commontool.shortvideotest;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.blankj.utilcode.constant.PermissionConstants;
import com.blankj.utilcode.util.PermissionUtils;
import com.gykj.commontool.R;

public class ShortVideoV2ModuleTestActivity extends AppCompatActivity {
    private TextView tvStart;
    private static  int requesetVideoCode = 3300;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_short_video_module_test);
        PermissionUtils.permission(PermissionConstants.CAMERA,PermissionConstants.LOCATION,PermissionConstants.STORAGE,PermissionConstants.MICROPHONE).request();
        tvStart =findViewById(R.id.tv_start);
        tvStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                ComponentName cn = new ComponentName(ShortVideoV2ModuleTestActivity.this, "com.gykj.shortvideov2module.VideoActivity");//YourActivity（全类名）
                intent.setComponent(cn);
                intent.putExtra("videoName","deamo");
                startActivityForResult(intent,requesetVideoCode);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==requesetVideoCode&&resultCode==-1){
            String videoPath =data.getStringExtra("videoPath");//本地视屏地址
            String thumbPicPath =data.getStringExtra("picPath");//缩略图地址

        }
    }
}
