package com.gykj.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.media.ThumbnailUtils;
import android.util.Log;

import com.gykj.cameramodule.R;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

/**
 * Created by zy on 2016/11/16.
 */

public class BitmapUtil {

    /**
     * 转换图片转换成圆角.
     *
     * @param bitmap 传入Bitmap对象
     * @return the bitmap
     */
    public static Bitmap toRoundBitmap(Bitmap bitmap) {
        if (bitmap == null) {
            return null;
        }
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        float roundPx;
        float left, top, right, bottom, dst_left, dst_top, dst_right, dst_bottom;
        if (width <= height) {
            roundPx = width / 2;
            top = 0;
            bottom = width;
            left = 0;
            right = width;
            height = width;
            dst_left = 0;
            dst_top = 0;
            dst_right = width;
            dst_bottom = width;
        } else {
            roundPx = height / 2;
            float clip = (width - height) / 2;
            left = clip;
            right = width - clip;
            top = 0;
            bottom = height;
            width = height;
            dst_left = 0;
            dst_top = 0;
            dst_right = height;
            dst_bottom = height;
        }

        Bitmap output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect src = new Rect((int) left, (int) top, (int) right,
                (int) bottom);
        final Rect dst = new Rect((int) dst_left, (int) dst_top,
                (int) dst_right, (int) dst_bottom);
        final RectF rectF = new RectF(dst);
        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, src, dst, paint);
        return output;
    }

    /**
     * 将图片圆形化
     *
     * @param bitmap
     * @return
     */
    public static Bitmap getRoundedCornerBitmap(Bitmap bitmap) {

        Bitmap output = null;
        if (bitmap != null) {

            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            float roundPx;
            float left, top, right, bottom, dst_left, dst_top, dst_right, dst_bottom;
            if (width <= height) {
                roundPx = width / 2;
                top = 0;
                bottom = width;
                left = 0;
                right = width;
                height = width;
                dst_left = 0;
                dst_top = 0;
                dst_right = width;
                dst_bottom = width;
            } else {
                roundPx = height / 2;
                float clip = (width - height) / 2;
                left = clip;
                right = width - clip;
                top = 0;
                bottom = height;
                width = height;
                dst_left = 0;
                dst_top = 0;
                dst_right = height;
                dst_bottom = height;
            }
            output = Bitmap.createBitmap(width, height,
                    Bitmap.Config.ARGB_4444);
            Canvas canvas = new Canvas(output);
            canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG));
            final int color = 0xff424242;
            final Paint paint = new Paint();
            final Rect src = new Rect((int) left, (int) top, (int) right,
                    (int) bottom);
            final Rect dst = new Rect((int) dst_left, (int) dst_top,
                    (int) dst_right, (int) dst_bottom);
            final RectF rectF = new RectF(dst);
            paint.setAntiAlias(true);
            canvas.drawARGB(0, 0, 0, 0);
            paint.setColor(color);
            canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
            canvas.drawBitmap(bitmap, src, dst, paint);

            bitmap = output;
            return bitmap;
        }
        output = null;
        return null;
    }

    /**
     * bitmap转byte数组
     *
     * @param bitmap
     * @return
     */
    public byte[] bitmap2Array(Bitmap bitmap) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();//初始化一个流对象
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, output);//把bitmap100%高质量压缩 到 output对象里
        bitmap.recycle();//自由选择是否进行回收
        byte[] result = output.toByteArray();//转换成功了
        try {
            output.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public static Bitmap getImageThumbnail(String imagePath, int width, int height) {
        Bitmap bitmap = null;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        // 获取这个图片的宽和高，注意此处的bitmap为null
        bitmap = BitmapFactory.decodeFile(imagePath, options);
        options.inJustDecodeBounds = false; // 设为 false
        // 计算缩放比
        int h = options.outHeight;
        int w = options.outWidth;
        int beWidth = w / width;
        int beHeight = h / height;
        int be = 1;
        if (beWidth < beHeight) {
            be = beWidth;
        } else {
            be = beHeight;
        }
        if (be <= 0) {
            be = 1;
        }
        options.inSampleSize = be;
        // 重新读入图片，读取缩放后的bitmap，注意这次要把options.inJustDecodeBounds 设为 false
        bitmap = BitmapFactory.decodeFile(imagePath, options);
        // 利用ThumbnailUtils来创建缩略图，这里要指定要缩放哪个Bitmap对象
        bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height, ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
        return bitmap;
    }

    /**
     * bitmap转File
     *
     * @param bitmap
     * @param file
     * @return
     */
    public static void bitmap2File(Bitmap bitmap, File file) {
        try {
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
            bitmap.compress(Bitmap.CompressFormat.JPEG, 99, bos);
            bos.flush();
            bos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * bitmap转File
     *
     * @param bitmap
     * @param path
     * @return
     */
    public static boolean bitmap2File(Bitmap bitmap, String path) {

        int lastIndex = path.lastIndexOf("/");
        String parentPath = path.substring(0, lastIndex);
        File f = new File(parentPath);
        if (!f.exists()) {
            f.mkdirs();
        }

        boolean flag = false;
        File file = new File(path);//将要保存图片的路径
        try {
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
            int fileS = bitmap.getByteCount();
            double z = (double) fileS / 1048576;
            if (z > 0.5) {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 70, bos);
            } else {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 99, bos);
            }
            bos.flush();
            bos.close();
            flag = true;
            if (!bitmap.isRecycled()) {
                bitmap.recycle();
            }
        } catch (IOException e) {
            new Throwable(e);
            flag = false;
        }
        return flag;
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
        File file = new File(path);//将要保存图片的路径
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
            if (!bitmap.isRecycled()) {
                bitmap.recycle();
            }
            Log.i("chashddddddda ", "ttt");
        } catch (IOException e) {
            new Throwable(e);
            Log.i("chashddda ", e.getLocalizedMessage());
            flag = false;
        }
        return flag;
    }

    /**
     * bitmap加水印
     *
     * @param bitmap
     * @param str
     * @param x
     * @param y
     * @return
     */
    public static Bitmap addWatermarkBitmap(Bitmap bitmap, String str, float x, float y, int rotate) {
        int destWidth = bitmap.getWidth();   //此处的bitmap已经限定好宽高
        int destHeight = bitmap.getHeight();

        Bitmap icon = Bitmap.createBitmap(destWidth, destHeight, Bitmap.Config.ARGB_8888); //定好宽高的全彩bitmap
        Canvas canvas = new Canvas(icon);//初始化画布绘制的图像到icon上

        Paint photoPaint = new Paint(); //建立画笔
        photoPaint.setDither(true); //获取跟清晰的图像采样
        photoPaint.setFilterBitmap(true);//过滤一些

        Rect src = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());//创建一个指定的新矩形的坐标
        Rect dst = new Rect(0, 0, destWidth, destHeight);//创建一个指定的新矩形的坐标
        canvas.drawBitmap(bitmap, src, dst, photoPaint);//将photo 缩放或则扩大到 dst使用的填充区photoPaint

        Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DEV_KERN_TEXT_FLAG);//设置画笔
        textPaint.setTextSize((float) (destWidth * 0.03));//字体大小
        textPaint.setTextAlign(Paint.Align.LEFT);
        textPaint.setTypeface(Typeface.DEFAULT_BOLD);//采用默认的宽度
        textPaint.setAntiAlias(true);  //抗锯齿
        textPaint.setStrokeWidth(0);
        textPaint.setAlpha(100);
        textPaint.setStyle(Paint.Style.FILL); //空心
        textPaint.setColor(Color.WHITE);//采用的颜色
        textPaint.setShadowLayer(1f, 0f, 3f, Color.LTGRAY);
        if (Math.abs(x - 0.5) < Math.abs(y - 0.5)) {
            x = x + y;
            y = x - y;
            x = x - y;
        }
        if (rotate == -90) {
            x = 1 - x;
        }
        String[] textArray = str.split(";");
        float interval = textPaint.getTextSize();
        canvas.drawText(textArray[0], x * destWidth, y * destHeight + interval * 0, textPaint);//绘制上去字，开始未知x,y采用那只笔绘制
        canvas.drawText(textArray[1], x * destWidth, y * destHeight + interval * 2, textPaint);//绘制上去字，开始未知x,y采用那只笔绘制
        canvas.drawText(textArray[2], x * destWidth, y * destHeight + interval * 4, textPaint);//绘制上去字，开始未知x,y采用那只笔绘制
        canvas.drawText(textArray[3], x * destWidth, y * destHeight + interval * 6, textPaint);//绘制上去字，开始未知x,y采用那只笔绘制

        canvas.save();
        canvas.restore();
        bitmap.recycle();
        return icon;
    }

    /**
     * @param bitmap
     * @param text
     * @return
     */
    public static Bitmap addMark2Bitmap(Bitmap bitmap, String text) {
        try {
            int destWidth = bitmap.getWidth();   //此处的bitmap已经限定好宽高
            int destHeight = bitmap.getHeight();

            Bitmap bp = Bitmap.createBitmap(destWidth, destHeight, Bitmap.Config.ARGB_8888); //定好宽高的全彩bitmap
            Canvas canvas = new Canvas(bp);//初始化画布绘制的图像到icon上
            Paint photoPaint = new Paint(); //建立画笔
            photoPaint.setDither(true); //获取跟清晰的图像采样
            photoPaint.setFilterBitmap(true);//过滤一些

            Rect src = new Rect(0, 0, destWidth, destHeight);//创建一个指定的新矩形的坐标
            Rect dst = new Rect(0, 0, destWidth, destHeight);//创建一个指定的新矩形的坐标
            canvas.drawBitmap(bitmap, src, dst, photoPaint);//将photo 缩放或则扩大到 dst使用的填充区photoPaint

            double fontSize = destWidth * 0.03;
            Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DEV_KERN_TEXT_FLAG);//设置画笔
            textPaint.setTextSize((float) fontSize);//字体大小
            textPaint.setTextAlign(Paint.Align.LEFT);
            textPaint.setTypeface(Typeface.DEFAULT_BOLD);//采用默认的宽度
            textPaint.setAntiAlias(true);  //抗锯齿
            textPaint.setStrokeWidth(0);
            textPaint.setAlpha(100);
            textPaint.setStyle(Paint.Style.FILL); //空心
            textPaint.setColor(Color.WHITE);//采用的颜色
            //textPaint.setShadowLayer(1f, 0f, 3f, Color.LTGRAY);
            int anchor = destHeight - 20;
            int textHt = (int) fontSize + 3;
            String[] textArray = text.split(";");
            for (int i = textArray.length - 1; i >= 0; i--) {
                if (textArray[i].contains("★")) {
                    textPaint.setColor(Color.RED);
                } else {
                    textPaint.setColor(Color.WHITE);//采用的颜色
                }
                canvas.drawText(textArray[i], 10, anchor - textHt * (textArray.length - i - 1), textPaint);
            }
            canvas.save();
            canvas.restore();
            if (!bitmap.isRecycled()) {
                bitmap.recycle();
            }
            return bp;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * @param bitmap
     * @param text
     * @param standWatwers
     * @return
     */
    public static Bitmap addMark2BitmapForStand(Bitmap bitmap, String text, Map<String, String> standWatwers) {
        try {
            int destWidth = bitmap.getWidth();   //此处的bitmap已经限定好宽高
            int destHeight = bitmap.getHeight();

            Bitmap bp = Bitmap.createBitmap(destWidth, destHeight, Bitmap.Config.ARGB_8888); //定好宽高的全彩bitmap
            Canvas canvas = new Canvas(bp);//初始化画布绘制的图像到icon上
            Paint photoPaint = new Paint(); //建立画笔
            photoPaint.setDither(true); //获取跟清晰的图像采样
            photoPaint.setFilterBitmap(true);//过滤一些

            Rect src = new Rect(0, 0, destWidth, destHeight);//创建一个指定的新矩形的坐标
            Rect dst = new Rect(0, 0, destWidth, destHeight);//创建一个指定的新矩形的坐标
            canvas.drawBitmap(bitmap, src, dst, photoPaint);//将photo 缩放或则扩大到 dst使用的填充区photoPaint

            double fontSize = destWidth * 0.03;
            Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DEV_KERN_TEXT_FLAG);//设置画笔
            textPaint.setTextSize((float) fontSize);//字体大小
            textPaint.setTextAlign(Paint.Align.LEFT);
            textPaint.setTypeface(Typeface.DEFAULT_BOLD);//采用默认的宽度
            textPaint.setAntiAlias(true);  //抗锯齿
            textPaint.setStrokeWidth(0);
            textPaint.setAlpha(100);
            textPaint.setStyle(Paint.Style.FILL); //空心
            textPaint.setColor(Color.WHITE);//采用的颜色
            //textPaint.setShadowLayer(1f, 0f, 3f, Color.LTGRAY);
            int anchor = destHeight - 20;
            int textHt = (int) fontSize + 3;
            String[] textArray = text.split(";");

            for (int i = textArray.length - 1; i >= 0; i--) {
                if (textArray[i].contains("★")) {
                    textPaint.setColor(Color.RED);
                } else {
                    textPaint.setColor(Color.WHITE);//采用的颜色
                }
                canvas.drawText(textArray[i], 10, anchor - textHt * (textArray.length - i - 1), textPaint);
            }
            Paint paint = new Paint();
            paint.setColor(Color.WHITE);
            paint.setAlpha(100);
            paint.setAntiAlias(true);
            paint.setTextAlign(Paint.Align.LEFT);
            paint.setTextSize(40);
            canvas.save();
            canvas.rotate(45);
            double x = destWidth * destWidth;
            double y = destHeight * destHeight;
            double yy = destWidth * destHeight / Math.sqrt(x + y);
            double xx = Math.sqrt(x - yy * yy);
//            String ww ="   仅限人保公司农险业务使用    "+"仅限人保公司农险业务使用    "+"仅限人保公司农险业务使用";
            String ww = null;
            if (standWatwers.containsKey("bxgsmc")) {
                String bxgsmc = standWatwers.get("bxgsmc");
                if (bxgsmc != null)
                    ww = "   仅限" + bxgsmc + "农险业务使用    " + "仅限" + bxgsmc + "农险业务使用    " + "仅限" + bxgsmc + "农险业务使用";
            }
            if (ww != null) {
                canvas.drawText(ww, 0, (float) (destHeight * 0.2) - (float) yy, paint);
                canvas.drawText(ww, 0, (float) (destHeight * 0.4) - (float) yy, paint);
                canvas.drawText(ww, 0, (float) (destHeight * 0.6) - (float) yy, paint);
                canvas.drawText(ww, 0, (float) (destHeight * 0.8) - (float) yy, paint);
                canvas.drawText(ww, 0, (float) (destHeight) - (float) yy, paint);
            }
            canvas.rotate(-45);

            canvas.drawText("坐标来源:GPS定位结果", destWidth - (textPaint.measureText("坐标来源:GPS定位结果") + 10), anchor, textPaint);
            if (standWatwers.containsKey("zwmc")) {
                canvas.drawText(standWatwers.get("zwmc"), destWidth - (textPaint.measureText(standWatwers.get("zwmc")) + 10), textHt, textPaint);
            }
            if (standWatwers.containsKey("nhxm")) {
                canvas.drawText(standWatwers.get("nhxm"), 10, textHt, textPaint);
            }
            if (standWatwers.containsKey("nhdm")) {
                canvas.drawText(standWatwers.get("nhdm"), 10, textHt + textHt, textPaint);
            }
            if (standWatwers.containsKey("ybr")) {
                canvas.drawText("验标人:" + standWatwers.get("ybr"), 10, textHt + textHt + textHt, textPaint);
            }
           /* if(standWatwers.containsKey("nhdm")){
                canvas.drawText(standWatwers.get("nhdm"), (destWidth-(textPaint.measureText(standWatwers.get("nhdm"))))/2, textHt+textHt , textPaint);
            }*/
            canvas.restore();
            canvas.save();
            if (!bitmap.isRecycled()) {
                bitmap.recycle();
            }
            if (!bitmap.isRecycled()) {
                bitmap.recycle();
            }
            return bp;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    //生成坐落图时调用此函数增加水印
    public static Bitmap addMark2BitmapEx(Bitmap bitmap, String text, int fSize) {
        if (text == null) return bitmap;
        Canvas canvas = new Canvas(bitmap);//初始化画布绘制的图像到icon上
        int destHeight = bitmap.getHeight();
        int destWidth = bitmap.getWidth();

        double fontSize = destWidth * 0.03;
        fontSize = (fontSize < fSize) ? fSize : fontSize;

        Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);//设置画笔
        textPaint.setTextSize((float) fontSize);//字体大小
        textPaint.setTextAlign(Paint.Align.LEFT);
        textPaint.setTypeface(Typeface.DEFAULT_BOLD);//采用默认的宽度
        textPaint.setAntiAlias(true);  //抗锯齿
        textPaint.setColor(Color.WHITE);//采用的颜色

        int anchor = destHeight - 10;
        int textHt = (int) fontSize + 3;
        String[] textArray = text.split(";");
        for (int i = textArray.length - 1; i >= 0; i--) {
            canvas.drawText(textArray[i], 10, anchor - textHt * (textArray.length - i - 1), textPaint);
        }
        canvas.save();
        canvas.restore();
        return bitmap;
    }

    /**
     * @param bitmap
     * @param waterMarks
     * @return
     */
    public static Bitmap addMark2Bitmap(Bitmap bitmap, Object[] waterMarks) {
        String text = (String) waterMarks[0];
        return addMark2Bitmap(bitmap, text);
    }

    /**
     * 验标定制水印
     *
     * @param bitmap
     * @param waterMarks
     * @return
     */
    public static Bitmap addMark2BitmapForStand(Bitmap bitmap, Object[] waterMarks, Map<String, String> standWatwers) {
        String text = (String) waterMarks[0];
        return addMark2BitmapForStand(bitmap, text, standWatwers);
    }

    /**
     * 新版查勘模块定制水印
     *
     * @param bitmap
     * @param waterMarks
     * @return
     */
    public static Bitmap addMark2BitmapForSurvey(Context context, Bitmap bitmap, Object[] waterMarks, Map<String, String> standWatwers) {
        String text = (String) waterMarks[0];
        return addMark2BitmapForSurvey(context, bitmap, text, standWatwers);
    }

    public static Bitmap addMark2BitmapForSurvey(Context context, Bitmap bitmap, String text, Map<String, String> standWatwers) {
        try {
            int destWidth = bitmap.getWidth();   //此处的bitmap已经限定好宽高
            int destHeight = bitmap.getHeight();

            Bitmap bp = Bitmap.createBitmap(destWidth, destHeight, Bitmap.Config.ARGB_8888); //定好宽高的全彩bitmap
            Canvas canvas = new Canvas(bp);//初始化画布绘制的图像到icon上
            Paint photoPaint = new Paint(); //建立画笔
            photoPaint.setDither(true); //获取跟清晰的图像采样
            photoPaint.setFilterBitmap(true);//过滤一些

            Rect src = new Rect(0, 0, destWidth, destHeight);//创建一个指定的新矩形的坐标
            Rect dst = new Rect(0, 0, destWidth, destHeight);//创建一个指定的新矩形的坐标
            canvas.drawBitmap(bitmap, src, dst, photoPaint);//将photo 缩放或则扩大到 dst使用的填充区photoPaint

            double fontSize = destWidth * 0.03;
            Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DEV_KERN_TEXT_FLAG);//设置画笔
            textPaint.setTextSize((float) fontSize);//字体大小
            textPaint.setTextAlign(Paint.Align.LEFT);
            textPaint.setTypeface(Typeface.DEFAULT_BOLD);//采用默认的宽度
            textPaint.setAntiAlias(true);  //抗锯齿
            //textPaint.setStrokeWidth(0);
            textPaint.setAlpha(100);
            textPaint.setStyle(Paint.Style.FILL); //空心
            textPaint.setColor(context.getResources().getColor(R.color.camera_module_waterRed));//采用的颜色

            //Paint textPaintBorder = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DEV_KERN_TEXT_FLAG);//设置画笔边框
            //textPaintBorder.setTextSize((float) fontSize);//字体大小
            //textPaintBorder.setTextAlign(Paint.Align.LEFT);
            //textPaintBorder.setTypeface(Typeface.DEFAULT_BOLD);//采用默认的宽度
            //textPaintBorder.setAntiAlias(true);  //抗锯齿
            //textPaintBorder.setStrokeWidth(0.3f);
            //textPaintBorder.setAlpha(100);
            //textPaintBorder.setStyle(Paint.Style.STROKE); //空心
            //textPaintBorder.setColor(Color.WHITE);//采用的颜色
            int anchor = destHeight - 20;
            int textHt = (int) fontSize + 3;
            String[] textArray = text.split(";");

            for (int i = textArray.length - 1; i >= 0; i--) {
                if (textArray[i].contains("★")) {
                    textPaint.setColor(context.getResources().getColor(R.color.camera_module_waterRed));
                } else {
                    textPaint.setColor(context.getResources().getColor(R.color.camera_module_waterRed));//采用的颜色
                }
                canvas.drawText(textArray[i], 10, anchor - textHt * (textArray.length - i - 1), textPaint);
                //canvas.drawText(textArray[i], 10, anchor - textHt * (textArray.length - i - 1), textPaintBorder);
            }
            Paint paint = new Paint();
            paint.setColor(Color.WHITE);
            paint.setAlpha(100);
            paint.setAntiAlias(true);
            paint.setTextAlign(Paint.Align.LEFT);
            paint.setTextSize(40);
            canvas.save();
            canvas.rotate(45);
            double x = destWidth * destWidth;
            double y = destHeight * destHeight;
            double yy = destWidth * destHeight / Math.sqrt(x + y);
            double xx = Math.sqrt(x - yy * yy);
            String ww = "";
            canvas.drawText(ww, 0, (float) (destHeight * 0.2) - (float) yy, paint);
            canvas.drawText(ww, 0, (float) (destHeight * 0.4) - (float) yy, paint);
            canvas.drawText(ww, 0, (float) (destHeight * 0.6) - (float) yy, paint);
            canvas.drawText(ww, 0, (float) (destHeight * 0.8) - (float) yy, paint);
            canvas.drawText(ww, 0, (float) (destHeight) - (float) yy, paint);
            canvas.rotate(-45);

            canvas.drawText("坐标来源:GPS定位结果", destWidth - (textPaint.measureText("坐标来源:GPS定位结果") + 10), anchor, textPaint);
            if (standWatwers.containsKey("zhyy")) {
                canvas.drawText("出险原因:" + standWatwers.get("zhyy"), 10, textHt * 2, textPaint);
            }
            if (standWatwers.containsKey("ybr")) {
                canvas.drawText("查勘员:" + standWatwers.get("ybr"), 10, textHt * 3, textPaint);
            }
            canvas.restore();
            canvas.save();
            if (!bitmap.isRecycled()) {
                bitmap.recycle();
            }
            if (!bitmap.isRecycled()) {
                bitmap.recycle();
            }
            return bp;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /* 旋转图片
     * @param angle
     * @param bitmap
     * @return Bitmap
     */
    public static Bitmap rotatingBitmap(Bitmap bitmap, int angle) {
        //modify by clj 20181115 角度为0，就不旋转
        if (angle == 0) return bitmap;
        //旋转图片 动作
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        // 创建新的图片
        Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        if (!bitmap.isRecycled()) {
            bitmap.recycle();
        }
        return resizedBitmap;
    }

    /**
     * 镜像翻转功能
     *
     * @param bitmap
     * @return
     */
    public static Bitmap convertBitmap(Bitmap bitmap) {
        Matrix matrix = new Matrix();
        matrix.postScale(-1, 1);
        Bitmap convertBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        return convertBitmap;
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
     * 成功返回相对路径
     *
     * @param bitmapArray
     * @param path        绝对路径
     * @return
     */
    public static String byte2File(byte[] bitmapArray, String path) {
        try {
            if (bitmapArray != null) {
                /**
                 * 创建父路径
                 */
                int lastIndex = path.lastIndexOf("/");
                String pathPath = path.substring(0, lastIndex);
                File f = new File(pathPath);
                if (!f.exists()) {
                    f.mkdirs();
                }
                File file = new File(path);
                if (file.exists()) {
                    file.delete();
                }
                File file1 = new File(path);
                FileOutputStream outputStream = new FileOutputStream(file1);
                outputStream.write(bitmapArray, 0, bitmapArray.length);
                outputStream.close();
                return path;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * 根据路径 转bitmap
     *
     * @param path
     * @return
     */
    public static Bitmap getBitMBitmap(String path) {

        Bitmap map = null;
        try {
            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(path, opts);
            int len = Math.max(opts.outHeight, opts.outWidth);
            int sampleSize = Math.round((float) len / 1000);
            if (sampleSize <= 0) sampleSize = 1;
            opts.inSampleSize = sampleSize;
            opts.inJustDecodeBounds = false;
            map = BitmapFactory.decodeFile(path, opts);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;
    }

    /**
     * 获取网络图片bitmap
     *
     * @param bitmap
     * @param url
     * @return
     */
    public Bitmap returnBitMap(Bitmap bitmap, final String url) {
        URL imageurl = null;
        try {
            imageurl = new URL(url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        try {
            HttpURLConnection conn = (HttpURLConnection) imageurl.openConnection();
            conn.setDoInput(true);
            conn.connect();
            InputStream is = conn.getInputStream();
            bitmap = BitmapFactory.decodeStream(is);
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    /**
     * 第二个位图添加到第一个上按位置
     *
     * @param b1
     * @param b2
     * @param location
     * @return
     */
    public static Bitmap mergeBitmap(Bitmap b1, Bitmap b2, String location) {

        Bitmap newBitmap = null;

        newBitmap = Bitmap.createBitmap(b1);
        Canvas canvas = new Canvas(newBitmap);
        Paint paint = new Paint();

        int w = b1.getWidth();
        int h = b1.getHeight();
        Bitmap zoomBp = zoomImg(b2, w / 10, w / 10);

        paint.setColor(Color.GRAY);
        paint.setAlpha(125);
        canvas.drawRect(0, 0, b1.getWidth(), b1.getHeight(), paint);

        paint = new Paint();
        switch (location) {
            case "top_left":
                canvas.drawBitmap(zoomBp, 0,
                        0, paint);
                break;
            case "top_right":
                canvas.drawBitmap(zoomBp, Math.abs(w - zoomBp.getWidth()),
                        0, paint);
                break;
            case "bottom_left":
                canvas.drawBitmap(zoomBp, 0,
                        Math.abs(h - zoomBp.getHeight()), paint);
                break;
            case "bottom_right":
                canvas.drawBitmap(zoomBp, Math.abs(w - zoomBp.getWidth()),
                        Math.abs(h - zoomBp.getHeight()), paint);
                break;
        }


        canvas.save();
        // 存储新合成的图片
        canvas.restore();

        return newBitmap;

    }

    public static Bitmap zoomImg(Bitmap bm, int newWidth, int newHeight) {
        // 获得图片的宽高
        int width = bm.getWidth();
        int height = bm.getHeight();
        // 计算缩放比例
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // 取得想要缩放的matrix参数
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        // 得到新的图片
        Bitmap newbm = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, true);
        return newbm;
    }
}
