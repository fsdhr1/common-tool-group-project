package com.grandtech.mapframe.ui.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.DrawableContainer;
import android.graphics.drawable.StateListDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.core.content.ContextCompat;

import com.grandtech.mapframe.ui.R;
import com.grandtech.mapframe.ui.util.MeasureUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName ToolView
 * @Description TODO
 * @Author: fs
 * @Date: 2021/5/13 10:08
 * @Version 2.0
 */
public class ToolView extends LinearLayout implements View.OnTouchListener,IToolView{

    private boolean mInitOk = false;
    /**
     * 是不是按下事件 true标识是按下后抬起
     */
    boolean mIsNormalBnt;
    /**
     * 判断互斥的toolview
     */
    private Boolean mIsExclusion;

    private List<String> mExclusionToolNames = new ArrayList<>();
    /**
     * 判断是不是地图绘制相关的工具 false表示不是,默认false
     */
    boolean mIsMapSketchTool;

    /**
     * 判断是不是允许在收起工具栏时该工具按钮被收起 true表示允许,并且默认初始就会被收起
     */
    boolean mCanPutNavigate;

    private int mNormalRes;

    private int mPressedRes;
    @ColorInt
    private int pressedTint;
    
    private String mToolName = "未命名";

    private IClick iClick;
    private ILongClick iLongClick;

    //子控件
    private TextView textView;
    private ImageView imageView;

    private boolean isCheck = false;

    //选中状态对应的系统资源
    private static final int[] CHECKED_STATE_SET = {
            android.R.attr.state_checked
    };

    public ToolView(@NonNull Context context, @NonNull String toolName, int normalRes, int pressedRes,  boolean isNormalBnt) {
        this(context,null);
        this.mIsNormalBnt = isNormalBnt;
        this.mNormalRes = normalRes;
        this.mPressedRes = pressedRes;
        this.mToolName = toolName;
        init();
    }
    public ToolView(@NonNull Context context, @NonNull String toolName, int normalRes, int pressedRes,  boolean isNormalBnt, boolean isMapSketchTool) {
        this(context,null);
        this.mIsNormalBnt = isNormalBnt;
        this.mNormalRes = normalRes;
        this.mPressedRes = pressedRes;
        this.mToolName = toolName;
        this.mIsMapSketchTool = isMapSketchTool;
        init();
    }
    public ToolView(@NonNull Context context, @NonNull String toolName, int normalRes, int pressedRes,  boolean isNormalBnt, boolean isMapSketchTool, boolean canPutNavigate) {
        this(context,null);
        this.mIsNormalBnt = isNormalBnt;
        this.mNormalRes = normalRes;
        this.mPressedRes = pressedRes;
        this.mToolName = toolName;
        this.mIsMapSketchTool = isMapSketchTool;
        this.mCanPutNavigate = canPutNavigate;
        init();
    }
    public ToolView(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    @SuppressLint("ResourceType")
    public ToolView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        if(attrs!=null){
            TypedArray attrArray = context.obtainStyledAttributes(attrs, R.styleable.map_ui);
            this.mIsNormalBnt = attrArray.getBoolean(R.styleable.map_ui_map_ui_isNormalBnt,false);
            this.mIsExclusion =  attrArray.getBoolean(R.styleable.map_ui_map_ui_isExclusion,!this.mIsNormalBnt);
            this.mIsMapSketchTool = attrArray.getBoolean(R.styleable.map_ui_map_ui_isMapSketchTool,false);
            this.mNormalRes = attrArray.getResourceId(R.styleable.map_ui_map_ui_normal,View.NO_ID);
            this.mPressedRes = attrArray.getResourceId(R.styleable.map_ui_map_ui_pressed,View.NO_ID);
            this.mCanPutNavigate = attrArray.getBoolean(R.styleable.map_ui_map_ui_canPutNavigate,false);
            this.pressedTint = attrArray.getColor(R.styleable.map_ui_map_ui_pressed_tint, 0);
            String toolname = attrArray.getString(R.styleable.map_ui_map_ui_tool_name);
            this.mToolName = toolname == null?this.mToolName:toolname;
            attrArray.recycle();
            init();
        }

    }


    protected void init() {
        this.setOrientation(VERTICAL);
        textView = new TextView(getContext());
        textView.setGravity(Gravity.CENTER);
        textView.setTextSize(9);
        textView.setText(mToolName);
        textView.setTextColor(Color.parseColor("#666666"));
        imageView = new ImageView(getContext());
        imageView.setImageResource(mNormalRes);
        LayoutParams _layoutParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        this.addView(imageView, _layoutParams);
        this.addView(textView, _layoutParams);
        this.setOnTouchListener(this);
        mInitOk = true;

    }

    /**
     * 设置工具按钮图标
     * @param normalRes
     * @param pressedRes
     */
    public void setIcon(int normalRes, int pressedRes){
        this.mNormalRes = normalRes;
        this.mPressedRes = pressedRes;
        setChecked(false);
    };

