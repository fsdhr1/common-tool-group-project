package com.grandtech.mapframe.core.sketch;

import android.graphics.PointF;

import com.grandtech.mapframe.core.enumeration.GeometryType;
import com.grandtech.mapframe.core.sketch.geometry.SPointF;
import com.grandtech.mapframe.core.sketch.geometry.SketchLineString;
import com.grandtech.mapframe.core.sketch.geometry.SketchMultiLineString;
import com.grandtech.mapframe.core.sketch.geometry.SketchMultiPolygon;
import com.grandtech.mapframe.core.sketch.geometry.SketchPolygon;
import com.grandtech.mapframe.core.util.CommonGeoUnion;
import com.mapbox.geojson.Geometry;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.MultiLineString;
import com.mapbox.geojson.MultiPolygon;
import com.mapbox.geojson.Point;
import com.mapbox.geojson.Polygon;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapboxMap;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Created by zy on 2018/12/20.
 */

public final class SketchUtil {

    public static List<Point> lineStringMidPoints(LineString lineString) {
        List<Point> vList = lineString.coordinates();
        return lineStringMidPoints(vList);
    }

    public static List<Point> lineStringMidPoints(List<Point> vList) {
        return getPoint2MidPoint(vList, false);
    }

    public static List<Point> ringMidPoints(List<Point> vList) {
        return getPoint2MidPoint(vList, true);
    }

    public static List<List<Point>> multiLineStringMidPoints(MultiLineString multiLineString) {
        List<List<Point>> vList = multiLineString.coordinates();
        return multiLineStringMidPoints(vList);
    }

    public static List<List<Point>> multiLineStringMidPoints(List<List<Point>> vList) {
        List<List<Point>> mid = new ArrayList<>();
        List<Point> LineStringList;
        for (int i = 0; i < vList.size(); i++) {
            LineStringList = vList.get(i);
            LineStringList = getPoint2MidPoint(LineStringList, false);
            mid.add(LineStringList);
        }
        return mid;
    }

    public static List<List<Point>> polygonMidPoints(Polygon polygon) {
        List<List<Point>> vList = polygon.coordinates();
        return polygonMidPoints(vList);
    }

    public static List<List<Point>> polygonMidPoints(List<List<Point>> vList) {
        List<List<Point>> mid = new ArrayList<>();
        List<Point> ring;
        for (int i = 0; i < vList.size(); i++) {
            ring = vList.get(i);
            ring = getPoint2MidPoint(ring, true);
            mid.add(ring);
        }
        return mid;
    }

    public static List<List<List<Point>>> multiPolygonMidPoints(List<List<List<Point>>> vList) {
        List<List<List<Point>>> mid = new ArrayList<>();
        for (int i = 0; i < vList.size(); i++) {
            mid.add(polygonMidPoints(vList.get(i)));
        }
        return mid;
    }

    public static List<LatLng> listPoint2ListLatLng(List<Point> points) {
        if (points == null) {
            return null;
        }
        List<LatLng> res = new ArrayList<>();
        for (int i = 0; i < points.size(); i++) {
            res.add(new LatLng(points.get(i).latitude(), points.get(i).longitude()));
        }
        return res;
    }

    public static List<Point> line2Ring(List<Point> lineOrRing, Point point) {
        if (lineOrRing.size() < 2) {
            lineOrRing.add(point);
            return lineOrRing;
        } else if (lineOrRing.size() == 2) {
            lineOrRing.add(point);
            lineOrRing.add(lineOrRing.get(0));
        } else {
            /**
             * 是环
             */
            if (lineOrRing.get(0).toJson().equals(lineOrRing.get(lineOrRing.size() - 1).toJson())) {
                lineOrRing.add(lineOrRing.size() - 1, point);
            } else {
                lineOrRing.add(point);
                lineOrRing.add(lineOrRing.get(0));
            }
        }
        return lineOrRing;
    }

