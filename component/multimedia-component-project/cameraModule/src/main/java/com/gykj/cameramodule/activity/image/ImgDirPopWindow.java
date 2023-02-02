package com.gykj.cameramodule.activity.image;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.gykj.base.adapter.BaseRecycleAdapter;
import com.gykj.base.popwindow.BasePopWin;
import com.gykj.cameramodule.R;
import com.gykj.cameramodule.adapter.ImgFolderAdapter;
import com.gykj.cameramodule.bean.ImgFolderBean;

import java.util.List;

/**
 * Created by zy on 2018/8/12.
 */

public class ImgDirPopWindow extends BasePopWin implements View.OnClickListener {

    List<ImgFolderBean> _imgFolderBeans;

    RecyclerView _rvImgDir;
    TextView _tvClose;
    ImgFolderAdapter _imgFolderAdapter;
    BaseRecycleAdapter.IViewClickListener<ImgFolderBean> iViewClickListener;

    public ImgDirPopWindow(Context context) {
        super(context);

    }

    @Override
    public void setContentView() {
        view = LayoutInflater.from(context).inflate(R.layout.camera_img_dir_popwindow_list, null);
    }

    @Override
    public void initialize() {
        _rvImgDir = view.findViewById(R.id.rvImgDir);
        _tvClose = view.findViewById(R.id.tvClose);
        _imgFolderAdapter = new ImgFolderAdapter(_rvImgDir);
        _rvImgDir.setLayoutManager(new GridLayoutManager(context, 2));
        _rvImgDir.setAdapter(_imgFolderAdapter);
    }

    @Override
    public void registerEvent() {
        _tvClose.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.tvClose) {
            _popWindow.dismiss();
        }
    }

    public void setImgFolderBeans(List<ImgFolderBean> imgFolderBeans) {
        this._imgFolderBeans = imgFolderBeans;
        _imgFolderAdapter.replaceAll(_imgFolderBeans);
    }

    public void setiViewClickListener(BaseRecycleAdapter.IViewClickListener<ImgFolderBean> iViewClickListener) {
        this.iViewClickListener = iViewClickListener;
        _imgFolderAdapter.setIViewClickListener(iViewClickListener);
    }
}
