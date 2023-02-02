package com.grantch.addressselectview.view;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.GsonUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ScreenUtils;
import com.blankj.utilcode.util.SizeUtils;
import com.google.gson.reflect.TypeToken;
import com.grantch.addressselectview.data.AddressBean;
import com.grantch.addressselectview.data.DataManager;
import com.grantch.addressselectview.data.LevelType;
import com.grantch.addressselectview.data.ListUtils;
import com.grantch.addressselectview.R;
import com.grantch.addressselectview.adapter.AddreassSelectorAdapter;
import com.grantch.addressselectview.adapter.AddressViewAdapter;
import com.grantch.addressselectview.base.BaseRecycleAdapter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ************************
 * 项目名称：commontool
 *
 * @Author hxh
 * 创建时间：2021/12/10   17:58
 * ************************
 */
public class AddressSelectView extends RelativeLayout  implements BaseRecycleAdapter.IViewClickListener<AddressBean>, AddressView {
    private Context mContext;
    private AddreassSelectorAdapter addreassSelectorAdapter;
    private AddressViewAdapter mAdapter;
    private DLSideBar mSideBarView;
    private LevelType mMaxLevel = LevelType.PROVINCE;
    private LevelType mMinLevel = LevelType.VILLAGE;
    private LevelType mCurrentLevel =mMaxLevel;
    private View indicator;
    private LinearLayout layout_tab;
    private TextView textViewProvince;
    private TextView textViewCity;
    private TextView textViewCounty;
    private TextView textViewTown;
    private TextView textViewVillage;
    private ProgressBar pgLoading;
    private List<AddressBean> mProvinceList ,mCitieList,mCountyList,mTownList,mVillageList;
    private AddressBean mProvince,mCity,mCounty,mTown,mVillage;
    private View view;
    private int selectedColor;
    private int unSelectedColor;
    private RecyclerView recyclerView;
    private boolean showSlideBar = true;
    private float mTitleTextSize;
    private float mContentTextSize;
    private String mStar; //内容和标题分隔符


    public AddressSelectView(Context context) {
        super(context);
        mContext = context;
        initData(null);
    }

