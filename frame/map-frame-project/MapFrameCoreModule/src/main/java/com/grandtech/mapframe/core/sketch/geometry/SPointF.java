package com.grandtech.mapframe.core.sketch.geometry;

import android.graphics.PointF;

/**
 * Created by zy on 2018/12/27.
 */

public class SPointF extends PointF {


    private StringBuilder geoIp;

    public SPointF(float x, float y) {
        super(x, y);
    }

    public SPointF(PointF pointF) {
        super(pointF.x, pointF.y);
    }

    public SPointF(float x, float y, int ip) {
        super(x, y);
        this.geoIp = new StringBuilder(ip);
    }

    public SPointF(float x, float y, int... ip) {
        super(x, y);
        this.geoIp = new StringBuilder();
        for (int i = 0; i < ip.length; i++) {
            this.geoIp.append(ip[i]);
            if (i != ip.length - 1) {
                this.geoIp.append('.');
            }
        }
    }

    public SPointF(PointF pointF, int ip) {
        super(pointF.x, pointF.y);
        this.geoIp = new StringBuilder(ip);
    }

    public SPointF(PointF pointF, int... ip) {
        super(pointF.x, pointF.y);
        this.geoIp = new StringBuilder();
        for (int i = 0; i < ip.length; i++) {
            this.geoIp.append(ip[i]);
            if (i != ip.length - 1) {
                this.geoIp.append('.');
            }
        }
    }

    public SPointF addIp(int ipItem) {
        if (geoIp == null) {
            geoIp = new StringBuilder();
        }
        if (geoIp.length() == 0) {
            geoIp.append(ipItem);
        } else {
            geoIp.append(".").append(ipItem);
        }
        return this;
    }

    public String getGeoIp() {
        if (geoIp == null) {
            return null;
        }
        return geoIp.toString();
    }

}
