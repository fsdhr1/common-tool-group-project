package com.gykj.mvpbasemodule;


import javax.inject.Inject;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;
import androidx.appcompat.widget.Toolbar;
import com.blankj.utilcode.util.KeyboardUtils;
import com.blankj.utilcode.util.NetworkUtils;
import com.blankj.utilcode.util.SizeUtils;
import com.blankj.utilcode.util.ToastUtils;

import com.gykj.mvpbasemodule.component.BaseActivityComponent;
import com.gykj.mvpbasemodule.databinding.BaseLayoutBinding;
import com.gykj.mvpbasemodule.dialog.LoadingDialog;
import com.trello.rxlifecycle2.LifecycleTransformer;
import com.trello.rxlifecycle2.components.support.RxAppCompatActivity;


/**
 * @author zyp
 * @dockit BaseMvpActivity
 * 
 */
public abstract class BaseMvpActivity<T extends BaseContract.BasePresenter, C extends BaseActivityComponent> extends RxAppCompatActivity implements BaseContract.BaseView {

    @Nullable
    public T getPresenter() {
        return mPresenter;
    }

    @Nullable
    @Inject
    protected T mPresenter;
    protected C mActivityComponent;
    @Nullable
    protected Toolbar mToolbar;
    protected BaseLayoutBinding mBaseLayoutBinding;
    @Nullable
    protected View mContentView;
    ImageView mBtBaseBack;
    TextView mTvBaseTitle;
    TextView mTvBaseRightText;
    FrameLayout mFlContent;
    RelativeLayout mRlTitle;
    ImageView iv_right_icon;
    LinearLayout ll_base_back;
    LinearLayout ll_right;
    LinearLayout ll_etParent;
    EditText et_search;
    protected final String TAG = this.getClass().getSimpleName();

    /**
     * @title ??????????????????LayoutId,???????????????????????????????????????ID
     * @return ????????????LayoutId
     */
    protected abstract int getLayoutId();

    /**
     * @title ???????????????????????????????????????Activity?????????
     */
    protected abstract void initInjector();

