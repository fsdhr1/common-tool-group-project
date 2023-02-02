package com.gykj.grandphotos.models.album;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.text.TextUtils;

import androidx.core.content.PermissionChecker;

import com.gykj.grandphotos.R;
import com.gykj.grandphotos.models.album.entity.Album;
import com.gykj.grandphotos.models.album.entity.AlbumItem;
import com.gykj.grandphotos.models.album.entity.GrandPhotoBean;
import com.gykj.grandphotos.result.Result;
import com.gykj.grandphotos.setting.Setting;
import com.gykj.grandphotos.utils.String.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 专辑模型
 * 修改内容：将AlbumModel的实例化与数据查询分开
 */
public class AlbumModel {
    private static final String TAG = "AlbumModel";
    public static AlbumModel instance;
    public Album album;
    private String[] projections;

    private AlbumModel() {
        album = new Album();
    }

    public static AlbumModel getInstance() {
        if (null == instance) {
            synchronized (AlbumModel.class) {
                if (null == instance) {
                    instance = new AlbumModel();
                }
            }
        }
        return instance;
    }

    /**
     * 专辑查询
     *
     * @param context  调用查询方法的context
     * @param callBack 查询完成后的回调
     */
    public volatile boolean canRun = true;

    public void query(Context context, final CallBack callBack) {
        final Context appCxt = context.getApplicationContext();
        if (PermissionChecker.checkSelfPermission(context,
                Manifest.permission.READ_EXTERNAL_STORAGE) != PermissionChecker.PERMISSION_GRANTED) {
            if (null != callBack) callBack.onAlbumWorkedCallBack();
            return;
        }
        canRun = true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                initAlbum(appCxt);
                if (null != callBack) callBack.onAlbumWorkedCallBack();
            }
        }).start();
    }

    public void stopQuery() {
        canRun = false;
    }

    private synchronized void initAlbum(Context context) {
        album.clear();
//        long now = System.currentTimeMillis();
        if (Setting.selectedPhotos.size() > Setting.count) {
            throw new RuntimeException("AlbumBuilder: 默认勾选的图片张数不能大于设置的选择数！" + "|默认勾选图片张数：" + Setting.selectedPhotos.size() + "|设置的选择数：" + Setting.count);
        }
        boolean canReadWidth =
                android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN;
        boolean isQ = android.os.Build.VERSION.SDK_INT == Build.VERSION_CODES.Q;
        final String sortOrder = MediaStore.Files.FileColumns.DATE_MODIFIED + " DESC";

        Uri contentUri;
        String selection = null;
        String[] selectionAllArgs = null;
        contentUri = MediaStore.Files.getContentUri("external");
        selection =
                "(" + MediaStore.Files.FileColumns.MEDIA_TYPE + "=?" + " OR " + MediaStore.Files.FileColumns.MEDIA_TYPE + "=?)";
        selectionAllArgs =
                new String[]{String.valueOf(MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE)};

        ContentResolver contentResolver = context.getContentResolver();


        List<String> projectionList = new ArrayList<String>();
        projectionList.add(MediaStore.Files.FileColumns._ID);
        if (isQ) {
            projectionList.add(MediaStore.MediaColumns.RELATIVE_PATH);
        } else {
            projectionList.add(MediaStore.MediaColumns.DATA);
        }
        projectionList.add(MediaStore.MediaColumns.DISPLAY_NAME);
        projectionList.add(MediaStore.MediaColumns.DATE_MODIFIED);
        projectionList.add(MediaStore.MediaColumns.MIME_TYPE);
        projectionList.add(MediaStore.MediaColumns.SIZE);
        projectionList.add(MediaStore.MediaColumns.BUCKET_DISPLAY_NAME);
        projectionList.add(MediaStore.MediaColumns.DATA);

        if (canReadWidth) {
            projectionList.add(MediaStore.MediaColumns.WIDTH);
            projectionList.add(MediaStore.MediaColumns.HEIGHT);
            projectionList.add(MediaStore.MediaColumns.ORIENTATION);
        }

        projections = projectionList.toArray(new String[0]);

        Cursor cursor = contentResolver.query(contentUri, projections, selection,
                selectionAllArgs, sortOrder);
        if (cursor == null) {
//            Log.d(TAG, "call: " + "Empty photos");
        } else if (cursor.moveToFirst()) {
            String albumItem_all_name = getAllAlbumName(context);

            int albumNameCol = cursor.getColumnIndex(MediaStore.MediaColumns.BUCKET_DISPLAY_NAME);
            int WidthCol = cursor.getColumnIndex(MediaStore.MediaColumns.WIDTH);
            int HeightCol = cursor.getColumnIndex(MediaStore.MediaColumns.HEIGHT);
            int orientationCol = cursor.getColumnIndex(MediaStore.MediaColumns.ORIENTATION);
            do {
                long id = cursor.getLong(0);
                String path = cursor.getString(1);
                String name = cursor.getString(2);
                long dateTime = cursor.getLong(3);
                String type = cursor.getString(4);
                long size = cursor.getLong(5);
                String sdPath = cursor.getString(7);


                if (TextUtils.isEmpty(path) || TextUtils.isEmpty(type)) {
                    continue;
                }

                int width = 0;
                int height = 0;
                int orientation = 0;
                if (orientationCol != -1) {
                    orientation = cursor.getInt(orientationCol);
                }
                if (canReadWidth) {
                    width = cursor.getInt(WidthCol);
                    height = cursor.getInt(HeightCol);
                }
                if (width == 0 || height == 0) {
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inJustDecodeBounds = true;
                    BitmapFactory.decodeFile(path, options);
                    width = options.outWidth;
                    height = options.outHeight;
                }

                if (orientation == 90 || orientation == 270) {
                    int temp = width;
                    width = height;
                    height = temp;
                }

                Uri uri = ContentUris.withAppendedId(MediaStore.Images.Media.getContentUri("external"), id);
                //某些机型，特定情况下三方应用或用户操作删除媒体文件时，没有通知媒体库，导致媒体库表中还有其数据，但真实文件已经不存在
                File file = new File(path);
                if (!file.isFile()) {
                    path = sdPath;
                    file = new File(path);
                    if (!file.isFile()) {
                        continue;
                    }
                }

                GrandPhotoBean imageItem = new GrandPhotoBean(name, uri, path, dateTime, width, height, orientation
                        , size, type);
                if (!Setting.selectedPhotos.isEmpty()) {
                    int selectSize = Setting.selectedPhotos.size();
                    for (int i = 0; i < selectSize; i++) {
                        GrandPhotoBean selectedPhoto = Setting.selectedPhotos.get(i);
                        if (path.equals(selectedPhoto.path)) {
                            imageItem.selectedOriginal = Setting.selectedOriginal;
                            Result.addPhoto(imageItem);
                        }
                    }
                }

                // 初始化“全部”专辑
                if (album.isEmpty()) {
                    // 用第一个图片作为专辑的封面
                    album.addAlbumItem(albumItem_all_name, "", path, uri);
                }
                // 把图片全部放进“全部”专辑
                album.getAlbumItem(albumItem_all_name).addImageItem(imageItem);
                // 添加当前图片的专辑到专辑模型实体中
                String albumName;
                String folderPath;
                if (albumNameCol > 0) {
                    albumName = cursor.getString(albumNameCol);
                    folderPath = albumName;
                } else {
                    File parentFile = new File(path).getParentFile();
                    if (null == parentFile) {
                        continue;
                    }
                    folderPath = parentFile.getAbsolutePath();
                    albumName = StringUtils.getLastPathSegment(folderPath);
                }
                album.addAlbumItem(albumName, folderPath, path, uri);
                album.getAlbumItem(albumName).addImageItem(imageItem);
            } while (cursor.moveToNext() && canRun);
            cursor.close();
        }
    }

    /**
     * 获取全部专辑名
     *
     * @return 专辑名
     */
    public String getAllAlbumName(Context context) {
        String albumItem_all_name =
                context.getString(R.string.selector_folder_all_video_photo_easy_photos);
        return albumItem_all_name;
    }

    /**
     * 获取当前专辑项目的图片集
     *
     * @return 当前专辑项目的图片集
     */
    public ArrayList<GrandPhotoBean> getCurrAlbumItemPhotos(int currAlbumItemIndex) {
        return album.getAlbumItem(currAlbumItemIndex).photos;
    }

    /**
     * 获取专辑项目集
     *
     * @return 专辑项目集
     */
    public ArrayList<AlbumItem> getAlbumItems() {
        return album.albumItems;
    }

    public interface CallBack {
        void onAlbumWorkedCallBack();
    }


    /**
     * 获取projections
     */
    public String[] getProjections() {
        if (null == projections || projections.length == 0) {
            boolean isQ = android.os.Build.VERSION.SDK_INT == Build.VERSION_CODES.Q;
            final String mData;
            if (isQ)
                mData = MediaStore.MediaColumns.RELATIVE_PATH;
            else
                mData = MediaStore.MediaColumns.DATA;
            projections = new String[]{
                    MediaStore.Files.FileColumns._ID,
                    mData
                    , MediaStore.MediaColumns.DISPLAY_NAME,
                    MediaStore.MediaColumns.DATE_MODIFIED,
                    MediaStore.MediaColumns.MIME_TYPE, MediaStore.MediaColumns.SIZE,
                    MediaStore.MediaColumns.BUCKET_DISPLAY_NAME,
                    MediaStore.MediaColumns.WIDTH, MediaStore.MediaColumns.HEIGHT,
                    MediaStore.MediaColumns.ORIENTATION};
        }
        return projections;
    }

}
