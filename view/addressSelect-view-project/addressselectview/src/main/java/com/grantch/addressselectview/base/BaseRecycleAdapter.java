package com.grantch.addressselectview.base;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zy on 2017/1/5.
 * RecycleView adapter基类
 */

public abstract class BaseRecycleAdapter<M, H extends BaseViewHolder<M>> extends RecyclerView.Adapter<H> {

    protected View mHeaderView;
    /**
     * RecyclerView
     */
    protected RecyclerView parentView;
    /**
     * 数据
     */
    protected List<M> dataList;
    /**
     * 事件
     */
    protected ItemClickListener.OnItemClickListener<H> listener;
    /**
     * Context
     */
    protected Context context;
    /**
     *
     */
    protected int firstItemPosition;
    /**
     *
     */
    protected int lastItemPosition;
    /**
     * 可见
     */
    protected int visibleItemCount = 0;
    /**
     *
     */
    protected List<BaseViewHolder> baseViewHolders;


    /**
     * 设置数据,并设置点击回调接口
     *
     * @param list     数据集合
     * @param listener 回调接口
     */
    public BaseRecycleAdapter(@Nullable Context context, @Nullable List<M> list, @Nullable ItemClickListener.OnItemClickListener<H> listener) {
        this.dataList = list;
        this.context = context;
        if (this.dataList == null) {
            this.dataList = new ArrayList<>();
        }
        this.listener = listener;
        this.baseViewHolders = new ArrayList<>();
    }

    /**
     * 设置数据,并设置点击回调接口
     *
     * @param context
     * @param list
     */
    public BaseRecycleAdapter(@Nullable Context context, @Nullable List<M> list) {
        this.dataList = list;
        this.context = context;
        if (this.dataList == null) {
            this.dataList = new ArrayList<>();
        }
        this.baseViewHolders = new ArrayList<>();
    }

    /**
     * 设置数据,并设置点击回调接口
     *
     * @param recyclerView
     * @param list
     */
    public BaseRecycleAdapter(@Nullable RecyclerView recyclerView, @Nullable List<M> list) {
        this.dataList = list;
        this.context = recyclerView.getContext();
        this.parentView = recyclerView;
        parentView.setItemAnimator(new DefaultItemAnimator());
        if (this.dataList == null) {
            this.dataList = new ArrayList<>();
        }
        this.baseViewHolders = new ArrayList<>();
    }

    public BaseRecycleAdapter(@Nullable RecyclerView recyclerView) {
        this.context = recyclerView.getContext();
        this.parentView = recyclerView;
        parentView.setItemAnimator(new DefaultItemAnimator());
        this.dataList = new ArrayList<>();
        this.baseViewHolders = new ArrayList<>();
    }

    @Override
    public abstract H onCreateViewHolder(ViewGroup parent, int viewType);

    protected void onCreateViewHolderHead(int viewType) {
        //if (mHeaderView != null && viewType == TYPE_HEADER) return new Holder(mHeaderView);
    }

