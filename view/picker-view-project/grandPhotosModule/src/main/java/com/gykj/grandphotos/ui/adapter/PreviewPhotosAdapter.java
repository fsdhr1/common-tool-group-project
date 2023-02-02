package com.gykj.grandphotos.ui.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.gykj.grandphotos.R;
import com.gykj.grandphotos.config.GrandPhotoHelper;
import com.gykj.grandphotos.models.album.entity.GrandPhotoBean;
import com.gykj.grandphotos.setting.Setting;

import java.io.File;
import java.util.ArrayList;

/**
 * 大图预览界面图片集合的适配器
 */
public class PreviewPhotosAdapter extends RecyclerView.Adapter<PreviewPhotosAdapter.PreviewPhotosViewHolder> {
    private ArrayList<GrandPhotoBean> photoDatas;
    private OnClickListener listener;
    private LayoutInflater inflater;

    // 点击一次左右选中调用一次
    public void setRoateOnce(int selectPosition, boolean mIsLeftRotate) {
        if (photoDatas != null && selectPosition < photoDatas.size()) {
            GrandPhotoBean mPhoto = photoDatas.get(selectPosition);
            int mDegree = mPhoto.getRotateDegree();// 旋转次数
            if (mIsLeftRotate) {
                mDegree--;
            } else {
                mDegree++;
            }
            mPhoto.setRotateDegree(mDegree);
            notifyDataSetChanged();
        }
    }

    public interface OnClickListener {
        void onPhotoClick();

        void onPhotoScaleChanged();
    }

    public PreviewPhotosAdapter(Context cxt, ArrayList<GrandPhotoBean> photoDatas, OnClickListener listener) {
        this.photoDatas = photoDatas;
        this.inflater = LayoutInflater.from(cxt);
        this.listener = listener;
    }

    @NonNull
    @Override
    public PreviewPhotosViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new PreviewPhotosViewHolder(inflater.inflate(R.layout.item_preview_photo_easy_photos, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull final PreviewPhotosViewHolder holder, int position) {
        //
        GrandPhotoBean mPhoto = photoDatas.get(position);
        //
        holder.position = position;
        holder.mPhoto = mPhoto;
        //
        final Uri uri = mPhoto.uri;

        holder.mImageView.setVisibility(View.GONE);

        holder.mImageView.setVisibility(View.VISIBLE);
        // 如果旋转过
        rotateImgDegree(mPhoto, holder.mImageView);
        Setting.imageEngine.loadPhoto(holder.mImageView.getContext(), uri,
                holder.mImageView);

        holder.mImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onPhotoClick();
            }
        });

    }

    // 数据
    private void rotateImgDegree(final GrandPhotoBean mPhoto, final ImageView mIvPhotoView) {
        final int mDegree = mPhoto.getRotateDegree();// 旋转角度
        int mFromDegree = mPhoto.getFromDegree() * 90;
        final int desDegree = mDegree * 90;
        long mAnimateTime = mPhoto.getAnimateTime();
        Animation rotateAnimation = new RotateAnimation(mFromDegree, desDegree, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        rotateAnimation.setFillAfter(true);
        rotateAnimation.setDuration(mAnimateTime);
        rotateAnimation.setRepeatCount(0);
        rotateAnimation.setInterpolator(new LinearInterpolator());
        rotateAnimation.setDetachWallpaper(true);
        rotateAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                int adjust = desDegree / 90;
                mPhoto.setFromDegree(adjust);
                mPhoto.setAnimateTime(500);
            }

            @Override
            public void onAnimationEnd(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        mIvPhotoView.startAnimation(rotateAnimation);
    }

    private Uri getUri(Context context, String path, Intent intent) {
        File file = new File(path);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            return FileProvider.getUriForFile(context, GrandPhotoHelper.getFileProviderPath(), file);
        } else {
            return Uri.fromFile(file);
        }
    }

    @Override
    public int getItemCount() {
        return photoDatas.size();
    }

    public class PreviewPhotosViewHolder extends RecyclerView.ViewHolder {
        int position;
        GrandPhotoBean mPhoto;
        ImageView mImageView;

        PreviewPhotosViewHolder(View itemView) {
            super(itemView);
            mImageView = itemView.findViewById(R.id.iv_photo_view);
        }
    }
}
