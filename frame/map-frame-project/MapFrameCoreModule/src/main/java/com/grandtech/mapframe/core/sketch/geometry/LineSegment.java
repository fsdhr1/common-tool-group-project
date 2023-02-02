package com.grandtech.mapframe.core.sketch.geometry;

import android.graphics.PointF;

/**
 *
 * @author zy
 * @date 2017/12/5
 */

public class LineSegment {

    public PointF pt1;
    public PointF pt2;

    public LineSegment() {
        this.pt1 = new PointF();
        this.pt2 = new PointF();
    }

    public LineSegment(PointF pt1, PointF pt2) {
        this.pt1 = pt1;
        this.pt2 = pt2;
    }

    public PointF getMidPoint(){
        return new PointF((pt1.x + pt2.x) / 2, (pt1.y + pt2.y) / 2);
    }

}
