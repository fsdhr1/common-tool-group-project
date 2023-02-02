package com.gradtech.mapframev10.core.sketch

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.View
import com.gradtech.mapframev10.R
import com.mapbox.maps.ScreenCoordinate
import org.jetbrains.annotations.NotNull

/**
 * @ClassName SketchHelpView
 * @Description TODO
 * @Author: fs
 * @Date: 2022/8/5 9:33
 * @Version 2.0
 */
class SketchHelpView : View {

    private var mPaint: Paint = Paint();
    private var mCrossHair: Bitmap? = null;
    private var mDrawRubberLineDisabled: Boolean = true;

    private var mPeakPoint: PointF? = null;
    private var mOnePoint: PointF? = null;
    private var mTwoPoint: PointF? = null;
    private var mAreaMsg: String = "";
    private var mLengthMsg: String = "";
    private var mIsDrawAreaMsg = false;
    private var mIsDrawLengthMsg = false;

    constructor(context: Context?) : super(context) {
        init();
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init();
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init();
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
        init();
    }

    private fun init() {
        mPaint.setAntiAlias(true)
        mPaint.setStrokeWidth(2f)
        mPaint.setStyle(Paint.Style.STROKE)
        mPaint.setColor(Color.RED)
        mPaint.setAlpha(255)
        mPaint.setTextSize(30.0F)
        mCrossHair = BitmapFactory.decodeResource(context.resources, R.mipmap.gmmfc_ic_cross_hair_v1)

    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        Log.i("SketchHelpView", "" + width + "" + height)
        val cw = (this.width / 2).toFloat()
        val ch = (this.height / 2).toFloat()
        if (!mDrawRubberLineDisabled) {
            mPaint.color = Color.YELLOW
            val path = Path()
            path.moveTo(mOnePoint!!.x, mOnePoint!!.y)
            if (mOnePoint != null) {
                path.lineTo(mPeakPoint!!.x, mPeakPoint!!.y);
            }
            if (mTwoPoint != null) {
                path.lineTo(mTwoPoint!!.x, mTwoPoint!!.y);
            }
            canvas?.drawPath(path, mPaint)
        }
        //中心点
        canvas?.drawBitmap(mCrossHair, cw - mCrossHair!!.getWidth() / 2, ch - mCrossHair!!.getHeight() / 2, mPaint)
        mPaint.setColor(Color.YELLOW)
        canvas?.drawText(mLengthMsg, cw - mCrossHair!!.getWidth() / 2, ch - mCrossHair!!.getHeight()*2 / 3, mPaint)
        canvas?.drawText(mAreaMsg, cw - mCrossHair!!.getWidth() / 2, ch - mCrossHair!!.getHeight()*4 / 3, mPaint)
    }

    /**
     * 返回当前view的中心点
     */
    fun getScreenCenterPoint(): ScreenCoordinate {
        val w = this.width
        val h = this.height
        return ScreenCoordinate((w / 2).toDouble(), (h / 2).toDouble())
    }

    fun drawRubberLine(@NotNull onePoint: PointF, @NotNull twoPoint: PointF, peakPoint: PointF? = null) {
        val screenCenterPoint = getScreenCenterPoint()
        mPeakPoint = peakPoint
                ?: PointF(screenCenterPoint.x.toFloat(), screenCenterPoint.y.toFloat())
        mOnePoint = onePoint;
        mTwoPoint = twoPoint;
        Log.i("橡皮筋1", "" + mOnePoint!!.x + "," + mOnePoint!!.y);
        Log.i("橡皮筋2", "" + mTwoPoint!!.x + "," + mTwoPoint!!.y);
        if (mOnePoint!!.y < 0) {
            mOnePoint!!.y = 0f;
        }
        if (mOnePoint!!.x < 0) {
            mOnePoint!!.x = 0f;
        }
        if (mTwoPoint!!.y < 0) {
            mTwoPoint!!.y = 0f;
        }
        if (mTwoPoint!!.x < 0) {
            mTwoPoint!!.x = 0f;
        }
        mDrawRubberLineDisabled = false;
        this.postInvalidate()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        //mtargetBitmap.recycle()
        cleanup()
    }

    fun cleanup() {
        mCrossHair?.recycle()
    }

    /**
     * 设置面积
     */
    fun setAreaMsg(double2halfUp: Double) {
        if(mIsDrawAreaMsg){
            mAreaMsg = "面积:"+double2halfUp.toString()+"亩"
        }else{
            mAreaMsg = "";
        }
    }

    /**
     * 设置周长
     */
    fun setLengthMsg(double2halfUp: Double) {
        if(mIsDrawLengthMsg){
            mLengthMsg = "长度:"+double2halfUp.toString()+"米"
        }else{
            mLengthMsg = ""
        }
    }

    fun drawArea(isDrawAreaMsg: Boolean) {
        mIsDrawAreaMsg = isDrawAreaMsg
    }

    fun drawLength(isDrawLengthMsg: Boolean) {
        mIsDrawLengthMsg = isDrawLengthMsg
    }

}