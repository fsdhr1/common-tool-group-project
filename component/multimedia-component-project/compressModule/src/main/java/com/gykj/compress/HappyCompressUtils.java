package com.gykj.compress;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by ZhaiJiaChang.
 * <p>
 * Date: 2022/8/17
 */
public class HappyCompressUtils {
    private File srcImg;
    private File tagImg;
    private int srcWidth;
    private int srcHeight;

    public HappyCompressUtils(File mSrcImg, File mTagImg) throws FileNotFoundException {
        srcImg = mSrcImg;
        tagImg = mTagImg;
        //
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        options.inSampleSize = 1;

        BitmapFactory.decodeStream(new FileInputStream(srcImg), null, options);
        this.srcWidth = options.outWidth;
        this.srcHeight = options.outHeight;
    }


    /**
     * 压缩的过程；先压缩大小，在压缩质量
     *
     * @param mLeastCompressSize
     */
    File compress(int mLeastCompressSize) throws IOException {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = computeSize();

        Bitmap tagBitmap = BitmapFactory.decodeStream(new FileInputStream(srcImg), null, options);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();

        if (FileCheckUtils.isJPG(FileCheckUtils.toByteArray(srcImg))) {
            tagBitmap = FileCheckUtils.rotatingImage(tagBitmap, FileCheckUtils.getOrientation(srcImg));
        }
        int kb = mLeastCompressSize * 1000;
        int quality = 100;
        // 先把图片加载到流里
//        long start = System.currentTimeMillis();
//        Log.e("HappyCompress: ", "首次压缩  --");
        tagBitmap.compress(Bitmap.CompressFormat.WEBP, quality, stream);
//        long end = System.currentTimeMillis();
//        long diff = end - start;
//        Log.e("HappyCompress: ", "首次完成  -- 耗时 " + diff);
        int count = 0;
        while (true) {
            int mLength = stream.toByteArray().length;
//            Log.e("HappyCompress: ", "stream长度 kb --  " + mLength/1024);
            if ((mLength <= kb)) break;
//            start = System.currentTimeMillis();
            count++;
            stream.reset();// 重置
            quality -= 10;
            tagBitmap.compress(Bitmap.CompressFormat.WEBP, quality, stream);
//            end = System.currentTimeMillis();
//            diff = end - start;
//            Log.e("HappyCompress: ", "执行压缩次数 " + count + " -- 耗时 " + diff);
        }
//        Log.e("HappyCompress: ", "压缩后quality " + quality + " --");
        tagBitmap.recycle();

        FileOutputStream fos = new FileOutputStream(tagImg);
        fos.write(stream.toByteArray());
        fos.flush();
        fos.close();
        stream.close();
        return tagImg;
    }


    /**
     * 计算 inSampleSize 的值做缩放
     */
    private int computeSize() {
        boolean mWidth = srcWidth % 2 == 1;
        boolean mHeight = srcHeight % 2 == 1;
        srcWidth = mWidth ? srcWidth + 1 : srcWidth;
        srcHeight = mHeight ? srcHeight + 1 : srcHeight;

        // Math.max:返回几个数中最大的
        // Math.min:返回几个数中最小的数
        int longSide = Math.max(srcWidth, srcHeight);
        int shortSide = Math.min(srcWidth, srcHeight);

        float scale = ((float) shortSide / longSide);
        if (scale <= 1 && scale > 0.5625) {
            if (longSide < 1664) {
                return 1;
            } else if (longSide < 4990) {
                return 2;
            } else if (longSide > 4990 && longSide < 10240) {
                return 4;
            } else {
                return longSide / 1280 == 0 ? 1 : longSide / 1280;
            }
        } else if (scale <= 0.5625 && scale > 0.5) {
            return longSide / 1280 == 0 ? 1 : longSide / 1280;
        } else {
            return (int) Math.ceil(longSide / (1280.0 / scale));
        }
    }
}
