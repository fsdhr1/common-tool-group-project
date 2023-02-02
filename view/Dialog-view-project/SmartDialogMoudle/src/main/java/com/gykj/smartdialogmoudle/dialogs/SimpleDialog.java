package com.gykj.smartdialogmoudle.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.gykj.smartdialogmoudle.R;
import com.gykj.smartdialogmoudle.adapter.SimpleAdapter;
import com.gykj.smartdialogmoudle.adapter.SimpleSelectMoreAdapter;
import com.gykj.smartdialogmoudle.anims.FadeEnter.FadeEnter;
import com.gykj.smartdialogmoudle.anims.FadeExit.FadeExit;
import com.gykj.smartdialogmoudle.anims.FlipEnter.FlipBottomEnter;
import com.gykj.smartdialogmoudle.anims.FlipEnter.FlipLeftEnter;
import com.gykj.smartdialogmoudle.anims.FlipEnter.FlipTopEnter;
import com.gykj.smartdialogmoudle.anims.FlipExit.FlipHorizontalExit;
import com.gykj.smartdialogmoudle.anims.FlipExit.FlipVerticalExit;
import com.gykj.smartdialogmoudle.anims.SlideEnter.SlideBottomEnter;
import com.gykj.smartdialogmoudle.anims.SlideEnter.SlideLeftEnter;
import com.gykj.smartdialogmoudle.anims.SlideEnter.SlideRightEnter;
import com.gykj.smartdialogmoudle.anims.SlideEnter.SlideTopEnter;
import com.gykj.smartdialogmoudle.anims.SlideExit.SlideBottomExit;
import com.gykj.smartdialogmoudle.anims.SlideExit.SlideLeftExit;
import com.gykj.smartdialogmoudle.anims.SlideExit.SlideRightExit;
import com.gykj.smartdialogmoudle.anims.SlideExit.SlideTopExit;
import com.gykj.smartdialogmoudle.anims.ZoomEnter.ZoomInBottomEnter;
import com.gykj.smartdialogmoudle.anims.ZoomEnter.ZoomInEnter;
import com.gykj.smartdialogmoudle.anims.ZoomEnter.ZoomInLeftEnter;
import com.gykj.smartdialogmoudle.anims.ZoomEnter.ZoomInTopEnter;
import com.gykj.smartdialogmoudle.anims.ZoomExit.ZoomInExit;
import com.gykj.smartdialogmoudle.anims.ZoomExit.ZoomOutBottomExit;
import com.gykj.smartdialogmoudle.anims.ZoomExit.ZoomOutRightExit;
import com.gykj.smartdialogmoudle.base.BaseAnimatorSet;
import com.gykj.smartdialogmoudle.base.BaseDialog;
import com.gykj.smartdialogmoudle.base.DialogAnimType;
import com.gykj.smartdialogmoudle.bean.SelectDialogListBean;

import java.util.ArrayList;
import java.util.List;

public class SimpleDialog extends BaseDialog<SimpleDialog> implements View.OnClickListener {
    private TextView contentTxt;
    private TextView titleTxt;
    private TextView submitTxt;
    private TextView cancelTxt;
    private TextView tvSure;
    private TextView tvCell;
    private TextView tvLoading;
    private ImageView imgLoading;
    private ImageView ivTitle;
    private EditText etContent;
    private LinearLayout llNormal;
    private LinearLayout llInput;
    private LinearLayout llList;
    private RelativeLayout rlTitle;
    private ListView lvSelect;
    private View vLine;
    private Context mContext;
    private String content;
    private OnCloseListener comfirelistener,cancleListenner;
    private String positiveName = "确定";
    private String negativeName = "取消";
    private String title = "温馨提示";
    private String mSureMessage = "知道了";
    private String inputMessage = "";
    private String mCellName = "";
    private int mAlertType;
    public static final int NORMAL_TYPE = 0;
    public static final int WARNNING_TYPE = 1;
    public static final int INPUT_TYPE = 2;
    public static final int LOADING_TYPE = 3;
    public static final int SELECT_SINGE_TYPE = 4;
    public static final int SELECT_MORE_TYPE = 5;
    public static final int PROGRESS_TYPE = 6;

