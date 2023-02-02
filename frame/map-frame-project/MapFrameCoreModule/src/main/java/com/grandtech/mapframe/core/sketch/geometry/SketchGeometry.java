package com.grandtech.mapframe.core.sketch.geometry;

import android.graphics.PointF;


import com.grandtech.mapframe.core.enumeration.GeometryType;
import com.grandtech.mapframe.core.sketch.GeoTouchAction;
import com.grandtech.mapframe.core.sketch.SketchUtil;
import com.mapbox.geojson.Geometry;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.MultiLineString;
import com.mapbox.geojson.MultiPoint;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.maps.MapboxMap;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zy on 2018/12/21.
 */

public class SketchGeometry implements Cloneable {

    MapboxMap mapboxMap;
    int fillColor;
    int strokeColor;
    float alpha;
    float width;
    private GeometryType sketchType;
    private Geometry sketchGeometry;
    private String geoIp;
    private StringBuilder geoIpSb = new StringBuilder();

    public static SketchGeometry create(MapboxMap mapboxMap, GeometryType sketchType) {
        return new SketchGeometry(mapboxMap, sketchType);
    }

    public static SketchGeometry create(MapboxMap mapboxMap, GeometryType sketchType, int strokeColor, float alpha, float width) {
        return new SketchGeometry(mapboxMap, sketchType, strokeColor, alpha, width);
    }

    public static SketchGeometry create(MapboxMap mapboxMap, GeometryType sketchType, int fillColor, int strokeColor, float alpha, float width) {
        return new SketchGeometry(mapboxMap, sketchType, fillColor, strokeColor, alpha, width);
    }

    private SketchGeometry(MapboxMap mapboxMap, GeometryType sketchType) {
        this.mapboxMap = mapboxMap;
        this.sketchType = sketchType;
    }

    private SketchGeometry(MapboxMap mapboxMap, GeometryType sketchType, int strokeColor, float alpha, float width) {
        this.mapboxMap = mapboxMap;
        this.strokeColor = strokeColor;
        this.alpha = alpha;
        this.width = width;
        this.sketchType = sketchType;
    }

    private SketchGeometry(MapboxMap mapboxMap, GeometryType sketchType, int fillColor, int strokeColor, float alpha, float width) {
        this.mapboxMap = mapboxMap;
        this.fillColor = fillColor;
        this.strokeColor = strokeColor;
        this.alpha = alpha;
        this.width = width;
        this.sketchType = sketchType;
    }

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
     * <p>
     * v.0
     */

