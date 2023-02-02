package com.gykj.selectlinearlayout;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.gykj.selectlinearlayout.bean.Dict;
import com.gykj.selectlinearlayout.picker.SinglePicker;
import com.trello.rxlifecycle2.components.support.RxAppCompatActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * 下拉布局组件
 * <p>
 * Created by jyh on 2021-02-26
 */
public class SelectLinearLayout extends LinearLayout
        implements View.OnClickListener {

    private Context _context;
    private LayoutParams _layoutParams;
    private TextView textView;
    private ImageView imageView;

    private String tvName;// TextView名称
    private boolean includesAll;// 是否包括"全部"
    private List<Dict> dicts;// 字典集合
    private Dict operationDict;// 当前操作字典实体

    /**
     * 设置字典名称
     *
     * @param tvName
     */
    public void setTvName(String tvName) {
        this.tvName = tvName;
        textView.setText(tvName);
        if (includesAll) {
            textView.setText(tvName + "(全部)");
        }
    }

    /**
     * 设置下拉列表是否包括"全部"
     *
     * @param includesAll
     */
    public void setIncludesAll(boolean includesAll) {
        this.includesAll = includesAll;
    }

    /**
     * 设置字典集合（下拉列表数据）
     *
     * @param dicts
     */
    public void setDicts(List<Dict> dicts) {
        this.dicts = dicts;
    }

    /**
     * 获取当前选择的字典实体
     *
     * @return
     */
    public Dict getOperationDict() {
        return operationDict;
    }

    public void setOperationDict(Dict operationDict) {
        this.operationDict = operationDict;
    }

    List<Dict> addDicts;

    /**
     * 追加字典实体
     * @param dict
     */
    public void addDict(Dict dict) {
        if (addDicts == null)
            addDicts = new ArrayList<>();
        addDicts.add(dict);
    }

    private boolean openListener = true;// 是否点击事件

    /**
     * 设置下拉布局组件是否可点击，默认true
     * @param b
     */
    public void setOpenListener(Boolean b) {
        openListener = b;
    }

    /**
     * 设置文本颜色
     */
    public void setColor(int color) {
        textView.setTextColor(color);
    }

    public SelectLinearLayout(Context context) {
        super(context);
        init(context);
    }

    public SelectLinearLayout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public SelectLinearLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        _context = context;
        //((RxAppCompatActivity) _context).addIOnActivityResults(this);

        this.setFocusable(true);
        this.setFocusableInTouchMode(true);
        this.setOnClickListener(this);

        textView = new TextView(_context);
        textView.setGravity(Gravity.CENTER);
        textView.setTextSize(14);
        textView.setText(tvName);
        if (includesAll) {
            textView.setText(tvName + "(全部)");
        }
//        Drawable drawable = getResources().getDrawable(R.mipmap.ic_pulldown32_ffffff);
//        drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
//        textView.setCompoundDrawables(null, null, drawable, null);
        _layoutParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
        this.addView(textView, _layoutParams);

        imageView = new ImageView(_context);
        imageView.setImageDrawable(ContextCompat.getDrawable(_context, R.mipmap.ic_pulldown32_ffffff));
        int l = dip2px(_context, 15);
        _layoutParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        this.addView(imageView, _layoutParams);
    }

    /**
     * dip转px
     *
     * @param context
     * @param dipValue
     * @return
     */
    public static int dip2px(Context context, float dipValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }

    @Override
    public void onClick(View v) {
        if (!openListener) return;
        if (dicts == null)
            dicts = new ArrayList<>();
        if (addDicts != null && addDicts.size() > 0) {
            dicts.addAll(addDicts);
            addDicts.clear();
        }

        if (dicts.size() > 0) {
            SinglePicker<Dict> picker = new SinglePicker<>((Activity) _context, dicts);
            picker.setCanceledOnTouchOutside(false);
            int index = 0;
            if (operationDict != null) {
                for (int i = 0; i < dicts.size(); i++) {
                    if (dicts.get(i).getKey().equals(operationDict.getKey())) {
                        index = i;
                        break;
                    }
                }
            }
            picker.setCycleDisable(true);
            picker.setSelectedIndex(index);
            picker.setOnItemPickListener(new SinglePicker.OnItemPickListener<Dict>() {
                @Override
                public void onItemPicked(int index1, Dict item) {
                    if (operationDict != null && operationDict.getKey().equals(item.getKey()))
                        return;
                    operationDict = item;
                    if (item.getValue().equals("全部")) {
                        textView.setText(tvName + "(全部)");
                    } else {
                        textView.setText(tvName + "(" + item.getValue() + ")");
                    }
                    if (iSelectDictListen != null)
                        iSelectDictListen.selectDict(operationDict);
                }
            });
            picker.show();
        } else {
            Toast.makeText(_context, "未设置字典集合", Toast.LENGTH_SHORT);
        }
    }

    /**
     * 设置要显示的字典实体
     *
     * @param dict
     */
    public void setDict(Dict dict) {
        operationDict = dict;
        if (dict == null || dict.getValue() == null || dict.getValue().equals("")) {
            textView.setText(tvName);
            if (includesAll) {
                textView.setText(tvName + "(全部)");
            }
        } else {
            textView.setText(tvName + "(" + dict.getValue() + ")");
        }
        if (iSelectDictListen != null)
            iSelectDictListen.selectDict(operationDict);
    }

    /**
     * 选中后回调
     */
    public interface ISelectDict {
        void selectDict(Dict dict);
    }

    private ISelectDict iSelectDictListen;

    public void setISelectDictListen(ISelectDict iSelectDictListen) {
        this.iSelectDictListen = iSelectDictListen;
    }
}
