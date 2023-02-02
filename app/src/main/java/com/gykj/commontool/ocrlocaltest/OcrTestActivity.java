package com.gykj.commontool.ocrlocaltest;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.grandtech.ocrlib.OcrResultModel;
import com.grandtech.ocrlib.OcrTool;
import com.gykj.commontool.R;

public class OcrTestActivity extends AppCompatActivity {

    private OcrTool mOcrTool;
    private ImageView mIv_input_image;
    private TextView mTv_status;
    private final static String regex = "[0-9]{12}[A-Z]{2}[0-9]{5}";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ocr_local_test);
        mIv_input_image = findViewById(R.id.iv_input_image);
        mTv_status = findViewById(R.id.tv_status);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mOcrTool = new OcrTool(this, new OcrTool.OcrListener() {
                @Override
                public void onLoadModelSuccess() {
                    mTv_status.setText("模型加载成功");
                }
    
                @Override
                public void onLoadModelFailed() {
                    mTv_status.setText("模型加载失败");
                }
    
                @Override
                public void onRunModelSuccess(List<OcrResultModel> results) {
                    mTv_status.setText("识别成功");
                    String s = results.toString();
                    Log.e("ocr",s);
                    String label = "";
                    for (OcrResultModel result : results) {
                        String templabel = result.getLabel();
                        templabel = templabel.replace("\r","");
                        if(templabel.length() == 19 && templabel.matches(regex)){
                            label = templabel;
                            break;
                        }
                    }
                    Toast.makeText(OcrTestActivity.this,label,Toast.LENGTH_SHORT).show();
                }
    
                @Override
                public void onRunModelFailed() {
                    mTv_status.setText("识别失败");
                }
            });
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        set_img();
    }
    public void set_img() {
        // Load test image from path and run model
        try {
            AssetManager assetManager = getAssets();
            InputStream in=assetManager.open("images/test.jpg");
            Bitmap bmp= BitmapFactory.decodeStream(in);
            mIv_input_image.setImageBitmap(bmp);
        } catch (IOException e) {
            Toast.makeText(OcrTestActivity.this, "Load image failed!", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
    public void init_ocr(View view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mOcrTool.init();
        }
    }

    public void start_ocr(View view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Bitmap image =((BitmapDrawable)mIv_input_image.getDrawable()).getBitmap();
            mTv_status.setText("正在识别...");
            mOcrTool.startOcr(image);
        }
    }

    public void release_ocr(View view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mOcrTool.release();
        }
    }
}