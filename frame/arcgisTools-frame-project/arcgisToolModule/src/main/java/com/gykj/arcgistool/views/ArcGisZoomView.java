package com.gykj.arcgistool.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.esri.arcgisruntime.mapping.Viewpoint;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.gykj.arcgistool.R;
import com.gykj.arcgistool.common.IToolBar;
import com.gykj.arcgistool.listener.IZoomClickListener;
import com.gykj.arcgistool.utils.Util;

import androidx.annotation.Nullable;

public class ArcGisZoomView extends LinearLayout implements IToolBar {
    private LinearLayout linearLayoutView,zoomBgView,zoomInLayout,zoomoutLayout,mapRotateLayout,mapMainLayout;
    private ImageView zoomInView,zoomOutView,mapRotateView,mapMainView;
    private TextView spiltLineView,spiltLineView2;
    private TextView zoomInTextView,zoomOutTextView,mapRotateTextView;
    protected MapView mMapView;
    private IZoomClickListener zoomClickListener=null;
    private int bgColor,fontColor,zoomInImage,zoomOutImage,mapRotateImage;
    private int zoomWidth,zoomHeight,fontSize;
    private double zoomInNum,zoomOutNum;
    private double mapRotateNum;
    private String mapRotateText;
    private String zoomInText,zoomOutText;
    private boolean isHorizontal=false,showText=false;

