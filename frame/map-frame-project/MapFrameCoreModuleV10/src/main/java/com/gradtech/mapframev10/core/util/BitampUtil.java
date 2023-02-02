package com.gradtech.mapframev10.core.util;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.util.Base64;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;


/**
 * @ClassName BitampUtil
 * @Description TODO
 * @Author: fs
 * @Date: 2022/12/21 11:14
 * @Version 2.0
 */
public class BitampUtil {

    public static Bitmap drawableToBitamp(Drawable drawable,float w,float h)
    {
        //声明将要创建的bitmap
        Bitmap bitmap = null;
        //获取图片宽度
        int width = dp2px(w);
        //获取图片高度
        int height = dp2px(h);
        //图片位深，PixelFormat.OPAQUE代表没有透明度，RGB_565就是没有透明度的位深，否则就用ARGB_8888。详细见下面图片编码知识。
        Bitmap.Config config = drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565;
        //创建一个空的Bitmap
        bitmap = Bitmap.createBitmap(width,height,config);
        //在bitmap上创建一个画布
        Canvas canvas = new Canvas(bitmap);
        //设置画布的范围
        drawable.setBounds(0, 0, width, height);
        //将drawable绘制在canvas上
        drawable.draw(canvas);
        return bitmap;
    }

    public static int dp2px(final float dpValue) {
        final float scale = Resources.getSystem().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * Value of px to value of dp.
     *
     * @param pxValue The value of px.
     * @return value of dp
     */
    public static int px2dp(final float pxValue) {
        final float scale = Resources.getSystem().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }


    /**
     * bitmap转为base64
     * @param bitmap
     * @return
     */
    public static String bitmapToBase64(Bitmap bitmap) {

        String result = null;
        ByteArrayOutputStream baos = null;
        try {
            if (bitmap != null) {
                baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);

                baos.flush();
                baos.close();

                byte[] bitmapBytes = baos.toByteArray();
                result = Base64.encodeToString(bitmapBytes, Base64.DEFAULT);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (baos != null) {
                    baos.flush();
                    baos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    /**
     * base64转为bitmap
     * @param base64Data
     * @return
     */
    public static Bitmap base64ToBitmap(String base64Data) {
        byte[] bytes = Base64.decode(base64Data, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    /**
     * 按长方形裁切图片
     *
     * @param bitmap
     * @return
     */
    public static Bitmap bitmapCropWithRect(Bitmap bitmap, Rect rect) {
        if (bitmap == null) {
            return null;
        }
        int w = bitmap.getWidth(); // 得到图片的宽，高
        int h = bitmap.getHeight();
        // 下面这句是关键
        Bitmap bmp = Bitmap.createBitmap(bitmap, rect.left, rect.top, rect.width(), rect.height(), null, false);
        if (!bitmap.isRecycled()) {
            bitmap.recycle();
        }
        return bmp;
    }

    /**
     * 生成坐落图时调用此函数增加水印
     * @param bitmap
     * @param marks
     * @param fSize
     * @return
     */
    public static Bitmap addMark2BitmapEx(Bitmap bitmap, List<String> marks, int fSize) {
        if(marks==null||marks.size() == 0) {
            return bitmap;
        }
        //初始化画布绘制的图像到icon上
        Canvas canvas = new Canvas(bitmap);
        int destHeight = bitmap.getHeight();
        int destWidth = bitmap.getWidth();
        double fontSize = destWidth * 0.03;
        fontSize = (fontSize < fSize) ? fSize : fontSize;
        //设置画笔
        Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        //字体大小
        textPaint.setTextSize((float) fontSize);
        textPaint.setTextAlign(Paint.Align.LEFT);
        //采用默认的宽度
        textPaint.setTypeface(Typeface.DEFAULT_BOLD);
        //抗锯齿
        textPaint.setAntiAlias(true);
        //采用的颜色
        textPaint.setColor(Color.WHITE);
        int anchor = destHeight - 10;
        int textHt = (int) fontSize + 3;
        for (int i = 0;i<marks.size();i++ ) {
            canvas.drawText(marks.get(i), 10, anchor - textHt * (marks.size() - i - 1), textPaint);
        }
        canvas.save();
        canvas.restore();
        return bitmap;
    }

    /**
     * bitmap转File
     *
     * @param bitmap
     * @param path
     * @return
     */
    public static boolean bitmap2File(Bitmap bitmap, String path, int quality) {

        int lastIndex = path.lastIndexOf("/");
        String parentPath = path.substring(0, lastIndex);
        File f = new File(parentPath);
        if (!f.exists()) {
            f.mkdirs();
        }
        Log.i("chashddda ", "dddd");
        boolean flag = false;
        //将要保存图片的路径
        File file = new File(path);
        try {
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
            int fileS = bitmap.getByteCount();
            double z = (double) fileS / 1048576;
            if (z > 0.5) {
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, bos);
            } else {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 99, bos);
            }
            //bitmap.compress(Bitmap.CompressFormat.JPEG, quality, bos);
            bos.flush();
            bos.close();
            flag = true;
            if (!bitmap.isRecycled())
            {
                bitmap.recycle();
            }
            Log.i("chashddddddda ","ttt");
        } catch (IOException e) {
            new Throwable(e);
            Log.i("chashddda ", e.getLocalizedMessage());
            flag = false;
        }
        return flag;
    }

}
