package com.gykj.commontool.imgCompressTest;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;

import com.gykj.commontool.R;
import com.gykj.compress.HappyCompress;
import com.gykj.compress.HappyCompressUtils;
import com.gykj.compress.interfaces.onHappyCompressListener;
import com.gykj.grandphotos.GrandPhotoPickers;
import com.gykj.grandphotos.models.album.entity.GrandPhotoBean;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ImgCompressActivity extends AppCompatActivity {

    private static final int CHOOSE_ING = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_img_compress);


        findViewById(R.id.text).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View mView) {
                GrandPhotoPickers.createBuilder(ImgCompressActivity.this).start(CHOOSE_ING);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == CHOOSE_ING) {
                ArrayList<GrandPhotoBean> mListExtra = data.getParcelableArrayListExtra(GrandPhotoPickers.RESULT_PHOTOS);
                if (mListExtra!=null&&mListExtra.size()>0){
                    GrandPhotoBean mBean = mListExtra.get(0);
                    String mPath = mBean.path;
                    double mLength = new File(mPath).length();
                    double size = mLength/1000;
                    Log.e("HappyCompress: ", " --- before "+ size +" kb --- ");
                    HappyCompress.with(getBaseContext()).load(mPath).setOnCompressListener(new onHappyCompressListener() {
                        @Override
                        public void onSuccess(List<File> mFiles) {
                            for (File mFile : mFiles) {
                                double mLength = mFile.length();
                                double size = mLength/1000;
                                Log.e("HappyCompress: ", " --- 压缩后 "+ size +" kb --- ");
                            }
                        }
                    }).startCompress();
                }
            }
        }
    }
}