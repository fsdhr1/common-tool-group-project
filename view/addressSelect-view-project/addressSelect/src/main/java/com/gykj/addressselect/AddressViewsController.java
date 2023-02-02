package com.gykj.addressselect;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.interpolator.view.animation.FastOutSlowInInterpolator;


import com.blankj.utilcode.util.CollectionUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.gykj.addressselect.bean.AddreBean;
import com.gykj.addressselect.bean.AddressUtils;
import com.gykj.addressselect.interfaces.OnAddressSelectedListener;
import com.gykj.addressselect.interfaces.onAddressConfirmClick;

import java.util.ArrayList;
import java.util.List;

import static com.gykj.addressselect.AddressWidget.TYPE_NORMAL;


public class AddressViewsController implements AdapterView.OnItemClickListener {
    public static final int INDEX_TAB_PROVINCE = 0;//省份标志
    public static final int INDEX_TAB_CITY = 1;//城市标志
    public static final int INDEX_TAB_COUNTY = 2;//区县标志
    public static final int INDEX_TAB_TOWN = 3;//街道标志
    public static final int INDEX_TAB_VILLAGE = 4;//村标志
    public int tabIndex = INDEX_TAB_PROVINCE; //默认是省份

    private static final int INDEX_INVALID = -1;
    private int provinceIndex = INDEX_INVALID; //省份的下标
    private int cityIndex = INDEX_INVALID;//城市的下标
    private int countyIndex = INDEX_INVALID;//区县的下标
    private int townIndex = INDEX_INVALID;//乡镇的下标
    private int villageIndex = INDEX_INVALID;//村的下标

    private Context context;
    private final LayoutInflater inflater;
    private View view;

    private View indicator;

    private LinearLayout layout_tab;
    private TextView textViewProvince;
    private TextView textViewCity;
    private TextView textViewCounty;
    private TextView textViewTown;
    private TextView textViewVillage;

    private ProgressBar progressBar;

    private ListView listView;
    private AddressAdapter mAddressAdapter;
    private List<AddreBean> mProvinceList = new ArrayList<>();
    private List<AddreBean> mCitieList = new ArrayList<>();
    private List<AddreBean> mCountyList = new ArrayList<>();
    private List<AddreBean> mTownList = new ArrayList<>();
    private List<AddreBean> mVillageList = new ArrayList<>();
    private OnAddressSelectedListener listener;
//    private OnDialogCloseListener dialogCloseListener;
//    private onSelectorAreaPositionListener selectorAreaPositionListener;

    private AddressDataManager mAddressDataManager;
    private ImageView iv_colse;
    private int selectedColor;
    private int unSelectedColor;
    private int provincePostion;
    private int cityPosition;
    private int countyPosition;
    private int townPosition;
    private int villagePosition;

    private int mStyle = TYPE_NORMAL;


    /**
     * Builder
     */
    private AddressConfigBuilder mBuilder;

    public AddressViewsController(Context context) {
        this.context = context;
        inflater = LayoutInflater.from(context);
        mAddressDataManager = new AddressDataManager(context);
        initViews();
        initAdapters();
    }

    public AddressConfigBuilder getBuilder() {
        return mBuilder;
    }

    public void setBuilder(AddressConfigBuilder mBuilder) {
        this.mBuilder = mBuilder;
    }

//    /**
//     * 得到数据库管理者
//     *
//     * @return
//     */
//    public AddressDataManager getAddressDataManager() {
//        return mAddressDataManager;
//    }

