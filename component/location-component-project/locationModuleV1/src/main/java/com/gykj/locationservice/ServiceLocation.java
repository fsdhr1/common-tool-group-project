package com.gykj.locationservice;

import android.location.Location;

/**
 * Created by zy on 2018/4/1.
 */

public class ServiceLocation {
    private ESignal eSignal;// 信号、强中弱
    private Location location;// 位置
    private Integer satelliteCount;// 卫星数
    private Integer validSatelliteCount;// 有效的卫星数，卫星信噪比 > 30

    private Double latitude;// 纬度
    private Double longitude;// 经度

    public ESignal geteSignal() {
        return eSignal;
    }

    public void seteSignal(ESignal eSignal) {
        this.eSignal = eSignal;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public Integer getSatelliteCount() {
        return satelliteCount;
    }

    public void setSatelliteCount(Integer satelliteCount) {
        this.satelliteCount = satelliteCount;
    }

    public Integer getValidSatelliteCount() {
        return validSatelliteCount;
    }

    public void setValidSatelliteCount(Integer validSatelliteCount) {
        this.validSatelliteCount = validSatelliteCount;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public enum ESignal {
        HIGH,
        MIDDLE,
        LOW;
    }
}