    private int mThemeBackgroudColor = 0;
    private int mThemeFontColor = 0;
    private int mCancleBackgroudColor = 0;
    private int mCancleFontColor = 0;
    private int mCustomPic = 0;
    private int mBootonBackColor = 0;
    private int mBootonFontColor = 0;
    private int mLoadingPic = 0;
    private String mLoadingText;
    private boolean hasSetBottonColor = false;
    private LinearLayout llLoadIng, llAlter, llContent;
    private List<SelectDialogListBean> mDataList;
    private ListSelectListener mSelectListener;
    private ListSelectMoreListener mSelectMoreListener;
    private List<SelectDialogListBean> mSelectedList;
    private SimpleSelectMoreAdapter moreSelectAdapter;
    private RelativeLayout rlPb;
    private ProgressBar progressBar;
    private TextView tvPs,tvPsTotal;
    private int mMax,mProgress,mPbDrawable=0;
    private SimpleAdapter singleAdapter;

    public SimpleDialog(Context context, int mAlertType) {
        super(context, R.style.SampleDialogV1);
        this.mContext = context;
        this.mAlertType = mAlertType;
    }

    public SimpleDialog(Context context, int themeResId, String content) {
        super(context, themeResId);
        this.mContext = context;
        this.content = content;
    }


    public SimpleDialog setSelectListData(List<SelectDialogListBean> dataList) {
        mDataList = dataList;
        if (mAlertType==SELECT_MORE_TYPE&&moreSelectAdapter!=null){
            SelectDialogListBean selectAll = new SelectDialogListBean();
            selectAll.setValue("全选");
            selectAll.setKey("-1-100");
            mDataList.add(0,selectAll);
            moreSelectAdapter.changeData(mDataList);
        }
        if (mAlertType==SELECT_SINGE_TYPE&&singleAdapter!=null){
            singleAdapter.setData(mDataList);
        }
        return this;
    }

    /**
     *
     * 多选dialog
     * */
    public SimpleDialog setMoreSelectListener(ListSelectMoreListener selectMoreListener){
        mSelectMoreListener = selectMoreListener;
        return this;
    }

    public SimpleDialog setSignSelectListener(ListSelectListener selectListener){
        mSelectListener = selectListener;
        return this;
    }

    public SimpleDialog setCancelClickListener(OnCloseListener listener) {
        this.cancleListenner = listener;
        return this;
    }

    public SimpleDialog setConfirmClickListener(OnCloseListener listener) {
        this.comfirelistener = listener;
        return this;
    }

    public SimpleDialog setTitle(String title) {
        this.title = title;
        refrash();
        return this;
    }

    public SimpleDialog setMakeSureMessage(String sureMessage) {
        this.mSureMessage = sureMessage;
        if (tvSure != null && tvSure.getVisibility() == View.VISIBLE) {
            tvSure.setText(sureMessage);
        }
        return this;
    }

    //设置dialog类型
    public SimpleDialog setDialogType(int mAlertType) {
        this.mAlertType = mAlertType;
        if (contentTxt != null) {
            changeAlertType(mAlertType);
        }
        return this;
    }

    //确认消息按钮
    public SimpleDialog setPositiveButton(String name) {
        this.positiveName = name;
        refrash();
        return this;
    }

    //拒绝消息按钮
    public SimpleDialog setNegativeButton(String name) {
        this.negativeName = name;
        refrash();
        return this;
    }

    public SimpleDialog setNegativeColor(int backgroudColorId, int fontColorId) {
        this.mCancleBackgroudColor = backgroudColorId;
        this.mCancleFontColor = fontColorId;
        if (cancelTxt != null && backgroudColorId > 0) {
            GradientDrawable background = (GradientDrawable) cancelTxt.getBackground();
            background.setColor(mContext.getResources().getColor(backgroudColorId));
        }
        if (cancelTxt != null && fontColorId > 0) {
            cancelTxt.setTextColor(mContext.getResources().getColor(fontColorId));
        }
        return this;
    }

    public SimpleDialog setThemeColor(int backgroudColorId, int fontColorId) {
        this.mThemeBackgroudColor = backgroudColorId;
        this.mThemeFontColor = fontColorId;
        if (submitTxt != null && backgroudColorId > 0) {
            GradientDrawable background = (GradientDrawable) submitTxt.getBackground();
            background.setColor(mContext.getResources().getColor(backgroudColorId));
            GradientDrawable rlTitleBackground = (GradientDrawable) rlTitle.getBackground();
            rlTitleBackground.setColor(mContext.getResources().getColor(backgroudColorId));
        }
        if (submitTxt != null && fontColorId > 0) {
            submitTxt.setTextColor(mContext.getResources().getColor(fontColorId));
            titleTxt.setTextColor(mContext.getResources().getColor(fontColorId));
        }
        if (mAlertType == WARNNING_TYPE && !hasSetBottonColor) {
            setWarnningBottomColor(backgroudColorId, fontColorId);
        }
        return this;
    }