    @Override
    public void onBindViewHolder(final H holder, final int position) {
        holder.position = position;
        holder.data = dataList.get(position);
        holder.setData(dataList.get(position), position);//item数据绑定

        //如果查找下一条哪里出错了，请删除这里 并检查
        if (baseViewHolders != null) {
            if (!baseViewHolders.contains(holder)) {
                baseViewHolders.add(holder);
            }
        }


        //holder.setEventListen(dataList.get(position), position);//item内空间事件的监听
        if (listener != null) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemClick(holder, position);
                }
            });
            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    listener.onItemLongClick(holder, position);
                    return false;
                }
            });
        }
    }


    public void setHeaderView(View headerView) {
        mHeaderView = headerView;
        notifyItemInserted(0);
    }

    public View getHeaderView() {
        return mHeaderView;
    }

    public int getRealPosition(RecyclerView.ViewHolder holder) {
        int position = holder.getLayoutPosition();
        return mHeaderView == null ? position : position - 1;
    }

    public BaseViewHolder getViewHolderByPosition(int position) {
        try {
            View view = parentView.getLayoutManager().findViewByPosition(position);
            BaseViewHolder viewHolder = null;
            if (view != null) {
                viewHolder = (BaseViewHolder) parentView.getChildViewHolder(view);
            }
            return viewHolder;
        } catch (Exception e) {
            return null;
        }
    }

    public BaseViewHolder getViewHolderByData(M data) {
        try {
            int position = dataList.indexOf(data);
            View view = parentView.getLayoutManager().findViewByPosition(position);
            BaseViewHolder viewHolder = null;
            if (view != null) {
                viewHolder = (BaseViewHolder) parentView.getChildViewHolder(view);
            }
            return viewHolder;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public int getItemCount() {
        return mHeaderView == null ? dataList.size() : dataList.size() + 1;
    }

    public int getLastItemPosition() {
        int lastPosition = getItemCount() - 1;
        return lastPosition > 0 ? lastPosition : 0;
    }

    /*@Override
    public int getItemCount() {
        return dataList.size();
    }*/

    /**
     * 填充数据,此方法会清空以前的数据
     *
     * @param list 需要显示的数据
     */
    public void fillList(List<M> list) {
        dataList.clear();
        dataList.addAll(list);
    }

    /**
     * 更新数据
     *
     * @param holder item对应的holder
     * @param data   item的数据
     */
    public void updateItem(H holder, M data) {
        dataList.set(holder.getLayoutPosition(), data);
    }

    /**
     * 获取一条数据
     *
     * @param holder item对应的holder
     * @return 该item对应的数据
     */
    public M getItem(H holder) {
        return dataList.get(holder.getLayoutPosition());
    }

    /**
     * 获取一条数据
     *
     * @param position item的位置
     * @return item对应的数据
     */
    public M getItem(int position) {
        return dataList.get(position);
    }

    public List<M> getItems(List<Integer> positions) {
        if (positions == null || positions.size() == 0) return null;
        M m;
        List<M> temp = new ArrayList<>();
        for (int i = 0; i < positions.size(); i++) {
            m = getItem(positions.get(i));
            if (m == null) continue;
            temp.add(m);
        }
        return temp;
    }


    /**
     * 追加一条数据
     *
     * @param data 追加的数据
     */
    public void appendItem(M data) {
        try {
            dataList.add(data);
            notifyDataSetChanged();
            if (parentView != null) {
                int lastIndex = getLastItemPosition();
                parentView.smoothScrollToPosition(lastIndex);
            }

           /* notifyItemRangeChanged(position, dataList.size() - position);//通知数据与界面重新绑定
            if (parentView != null) {
                parentView.smoothScrollToPosition(position);
            }*/

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void removeItem(int position) {
        try {
            dataList.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, dataList.size());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 删除指定数据条目
     *
     * @param model
     */
    public void removeItem(M model) {
        removeItem(dataList.indexOf(model));
    }


    public void insertItem(int position, M item) {
        try {
            dataList.add(position, item);

            notifyItemInserted(position);//通知演示插入动画
            notifyItemRangeChanged(position, dataList.size() - position);//通知数据与界面重新绑定

            if (parentView != null) {
                parentView.smoothScrollToPosition(position);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void upDateItem(M item) {
        try {
            int position = dataList.indexOf(item);
            notifyItemChanged(position);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void reLoadItem(int position) {
        M item = getItem(position);
        upDateItem(item);
    }

    public void changeItem(int position, M item) {
        try {
            dataList.remove(position);
            dataList.add(position, item);
            notifyItemChanged(position);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void removeAll() {
        try {
            dataList.clear();
            notifyDataSetChanged();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void replaceAll(List<M> items) {
        removeAll();
        appendList(items);
    }

    public void removeItems(List<M> items) {
        try {
            for (M m : items) {
                removeItem(dataList.indexOf(m));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 追加一个集合数据
     *
     * @param list 要追加的数据集合
     */
    public void appendList(List<M> list) {
        try {
            dataList.addAll(list);
            notifyDataSetChanged();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void appendList(List<M> list, boolean autoLoad) {
        try {
            dataList.addAll(list);
            if (autoLoad) {
                notifyDataSetChanged();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 在最顶部前置数据
     *
     * @param data 要前置的数据
     */
    public void preposeItem(M data) {
        dataList.add(0, data);
    }

    /**
     * 在顶部前置数据集合
     *
     * @param list 要前置的数据集合
     */
    public void preposeList(List<M> list) {
        dataList.addAll(0, list);
    }

    public Boolean compareEqual(M m1, M m2) {
        return null;
    }


    public class ScrollListen extends RecyclerView.OnScrollListener {

        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            scrollState(recyclerView, newState);
        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
            //判断是当前layoutManager是否为LinearLayoutManager
            // 只有LinearLayoutManager才有查找第一个和最后一个可见view位置的方法
            if (layoutManager instanceof LinearLayoutManager) {
                LinearLayoutManager linearManager = (LinearLayoutManager) layoutManager;
                //获取最后一个可见view的位置
                int lastItemPosition = linearManager.findLastVisibleItemPosition();
                //获取第一个可见view的位置
                int firstItemPosition = linearManager.findFirstVisibleItemPosition();
                isPosItemVisible(firstItemPosition, lastItemPosition);
            }
        }
    }

    public abstract boolean isPinnedPosition(int position);

    public abstract void isPosItemVisible(int firstItemPosition, int lastItemPosition);

    public abstract void scrollState(RecyclerView recyclerView, int newState);

    public List<M> getDataList() {
        return dataList;
    }

    /**
     * 点击事件事件1
     */
    protected IClickListener iClickListener;

    public interface IClickListener<M> {
        public void onClick(M data, int position);
    }

    public void setIClickListener(IClickListener iClickListener) {
        this.iClickListener = iClickListener;
    }
    /**
     * 点击事件事件1
     */


    /**
     * 点击事件事件2
     */
    protected IViewClickListener iViewClickListener;

    public interface IViewClickListener<M> {
        public void onClick(View view, M data, int position);
    }

    public void setIViewClickListener(IViewClickListener iViewClickListener) {
        this.iViewClickListener = iViewClickListener;
    }

    /**
     * 点击事件事件2
     */
    protected ILongClickListener iLongClickListener;

    public interface ILongClickListener<M> {
        public void onLongClick(M data, int position);
    }

    public void setILongClickListener(ILongClickListener iLongClickListener) {
        this.iLongClickListener = iLongClickListener;
    }


    /**
     * 点击事件事件2
     */
    protected ILongViewClickListener iLongViewClickListener;

    public interface ILongViewClickListener<M> {
        public void onLongViewClick(View view, M data, int position);
    }

    public void setILongViewClickListener(ILongViewClickListener iLongViewClickListener) {
        this.iLongViewClickListener = iLongViewClickListener;
    }

    //点击事件3
    protected IPViewClickListener ipViewClickListener;

    public interface IPViewClickListener<M> {
        void iPViewClick(View parent, View view, M data, int position);
    }

    protected IPViewLongClickListener ipViewLongClickListener;

    public interface IPViewLongClickListener<M> {
        void iPViewLongClick(View parent, View view, M data, int position);
    }

    public void setIpViewClickListener(IPViewClickListener ipViewClickListener) {
        this.ipViewClickListener = ipViewClickListener;
    }

    public void setIpViewLongClickListener(IPViewLongClickListener ipViewLongClickListener) {
        this.ipViewLongClickListener = ipViewLongClickListener;
    }

    public void simulationViewClick(int position) {
        if (iViewClickListener != null) {
            iViewClickListener.onClick(null, getItem(position), position);
        }
    }

    public void simulationViewClick(int position, int viewId) {
        if (iViewClickListener != null) {
            View view = new View(context);
            view.setId(viewId);
            iViewClickListener.onClick(view, getItem(position), position);
        }
    }

    public void simulationViewLongClick(int position) {
        if (iLongViewClickListener != null)
            iLongViewClickListener.onLongViewClick(null, getItem(position), position);
    }
}