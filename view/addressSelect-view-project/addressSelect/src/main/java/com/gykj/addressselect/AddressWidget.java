package com.gykj.addressselect;

import android.text.TextUtils;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.blankj.utilcode.util.CollectionUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.gykj.addressselect.bean.AddreBean;
import com.gykj.addressselect.interfaces.OnAddressSelectedListener;
import com.gykj.addressselect.interfaces.onAddressConfirmClick;

import java.lang.ref.SoftReference;
import java.util.List;

/**
 * Created by ZhaiJiaChang.
 * <p>
 * Date: 2021/10/22
 */
public class AddressWidget {

    private BottomAddressDialog mAddressDialog;
    private FragmentActivity mFragmentActivity;
    private AddressViewsController mViewsController;


    private SoftReference<BottomAddressDialog> mSoftReference;

    public static final int TYPE_NORMAL = 1; // 以前的样式
    public static final int TYPE_CUSTOM_HEADER = 2;// 自定义头部、添加输入区划名称区划代码到列表第一项、并可以随时选中
    public static final int TYPE_ADD_INPUT_HEAD = 3;// 添加输入区划名称区划代码到列表第一项


    /**
     * 静态内部类+单例保证对象的唯一性，并设置上下文信息
     */
    public static AddressWidget getInstance() {
        return InnerClass.mWidget;
    }

    public static AddressWidget getNewInstance() {
        return new AddressWidget();
    }


    private static class InnerClass {
        private static AddressWidget mWidget = new AddressWidget();
    }


    /**
     * 创建一个Diolog,此方法只在当前包内可见
     */
    protected AddressWidget getWidgetCached(FragmentActivity mActivity) {
        if (mSoftReference == null || mSoftReference.get() == null) {
            mFragmentActivity = mActivity;
            mViewsController = new AddressViewsController(mActivity);
            mViewsController.retrieveProvinces();
            mAddressDialog = new BottomAddressDialog(mActivity);
            mAddressDialog.setViewsController(mViewsController);
            mSoftReference = new SoftReference<BottomAddressDialog>(mAddressDialog);
        }
        return this;
    }

    /**
     *
     */
    protected AddressWidget getWidgetCached(Fragment mFragment) {
        if (mSoftReference == null || mSoftReference.get() == null) {
            mFragmentActivity = mFragment.getActivity();
            mViewsController = new AddressViewsController(mFragment.getContext());
            mViewsController.retrieveProvinces();
            mAddressDialog = new BottomAddressDialog(mFragment);
            mAddressDialog.setViewsController(mViewsController);
            mSoftReference = new SoftReference<BottomAddressDialog>(mAddressDialog);
        }
        return this;
    }

    /**
     * 创建一个新的widget，此方法只在当前包内可见
     */
    protected AddressWidget createNewWidget(FragmentActivity mActivity) {
        mFragmentActivity = mActivity;
        mViewsController = new AddressViewsController(mActivity);
        mAddressDialog = new BottomAddressDialog(mActivity);
        mAddressDialog.setViewsController(mViewsController);
        mSoftReference = new SoftReference<BottomAddressDialog>(mAddressDialog);
        return this;
    }

    protected AddressWidget createNewWidget(Fragment mFragment) {
        mFragmentActivity = mFragment.getActivity();
        mViewsController = new AddressViewsController(mFragment.getContext());
        mAddressDialog = new BottomAddressDialog(mFragment);
        mAddressDialog.setViewsController(mViewsController);
        mSoftReference = new SoftReference<BottomAddressDialog>(mAddressDialog);
        return this;
    }


    /***
     * 获取控制层
     */
    public AddressViewsController getController() {
        return mViewsController;
    }

    /**
     * 显示Dialog
     * 显示普通的地址选择器
     */
    public void showWidget() {
        if (mFragmentActivity != null) {
            if (!mFragmentActivity.isFinishing()) {
                mAddressDialog.showDialog();
                mViewsController.retrieveProvinces();
            }
        }
    }