    /**
     * 初始化布局
     */
    private void initViews() {
        view = inflater.inflate(R.layout.layout_widget_address_selector, null);
        this.progressBar = (ProgressBar) view.findViewById(R.id.progressBar);//进度条
        this.iv_colse = (ImageView) view.findViewById(R.id.iv_colse);
        this.listView = (ListView) view.findViewById(R.id.listView);//listview
        this.indicator = view.findViewById(R.id.indicator); //指示器
        this.layout_tab = (LinearLayout) view.findViewById(R.id.layout_tab);
        this.textViewProvince = (TextView) view.findViewById(R.id.textViewProvince);//省份
        this.textViewCity = (TextView) view.findViewById(R.id.textViewCity);//城市
        this.textViewCounty = (TextView) view.findViewById(R.id.textViewCounty);//区 县
        this.textViewTown = (TextView) view.findViewById(R.id.textViewTown);//乡
        this.textViewVillage = (TextView) view.findViewById(R.id.textViewVillage);//村

        this.textViewProvince.setOnClickListener(new OnProvinceTabClickListener());
        this.textViewCity.setOnClickListener(new OnCityTabClickListener());
        this.textViewCounty.setOnClickListener(new onCountyTabClickListener());
        this.textViewTown.setOnClickListener(new OnTownTabClickListener());
        this.textViewVillage.setOnClickListener(new OnVillageTabClickListener());

        this.listView.setOnItemClickListener(this);


//        this.iv_colse.setOnClickListener(new onCloseClickListener());
        updateIndicator();
    }

//    /**
//     * 设置字体选中的颜色
//     */
//    public void setTextSelectedColor(int selectedColor) {
//        this.selectedColor = selectedColor;
//    }

//    /**
//     * 设置字体没有选中的颜色
//     */
//    public void setTextUnSelectedColor(int unSelectedColor) {
//        this.unSelectedColor = unSelectedColor;
//    }

//    /**
//     * 设置字体的大小
//     */
//    public void setTextSize(float dp) {
//        textViewProvince.setTextSize(dp);
//        textViewCity.setTextSize(dp);
//        textViewCounty.setTextSize(dp);
//        textViewTown.setTextSize(dp);
//    }

//    /**
//     * 设置字体的背景
//     */
//    public void setBackgroundColor(int colorId) {
//        layout_tab.setBackgroundColor(context.getResources().getColor(colorId));
//    }

//    /**
//     * 设置指示器的背景
//     */
//    public void setIndicatorBackgroundColor(int colorId) {
//        indicator.setBackgroundColor(context.getResources().getColor(colorId));
//    }

//    /**
//     * 设置指示器的背景
//     */
//    public void setIndicatorBackgroundColor(String color) {
//        indicator.setBackgroundColor(Color.parseColor(color));
//    }

    /**
     * 初始化adapter
     */
    private void initAdapters() {
        mAddressAdapter = new AddressAdapter();
        listView.setAdapter(mAddressAdapter);
    }

    /**
     * 不更改样式，只是设置类型
     */
    public void setAddressStyle(int mStyle) {
        this.mStyle = mStyle;
    }

