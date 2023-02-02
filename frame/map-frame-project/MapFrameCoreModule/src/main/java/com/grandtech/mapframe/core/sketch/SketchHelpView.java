package com.grandtech.mapframe.core.sketch;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;

import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.FrameLayout;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.grandtech.mapframe.core.R;
import com.grandtech.mapframe.core.util.MeasureUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by zy on 2018/5/29.
 */

public final class SketchHelpView extends FrameLayout {

    /**
     * 绘制拖拽时候的线和面
     */
    public static final int DRAG_POLYGON_POLYLINE = 1000;
    /**
     * 绘制十字丝
     */
    public static final int DRAW_TARGET = 2000;
    /**
     * 绘制帮助点
     */
    public static final int DRAG_POINT = 3000;
    /**
     * 绘制橡皮绳 释放
     */
    public static final int DRAG_NORMAL = 4000;

    /**
     *
     */
    public static final int DRAG_NOTHING = 5000;
    private int _flag;
    private Context _context;

    private String[] _text;
    /**
     * 屏幕中心坐标
     */
    private PointF _screenCenterPoint;
    /**
     *
     */
    private Paint _paint;
    /**
     *
     */
    private int _alpha = 255;
    /**
     * 圆圈半径
     */
    private int _radius = 5;

    private int _textSize = 30;
    /**
     * 绘制图形
     */
    private Path _path;
    //*************************橡皮绳*************************
    /**
     * 坐标一
     */
    private PointF _screenPoint1;
    /**
     * 坐标二
     */
    private PointF _screenPoint2;
    //***********************拖拽和平移***********************

    private Bitmap _targetBitmap;
    private Bitmap _crossHair;
    /**
     * 平移矢量
     */
    private float _vx = 0;
    private float _vy = 0;
    private PointF _bPoint;
    private PointF _aPoint;
    private PointF _cPoint;
    //拖拽点的完成结果
    private PointF _mbPoint;
    /**
     * 图形点集合
     */
    private List<PointF> _oPointFS;
    /**
     * 平移后的
     */
    private List<PointF> _mPointFS;
    private boolean _isClose = true;
    private boolean _isRelease;

    //***********************拖拽和平移***********************

    public SketchHelpView(@NonNull Context context) {
        super(context);
        this._context = context;
        init();
    }

