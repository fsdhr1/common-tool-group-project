package com.grandtech.mapframe.core.util;

import android.graphics.PointF;

import com.grandtech.mapframe.core.sketch.geometry.LineSegment;

import java.util.List;

/**
 * Created by zy on 2018/12/26.
 */

public class GeoRelationShip {
    static double INFINITY = 1e10;
    static double ESP = 1e-5;

    // 计算叉乘 |P0P1| × |P0P2|
    public static double multiply(PointF p1, PointF p2, PointF p0) {
        return ((p1.x - p0.x) * (p2.y - p0.y) - (p2.x - p0.x) * (p1.y - p0.y));
    }

    public static PointF mid2Point(PointF point2D1, PointF point2D2) {
        return new PointF((point2D1.x + point2D2.x) / 2, (point2D1.y + point2D2.y) / 2);
    }

    /**
     * 计算两个屏幕点的距离
     *
     * @param point2D1
     * @param point2D2
     * @return
     */
    public static double dis2Point(PointF point2D1, PointF point2D2) {
        return Math.sqrt((point2D1.x - point2D2.x) * (point2D1.x - point2D2.x) + (point2D1.y - point2D2.y) * (point2D1.y - point2D2.y));
    }

    // 判断线段是否包含点point
    public static boolean isOnline(LineSegment line, PointF checkPoint) {
        return ((Math.abs(multiply(line.pt1, line.pt2, checkPoint)) < ESP) &&
                ((checkPoint.x - line.pt1.x) * (checkPoint.x - line.pt2.x) <= 0) &&
                ((checkPoint.y - line.pt1.y) * (checkPoint.y - line.pt2.y) <= 0));
    }

    /**
     * 判断一个点是否在线段上是否有垂直投影点
     *
     * @param p1
     * @param p2
     * @param cP
     * @return
     */
    private static boolean pointIsVerticalPro2LineSegment(PointF p1, PointF p2, PointF cP) {
        double cross = (p2.x - p1.x) * (cP.x - p1.x) + (p2.y - p1.y) * (cP.y - p1.y);
        if (cross <= 0) {
            return false;
        }
        double d2 = (p2.x - p1.x) * (p2.x - p1.x) + (p2.y - p1.y) * (p2.y - p1.y);
        return !(cross >= d2);
    }

    private static boolean pointIsVerticalPro2LineSegment(LineSegment line, PointF cP) {
        return pointIsVerticalPro2LineSegment(line.pt1, line.pt2, cP);
    }


    /**
     * 点到线段的距离
     * @param p
     * @param q
     * @param pt
     * @return
     */
    public static double dis2LineSegment(PointF p, PointF q, PointF pt) {
        float pqx = q.x - p.x;
        float pqy = q.y - p.y;
        float dx = pt.x - p.x;
        float dy = pt.y - p.y;
        float d = pqx * pqx + pqy * pqy;
        float t = pqx * dx + pqy * dy;
        if (d > 0) {
            t /= d;
        }
        if (t < 0) {
            t = 0;
        } else if (t > 1) {
            t = 1;
        }
        dx = p.x + t * pqx - pt.x;
        dy = p.y + t * pqy - pt.y;
        return Math.sqrt(dx * dx + dy * dy);
    }


    /**
     * 判断点到线段的距离
     *
     * @param line
     * @param c
     * @return
     */
    private static double dis2LineSegment(LineSegment line, PointF c) {
        return dis2LineSegment(line.pt1, line.pt2, c);
    }

    // 判断线段相交
    public static boolean intersect(LineSegment L1, LineSegment L2) {
        return ((Math.max(L1.pt1.x, L1.pt2.x) >= Math.min(L2.pt1.x, L2.pt2.x)) &&
                (Math.max(L2.pt1.x, L2.pt2.x) >= Math.min(L1.pt1.x, L1.pt2.x)) &&
                (Math.max(L1.pt1.y, L1.pt2.y) >= Math.min(L2.pt1.y, L2.pt2.y)) &&
                (Math.max(L2.pt1.y, L2.pt2.y) >= Math.min(L1.pt1.y, L1.pt2.y)) &&
                (multiply(L2.pt1, L1.pt2, L1.pt1) * multiply(L1.pt2, L2.pt2, L1.pt1) >= 0) &&
                (multiply(L1.pt1, L2.pt2, L2.pt1) * multiply(L2.pt2, L1.pt2, L2.pt1) >= 0)
        );
    }

    /* 射线法判断点q与多边形polygon的位置关系，要求polygon为简单多边形，顶点逆时针排列
    如果点在多边形内： 返回0
	如果点在多边形边上： 返回1
	如果点在多边形外： 返回2
	*/
    public static int inPolygon(List<PointF> polygon, PointF point) {
        int count = 0;
        //射线
        LineSegment line = new LineSegment();
        line.pt1 = point;
        line.pt2.y = point.y;
        line.pt2.x = Float.MIN_VALUE;
        LineSegment side;
        for (int i = 0, len = polygon.size(); i < len; i++) {
            // 得到多边形的一条边
            side = new LineSegment();
            side.pt1 = polygon.get(i);
            side.pt2 = polygon.get((i + 1) % len);
            if (isOnline(side, point)) {
                return 1;
            }
            // 如果side平行x轴则不作考虑
            if (Math.abs(side.pt1.y - side.pt2.y) < ESP) {
                continue;
            }
            if (isOnline(line, side.pt1)) {
                if (side.pt1.y > side.pt2.y) {
                    count++;
                }
            } else if (isOnline(line, side.pt2)) {
                if (side.pt2.y > side.pt1.y) {
                    count++;
                }
            } else if (intersect(line, side)) {
                count++;
            }
        }
        if (count % 2 == 1) {
            return 0;
        } else {
            return 2;
        }
    }
}
