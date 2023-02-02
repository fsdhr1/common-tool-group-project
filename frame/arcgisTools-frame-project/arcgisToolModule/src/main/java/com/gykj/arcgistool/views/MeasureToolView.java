package com.gykj.arcgistool.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.esri.arcgisruntime.mapping.view.DefaultMapViewOnTouchListener;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.gykj.arcgistool.R;
import com.gykj.arcgistool.common.DrawType;
import com.gykj.arcgistool.common.IToolBar;
import com.gykj.arcgistool.common.MapViewOnTouchListener;
import com.gykj.arcgistool.common.Measure;
import com.gykj.arcgistool.common.Variable;
import com.gykj.arcgistool.entity.DrawEntity;
import com.gykj.arcgistool.listener.IMeasureClickListener;
import com.gykj.arcgistool.utils.ArcGisMeasure;
import com.gykj.arcgistool.utils.Util;

import androidx.annotation.Nullable;

public class MeasureToolView extends LinearLayout implements IToolBar {
    private Context context;
    private ArcGisMeasure arcgisMeasure;
    private MapView mMapView;
    private LinearLayout measureBgView,measurePrevlayout,measureNextlayout,measureLengthLayout,measureAreaLayout,measureClearLayout,measureEndLayout;
    private ImageView prevImageView,nextImageView,lengthImageView,areaImageView,clearImageView,endImageView;
    private TextView measurePrevText,measureNextText,measureLengthText,measureAreaText,measureClearText,measureEndText;
    private int bgColor,fontColor,measurePrevImage,measureNextImage,measureLengthImage,measureAreaImage,measureClearImage,measureEndImage;
    private int buttonWidth,buttonHeight,fontSize;
    private boolean isHorizontal,showText=false;
    private String measurePrevStr,measureNextStr,measureLengthStr,measureAreaStr,measureClearStr,measureEndStr;
    private DrawType drawType=null;
    private Measure measureLengthType=Measure.M;
    private Measure measureAreaType=Measure.M2;
    private IMeasureClickListener measureClickListener;
    private DefaultMapViewOnTouchListener defaultMapViewOnTouchListener;
    private MapViewOnTouchListener mapListener;
    private boolean enable=false;
    public MeasureToolView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }

    public MeasureToolView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context=context;
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.ViewAttr);
        isHorizontal=ta.getBoolean(R.styleable.ViewAttr_is_horizontal,true);
        if(!isHorizontal) {
            LayoutInflater.from(context).inflate(R.layout.measure_tool_vertical_view, this);
        }else{
            LayoutInflater.from(context).inflate(R.layout.measure_tool_horizontal_view, this);
        }

        initView();
        initAttr(ta);
    }
    /**
     *
     */
    public void setMapClickCallBack(MapViewOnTouchListener mapListener){
        this.mapListener=mapListener;
    }
    public void init(MapView mMapView){
        this.mMapView=mMapView;
        arcgisMeasure=new ArcGisMeasure(context,mMapView);
        arcgisMeasure.setSpatialReference(mMapView.getSpatialReference());
        registerMapEvent();
    }

    private void  registerMapEvent(){
        defaultMapViewOnTouchListener=new DefaultMapViewOnTouchListener(context,mMapView){
            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                onMapSingleTapUp(e);
                if(mapListener!=null){
                    super.onSingleTapUp(e);
                    return mapListener.onSingleTapUp(e);
                }
                return super.onSingleTapUp(e);
            }

            @Override
            public boolean onDoubleTap(MotionEvent e) {
                if(mapListener!=null) {
                    super.onDoubleTap(e);
                    return mapListener.onDoubleTap(e);
                }
                return super.onDoubleTap(e);
            }
            @Override
            public boolean onDoubleTouchDrag(MotionEvent e) {
                if(mapListener!=null) {
                    super.onDoubleTouchDrag(e);
                    return mapListener.onDoubleTouchDrag(e);
                }
                return super.onDoubleTouchDrag(e);
            }
            @Override
            public boolean  onFling(MotionEvent e1,MotionEvent e2,float velocityX, float velocityY) {
                if(mapListener!=null) {
                    super.onFling(e1,e2,velocityX,velocityY);
                    return mapListener.onFling(e1,e2,velocityX,velocityY);
                }
                return super.onFling(e1,e2,velocityX,velocityY);
            }
            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                if(mapListener!=null) {
                    super.onScroll(e1, e2, distanceX, distanceY);
                    return mapListener.onScroll(e1, e2, distanceX, distanceY);
                }
                return super.onScroll(e1, e2, distanceX, distanceY);
            }

            @Override
            public boolean onRotate(MotionEvent event, double rotationAngle) {
                if(mapListener!=null) {
                    super.onRotate(event, rotationAngle);
                    return mapListener.onRotate(event, rotationAngle);
                }
                return super.onRotate(event, rotationAngle);
            }

            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                if(mapListener!=null) {
                    super.onScale(detector);
                    return mapListener.onScale(detector);
                }
                return super.onScale(detector);
            }

            @Override
            public boolean onDown(MotionEvent e) {
                if(mapListener!=null) {
                    super.onDown(e);
                    return mapListener.onDown(e);
                }
                return super.onDown(e);
            }

            @Override
            public boolean onUp(MotionEvent e) {
                if(mapListener!=null) {
                    super.onUp(e);
                    return mapListener.onUp(e);
                }
                return super.onUp(e);
            }

            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if(mapListener!=null) {
                    super.onTouch(view, event);
                    return mapListener.onTouch(view, event);
                }
                return super.onTouch(view, event);
            }

            @Override
            public void onLongPress(MotionEvent e) {
                if(mapListener!=null) {
                    mapListener.onLongPress(e);
                }
                super.onLongPress(e);
            }

            @Override
            public boolean onDoubleTapEvent(MotionEvent e) {
                if(mapListener!=null) {
                    super.onDoubleTapEvent(e);
                    return mapListener.onDoubleTapEvent(e);
                }
                return super.onDoubleTapEvent(e);
            }

            @Override
            public boolean onMultiPointerTap(MotionEvent event) {
                if(mapListener!=null) {
                    super.onMultiPointerTap(event);
                    return mapListener.onMultiPointerTap(event);
                }
                return super.onMultiPointerTap(event);
            }

            @Override
            public boolean onScaleBegin(ScaleGestureDetector detector) {
                if(mapListener!=null) {
                    super.onScaleBegin(detector);
                    return mapListener.onScaleBegin(detector);
                }
                return super.onScaleBegin(detector);
            }

            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                if(mapListener!=null) {
                    super.onSingleTapConfirmed(e);
                    return mapListener.onSingleTapConfirmed(e);
                }
                return super.onSingleTapConfirmed(e);
            }

            @Override
            public void onScaleEnd(ScaleGestureDetector detector) {
                if(mapListener!=null) {
                    mapListener.onScaleEnd(detector);
                }
                super.onScaleEnd(detector);
            }

            @Override
            public void onShowPress(MotionEvent e) {
                if(mapListener!=null) {
                    mapListener.onShowPress(e);
                }
                super.onShowPress(e);
            }
        };
        mMapView.setOnTouchListener(defaultMapViewOnTouchListener);
        enable=true;
    }

    private void initView(){
        prevImageView=(ImageView)findViewById(R.id.measure_prev);
        nextImageView=(ImageView)findViewById(R.id.measure_next);
        lengthImageView=(ImageView)findViewById(R.id.measure_length);
        areaImageView=(ImageView)findViewById(R.id.measure_area);
        clearImageView=(ImageView)findViewById(R.id.measure_clear);
        endImageView=(ImageView)findViewById(R.id.measure_end);
        measureBgView=(LinearLayout)findViewById(R.id.measure_bg);
        measurePrevlayout=(LinearLayout)findViewById(R.id.measure_prev_layout);
        measureNextlayout=(LinearLayout)findViewById(R.id.measure_next_layout);
        measureLengthLayout=(LinearLayout)findViewById(R.id.measure_length_layout);
        measureAreaLayout=(LinearLayout)findViewById(R.id.measure_area_layout);
        measureClearLayout=(LinearLayout)findViewById(R.id.measure_clear_layout);
        measureEndLayout=(LinearLayout)findViewById(R.id.measure_end_layout);
        measurePrevText=(TextView)findViewById(R.id.measure_prev_text);
        measureNextText=(TextView)findViewById(R.id.measure_next_text);
        measureLengthText=(TextView)findViewById(R.id.measure_length_text);
        measureAreaText=(TextView)findViewById(R.id.measure_area_text);
        measureClearText=(TextView)findViewById(R.id.measure_clear_text);
        measureEndText=(TextView)findViewById(R.id.measure_end_text);

        measurePrevlayout.setOnClickListener(listener);
        measureNextlayout.setOnClickListener(listener);
        measureLengthLayout.setOnClickListener(listener);
        measureAreaLayout.setOnClickListener(listener);
        measureClearLayout.setOnClickListener(listener);
        measureEndLayout.setOnClickListener(listener);
    }

    private void initAttr(TypedArray ta){
        bgColor=ta.getResourceId(R.styleable.ViewAttr_view_background,R.drawable.sddman_shadow_bg);
        buttonWidth=ta.getDimensionPixelSize(R.styleable.ViewAttr_button_width, Util.valueToSp(getContext(),35));
        buttonHeight=ta.getDimensionPixelSize(R.styleable.ViewAttr_button_height, Util.valueToSp(getContext(),35));
        showText=ta.getBoolean(R.styleable.ViewAttr_show_text,false);
        measurePrevStr=ta.getString(R.styleable.ViewAttr_measure_prev_text);
        measureNextStr=ta.getString(R.styleable.ViewAttr_measure_next_text);
        measureLengthStr=ta.getString(R.styleable.ViewAttr_measure_length_text);
        measureAreaStr=ta.getString(R.styleable.ViewAttr_measure_area_text);
        measureClearStr=ta.getString(R.styleable.ViewAttr_measure_clear_text);
        measureEndStr=ta.getString(R.styleable.ViewAttr_measure_end_text);
        fontColor=ta.getResourceId(R.styleable.ViewAttr_font_color,R.color.gray);
        fontSize=ta.getInt(R.styleable.ViewAttr_font_size,12);
        measurePrevImage=ta.getResourceId(R.styleable.ViewAttr_measure_prev_image,R.drawable.sddman_measure_prev);
        measureNextImage=ta.getResourceId(R.styleable.ViewAttr_measure_next_image,R.drawable.sddman_measure_next);
        measureLengthImage=ta.getResourceId(R.styleable.ViewAttr_measure_length_image,R.drawable.sddman_measure_length);
        measureAreaImage=ta.getResourceId(R.styleable.ViewAttr_measure_area_image,R.drawable.sddman_measure_area);
        measureClearImage=ta.getResourceId(R.styleable.ViewAttr_measure_clear_image,R.drawable.sddman_measure_clear);
        measureEndImage=ta.getResourceId(R.styleable.ViewAttr_measure_end_image,R.drawable.sddman_measure_end);

        setDpButtonWidth(buttonWidth);
        setDpButtonHeight(buttonHeight);
        setSohwText();
    }

    private OnClickListener listener=new OnClickListener()
    {
        @Override
        public void onClick(View view) {
            int i = view.getId();
            if (i == R.id.measure_prev_layout){
                if(!enable) {
                    return;
                }
                boolean hasPrev=arcgisMeasure.prevDraw();
                if(measureClickListener!=null){
                    measureClickListener.prevClick(hasPrev);
                }
            }else if (i == R.id.measure_next_layout){
                if(!enable) {
                    return;
                }
                boolean hasNext=arcgisMeasure.nextDraw();
                if(measureClickListener!=null){
                    measureClickListener.nextClick(hasNext);
                }
            }else if (i == R.id.measure_length_layout){
                if(!enable) {
                    return;
                }
                if(!mMapView.getOnTouchListener().equals(defaultMapViewOnTouchListener)){
                    registerMapEvent();
                }
                drawType=DrawType.LINE;
                arcgisMeasure.endMeasure();
                measureLengthLayout.setBackgroundColor(getResources().getColor(R.color.black_1a));
                measureAreaLayout.setBackgroundColor(getResources().getColor(R.color.transparent));
                if(measureClickListener!=null){
                    measureClickListener.lengthClick();
                }
            }else if (i == R.id.measure_area_layout){
                if(!enable) {
                    return;
                }
                drawType=DrawType.POLYGON;
                arcgisMeasure.endMeasure();
                measureLengthLayout.setBackgroundColor(getResources().getColor(R.color.transparent));
                measureAreaLayout.setBackgroundColor(getResources().getColor(R.color.black_1a));
                if(measureClickListener!=null){
                    measureClickListener.areaClick();
                }
            }else if (i == R.id.measure_clear_layout){
                if(!enable) {
                    return;
                }
                drawType=null;
                DrawEntity draw=arcgisMeasure.clearMeasure();
                measureLengthLayout.setBackgroundColor(getResources().getColor(R.color.transparent));
                measureAreaLayout.setBackgroundColor(getResources().getColor(R.color.transparent));
                if(measureClickListener!=null){
                    measureClickListener.clearClick(draw);
                }
            }else if (i == R.id.measure_end_layout){
                if(!enable) {
                    return;
                }
                drawType=null;
                DrawEntity draw=arcgisMeasure.endMeasure();
                measureLengthLayout.setBackgroundColor(getResources().getColor(R.color.transparent));
                measureAreaLayout.setBackgroundColor(getResources().getColor(R.color.transparent));
                if(measureClickListener!=null){
                    measureClickListener.endClick(draw);
                }
            }
        }
    };

    private void onMapSingleTapUp(MotionEvent e){
        if(drawType== DrawType.LINE) {
            arcgisMeasure.startMeasuredLength(e.getX(), e.getY());
        }else if(drawType== DrawType.POLYGON){
            arcgisMeasure.startMeasuredArea(e.getX(), e.getY());
        }
    }

    public void setMeasureClickListener(IMeasureClickListener measureClickListener) {
        this.measureClickListener = measureClickListener;
    }

    private void setDpButtonWidth(int buttonWidth) {
        this.buttonWidth = buttonWidth;
        prevImageView.getLayoutParams().width=buttonWidth;
        nextImageView.getLayoutParams().width=buttonWidth;
        lengthImageView.getLayoutParams().width=buttonWidth;
        areaImageView.getLayoutParams().width=buttonWidth;
        clearImageView.getLayoutParams().width=buttonWidth;
        endImageView.getLayoutParams().width=buttonWidth;
    }
    private void setDpButtonHeight(int buttonHeight) {
        this.buttonHeight = buttonHeight;
        prevImageView.getLayoutParams().height=buttonHeight;
        nextImageView.getLayoutParams().height=buttonHeight;
        lengthImageView.getLayoutParams().height=buttonHeight;
        areaImageView.getLayoutParams().height=buttonHeight;
        clearImageView.getLayoutParams().height=buttonHeight;
        endImageView.getLayoutParams().height=buttonHeight;
    }
    private void setSohwText(){
        int view=showText?View.VISIBLE:View.GONE;
        measurePrevText.setVisibility(view);
        measureNextText.setVisibility(view);
        measureLengthText.setVisibility(view);
        measureAreaText.setVisibility(view);
        measureClearText.setVisibility(view);
        measureEndText.setVisibility(view);
    }
}
