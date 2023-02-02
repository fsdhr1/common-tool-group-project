package com.gykj.smartdialogmoudle.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.gykj.smartdialogmoudle.R;
import com.gykj.smartdialogmoudle.bean.SelectDialogListBean;

import java.util.List;

public class SimpleAdapter extends BaseAdapter {
    private Context context;
    private List<SelectDialogListBean> list;

    public SimpleAdapter(Context context, List<SelectDialogListBean> crops) {
        this.context = context;
        this.list = crops;
    }

    public void setData(List<SelectDialogListBean> datas){
        list = datas;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int i) {
        return list.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        SimpleAdapter.ViewHolder holder;
        if (view == null) {
            view = View.inflate(context, R.layout.item_from_select_dialog, null);
            holder = new SimpleAdapter.ViewHolder(view);
            view.setTag(holder);
        }else {
            holder = (SimpleAdapter.ViewHolder) view.getTag();
        }
        holder.tvCrop.setText(list.get(i).getValue());
        return view;
    }

    static class ViewHolder {
        TextView tvCrop ;
        public ViewHolder(View view){
            tvCrop = view.findViewById(R.id.more_button_txt);
        }
    }
}
