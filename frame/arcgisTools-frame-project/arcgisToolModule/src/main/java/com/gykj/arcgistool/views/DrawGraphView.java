package com.gykj.arcgistool.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.esri.arcgisruntime.mapping.view.DefaultMapViewOnTouchListener;
import com.esri.arcgisruntime.mapping.view.Graphic;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.gykj.arcgistool.R;
import com.gykj.arcgistool.common.GraphType;
import com.gykj.arcgistool.common.IToolBar;
import com.gykj.arcgistool.common.Variable;
import com.gykj.arcgistool.listener.IDrawGraphListener;
import com.gykj.arcgistool.listener.IDrawListener;
import com.gykj.arcgistool.utils.GraphicDraw;
import com.gykj.arcgistool.utils.Util;

import androidx.annotation.Nullable;

public class DrawGraphView extends LinearLayout implements IToolBar {
    private Context context;
    private MapView mMapView;
    private LinearLayout drawingBgView,drawingLineLayout,drawingPolygonLayout,drawingOrthogonLayout,drawingCircleLayout,
            drawingEllipseLayout,drawingRhombusLayout,clearDrawLayout,drawingTextLayout,drawingEndLayout;
    private ImageView drawingLineImageView,drawingPolygonImageView,drawingOrthogonImageView,drawingCircleImageView,
            drawingEllipseImageView,drawingRhombusImageView,drawingTextView;
    private TextView drawingLineText,drawingPolygonText,drawingOrthogonText,drawingCircleText,
            drawingEllipseText,drawingRhombusText,clearDrawText,drawingTextTv,drawingEndTv;
    private int bgColor,fontColor,drawingLineImage,drawingPolygonImage,drawingOrthogonImage,drawingCircleImage,
            drawingEllipseImage,drawingRhombusImage;
    private int buttonWidth,buttonHeight,fontSize;
    private boolean isHorizontal,showText=false;
    private GraphType graphType=null;
    private String drawingLineStr,drawingPolygonStr,drawingOrthogonStr,drawingCircleStr,drawingEllipseStr,drawingRhombusStr,clearDrawStr;
    private GraphicDraw graphicDraw;
    public DrawGraphView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }
    private IDrawGraphListener drawGraphListener;
    public DrawGraphView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context=context;
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.ViewAttr);
        isHorizontal=ta.getBoolean(R.styleable.ViewAttr_is_horizontal,true);
        LayoutInflater.from(context).inflate(R.layout.drawing_tool_horizontal_view, this);
        initView();
        initAttr(ta);

    }
    private boolean enable=false;

    /***
     * 初始化工具条，注入地图控件
     * @param mapView 地图
     */
    public void init(MapView mapView){
        this.mMapView=mapView;
        graphicDraw=new GraphicDraw(context,mMapView);
        this.graphicDraw.init();
        this.graphicDraw.setDrawListener(new IDrawListener() {
            @Override
            public void onDrawEnd(GraphType graphType, Graphic graphic) {
                clearBgColor();
                if(drawGraphListener!=null){
                    drawGraphListener.drawEnd(graphType,graphic);
                }
            }
            @Override
            public void onDrawStart(GraphType graphType) {
                clearBgColor();
                if(graphType== GraphType.LINE){
                    drawingLineLayout.setBackgroundColor(getResources().getColor(R.color.black_1a));
                }
                if(graphType== GraphType.BOX){
                    drawingOrthogonLayout.setBackgroundColor(getResources().getColor(R.color.black_1a));
                }
                if(graphType == GraphType.POLYGON){
                    drawingPolygonLayout.setBackgroundColor(getResources().getColor(R.color.black_1a));
                }
                if(drawGraphListener!=null){
                    drawGraphListener.drawStart(graphType);
                }
            }
        });
        registerMapEvent();
    }
    private DefaultMapViewOnTouchListener defaultMapViewOnTouchListener;
    private void registerMapEvent() {
        defaultMapViewOnTouchListener=new DefaultMapViewOnTouchListener(context,mMapView){
            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                try {
                    graphicDraw.drawing(e.getX(), e.getY());
                }catch (Exception ex){
                    Log.e("drawTool",ex.getMessage());
                }
                return super.onSingleTapConfirmed(e);
            }
        };
        mMapView.setOnTouchListener(defaultMapViewOnTouchListener);
        enable=true;
    }

    private void initView(){
        drawingLineImageView=(ImageView)findViewById(R.id.drawing_line);
        drawingPolygonImageView=(ImageView)findViewById(R.id.drawing_polygon);
        drawingOrthogonImageView=(ImageView)findViewById(R.id.drawing_orthogon);
        drawingCircleImageView=(ImageView)findViewById(R.id.drawing_circle);
        drawingEllipseImageView=(ImageView)findViewById(R.id.drawing_ellipse);
        drawingRhombusImageView=(ImageView)findViewById(R.id.drawing_rhombus);
        drawingTextView = findViewById(R.id.drawing_text);

        drawingLineText=(TextView)findViewById(R.id.drawing_line_text);
        drawingPolygonText=(TextView)findViewById(R.id.drawing_polygon_text);
        drawingOrthogonText=(TextView)findViewById(R.id.drawing_orthogon_text);
        drawingCircleText=(TextView)findViewById(R.id.drawing_circle_text);
        drawingEllipseText=(TextView)findViewById(R.id.drawing_ellipse_text);
        drawingRhombusText=(TextView)findViewById(R.id.drawing_rhombus_text);
        clearDrawText=(TextView)findViewById(R.id.clear_draw_text);
        drawingTextTv = findViewById(R.id.drawing_text_tv);
        drawingEndTv = findViewById(R.id.drawing_end_text);

        drawingBgView=(LinearLayout)findViewById(R.id.drawing_bg);
        drawingLineLayout=(LinearLayout)findViewById(R.id.drawing_line_layout);
        drawingPolygonLayout=(LinearLayout)findViewById(R.id.drawing_polygon_layout);
        drawingOrthogonLayout=(LinearLayout)findViewById(R.id.drawing_orthogon_layout);
        drawingCircleLayout=(LinearLayout)findViewById(R.id.drawing_circle_layout);
        drawingEllipseLayout=(LinearLayout)findViewById(R.id.drawing_ellipse_layout);
        drawingRhombusLayout=(LinearLayout)findViewById(R.id.drawing_rhombus_layout);
        clearDrawLayout=(LinearLayout)findViewById(R.id.drawing_clear_layout);
        drawingEndLayout= findViewById(R.id.drawing_end_layout);
        drawingTextLayout=findViewById(R.id.drawing_text_layout);

        drawingLineLayout.setOnClickListener(listener);
        drawingPolygonLayout.setOnClickListener(listener);
        drawingOrthogonLayout.setOnClickListener(listener);
        drawingCircleLayout.setOnClickListener(listener);
        drawingEllipseLayout.setOnClickListener(listener);
        drawingRhombusLayout.setOnClickListener(listener);
        clearDrawLayout.setOnClickListener(listener);
        drawingTextLayout.setOnClickListener(listener);
        drawingEndLayout.setOnClickListener(listener);
    }
    private void initAttr(TypedArray ta){
        bgColor=ta.getResourceId(R.styleable.ViewAttr_view_background,R.drawable.sddman_shadow_bg);
        buttonWidth=ta.getDimensionPixelSize(R.styleable.ViewAttr_button_width, Util.valueToSp(getContext(),35));
        buttonHeight=ta.getDimensionPixelSize(R.styleable.ViewAttr_button_height, Util.valueToSp(getContext(),35));
        showText=ta.getBoolean(R.styleable.ViewAttr_show_text,false);
        drawingLineStr=ta.getString(R.styleable.ViewAttr_drawing_line_text);
        drawingPolygonStr=ta.getString(R.styleable.ViewAttr_drawing_polygon_text);
        drawingOrthogonStr=ta.getString(R.styleable.ViewAttr_drawing_orthogon_text);
        drawingCircleStr=ta.getString(R.styleable.ViewAttr_drawing_circle_text);
        drawingEllipseStr=ta.getString(R.styleable.ViewAttr_drawing_ellipse_text);
        drawingRhombusStr=ta.getString(R.styleable.ViewAttr_drawing_rhombus_text);
        fontColor=ta.getResourceId(R.styleable.ViewAttr_font_color,R.color.gray);
        fontSize=ta.getInt(R.styleable.ViewAttr_font_size,12);
        drawingLineImage=ta.getResourceId(R.styleable.ViewAttr_drawing_line_image,R.drawable.sddman_drawing_line);
        drawingPolygonImage=ta.getResourceId(R.styleable.ViewAttr_drawing_polygon_image,R.drawable.sddman_drawing_polygon);
        drawingOrthogonImage=ta.getResourceId(R.styleable.ViewAttr_drawing_orthogon_image,R.drawable.sddman_drawing_orthogon);
        drawingCircleImage=ta.getResourceId(R.styleable.ViewAttr_drawing_circle_image,R.drawable.sddman_drawing_circle);
        drawingEllipseImage=ta.getResourceId(R.styleable.ViewAttr_drawing_ellipse_image,R.drawable.sddman_drawing_ellipse);
        drawingRhombusImage=ta.getResourceId(R.styleable.ViewAttr_drawing_rhombus_image,R.drawable.sddman_drawing_rhombus);
        setSohwText();
    }
    private void setSohwText(){
        int view=showText? View.VISIBLE:View.GONE;
        drawingLineText.setVisibility(view);
        drawingPolygonText.setVisibility(view);
        drawingOrthogonText.setVisibility(view);
        drawingCircleText.setVisibility(view);
        drawingEllipseText.setVisibility(view);
        drawingRhombusText.setVisibility(view);
        clearDrawText.setVisibility(view);
        drawingTextTv.setVisibility(view);
        drawingEndTv.setVisibility(view);
    }
    private OnClickListener listener=new OnClickListener() {
        @Override
        public void onClick(View view) {
            if(view.getId()==R.id.drawing_line_layout){
                if(!enable) {
                    return;
                }
                if(!mMapView.getOnTouchListener().equals(defaultMapViewOnTouchListener)){
                    registerMapEvent();
                }
                graphicDraw.startDraw(GraphType.LINE);

            }
            if(view.getId() == R.id.drawing_polygon_layout){
                if(!enable) {
                    return;
                }
                if(!mMapView.getOnTouchListener().equals(defaultMapViewOnTouchListener)){
                    registerMapEvent();
                }
                graphicDraw.startDraw(GraphType.POLYGON);
            }
            if(view.getId() == R.id.drawing_circle_layout){
                //todo 开始画圆
            }
            if(view.getId() == R.id.drawing_orthogon_layout){
                graphicDraw.startDraw(GraphType.BOX);
            }
            if(view.getId() == R.id.drawing_text_layout){
                //TODO 文本
            }
            if(view.getId() == R.id.drawing_clear_layout){
                graphicDraw.clearGraphics();
            }
            if(view.getId() == R.id.drawing_end_layout){
                if(!graphicDraw.hasEnd()) {
                    clearBgColor();
                    if(drawGraphListener!=null){
                        drawGraphListener.drawEnd(graphicDraw.getGraphType(),graphicDraw.endDraw());
                    }
                }
            }
        }
    };

    public IDrawGraphListener getDrawGraphListener() {
        return drawGraphListener;
    }

    public void setDrawGraphListener(IDrawGraphListener drawGraphListener) {
        this.drawGraphListener = drawGraphListener;
    }

    private void clearBgColor(){
        drawingLineLayout.setBackgroundColor(getResources().getColor(R.color.transparent));
        drawingPolygonLayout.setBackgroundColor(getResources().getColor(R.color.transparent));
        drawingOrthogonLayout.setBackgroundColor(getResources().getColor(R.color.transparent));
        drawingCircleLayout.setBackgroundColor(getResources().getColor(R.color.transparent));
        drawingEllipseLayout.setBackgroundColor(getResources().getColor(R.color.transparent));
        drawingRhombusLayout.setBackgroundColor(getResources().getColor(R.color.transparent));
    }
}