    /**
     * 根据qhdm显示下级区划
     * 显示普通的地址选择器
     */
    public void showWidget(String qhdm) {
        if (TextUtils.isEmpty(qhdm)) return;
        if (mFragmentActivity != null) {
            if (!mFragmentActivity.isFinishing()) {
                if (!TextUtils.isEmpty(qhdm)) {
                    // 默认显示普通的样式
                    mViewsController.setAddressStyle(AddressWidget.TYPE_NORMAL, null);
                    //
                    AddreBean mAddreBean = new AddreBean();
                    mAddreBean.setQhdm(qhdm);
                    if (qhdm.length() == 2) {
                        mViewsController.resetUnderProvince();
                        mViewsController.retrieveCitiesWith(mAddreBean);
                        mAddressDialog.showDialog();
                    } else if (qhdm.length() == 4) {
                        mViewsController.resetUnderCity();
                        mViewsController.retrieveCountiesWith(mAddreBean);
                        mAddressDialog.showDialog();
                    } else if (qhdm.length() == 6) {
                        mViewsController.resetUnderCountry();
                        mViewsController.retrieveTownWith(mAddreBean);
                        mAddressDialog.showDialog();
                    } else if (qhdm.length() == 9) {
                        mViewsController.resetTown();
                        mViewsController.retrieveVillage(mAddreBean);
                        mAddressDialog.showDialog();
                    } else {
                        ToastUtils.showShort("请输入正确区划代码");
                    }
                } else {
                    mViewsController.retrieveProvinces();
                }

            }
        }
    }

    /**
     * 根据qhdm显示下级区划，可选择到任意区划点击确定即可选择
     * 显示自定义的样式
     */
    public void showWidget(String qhdm, String qhmc, onAddressConfirmClick mConfirmClick) {
        if (TextUtils.isEmpty(qhdm)) return;
        if (mFragmentActivity != null) {
            if (!mFragmentActivity.isFinishing()) {
                if (!TextUtils.isEmpty(qhdm)) {
                    // 自定义地址选择器
                    if (mViewsController != null) {
                        mViewsController.setAddressStyle(AddressWidget.TYPE_CUSTOM_HEADER, mConfirmClick);
                    }
                    //
                    AddreBean mAddreBean = new AddreBean();
                    mAddreBean.setQhdm(qhdm);
                    mAddreBean.setQhmc(qhmc);
                    if (qhdm.length() == 2) {
                        mViewsController.resetUnderProvince();
                        mViewsController.retrieveCitiesWith(mAddreBean);
                        mAddressDialog.showDialog();
                    } else if (qhdm.length() == 4) {
                        mViewsController.resetUnderCity();
                        mViewsController.retrieveCountiesWith(mAddreBean);
                        mAddressDialog.showDialog();
                    } else if (qhdm.length() == 6) {
                        mViewsController.resetUnderCountry();
                        mViewsController.retrieveTownWith(mAddreBean);
                        mAddressDialog.showDialog();
                    } else if (qhdm.length() == 9) {
                        mViewsController.resetTown();
                        mViewsController.retrieveVillage(mAddreBean);
                        mAddressDialog.showDialog();
                    } else {
                        ToastUtils.showShort("请输入正确区划代码");
                    }
                } else {
                    mViewsController.retrieveProvinces();
                }

            }
        }
    }

    /**
     * 根据qhdm显示下级区划，顶部第一项是当前输入的区划
     */
    public void showWidget(String qhdm, String qhmc) {
        if (TextUtils.isEmpty(qhdm)) return;
        if (mFragmentActivity != null) {
            if (!mFragmentActivity.isFinishing()) {
                if (!TextUtils.isEmpty(qhdm)) {
                    //
                    mViewsController.setAddressStyle(AddressWidget.TYPE_ADD_INPUT_HEAD);
                    //
                    AddreBean mAddreBean = new AddreBean();
                    mAddreBean.setQhdm(qhdm);
                    mAddreBean.setQhmc(qhmc);
                    if (qhdm.length() == 2) {
                        mViewsController.resetUnderProvince();
                        mViewsController.retrieveCitiesWith(mAddreBean);
                        mAddressDialog.showDialog();
                    } else if (qhdm.length() == 4) {
                        mViewsController.resetUnderCity();
                        mViewsController.retrieveCountiesWith(mAddreBean);
                        mAddressDialog.showDialog();
                    } else if (qhdm.length() == 6) {
                        mViewsController.resetUnderCountry();
                        mViewsController.retrieveTownWith(mAddreBean);
                        mAddressDialog.showDialog();
                    } else if (qhdm.length() == 9) {
                        mViewsController.resetTown();
                        mViewsController.retrieveVillage(mAddreBean);
                        mAddressDialog.showDialog();
                    } else {
                        ToastUtils.showShort("请输入正确区划代码");
                    }
                } else {
                    mViewsController.retrieveProvinces();
                }

            }
        }
    }

