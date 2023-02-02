package com.baidu.utils;

import android.content.Context;
import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;

import com.baidu.ocr.ui.camera.CameraActivity;
import com.baidu.ocr.ui.util.FileUtil;

public class OcrOpenUtils {

    private static OcrOpenUtils mOpenUtils;

    public static OcrOpenUtils getInstance() {
        if (mOpenUtils == null) {
            synchronized (OcrOpenUtils.class) {
                if (mOpenUtils == null) {
                    mOpenUtils = new OcrOpenUtils();
                }
            }
        }
        return mOpenUtils;
    }

    /**
     * 身份证扫描正面
     *
     * @param mActivity
     * @param path
     * @param requestCode
     */
    public void recIdCardFront(AppCompatActivity mActivity, String path, int requestCode) {
        Intent intent = new Intent(mActivity, CameraActivity.class);
        intent.putExtra(CameraActivity.KEY_OUTPUT_FILE_PATH, path);
        intent.putExtra(CameraActivity.KEY_NATIVE_ENABLE, true);
        intent.putExtra(CameraActivity.KEY_NATIVE_TOKEN, Global.OCR_TOKEN);
        intent.putExtra(CameraActivity.KEY_CONTENT_TYPE, CameraActivity.CONTENT_TYPE_ID_CARD_FRONT);
        mActivity.startActivityForResult(intent, requestCode);
    }

    /**
     * 身份证扫描反面
     *
     * @param mActivity
     * @param path
     * @param requestCode
     */
    public void recIdCardBack(AppCompatActivity mActivity, String path, int requestCode) {
        Intent intent = new Intent(mActivity, CameraActivity.class);
        intent.putExtra(CameraActivity.KEY_OUTPUT_FILE_PATH, path);
        intent.putExtra(CameraActivity.KEY_NATIVE_ENABLE, true);
        intent.putExtra(CameraActivity.KEY_NATIVE_TOKEN, Global.OCR_TOKEN);
        intent.putExtra(CameraActivity.KEY_CONTENT_TYPE, CameraActivity.CONTENT_TYPE_ID_CARD_BACK);
        mActivity.startActivityForResult(intent, requestCode);
    }

    /***
     * 银行卡扫描
     *
     * @param mActivity
     * @param path
     * @param requestCode
     */
    public void recBankCard(AppCompatActivity mActivity, String path, int requestCode) {
        Intent intent2 = new Intent(mActivity, CameraActivity.class);
        intent2.putExtra(CameraActivity.KEY_OUTPUT_FILE_PATH, path);
        intent2.putExtra(CameraActivity.KEY_CONTENT_TYPE, CameraActivity.CONTENT_TYPE_BANK_CARD);
        mActivity.startActivityForResult(intent2, requestCode);
    }

    /**
     * 营业执照扫描
     *
     * @param mActivity
     * @param path
     * @param requestCode
     */
    public void recBusinessLicense(AppCompatActivity mActivity, String path, int requestCode) {
        Intent intent3 = new Intent(mActivity, CameraActivity.class);
        intent3.putExtra(CameraActivity.KEY_OUTPUT_FILE_PATH, path);
        intent3.putExtra(CameraActivity.KEY_CONTENT_TYPE, CameraActivity.CONTENT_TYPE_GENERAL);
        mActivity.startActivityForResult(intent3, requestCode);
    }

    /**
     * 动物扫描
     *
     * @param mActivity
     * @param path
     * @param requestCode
     */
    public void recAnimalDetect(AppCompatActivity mActivity, String path, int requestCode) {
        Intent intent3 = new Intent(mActivity, CameraActivity.class);
        intent3.putExtra(CameraActivity.KEY_OUTPUT_FILE_PATH, path);
        intent3.putExtra(CameraActivity.KEY_CONTENT_TYPE, CameraActivity.CONTENT_TYPE_GENERAL);
        mActivity.startActivityForResult(intent3, requestCode);
    }

    /***
     * 植物扫描
     *
     * @param mActivity
     * @param path
     * @param requestCode
     */
    public void recPlantDetect(AppCompatActivity mActivity, String path, int requestCode) {
        Intent intent3 = new Intent(mActivity, CameraActivity.class);
        intent3.putExtra(CameraActivity.KEY_OUTPUT_FILE_PATH, path);
        intent3.putExtra(CameraActivity.KEY_CONTENT_TYPE, CameraActivity.CONTENT_TYPE_GENERAL);
        mActivity.startActivityForResult(intent3, requestCode);
    }

    /**
     * 果蔬识别扫描
     *
     * @param mActivity
     * @param path
     * @param requestCode
     */
    public void recIngredient(AppCompatActivity mActivity, String path, int requestCode) {
        Intent intent3 = new Intent(mActivity, CameraActivity.class);
        intent3.putExtra(CameraActivity.KEY_OUTPUT_FILE_PATH, path);
        intent3.putExtra(CameraActivity.KEY_CONTENT_TYPE, CameraActivity.CONTENT_TYPE_GENERAL);
        mActivity.startActivityForResult(intent3, requestCode);
    }


}
