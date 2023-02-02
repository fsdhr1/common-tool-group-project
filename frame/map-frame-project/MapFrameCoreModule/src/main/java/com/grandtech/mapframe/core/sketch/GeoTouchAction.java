package com.grandtech.mapframe.core.sketch;

import android.graphics.PointF;

import com.grandtech.mapframe.core.enumeration.GeometryType;
import com.grandtech.mapframe.core.sketch.geometry.LineSegment;
import com.grandtech.mapframe.core.sketch.geometry.SPointF;
import com.grandtech.mapframe.core.util.GeoRelationShip;

import java.util.List;
import java.util.regex.Pattern;

/**
 *
 * @author zy
 * @date 2018/12/25
 * geometry事件处理器
 */

public class GeoTouchAction {

    //优先级是  点>线>面
    /**
     * 点容差
     */
    public final static float pTolerance = 100;
    /**
     * 线容差
     */
    public final static float lTolerance = 100;
    /**
     * 面容差
     */
    public final static float aTolerance = 150;


    private GeoTouchMode geoTouchMode = GeoTouchMode.TouchOnNull;

    GeometryType sketchType;

    PointF touchPoint;

    SPointF pointFs0;

    List<SPointF> pointFS1;

    List<List<SPointF>> pointFS2;

    List<List<List<SPointF>>> pointFS3;

    String geoIp;

    //--------------------------------这三个是长按编辑比较关注的------------------------------------

    private SPointF aPointF;       //橡皮绳挂点 1

    private SPointF bPointF;       //当前编辑的点   a*-------b*-------c*

    private SPointF cPointF;        //橡皮绳挂点 2

    //--------------------------------这三个是长按编辑比较关注的------------------------------------
    private GeoTouchAction() {

    }

    private GeoTouchAction(GeometryType sketchType, PointF touchPoint) {
        this.sketchType = sketchType;
        this.touchPoint = touchPoint;
    }

    private void setPointFs0(SPointF pointFs0) {
        this.pointFs0 = pointFs0;
    }

    private void setPointFS1(List<SPointF> pointFS1) {
        this.pointFS1 = pointFS1;
    }

    private void setPointFS2(List<List<SPointF>> pointFS2) {
        this.pointFS2 = pointFS2;
    }

    private void setPointFS3(List<List<List<SPointF>>> pointFS3) {
        this.pointFS3 = pointFS3;
    }

    public SPointF getPointFs0() {
        return pointFs0;
    }

    public List<SPointF> getPointFS1() {
        return pointFS1;
    }

    public List<List<SPointF>> getPointFS2() {
        return pointFS2;
    }

    public List<List<List<SPointF>>> getPointFS3() {
        return pointFS3;
    }

    //----------------------------------------------------------------------------------------------

    public static GeoTouchAction createEvent() {
        return new GeoTouchAction();
    }

    public static GeoTouchAction createEvent0(GeometryType sketchType, PointF touchPoint, SPointF pointFs0) {
        GeoTouchAction geoTouchAction = new GeoTouchAction(sketchType, touchPoint);
        geoTouchAction.setPointFs0(pointFs0);
        return geoTouchAction;
    }

    public static GeoTouchAction createEvent1(GeometryType sketchType, PointF touchPoint, List<SPointF> pointFS1) {
        GeoTouchAction geoTouchAction = new GeoTouchAction(sketchType, touchPoint);
        geoTouchAction.setPointFS1(pointFS1);
        String ip = geoTouchAction.compute1DimensionPointIp(pointFS1, touchPoint);
        geoTouchAction.computeTouch(ip);
        return geoTouchAction;
    }

    public static GeoTouchAction createEvent2(GeometryType sketchType, PointF touchPoint, List<List<SPointF>> pointFS2) {
        GeoTouchAction geoTouchAction = new GeoTouchAction(sketchType, touchPoint);
        geoTouchAction.setPointFS2(pointFS2);
        String ip = null;
        if (sketchType == GeometryType.multiLine) {
            ip = geoTouchAction.compute2DimensionPointIp(pointFS2, touchPoint);
        }
        if (sketchType == GeometryType.polygon) {
            ip = geoTouchAction.compute2DimensionPointIp(pointFS2, touchPoint);
        }
        geoTouchAction.computeTouch(ip);
        return geoTouchAction;
    }