    /**
     * 设置工具按钮名称
     * @param toolName
     */
    public void setToolName(String toolName){
        if(mInitOk ){
            this.mToolName = toolName;
            textView.setText(mToolName);
        }
    };

    /**
     * 获取TextView
     * @return
     */
    public TextView getTextView() {
        return textView;
    }

    /**
     * 获取imageView
     * @return
     */
    public ImageView getImageView() {
        return imageView;
    }

    public void setNormalBnt(boolean _isNormalBnt)
    {
        this.mIsNormalBnt = _isNormalBnt;
    }

    @Override
    public int[] onCreateDrawableState(int extraSpace) {
        //固定写法
        final int[] drawableState = super.onCreateDrawableState(extraSpace + 1);
        //判断是否选择
        if (isChecked()) {
            //如果选择, 把选择的状态, 合并到现有的状态中.
            mergeDrawableStates(drawableState, CHECKED_STATE_SET);
        }
        return drawableState;
    }


    @Override
    public void setChecked(boolean checked) {
        if (mInitOk) {
            isCheck = checked;
            if (checked) {
                imageView.setImageResource(mPressedRes);
                if(pressedTint != 0){
                    imageView.setColorFilter(pressedTint);
                }
            } else {
                imageView.setImageResource(mNormalRes);
                if(pressedTint != 0){
                    imageView.setColorFilter(null);
                }
            }
            //固定写法, 刷新drawable的状态
            refreshDrawableState();
        }
    }

    @Override
    public boolean isChecked(){
        return isCheck;
    }

    private float _x, _y;
    private double _dix = 20;
    private long startTime;
    private long interval = 500;

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            _x = event.getX();
            _y = event.getY();
            startTime = System.currentTimeMillis();
            if (longPressRunnable != null) {
                removeCallbacks(longPressRunnable);
                int eventPointCount = event.getPointerCount();
                if (event.getAction() == MotionEvent.ACTION_DOWN && eventPointCount == 1) {
                    postCheckForLongTouch(event.getX(), event.getY());
                }
            }
        }
        if (event.getAction() == MotionEvent.ACTION_MOVE) {
            if (mIsNormalBnt) {
                setChecked(true);
            }
        }
        if (event.getAction() == MotionEvent.ACTION_UP) {
            if (mIsNormalBnt) {
                setChecked(false);
            } else {
                if (isChecked()) {
                    this.setChecked(false);
                } else {
                    this.setChecked(true);
                }
            }
            if (System.currentTimeMillis() - startTime < interval) {
                removeCallbacks(longPressRunnable);
                if (iClick != null) {
                    Log.i("toolViewClick","toolViewClick");
                    iClick.toolViewClick(this);
                }
            }
        }
        if (event.getAction() == MotionEvent.ACTION_CANCEL) {
            if (mIsNormalBnt) {
                setChecked(false);
            }
            removeCallbacks(longPressRunnable);
        }
        return true;
    }


    @Override
    public void setIClick(IClick iClick) {
        this.iClick = iClick;
    }


    @Override
    public void setILongClick(ILongClick iLongClick) {
        this.iLongClick = iLongClick;
    }

    @Override
    public View getToolViewRoot() {
        return this;
    }

    private void postCheckForLongTouch(float x, float y) {
        longPressRunnable.setPressLocation(x, y);
        postDelayed(longPressRunnable, interval);
    }

    /**
     * 当长按事件发生时，要触发的任务
     */
    private LongPressRunnable longPressRunnable = new LongPressRunnable();

    private class LongPressRunnable implements Runnable {

        private int x, y;

        public void setPressLocation(float x, float y) {
            this.x = (int) x;
            this.y = (int) y;
        }

        @Override
        public void run() {
            if (iLongClick != null) {
                iLongClick.toolViewLongClick(ToolView.this);
            }
        }

    }

    /**
     * 是否是地图事件独占根据是否是按下事件判断
     * @return
     */
    @Override
    public boolean isExclusion() {
        return mIsExclusion == null?!mIsNormalBnt:mIsExclusion;
    }

    public void setIsExclusion(Boolean isExclusion) {
        this.mIsExclusion = isExclusion;
    }

    /**
     * 添加互斥的Toolview
     * @param toolName
     * @return
     */
    public ToolView addExclusionToolName(String toolName){
       if(mExclusionToolNames != null) {
            mExclusionToolNames.add(toolName);
        }
        return this;
    }


    @Override
    public List<String> getExclusionToolNames() {
        return mExclusionToolNames;
    }

    /**
     * 获取工具名称
     * @return
     */
    @Override
    public String getmToolName() {
        return mToolName;
    }

    /**
     * 判断是不是地图绘制相关的工具 false表示不是,默认false
     * @return
     */
    @Override
    public boolean isMapSketchTool() {
        return mIsMapSketchTool;
    }

    @Override
    public boolean canPutNavigate() {
        return mCanPutNavigate;
    }
}
