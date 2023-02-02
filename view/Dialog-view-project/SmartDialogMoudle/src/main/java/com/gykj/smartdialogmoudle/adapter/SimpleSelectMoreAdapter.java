package com.gykj.smartdialogmoudle.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.gykj.smartdialogmoudle.R;
import com.gykj.smartdialogmoudle.bean.SelectDialogListBean;

import java.util.List;

public class SimpleSelectMoreAdapter extends BaseAdapter {
    private Context context;
    private List<SelectDialogListBean> list;

    public SimpleSelectMoreAdapter(Context context, List<SelectDialogListBean> crops) {
        this.context = context;
        this.list = crops;
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
        SimpleSelectMoreAdapter.ViewHolder holder;
        if (view == null) {
            view = View.inflate(context, R.layout.item_from_select_more_dialog, null);
            holder = new SimpleSelectMoreAdapter.ViewHolder(view);
            view.setTag(holder);
        }else {
            holder = (SimpleSelectMoreAdapter.ViewHolder) view.getTag();
        }
        holder.tvContent.setText(list.get(i).getValue());
        if (list.get(i).isSelect()){
            holder.cbSelect.setChecked(true);
        }else {
            holder.cbSelect.setChecked(false);
        }
        return view;
    }

    public void changeData(List<SelectDialogListBean> mDataList) {
        list = mDataList;
        notifyDataSetChanged();
    }
    public List<SelectDialogListBean> getListData() {
        return list;
    }
    static class ViewHolder {
        TextView tvContent ;
        CheckBox cbSelect;
        public ViewHolder(View view){
            tvContent = view.findViewById(R.id.tv_item_content);
            cbSelect = view.findViewById(R.id.cb_select);
        }
    }
}