    public static List<Point> line2Ring(List<Point> line) {
        line.add(line.get(0));
        return line;
    }

    /**
     * 闭合 Polygon
     *
     * @return
     */
    public static Polygon closeSketchPolygon(SketchPolygon sketchPolygon) {
        if (sketchPolygon == null) {
            return null;
        }
        List<List<Point>> lists = sketchPolygon.coordinates();
        List<Point> temp;
        List<List<Point>> res = new ArrayList<>();
        for (int i = 0; i < lists.size(); i++) {
            temp = new ArrayList<>();
            temp.addAll(lists.get(i));
            temp.add(lists.get(i).get(0));
            res.add(temp);
        }
        return Polygon.fromLngLats(res);
    }


    public static Polygon closeListLine(List<List<Point>> lists) {
        try {
            List<Point> temp;
            List<List<Point>> res = new ArrayList<>();
            for (int i = 0; i < lists.size(); i++) {
                temp = new ArrayList<>();
                temp.addAll(lists.get(i));
                temp.add(lists.get(i).get(0));
                res.add(temp);
            }
            return Polygon.fromLngLats(res);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 闭合 MultiPolygon
     *
     * @return
     */
    public static MultiPolygon closeSketchMultiPolygon(SketchMultiPolygon sketchMultiPolygon) {
        if (sketchMultiPolygon == null) {
            return null;
        }
        List<List<List<Point>>> lists = sketchMultiPolygon.coordinates();
        List<List<Point>> temp;
        List<Polygon> polygons = new ArrayList<>();
        for (int i = 0; i < lists.size(); i++) {
            temp = lists.get(i);
            Polygon polygon = closeListLine(temp);
            if (polygon != null) {
                polygons.add(polygon);
            }
        }
        return MultiPolygon.fromPolygons(polygons);
    }

    public static List<Point> deepCopy1DimensionList(List<Point> points) {
        Point point;
        List<Point> deepCopy = new ArrayList<>();
        for (int i = 0; i < points.size(); i++) {
            point = Point.fromLngLat(points.get(i).longitude(), points.get(i).latitude());
            deepCopy.add(point);
        }
        return deepCopy;
    }

    public static List<List<Point>> deepCopy2DimensionList(List<List<Point>> points) {
        Point point;
        List<List<Point>> deepCopy = new ArrayList<>();
        for (int i = 0; i < points.size(); i++) {
            deepCopy.add(new ArrayList<Point>());
            for (int j = 0; j < points.get(i).size(); j++) {
                point = Point.fromLngLat(points.get(i).get(j).longitude(), points.get(i).get(j).latitude());
                deepCopy.get(i).add(point);
            }
        }
        return deepCopy;
    }

    public static List<List<Point>> close2DimensionList(List<List<Point>> points) {
        for (int i = 0; i < points.size(); i++) {
            line2Ring(points.get(i));
        }
        return points;
    }

    public static List<List<List<Point>>> deepCopy3DimensionList(List<List<List<Point>>> points) {
        Point point;
        List<List<List<Point>>> deepCopy = new ArrayList<>();
        for (int i = 0; i < points.size(); i++) {
            deepCopy.add(new ArrayList<List<Point>>());
            for (int j = 0; j < points.get(i).size(); j++) {
                deepCopy.get(i).add(new ArrayList<Point>());
                for (int k = 0; k < points.get(i).get(j).size(); k++) {
                    point = Point.fromLngLat(points.get(i).get(j).get(k).longitude(), points.get(i).get(j).get(k).latitude());
                    deepCopy.get(i).get(j).add(point);
                }
            }
        }
        return deepCopy;
    }

    public static List<List<List<Point>>> close3DimensionList(List<List<List<Point>>> points) {
        for (int i = 0; i < points.size(); i++) {
            close2DimensionList(points.get(i));
        }
        return points;
    }


    /**
     * 计算Geometry 绘制时橡皮绳的悬挂点
     *
     * @param type
     * @param geometry
     * @param geoIp
     * @return a*---------------*b-----------------*c   //橡皮绳数组   (b为屏幕中心点)
     */
    public static Point[] computeRubberAnchor(GeometryType type, Geometry geometry, String geoIp) {
        if (geoIp == null) {
            return null;
        }
        System.out.println(geoIp);
        String[] item = geoIp.split("\\.");
        if (item == null || item.length == 0) {
            return null;
        }
        if (type == GeometryType.point) {
            return null;
        } else if (type == GeometryType.multiPoint) {
            return null;
        } else if (type == GeometryType.line && Pattern.matches(GeoTouchAction.lV, geoIp)) {
            int indexV = Integer.parseInt(item[1]);
            SketchLineString sketchLineString = (SketchLineString) geometry;
            if (sketchLineString == null) {
                return null;
            }
            List<Point> points = sketchLineString.coordinates();
            return new Point[]{points.get(indexV), null, null};
        } else if (type == GeometryType.multiLine && Pattern.matches(GeoTouchAction.mLV, geoIp)) {
            int indexL = Integer.parseInt(item[1]);
            int indexV = Integer.parseInt(item[3]);
            SketchMultiLineString sketchMultiLineString = (SketchMultiLineString) geometry;
            if (sketchMultiLineString == null) {
                return null;
            }
            List<List<Point>> points = sketchMultiLineString.coordinates();
            List<Point> l = points.get(indexL);
            return new Point[]{l.get(0), null, l.get(indexV)};
        } else if (type == GeometryType.polygon && Pattern.matches(GeoTouchAction.pV, geoIp)) {
            int indexR = Integer.parseInt(item[1]);
            int indexV = Integer.parseInt(item[3]);
            SketchPolygon sketchPolygon = (SketchPolygon) geometry;
            if (sketchPolygon == null) {
                return null;
            }
            List<List<Point>> points = sketchPolygon.coordinates();
            List<Point> r = points.get(indexR);
            return new Point[]{r.get(0), null, r.get(indexV)};
        } else if (type == GeometryType.multiPolygon && Pattern.matches(GeoTouchAction.pV, geoIp)) {
            int indexP = Integer.parseInt(item[1]);
            int indexR = Integer.parseInt(item[3]);
            int indexV = Integer.parseInt(item[5]);
            SketchMultiPolygon sketchMultiPolygon = (SketchMultiPolygon) geometry;
            if (sketchMultiPolygon == null) {
                return null;
            }
            List<List<List<Point>>> points = sketchMultiPolygon.coordinates();
            List<List<Point>> p = points.get(indexP);
            List<Point> r = p.get(indexR);
            return new Point[]{r.get(0), null, r.get(indexV)};
        }
        //编辑多面的顶点、编辑多面的中点
        else if (type == GeometryType.multiPolygon && (Pattern.matches(GeoTouchAction.mPV, geoIp) || Pattern.matches(GeoTouchAction.mPM, geoIp))) {
            int indexP = Integer.parseInt(item[1]);
            int indexR = Integer.parseInt(item[3]);
            int indexV = Integer.parseInt(item[5]);
            SketchMultiPolygon sketchMultiPolygon = (SketchMultiPolygon) geometry;
            if (sketchMultiPolygon == null) {
                return null;
            }
            List<List<List<Point>>> points = deepCopy3DimensionList(sketchMultiPolygon.coordinates());
            List<List<Point>> p = points.get(indexP);
            List<Point> r = p.get(indexR);
            return new Point[]{r.get(0), null, r.get(indexV)};
        } else {
            System.out.println("未能获取橡皮绳挂点");
            return null;
        }
    }

    /**
     * Geometry+屏幕中点  构成新geometry
     *
     * @param type
     * @param geometry
     * @param geoIp
     * @param anchorPoint
     * @return
     */
    public static Geometry getRubberAnchorGeometry(GeometryType type, Geometry geometry, String geoIp, Point anchorPoint) {
        if (geoIp == null) {
            return null;
        }
        String[] item = geoIp.split("\\.");
        if (item == null || item.length == 0) {
            return null;
        }
        if (type == GeometryType.point) {
            return null;
        } else if (type == GeometryType.multiPoint) {
            return null;
        } else if (type == GeometryType.line && Pattern.matches(GeoTouchAction.lV, geoIp)) {
            int indexV = Integer.parseInt(item[1]);
            SketchLineString sketchLineString = (SketchLineString) geometry;
            if (sketchLineString == null) {
                return null;
            }
            List<Point> points = deepCopy1DimensionList(sketchLineString.coordinates());
            points.add(indexV + 1, anchorPoint);
            return LineString.fromLngLats(points);
        } else if (type == GeometryType.multiLine && Pattern.matches(GeoTouchAction.mLV, geoIp)) {
            int indexL = Integer.parseInt(item[1]);
            int indexV = Integer.parseInt(item[3]);
            SketchMultiLineString sketchMultiLineString = (SketchMultiLineString) geometry;
            if (sketchMultiLineString == null) {
                return null;
            }
            List<List<Point>> points = deepCopy2DimensionList(sketchMultiLineString.coordinates());
            List<Point> l = points.get(indexL);
            l.add(indexV + 1, anchorPoint);
            return MultiLineString.fromLngLats(points);
        } else if (type == GeometryType.polygon && Pattern.matches(GeoTouchAction.pV, geoIp)) {
            int indexR = Integer.parseInt(item[1]);
            int indexV = Integer.parseInt(item[3]);
            SketchPolygon sketchPolygon = (SketchPolygon) geometry;
            if (sketchPolygon == null) {
                return null;
            }
            List<List<Point>> points = deepCopy2DimensionList(sketchPolygon.coordinates());
            List<Point> r = points.get(indexR);
            r.add(indexV + 1, anchorPoint);
            points = close2DimensionList(points);
            return Polygon.fromLngLats(points);
        } else {
            int indexP = Integer.parseInt(item[1]);
            int indexR = Integer.parseInt(item[3]);
            int indexV = Integer.parseInt(item[5]);
            SketchMultiPolygon sketchMultiPolygon = (SketchMultiPolygon) geometry;
            if (sketchMultiPolygon == null) {
                return null;
            }
            List<List<List<Point>>> points = deepCopy3DimensionList(sketchMultiPolygon.coordinates());
            List<List<Point>> p = points.get(indexP);
            List<Point> r = p.get(indexR);
            r.add(indexV + 1, anchorPoint);
            points = close3DimensionList(points);
            return MultiPolygon.fromLngLats(points);
        }
    }

    public static Geometry getEditGeometry(GeometryType type, Geometry geometry, String geoIp, Point editPoint) {
        try{
            if (geoIp == null) {
                return null;
            }
            String[] item = geoIp.split("\\.");
            if (item == null || item.length == 0) {
                return null;
            }
            if (type == GeometryType.point) {
                return null;
            } else if (type == GeometryType.multiPoint) {
                return null;
            }
            //编辑线的顶点
            else if (type == GeometryType.line && Pattern.matches(GeoTouchAction.lV, geoIp)) {
                int indexV = Integer.parseInt(item[1]);
                SketchLineString sketchLineString = (SketchLineString) geometry;
                if (sketchLineString == null) {
                    return null;
                }
                List<Point> points = deepCopy1DimensionList(sketchLineString.coordinates());
                points.set(indexV, editPoint);
                return LineString.fromLngLats(points);
            }
            //编辑线的中点
            else if (type == GeometryType.line && Pattern.matches(GeoTouchAction.lM, geoIp)) {
                int indexV = Integer.parseInt(item[1]);
                SketchLineString sketchLineString = (SketchLineString) geometry;
                if (sketchLineString == null) {
                    return null;
                }
                List<Point> points = deepCopy1DimensionList(sketchLineString.coordinates());
                points.add(indexV + 1, editPoint);
                return LineString.fromLngLats(points);
            }
            //编辑多线的顶点
            else if (type == GeometryType.multiLine && Pattern.matches(GeoTouchAction.mLV, geoIp)) {
                int indexL = Integer.parseInt(item[1]);
                int indexV = Integer.parseInt(item[3]);
                SketchMultiLineString sketchMultiLineString = (SketchMultiLineString) geometry;
                if (sketchMultiLineString == null) {
                    return null;
                }
                List<List<Point>> points = deepCopy2DimensionList(sketchMultiLineString.coordinates());
                List<Point> l = points.get(indexL);
                l.set(indexV, editPoint);
                return MultiLineString.fromLngLats(points);
            }
            //编辑面的顶点
            else if (type == GeometryType.polygon && Pattern.matches(GeoTouchAction.pV, geoIp)) {
                int indexR = Integer.parseInt(item[1]);
                int indexV = Integer.parseInt(item[3]);
                SketchPolygon sketchPolygon = (SketchPolygon) geometry;
                if (sketchPolygon == null) {
                    return null;
                }
                List<List<Point>> points = deepCopy2DimensionList(sketchPolygon.coordinates());
                List<Point> r = points.get(indexR);
                r.set(indexV, editPoint);
                return Polygon.fromLngLats(points);
            }
            //编辑面的中点
            else if (type == GeometryType.polygon && Pattern.matches(GeoTouchAction.pM, geoIp)) {
                int indexR = Integer.parseInt(item[1]);
                int indexV = Integer.parseInt(item[3]);
                SketchPolygon sketchPolygon = (SketchPolygon) geometry;
                if (sketchPolygon == null) {
                    return null;
                }
                List<List<Point>> points = deepCopy2DimensionList(sketchPolygon.coordinates());
                List<Point> r = points.get(indexR);
                r.add(indexV + 1, editPoint);
                return Polygon.fromLngLats(points);
            }
            //编辑多面的顶点
            else if (type == GeometryType.multiPolygon && Pattern.matches(GeoTouchAction.mPV, geoIp)) {
                int indexP = Integer.parseInt(item[1]);
                int indexR = Integer.parseInt(item[3]);
                int indexV = Integer.parseInt(item[5]);
                SketchMultiPolygon sketchMultiPolygon = (SketchMultiPolygon) geometry;
                if (sketchMultiPolygon == null) {
                    return null;
                }
                List<List<List<Point>>> points = deepCopy3DimensionList(sketchMultiPolygon.coordinates());
                List<List<Point>> p = points.get(indexP);
                List<Point> r = p.get(indexR);
                r.set(indexV, editPoint);
                return MultiPolygon.fromLngLats(points);
            }
            //编辑多面的中点
            else if (type == GeometryType.multiPolygon && Pattern.matches(GeoTouchAction.mPM, geoIp)) {
                int indexP = Integer.parseInt(item[1]);
                int indexR = Integer.parseInt(item[3]);
                int indexV = Integer.parseInt(item[5]);
                SketchMultiPolygon sketchMultiPolygon = (SketchMultiPolygon) geometry;
                if (sketchMultiPolygon == null) {
                    return null;
                }
                List<List<List<Point>>> points = deepCopy3DimensionList(sketchMultiPolygon.coordinates());
                List<List<Point>> p = points.get(indexP);
                List<Point> r = p.get(indexR);
                r.add(indexV + 1, editPoint);
                return MultiPolygon.fromLngLats(points);
            } else {
                return null;
            }
        }catch (Exception e){
            return null;
        }

    }

    public static LatLng get2Point2MidPoint(LatLng _point1, LatLng _point2) {
        double[] res = CommonGeoUnion.get2Point2MidPoint(_point1.getLatitude(), _point1.getLongitude(), _point2.getLatitude(), _point2.getLongitude());
        return new LatLng(res[0], res[1]);
    }


    public static List<LatLng> get2Point2MidPoint(List<LatLng> _points, boolean isClose) {
        List<LatLng> midPoint = new ArrayList<>();
        if (_points != null && _points.size() > 1) {
            LatLng tempMidPoint, point1, point2;
            for (int i = 0; i < _points.size(); i++) {
                if (!isClose) {
                    if (i != _points.size() - 1) {
                        point1 = _points.get(i);
                        point2 = _points.get(i + 1);
                        tempMidPoint = get2Point2MidPoint(point1, point2);
                        midPoint.add(tempMidPoint);
                    }
                } else {
                    if (_points.size() > 2) {
                        if (i != _points.size() - 1) {
                            point1 = _points.get(i);
                            point2 = _points.get(i + 1);
                            tempMidPoint = get2Point2MidPoint(point1, point2);
                            midPoint.add(tempMidPoint);
                        } else {
                            point1 = _points.get(i);
                            point2 = _points.get(0);
                            tempMidPoint = get2Point2MidPoint(point1, point2);
                            midPoint.add(tempMidPoint);
                        }
                    } else {
                        point1 = _points.get(1);
                        point2 = _points.get(0);
                        tempMidPoint = get2Point2MidPoint(point1, point2);
                        midPoint.add(tempMidPoint);
                        break;
                    }
                }
            }
        }
        return midPoint;
    }

    public static Point getPoint2MidPoint(Point _point1, Point _point2) {
        double[] res = CommonGeoUnion.get2Point2MidPoint(_point1.longitude(), _point1.latitude(), _point2.longitude(), _point2.latitude());
        return Point.fromLngLat(res[0], res[1]);
    }

    public static List<Point> getPoint2MidPoint(List<Point> _points) {
        return getPoint2MidPoint(_points, true);
    }

    public static List<Point> getPoint2MidPoint(List<Point> _points, boolean isClose) {
        List<Point> midPoint = new ArrayList<>();
        if (_points != null && _points.size() > 1) {
            Point tempMidPoint, point1, point2;
            for (int i = 0; i < _points.size(); i++) {
                if (!isClose) {
                    if (i != _points.size() - 1) {
                        point1 = _points.get(i);
                        point2 = _points.get(i + 1);
                        tempMidPoint = getPoint2MidPoint(point1, point2);
                        midPoint.add(tempMidPoint);
                    }
                } else {
                    if (_points.size() > 2) {
                        if (i != _points.size() - 1) {
                            point1 = _points.get(i);
                            point2 = _points.get(i + 1);
                            tempMidPoint = getPoint2MidPoint(point1, point2);
                            midPoint.add(tempMidPoint);
                        } else {
                            point1 = _points.get(i);
                            point2 = _points.get(0);
                            tempMidPoint = getPoint2MidPoint(point1, point2);
                            midPoint.add(tempMidPoint);
                        }
                    } else {
                        point1 = _points.get(1);
                        point2 = _points.get(0);
                        tempMidPoint = getPoint2MidPoint(point1, point2);
                        midPoint.add(tempMidPoint);
                        break;
                    }
                }
            }
        }
        return midPoint;
    }


    /**
     * 判断是否两个点的距离是否超过某个阈值
     *
     * @param _point1
     * @param _point2
     * @param _dis
     * @return
     */
    public static boolean isNearPoint(LatLng _point1, LatLng _point2, double _dis) {
        return CommonGeoUnion.isNearPoint(_point1.getLatitude(), _point1.getLongitude(), _point2.getLatitude(), _point2.getLongitude(), _dis);
    }

    /**
     * 判断 point1， point1 谁离_p 最近
     *
     * @param point1
     * @param point2
     * @param _p
     * @return
     */
    public static int nearPoint(LatLng point1, LatLng point2, LatLng _p) {
        return CommonGeoUnion.nearPoint(point1.getLatitude(), point1.getLongitude(), point2.getLatitude(), point2.getLongitude(), _p.getLatitude(), _p.getLongitude());
    }

    /**
     * 计算两个屏幕点的距离
     *
     * @param pointF1
     * @param pointF2
     * @return
     */
    public static double dis2Point(PointF pointF1, PointF pointF2) {
        return Math.sqrt((pointF1.x - pointF2.x) * (pointF1.x - pointF2.x) + (pointF1.y - pointF2.y) * (pointF1.y - pointF2.y));
    }

    /**
     * 返回最近的索引
     *
     * @param list
     * @param pointF2
     * @param dis
     * @return
     */
    public static int getListNearestIndex(List<PointF> list, PointF pointF2, double dis) {
        PointF _pointF;
        double minDis = Double.MAX_VALUE;
        double _dis;
        int index = -1;
        for (int i = 0; i < list.size(); i++) {
            _pointF = list.get(i);
            _dis = dis2Point(_pointF, pointF2);
            if (_dis < minDis) {
                minDis = _dis;
                if (minDis < dis) {
                    index = i;
                }
            }
        }
        return index;
    }


    /**
     * 地理坐标点转屏幕坐标
     *
     * @param mapboxMap
     * @param point
     * @return
     */
    public static SPointF geo0DimensionPoint(MapboxMap mapboxMap, Point point) {
        PointF pointF = mapboxMap.getProjection().toScreenLocation(new LatLng(point.latitude(), point.longitude()));
        return new SPointF(pointF);
    }

    /**
     * 地理坐标串转屏幕坐标
     *
     * @param mapboxMap
     * @param list
     * @return
     */
    public static List<SPointF> geo1DimensionList(MapboxMap mapboxMap, List<Point> list) {
        Point point;
        List<SPointF> res = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            point = list.get(i);
            PointF pointF = mapboxMap.getProjection().toScreenLocation(new LatLng(point.latitude(), point.longitude()));
            res.add(new SPointF(pointF, i));
        }
        return res;
    }

    /**
     * 地理坐标串转屏幕坐标
     *
     * @param mapboxMap
     * @param list
     * @return
     */
    public static List<List<SPointF>> geo2DimensionList(MapboxMap mapboxMap, List<List<Point>> list) {
        PointF pointF;
        SPointF sPointF;
        List<Point> points;
        List<SPointF> sPointFS;
        List<List<SPointF>> res = new ArrayList<>();
        for (int i = 0, lenI = list.size(); i < lenI; i++) {
            points = list.get(i);
            sPointFS = new ArrayList<>();
            for (int j = 0, lenJ = points.size(); j < lenJ; j++) {
                pointF = mapboxMap.getProjection().toScreenLocation(new LatLng(points.get(j).latitude(), points.get(j).longitude()));
                sPointF = new SPointF(pointF, i, j);
                sPointFS.add(sPointF);
            }
            res.add(sPointFS);
        }
        return res;
    }


    public static List<List<List<SPointF>>> geo3DimensionList(MapboxMap mapboxMap, List<List<List<Point>>> list) {
        Point point;
        PointF pointF;
        SPointF sPointF;
        List<SPointF> sPointFS1;
        List<List<SPointF>> sPointFS2;
        List<List<List<SPointF>>> res = new ArrayList<>();
        for (int i = 0, lenI = list.size(); i < lenI; i++) {
            sPointFS2 = new ArrayList<>();
            for (int j = 0, lenJ = list.get(i).size(); j < lenJ; j++) {
                sPointFS1 = new ArrayList<>();
                sPointFS2.add(sPointFS1);
                for (int k = 0, lenK = list.get(i).get(j).size(); k < lenK; k++) {
                    point = list.get(i).get(j).get(k);
                    pointF = mapboxMap.getProjection().toScreenLocation(new LatLng(point.latitude(), point.longitude()));
                    sPointF = new SPointF(pointF, i, j, k);
                    sPointFS1.add(sPointF);
                }
            }
            res.add(sPointFS2);
        }
        return res;
    }
}
