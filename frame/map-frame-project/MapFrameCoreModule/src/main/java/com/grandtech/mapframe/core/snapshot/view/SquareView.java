package com.grandtech.mapframe.core.snapshot.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.ViewGroup;

import java.util.List;

/**
 * @ClassName SquareBoxMapView
 * @Description TODO 矩形框 让他
 * @Author: fs
 * @Date: 2021/8/3 15:27
 * @Version 2.0
 */
public class SquareView extends ViewGroup {

    private Rect rectOut;

    private Rect rectIn;

    private int width;

    private int height;

    private Bitmap maskBitmap;

    private Canvas maskCanvas;

    private Paint maskPaint;

    private Paint transparentPaint;

    private Paint paint;

    /**
     * 绘制文字
     */
    private Paint mPaintText;

    private PorterDuffXfermode porterDuffXfermode;

    private List<String> warter;


    public SquareView(Context context) {
        super(context);

        init();
    }

    public SquareView(Context context, AttributeSet attrs) {
        super(context, attrs);

        init();
    }

    public SquareView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init();
    }

    private void init() {
        //必须
        this.setWillNotDraw(false);
        maskPaint = new Paint();
        maskPaint.setColor(Color.argb(200, 0, 0, 0));
        transparentPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        transparentPaint.setColor(Color.BLUE);
        porterDuffXfermode = new PorterDuffXfermode(PorterDuff.Mode.CLEAR);
        //非常重要
        transparentPaint.setXfermode(porterDuffXfermode);
        paint = new Paint();
        mPaintText  = new Paint();
    }

    @Override
    protected void onLayout(boolean b, int i, int i1, int i2, int i3) {

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        width = getWidth();
        height = getHeight();
        // 绘制遮罩层
        if (maskBitmap == null) {
            maskBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            maskCanvas = new Canvas(maskBitmap);
        }
        rectOut = new Rect(0, 0, width, height);
        rectIn = new Rect(0, (height - width) / 2, width, (height + width) / 2);
        maskCanvas.drawRect(rectOut, maskPaint);
        // 在遮罩层上挖一个矩形
        maskCanvas.drawRect(rectIn, transparentPaint);
        canvas.drawBitmap(maskBitmap, 0, 0, paint);
        if(warter!=null) {
            /**
             * 绘制文字
             */
            double fontSize = width * 0.03;
            mPaintText.setTextSize((float) fontSize);
            mPaintText.setTextAlign(Paint.Align.LEFT);
            //采用默认的宽度
            mPaintText.setTypeface(Typeface.DEFAULT_BOLD);
            //抗锯齿
            mPaintText.setAntiAlias(true);
            mPaintText.setStrokeWidth(0);
            mPaintText.setAlpha(100);
            //空心
            mPaintText.setStyle(Paint.Style.FILL);
            //采用的颜色
            mPaintText.setColor(Color.WHITE);
            int anchor = height - (height - width) / 2 - 20;
            int textHt = (int) fontSize + 3;
            for (int i = 0;i<warter.size() ;i++) {
                if(warter.get(i).contains("★")){
                    mPaintText.setColor(Color.RED);
                }else {
                    //采用的颜色
                    mPaintText.setColor(Color.WHITE);
                }
                maskCanvas.drawText(warter.get(i), 10, anchor - textHt * (warter.size() - i - 1), mPaintText);
            }
        }
    }

    public Rect getRectIn() {
        return rectIn;
    }
    public void setWarter(List<String> warter){
        this.warter =warter;
        postInvalidate();
    }
}
