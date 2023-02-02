package com.gykj.grandphotos.models.album.entity;

import android.net.Uri;

import java.util.ArrayList;

/**
 * 专辑项目实体类
 */

public class AlbumItem {
    public String name;
    public String folderPath;
    public String coverImagePath;
    public Uri coverImageUri;
    public ArrayList<GrandPhotoBean> photos;

    AlbumItem(String name, String folderPath, String coverImagePath, Uri coverImageUri) {
        this.name = name;
        this.folderPath = folderPath;
        this.coverImagePath = coverImagePath;
        this.coverImageUri = coverImageUri;
        this.photos = new ArrayList<>();
    }

    public void addImageItem(GrandPhotoBean imageItem) {
        this.photos.add(imageItem);
    }

    public void addImageItem(int index, GrandPhotoBean imageItem) {
        this.photos.add(index, imageItem);
    }
}