    public static GeoTouchAction createEvent3(GeometryType sketchType, PointF touchPoint, List<List<List<SPointF>>> pointFS3) {
        GeoTouchAction geoTouchAction = new GeoTouchAction(sketchType, touchPoint);
        geoTouchAction.setPointFS3(pointFS3);
        String ip = geoTouchAction.compute3DimensionPointIp(pointFS3, touchPoint);
        geoTouchAction.computeTouch(ip);
        return geoTouchAction;
    }

    //----------------------------------------------------------------------------------------------
    public GeoTouchMode getGeoTouchMode() {
        return geoTouchMode;
    }

    public enum GeoTouchMode {
        TouchOnNull,
        TouchOnMidPoint,
        TouchOnPoint,
        TouchOnLine,
        TouchOnPolygon
    }
    //--------------------------------------------拖拽参数------------------------------------------
    //--------------------------------------------拖拽参数------------------------------------------

    public PointF getAPointF() {
        return aPointF;
    }

    public PointF getBPointF() {
        return bPointF;
    }

    public PointF getCPointF() {
        return cPointF;
    }

    public String getGeoIp() {
        return geoIp;
    }


    //-------------------------------------计算touch结果参数----------------------------------------
    //-------------------------------------计算touch结果参数----------------------------------------

    /**
     * mp.2.p.0.r.3.v 表示：multipolygon第2个polygon的第0个ring的第3个顶点
     * <p>
     * mp.2.p.0.r.3.m 表示：multipolygon第2个polygon的第0个ring的第3个中点
     * p.0.r.3.m 表示：polygon的第0个ring的第3个中点
     * p.0.r.3.m 表示：polygon的第0个ring的第3个中点
     * <p>
     * ml.0.l.22.v 表示：multiline第0个line的第22个顶点
     * <p>
     * ml.0.m.22 表示：multiline第0个line的第22个中点
     * <p>
     * l.0.v
     * <p>
     * l.0.m
     * <p>
     * mp.3.p
     * <p>
     * mp.2.p
     * <p>
     * v.0
     */
    public final static String mPM = "mp.*.p.*.r.*.m";
    public final static String mPV = "mp.*.p.*.r.*.v";

    public final static String pM = "p.*.r.*.m";
    public final static String pV = "p.*.r.*.v";

    public final static String mLV = "ml.*.l*.v";
    public final static String mLM = "ml.*.l*.m";

    public final static String lV = "l.*.v";
    public final static String lM = "l.*.m";