    public ArcGisZoomView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs,0);
    }
    public void init(MapView mMapView){
        this.mMapView=mMapView;
    }
    public ArcGisZoomView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        LayoutInflater.from(context).inflate(R.layout.zoom_view, this);
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.ViewAttr);
        initView();
        initAttr(ta);
    }
    private void initView(){
        linearLayoutView = (LinearLayout)findViewById(R.id.linearLayout);
        zoomInLayout= (LinearLayout)findViewById(R.id.zoom_in_layout);
        zoomoutLayout= (LinearLayout)findViewById(R.id.zoom_out_layout);
        mapRotateLayout = findViewById(R.id.zoom_rotate_layout);
        zoomBgView = (LinearLayout)findViewById(R.id.zoom_bg);
        zoomInView = (ImageView)findViewById(R.id.zoom_in);
        zoomOutView = (ImageView)findViewById(R.id.zoom_out);
        mapRotateView = findViewById(R.id.zoom_rotate);
        spiltLineView=(TextView)findViewById(R.id.spilt_line);
        spiltLineView2=(TextView)findViewById(R.id.spilt_line2);
        zoomInTextView=(TextView)findViewById(R.id.zoom_in_text);
        zoomOutTextView=(TextView)findViewById(R.id.zoom_out_text);
        mapRotateTextView=(TextView)findViewById(R.id.zoom_rotate_text);
        mapMainLayout = findViewById(R.id.zoom_main_layout);
        mapMainView = findViewById(R.id.zoom_main_view);

        mapMainLayout.setOnClickListener(listener);
        zoomInLayout.setOnClickListener(listener);
        zoomoutLayout.setOnClickListener(listener);
        mapRotateLayout.setOnClickListener(listener);
    }
    private void initAttr(TypedArray ta){
        bgColor=ta.getResourceId(R.styleable.ViewAttr_view_background,R.drawable.sddman_shadow_bg);
        zoomWidth=ta.getDimensionPixelSize(R.styleable.ViewAttr_button_width, Util.valueToSp(getContext(),35));
        zoomHeight=ta.getDimensionPixelSize(R.styleable.ViewAttr_button_height, Util.valueToSp(getContext(),35));
        isHorizontal=ta.getBoolean(R.styleable.ViewAttr_is_horizontal,false);
        showText=ta.getBoolean(R.styleable.ViewAttr_show_text,false);
        zoomInNum=ta.getDimension(R.styleable.ViewAttr_zoom_in_num,2);
        zoomOutNum=ta.getDimension(R.styleable.ViewAttr_zoom_out_num,2);
        fontSize=ta.getInt(R.styleable.ViewAttr_font_size,12);
        zoomInImage=ta.getResourceId(R.styleable.ViewAttr_zoom_in_image,R.drawable.sddman_zoomin);
        zoomOutImage=ta.getResourceId(R.styleable.ViewAttr_zoom_out_image,R.drawable.sddman_zoomout);
        fontColor=ta.getResourceId(R.styleable.ViewAttr_font_color,R.color.gray);
        zoomInText=ta.getString(R.styleable.ViewAttr_zoom_in_text);
        zoomOutText=ta.getString(R.styleable.ViewAttr_zoom_out_text);

        mapRotateNum=ta.getDimension(R.styleable.ViewAttr_map_rotate_num,90);
        mapRotateImage=ta.getResourceId(R.styleable.ViewAttr_map_rotate_image,R.drawable.sddman_rotate);
        mapRotateText=ta.getString(R.styleable.ViewAttr_map_rotate_text);
        setZoomDpWidth(zoomWidth);
        setZoomDpHeight(zoomHeight);
        setShowText(showText);
    }
    private OnClickListener listener=new OnClickListener() {
        @Override
        public void onClick(View view) {
            int i = view.getId();
            if (i == R.id.zoom_in_layout) {
                double scale = mMapView.getMapScale();
                mMapView.setViewpointScaleAsync(scale * (1.0/zoomInNum));
                if(zoomClickListener!=null) {
                    zoomClickListener.zoomInClick(view);
                }
            }else if (i == R.id.zoom_out_layout) {
                double scale = mMapView.getMapScale();
                mMapView.setViewpointScaleAsync(scale * zoomOutNum);
                if(zoomClickListener!=null){
                    zoomClickListener.zoomOutClick(view);
                }
            }else if(i== R.id.zoom_rotate_layout){
                double rotate=mMapView.getMapRotation();
                mMapView.setViewpointRotationAsync(rotate-mapRotateNum);
                if(zoomClickListener!=null){
                    zoomClickListener.mapRotateClick(view);
                }
            } else if(i== R.id.zoom_main_layout){
                Viewpoint viewpoint= mMapView.getMap().getInitialViewpoint();
                mMapView.setViewpointAsync(viewpoint);
                if(zoomClickListener!=null){
                    zoomClickListener.mapMainClick(view);
                }
            }
        }
    };
    private void setZoomDpWidth(int w){
        zoomWidth=w;
        zoomInView.getLayoutParams().width=w;
        zoomOutView.getLayoutParams().width=w;
        mapRotateView.getLayoutParams().width=w;
        LinearLayout.LayoutParams linearParams =(LinearLayout.LayoutParams) spiltLineView.getLayoutParams();
        linearParams.width = w-10;
        spiltLineView.setLayoutParams(linearParams);
    }
    private void setZoomDpHeight(int h){
        zoomHeight=h;
        zoomInView.getLayoutParams().height=h;
        zoomOutView.getLayoutParams().height=h;
        mapRotateView.getLayoutParams().height=h;
    }
    public void setZoomClickListener(IZoomClickListener zoomClickListener) {
        this.zoomClickListener = zoomClickListener;
    }
    private void setShowText(boolean showText) {
        this.showText = showText;
        int padding= Util.valueToSp(getContext(),5);
        if(showText){
            zoomInTextView.setVisibility(View.VISIBLE);
            zoomOutTextView.setVisibility(View.VISIBLE);
            mapRotateTextView.setVisibility(VISIBLE);
            zoomInView.setPadding(padding,padding,padding,0);
            zoomOutView.setPadding(padding,padding,padding,0);
            mapRotateTextView.setPadding(padding,padding,padding,0);
        }else{
            zoomInTextView.setVisibility(View.GONE);
            zoomOutTextView.setVisibility(View.GONE);
            mapRotateTextView.setVisibility(GONE);
            zoomInView.setPadding(padding,padding,padding,padding);
            zoomOutView.setPadding(padding,padding,padding,padding);
            mapRotateTextView.setPadding(padding,padding,padding,padding);
        }
    }
}
