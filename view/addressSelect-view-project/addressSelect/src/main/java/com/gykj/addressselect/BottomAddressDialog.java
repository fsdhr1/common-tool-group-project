package com.gykj.addressselect;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import androidx.fragment.app.Fragment;


public class BottomAddressDialog extends Dialog {
    //    private AddressSelector mAddressSelector;
    private Activity mActivity;
    private AddressViewsController mViewsController;

    public BottomAddressDialog(Activity context) {
        super(context, R.style.BottomDialogStyle);
        mActivity = context;
    }

    public BottomAddressDialog(Fragment mFragment) {
        super(mFragment.getContext(), R.style.BottomDialogStyle);
        mActivity = mFragment.getActivity();
    }

    public BottomAddressDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }


    /**
     * 初始化
     */
    private void initViews(AddressViewsController mController) {
        //
        View mView = mController.getView();
        setContentView(mView);
        ImageView iv_colse = (ImageView) mView.findViewById(R.id.iv_colse);
        iv_colse.setOnClickListener(v -> {
            dissmissDialog();
        });
        ImageView iv_colse2 = (ImageView) mView.findViewById(R.id.iv_colse2);
        iv_colse2.setOnClickListener(v -> {
            dissmissDialog();
        });
        //
        Window window = getWindow();
        WindowManager.LayoutParams params = window.getAttributes();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = dp2px(mActivity, 300);
        window.setAttributes(params);
        window.setGravity(Gravity.BOTTOM);
        // 配置
        AddressConfigBuilder mBuilder = mController.getBuilder();
        if (mBuilder != null) {
            setCanceledOnTouchOutside(mBuilder.getCanceledOnTouchOutside());
        }

    }

    public static int dp2px(Context context, float dp) {
        return (int) Math.ceil((double) (context.getResources().getDisplayMetrics().density * dp));
    }

//    public BottomAddressDialog setOnAddressSelectedListener(OnAddressSelectedListener listener) {
//        this.mAddressSelector.setOnAddressSelectedListener(listener);
//        return this;
//    }

//    public void showDialog() {
//        if (mActivity != null) {
////            Log.e("showDialog: ", " --- "+mActivity.hashCode()+" ---");
//            if (!mActivity.isFinishing()) {
//                if (!isShowing()) {
//                    if (mAddressSelector == null) {
//                        init(getContext());
//                    }
//                    show();
//                    mAddressSelector.retrieveProvinces();
//                }
//            }
//        }
//    }

//    public void showDialog(String qhdm) {
//        if (mActivity != null) {
////            Log.e("showDialog: ", " --- "+mActivity.hashCode()+" ---");
//            if (!mActivity.isFinishing()) {
//                if (!isShowing()) {
//                    show();
//                    if (!TextUtils.isEmpty(qhdm)) {
//                        if (qhdm.length() == 2) {
//                            mViewsController.resetUnderProvince();
//                            mViewsController.retrieveCitiesWith(qhdm);
//                        } else if (qhdm.length() == 4) {
//                            mViewsController.resetUnderCity();
//                            mViewsController.retrieveCountiesWith(qhdm);
//                        } else if (qhdm.length() == 6) {
//                            mViewsController.resetUnderCountry();
//                            mViewsController.retrieveTownWith(qhdm);
//                        } else if (qhdm.length() == 9) {
//                            mViewsController.resetVillage();
//                            mViewsController.retrieveVillage(qhdm);
//                        } else {
//                            ToastUtils.showShort("请输入正确区划代码");
//                        }
//                    } else {
//                        mViewsController.retrieveProvinces();
//                    }
//                }
//            }
//        }
//    }

    public void dissmissDialog() {
        if (isShowing()) {
            dismiss();
        }
    }

//    public BottomAddressDialog setDialogDismisListener(AddressSelector.OnDialogCloseListener listener) {
//        this.mAddressSelector.setOnDialogCloseListener(listener);
//        return this;
//    }

//    /**
//     * 设置选中位置的监听
//     *
//     * @param listener
//     */
//    public BottomAddressDialog setSelectorAreaPositionListener(AddressSelector.onSelectorAreaPositionListener listener) {
//        this.mAddressSelector.setOnSelectorAreaPositionListener(listener);
//        return this;
//    }
//
//    /**
//     * 设置字体选中的颜色
//     */
//    public BottomAddressDialog setTextSelectedColor(int selectedColor) {
//        this.mAddressSelector.setTextSelectedColor(selectedColor);
//        return this;
//    }
//
//    /**
//     * 设置字体没有选中的颜色
//     */
//    public BottomAddressDialog setTextUnSelectedColor(int unSelectedColor) {
//        this.mAddressSelector.setTextUnSelectedColor(unSelectedColor);
//        return this;
//    }
//
//    /**
//     * 设置字体的大小
//     */
//    public BottomAddressDialog setTextSize(float dp) {
//        this.mAddressSelector.setTextSize(dp);
//        return this;
//    }
//
//    /**
//     * 设置字体的背景
//     */
//    public BottomAddressDialog setBackgroundColor(int colorId) {
//        this.mAddressSelector.setBackgroundColor(colorId);
//        return this;
//    }
//
//    /**
//     * 设置指示器的背景
//     */
//    public BottomAddressDialog setIndicatorBackgroundColor(int colorId) {
//        this.mAddressSelector.setIndicatorBackgroundColor(colorId);
//        return this;
//    }
//
//    /**
//     * 设置指示器的背景
//     */
//    public BottomAddressDialog setIndicatorBackgroundColor(String color) {
//        this.mAddressSelector.setIndicatorBackgroundColor(color);
//        return this;
//    }

//    /**
//     * 获取选择器
//     */
//    public AddressSelector getSelector() {
//        return mAddressSelector;
//    }

    /**
     * 返回DataManager实例，请求接口
     */
//    public AddressDataManager getDataManager() {
//        return mAddressSelector.getAddressDataManager();
//    }
    public void setViewsController(AddressViewsController mSelector) {
        this.mViewsController = mSelector;
    }

    public void showDialog() {
        initViews(mViewsController);
        show();
    }

    /**
     * 设置已选中的地区
     *
     * @param provinceCode   省份code
     * @param provinPosition 省份所在的位置
     * @param cityCode       城市code
     * @param cityPosition   城市所在的位置
     * @param countyCode     乡镇code
     * @param countyPosition 乡镇所在的位置
     * @param streetCode     街道code
     * @param streetPosition 街道所在位置
     */
//    public void setDisplaySelectorArea(String provinceCode, int provinPosition, String cityCode, int cityPosition, String countyCode, int countyPosition, String streetCode, int streetPosition) {
//        this.selector.getSelectedArea(provinceCode, provinPosition, cityCode, cityPosition, countyCode, countyPosition, streetCode, streetPosition);
//    }

}
