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
     * 重写此方法进行初始化数据
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
     * Fragment销毁时调用
     */
    public void onChildViewDestory() {

    }
    /**
     * 显示loading框
     */
    @Override
    public void showLoading() {
        if (mActivity != null)
            mActivity.showLoading();
    }
    
    /**
     * 显示带文字说明的Loading框
     * @param msg 显示的文字说明
     */
    @Override
    public void showLoading(String msg) {
        if (mActivity != null)
            mActivity.showLoading(msg);
    }
    /**
     * 隐藏Loading框
     */
    @Override
    public void hideLoading() {
        if (mActivity != null)
            mActivity.hideLoading();
    }

    /**
     * 显示成功提示
     * @param successMsg 成功提示信息
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
     * 初始化FragmentComponent
     */
    protected abstract void initFragmentComponent();

    /**
     * 贴上view
     */
    private void attachView() {
        if (mPresenter != null) {
            mPresenter.attachView(this);
        }
    }

    /**
     * 分离view
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
     * 设置View
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
