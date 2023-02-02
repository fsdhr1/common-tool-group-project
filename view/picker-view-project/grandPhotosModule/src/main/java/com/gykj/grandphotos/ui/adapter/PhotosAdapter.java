package com.gykj.grandphotos.ui.adapter;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.gykj.grandphotos.R;
import com.gykj.grandphotos.models.album.entity.GrandPhotoBean;
import com.gykj.grandphotos.result.Result;
import com.gykj.grandphotos.setting.Setting;
import com.gykj.grandphotos.ui.widget.PressedImageView;

import java.util.ArrayList;

/**
 * 专辑相册适配器
 */
public class PhotosAdapter extends RecyclerView.Adapter {
    private static final int TYPE_CAMERA = 1;
    private static final int TYPE_ALBUM_ITEMS = 2;

    private ArrayList<Object> dataList;
    private LayoutInflater mInflater;
    private OnClickListener listener;
    private boolean unable, isSingle;
    private int singlePosition;

    private boolean clearAd = false;


    public PhotosAdapter(Context cxt, ArrayList<Object> dataList, OnClickListener listener) {
        this.dataList = dataList;
        this.listener = listener;
        this.mInflater = LayoutInflater.from(cxt);
        this.unable = Result.count() == Setting.count;
        this.isSingle = Setting.count == 1;
    }

    public void change() {
        unable = Result.count() == Setting.count;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType) {
            case TYPE_CAMERA:
                return new CameraViewHolder(mInflater.inflate(R.layout.item_camera_easy_photos, parent, false));
            default:
                return new PhotoViewHolder(mInflater.inflate(R.layout.item_rv_photos_easy_photos, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, int position) {
        final int p = position;
        if (holder instanceof PhotoViewHolder) {
            int mDataPosition = position - 1;// 0是相机，1是真正的item
            final GrandPhotoBean item = (GrandPhotoBean) dataList.get(mDataPosition);
            if (item == null) return;
            updateSelector(((PhotoViewHolder) holder).tvSelector, item.selected, item, p);
            Uri uri = item.uri;
            Setting.imageEngine.loadPhoto(((PhotoViewHolder) holder).ivPhoto.getContext(), uri, ((PhotoViewHolder) holder).ivPhoto);
            ((PhotoViewHolder) holder).vSelector.setVisibility(View.VISIBLE);
            ((PhotoViewHolder) holder).tvSelector.setVisibility(View.VISIBLE);
            ((PhotoViewHolder) holder).ivPhoto.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int realPosition = p;
                    realPosition--;
                    listener.onPhotoClick(item, p, realPosition);
                }
            });

            ((PhotoViewHolder) holder).vSelector.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (isSingle) {
                        singleSelector(item, p);
                        return;
                    }
                    if (unable) {
                        if (item.selected) {
                            item.selPos = 0;
                            item.isClick = false;
                            Result.removePhoto(item);
                            if (unable) {
                                unable = false;
                            }
                            listener.onSelectorChanged();
                            notifyDataSetChanged();
                            return;
                        }
                        listener.onSelectorOutOfMax(null);
                        return;
                    }
                    item.selected = !item.selected;
                    if (item.selected) {
                        int res = Result.addPhoto(item);
                        item.selPos = Result.count();
                        if (res != 0) {
                            listener.onSelectorOutOfMax(res);
                            item.selected = false;
                            return;
                        }
                        ((PhotoViewHolder) holder).tvSelector.setBackgroundResource(R.drawable.bg_select_true_easy_photos);
                        ((PhotoViewHolder) holder).tvSelector.setText(String.valueOf(Result.count()));
                        if (Result.count() == Setting.count) {
                            unable = true;
                            notifyDataSetChanged();
                        }
                    } else {
                        item.selPos = 0;
                        item.isClick = false;
                        Result.removePhoto(item);
                        if (unable) {
                            unable = false;
                        }
                        notifyDataSetChanged();
                    }
                    listener.onSelectorChanged();
                }
            });
            return;
        } else if (holder instanceof CameraViewHolder) {
            ((CameraViewHolder) holder).flCamera.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onCameraClick();
                }
            });
        }
    }

    public void clearAd() {
        clearAd = true;
        notifyDataSetChanged();
    }

    private void singleSelector(GrandPhotoBean photo, int position) {
        if (!Result.isEmpty()) {
            if (Result.getPhotoPath(0).equals(photo.path)) {
                Result.removePhoto(photo);
            } else {
                Result.removePhoto(0);
                Result.addPhoto(photo);
                notifyItemChanged(singlePosition);
            }
        } else {
            Result.addPhoto(photo);
        }
        notifyItemChanged(position);
        listener.onSelectorChanged();
    }

    private void updateSelector(TextView tvSelector, boolean selected, GrandPhotoBean photo, int position) {
        if (selected) {
            String number = Result.getSelectorNumber(photo);
            if (number.equals("0")) {
                tvSelector.setBackgroundResource(R.drawable.bg_select_false_easy_photos);
                tvSelector.setText(null);
                return;
            }
            tvSelector.setText(number);
            tvSelector.setBackgroundResource(R.drawable.bg_select_true_easy_photos);
            if (isSingle) {
                singlePosition = position;
                tvSelector.setText("1");
            }
        } else {
            if (unable) {
                tvSelector.setBackgroundResource(R.drawable.bg_select_false_unable_easy_photos);
            } else {
                tvSelector.setBackgroundResource(R.drawable.bg_select_false_easy_photos);
            }
            tvSelector.setText(null);
        }
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (0 == position) {
            return TYPE_CAMERA;
        }
        return TYPE_ALBUM_ITEMS;
    }

    public interface OnClickListener {
        void onCameraClick();

        void onPhotoClick(GrandPhotoBean mItem, int position, int realPosition);

        void onSelectorOutOfMax(@Nullable Integer result);

        void onSelectorChanged();
    }

    private class CameraViewHolder extends RecyclerView.ViewHolder {
        final FrameLayout flCamera;

        CameraViewHolder(View itemView) {
            super(itemView);
            this.flCamera = itemView.findViewById(R.id.fl_camera);
        }
    }

    public class PhotoViewHolder extends RecyclerView.ViewHolder {
        final PressedImageView ivPhoto;
        final TextView tvSelector;
        final View vSelector;

        PhotoViewHolder(View itemView) {
            super(itemView);
            this.ivPhoto = itemView.findViewById(R.id.iv_photo);
            this.tvSelector = itemView.findViewById(R.id.tv_selector);
            this.vSelector = itemView.findViewById(R.id.v_selector);
        }
    }
}
