package com.gradtech.mapframe.mapdemo;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;

import com.blankj.utilcode.util.SizeUtils;

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
        int width = SizeUtils.dp2px(w);
        //获取图片高度
        int height = SizeUtils.dp2px(h);
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

}
