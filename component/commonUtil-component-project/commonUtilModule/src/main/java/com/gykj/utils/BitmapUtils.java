package com.gykj.utils;

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

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class BitmapUtils {


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

    /**
     * 获取bitmap的缩略图
     *
     * @param imagePath
     * @param width
     * @param height
     * @return
     */
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
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
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
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, bos);
            } else {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
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

        boolean flag = false;
        File file = new File(path);//将要保存图片的路径
        try {
            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
            int fileS = bitmap.getByteCount();
            double z = (double) fileS / 1048576;
            if (z > 0.5) {
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, bos);
            } else {
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, bos);
            }
            //bitmap.compress(Bitmap.CompressFormat.JPEG, quality, bos);
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

    //生成坐落图时调用此函数增加水印
    public static Bitmap addMark2BitmapEx(Bitmap bitmap, String text, int fSize) {
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
    public static Bitmap getBitmapFromSDPath(String path) {

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
     * @param url
     * @return
     */
    public static Bitmap getBitmapFromHttp(final String url) {
        URL imageurl = null;
        Bitmap bitmap = null;
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
}