    public SimpleDialog setWarnningBottomColor(int backgroudColorId, int fontColorId) {
        hasSetBottonColor = true;
        this.mBootonBackColor = backgroudColorId;
        this.mBootonFontColor = fontColorId;
        if (tvSure != null && backgroudColorId > 0) {
            GradientDrawable background = (GradientDrawable) tvSure.getBackground();
            background.setColor(mContext.getResources().getColor(backgroudColorId));
        }
        if (tvSure != null && fontColorId > 0) {
            tvSure.setTextColor(mContext.getResources().getColor(fontColorId));
        }
        return this;
    }

    public SimpleDialog setContent(String content) {
        this.content = content;
        if (contentTxt != null) {
            contentTxt.setText(content);
        }
        if(mAlertType == INPUT_TYPE&&etContent!=null){
            etContent.setText(content);
        }
        return this;
    }

    public SimpleDialog setRightImage(int sourceId) {
        this.mCustomPic = sourceId;
        if (ivTitle != null && sourceId != 0) {
            ivTitle.setVisibility(View.VISIBLE);
            ivTitle.setImageResource(sourceId);
        }
        return this;
    }

    public SimpleDialog setCellName(String cellName) {
        this.mCellName = cellName;
        if (mAlertType == INPUT_TYPE && tvCell != null && !TextUtils.isEmpty(cellName)) {
            tvCell.setText(cellName);
            tvCell.setVisibility(View.VISIBLE);
        }
        return this;
    }

    public int getmMax() {
        return mMax;
    }

    public SimpleDialog setmMax(int mMax) {
        this.mMax = mMax;
        return this;
    }

    public int getmProgress() {
        return mProgress;

    }

    public SimpleDialog setmProgress(int mProgress) {
        this.mProgress = mProgress;
        if (progressBar!=null){
            progressBar.setProgress(mProgress);
            tvPs.setText((mProgress*100/mMax)+"%");
            tvPsTotal.setText(mProgress+"/"+mMax);
        }
        return this;
    }



    public SimpleDialog setmPbDrawable(int mPbDrawable) {
        this.mPbDrawable = mPbDrawable;
        return this;
    }

    @Override
    public View onCreateView() {
        widthScale(0.6f);
        View inflate = View.inflate(mContext, R.layout.dialog_sample_v1, null);
        initView(inflate);
        return inflate;
    }

