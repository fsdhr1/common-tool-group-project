package com.gykj.mvpbasemodule;

import javax.inject.Inject;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;

import com.blankj.utilcode.util.NetworkUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.gykj.mvpbasemodule.component.BaseFragmentComponent;
import com.trello.rxlifecycle2.LifecycleTransformer;
import com.trello.rxlifecycle2.components.support.RxFragment;


/**
 * @author zyp
 * 2019-05-09
 */
public abstract class BaseMvpFragment<T extends BaseContract.BasePresenter, A extends BaseMvpActivity, F extends BaseFragmentComponent> extends RxFragment implements BaseContract.BaseView {
    private static final String STATE_SAVE_IS_HIDDEN = "STATE_SAVE_IS_HIDDEN";
    @Nullable
    @Inject
    protected T mPresenter;
    protected F mFragmentComponent;
    private View mRootView, mErrorView, mEmptyView;
    protected A mActivity;

    protected abstract int getLayoutId();

    protected void initInjector() {

    }

    protected void initView(View view) {

    }
    

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initFragmentComponent();
        //ARouter.getInstance().inject(this);
        mActivity = (A) getActivity();
        initInjector();
        attachView();
        //if (!NetworkUtils.isConnected()) showNoNet();
        if (savedInstanceState != null) {
            boolean isSupportHidden = savedInstanceState.getBoolean(STATE_SAVE_IS_HIDDEN);
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            if (isSupportHidden) {
                ft.hide(this);
            } else {
                ft.show(this);
            }
            ft.commit();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(STATE_SAVE_IS_HIDDEN, isHidden());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        inflaterView(inflater, container);
        initView(mRootView);
        initData();
        return mRootView;
    }

    /**
     * ????????????????????????????????????
     */
    public void initData(){

    };


    @Override
    public void onDestroy() {
        super.onDestroy();
        detachView();
        onChildViewDestory();
    }

    /**
     * Fragment???????????????
     */
    public void onChildViewDestory() {

    }
    /**
     * ??????loading???
     */
    @Override
    public void showLoading() {
        if (mActivity != null)
            mActivity.showLoading();
    }
    
    /**
     * ????????????????????????Loading???
     * @param msg ?????????????????????
     */
    @Override
    public void showLoading(String msg) {
        if (mActivity != null)
            mActivity.showLoading(msg);
    }
    /**
     * ??????Loading???
     */
    @Override
    public void hideLoading() {
        if (mActivity != null)
            mActivity.hideLoading();
    }

    /**
     * ??????????????????
     * @param successMsg ??????????????????
     */
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
    public void onRetry() {
        ToastUtils.showShort("onRetry");
    }

    @Override
    public <T> LifecycleTransformer<T> bindToLife() {
        return this.bindToLifecycle();
    }


    /**
     * ?????????FragmentComponent
     */
    protected abstract void initFragmentComponent();

    /**
     * ??????view
     */
    private void attachView() {
        if (mPresenter != null) {
            mPresenter.attachView(this);
        }
    }

    /**
     * ??????view
     */
    private void detachView() {
        if (mPresenter != null) {
            FragmentActivity activity = getActivity();
            if (activity instanceof BaseMvpActivity) {
                mPresenter.detachView((BaseMvpActivity) activity);
            } else {
                mPresenter.detachView(null);
            }

        }
    }

    /**
     * ??????View
     *
     * @param inflater
     * @param container
     */
    private void inflaterView(LayoutInflater inflater, @Nullable ViewGroup container) {
        if (mRootView == null) {
            mRootView = inflater.inflate(getLayoutId(), container, false);
        }
    }
    

}