    /**
     * 设置样式
     * 默认样式不用传接口，传接口的都是自定义样式
     */
    public void setAddressStyle(int mStyle, onAddressConfirmClick mConfirmClick) {
        this.mStyle = mStyle;
        if (mStyle == AddressWidget.TYPE_CUSTOM_HEADER) {
            if (view != null) {
                // 交换按钮位置，显示选中按钮
                ImageView iv2 = view.findViewById(R.id.iv_colse2);
                iv2.setVisibility(View.VISIBLE);
                ImageView iv = view.findViewById(R.id.iv_colse);
                iv.setVisibility(View.GONE);
                TextView mBtConfirm = view.findViewById(R.id.bt_confirm);
                mBtConfirm.setVisibility(View.VISIBLE);
                mBtConfirm.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View mView) {
                        if (mConfirmClick != null) {
                            mConfirmClick.onAddressConfirmResp();
                        }
                    }
                });
            }
        }
    }

    /**
     * 更新tab 指示器
     */
    private void updateIndicator() {
        view.post(new Runnable() {
            @Override
            public void run() {
                switch (tabIndex) {
                    case INDEX_TAB_PROVINCE: //省份
                        buildIndicatorAnimatorTowards(textViewProvince).start();
                        break;
                    case INDEX_TAB_CITY: //城市
                        buildIndicatorAnimatorTowards(textViewCity).start();
                        break;
                    case INDEX_TAB_COUNTY: //区县
                        buildIndicatorAnimatorTowards(textViewCounty).start();
                        break;
                    case INDEX_TAB_TOWN: //乡镇
                        buildIndicatorAnimatorTowards(textViewTown).start();
                        break;
                    case INDEX_TAB_VILLAGE: //村
                        buildIndicatorAnimatorTowards(textViewVillage).start();
                        break;
                }
            }
        });
    }

    /**
     * tab 来回切换的动画
     *
     * @param tab
     * @return
     */
    private AnimatorSet buildIndicatorAnimatorTowards(TextView tab) {
        ObjectAnimator xAnimator = ObjectAnimator.ofFloat(indicator, "X", indicator.getX(), tab.getX());

        final ViewGroup.LayoutParams params = indicator.getLayoutParams();
        ValueAnimator widthAnimator = ValueAnimator.ofInt(params.width, tab.getMeasuredWidth());
        widthAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                params.width = (int) animation.getAnimatedValue();
                indicator.setLayoutParams(params);
            }
        });

        AnimatorSet set = new AnimatorSet();
        set.setInterpolator(new FastOutSlowInInterpolator());
        set.playTogether(xAnimator, widthAnimator);

        return set;
    }

    /**
     * 更新tab显示
     */
    private void updateTabsVisibility() {
        textViewProvince.setVisibility(AddressUtils.notEmpty(mProvinceList) ? View.VISIBLE : View.GONE);
        textViewCity.setVisibility(AddressUtils.notEmpty(mCitieList) ? View.VISIBLE : View.GONE);
        textViewCounty.setVisibility(AddressUtils.notEmpty(mCountyList) ? View.VISIBLE : View.GONE);
        textViewTown.setVisibility(AddressUtils.notEmpty(mTownList) ? View.VISIBLE : View.GONE);
        textViewVillage.setVisibility(AddressUtils.notEmpty(mVillageList) ? View.VISIBLE : View.GONE);
        //按钮能不能点击 false 不能点击 true 能点击
        textViewProvince.setEnabled(tabIndex != INDEX_TAB_PROVINCE);
        textViewCity.setEnabled(tabIndex != INDEX_TAB_CITY);
        textViewCounty.setEnabled(tabIndex != INDEX_TAB_COUNTY);
        textViewTown.setEnabled(tabIndex != INDEX_TAB_TOWN);
        textViewVillage.setEnabled(tabIndex != INDEX_TAB_VILLAGE);
        if (selectedColor != 0 && unSelectedColor != 0) {
            updateTabTextColor();
        }
    }

    /**
     * 更新字体的颜色
     */
    private void updateTabTextColor() {
        if (tabIndex != INDEX_TAB_PROVINCE) {
            textViewProvince.setTextColor(context.getResources().getColor(selectedColor));
        } else {
            textViewProvince.setTextColor(context.getResources().getColor(unSelectedColor));
        }
        if (tabIndex != INDEX_TAB_CITY) {
            textViewCity.setTextColor(context.getResources().getColor(selectedColor));
        } else {
            textViewCity.setTextColor(context.getResources().getColor(unSelectedColor));
        }
        if (tabIndex != INDEX_TAB_COUNTY) {
            textViewCounty.setTextColor(context.getResources().getColor(selectedColor));
        } else {
            textViewCounty.setTextColor(context.getResources().getColor(unSelectedColor));
        }
        if (tabIndex != INDEX_TAB_TOWN) {
            textViewTown.setTextColor(context.getResources().getColor(selectedColor));
        } else {
            textViewTown.setTextColor(context.getResources().getColor(unSelectedColor));
        }
        if (tabIndex != INDEX_TAB_VILLAGE) {
            textViewVillage.setTextColor(context.getResources().getColor(selectedColor));
        } else {
            textViewVillage.setTextColor(context.getResources().getColor(unSelectedColor));
        }

    }


    /**
     * 点击省份的监听
     */
    private class OnProvinceTabClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            tabIndex = INDEX_TAB_PROVINCE;
            mAddressAdapter.refreshData(mProvinceList);
            mAddressAdapter.notifyDataSetChanged();
            if (provincePostion != INDEX_INVALID) {
                listView.setSelection(provincePostion);
            }
            updateTabsVisibility();
            updateIndicator();
        }
    }

    /**
     * 点击城市的监听
     */
    private class OnCityTabClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            tabIndex = INDEX_TAB_CITY;
            mAddressAdapter.refreshData(mCitieList);
            mAddressAdapter.notifyDataSetChanged();
            if (cityPosition != INDEX_INVALID) {
                listView.setSelection(cityPosition);
            }
            updateTabsVisibility();
            updateIndicator();
        }
    }

    /**
     * 点击区县的监听
     */
    private class onCountyTabClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            tabIndex = INDEX_TAB_COUNTY;
            mAddressAdapter.refreshData(mCountyList);
            mAddressAdapter.notifyDataSetChanged();
            if (countyPosition != INDEX_INVALID) {
                listView.setSelection(countyPosition);
            }
            updateTabsVisibility();
            updateIndicator();
        }
    }

    /**
     * 点乡镇的监听
     */
    private class OnTownTabClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            tabIndex = INDEX_TAB_TOWN;
            mAddressAdapter.refreshData(mTownList);
            mAddressAdapter.notifyDataSetChanged();
            if (townPosition != INDEX_INVALID) {
                listView.setSelection(townPosition);
            }
            updateTabsVisibility();
            updateIndicator();
        }
    }

    /**
     * 点击街道的监听
     */
    private class OnVillageTabClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            tabIndex = INDEX_TAB_VILLAGE;
            mAddressAdapter.refreshData(mVillageList);
            mAddressAdapter.notifyDataSetChanged();
            if (villagePosition != INDEX_INVALID) {
                listView.setSelection(villagePosition);
            }
            updateTabsVisibility();
            updateIndicator();
        }
    }

    /**
     * 点击右边关闭dialog监听
     */
