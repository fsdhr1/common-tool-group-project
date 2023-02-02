package com.gykj.grandphotos.ui.adapter;

import android.content.Context;

import androidx.recyclerview.widget.RecyclerView;

import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.gykj.grandphotos.R;
import com.gykj.grandphotos.result.Result;
import com.gykj.grandphotos.setting.Setting;
import com.gykj.grandphotos.models.album.entity.GrandPhotoBean;
import com.gykj.grandphotos.ui.widget.PressedImageView;

import java.util.List;

/**
 * 预览所有选中图片集合的适配器
 */

public class PreviewPhotosFragmentAdapter extends RecyclerView.Adapter<PreviewPhotosFragmentAdapter.PreviewPhotoVH> {
    private LayoutInflater inflater;
    private OnClickListener listener;
    private List<GrandPhotoBean> mPhotoList;

    public PreviewPhotosFragmentAdapter(Context context, OnClickListener listener) {
        this.inflater = LayoutInflater.from(context);
        this.listener = listener;
        mPhotoList = Result.photos;
    }


    @Override
    public PreviewPhotoVH onCreateViewHolder(ViewGroup parent, int viewType) {
        return new PreviewPhotoVH(inflater.inflate(R.layout.item_preview_selected_photos_easy_photos, parent, false));
    }

    @Override
    public void onBindViewHolder(PreviewPhotoVH holder, int position) {
        final int p = position;
        GrandPhotoBean mPhoto = mPhotoList.get(position);
        Uri uri = mPhoto.uri;

        Setting.imageEngine.loadPhoto(holder.ivPhoto.getContext(), uri, holder.ivPhoto);
        holder.tvType.setVisibility(View.GONE);

        if (mPhoto.isBotSelect()) {
            holder.frame.setVisibility(View.VISIBLE);
        } else {
            holder.frame.setVisibility(View.GONE);
        }
        // 旋转前和旋转后不相等才旋转
//        int mFromDegree = mPhoto.getFromDegree();
        int mDestDegree = mPhoto.getRotateDegree();
        final int desDegree = mPhoto.getRotateDegree() * 90;// 结束旋转角度
        holder.ivPhoto.setRotation(desDegree);
//        if (mFromDegree != mDestDegree) {
        // 设置图片的翻转
//            setThumbImgRotate(holder, mPhoto);
//        }
        //
        holder.ivPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onPhotoClick(p);
            }
        });
    }

    private void setThumbImgRotate(final PreviewPhotoVH mHolder, final GrandPhotoBean mPhoto) {
        final int fromDegree = mPhoto.getFromDegree() * 90;// 开始旋转角度


//        Animation rotateAnimation = new RotateAnimation(fromDegree, desDegree, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
//        rotateAnimation.setFillAfter(true);
//        rotateAnimation.setDuration(0);
//        rotateAnimation.setRepeatCount(0);
//        rotateAnimation.setInterpolator(new LinearInterpolator());
//        rotateAnimation.setDetachWallpaper(true);
//        rotateAnimation.setAnimationListener(new Animation.AnimationListener() {
//            @Override
//            public void onAnimationStart(Animation animation) {
//                int adjust = desDegree / 90;
//                mPhoto.setFromDegree(adjust);
//                int mFromDegree = mPhoto.getFromDegree();
//                Log.e("setThumbImgRotate: ", " --- anim mFromDegree " + mFromDegree + " --- " + desDegree);
//            }
//
//            @Override
//            public void onAnimationEnd(Animation animation) {
//
//            }
//
//            @Override
//            public void onAnimationRepeat(Animation animation) {
//
//            }
//        });
//        mHolder.ivPhoto.startAnimation(rotateAnimation);


    }

    @Override
    public int getItemCount() {
        return Result.count();
    }

    // 设置底部是否被选中
    public void setChecked(int position) {
        if (mPhotoList != null && mPhotoList.size() > 0) {
            for (int i = 0; i < mPhotoList.size(); i++) {
                GrandPhotoBean mPhoto = mPhotoList.get(i);
                mPhoto.setBotSelect(position == i);
            }
            notifyDataSetChanged();
        }
    }

    class PreviewPhotoVH extends RecyclerView.ViewHolder {
        PressedImageView ivPhoto;
        View frame;
        TextView tvType;

        public PreviewPhotoVH(View itemView) {
            super(itemView);
            ivPhoto = (PressedImageView) itemView.findViewById(R.id.iv_photo);
            frame = itemView.findViewById(R.id.v_selector);
            tvType = (TextView) itemView.findViewById(R.id.tv_type);
        }
    }

    public interface OnClickListener {
        void onPhotoClick(int position);
    }
}