    /**
     * 显示同一级别区划列表、点击某一条显示子集
     */
    public void showWidgetList(List<AddreBean> mList) {
        if (CollectionUtils.isEmpty(mList)) return;
        if (mFragmentActivity != null) {
            if (!mFragmentActivity.isFinishing()) {
                // 自定义地址选择器
//                if (mViewsController != null) {
//                    mViewsController.setAddressStyle(AddressWidget.TYPE_CUSTOM_HEADER, mConfirmClick);
//                }
                // 保证区划代码长度一致
                int length = 0;
                for (AddreBean mBean : mList) {
                    String mQhdm = mBean.getQhdm();
                    if (length == 0) {
                        length = mQhdm.length();
                    } else {
                        int mLength = mQhdm.length();
                        if (mLength != length) {
                            Toast.makeText(mFragmentActivity, "请保证地址列表区划代码长度一致", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                }
                //
                if (length == 2) {
                    mViewsController.resetUnderProvince();
                    mViewsController.addProvinces(mList);
                    mAddressDialog.showDialog();
                } else if (length == 4) {
                    mViewsController.resetUnderCity();
                    mViewsController.addCitiesWith(mList);
                    mAddressDialog.showDialog();
                } else if (length == 6) {
                    mViewsController.resetUnderCountry();
                    mViewsController.addCountiesWith(mList);
                    mAddressDialog.showDialog();
                } else if (length == 9) {
                    mViewsController.resetTown();
                    mViewsController.addTownWith(mList);
                    mAddressDialog.showDialog();
                } else {
                    ToastUtils.showShort("请输入正确区划代码");
                }
            }
        }
    }

    /**
     * 显示同一级别区划列表、点击某一条显示子集
     * 可以选择到任意位置地址
     */
    public void showWidgetList(List<AddreBean> mList, onAddressConfirmClick mConfirmClick) {
        if (CollectionUtils.isEmpty(mList)) return;
        if (mFragmentActivity != null) {
            if (!mFragmentActivity.isFinishing()) {
                // 自定义地址选择器
                if (mViewsController != null) {
                    mViewsController.setAddressStyle(AddressWidget.TYPE_CUSTOM_HEADER, mConfirmClick);
                }
                // 保证区划代码长度一致
                int length = 0;
                for (AddreBean mBean : mList) {
                    String mQhdm = mBean.getQhdm();
                    if (length == 0) {
                        length = mQhdm.length();
                    } else {
                        int mLength = mQhdm.length();
                        if (mLength != length) {
                            Toast.makeText(mFragmentActivity, "请保证地址列表区划代码长度一致", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                }
                //
                if (length == 2) {
                    mViewsController.resetUnderProvince();
                    mViewsController.addProvinces(mList);
                    mAddressDialog.showDialog();
                } else if (length == 4) {
                    mViewsController.resetUnderCity();
                    mViewsController.addCitiesWith(mList);
                    mAddressDialog.showDialog();
                } else if (length == 6) {
                    mViewsController.resetUnderCountry();
                    mViewsController.addCountiesWith(mList);
                    mAddressDialog.showDialog();
                } else if (length == 9) {
                    mViewsController.resetTown();
                    mViewsController.addTownWith(mList);
                    mAddressDialog.showDialog();
                } else {
                    ToastUtils.showShort("请输入正确区划代码");
                }
            }
        }
    }

    /**
     * 关闭地址选择器
     */
    public void onDissWidget() {
        if (mFragmentActivity != null && mAddressDialog != null) {
            mAddressDialog.dissmissDialog();
        }
    }

    /**
     * 关闭引用
     */
    public void onWidgetDestory() {
        if (mSoftReference != null) {
            mSoftReference.clear();
            mSoftReference = null;
        }
        if (mAddressDialog != null) {
            mAddressDialog.dissmissDialog();
            mAddressDialog = null;
        }
    }

    /**
     * 设置选中监听
     */
    public AddressWidget setOnAddressSelectedListener(OnAddressSelectedListener mSelectedListener) {
        mViewsController.setOnAddressSelectedListener(mSelectedListener);
        return this;
    }

//    /**
//     * 关闭按钮
//     */
//    public AddressWidget setOnDialogCloseListener(AddressViewsController.OnDialogCloseListener mListener) {
//        mViewsController.setOnDialogCloseListener(mListener);
//        return this;
//    }


}
