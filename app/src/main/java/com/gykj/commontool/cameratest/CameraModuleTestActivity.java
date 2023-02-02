package com.gykj.commontool.cameratest;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ImageButton;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cazaea.sweetalert.SweetAlertDialog;
//import com.grandtech.mapframe.core.util.FileUtil;
import com.gykj.base.adapter.BaseRecycleAdapter;
import com.gykj.cameramodule.activity.image.ImageViewPager;
import com.gykj.cameramodule.bean.ImagePagerBean;
import com.gykj.cameramodule.bean.PhotoFile;
import com.gykj.commontool.R;
import com.gykj.utils.FileUtil;
import com.gykj.utils.VibratorUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.gykj.cameramodule.activity.camera.CameraActivity.SUCCESS;

/**
 * Description: 相册组件示例
 * Created by jyh
 * DateTime: 2021-07-28 13:23
 * package: com.gykj.commontool.cameratest
 */
public class CameraModuleTestActivity extends AppCompatActivity
        implements View.OnClickListener,
        BaseRecycleAdapter.IPViewClickListener,
        BaseRecycleAdapter.IPViewLongClickListener {

    private ImageButton ibAddPhoto;
    private RecyclerView rvPhoto;

    private ImageButton ibCamera;

    String ROOT_PATH = Environment.getExternalStorageDirectory().getAbsolutePath().toString();
    String path = ROOT_PATH + "/commontool" + "/file/" + System.currentTimeMillis() + "/";
    String localPath = ROOT_PATH + "/commontool" + "/image/" + "/local/";
    String AREA_NAME_URL = "http://gykj123.cn:8849/" + "agribigdata-server-base/" + "api/v1/dicts/level/wkt";//获取区划全称的地址
    // 图片
    public static final int RESULT_IMAGE = 10001;
    public static final int RESULT_CAMERA = 10002;

    PhotoFileAdapter picFileAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_test);
        ibAddPhoto = findViewById(R.id.ibAddPhoto);
        rvPhoto = findViewById(R.id.rvPhoto);
        ibCamera = findViewById(R.id.ibCamera);

        picFileAdapter = new PhotoFileAdapter(rvPhoto, null);
        picFileAdapter.setIpViewClickListener(this);
        picFileAdapter.setIpViewLongClickListener(this);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        rvPhoto.setLayoutManager(layoutManager);
        rvPhoto.setAdapter(picFileAdapter);
        registerEvent();

        // 初始加载本地照片
        String path = ROOT_PATH + "/commontool" + "/file/";
        List<Object> list = FileUtil.getFilePathInFolder(path, ".jpg", true);
        if (list != null && list.size() > 0) {
            for (Object o : list) {
                PhotoFile photoFile = new PhotoFile("照片", o.toString(), "");
                picFileAdapter.appendItem(photoFile);
            }
        }
    }

    protected void registerEvent() {
        ibAddPhoto.setOnClickListener(this);
        ibCamera.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.ibAddPhoto) {
            // 跳转到相册页面，同时支持相机拍照
            Intent intent = new Intent();
            ComponentName cn = new ComponentName(this, "com.gykj.cameramodule.activity.image.ImgPickActivity");// this--所属页面Context
            intent.setComponent(cn);
            intent.putExtra("isBatch", true);// 是否批处理（连拍），非必传 不传默认false
            intent.putExtra("filePath", path);// 拍摄的照片的存储路径，必传
            intent.putExtra("basePath", path);// 选择相册中的图片存储的路径，必传
            intent.putExtra("firstPath", localPath);// 判断优先显示的路径下有没有图片，有的话就优先显示，没有就不管他
            intent.putExtra("extendName", "XCZP");// 拓展名，必传
            intent.putExtra("areaName", "areaName");// 水印信息显示的地点名称，非必传
//            intent.putExtra("areaCode", "null");// 地点代码，拼接在图片名中，非必传
            intent.putExtra("isOrdinary", false);// 是否为普通相机，非必传 不传默认true，照片不带水印
//            intent.putExtra("areaNameUrl", AREA_NAME_URL);// 获取区划全称的地址，非必传
            intent.putExtra("isHideTitle", false);// 是否隐藏相册页面的标题头，非必传 不传默认false不隐藏
            intent.putExtra("backgroundColorTitle", "#008577");// 标题头背景色，非必传 不传则采用默认颜色，通常设置为主题色
            startActivityForResult(intent, RESULT_IMAGE);// RESULT_CAMERA--int类型请求码
        }
        if (view.getId() == R.id.ibCamera) {
            // 直接跳转到相机拍照
            Intent intent = new Intent();
            ComponentName cn = new ComponentName(this, "com.gykj.cameramodule.activity.camera.CameraActivity");// this--所属页面Context
            intent.setComponent(cn);
            intent.putExtra("isBatch", true);// 是否批处理（连拍），非必传 不传默认false
            intent.putExtra("filePath", path);// 拍摄的照片的存储路径，必传
            intent.putExtra("extendName", "XCZP");// 拓展名，必传
            intent.putExtra("areaName", "areaName");// 水印信息显示的地点名称，非必传
            intent.putExtra("areaCode", "000000000000");// 地点代码，拼接在图片名中，非必传
            intent.putExtra("isOrdinary", false);// 是否为普通相机，非必传 不传默认true，照片不带水印
//            intent.putExtra("areaNameUrl", AREA_NAME_URL);// 获取区划全称的地址，非必传
            //intent.putExtra("waterMarkerText", waterMarkerText);// 水印信息
            startActivityForResult(intent, RESULT_CAMERA);
        }
    }

    /**
     * 返回参数接收
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // 相册返回
        if (requestCode == RESULT_IMAGE) {
            if (data.getStringExtra("flag").equals("nothing")) return;// 没有图片
            if (data.getStringExtra("flag").equals("camera")) {// 相机拍照
                List<PhotoFile> list = (List<PhotoFile>) data.getSerializableExtra("pathPhotoFile");
                // 是否批处理（连拍）
                if (list != null) {
                    int i = 0;
                    for (PhotoFile photoFile : list) {
                        i = i + 1;
                        photoFile.setDesc(i + "");
                        picFileAdapter.appendItem(photoFile);
                        // 拷贝到本地路径
                        FileUtil.copyFileAnyhow(photoFile.getPath(), localPath + System.currentTimeMillis() + "_照片.jpg");
                    }
                } else {
                    String path = data.getStringExtra("path");
                    PhotoFile photoFile = new PhotoFile("现场照片", path, data.getStringExtra("desc"));// desc 水印信息
                    picFileAdapter.appendItem(photoFile);
                    // 拷贝到本地路径
                    FileUtil.copyFileAnyhow(path, localPath + System.currentTimeMillis() + "_照片.jpg");
                }
            } else if (data.getStringExtra("flag").equals("pickImg")) {// 相册选择
                ArrayList<String> items = data.getStringArrayListExtra("items");// 选择相册中的图片存储的全路径
                int i = 0;
                for (String item : items) {
                    i = i + 1;
                    try {
                        PhotoFile photoFile = new PhotoFile("现场照片", item, data.getStringExtra("desc"));// desc 水印信息
                        photoFile.setDesc(i + "");
                        picFileAdapter.appendItem(photoFile);
                        String albumpath = data.getStringExtra("albumpath");
                        if (albumpath != null && albumpath.contains(localPath))
                            continue;//如果是从缓存列表选的就不要再复制了
                        // 拷贝到本地路径
                        FileUtil.copyFileAnyhow(item, localPath + System.currentTimeMillis() + "_照片.jpg");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        // 相机拍照返回
        if (requestCode == RESULT_CAMERA) {
            if (data == null) return;
            if (resultCode == SUCCESS) {
                List<PhotoFile> list = (List<PhotoFile>) data.getSerializableExtra("pathPhotoFile");
                // 是否批处理（连拍）
                if (list != null) {
                    int i = 0;
                    for (PhotoFile photoFile : list) {
                        i = i + 1;
                        photoFile.setDesc(i + "");
                        picFileAdapter.appendItem(photoFile);
                        // 拷贝到本地路径
                        FileUtil.copyFileAnyhow(photoFile.getPath(), localPath + System.currentTimeMillis() + "_照片.jpg");
                    }
                } else {
                    String path = data.getStringExtra("path");
                    PhotoFile photoFile = new PhotoFile("现场照片", path, data.getStringExtra("desc"));// desc 水印信息
                    picFileAdapter.appendItem(photoFile);
                    // 拷贝到本地路径
                    FileUtil.copyFileAnyhow(path, localPath + System.currentTimeMillis() + "_照片.jpg");
                }
            }
        }
    }

    /**
     * 单击图片预览
     *
     * @param parent
     * @param view
     * @param data
     * @param position
     */
    @Override
    public void iPViewClick(View parent, View view, Object data, int position) {
        if (parent.getId() == rvPhoto.getId()) {
            ArrayList<ImagePagerBean> imagePagerBeans = createImagePagerBeanList();
            ImageViewPager.start(this, imagePagerBeans, getImageIndex(imagePagerBeans, ((UploadImageView) view).getCollectFile().getPath()));
        }
    }

    /**
     * 获取所有ImagePagerBean
     */
    private ArrayList<ImagePagerBean> createImagePagerBeanList() {
        ArrayList<ImagePagerBean> mlist = new ArrayList<>();
        if (picFileAdapter.getItemCount() > 0) {
            List<PhotoFile> list = picFileAdapter.getDataList();
            for (int i = 0; i < list.size(); i++) {
                PhotoFile photoFile = list.get(i);
                int j = i + 1;
                ImagePagerBean imagePagerBean = new ImagePagerBean(photoFile.getPath(), "照片" + j, photoFile.getPath());
                mlist.add(imagePagerBean);
            }
        }
        return mlist;
    }

    /**
     * 获取当前选择的图片的顺序位置
     */
    private int getImageIndex(ArrayList<ImagePagerBean> mlist, String path) {
        for (int i = 0; i < mlist.size(); i++) {
            if (mlist.get(i).getUrl().equals(path)) {
                return i;
            }
        }
        return 0;
    }

    /**
     * 长按删除
     *
     * @param parent
     * @param view
     * @param data
     * @param position
     */
    @Override
    public void iPViewLongClick(View parent, View view, Object data, int position) {
        if (parent.getId() == rvPhoto.getId()) {
            if (view.getId() == R.id.uivImage) {
                VibratorUtil.Vibrate(this, 200);
                SweetAlertDialog sweetAlertDialog = new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE);
                sweetAlertDialog.setTitleText("提示")
                        .setContentText("确定要删除吗?")
                        .setCancelText("取消")
                        .setConfirmText("确定")
                        .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sweetAlertDialog) {
                                sweetAlertDialog.dismiss();
                                picFileAdapter.removeItem(position);
                                PhotoFile photoFile = (PhotoFile) data;
                                File file = new File(photoFile.getPath());
                                file.delete();
                            }
                        }).show();
            }
        }
    }
}