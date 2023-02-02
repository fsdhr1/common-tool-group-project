package com.gykj.location.beans;

/**
 * Created by ZhaiJiaChang.
 * <p>
 * Date: 2021/12/7
 */
public class LocationBean {

    private int LOCATION_GPS = 1;
    private int LOCATION_NET = 2;

    private double latitude;// 纬度
    private double longitude;// 经度
    private int locationType;// 1.GPS 、2.NETWORK


    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double mLatitude) {
        latitude = mLatitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double mLongitude) {
        longitude = mLongitude;
    }

    public int getLocationType() {
        return locationType;
    }

    public void setLocationType(int mLocationType) {
        locationType = mLocationType;
    }
}
