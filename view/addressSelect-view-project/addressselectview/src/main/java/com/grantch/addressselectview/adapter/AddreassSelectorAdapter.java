package com.grantch.addressselectview.adapter;

import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.SPUtils;
import com.grantch.addressselectview.data.AddressBean;
import com.grantch.addressselectview.R;
import com.grantch.addressselectview.base.BaseRecycleAdapter;
import com.grantch.addressselectview.base.BaseViewHolder;
import com.grantch.addressselectview.data.LevelType;

/**
 * ************************
 * 项目名称：commontool
 *
 * @Author hxh
 * 创建时间：2021/12/9   14:10
 * ************************
 */
public class AddreassSelectorAdapter extends BaseRecycleAdapter<AddressBean,BaseViewHolder<AddressBean>> {

    public static final int VIEW_TYPE_ITEM_TITLE = 0;
    private static final int VIEW_TYPE_ITEM_CONTENT = 1;
    private int selectPos;
    private float mContentSize=-1;

    public AddreassSelectorAdapter(@Nullable RecyclerView recyclerView) {
        super(recyclerView);
        mUnSelectedColor =context.getResources().getColor(R.color.color_4d4d4d);
    }
    @Override
    public BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_ITEM_TITLE) {
            return new TitleHolder(parent, R.layout.item_from_address_group_title);
        } else {
            return new ContentHolder(parent,R.layout.item_from_address_child);
        }
    }

    private int mSelectedColor=-1;
    private int mUnSelectedColor;
    public void setSelectColors(int selectedColor,int unSelectColor){
        mSelectedColor = selectedColor;
        mUnSelectedColor = unSelectColor;
    }

    private void setSelectorColor(TextView textView, int normal, int checked){
        int[] colors =new int[] { normal, checked,normal};

        int[][] states =new int[3][];

        states[0] =new int[] { -android.R.attr.state_enabled};

        states[1] =new int[] { android.R.attr.state_enabled};

        states[2] =new int[] {};

        ColorStateList colorStateList =new ColorStateList(states,colors);

        textView.setTextColor(colorStateList);

    }


    @Override
    public void onBindViewHolder(BaseViewHolder<AddressBean> holder, int position) {
        if (getItemViewType(position) == VIEW_TYPE_ITEM_TITLE) {
            TitleHolder titleHolder = (TitleHolder) holder;
            titleHolder.mTextTitle.setText(dataList.get(position).getQhmc());
        } else {
            ContentHolder contentHolder = (ContentHolder) holder;
            contentHolder.tvAddress.setText(dataList.get(position).getQhmc());
            AddressBean selectData= dataList.get(position);
            boolean checked = dataList.get(position).isSelect();
            if (checked){
                selectPos = position;
            }
            if (mSelectedColor!=-1){
                setSelectorColor(contentHolder.tvAddress,ContextCompat.getColor(context,mSelectedColor),ContextCompat.getColor(context,mUnSelectedColor));
                contentHolder.imageViewCheckMark.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(context,mSelectedColor)));;
            }
            if (mContentSize!=-1){
                contentHolder.tvAddress.setTextSize(mContentSize);
            }
            contentHolder.tvAddress.setEnabled(!checked);
            contentHolder.imageViewCheckMark.setVisibility(checked ? View.VISIBLE : View.GONE);
            contentHolder.rlParent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (iViewClickListener!=null){
                        iViewClickListener.onClick(v,selectData,position);
                    }
                }
            });
        }
    }

    public int getSelectPos() {
        return selectPos;
    }

    @Override
    public int getItemViewType(int position) {
        return dataList.get(position).getType();
    }

    @Override
    public boolean isPinnedPosition(int position) {
        return getItemViewType(position) == VIEW_TYPE_ITEM_TITLE;
    }

    @Override
    public void isPosItemVisible(int firstItemPosition, int lastItemPosition) {

    }

    @Override
    public void scrollState(RecyclerView recyclerView, int newState) {

    }


    public int getPositionByTitel(String title){
        for (int i=0;i<dataList.size();i++){
            if (dataList.get(i).qhmc.equals(title)){
                return i;
            }
        }
        return -1;
    }

    public void setContentSize(float mContentTextSize) {
        this.mContentSize = mContentTextSize;
    }

    public void setTitleSize(float mTitleTextSize) {
        
    }

    public class ContentHolder extends BaseViewHolder {

        TextView tvAddress;
        ImageView imageViewCheckMark;
        RelativeLayout rlParent;
        public ContentHolder(ViewGroup parent, int resId) {
            super(parent, resId);
            tvAddress = (TextView) getView(R.id.tv_address);
            imageViewCheckMark = (ImageView) getView(R.id.imageViewCheckMark);
            rlParent = (RelativeLayout) getView(R.id.rl_parent);
        }


        @Override
        public void setData(Object data, int position) {

        }

   }

    public class TitleHolder extends BaseViewHolder {

        TextView mTextTitle;

        public TitleHolder(ViewGroup parent, int resId) {
            super(parent, resId);
            mTextTitle = (TextView) getView(R.id.tv_title);
        }


        @Override
        public void setData(Object data, int position) {

        }
    }
}
