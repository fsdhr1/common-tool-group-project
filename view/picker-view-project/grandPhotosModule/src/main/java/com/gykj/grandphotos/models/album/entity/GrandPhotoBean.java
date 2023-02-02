package com.gykj.grandphotos.models.album.entity;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

/**
 * 图片item实体类
 */

public class GrandPhotoBean implements Parcelable {
    private static final String TAG = "Photo";
    public Uri uri;//图片Uri
    public String name;//图片名称
    public String path;//图片全路径
    public String type;//图片类型
    public int selPos = 0;// 选中图片的位置
    public int width;//图片宽度
    public int height;//图片高度
    public int orientation;//图片旋转角度
    public long size;//图片文件大小，单位：Bytes
    public long time;//图片拍摄的时间戳,单位：毫秒
    public boolean isClick = false;// 是否被点击
    public boolean selected;//是否被选中,内部使用,无需关心
    public boolean selectedOriginal;//用户选择时是否选择了原图选项\
    // 自定义 是否被旋转过
//    public boolean isRotate = false;
    // 自定义 旋转角度；值为123456...
    private int rotateDegree = 0;//
    // 自定义 记录旋转之前的角度
    private int fromDegree = 0;
    // 自定义 底部预览框是否被选中
    // 自定义 动画时长
    private long animateTime = 500;
    private boolean botSelect = false;

    public GrandPhotoBean(String name, Uri uri, String path, long time, int width, int height, int orientation, long size, String type) {
        this.name = name;
        this.uri = uri;
        this.path = path;
        this.time = time;
        this.width = width;
        this.height = height;
        this.orientation = orientation;
        this.type = type;
        this.size = size;
        this.selected = false;
        this.selectedOriginal = false;
    }

    public long getAnimateTime() {
        return animateTime;
    }

    public void setAnimateTime(long mAnimateTime) {
        animateTime = mAnimateTime;
    }

    public int getRotateDegree() {
        return rotateDegree ;
    }

    public boolean isBotSelect() {
        return botSelect;
    }

    public void setBotSelect(boolean mBotSelect) {
        botSelect = mBotSelect;
    }

    public void setFromDegree(int mFromDegree) {
        fromDegree = mFromDegree;
    }

    public int getFromDegree() {
        return fromDegree ;
    }

    public void initFromDegree() {
        fromDegree = rotateDegree;
    }

    public void setRotateDegree(int mRotateDegree) {
        rotateDegree = mRotateDegree;
    }

    @Override
    public boolean equals(Object o) {
        try {
            GrandPhotoBean other = (GrandPhotoBean) o;
            return this.path.equalsIgnoreCase(other.path);
        } catch (ClassCastException e) {
            Log.e(TAG, "equals: " + Log.getStackTraceString(e));
        }
        return super.equals(o);
    }

    @Override
    public String toString() {
        return "Photo{" +
                "name='" + name + '\'' +
                ", uri='" + uri.toString() + '\'' +
                ", path='" + path + '\'' +
                ", time=" + time + '\'' +
                ", minWidth=" + width + '\'' +
                ", minHeight=" + height +
                ", orientation=" + orientation +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.uri, flags);
        dest.writeString(this.name);
        dest.writeString(this.path);
        dest.writeString(this.type);
        dest.writeInt(this.width);
        dest.writeInt(this.height);
        dest.writeInt(this.orientation);
        dest.writeLong(this.size);
        dest.writeLong(this.time);
        dest.writeByte(this.selected ? (byte) 1 : (byte) 0);
        dest.writeByte(this.selectedOriginal ? (byte) 1 : (byte) 0);
    }

    protected GrandPhotoBean(Parcel in) {
        this.uri = in.readParcelable(Uri.class.getClassLoader());
        this.name = in.readString();
        this.path = in.readString();
        this.type = in.readString();
        this.width = in.readInt();
        this.height = in.readInt();
        this.orientation = in.readInt();
        this.size = in.readLong();
        this.time = in.readLong();
        this.selected = in.readByte() != 0;
        this.selectedOriginal = in.readByte() != 0;
    }

    public static final Creator<GrandPhotoBean> CREATOR = new Creator<GrandPhotoBean>() {
        @Override
        public GrandPhotoBean createFromParcel(Parcel source) {
            return new GrandPhotoBean(source);
        }

        @Override
        public GrandPhotoBean[] newArray(int size) {
            return new GrandPhotoBean[size];
        }
    };
}
