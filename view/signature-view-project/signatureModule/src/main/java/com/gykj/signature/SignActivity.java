package com.gykj.signature;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.widget.Toolbar;

import com.trello.rxlifecycle2.components.support.RxAppCompatActivity;

/**
 * 签字
 *
 * Created by jyh on 2021-02-24
 */
public class SignActivity extends RxAppCompatActivity implements View.OnClickListener{
    public final static int OK = 666;
    public final static int FAIL = 555;

    private String signPath;// 路径
    private String colorBtnNormal;// Button正常状态下的背景色
    private String colorBtnPressed;// Button按下时的背景色

    private Toolbar _toolbar;
    private ImageView ivBack;
    private LinePathView lpvSign;
    private Button btnClear, btnConfirm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);// 横屏
        Intent intent = getIntent();
        if (intent != null) {
            signPath = intent.getStringExtra("signPath");
            colorBtnNormal = intent.getStringExtra("colorBtnNormal");
            //colorBtnNormal = "#13c287";
        }

        setContentView(R.layout.sign_activity);
        initialize();
        registerEvent();
    }

    protected void initialize() {
        _toolbar = (Toolbar) findViewById(R.id._toolbar);
        ivBack = (ImageView) findViewById(R.id.ivBack);
        lpvSign = (LinePathView) findViewById(R.id.lpvSign);
        btnConfirm = (Button) findViewById(R.id.btnConfirm);
        btnClear = (Button) findViewById(R.id.btnClear);

        if (!TextUtils.isEmpty(colorBtnNormal)) {
            int color = Color.parseColor(colorBtnNormal);
            _toolbar.setBackgroundColor(color);

            StateListDrawable background = (StateListDrawable) btnConfirm.getBackground();
            Drawable current = background.getCurrent();
            if(current instanceof GradientDrawable){
                GradientDrawable drawable = (GradientDrawable) current;
                // 边线颜色、边线宽度
                //drawable.setStroke(0, getResources().getColor(R.color.color_00ff00));
                // 改变drawable的背景填充色
                drawable.setColor(color);
            }

            background = (StateListDrawable) btnClear.getBackground();
            current = background.getCurrent();
            if(current instanceof GradientDrawable){
                GradientDrawable drawable = (GradientDrawable) current;
                // 边线颜色、边线宽度
                //drawable.setStroke(0, getResources().getColor(R.color.color_00ff00));
                // 改变drawable的背景填充色
                drawable.setColor(color);
            }
        }
    }

    protected void registerEvent() {
        ivBack.setOnClickListener(this);
        btnConfirm.setOnClickListener(this);
        btnClear.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.ivBack) {
            finish();
        }
        // 确定
        if (view.getId() == R.id.btnConfirm) {
            byte[] bytes = lpvSign.getBytes();
            String flag = BitmapUtil.byte2File(bytes, signPath);
            Intent intent = new Intent();
            if (flag != null) {
                intent.putExtra("RES_SIGN", signPath);
                setResult(OK, intent);
            } else {
                intent.putExtra("RES_SIGN", signPath);
                setResult(FAIL, intent);
            }
            this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            finish();
        }
        // 清除
        if (view.getId() == R.id.btnClear) {
            lpvSign.clear();
        }
    }
}
