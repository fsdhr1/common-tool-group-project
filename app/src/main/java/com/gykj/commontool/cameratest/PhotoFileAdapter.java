package com.gykj.commontool.cameratest;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.LayoutRes;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.gykj.base.adapter.BaseRecycleAdapter;
import com.gykj.base.adapter.BaseViewHolder;
import com.gykj.cameramodule.bean.PhotoFile;
import com.gykj.commontool.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zy on 2018/10/25.
 */

public class PhotoFileAdapter extends BaseRecycleAdapter<PhotoFile, BaseViewHolder<PhotoFile>> {

    private String baseUrl;
    private boolean nameVisible = true;

    public PhotoFileAdapter(@Nullable RecyclerView recyclerView) {
        super(recyclerView);
        this.baseUrl = baseUrl;
    }

    public PhotoFileAdapter(@Nullable RecyclerView recyclerView, String baseUrl) {
        super(recyclerView);
        this.baseUrl = baseUrl;
    }

    public PhotoFileAdapter(@Nullable RecyclerView recyclerView, String baseUrl, boolean nameVisible) {
        super(recyclerView);
        this.baseUrl = baseUrl;
        this.nameVisible = nameVisible;
    }

    @Override

    public BaseViewHolder<PhotoFile> onCreateViewHolder(ViewGroup parent, int viewType) {
        return new Holder(parent, R.layout.activity_camera_photo_adapter_file);
    }

    @Override
    public void isPosItemVisible(int firstItemPosition, int lastItemPosition) {

    }

    @Override
    public void scrollState(RecyclerView recyclerView, int newState) {

    }

    private class Holder extends BaseViewHolder<PhotoFile>
            implements View.OnClickListener,
            View.OnLongClickListener {

        private TextView tvName;
        private UploadImageView uivImage;

        public Holder(ViewGroup parent, @LayoutRes int resId) {
            super(parent, resId);
            uivImage = getView(R.id.uivImage);
            tvName = getView(R.id.tvName);
            uivImage.setOnClickListener(this);
            uivImage.setOnLongClickListener(this);
        }

        @Override
        public void setData(PhotoFile data, int position) {
            if (!data.getPath().equals("")) {
                if (nameVisible) {
                    tvName.setVisibility(View.GONE);
                } else {
                    tvName.setText(data.getDesc());
                }
//                if (data.getPath().contains("mobile")) {
//                    uivImage.setImgPath(data, R.drawable.ic_pic_error);
//                } else {
                uivImage.setImgPath(data.getPath(), data.getName(), data.getDesc(), R.drawable.ic_pic_error);
//                }
            }
        }

        @Override
        public void onClick(View view) {
            if (ipViewClickListener != null)
                ipViewClickListener.iPViewClick(parentView, view, data, position);
        }

        @Override
        public boolean onLongClick(View v) {
            if (ipViewLongClickListener != null)
                ipViewLongClickListener.iPViewLongClick(parentView, v, data, position);
            return false;
        }
    }

    public List<PhotoFile> getNetCF() {
        List<PhotoFile> photoFiles = new ArrayList<>();
        for (PhotoFile photoFile : getDataList()) {
            if (photoFile.getPath().contains("mobile")) {
                photoFiles.add(photoFile);
            }
        }
        return photoFiles;
    }
}