    private SPointF[] computeTouch(String ip) {
        try{
            if (ip == null || ip.length() == 0) {
                return null;
            }
            this.geoIp = ip;
            String[] item = ip.split("\\.");
            if (sketchType == GeometryType.point) {

            }
            if (sketchType == GeometryType.multiPoint) {

            }
            if (sketchType == GeometryType.line) {
                if (Pattern.matches(lM, ip)) {
                    int len = pointFS1.size();
                    if (len < 2) {
                        return null;
                    }
                    int index = Integer.parseInt(item[1]);
                    aPointF = pointFS1.get(index);
                    cPointF = pointFS1.get(index + 1);
                    bPointF = new SPointF((aPointF.x + cPointF.x) / 2, (aPointF.y + cPointF.y) / 2);
                    geoTouchMode = GeoTouchMode.TouchOnMidPoint;
                    return new SPointF[]{aPointF, bPointF, cPointF};
                } else if (Pattern.matches(lV, ip)) {
                    int len = pointFS1.size();
                    if (len < 2) {
                        return null;
                    }
                    int index = Integer.parseInt(item[1]);
                    if (index == 0) {
                    /*aPointF = null;
                    bPointF = pointFS1.get(index);
                    cPointF = pointFS1.get(index + 1);*/
                        aPointF = pointFS1.get(index);
                        bPointF = pointFS1.get(index + 1);
                        cPointF = null;
                    }
                    //选中最后一个点
                    else if (index == len - 1) {
                    /*aPointF = pointFS1.get(index - 1);
                    bPointF = pointFS1.get(index);
                    cPointF = null;*/
                        aPointF = null;
                        bPointF = pointFS1.get(index - 1);
                        cPointF = pointFS1.get(index);
                    } else {
                        aPointF = pointFS1.get(index - 1);
                        bPointF = pointFS1.get(index);
                        cPointF = pointFS1.get(index + 1);
                    }
                    geoTouchMode = GeoTouchMode.TouchOnPoint;
                    return new SPointF[]{aPointF, bPointF, cPointF};
                } else {
                    geoTouchMode = GeoTouchMode.TouchOnLine;
                }
            }
            if (sketchType == GeometryType.multiLine) {
                if (Pattern.matches(mLM, ip)) {
                    int rIndex = Integer.parseInt(item[1]);
                    int vIndex = Integer.parseInt(item[3]);
                    int len = pointFS2.get(rIndex).size();
                    aPointF = pointFS2.get(rIndex).get(vIndex);
                    cPointF = pointFS2.get(rIndex).get((vIndex + 1) % len);
                    bPointF = new SPointF((aPointF.x + cPointF.x) / 2, (aPointF.y + cPointF.y) / 2);
                    geoTouchMode = GeoTouchMode.TouchOnMidPoint;
                    return new SPointF[]{aPointF, bPointF, cPointF};
                } else if (Pattern.matches(mLV, ip)) {
                    int rIndex = Integer.parseInt(item[1]);
                    int vIndex = Integer.parseInt(item[3]);
                    int len = pointFS2.get(rIndex).size();
                    aPointF = pointFS2.get(rIndex).get((vIndex - 1 + len) % len);
                    cPointF = pointFS2.get(rIndex).get((vIndex + 1) % len);
                    bPointF = pointFS2.get(rIndex).get(vIndex);
                    geoTouchMode = GeoTouchMode.TouchOnPoint;
                    return new SPointF[]{aPointF, bPointF, cPointF};
                } else {
                    geoTouchMode = GeoTouchMode.TouchOnLine;
                }
            }
            if (sketchType == GeometryType.polygon) {
                if (Pattern.matches(pM, ip)) {
                    int rIndex = Integer.parseInt(item[1]);
                    int vIndex = Integer.parseInt(item[3]);
                    int len = pointFS2.get(rIndex).size();
                    aPointF = pointFS2.get(rIndex).get(vIndex);
                    cPointF = pointFS2.get(rIndex).get((vIndex + 1) % len);
                    bPointF = new SPointF((aPointF.x + cPointF.x) / 2, (aPointF.y + cPointF.y) / 2);
                    geoTouchMode = GeoTouchMode.TouchOnMidPoint;
                    return new SPointF[]{aPointF, bPointF, cPointF};
                } else if (Pattern.matches(pV, ip)) {
                    int rIndex = Integer.parseInt(item[1]);
                    int vIndex = Integer.parseInt(item[3]);
                    int len = pointFS2.get(rIndex).size();
                    aPointF = pointFS2.get(rIndex).get((vIndex - 1 + len) % len);
                    cPointF = pointFS2.get(rIndex).get((vIndex + 1) % len);
                    bPointF = pointFS2.get(rIndex).get(vIndex);
                    geoTouchMode = GeoTouchMode.TouchOnPoint;
                    return new SPointF[]{aPointF, bPointF, cPointF};
                } else {
                    geoTouchMode = GeoTouchMode.TouchOnPolygon;
                }
            }
            if (sketchType == GeometryType.multiPolygon) {
                if (Pattern.matches(mPM, ip)) {
                    int pIndex = Integer.parseInt(item[1]);
                    int rIndex = Integer.parseInt(item[3]);
                    int mIndex = Integer.parseInt(item[5]);
                    List<List<SPointF>> polygon = pointFS3.get(pIndex);
                    int len = polygon.get(rIndex).size();
                    aPointF = polygon.get(rIndex).get(mIndex);
                    cPointF = polygon.get(rIndex).get((mIndex + 1) % len);
                    bPointF = new SPointF((aPointF.x + cPointF.x) / 2, (aPointF.y + cPointF.y) / 2);
                    geoTouchMode = GeoTouchMode.TouchOnMidPoint;
                    return new SPointF[]{aPointF, bPointF, cPointF};
                } else if (Pattern.matches(mPV, ip)) {
                    int pIndex = Integer.parseInt(item[1]);
                    int rIndex = Integer.parseInt(item[3]);
                    int vIndex = Integer.parseInt(item[5]);
                    List<List<SPointF>> polygon = pointFS3.get(pIndex);
                    int len = polygon.get(rIndex).size();
                    aPointF = polygon.get(rIndex).get((vIndex - 1 + len) % len);
                    cPointF = polygon.get(rIndex).get((vIndex + 1) % len);
                    bPointF = polygon.get(rIndex).get(vIndex);
                    geoTouchMode = GeoTouchMode.TouchOnPoint;
                    return new SPointF[]{aPointF, bPointF, cPointF};
                } else {
                    geoTouchMode = GeoTouchMode.TouchOnPolygon;
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        return null;
    }


    /**
     * 可计算，polygon，multiLineString
     *
     * @param lists
     * @param touchPoint
     * @return
     */
    private String compute1DimensionPointIp(List<SPointF> lists, PointF touchPoint) {
        double vDis, mDis, lDis;
        double minVDis = Double.MAX_VALUE;
        double minMDis = Double.MAX_VALUE;
        double minLDis = Double.MAX_VALUE;
        //在第N条条线上
        int lineIndexV = 0, lineIndexM = 0;
        //在第N条线的第N个点上
        int indexV = -1, indexL = -1, indexM = -1;
        SPointF midPoint;
        for (int i = 0, lenI = lists.size(); i < lenI; i++) {
            vDis = GeoRelationShip.dis2Point(touchPoint, lists.get(i));
            if (vDis <= pTolerance) {
                if (vDis < minVDis) {
                    minVDis = vDis;
                    indexV = i;
                }
            }
            midPoint = new SPointF(GeoRelationShip.mid2Point(lists.get(i), lists.get((i + 1) % lenI)));
            mDis = GeoRelationShip.dis2Point(touchPoint, midPoint);
            if (mDis <= pTolerance) {
                if (mDis < minMDis) {
                    minMDis = mDis;
                    indexM = i;
                }
            }
            lDis = GeoRelationShip.dis2LineSegment(lists.get(i), lists.get((i + 1) % lenI), touchPoint);
            if (lDis <= lTolerance) {
                if (lDis < minLDis) {
                    minLDis = lDis;
                    indexL = i;
                }
            }
        }
        StringBuilder geoIp = new StringBuilder();
        if (indexV != -1 && indexM == -1) {
            if (sketchType == GeometryType.multiPoint) {
                geoIp.append("mp.").append(lineIndexV).append(".l.").append(indexV).append(".v");
            }
            if (sketchType == GeometryType.line) {
                geoIp.append("l.").append(indexV).append(".v");
            }
        } else if (indexV == -1 && indexM != -1) {
            if (sketchType == GeometryType.line) {
                geoIp.append("l.").append(indexM).append(".m");
            }
        } else if (indexV != -1 && indexM != -1) {
            if (sketchType == GeometryType.line) {
                if (minVDis < minMDis) {
                    geoIp.append("l.").append(indexV).append(".v");
                } else {
                    geoIp.append("l.").append(indexM).append(".m");
                }
            }
        } else if (indexV == -1 || indexM == -1) {
            if (sketchType == GeometryType.line) {
                if (indexL != -1) {
                    geoIp.append("l.").append(indexL);
                }
            }
        }
        return geoIp.toString();
    }


    /**
     * 可计算，polygon，multiLineString
     *
     * @param lists
     * @param touchPoint
     * @return
     */
    private String compute2DimensionPointIp(List<List<SPointF>> lists, PointF touchPoint) {
        double vDis, mDis, lDis;
        double minVDis = Double.MAX_VALUE;
        double minMDis = Double.MAX_VALUE;
        double minLDis = Double.MAX_VALUE;
        //在第N条条线上
        int lineIndexV = 0, lineIndexM = 0;
        //在第N条线的第N个点上
        int indexV = -1, indexL = -1, indexM = -1;
        SPointF midPoint;
        List<SPointF> line;
        for (int i = 0, lenI = lists.size(); i < lenI; i++) {
            line = lists.get(i);
            for (int j = 0, lenJ = line.size(); j < lenJ; j++) {
                vDis = GeoRelationShip.dis2Point(touchPoint, line.get(j));
                if (vDis <= pTolerance) {
                    if (vDis < minVDis) {
                        minVDis = vDis;
                        lineIndexV = i;
                        indexV = j;
                    }
                }
                midPoint = new SPointF(GeoRelationShip.mid2Point(line.get(j), line.get((j + 1) % lenJ)));
                mDis = GeoRelationShip.dis2Point(touchPoint, midPoint);
                if (mDis <= pTolerance) {
                    if (mDis < minMDis) {
                        minMDis = mDis;
                        lineIndexM = i;
                        indexM = j;
                    }
                }
                lDis = GeoRelationShip.dis2LineSegment(line.get(j), line.get((j + 1) % lenJ), touchPoint);
                if (lDis <= lTolerance) {
                    if (lDis < minLDis) {
                        minLDis = lDis;
                        indexL = j;
                    }
                }
            }
        }
        StringBuilder geoIp = new StringBuilder();
        if (indexV != -1 && indexM == -1) {
            if (sketchType == GeometryType.multiLine) {
                geoIp.append("ml.").append(lineIndexV).append(".l.").append(indexV).append(".v");
            }
            if (sketchType == GeometryType.polygon) {
                geoIp.append("p.").append(lineIndexV).append(".r.").append(indexV).append(".v");
            }
        } else if (indexV == -1 && indexM != -1) {
            if (sketchType == GeometryType.multiLine) {
                geoIp.append("ml.").append(lineIndexM).append(".l.").append(indexM).append(".m");
            }
            if (sketchType == GeometryType.polygon) {
                geoIp.append("p.").append(lineIndexM).append(".r.").append(indexM).append(".m");
            }
        } else if (indexV != -1 && indexM != -1) {
            if (minVDis < minMDis) {
                if (sketchType == GeometryType.multiLine) {
                    geoIp.append("ml.").append(lineIndexV).append(".l.").append(indexV).append(".v");
                }
                if (sketchType == GeometryType.polygon) {
                    geoIp.append("p.").append(lineIndexV).append(".r.").append(indexV).append(".v");
                }
            } else {
                if (sketchType == GeometryType.multiLine) {
                    geoIp.append("ml.").append(lineIndexM).append(".l.").append(indexM).append(".m");
                }
                if (sketchType == GeometryType.polygon) {
                    geoIp.append("p.").append(lineIndexM).append(".r.").append(indexM).append(".m");
                }
            }
        } else if (indexV == -1 && indexM == -1) {
            boolean isOnLine = false;
            if (indexL != -1) {
                if (sketchType == GeometryType.multiLine) {
                    geoIp.append("ml.").append(indexL);
                }
                if (sketchType == GeometryType.polygon) {
                    isOnLine = true;
                }
            }
            if (sketchType == GeometryType.polygon) {
                if (computePolygon(lists, touchPoint) || isOnLine) {
                    geoIp.append("p.").append(0);
                }
            }
        }
        return geoIp.toString();
    }

    // mp.2.p.0.r.3.v 表示：multipolygon第2个polygon的第0个ring的第3个顶点
    private String compute3DimensionPointIp(List<List<List<SPointF>>> lists, PointF touchPoint) {
        double vDis, mDis, lDis;
        double minVDis = Double.MAX_VALUE;
        double minMDis = Double.MAX_VALUE;
        double minLDis = Double.MAX_VALUE;
        int indexP = -1, indexP_V = -1, indexP_M = -1, indexR = -1, indexR_V = -1, indexR_M = -1, indexV = -1, indexM = -1;
        SPointF midPoint;
        for (int i = 0, lenI = lists.size(); i < lenI; i++) {
            for (int j = 0, lenJ = lists.get(i).size(); j < lenJ; j++) {
                for (int k = 0, lenK = lists.get(i).get(j).size(); k < lenK; k++) {
                    vDis = GeoRelationShip.dis2Point(touchPoint, lists.get(i).get(j).get(k));
                    if (vDis <= pTolerance) {
                        if (vDis < minVDis) {
                            minVDis = vDis;
                            indexP_V = i;
                            indexR_V = j;
                            indexV = k;
                        }
                    }
                    midPoint = new SPointF(GeoRelationShip.mid2Point(lists.get(i).get(j).get(k), lists.get(i).get(j).get((k + 1) % lenK)));
                    mDis = GeoRelationShip.dis2Point(touchPoint, midPoint);
                    if (mDis <= pTolerance) {
                        if (mDis < minMDis) {
                            minMDis = mDis;
                            indexP_M = i;
                            indexR_M = j;
                            indexM = k;
                        }
                    }
                    lDis = GeoRelationShip.dis2LineSegment(lists.get(i).get(j).get(k), lists.get(i).get(j).get((k + 1) % lenK), touchPoint);
                    if (lDis <= lTolerance) {
                        if (lDis < minLDis) {
                            minLDis = lDis;
                            indexP = i;
                            indexR = j;
                        }
                    }
                }
            }
        }
        //  mp.2.p.0.r.3.v
        StringBuilder geoIp = new StringBuilder();
        if (indexV != -1 && indexM == -1) {
            geoIp.append("mp.").append(indexP_V).append(".p.").append(indexR_V).append(".r.").append(indexV).append(".v");
        } else if (indexV == -1 && indexM != -1) {
            geoIp.append("mp.").append(indexP_M).append(".p.").append(indexR_M).append(".r.").append(indexM).append(".m");
        } else if (indexV != -1 && indexM != -1) {
            if (minVDis < minMDis) {
                geoIp.append("mp.").append(indexP_V).append(".p.").append(indexR_V).append(".r.").append(indexV).append(".v");
            } else {
                geoIp.append("mp.").append(indexP_M).append(".p.").append(indexR_M).append(".r.").append(indexM).append(".m");
            }
        } else if (indexV == -1 && indexM == -1) {
            if (indexR != -1) {
                geoIp.append("mp.").append(indexP).append(".p.").append(indexR).append(".r.");
            }
        }
        return geoIp.toString();
    }


    /**
     * 面点击不需要容差判断
     * 计算点与多边形的关系
     *
     * @param lists
     * @param touchPoint
     */
    private boolean computePolygon(List<List<SPointF>> lists, PointF touchPoint) {
        //射线(很长的线段)
        int intersectCount = 0;
        LineSegment radioLine = new LineSegment();
        radioLine.pt1 = touchPoint;
        radioLine.pt2.x = Float.MIN_VALUE;
        radioLine.pt2.y = touchPoint.y;
        LineSegment sideLine;
        List<SPointF> ring;
        for (int i = 0, lenI = lists.size(); i < lenI; i++) {
            ring = lists.get(i);
            for (int j = 0, lenJ = ring.size(); j < lenJ; j++) {
                sideLine = new LineSegment(ring.get(j), ring.get((j + 1) % lenJ));
                if (GeoRelationShip.isOnline(sideLine, touchPoint)) {
                    return true;
                }
                // 如果side平行x轴则不作考虑
                if (Math.abs(sideLine.pt1.y - sideLine.pt2.y) == 0) {
                    continue;
                }
                if (GeoRelationShip.isOnline(radioLine, sideLine.pt1)) {
                    if (sideLine.pt1.y > sideLine.pt2.y) {
                        intersectCount++;
                    }
                } else if (GeoRelationShip.isOnline(radioLine, sideLine.pt2)) {
                    if (sideLine.pt2.y > sideLine.pt1.y) {
                        intersectCount++;
                    }
                } else if (GeoRelationShip.intersect(radioLine, sideLine)) {
                    intersectCount++;
                }
            }
        }
        if (intersectCount % 2 == 0) {
            return false;
        } else {
            return true;
        }
    }

    //-------------------------------------计算touch结果参数----------------------------------------
    //-------------------------------------计算touch结果参数----------------------------------------
}
