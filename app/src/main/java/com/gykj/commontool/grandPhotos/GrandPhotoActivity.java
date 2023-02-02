package com.gykj.commontool.grandPhotos;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.gykj.commontool.R;
import com.gykj.grandphotos.GrandPhotoPickers;
import com.gykj.grandphotos.constant.GrandCode;
import com.gykj.grandphotos.models.album.entity.GrandPhotoBean;

import java.util.ArrayList;

public class GrandPhotoActivity extends AppCompatActivity {


    private RecyclerView mRecyclerView;
    private MainAdapter mMainAdapter;
    private ArrayList<GrandPhotoBean> mPhotoList = new ArrayList<>();
    private ArrayList<GrandPhotoBean> resultPhotos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grand_photo);
        mRecyclerView = findViewById(R.id.rv_image);
        // initRecycler
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this,
                LinearLayoutManager.HORIZONTAL, false);
        mMainAdapter = new MainAdapter(this, mPhotoList);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mRecyclerView.setAdapter(mMainAdapter);
//        SnapHelper snapHelper = new PagerSnapHelper();
//        snapHelper.attachToRecyclerView(mRecyclerView);
        //

    }

    // 点击事件
    public void onViewClick(View view) {
        int mId = view.getId();
        // 单选
        if (mId == R.id.bt_single) {
            GrandPhotoPickers.createBuilder(this).start(100);
        } else if (mId == R.id.bt_much) {
            GrandPhotoPickers.createBuilder(this).setMaxCount(5).start(200);
        } else if (mId == R.id.bt_much_sel){
            GrandPhotoPickers.createBuilder(this).setMaxCount(5).setSelectedPhotos(resultPhotos).start(200);
        } else if (mId == R.id.bt_camera) {
            GrandPhotoPickers.createCamera(this).start(GrandCode.REQUEST_CAMERA);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            mPhotoList.clear();
            resultPhotos = data.getParcelableArrayListExtra(GrandPhotoPickers.RESULT_PHOTOS);
            if (resultPhotos != null) {
                mPhotoList.addAll(resultPhotos);
                mMainAdapter.notifyDataSetChanged();
            }
        }
    }
}