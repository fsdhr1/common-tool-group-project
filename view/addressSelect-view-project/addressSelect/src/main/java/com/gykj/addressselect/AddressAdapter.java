package com.gykj.addressselect;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;


import com.gykj.addressselect.bean.AddreBean;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ZhaiJiaChang.
 * <p>
 */
public class AddressAdapter extends BaseAdapter {
    private List<AddreBean> mBeanList = new ArrayList<>();

    public void refreshData(List<AddreBean> mBeans) {
        mBeanList.clear();
        mBeanList.addAll(mBeans);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mBeanList == null ? 0 : mBeanList.size();
    }

    @Override
    public AddreBean getItem(int position) {
        return mBeanList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Holder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.address_item_area, parent, false);
            holder = new Holder();
            holder.textView = (TextView) convertView.findViewById(R.id.textView);
            holder.imageViewCheckMark = (ImageView) convertView.findViewById(R.id.imageViewCheckMark);
            convertView.setTag(holder);
        } else {
            holder = (Holder) convertView.getTag();
        }
        AddreBean item = getItem(position);
        holder.textView.setText(item.getQhmc());
        boolean checked = mBeanList.get(position).isSelect();
        holder.textView.setEnabled(!checked);
        holder.imageViewCheckMark.setVisibility(checked ? View.VISIBLE : View.GONE);
        return convertView;
    }

    class Holder {
        TextView textView;
        ImageView imageViewCheckMark;
    }
}