    @Override
    public void setUiBeforShow() {
        if (mAlertType == 0) {
            mAlertType = NORMAL_TYPE;
        }
        changeAlertType(mAlertType);
        setThemeColor(mThemeBackgroudColor, mThemeFontColor);
        setNegativeColor(mCancleBackgroudColor, mCancleFontColor);
        setCellName(mCellName);
        setRightImage(mCustomPic);
        setMakeSureMessage(mSureMessage);
        setContent(content);
        setWarnningBottomColor(mBootonBackColor, mBootonFontColor);
        setLoadingPic(mLoadingPic);
        setLoadingText(mLoadingText);
        if (mAlertType == LOADING_TYPE) {
            // 加载动画，动画用户使img图片不停的旋转
            Animation animation = AnimationUtils.loadAnimation(mContext,
                    R.anim.dialog_load_animation);
            // 显示动画
            imgLoading.startAnimation(animation);
        }
        if (mAlertType == SELECT_SINGE_TYPE) {
            if (mDataList==null) {Toast.makeText(mContext,"请设置数据",Toast.LENGTH_SHORT).show();return;}
            singleAdapter = new SimpleAdapter(mContext, mDataList);
            lvSelect.setAdapter(singleAdapter);
            lvSelect.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    mSelectListener.onResultFormDialog(SimpleDialog.this,mDataList.get(i));
                }
            });
        }

        if (mAlertType==SELECT_MORE_TYPE){
            vLine.setVisibility(View.VISIBLE);
            if (mDataList==null) {Toast.makeText(mContext,"请设置数据",Toast.LENGTH_SHORT).show();return;}
            mSelectedList=new ArrayList<>();
            SelectDialogListBean selectAll = new SelectDialogListBean();
            selectAll.setValue("全选");
            selectAll.setKey("-1-100");
            mDataList.add(0,selectAll);
            moreSelectAdapter = new SimpleSelectMoreAdapter(mContext, mDataList);
            lvSelect.setAdapter(moreSelectAdapter);
            lvSelect.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    if (mDataList.get(i).isSelect()){
                        mDataList.get(i).setSelect(false);
                    }else {
                        mDataList.get(i).setSelect(true);
                    }
                    if (i == 0) {
                        for (SelectDialogListBean bean : mDataList) {
                            bean.setSelect(mDataList.get(0).isSelect());
                        }
                    }else {
                        mDataList.get(0).setSelect(false);
                    }
                    moreSelectAdapter.changeData(mDataList);
                }
            });
        }
        if(mAlertType==PROGRESS_TYPE){
            setCancelable(false);
            progressBar.setProgress(0);
            progressBar.setMax(mMax);
            if (mPbDrawable!=0){
                progressBar.setProgressDrawable(mContext.getResources().getDrawable(mPbDrawable));
            }
        }
    }


    public SimpleDialog setLoadingPic(int loadingPic) {
        this.mLoadingPic = loadingPic;
        if (imgLoading != null) {
            imgLoading.setImageResource(loadingPic);
        }
        return this;
    }

    public SimpleDialog setLoadingText(String loadingText) {
        this.mLoadingText = loadingText;
        if (tvLoading != null) {
            tvLoading.setText(loadingText);
        }
        return this;
    }

    private void changeAlertType(int mAlertType) {
        restore();
        if (mAlertType == NORMAL_TYPE) {
            llNormal.setVisibility(View.VISIBLE);
        } else if (mAlertType == WARNNING_TYPE) {
            tvSure.setVisibility(View.VISIBLE);
        } else if (mAlertType == INPUT_TYPE) {
            llInput.setVisibility(View.VISIBLE);
            llNormal.setVisibility(View.VISIBLE);
            contentTxt.setVisibility(View.GONE);
        } else if (mAlertType == LOADING_TYPE) {
            llAlter.setVisibility(View.GONE);
            llLoadIng.setVisibility(View.VISIBLE);
        } else if (mAlertType == SELECT_SINGE_TYPE) {
            llContent.setVisibility(View.GONE);
            llList.setVisibility(View.VISIBLE);
        } else if (mAlertType == SELECT_MORE_TYPE){
            llContent.setVisibility(View.GONE);
            llList.setVisibility(View.VISIBLE);
            LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) llList.getLayoutParams();
            lp.setMargins(0,0,0,0);
            llList.setLayoutParams(lp);
            llNormal.setVisibility(View.VISIBLE);
        } else if (mAlertType==PROGRESS_TYPE){
            llContent.setVisibility(View.GONE);
            rlPb.setVisibility(View.VISIBLE);
        }
    }

    //恢复
    private void restore() {
        tvSure.setVisibility(View.GONE);
        llNormal.setVisibility(View.GONE);
        llInput.setVisibility(View.GONE);
    }

    private void initView(View view) {
        contentTxt = view.findViewById(R.id.content);
        titleTxt = view.findViewById(R.id.title);
        submitTxt = view.findViewById(R.id.submit);
        submitTxt.setOnClickListener(this);
        cancelTxt = view.findViewById(R.id.cancel);
        cancelTxt.setOnClickListener(this);
        contentTxt.setText(content);
        llNormal = view.findViewById(R.id.ll_cancle_and_comfire);
        llAlter = view.findViewById(R.id.ll_normal);
        tvSure = view.findViewById(R.id.tv_sure);
        tvSure.setOnClickListener(this);
        rlTitle = view.findViewById(R.id.rl_title);
        ivTitle = view.findViewById(R.id.iv_pic);
        etContent = view.findViewById(R.id.et_content);
        tvCell = view.findViewById(R.id.tv_cell);
        llInput = view.findViewById(R.id.ll_input);
        llLoadIng = view.findViewById(R.id.ll_loading);
        imgLoading = view.findViewById(R.id.img_loading);
        tvLoading = view.findViewById(R.id.tv_loading);
        lvSelect = view.findViewById(R.id.lv_select);
        vLine = view.findViewById(R.id.v_line);
        llContent = view.findViewById(R.id.ll_content);
        llList = view.findViewById(R.id.ll_list);
        rlPb = view.findViewById(R.id.rl_pb);
        progressBar = view.findViewById(R.id.pb);
        tvPs = view.findViewById(R.id.tv_ps);
        tvPsTotal = view.findViewById(R.id.tv_ps_total);
        refrash();
    }

    private void refrash(){

        if (!TextUtils.isEmpty(positiveName)&&submitTxt!=null) {
            submitTxt.setText(positiveName);
        }

        if (!TextUtils.isEmpty(negativeName)&&cancelTxt!=null) {
            cancelTxt.setText(negativeName);
        }

        if (!TextUtils.isEmpty(title)&&titleTxt!=null) {
            titleTxt.setText(title);
        }
        if (tvCell!=null){
            if (!TextUtils.isEmpty(mCellName)) {
                tvCell.setVisibility(View.VISIBLE);
            } else {
                tvCell.setVisibility(View.GONE);
            }
        }

    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (mAlertType == INPUT_TYPE) {
            inputMessage = etContent.getText().toString().trim();
            inputMessage = inputMessage.replace(" ", "");
        }
        if (id == R.id.cancel) {
            if (cancleListenner != null) {
                cancleListenner.onClick(this, false, inputMessage);
            }
            if (mAlertType==INPUT_TYPE||mAlertType==SELECT_MORE_TYPE){
                this.dismiss();
            }
        } else if (id == R.id.submit) {
            if (mSelectMoreListener!=null){
                mSelectedList.clear();
                for (int i=0;i<moreSelectAdapter.getListData().size();i++){
                    if (moreSelectAdapter.getListData().get(i).isSelect()&&i!=0){
                        mSelectedList.add(moreSelectAdapter.getListData().get(i));
                    }
                }
                mSelectMoreListener.onResultFormDialog(this,mSelectedList);
            }
            if (comfirelistener != null) {
                comfirelistener.onClick(this, true, inputMessage);
            }
        } else if (id == R.id.tv_sure) {
            this.dismiss();
        }
    }


    public SimpleDialog setAnmiation(DialogAnimType type){
        if (type==DialogAnimType.NONE){
            showAnim(null);
            dismissAnim(null);
            show(-1);

        }else if (type==DialogAnimType.ROTATE_V){
            showAnim(new FlipBottomEnter());
            dismissAnim(new FlipVerticalExit());
        }else if (type==DialogAnimType.ROTATE_H){
            showAnim(new FlipLeftEnter());
            dismissAnim(new FlipHorizontalExit());
        }else if (type==DialogAnimType.TRANSLATE_L_R){
            showAnim(new SlideLeftEnter());
            dismissAnim(new SlideRightExit());
        }if (type==DialogAnimType.TRANSLATE_R_L){
            showAnim(new SlideRightEnter());
            dismissAnim(new SlideLeftExit());
        }if (type==DialogAnimType.TRANSLATE_B_T){
            showAnim(new SlideBottomEnter());
            dismissAnim(new SlideBottomExit());
        }if (type==DialogAnimType.TRANSLATE_T_B){
            showAnim(new SlideTopEnter());
            dismissAnim(new SlideTopExit());
        }if (type==DialogAnimType.SCALE_V){
            showAnim(new ZoomInTopEnter());
            dismissAnim(new ZoomOutBottomExit());
        }else if (type==DialogAnimType.SCALE_H){
            showAnim(new ZoomInLeftEnter());
            dismissAnim(new ZoomOutRightExit());
        }else if (type==DialogAnimType.ALPHA){
            showAnim(new FadeEnter());
            dismissAnim(new FadeExit());
        }

        return this;
    }

    public SimpleDialog setCancelOnOutside(boolean isMiss){
        this.setCanceledOnTouchOutside(isMiss);
        return this;
    }


    public interface OnCloseListener {
        /**
         * description: diolog 取消 或 确定回调 <br>
         */
        void onClick(Dialog dialog, boolean confirm, String returnMessage);
    }

    public interface ListSelectListener {
        /**
         * description: 单选按钮回调方法 <br>
         */
        void onResultFormDialog(Dialog dialog, SelectDialogListBean t);
    }

    public interface ListSelectMoreListener {
        /**
         * description: 多选按钮回调方法 <br>
         */
        void onResultFormDialog(Dialog dialog, List<SelectDialogListBean> t);
    }
}
