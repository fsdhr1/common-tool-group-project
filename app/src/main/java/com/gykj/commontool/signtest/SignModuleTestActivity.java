package com.gykj.commontool.signtest;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.gykj.commontool.R;

/**
 * 签字组件示例
 *
 * Created by jyh on 2021-02-25
 */
public class SignModuleTestActivity extends AppCompatActivity implements View.OnClickListener {

    private AppCompatImageView civSign;
    String ROOT_PATH = Environment.getExternalStorageDirectory().getAbsolutePath().toString();
    String path = ROOT_PATH + "/commontool" + "/image/" + System.currentTimeMillis() + "/";
    // 签字
    private static final int RESULT_SIGN = 10001;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signature_test);
        civSign = findViewById(R.id.civSign);

        registerEvent();
    }

    protected void registerEvent() {
        civSign.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.civSign) {
            // 跳转到签字页面
            Intent intent = new Intent();
            ComponentName cn = new ComponentName(this,"com.gykj.signature.SignActivity");// this--所属页面Context
            intent.setComponent(cn);
            intent.putExtra("signPath", path + "QM.jpg");// 签字图片存储路径，必传
            intent.putExtra("colorBtnNormal", "#13c287");// Button正常状态下的背景色，非必传 不传则采用默认颜色，通常设置为主题色
            startActivityForResult(intent, RESULT_SIGN);// RESULT_SIGN--int类型请求码
        }
    }

    /**
     * 返回参数接收
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_SIGN) {
            String signPath = data.getStringExtra("RES_SIGN");// 返回签字图片路径
            // 图片加载
            Glide.with(this)
                    .load(signPath)
                    .skipMemoryCache(true)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .error(getResources().getDrawable(R.mipmap.signature))
                    .into(civSign);
        }
    }
}
