package com.grandtech.mapframe.core.util;

import com.mapbox.mapboxsdk.geometry.LatLng;

import java.util.List;

/**
 *
 * @author zy
 * @date 2018/1/5
 */

public final class CommonGeoUnion {

    /**
     * 计算两个点的中点
     *
     * @param _x1
     * @param _y1
     * @param _x2
     * @param _y2
     * @return
     */
    public static double[] get2Point2MidPoint(double _x1, double _y1, double _x2, double _y2) {
        double[] res = new double[2];
        res[0] = (_x1 + _x2) / 2;
        res[1] = (_y1 + _y2) / 2;
        return res;
    }

    /**
     * 计算两个点的距离是否超过阈值
     *
     * @param _x1
     * @param _y1
     * @param _x2
     * @param _y2
     * @param _dis
     * @return
     */
    public static boolean isNearPoint(double _x1, double _y1, double _x2, double _y2, double _dis) {
        if ((_x1 - _x2) * (_x1 - _x2) + (_y1 - _y2) * (_y1 - _y2) > _dis * _dis) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * 计算第三个点里前两个点 哪个近
     *
     * @param _x1
     * @param _y1
     * @param _x2
     * @param _y2
     * @param _cx
     * @param _cy
     * @return
     */
    public static int nearPoint(double _x1, double _y1, double _x2, double _y2, double _cx, double _cy) {
        double dis1 = (_x1 - _cx) * (_x1 - _cx) + (_y1 - _cy) * (_y1 - _cy);
        double dis2 = (_x2 - _cx) * (_x2 - _cx) + (_y2 - _cy) * (_y2 - _cy);
        if (dis1 <= dis2) {
            return 1;
        } else {
            return 2;
        }
    }

    public static double polygonArea(List<double[]> points) {
        if (points == null || points.size() < 3) {
            return 0.0;
        }
        int n = points.size();
        double s = points.get(0)[1] * (points.get(n - 1)[0] - points.get(1)[0]);
        points.add(n, points.get(0));
        for (int i = 1; i < n; ++i) {
            s += points.get(i)[1] * (points.get(i - 1)[0] - points.get(i + 1)[0]);
        }
        return s * 0.5;
    }

    public static double polygonLength(List<double[]> points) {
        if (points == null || points.size() < 2) {
            return 0.0;
        }
        int n = points.size();
        points.add(n, points.get(0));
        double s = Math.sqrt((points.get(0)[0] - points.get(1)[0]) * (points.get(0)[0] - points.get(1)[0]) + (points.get(0)[1] - points.get(1)[1]) * (points.get(0)[1] - points.get(1)[1]));
        for (int i = 1; i < n; i++) {
            s += Math.sqrt((points.get(i)[0] - points.get(i + 1)[0]) * (points.get(i)[0] - points.get(i + 1)[0]) + (points.get(i)[1] - points.get(i + 1)[1]) * (points.get(i)[1] - points.get(i + 1)[1]));
        }
        return s;
    }

    public static double polygonLast2PointLength(List<double[]> points) {
        if (points == null || points.size() < 2) {
            return 0.0;
        }
        int len = points.size();
        double[] d1, d2;
        if (points.size() == 2) {
            d1 = points.get(0);
            d2 = points.get(1);
        } else {
            d1 = points.get(points.size() - 2);
            d2 = points.get(points.size() - 3);
        }
        /*int n = points.size();
        points.add(n, points.get(0));
        double s = Math.sqrt((points.get(0)[0] - points.get(1)[0]) * (points.get(0)[0] - points.get(1)[0]) + (points.get(0)[1] - points.get(1)[1]) * (points.get(0)[1] - points.get(1)[1]));
        for (int i = 1; i < n; i++) {
            s += Math.sqrt((points.get(i)[0] - points.get(i + 1)[0]) * (points.get(i)[0] - points.get(i + 1)[0]) + (points.get(i)[1] - points.get(i + 1)[1]) * (points.get(i)[1] - points.get(i + 1)[1]));
        }*/
        return Math.sqrt((d1[0] - d2[0]) * (d1[0] - d2[0]) + (d1[1] - d2[1]) * (d1[1] - d2[1]));
    }

    public static double _2PointDis(LatLng[] lens) {
        if (lens == null || lens.length != 2) {
            return 0.0;
        }
        LatLng p1, p2;
        p1 = lens[0];
        p2 = lens[1];
        return Math.sqrt((p1.getLongitude() - p2.getLongitude()) * (p1.getLongitude() - p2.getLongitude()) + (p1.getLatitude() - p2.getLatitude()) * (p1.getLatitude() - p2.getLatitude()));
    }

    public static String polygonLeftOrRight(List<double[]> points) {
        double area = polygonArea(points);
        if (area > 0) {
            return "N";
        } else {
            return "S";
        }
    }
}