//    private class onCloseClickListener implements View.OnClickListener {
//
//        @Override
//        public void onClick(View view) {
//            if (dialogCloseListener != null) {
//                dialogCloseListener.dialogclose();
//            }
//        }
//    }
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        String text = "";
        switch (tabIndex) {
            case INDEX_TAB_PROVINCE: //省份
                AddreBean province = mAddressAdapter.getItem(position);
                province.setSelect(true);
                provincePostion = position;
                // 更新当前级别及子级标签文本
                textViewProvince.setText(province.getQhmc());
                textViewCity.setText("请选择");
                textViewCounty.setText("请选择");
                textViewTown.setText("请选择");
                textViewVillage.setText("请选择");
                // 清空子级数据
                mCitieList.clear();
                mCountyList.clear();
                mTownList.clear();
                mVillageList.clear();
                // 更新已选中项
                if (provinceIndex != -1 && provinceIndex != position)
                    mProvinceList.get(provinceIndex).setSelect(false);
                this.provinceIndex = position;
                this.cityIndex = INDEX_INVALID;
                this.countyIndex = INDEX_INVALID;
                this.townIndex = INDEX_INVALID;
                this.villageIndex = INDEX_INVALID;
                // 更新选中效果
                mAddressAdapter.notifyDataSetChanged();
                callbackInternal();
                break;
            case INDEX_TAB_CITY://城市
                AddreBean city = mAddressAdapter.getItem(position);
                city.setSelect(true);
                cityPosition = position;
                textViewCity.setText(city.getQhmc());
                // 清空下级别数据
                textViewCounty.setText("请选择");
                textViewTown.setText("请选择");
                textViewVillage.setText("请选择");
                mCountyList.clear();
                mTownList.clear();
                mVillageList.clear();
                this.countyIndex = INDEX_INVALID;
                this.townIndex = INDEX_INVALID;
                this.villageIndex = INDEX_INVALID;
                // 更新已选中项
                if (cityIndex != -1 && cityIndex != position)
                    mCitieList.get(cityIndex).setSelect(false);
                this.cityIndex = position;
                // 更新选中效果
                mAddressAdapter.notifyDataSetChanged();
                callbackInternal();
                break;
            case INDEX_TAB_COUNTY:
                AddreBean county = mAddressAdapter.getItem(position);
                county.setSelect(true);
                countyPosition = position;
                textViewCounty.setText(county.getQhmc());
                //清空下级别数据
                textViewTown.setText("请选择");
                textViewVillage.setText("请选择");
                mTownList.clear();
                mVillageList.clear();
                this.townIndex = INDEX_INVALID;
                this.villageIndex = INDEX_INVALID;
                //
                if (countyIndex != -1 && countyIndex != position)
                    mCountyList.get(countyIndex).setSelect(false);
                this.countyIndex = position;
                mAddressAdapter.notifyDataSetChanged();
                callbackInternal();
                break;
            case INDEX_TAB_TOWN:
                AddreBean town = mAddressAdapter.getItem(position);
                town.setSelect(true);
                if (townIndex != -1 && townIndex != position)
                    mTownList.get(townIndex).setSelect(false);
                townPosition = position;
                textViewTown.setText(town.getQhmc());
                this.townIndex = position;
                // 清空下级别数据
                textViewVillage.setText("请选择");
                villageIndex = INDEX_INVALID;
                mVillageList.clear();
                //
                mAddressAdapter.notifyDataSetChanged();
                callbackInternal();
                break;
            case INDEX_TAB_VILLAGE:
                AddreBean village = mAddressAdapter.getItem(position);
                village.setSelect(true);
                textViewVillage.setText(village.getQhmc());
                if (villageIndex != -1 && villageIndex != position)
                    mVillageList.get(villageIndex).setSelect(false);
                villagePosition = position;
                villageIndex = position;
                mAddressAdapter.notifyDataSetChanged();
                callbackInternal();
                break;
        }
    }

    // 只显示省份下的信息
    protected void resetUnderProvince() {
        mProvinceList.clear();
        tabIndex = INDEX_TAB_PROVINCE;
        setViewsGone(textViewProvince);
        setViewsVisiable(textViewCity, textViewCounty, textViewTown, textViewVillage);
        textViewCity.setText("请选择");
        textViewCounty.setText("请选择");
        textViewTown.setText("请选择");
        textViewVillage.setText("请选择");
        // 清空子级数据
        mCitieList.clear();
        mCountyList.clear();
        mTownList.clear();
        mVillageList.clear();
        //下标
        provinceIndex = INDEX_INVALID;
        cityIndex = INDEX_INVALID;
        countyIndex = INDEX_INVALID;
        townIndex = INDEX_INVALID;
        villageIndex = INDEX_INVALID;
        //
        provincePostion = 0;
        cityPosition = 0;
        countyPosition = 0;
        townPosition = 0;
        villagePosition = 0;
    }

    // 只显示城市下的信息
    protected void resetUnderCity() {
        tabIndex = INDEX_TAB_CITY;
        setViewsGone(textViewProvince, textViewCity);
        setViewsVisiable(textViewCounty, textViewTown, textViewVillage);
        //
        textViewCounty.setText("请选择");
        textViewTown.setText("请选择");
        textViewVillage.setText("请选择");
        //
        mProvinceList.clear();
        mCitieList.clear();
        mCountyList.clear();
        mTownList.clear();
        mVillageList.clear();
        //下标
        provinceIndex = INDEX_INVALID;
        cityIndex = INDEX_INVALID;
        countyIndex = INDEX_INVALID;
        townIndex = INDEX_INVALID;
        villageIndex = INDEX_INVALID;

        provincePostion = 0;
        cityPosition = 0;
        countyPosition = 0;
        townPosition = 0;
        villagePosition = 0;
    }

    private void setViewsVisiable(View... mViews) {
        for (View mView : mViews) {
            mView.setVisibility(View.VISIBLE);
        }
    }

    // 只显示乡镇下的信息
    protected void resetUnderCountry() {
        tabIndex = INDEX_TAB_COUNTY;
        setViewsGone(textViewProvince, textViewCity, textViewCounty);
        setViewsVisiable(textViewTown, textViewVillage);
        //
        textViewTown.setText("请选择");
        textViewVillage.setText("请选择");
        //
        mProvinceList.clear();
        mCitieList.clear();
        mCountyList.clear();
        mTownList.clear();
        mVillageList.clear();
        //下标
        provinceIndex = INDEX_INVALID;
        cityIndex = INDEX_INVALID;
        countyIndex = INDEX_INVALID;
        townIndex = INDEX_INVALID;
        villageIndex = INDEX_INVALID;
        //
        provincePostion = 0;
        cityPosition = 0;
        countyPosition = 0;
        townPosition = 0;
        villagePosition = 0;
    }

    // 只显示村级信息
    protected void resetTown() {
        tabIndex = INDEX_TAB_TOWN;
        setViewsGone(textViewProvince, textViewCity, textViewCounty, textViewTown, textViewVillage);
        //
        textViewVillage.setText("请选择");
        //
        mProvinceList.clear();
        mCitieList.clear();
        mCountyList.clear();
        mTownList.clear();
        mVillageList.clear();
        //下标
        provinceIndex = INDEX_INVALID;
        cityIndex = INDEX_INVALID;
        countyIndex = INDEX_INVALID;
        townIndex = INDEX_INVALID;
        villageIndex = INDEX_INVALID;
        //
        provincePostion = 0;
        cityPosition = 0;
        countyPosition = 0;
        townPosition = 0;
        villagePosition = 0;
    }

    private void setViewsGone(View... view) {
        for (View mView : view) {
            mView.setVisibility(View.GONE);
        }
    }

    private List<AddreBean> addHeadBean(List<AddreBean> mList, AddreBean mAddreBean) {
        if (mStyle == AddressWidget.TYPE_CUSTOM_HEADER || mStyle == AddressWidget.TYPE_ADD_INPUT_HEAD) {
            AddreBean mBean = new AddreBean();
            mBean.setQhdm(mAddreBean.getQhdm());
            mBean.setQhmc(mAddreBean.getQhmc());
            mBean.setCustom(true);
            mList.add(0, mBean);
        }
        return mList;
    }

    /**
     * 查询省份列表
     */
    protected void retrieveProvinces() {
        if (AddressUtils.notEmpty(mProvinceList) || AddressUtils.notEmpty(mCitieList) || AddressUtils.notEmpty(mCountyList) || AddressUtils.notEmpty(mTownList) || AddressUtils.notEmpty(mVillageList)) {
            return;
        }
        progressBar.setVisibility(View.VISIBLE);
        mAddressDataManager.getAddressDataList("1", new AddressDataManager.AddressCallBack<AddreBean>() {
            @Override
            public void onAddressCallBack(List<AddreBean> mList) {
                if (mList == null) {
                    showNoDataToast();
                    return;
                }
                if (mList.size() == 0) {
                    showNoDataToast();
                }
                mProvinceList = mList;
                mAddressAdapter.refreshData(mList);
                //
                updateTabsVisibility();
                updateProgressVisibility();
                updateIndicator();
            }
        });
    }

    /**
     * 添加本地数据
     * 升级
     */
    protected void addProvinces(List<AddreBean> mList) {
        if (AddressUtils.notEmpty(mProvinceList) || AddressUtils.notEmpty(mCitieList) || AddressUtils.notEmpty(mCountyList) || AddressUtils.notEmpty(mTownList) || AddressUtils.notEmpty(mVillageList)) {
            return;
        }
        progressBar.setVisibility(View.VISIBLE);
        mProvinceList = mList;
        mAddressAdapter.refreshData(mList);
        //
        updateTabsVisibility();
        updateProgressVisibility();
        updateIndicator();
    }

    /**
     * 根据省份id查询城市列表
     *
     * @param cityValue 城市数据
     */
    public void retrieveCitiesWith(AddreBean cityValue) {
        if (!TextUtils.isEmpty(cityValue.getQhdm())) {
            progressBar.setVisibility(View.VISIBLE);
            mAddressDataManager.getAddressDataList(cityValue.getQhdm(), new AddressDataManager.AddressCallBack<AddreBean>() {
                @Override
                public void onAddressCallBack(List<AddreBean> mList) {
                    if (mList == null) {
                        showNoDataToast();
                        return;
                    }
                    if (mList.size() == 0) {
                        showNoDataToast();
                    }
                    addHeadBean(mList, cityValue);
                    mCitieList = mList;
                    // 以次级内容更新列表
                    mAddressAdapter.refreshData(mList);
                    // 更新索引为次级
                    tabIndex = INDEX_TAB_CITY;
                    //
                    updateTabsVisibility();
                    updateProgressVisibility();
                    updateIndicator();
                }
            });
        } else {
            ToastUtils.showShort("城市列表为空");
        }
    }

    /**
     * 添加本地数据
     * 城市
     */
    public void addCitiesWith(List<AddreBean> mList) {
        if (!CollectionUtils.isEmpty(mList)) {
            progressBar.setVisibility(View.VISIBLE);
            mCitieList = mList;
            // 以次级内容更新列表
            mAddressAdapter.refreshData(mList);
            // 更新索引为次级
            tabIndex = INDEX_TAB_CITY;
            //
            updateTabsVisibility();
            updateProgressVisibility();
            updateIndicator();
        } else {
            ToastUtils.showShort("城市列表为空");
        }
    }

    /**
     * 根据城市id查询乡镇列表
     *
     * @param conuntryValue 城市
     */
    public void retrieveCountiesWith(AddreBean conuntryValue) {
        if (!TextUtils.isEmpty(conuntryValue.getQhdm())) {
            progressBar.setVisibility(View.VISIBLE);
            mAddressDataManager.getAddressDataList(conuntryValue.getQhdm(), new AddressDataManager.AddressCallBack<AddreBean>() {
                @Override
                public void onAddressCallBack(List<AddreBean> mList) {
                    if (mList == null) {
                        showNoDataToast();
                        return;
                    }
                    if (mList.size() == 0) {
                        showNoDataToast();
                    }
                    addHeadBean(mList, conuntryValue);
                    mCountyList = mList;
                    mAddressAdapter.refreshData(mList);
                    mAddressAdapter.notifyDataSetChanged();
                    tabIndex = INDEX_TAB_COUNTY;
                    //
                    updateTabsVisibility();
                    updateProgressVisibility();
                    updateIndicator();
                }
            });
        } else {
            ToastUtils.showShort("乡镇列表为空");
        }
    }

    /**
     * 添加本地数据
     * 县区
     */
    public void addCountiesWith(List<AddreBean> mList) {
        if (!CollectionUtils.isEmpty(mList)) {
            progressBar.setVisibility(View.VISIBLE);
            mCountyList = mList;
            mAddressAdapter.refreshData(mList);
            mAddressAdapter.notifyDataSetChanged();
            tabIndex = INDEX_TAB_COUNTY;
            //
            updateTabsVisibility();
            updateProgressVisibility();
            updateIndicator();
        } else {
            ToastUtils.showShort("乡镇列表为空");
        }
    }

    /**
     * 根据乡镇id查询乡镇列表
     *
     * @param xiangValue 乡镇id
     */
    public void retrieveTownWith(AddreBean xiangValue) {
        progressBar.setVisibility(View.VISIBLE);
        mAddressDataManager.getAddressDataList(xiangValue.getQhdm(), new AddressDataManager.AddressCallBack<AddreBean>() {
            @Override
            public void onAddressCallBack(List<AddreBean> mList) {
                if (mList == null) {
                    showNoDataToast();
                    return;
                }
                if (mList.size() == 0) {
                    showNoDataToast();
                }
                addHeadBean(mList, xiangValue);
                mTownList = mList;
                mAddressAdapter.refreshData(mList);
                tabIndex = INDEX_TAB_TOWN;
                //
                updateTabsVisibility();
                updateProgressVisibility();
                updateIndicator();
            }
        });
    }

    /**
     * 添加本地数据
     * 乡镇
     */
    public void addTownWith(List<AddreBean> mList) {
        progressBar.setVisibility(View.VISIBLE);
        mTownList = mList;
        mAddressAdapter.refreshData(mList);
        tabIndex = INDEX_TAB_TOWN;
        //
        updateTabsVisibility();
        updateProgressVisibility();
        updateIndicator();
    }

    /**
     * 根据id查询村列表
     *
     * @param villageValue 乡镇id
     */
    public void retrieveVillage(AddreBean villageValue) {
        progressBar.setVisibility(View.VISIBLE);
        mAddressDataManager.getVillageDataList(villageValue.getQhdm(), new AddressDataManager.AddressCallBack<AddreBean>() {
            @Override
            public void onAddressCallBack(List<AddreBean> mList) {
                if (mList == null) {
                    showNoDataToast();
                    return;
                }
                if (mList.size() == 0) {
                    showNoDataToast();
                }
                addHeadBean(mList, villageValue);
                mVillageList = mList;
                mAddressAdapter.refreshData(mList);
                tabIndex = INDEX_TAB_VILLAGE;
                //
                updateTabsVisibility();
                updateProgressVisibility();
                updateIndicator();
            }
        });
    }

    /**
     * 添加本地数据
     * 乡村
     */
    public void addVillage(List<AddreBean> mList) {
        progressBar.setVisibility(View.VISIBLE);
        mVillageList = mList;
        mAddressAdapter.refreshData(mList);
        tabIndex = INDEX_TAB_VILLAGE;
        //
        updateTabsVisibility();
        updateProgressVisibility();
        updateIndicator();
    }

    /**
     * 省份 城市 乡镇 街道 都选中完 后的回调
     */
    private void callbackInternal() {
        if (listener != null) {
            AddreBean province = mProvinceList == null || provinceIndex == INDEX_INVALID ? null : mProvinceList.get(provinceIndex);
            AddreBean city = mCitieList == null || cityIndex == INDEX_INVALID ? null : mCitieList.get(cityIndex);
            AddreBean county = mCountyList == null || countyIndex == INDEX_INVALID ? null : mCountyList.get(countyIndex);
            AddreBean town = mTownList == null || townIndex == INDEX_INVALID ? null : mTownList.get(townIndex);
            AddreBean village = mVillageList == null || villageIndex == INDEX_INVALID ? null : mVillageList.get(villageIndex);
            //
            listener.onAddressSelected(tabIndex, province, city, county, town, village);
        }
    }

    private void showNoDataToast() {
        Toast.makeText(context, "当前区划下没有数据", Toast.LENGTH_SHORT).show();
    }

    /**
     * 更新进度条
     */
    private void updateProgressVisibility() {
        ListAdapter adapter = listView.getAdapter();
        int itemCount = adapter.getCount();
        progressBar.setVisibility(itemCount >= 0 ? View.GONE : View.VISIBLE);
    }

    /**
     * 获得view
     *
     * @return
     */
    protected View getView() {
        return view;
    }


//    public OnAddressSelectedListener getOnAddressSelectedListener() {
//        return listener;
//    }

    /**
     * 设置地址监听
     *
     * @param listener
     */
    protected void setOnAddressSelectedListener(OnAddressSelectedListener listener) {
        this.listener = listener;
    }

//    protected interface OnDialogCloseListener {
//        void dialogclose();
//    }

    /**
     * 设置close监听
     */
//    public void setOnDialogCloseListener(OnDialogCloseListener listener) {
//        this.dialogCloseListener = listener;
//    }

//    protected interface onSelectorAreaPositionListener {
//        void selectorAreaPosition(int provincePosition, int cityPosition, int countyPosition, int townPosition, int villagePosition);
//    }

//    public void setOnSelectorAreaPositionListener(onSelectorAreaPositionListener listener) {
//        this.selectorAreaPositionListener = listener;
//    }
}
