package com.gykj.commontool;

import android.content.Intent;
import android.os.Build;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.blankj.utilcode.util.ToastUtils;
import com.gykj.commontool.addressSelectView.StartoverActivity;
import com.gykj.commontool.addressselect.AddressSelectActivity;
import com.gykj.commontool.arcgistooltest.ArcgisToolTestActivity;
import com.gykj.commontool.autoupdatetest.AutoUpdateTestActivity;
import com.gykj.commontool.cameratest.CameraModuleTestActivity;
import com.gykj.commontool.commonUtils.CommonUtilsTestActivity;
import com.gykj.commontool.echarttest.EchartDemoActivity;
import com.gykj.commontool.grandPhotos.GrandPhotoActivity;
import com.gykj.commontool.imgCompressTest.ImgCompressActivity;
import com.gykj.commontool.locationservicetest.LocationServiceTestActivity;
import com.gykj.commontool.locationTest.LocationServiceTestActivity2;
import com.gykj.commontool.maptest.MapTestActivity;
import com.gykj.commontool.mvptest.testmodule.MvpTestActivity;
import com.gykj.commontool.networktest.NetworkTestActivity;
import com.gykj.commontool.ocrModuletest.OcrModuleTestActivity;
import com.gykj.commontool.ocrlocaltest.OcrTestActivity;
import com.gykj.commontool.osstest.OssTestActivity;
import com.gykj.commontool.sampledialogtest.SmartDialogModuleTestActivity;
import com.gykj.commontool.selectlinearlayouttest.SelectLinearLayoutTestActivity;
import com.gykj.commontool.shortvideotest.ShortVideoV2ModuleTestActivity;
import com.gykj.commontool.signtest.SignModuleTestActivity;
import com.gykj.commontool.timepickertest.TimePickerTestActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void shortVideoTest(View view) {
        Intent intent = new Intent(this, ShortVideoV2ModuleTestActivity.class);
        startActivity(intent);
    }

    public void signModuleTest(View view) {
        Intent intent = new Intent(this, SignModuleTestActivity.class);
        startActivity(intent);
    }

    public void mvpModuleTest(View view) {
        Intent intent = new Intent(this, MvpTestActivity.class);
        startActivity(intent);
    }

    public void networkModuleTest(View view) {
        Intent intent = new Intent(this, NetworkTestActivity.class);
        startActivity(intent);
    }

    public void ocrModuleTest(View view) {
        Intent intent = new Intent(this, OcrModuleTestActivity.class);
        startActivity(intent);
    }

    public void mapFrame(View view) {
        Intent intent = new Intent(this, MapTestActivity.class);
        startActivity(intent);
    }

    public void onEchartTestClick(View view) {
        Intent intent = new Intent(this, EchartDemoActivity.class);
        startActivity(intent);
    }

    /**
     * 下拉布局组件
     *
     * @param view
     */
    public void selectLinearLayoutTest(View view) {
        Intent intent = new Intent(this, SelectLinearLayoutTestActivity.class);
        startActivity(intent);
    }

    /**
     * 时间选择器控件
     *
     * @param view
     */
    public void timePickerTestActivity(View view) {
        Intent intent = new Intent(this, TimePickerTestActivity.class);
        startActivity(intent);
    }

    /**
     * 定位服务
     *
     * @param view
     */
    public void locationServiceTestActivity(View view) {
        Intent intent = new Intent(this, LocationServiceTestActivity.class);
        startActivity(intent);
    }


    /**
     * 自动更新
     *
     * @param view
     */
    public void AutoUpdateTestActivity(View view) {
        Intent intent = new Intent(this, AutoUpdateTestActivity.class);
        startActivity(intent);
    }

    /**
     * Oss
     *
     * @param view
     */
    public void OssTestActivity(View view) {
        Intent intent = new Intent(this, OssTestActivity.class);
        startActivity(intent);
    }

    public void SmartDialog(View view) {
        Intent intent = new Intent(this, SmartDialogModuleTestActivity.class);
        startActivity(intent);
    }

    public void ArcgisTool(View view) {
        Intent intent = new Intent(this, ArcgisToolTestActivity.class);
        startActivity(intent);

     /*   Intent intent = new Intent(this, ArcgisGeoDatabaseTestActivity.class);
        startActivity(intent);*/
    }

    public void AddressSelect(View view) {
        Intent intent = new Intent(this, AddressSelectActivity.class);
        startActivity(intent);
    }

    public void AddressSelectView(View view) {
        Intent intent = new Intent(this, StartoverActivity.class);
        startActivity(intent);
    }

    public void CameraModuleTest(View view) {
        Intent intent = new Intent(this, CameraModuleTestActivity.class);
        startActivity(intent);
    }

    public void OcrLocalModuleTest(View view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Intent intent = new Intent(this, OcrTestActivity.class);
            startActivity(intent);
        } else {
            ToastUtils.showShort("最低版本要求6.0!");
        }
    }

    /**
     * 图片选择器
     */
    public void onPhotoPickClick(View view) {
        Intent mIntent = new Intent(this, GrandPhotoActivity.class);
        startActivity(mIntent);
    }


    /**
     * 定位服务
     */
    public void onLocationClick(View view) {
        Intent mIntent = new Intent(this, LocationServiceTestActivity2.class);
        startActivity(mIntent);
    }

    /**
     * 通用工具类
     */
    public void onCommonUtilsClick(View view) {
        Intent mIntent = new Intent(this, CommonUtilsTestActivity.class);
        startActivity(mIntent);
    }

    /**
     * 通用工具类
     */
    public void onCompressClick(View view) {
        Intent mIntent = new Intent(this, ImgCompressActivity.class);
        startActivity(mIntent);
    }

}
