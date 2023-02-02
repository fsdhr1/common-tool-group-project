package com.gykj.mvpbasemodule;

/**
 * @author zyp
 * 2019-05-09
 */
public class BasePresenter<T extends BaseContract.BaseView> implements BaseContract.BasePresenter<T> {
    protected T mView;

    @Override
    public void attachView(T view) {
        if (mView == null || (view != null && mView.getClass() == view.getClass())) {
            this.mView = view;
        }

    }

    @Override
    public void detachView(BaseMvpActivity baseActivity) {
        if (mView == baseActivity || baseActivity == null) {
            mView = null;
        }
    }

    public T getView() {
        return mView;
    }
}