    protected void initView() {
        ll_base_back.setOnClickListener(this::onBackButtonClick);
        ll_right.setOnClickListener(this::rightLayoutClick);
        et_search.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    onSearchClick(v.getText().toString());
                    KeyboardUtils.hideSoftInput(v);
                    return true;
                }
                return false;
            }
        });
    }

    private void initBaseView() {
        mBtBaseBack = mBaseLayoutBinding.ivBaseBack;
        mTvBaseTitle = mBaseLayoutBinding.tvBaseTitle;
        mTvBaseRightText = mBaseLayoutBinding.tvBaseRightText;
        mFlContent = mBaseLayoutBinding.flContent;
        mRlTitle = mBaseLayoutBinding.rlTitle;
        iv_right_icon = mBaseLayoutBinding.ivRightIcon;
        ll_base_back = mBaseLayoutBinding.llBaseBack;
        ll_right = mBaseLayoutBinding.llRight;
        ll_etParent = mBaseLayoutBinding.llEtParent;
        et_search = mBaseLayoutBinding.etSearch;
    }

    /**
     * ????????????????????????????????????
     */
    protected void initData() {

    }

    /**
     * ???????????????????????????????????????????????????
     * @param string ?????????????????????
     */
    protected void onSearchClick(String string) {

    }

    protected LoadingDialog mProgressDialog;

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        mBaseLayoutBinding  = BaseLayoutBinding.inflate(getLayoutInflater());
        super.onCreate(savedInstanceState);
        if (afterSuperOnCreate()) {
            return;
        }
        if(getCurrentTheme() != 0){
            setTheme(getCurrentTheme());
        }
        setStatusBarColor(getPrimaryColor());
        initActivityComponent();
        int layoutId = getLayoutId();
        setContentView(layoutId);
        initInjector();
        mProgressDialog = new LoadingDialog(this,getPrimaryColor());
        attachView();
        initBaseView();
        initView();
        initData();
    }
    
    public FrameLayout getRootView(){
        return findViewById(R.id.fl_content);
    }
    
    /**
     * ???????????????????????????
     * @return ??????ID
     */
    protected @StyleRes
    int getCurrentTheme() {
        return 0;
    }

    /**
     * ????????????Activity setContentView????????????
     * @return ????????????true????????????????????????????????????
     */
    protected boolean afterSuperOnCreate() {
        return false;
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(mBaseLayoutBinding.getRoot());
        if(layoutResID != 0){
            ViewGroup viewGroup = mBaseLayoutBinding.flContent;
            mContentView = LayoutInflater.from(this).inflate(layoutResID, null);
            viewGroup.addView(mContentView);
        }
    }

    /**
     * ?????????????????????
     * @param color ??????ID
     */
    protected void setStatusBarColor(@ColorInt int color) {
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {
            setTranslucentStatus(true);
            SystemBarTintManager tintManager = new SystemBarTintManager(this);
            tintManager.setStatusBarTintEnabled(true);
            tintManager.setNavigationBarTintColor(color);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(color);
        }
    }
    
    @TargetApi(19)
    private void setTranslucentStatus(boolean on) {
        Window win = getWindow();
        WindowManager.LayoutParams winParams = win.getAttributes();
        final int bits = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
        if (on) {
            winParams.flags |= bits;
        } else {
            winParams.flags &= ~bits;
        }
        win.setAttributes(winParams);
    }

    /**
     * ??????loading???
     */
    @Override
    public void showLoading() {
        showLoading("????????????...");
    }

    /**
     * ????????????????????????Loading???
     * @param msg ?????????????????????
     */
    @Override
    public void showLoading(String msg) {
        mProgressDialog.cancel();
        mProgressDialog.setMsg(msg);
        mProgressDialog.show();
    }

    /**
     * ??????Loading???
     */
    @Override
    public void hideLoading() {
        if (mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        attachView();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        hideLoading();
        detachView();
        onChildViewDestory();
    }

    public void onChildViewDestory() {
    }

    public void setLoadingMsg(String msg) {
        if (mProgressDialog != null &&
                mProgressDialog.isShowing() && !TextUtils.isEmpty(msg)) {
            mProgressDialog.setLoadingMsg(msg);
        }
    }


    /**
     * ??????view
     */
    private void detachView() {
        if (mPresenter != null) {
            mPresenter.detachView(this);
        }
    }

    private void attachView() {
        if (mPresenter != null) {
            mPresenter.attachView(this);
        }
    }

    @Override
    public void onRetry() {

    }

    /**
     * ??????????????????
     * @return 
     */
    @Override
    public <T> LifecycleTransformer<T> bindToLife() {
        return this.bindToLifecycle();
    }

    @Override
    public void showSuccess(String successMsg) {
        ToastUtils.showShort(successMsg);
    }

    @Override
    public void showFaild(String errorMsg) {
        ToastUtils.showShort(errorMsg);
    }

    @Override
    public void showNoNet() {
        ToastUtils.showShort(R.string.no_network_connection);
    }

    

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    finishAfterTransition();
                } else {
                    finish();
                }
                break;
        }
        return true;
    }

    protected abstract void initActivityComponent();

    /**
     * ???????????????????????????
     * @param title ????????????????????????
     */
    protected void setTitle(String title) {
        showTilte(title);
    }

    /**
     * ?????????????????????????????????????????????
     * @param rightText ?????????????????????
     */
    protected void setTvBaseRightText(String rightText) {
        mTvBaseRightText.setText(rightText);
        showRight();
        mTvBaseRightText.setVisibility(View.VISIBLE);
    }

    protected void setIv_right_icon(int res) {
        if (res != 0) {
            iv_right_icon.setImageResource(res);
        }
        showRight();
        iv_right_icon.setVisibility(View.VISIBLE);
        ViewGroup.LayoutParams mLayoutParams = iv_right_icon.getLayoutParams();
        
        mLayoutParams.width = SizeUtils.dp2px(23);
        mLayoutParams.height = SizeUtils.dp2px(23);
        iv_right_icon.setLayoutParams(mLayoutParams);
    }

    /**
     * ??????????????????
     */
    protected void hideRight() {
        ll_right.setVisibility(View.GONE);
    }

    /**
     * ??????????????????
     */
    protected void hideBack() {
        ll_base_back.setVisibility(View.GONE);
    }

    /**
     * ??????????????????
     */
    protected void showRight() {
        ll_right.setVisibility(View.VISIBLE);
    }

    /**
     * ????????????
     */
    public void hideTitle() {
        mRlTitle.setVisibility(View.GONE);
    }

    /**
     * ????????????????????????
     * @param title ??????????????????
     */
    protected void showTilte(String title) {
        ll_etParent.setVisibility(View.GONE);
        mTvBaseTitle.setText(title);
        mTvBaseTitle.setVisibility(View.VISIBLE);
        mTvBaseTitle.setEllipsize(TextUtils.TruncateAt.END);
        mTvBaseTitle.setLines(1);
        mTvBaseTitle.setMaxEms(12);
        mRlTitle.setVisibility(View.VISIBLE);
        mRlTitle.setBackgroundColor(getPrimaryColor());
        //
        setStatusBarColor(getPrimaryColor());
    }

    /**
     * ???????????????
     * @param hint ???????????????????????????
     */
    protected void showSearchLayout(@Nullable String hint) {
        mRlTitle.setVisibility(View.VISIBLE);
        mTvBaseTitle.setVisibility(View.GONE);
        ll_etParent.setVisibility(View.VISIBLE);
        if (TextUtils.isEmpty(hint)) return;
        et_search.setHint(hint);
    }

    /**
     * ??????????????????????????????????????????
     * @param view ??????????????????????????????
     */
    protected void onBackButtonClick(View view) {
        finish();
    }
    

    public BaseActivityComponent getBaseActivityComponent() {
        return mActivityComponent;
    }

    /**
     * ?????????????????????????????????????????????
     * @param v ????????????View
     */
    protected void rightLayoutClick(View v) {

    }

    @Override
    public Activity getActivity() {
        return this;
    }
    
    public abstract @ColorInt int getPrimaryColor();

    public void setViewClickListener(View... mView) {
        for (View v : mView) {
            v.setOnClickListener(this::onViewClick);
        }
    }

    public void onViewClick(View view) {

    }
}