    public SketchHelpView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this._context = context;
        init();
    }

    public SketchHelpView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this._context = context;
        init();
    }

    private void init() {
        _paint = new Paint();
        _paint.setAntiAlias(true);
        _paint.setStrokeWidth(2);
        _paint.setStyle(Paint.Style.STROKE);
        _paint.setColor(Color.RED);
        _paint.setAlpha(_alpha);
        _paint.setTextSize(_textSize);
        _crossHair = BitmapFactory.decodeResource(_context.getResources(), R.mipmap.gmmfc_ic_cross_hair_v1);
        this.setWillNotDraw(false);
    }

    //橡皮绳的颜色
    int yellow = Color.parseColor("#ffff00");
    //分割面积颜色
    int blue = Color.parseColor("#62FFFF");
    String info;

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float cw = this.getWidth() / 2;
        float ch = this.getHeight() / 2;
        if (_text != null && _text.length > 0) {
            _paint.setStrokeWidth(2);
            _paint.setColor(Color.YELLOW);
            _paint.setTextSize( MeasureUtil.dp2px(getContext(),15));
            if(!"".equals(_text[0])){
                canvas.drawText(_text[0], cw - _crossHair.getWidth() / 2, ch - _crossHair.getHeight() * 2 / 3, _paint);
            }
            if(!"".equals(_text[1])){
                canvas.drawText(_text[1], cw - _crossHair.getWidth() / 2, ch - _crossHair.getHeight() * 4 / 3, _paint);
            }

        }
        if (_info != null && _info.size() > 0) {
            _paint.setStrokeWidth(1);
            _paint.setColor(Color.WHITE);
            _paint.setTextSize( MeasureUtil.dp2px(getContext(),10));
            Double area;
            for (PointF pointF : _info.keySet()) {
                area = _info.get(pointF);
                info = String.format("%.2f", area)+"亩";
                canvas.drawText(info, pointF.x - _paint.measureText(info) / 2, pointF.y, _paint);
            }
        }else {
            if (mLineInfo != null && mLineInfo.size() > 0) {
                _paint.setStyle(Paint.Style.FILL);
                _paint.setColor(Color.WHITE);
                _paint.setTextSize( MeasureUtil.dp2px(getContext(),15));
                _paint.setTextAlign(Paint.Align.CENTER);
                Double area;
                for (Path path : mLineInfo.keySet()) {
                    area = mLineInfo.get(path);
                    info = String.format("%.2f", area)+"m";
                    canvas.drawTextOnPath(info, path, 0,-8, _paint);
                }
            }
        }

        _paint.setStyle(Paint.Style.STROKE);
        _paint.setTextSize(_textSize);
        _paint.setColor(yellow);
        _paint.setStrokeWidth(3);
        if(_crossHair.isRecycled()){
            _crossHair = BitmapFactory.decodeResource(_context.getResources(), R.mipmap.gmmfc_ic_cross_hair_v1);
        }
        //中心点
        canvas.drawBitmap(_crossHair, cw - _crossHair.getWidth() / 2, ch - _crossHair.getHeight() / 2, _paint);
        //橡皮绳
        if (_isRelease) {
            if (_flag == DRAG_NORMAL) {
                dragHelpLine(canvas);
            }
        }
        //拖拽
        else {
            if (_flag == DRAG_POLYGON_POLYLINE) {
                dragPolygonDraw(canvas);
            }
            if (_flag == DRAW_TARGET) {
                drawTarget(canvas);
            }
            if (_flag == DRAG_POINT) {
                dragNodeDraw(canvas);
            }
        }
    }

    public PointF getScreenCenterPoint() {
        int w = this.getWidth();
        int h = this.getHeight();
        _screenCenterPoint = new PointF(w / 2, h / 2);
        return _screenCenterPoint;
    }

    /**
     * 橡皮绳
     *
     * @param points 1,2
     */
    public void setPoints(PointF... points) {
        _isRelease = true;
        _flag = DRAG_NORMAL;
        if (points == null) {
            _screenPoint1 = null;
            _screenPoint2 = null;
            return;
        }
        if (points.length == 1) {
            _screenPoint1 = points[0];
            _screenPoint2 = null;
        }
        if (points.length == 2) {
            _screenPoint1 = points[0];
            _screenPoint2 = points[1];
        }
        postInvalidate();
    }

    public void setText(String[] text) {
        this._text = text;
        postInvalidate();
    }


    private void dragHelpLine(Canvas canvas) {
        getScreenCenterPoint();
        if (_screenPoint1 == null) {
            _path = null;
        }
        /**
         * 橡皮线
         */
        if (_screenPoint1 != null && _screenPoint2 == null) {
            _path = new Path();
            _path.moveTo(_screenCenterPoint.x, _screenCenterPoint.y);
            _path.lineTo(_screenPoint1.x, _screenPoint1.y);
        }
        /**
         * 橡皮面
         */
        if (_screenPoint1 != null && _screenPoint2 != null) {
            _path = new Path();
            _path.moveTo(_screenPoint1.x, _screenPoint1.y);
            _path.lineTo(_screenCenterPoint.x, _screenCenterPoint.y);
            _path.lineTo(_screenPoint2.x, _screenPoint2.y);
        }
        if (_path != null) {
            canvas.drawPath(_path, _paint);
        }
    }


    /**
     * *********************************************已下非橡皮绳************************************************
     */
    /**
     * 初始化绘制参数
     */
    private void initPolygonDraw() {
        if (_oPointFS != null) {
            _mPointFS = new ArrayList<>();
            PointF _pf;
            for (int i = 0; i < _oPointFS.size(); i++) {
                _pf = _oPointFS.get(i);
                _mPointFS.add(new PointF(_pf.x + _vx, _pf.y + _vy));
            }
        }
    }

    /**
     * 绘制
     *
     * @param canvas
     */
    private void dragPolygonDraw(Canvas canvas) {
        initPolygonDraw();
        if (_mPointFS != null) {
            _path = new Path();
            for (int i = 0; i < _mPointFS.size(); i++) {
                if (i == 0) {
                    _path.moveTo(_mPointFS.get(i).x, _mPointFS.get(i).y);
                } else {
                    _path.lineTo(_mPointFS.get(i).x, _mPointFS.get(i).y);
                }
            }
            if (_isClose) {
                _path.close();
            }
            canvas.drawPath(_path, _paint);
        }
    }

    /**
     * 绘制拖拽多边形
     */
    public void startPolygonDraw() {
        _flag = DRAG_POLYGON_POLYLINE;
        _isRelease = false;
        postInvalidate();
    }

    public SketchHelpView setPointFS(List<PointF> pointFS) {
        this._oPointFS = pointFS;
        return this;
    }

    public SketchHelpView setClose(boolean close) {
        _isClose = close;
        return this;
    }

    public SketchHelpView setVT(float vx, float vy) {
        this._vx = vx;
        this._vy = vy;
        return this;
    }

    /**
     * *********************************************************************************************
     */
    public SketchHelpView setTargetPoint(PointF mbPoint) {
        this._mbPoint = mbPoint;
       // VibratorUtil.Vibrate(_context, 100);
        return this;
    }


    private void initTargetDraw() {
        _targetBitmap = BitmapFactory.decodeResource(_context.getResources(), R.mipmap.gmmfc_draw_tool_node_edit);
    }

    public void drawTargetView() {
        _flag = DRAW_TARGET;
        _isRelease = false;
        initTargetDraw();
        postInvalidate();
    }

    private void drawTarget(Canvas canvas) {
        int w = _targetBitmap.getWidth();
        int h = _targetBitmap.getHeight();
        _paint.setAlpha(_alpha);
        canvas.drawBitmap(_targetBitmap, _mbPoint.x - w / 2, _mbPoint.y - h, _paint);
    }


    public SketchHelpView dragNodeDraw(PointF aPoint, PointF bPoint, PointF cPoint, PointF mbPoint) {
        this._aPoint = aPoint;
        this._bPoint = bPoint;
        this._cPoint = cPoint;
        this._mbPoint = mbPoint;
        return this;
    }

    public void dragNodeDraw() {
        _flag = DRAG_POINT;
        postInvalidate();
    }

    // a*----------b*-----------c*
    private void dragNodeDraw(Canvas canvas) {
        drawTarget(canvas);
        _path = new Path();
        if (_aPoint != null && _bPoint != null && _cPoint != null) {
            _path.moveTo(_aPoint.x, _aPoint.y);
            _path.lineTo(_bPoint.x, _bPoint.y);
            _path.lineTo(_cPoint.x, _cPoint.y);
            _path.lineTo(_mbPoint.x, _mbPoint.y);
            _path.close();
        }
        if (_aPoint == null && _bPoint != null && _cPoint != null) {
            _path.moveTo(_bPoint.x, _bPoint.y);
            _path.lineTo(_mbPoint.x, _mbPoint.y);
        }
        if (_aPoint != null && _bPoint != null && _cPoint == null) {
            _path.moveTo(_bPoint.x, _bPoint.y);
            _path.lineTo(_mbPoint.x, _mbPoint.y);
        }
        canvas.drawPath(_path, _paint);
    }

    public PointF getEditPoint() {
        return _mbPoint;
    }

    /**
     * *********************************************************************************************
     */
    public int getFlag() {
        return _flag;
    }


    /**
     * *********************************************************************************************
     */
    public void clear(Boolean holdSplitArea) {
        _flag = DRAG_NOTHING;
        releaseDraw(holdSplitArea == null ? false : holdSplitArea);
        postInvalidate();
    }

    private void releaseDraw(boolean holdSplitArea) {
        _isRelease = true;
        if (_oPointFS != null) {
            _oPointFS.clear();
        }
        if (_mPointFS != null) {
            _mPointFS.clear();
        }
        if (_targetBitmap != null) {
            _targetBitmap.recycle();
        }
        if (!holdSplitArea) {
            _info = null;
        }
        mLineInfo = null;
        _text = null;
    }

    private Map<PointF, Double> _info;

    public void splitInfo(Map<PointF, Double> info) {
        this._info = info;
    }

    /**
     * 线段上填写的信息
     */
    private Map<Path,Double> mLineInfo;

    public void setLineInfo(Map<Path,Double> mLineInfo) {
        this.mLineInfo = mLineInfo;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if(_targetBitmap!=null){
            _targetBitmap.recycle();
        }
        if(_crossHair!=null){
            _crossHair.recycle();
        }
    }
}