    public AddressSelectView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        initData(attrs);
    }

    public AddressSelectView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        initData(attrs);
    }


    private void initData(@Nullable AttributeSet attrs) {
        final float defaultSideTextSize = getResources().getDimension(R.dimen.default_side_text_size);
        final float defaultSelectviewTitleTextSize = getResources().getDimension(R.dimen.default_selectview_title_text_size);
        TypedArray a = mContext.obtainStyledAttributes(attrs, R.styleable.selectview);
        selectedColor = R.color.color_f65347;
        unSelectedColor =R.color.color_4d4d4d;

        mTitleTextSize = a.getDimension(R.styleable.selectview_titleTextSize, defaultSelectviewTitleTextSize);
        mContentTextSize = a.getDimension(R.styleable.selectview_contentTextSize, defaultSideTextSize);

        view = LayoutInflater.from(mContext).inflate(R.layout.layout_widget_address_selector_view, null);
        addView(view);
        indicator = view.findViewById(R.id.indicator); //指示器
        layout_tab = (LinearLayout) view.findViewById(R.id.layout_tab);
        textViewProvince = (TextView) view.findViewById(R.id.textViewProvince);//省份
        textViewCity = (TextView) view.findViewById(R.id.textViewCity);//城市
        textViewCounty = (TextView) view.findViewById(R.id.textViewCounty);//区 县
        textViewTown = (TextView) view.findViewById(R.id.textViewTown);//乡
        textViewVillage = (TextView) view.findViewById(R.id.textViewVillage);//村
        pgLoading = (ProgressBar) view.findViewById(R.id.pb_address);

        recyclerView = view.findViewById(R.id.rv_address);
        mSideBarView = view.findViewById(R.id. dsb_address);
        LinearLayoutManager manager = new LinearLayoutManager(mContext);
        recyclerView.setLayoutManager(manager);
        addreassSelectorAdapter = new AddreassSelectorAdapter(recyclerView);
        recyclerView.setAdapter(addreassSelectorAdapter);
        recyclerView.addItemDecoration(new rvItemDecoration());
        mSideBarView.setOnTouchingLetterChangedListener(new DLSideBar.OnTouchingLetterChangedListener() {
            @Override
            public void onTouchingLetterChanged(String str) {
                int i = addreassSelectorAdapter.getPositionByTitel(str);
                if (i!=-1){
                    recyclerView.scrollToPosition(i);
                }
            }
        });

        updateTabTextSize();
        addreassSelectorAdapter.setIViewClickListener(this);

        textViewProvince.setOnClickListener(new OnProvinceTabClickListener());
        textViewCity.setOnClickListener(new OnCityTabClickListener());
        textViewCounty.setOnClickListener(new onCountyTabClickListener());
        textViewTown.setOnClickListener(new OnTownTabClickListener());
        textViewVillage.setOnClickListener(new OnVillageTabClickListener());



    }


    private void refreshData(List<AddressBean> datas){
        DataManager d= DataManager.getInstance(mContext);
        d.setData(datas);
        if (showSlideBar){
            if (d.getDataList()!=null&&d.getDataList().size()>0){
                addreassSelectorAdapter.removeAll();
                addreassSelectorAdapter.appendList(d.getDataList());
                mSideBarView.setStringArray(d.getHeadList());
                recyclerView.scrollToPosition(addreassSelectorAdapter.getSelectPos());
            }
        }else {
            if (datas!=null&&datas.size()>0){
                addreassSelectorAdapter.removeAll();
                addreassSelectorAdapter.appendList(datas);
                mSideBarView.setVisibility(GONE);
                recyclerView.scrollToPosition(addreassSelectorAdapter.getSelectPos());
            }
        }

    }



    public void setAdapter(AddressViewAdapter addressViewAdapter){
        mAdapter =addressViewAdapter;
        mMaxLevel = mAdapter.getmMaxLevel();
        mMinLevel = mAdapter.getmMinLevel();
        if (mCurrentLevel==null){
            mCurrentLevel=mMaxLevel;
        }
        refreshUi(mCurrentLevel);
    }

    private List<AddressBean> changeDataSelect(List<AddressBean> datas , AddressBean selected){
        for (AddressBean cell:datas){
            if (selected!=null&&selected.toString().equals(cell.toString())){
                cell.setSelect(true);
            }else{
                cell.setSelect(false);
            }
        }
        return datas;
    }

    private void setmCurrentLevelByCode(String areaCode){
        if (areaCode!=null){
            if (areaCode.length()==2){
                mCurrentLevel = LevelType.PROVINCE;
            }else if (areaCode.length()==4){
                mCurrentLevel = LevelType.CITY;
            }else if (areaCode.length()==6){
                mCurrentLevel = LevelType.COUNTY;
            }else if (areaCode.length()==9){
                mCurrentLevel= LevelType.TOWN;
            }else if (areaCode.length()==12){
                mCurrentLevel = LevelType.VILLAGE;
            }
        }
    }

    @Override
    public void clearLevelSelected(LevelType levelType) {
        if (levelType==LevelType.PROVINCE){
            if (mProvinceList==null){
                LogUtils.v("没有设置省级数据");
                return;
            }
            mProvince=null;
            refreshData(changeDataSelect(mProvinceList,mProvince));
            // 更新当前级别及子级标签文本
            textViewProvince.setText("请选择");
            textViewCity.setText("请选择");
            textViewCounty.setText("请选择");
            textViewTown.setText("请选择");
            textViewVillage.setText("请选择");
            // 清空子级数据
            mCitieList=null;
            mCountyList=null;
            mTownList=null;
            mVillageList=null;
        }else if (levelType==LevelType.CITY){
            if (mCitieList==null){
                LogUtils.v("没有设置市级数据");
                return;
            }
            mCity =null;
            refreshData(changeDataSelect(mCitieList,mCity));
            textViewCity.setText("请选择");
            // 清空下级别数据
            textViewCounty.setText("请选择");
            textViewTown.setText("请选择");
            textViewVillage.setText("请选择");
            mCountyList=null;
            mTownList=null;
            mVillageList=null;
        }else if (levelType==LevelType.COUNTY){
            if (mCountyList==null){
                LogUtils.v("没有设置县区级数据");
                return;
            }
            mCounty =null;
            refreshData(changeDataSelect(mCountyList,mCounty));
            textViewCounty.setText("请选择");
            //清空下级别数据
            textViewTown.setText("请选择");
            textViewVillage.setText("请选择");
            mTownList=null;
            mVillageList=null;
        }else if(levelType==LevelType.TOWN){

            if (mTownList==null){
                LogUtils.v("没有设置乡镇级数据");
                return;
            }
            mTown = null;
            refreshData(changeDataSelect(mTownList,mTown));
            // 清空下级别数据
            textViewVillage.setText("请选择");
            textViewTown.setText("请选择");
            mVillageList=null;
        }else if (levelType==LevelType.VILLAGE){
            if (mVillageList==null){
                LogUtils.v("没有设置村级数据");
                return;
            }
            mVillage =null;
            textViewVillage.setText("请选择");
            refreshData(changeDataSelect(mVillageList,mVillage));
        }
        refreshUi(levelType);
    }


    public void setContentSplitString(String star){
            this.mStar = star;
    }

    @Override
    public void onClick(View view, AddressBean data, int position) {
        setmCurrentLevelByCode(data.getQhdm());
        String title =data.getQhmc();
        if (!TextUtils.isEmpty(mStar)){
            title = title.contains(mStar)?title.split(mStar)[0]:title;
        }
        if (mCurrentLevel==LevelType.PROVINCE){
//            if (mProvince!=null&&mProvince.toString().equals(data.toString())){
//                return;
//            }
            if (mProvinceList==null){
                LogUtils.v("没有设置省级数据");
                return;
            }
            mProvince =data;
            refreshData(changeDataSelect(mProvinceList,mProvince));
            // 更新当前级别及子级标签文本
            textViewProvince.setText(title);
            textViewCity.setText("请选择");
            textViewCounty.setText("请选择");
            textViewTown.setText("请选择");
            textViewVillage.setText("请选择");
            // 清空子级数据
            mCitieList=null;
            mCountyList=null;
            mTownList=null;
            mVillageList=null;
            mAdapter.currentLevelSelected(LevelType.PROVINCE,mProvince);
        }else if (mCurrentLevel==LevelType.CITY){
//            if (mCity!=null&&mCity.toString().equals(data.toString())){
//                return;
//            }
            if (mCitieList==null){
                LogUtils.v("没有设置市级数据");
                return;
            }
            mCity =data;
            refreshData(changeDataSelect(mCitieList,mCity));
            textViewCity.setText(title);
            // 清空下级别数据
            textViewCounty.setText("请选择");
            textViewTown.setText("请选择");
            textViewVillage.setText("请选择");
            mCountyList=null;
            mTownList=null;
            mVillageList=null;
            mAdapter.currentLevelSelected(LevelType.CITY,mCity);
        }else if (mCurrentLevel==LevelType.COUNTY){
//            if (mCounty!=null&&mCounty.toString().equals(data.toString())){
//                return;
//            }
            if (mCountyList==null){
                LogUtils.v("没有设置县区级数据");
                return;
            }
            mCounty =data;
            refreshData(changeDataSelect(mCountyList,mCounty));
            textViewCounty.setText(title);
            //清空下级别数据
            textViewTown.setText("请选择");
            textViewVillage.setText("请选择");
            mTownList=null;
            mVillageList=null;
            mAdapter.currentLevelSelected(LevelType.COUNTY,mCounty);
        }else if(mCurrentLevel==LevelType.TOWN){
//            if (mTown!=null&&mTown.toString().equals(data.toString())){
//                return;
//            }
            if (mTownList==null){
                LogUtils.v("没有设置乡镇级数据");
                return;
            }
            mTown = data;
            refreshData(changeDataSelect(mTownList,mTown));
            // 清空下级别数据
            textViewVillage.setText("请选择");
            textViewTown.setText(title);
            mVillageList=null;
            mAdapter.currentLevelSelected(LevelType.TOWN,mTown);
        }else if (mCurrentLevel==LevelType.VILLAGE){
            if (mVillage!=null&&mVillage.toString().equals(data.toString())){
                return;
            }
            if (mVillageList==null){
                LogUtils.v("没有设置村级数据");
                return;
            }
            mVillage =data;
            textViewVillage.setText(title);
            refreshData(changeDataSelect(mVillageList,mVillage));
            updateIndicator(LevelType.VILLAGE);
            if (selectedColor != 0 && unSelectedColor != 0) {
                updateTabTextColor(mCurrentLevel);
            }
            mAdapter.currentLevelSelected(LevelType.VILLAGE,mVillage);
        }
    }

    @Override
    public void dataChanged(LevelType levelType, List<AddressBean> datas) {
        if (levelType.equals(LevelType.PROVINCE)){
            mProvinceList =datas;
        }else if (levelType.equals(LevelType.CITY)){
            mCitieList =datas;
        }else if (levelType.equals(LevelType.COUNTY)){
            mCountyList=datas;
        }else if (levelType.equals(LevelType.TOWN)){
            mTownList = datas;
        }else if (levelType.equals(LevelType.VILLAGE)){
            mVillageList =datas;
        }
        refreshUi(levelType);
        refreshData(datas);
    }

    private void refreshUi(LevelType levelType) {

        textViewProvince.setVisibility(mMaxLevel.getmLevel()<LevelType.PROVINCE.getmLevel()?GONE:VISIBLE);
        textViewCity.setVisibility(mMaxLevel.getmLevel()<LevelType.CITY.getmLevel()?GONE:VISIBLE);
        textViewCounty.setVisibility(mMaxLevel.getmLevel()<LevelType.COUNTY.getmLevel()?GONE:VISIBLE);
        textViewTown.setVisibility(mMaxLevel.getmLevel()<LevelType.TOWN.getmLevel()?GONE:VISIBLE);

        textViewVillage.setVisibility(mMinLevel.getmLevel()>LevelType.VILLAGE.getmLevel()?GONE:VISIBLE);
        textViewTown.setVisibility(mMinLevel.getmLevel()>LevelType.TOWN.getmLevel()?GONE:VISIBLE);
        textViewCounty.setVisibility(mMinLevel.getmLevel()>LevelType.COUNTY.getmLevel()?GONE:VISIBLE);
        textViewCity.setVisibility(mMinLevel.getmLevel()>LevelType.CITY.getmLevel()?GONE:VISIBLE);

        textViewProvince.setVisibility(ListUtils.notEmpty(mProvinceList) ? View.VISIBLE : View.GONE);
        textViewCity.setVisibility(ListUtils.notEmpty(mCitieList) ? View.VISIBLE : View.GONE);
        textViewCounty.setVisibility(ListUtils.notEmpty(mCountyList) ? View.VISIBLE : View.GONE);
        textViewTown.setVisibility(ListUtils.notEmpty(mTownList) ? View.VISIBLE : View.GONE);
        textViewVillage.setVisibility(ListUtils.notEmpty(mVillageList) ? View.VISIBLE : View.GONE);
        updateIndicator(levelType);
        if (selectedColor != 0 && unSelectedColor != 0) {
            updateTabTextColor(levelType);
        }
    }

    /**
     * 更新字体的颜色
     */
    private void updateTabTextColor(LevelType levelType) {
        indicator.setBackgroundColor(mContext.getResources().getColor(selectedColor));
        textViewProvince.setTextColor(mContext.getResources().getColor(levelType==LevelType.PROVINCE?selectedColor:unSelectedColor));
        textViewCity.setTextColor(mContext.getResources().getColor(levelType==LevelType.CITY?selectedColor:unSelectedColor));
        textViewCounty.setTextColor(mContext.getResources().getColor(levelType==LevelType.COUNTY?selectedColor:unSelectedColor));
        textViewTown.setTextColor(mContext.getResources().getColor(levelType==LevelType.TOWN?selectedColor:unSelectedColor));
        textViewVillage.setTextColor(mContext.getResources().getColor(levelType==LevelType.VILLAGE?selectedColor:unSelectedColor));
        addreassSelectorAdapter.setSelectColors(selectedColor,unSelectedColor);
    }

    private void updateTabTextSize() {
        textViewProvince.setTextSize(SizeUtils.px2sp(mTitleTextSize));
        textViewCity.setTextSize(SizeUtils.px2sp(mTitleTextSize));
        textViewCounty.setTextSize(SizeUtils.px2sp(mTitleTextSize));
        textViewTown.setTextSize(SizeUtils.px2sp(mTitleTextSize));
        textViewVillage.setTextSize(SizeUtils.px2sp(mTitleTextSize));
        addreassSelectorAdapter.setContentSize(SizeUtils.px2sp(mContentTextSize));
        addreassSelectorAdapter.setTitleSize(SizeUtils.px2sp(mContentTextSize));

    }


    /**
     * 更新tab 指示器
     */

    private void updateIndicator(LevelType levelType) {
        view.post(new Runnable() {
            @Override
            public void run() {
                switch (levelType) {
                    case PROVINCE: //省份
                        buildIndicatorAnimatorTowards(textViewProvince).start();
                        break;
                    case CITY: //城市
                        buildIndicatorAnimatorTowards(textViewCity).start();
                        break;
                    case COUNTY: //区县
                        buildIndicatorAnimatorTowards(textViewCounty).start();
                        break;
                    case TOWN: //乡镇
                        buildIndicatorAnimatorTowards(textViewTown).start();
                        break;
                    case VILLAGE: //村
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
     * 点击省份的监听
     */
    private class OnProvinceTabClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            mCurrentLevel = LevelType.PROVINCE;
            if (mProvinceList!=null){
                refreshUi(mCurrentLevel);
                refreshData(changeDataSelect(mProvinceList,mProvince));
            }
        }
    }

    /**
     * 点击城市的监听
     */
    private class OnCityTabClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            mCurrentLevel = LevelType.CITY;
            if (mCitieList!=null){
                refreshUi(mCurrentLevel);
                refreshData(changeDataSelect(mCitieList,mCity));
            }

        }
    }

    /**
     * 点击区县的监听
     */
    private class onCountyTabClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            mCurrentLevel = LevelType.COUNTY;
            if (mCountyList!=null){
                refreshUi(mCurrentLevel);
                refreshData(changeDataSelect(mCountyList,mCounty));
            }

        }
    }

    /**
     * 点乡镇的监听
     */
    private class OnTownTabClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            mCurrentLevel = LevelType.TOWN;
            if(mTownList!=null){
                refreshUi(mCurrentLevel);
                refreshData(changeDataSelect(mTownList,mTown));
            }
        }
    }

    /**
     * 点击街道的监听
     */
    private class OnVillageTabClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            mCurrentLevel = LevelType.VILLAGE;
            if (mVillageList!=null){
                refreshUi(mCurrentLevel);
                refreshData(changeDataSelect(mVillageList,mVillage));
            }

        }
    }

    @Override
    public void showLoading() {
        pgLoading.setVisibility(VISIBLE);
    }

    @Override
    public void closeLoading() {
        pgLoading.setVisibility(GONE);
    }

    @Override
    public void delfultSelectedItem(LevelType levelType, AddressBean addreBean) {
        switch (levelType){
            case PROVINCE: //省份
                mProvince=addreBean;
                break;
            case CITY: //城市
                mCity = addreBean;
                break;
            case COUNTY: //区县
                mCounty =addreBean;
                break;
            case TOWN: //乡镇
                mTown =addreBean;
                break;
            case VILLAGE: //村
                mVillage=addreBean;
                break;
        }
        if (addreBean!=null&&addreBean.getQhdm()!=null){
            String jsonString = SPUtils.getInstance("selectview").getString(addreBean.getQhdm());
            getHistoryData(jsonString);
        }
    }


    private void getHistoryData(String jsonString){
        if (!TextUtils.isEmpty(jsonString)){
            Map<String,List<AddressBean>> map=GsonUtils.fromJson(jsonString,new TypeToken<Map<String,List<AddressBean>>>(){}.getType());
            if (map!=null){
                for (String key:map.keySet()){
                    AddressBean data =getDataByCode(key,map.get(key));
                    String title =data.getQhmc();
                    if (!TextUtils.isEmpty(mStar)){
                        title = title.contains(mStar)?title.split(mStar)[0]:title;
                    }
                    if(key.length()==2){
                        mProvinceList =map.get(key);
                        if (data!=null){
                            mProvince =data;
                            textViewProvince.setText(title);
                            mCurrentLevel =LevelType.PROVINCE;
                            refreshData(changeDataSelect(mProvinceList,mProvince));
                        }
                    }else if (key.length()==4){
                        mCitieList = map.get(key);
                        if (data!=null){
                            mCity = data;
                            mCurrentLevel = LevelType.CITY;
                            textViewCity.setText(title);
                            refreshData(changeDataSelect(mCitieList,mCity));
                        }
                    }else if (key.length()==6){
                        mCountyList = map.get(key);
                        if (data!=null){
                            mCounty = data;
                            textViewCounty.setText(title);
                            mCurrentLevel =LevelType.COUNTY;
                            refreshData(changeDataSelect(mCountyList,mCounty));
                        }
                    }else if (key.length()==9){
                        mTownList = map.get(key);
                        if (data!=null){
                            mTown = data;
                            textViewTown.setText(title);
                            mCurrentLevel =LevelType.TOWN;
                            refreshData(changeDataSelect(mTownList,mTown));
                        }
                    }else if(key.length()==12){
                        mVillageList = map.get(key);
                        if (data!=null){
                            mVillage = data;
                            textViewVillage.setText(title);
                            mCurrentLevel =LevelType.VILLAGE;
                            refreshData(changeDataSelect(mVillageList,mVillage));
                        }
                    }
                }
            }
            refreshUi(mCurrentLevel);

        }
    }

    @Override
    public void saveSelectedItem(AddressBean addreBean) {
        Map<String,List<AddressBean>> map = new HashMap();
        if (mProvince!=null&&mProvinceList!=null){
            map.put(mProvince.getQhdm(),mProvinceList);
        }
        if (mCity!=null&&mCitieList!=null){
            map.put(mCity.getQhdm(),mCitieList);
        }
        if (mCounty!=null&&mCountyList!=null){
            map.put(mCounty.getQhdm(),mCountyList);
        }
        if (mTown!=null&&mTownList!=null){
            map.put(mTown.getQhdm(),mTownList);
        }
        if (mVillage!=null&&mVillageList!=null){
            map.put(mVillage.getQhdm(),mVillageList);
        }
        if (map.size()>0){
            String jsonString =GsonUtils.toJson(map);
            SPUtils.getInstance("selectview").put(addreBean.getQhdm(),jsonString);
        }

    }



    private AddressBean getDataByCode(String code, List<AddressBean> datas){
        if(datas==null)return null;
        for (int i=0;i<datas.size();i++){
            if (code.equals(datas.get(i).getQhdm())){
                return datas.get(i);
            }
        }
        return null;
    }

    public void setSelectedColor(int selectedColor) {
        this.selectedColor = selectedColor;
    }

    public void setUnSelectedColor(int unSelectedColor) {
        this.unSelectedColor = unSelectedColor;
    }

    public void setShowSlideBar(boolean showSlideBar) {
        this.showSlideBar = showSlideBar;
        if (mSideBarView!=null){
            if (showSlideBar){
                mSideBarView.setVisibility(VISIBLE);
            }else {
                mSideBarView.setVisibility(GONE);
            }
        }
    }

    public void setmTitleTextSize(float mTitleTextSize) {
        this.mTitleTextSize = mTitleTextSize;
        updateTabTextSize();
    }

    public void setmContentTextSize(float mContentTextSize) {
        this.mContentTextSize = mContentTextSize;
        updateTabTextSize();
    }
}