    public String computeDefaultGeoId() {
        try {
            if (sketchType == GeometryType.point) {
                //SketchPoint sketchPoint = (SketchPoint) sketchGeometry;
                this.geoIp = geoIpSb.append("v.").append(0).toString();
            }
            if (sketchType == GeometryType.multiPoint) {
                SketchMultiPoint sketchMultiPoint = (SketchMultiPoint) sketchGeometry;
                List<Point> multiPoint = sketchMultiPoint.coordinates();
                this.geoIp = geoIpSb.append("mv.").append(multiPoint.size() - 1).append(".v").toString();
            }
            if (sketchType == GeometryType.line) {
                SketchLineString sketchLineString = (SketchLineString) sketchGeometry;
                List<Point> line = sketchLineString.coordinates();
                this.geoIp = geoIpSb.append("l.")
                        .append(line.size() - 1)
                        .append(".v").toString();
            }
            if (sketchType == GeometryType.multiLine) {
                SketchMultiLineString sketchMultiLineString = (SketchMultiLineString) sketchGeometry;
                List<List<Point>> multiLine = sketchMultiLineString.coordinates();
                this.geoIp = geoIpSb.append("ml.")
                        .append(multiLine.size() - 1)
                        .append(".l.")
                        .append(multiLine.get(multiLine.size() - 1).size() - 1)
                        .append(".v").toString();
            }
            if (sketchType == GeometryType.polygon) {
                SketchPolygon sketchPolygon = (SketchPolygon) sketchGeometry;
                List<List<Point>> polygon = sketchPolygon.coordinates();
                this.geoIp = geoIpSb.append("p.")
                        .append(polygon.size() - 1)
                        .append(".r.")
                        .append(polygon.get(polygon.size() - 1).size() - 1)
                        .append(".v").toString();
            }
            if (sketchType == GeometryType.multiPolygon) {
                SketchMultiPolygon sketchMultiPolygon = (SketchMultiPolygon) sketchGeometry;
                List<List<List<Point>>> multiPolygon = sketchMultiPolygon.coordinates();
                this.geoIp = geoIpSb.append("mp.")
                        .append(multiPolygon.size() - 1)
                        .append(".p.")
                        .append(multiPolygon.get(multiPolygon.size() - 1).size() - 1)
                        .append(".r.")
                        .append(multiPolygon.get(multiPolygon.get(multiPolygon.size() - 1).size() - 1).size() - 1)
                        .append(".v").toString();
            }
            this.geoIp = geoIpSb.toString();
            return geoIp;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return geoIp;
    }

    public void smartCreateGeometry(Point point) {
        if (sketchType == null) {
            throw new RuntimeException("未知的Geometry类型!");
        }
        if (sketchGeometry == null) {
            if (sketchType == GeometryType.point) {
                sketchGeometry = SketchPoint.fromLngLat(point, SketchPoint.V_POINT);
            }
            if (sketchType == GeometryType.multiPoint) {
                List<Point> temp = new ArrayList<>();
                temp.add(point);
                sketchGeometry = SketchMultiPoint.fromLngLats(temp);
            }
            if (sketchType == GeometryType.line) {
                List<Point> line = new ArrayList<>();
                line.add(point);
                sketchGeometry = SketchLineString.fromLngLats(line, strokeColor, alpha, width);
            }
            if (sketchType == GeometryType.multiLine) {
                List<Point> line = new ArrayList<>();
                line.add(point);
                List<List<Point>> multiLine = new ArrayList<>();
                multiLine.add(line);
                sketchGeometry = SketchMultiLineString.fromLngLats(multiLine, strokeColor, alpha, width);
            }
            if (sketchType == GeometryType.polygon) {
                List<Point> ring = new ArrayList<>();
                ring.add(point);
                List<List<Point>> polygon = new ArrayList<>();
                polygon.add(ring);
                sketchGeometry = SketchPolygon.fromLngLats(polygon, fillColor, strokeColor, alpha, width);
            }
            if (sketchType == GeometryType.multiPolygon) {
                List<Point> ring = new ArrayList<>();
                ring.add(point);
                List<List<Point>> polygon = new ArrayList<>();
                polygon.add(ring);
                List<List<List<Point>>> multiPolygon = new ArrayList<>();
                multiPolygon.add(polygon);
                sketchGeometry = SketchMultiPolygon.fromLngLats(multiPolygon, fillColor, strokeColor, alpha, width);
            }
            computeDefaultGeoId();
        }
    }

    /**
     * 默认放到最后一个GeometryN里
     *
     * @param geometry
     * @param point
     */
    public void appendPoint2Geometry(Geometry geometry, Point point) {
        if (sketchType == null) {
            throw new RuntimeException("未知的Geometry类型!");
        }
        if (geometry == null) {
            return;
        }
        if (sketchType == GeometryType.point) {
            SketchPoint sketchPoint = (SketchPoint) geometry;
            this.sketchGeometry = SketchPoint.fromLngLat(point, sketchPoint.getPointType());
        }
        if (sketchType == GeometryType.multiPoint) {
            SketchMultiPoint sketchMultiPoint = (SketchMultiPoint) geometry;
            List<Point> temp = SketchUtil.deepCopy1DimensionList(sketchMultiPoint.coordinates());
            temp.add(point);
            this.sketchGeometry = SketchMultiPoint.fromLngLats(temp);
        }
        if (sketchType == GeometryType.line) {
            SketchLineString sketchLineString = (SketchLineString) geometry;
            List<Point> line = SketchUtil.deepCopy1DimensionList(sketchLineString.coordinates());
            line.add(point);
            this.sketchGeometry = SketchLineString.fromLngLats(line, strokeColor, alpha, width);
        }
        if (sketchType == GeometryType.multiLine) {
            SketchMultiLineString sketchMultiLineString = (SketchMultiLineString) geometry;
            List<List<Point>> multiLine = SketchUtil.deepCopy2DimensionList(sketchMultiLineString.coordinates());
            List<Point> line = multiLine.remove(multiLine.size() - 1);
            line.add(point);
            this.sketchGeometry = SketchMultiLineString.fromLngLats(multiLine, strokeColor, alpha, width);
        }
        if (sketchType == GeometryType.polygon) {
            SketchPolygon sketchPolygon = (SketchPolygon) geometry;
            List<List<Point>> polygon = SketchUtil.deepCopy2DimensionList(sketchPolygon.coordinates());
            List<Point> ring = polygon.get(polygon.size() - 1);
            ring.add(point);
            this.sketchGeometry = SketchPolygon.fromLngLats(polygon, fillColor, strokeColor, alpha, width);
        }
        if (sketchType == GeometryType.multiPolygon) {
            SketchMultiPolygon sketchMultiPolygon = (SketchMultiPolygon) geometry;
            List<List<List<Point>>> multiPolygon = SketchUtil.deepCopy3DimensionList(sketchMultiPolygon.coordinates());
            List<List<Point>> polygon = multiPolygon.get(multiPolygon.size() - 1);
            List<Point> ring = polygon.get(polygon.size() - 1);
            ring.add(point);
            this.sketchGeometry = SketchMultiPolygon.fromLngLats(multiPolygon, fillColor, strokeColor, alpha, width);
        }
        computeDefaultGeoId();
    }

    /**
     * 编辑
     *
     * @param geometry
     * @param point
     */
    public void editGeometry(Geometry geometry, Point point, String geoId) {
        if (sketchType == null) {
            throw new RuntimeException("未知的Geometry类型!");
        }
        if (geometry == null) {
            return;
        }
        Geometry geometryEdit = SketchUtil.getEditGeometry(sketchType, geometry, geoId, point);
        if (sketchType == GeometryType.point) {
            this.sketchGeometry = SketchPoint.fromGeometry(geometryEdit, SketchPoint.V_POINT);
        }
        if (sketchType == GeometryType.multiPoint) {
            this.sketchGeometry = SketchMultiPoint.fromGeometry(geometryEdit);
        }
        if (sketchType == GeometryType.line) {
            this.sketchGeometry = SketchLineString.fromGeometry(geometryEdit, strokeColor, alpha, width);
        }
        if (sketchType == GeometryType.multiLine) {
            this.sketchGeometry = SketchMultiLineString.fromGeometry(geometryEdit, strokeColor, alpha, width);
        }
        if (sketchType == GeometryType.polygon) {
            this.sketchGeometry = SketchPolygon.fromGeometry(geometryEdit, fillColor, strokeColor, alpha, width);
        }
        if (sketchType == GeometryType.multiPolygon) {
            this.sketchGeometry = SketchMultiPolygon.fromGeometry(geometryEdit, fillColor, strokeColor, alpha, width);
        }
        computeDefaultGeoId();
    }

    public void addGeometry(Geometry geometry) {
        if (sketchType == null) {
            throw new RuntimeException("未知的Geometry类型!");
        }
        if (geometry == null) {
            return;
        }
        if (sketchType == GeometryType.point) {
            this.sketchGeometry = SketchPoint.fromGeometry(geometry, SketchPoint.V_POINT);
        }
        if (sketchType == GeometryType.multiPoint) {
            this.sketchGeometry = SketchMultiPoint.fromGeometry(geometry);
        }
        if (sketchType == GeometryType.line) {
            this.sketchGeometry = SketchLineString.fromGeometry(geometry, strokeColor, alpha, width);
        }
        if (sketchType == GeometryType.multiLine) {
            this.sketchGeometry = SketchMultiLineString.fromGeometry(geometry, strokeColor, alpha, width);
        }
        if (sketchType == GeometryType.polygon) {
            this.sketchGeometry = SketchPolygon.fromGeometry(geometry, fillColor, strokeColor, alpha, width);
        }
        if (sketchType == GeometryType.multiPolygon) {
            this.sketchGeometry = SketchMultiPolygon.fromGeometry(geometry, fillColor, strokeColor, alpha, width);
        }
        computeDefaultGeoId();
    }

    public GeometryType getSketchType() {
        return sketchType;
    }

    public void drawToMap(MapboxMap mapboxMap) {
        if (getSketchGeometry() == null) {
            return;
        }
        if (getSketchType() == GeometryType.line) {
            SketchLineString sketchLineString = (SketchLineString) getSketchGeometry();
            sketchLineString.drawToMap(mapboxMap);
        }
        if (getSketchType() == GeometryType.polygon) {
            SketchPolygon sketchPolygon = (SketchPolygon) getSketchGeometry();
            sketchPolygon.drawToMap(mapboxMap);
        }
        if (getSketchType() == GeometryType.multiPolygon) {
            SketchMultiPolygon sketchMultiPolygon = (SketchMultiPolygon) getSketchGeometry();
            sketchMultiPolygon.drawToMap(mapboxMap);
        }
    }

    public void clearFromMap(MapboxMap mapboxMap) {
        if (getSketchGeometry() == null) {
            return;
        }
        if (getSketchType() == GeometryType.line) {
            SketchLineString lastSketchLineString = (SketchLineString) getSketchGeometry();
            lastSketchLineString.clearFromMap(mapboxMap);
        }
        if (getSketchType() == GeometryType.polygon) {
            SketchPolygon lastSketchPolygon = (SketchPolygon) getSketchGeometry();
            lastSketchPolygon.clearFromMap(mapboxMap);
        }
        if (getSketchType() == GeometryType.multiPolygon) {
            SketchMultiPolygon sketchMultiPolygon = (SketchMultiPolygon) getSketchGeometry();
            sketchMultiPolygon.clearFromMap(mapboxMap);
        }
    }

    /**
     * 不标准的,Polygon首位不闭合
     *
     * @return
     */
    public Geometry getSketchGeometry() {
        return sketchGeometry;
    }

    /**
     * 标准的Mapbox Geometry
     *
     * @return
     */
    public Geometry getGeometry() {
        if (sketchType == GeometryType.point) {
            SketchPoint sketchPoint = (SketchPoint) sketchGeometry;
            if (sketchPoint == null) {
                return null;
            }
            return Point.fromLngLat(sketchPoint.point.longitude(), sketchPoint.point.latitude());
        } else if (sketchType == GeometryType.multiPoint) {
            SketchMultiPoint sketchMultiPoint = (SketchMultiPoint) sketchGeometry;
            if (sketchMultiPoint == null) {
                return null;
            }
            return MultiPoint.fromLngLats(sketchMultiPoint.coordinates());
        } else if (sketchType == GeometryType.line) {
            SketchLineString sketchLineString = (SketchLineString) sketchGeometry;
            if (sketchLineString == null) {
                return null;
            }
            return LineString.fromLngLats(sketchLineString.coordinates());
        } else if (sketchType == GeometryType.multiLine) {
            SketchMultiLineString sketchMultiLineString = (SketchMultiLineString) sketchGeometry;
            if (sketchMultiLineString == null) {
                return null;
            }
            return MultiLineString.fromLngLats(sketchMultiLineString.coordinates());
        } else if (sketchType == GeometryType.polygon) {
            SketchPolygon sketchPolygon = (SketchPolygon) sketchGeometry;
            if (sketchPolygon == null) {
                return null;
            }
            return SketchUtil.closeSketchPolygon(sketchPolygon);
        } else {
            SketchMultiPolygon sketchMultiPolygon = (SketchMultiPolygon) sketchGeometry;
            if (sketchMultiPolygon == null) {
                return null;
            }
            return SketchUtil.closeSketchMultiPolygon(sketchMultiPolygon);
        }
    }

    /**
     * 检查点击
     * 所有点首位都不不闭合
     *
     * @param pointF 屏幕坐标
     * @return
     */
    public GeoTouchAction checkGeoTouchMode(MapboxMap mapboxMap, PointF pointF) {
        GeoTouchAction geoTouchAction;
        if (sketchType == GeometryType.point) {
            SketchPoint sketchPoint = (SketchPoint) sketchGeometry;
            if (sketchPoint == null) {
                return null;
            }
            Point point = Point.fromLngLat(sketchPoint.point.longitude(), sketchPoint.point.latitude());
            SPointF sPointF = SketchUtil.geo0DimensionPoint(mapboxMap, point);
            geoTouchAction = GeoTouchAction.createEvent0(sketchType, pointF, sPointF);
        } else if (sketchType == GeometryType.multiPoint) {
            SketchMultiPoint sketchMultiPoint = (SketchMultiPoint) sketchGeometry;
            if (sketchMultiPoint == null) {
                return null;
            }
            List<Point> points = sketchMultiPoint.coordinates();
            List<SPointF> pointFS = SketchUtil.geo1DimensionList(mapboxMap, points);
            geoTouchAction = GeoTouchAction.createEvent1(sketchType, pointF, pointFS);
        } else if (sketchType == GeometryType.line) {
            SketchLineString sketchLineString = (SketchLineString) sketchGeometry;
            if (sketchLineString == null) {
                return null;
            }
            List<Point> points = sketchLineString.coordinates();
            List<SPointF> pointFS = SketchUtil.geo1DimensionList(mapboxMap, points);
            geoTouchAction = GeoTouchAction.createEvent1(sketchType, pointF, pointFS);
        } else if (sketchType == GeometryType.multiLine) {
            SketchMultiLineString sketchMultiLineString = (SketchMultiLineString) sketchGeometry;
            if (sketchMultiLineString == null) {
                return null;
            }
            List<List<Point>> points = sketchMultiLineString.coordinates();
            List<List<SPointF>> pointFS = SketchUtil.geo2DimensionList(mapboxMap, points);
            geoTouchAction = GeoTouchAction.createEvent2(sketchType, pointF, pointFS);
        } else if (sketchType == GeometryType.polygon) {
            SketchPolygon sketchPolygon = (SketchPolygon) sketchGeometry;
            if (sketchPolygon == null) {
                return null;
            }
            List<List<Point>> points = sketchPolygon.coordinates();
            List<List<SPointF>> pointFS = SketchUtil.geo2DimensionList(mapboxMap, points);
            geoTouchAction = GeoTouchAction.createEvent2(sketchType, pointF, pointFS);
        } else {
            SketchMultiPolygon sketchMultiPolygon = (SketchMultiPolygon) sketchGeometry;
            if (sketchMultiPolygon == null) {
                return null;
            }
            List<List<List<Point>>> points = sketchMultiPolygon.coordinates();
            List<List<List<SPointF>>> pointFS = SketchUtil.geo3DimensionList(mapboxMap, points);
            geoTouchAction = GeoTouchAction.createEvent3(sketchType, pointF, pointFS);
        }
        return geoTouchAction;
    }

    /**
     * 获取当前橡皮绳锚点
     *
     * @return
     */
    public Point[] getRubberAnchor() {
        return SketchUtil.computeRubberAnchor(sketchType, sketchGeometry, geoIp);
    }

    /**
     * 获取橡皮绳所构成的临时Geometry
     *
     * @return
     */
    public Geometry getRubberAnchorGeometry(Point point) {
        return SketchUtil.getRubberAnchorGeometry(sketchType, sketchGeometry, geoIp, point);
    }


}
