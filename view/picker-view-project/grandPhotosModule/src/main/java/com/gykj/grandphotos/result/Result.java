package com.gykj.grandphotos.result;

import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;

import com.gykj.grandphotos.models.album.entity.GrandPhotoBean;
import com.gykj.grandphotos.setting.Setting;

import java.util.ArrayList;

/**
 * 存储的返回图片集
 */

public class Result {
    // 相册选中的
    public static ArrayList<GrandPhotoBean> photos = new ArrayList<>();
    public static final int ADD_SUCCESS = 0;
    public static final int PICTURE_OUT = -1;
    public static final int VIDEO_OUT = -2;
    public static final int SINGLE_TYPE = -3;

    /**
     * @return 0：添加成功 -2：超过视频选择数 -1：超过图片选择数
     */
    public static int addPhoto(GrandPhotoBean photo) {
        if (photos.isEmpty()) {
            photo.selected = true;
            photos.add(photo);
            return ADD_SUCCESS;
        }
//        if (Setting.complexSelector) {
//            if (Setting.complexSingleType) {
//                if (photos.get(0).type.contains(Type.VIDEO)) {
//                    if (!photo.type.contains(Type.VIDEO)) {
//                        return SINGLE_TYPE;
//                    }
//                }
//                if (!photos.get(0).type.contains(Type.VIDEO)) {
//                    if (photo.type.contains(Type.VIDEO)) {
//                        return SINGLE_TYPE;
//                    }
//                }
//
//            }
//            int number = getVideoNumber();
//            if (photo.type.contains(Type.VIDEO) && number >= Setting.complexVideoCount) {
//                return VIDEO_OUT;
//            }
//            number = photos.size() - number;
//            if ((!photo.type.contains(Type.VIDEO)) && number >= Setting.complexPictureCount) {
//                return PICTURE_OUT;
//            }
//        }
        photo.selected = true;
        photos.add(photo);
        return ADD_SUCCESS;
    }

    public static void initPhotos() {
        if (photos != null && !photos.isEmpty()) {
            for (GrandPhotoBean mPhoto : photos) {
                mPhoto.setFromDegree(0);
                mPhoto.setRotateDegree(0);
//                mPhoto.isRotate = false;
            }
        }
    }

    public static void removePhoto(GrandPhotoBean photo) {
        photo.selected = false;
        photos.remove(photo);
    }

    public static void removePhoto(int photoIndex) {
        removePhoto(photos.get(photoIndex));
    }

    public static void removeAll() {
        int size = photos.size();
        for (int i = 0; i < size; i++) {
            removePhoto(0);
        }
    }


    public static void processOriginal() {
        boolean isIceApi = Build.VERSION.SDK_INT == Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1;
        if (Setting.showOriginalMenu) {
            if (Setting.originalMenuUsable) {
                for (GrandPhotoBean photo : photos) {
                    photo.selectedOriginal = Setting.selectedOriginal;
                    if (isIceApi && photo.width == 0) {
                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inJustDecodeBounds = true;
                        BitmapFactory.decodeFile(photo.path, options);
                        photo.width = options.outWidth;
                        photo.height = options.outHeight;
                    }
                }
            }
        }
    }

    public static void clear() {
        photos.clear();
    }

    public static boolean isEmpty() {
        return photos.isEmpty();
    }

    public static int count() {
        return photos.size();
    }

    /**
     * 获取选择器应该显示的数字
     *
     * @param photo 当前图片
     * @return 选择器应该显示的数字
     */
    public static String getSelectorNumber(GrandPhotoBean photo) {
        return String.valueOf(photos.indexOf(photo) + 1);
    }

    public static GrandPhotoBean getPhoto(int position) {
        return photos.get(position);
    }

    public static String getPhotoPath(int position) {
        return photos.get(position).path;
    }

    public static Uri getPhotoUri(int position) {
        return photos.get(position).uri;
    }

    public static String getPhotoType(int position) {
        return photos.get(position).type;
    }

    public static void setListNoClick() {
        for (GrandPhotoBean mPhoto : photos) {
            mPhoto.isClick = false;
        }
    }
}
