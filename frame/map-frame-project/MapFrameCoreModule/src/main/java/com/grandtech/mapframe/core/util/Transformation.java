package com.grandtech.mapframe.core.util;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import com.grandtech.mapframe.core.rules.Rules;
import com.grandtech.mapframe.core.sketch.geometry.LineSegment;
import com.mapbox.geojson.BoundingBox;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.GeoJson;
import com.mapbox.geojson.Geometry;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.MultiLineString;
import com.mapbox.geojson.MultiPoint;
import com.mapbox.geojson.MultiPolygon;
import com.mapbox.geojson.Point;
import com.mapbox.geojson.Polygon;
import com.mapbox.geojson.gson.BoundingBoxTypeAdapter;
import com.mapbox.mapboxsdk.annotations.Annotation;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.io.WKBReader;
import com.vividsolutions.jts.io.WKTReader;
import com.vividsolutions.jts.io.WKTWriter;
import com.vividsolutions.jts.operation.polygonize.Polygonizer;
import com.vividsolutions.jts.operation.union.CascadedPolygonUnion;
import com.vividsolutions.jts.operation.valid.IsValidOp;
import com.vividsolutions.jts.operation.valid.TopologyValidationError;

import org.geotools.geojson.geom.GeometryJSON;

import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by zy on 2018/4/12.
 */

public final class Transformation implements Rules {
    public final static float m2jwd = 0.00001f;//1????????????0.00001???
    final static GeometryJSON geometryJSON = new GeometryJSON(9);
    final static WKBReader wkbReader = new WKBReader();
    final static WKTWriter wktWriter = new WKTWriter();
    final static WKTReader wktReader = new WKTReader();

    /**
     * GeoJson???JTS Geometry
     *
     * @param geoJSON
     * @return
     */
    public static com.vividsolutions.jts.geom.Geometry geoJson2JstGeometry(GeoJson geoJSON) {
        try{
            if (geoJSON == null) {
                return null;
            }
            String _geoJSON = geoJSON.toJson();
            return geoJsonStr2JstGeometry(_geoJSON);
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }

    }

    public static GeoJson unionGeoJson(List<Feature> features) {
        if (features.size() == 0) {
            return null;
        }
        List geoList = new ArrayList();
        com.vividsolutions.jts.geom.Geometry geo;
        for (int i = 0; i < features.size(); i++) {
            if(features.get(i).geometry() instanceof Polygon ||features.get(i).geometry() instanceof MultiPolygon){
                geo = geoJson2JstGeometry(features.get(i).geometry());
                geo = validate(geo);
                if (geo == null) {
                    continue;
                }
                if (geo.isValid()) {
                    geoList.add(geo);
                }
            }
        }
        return jstGeometry2GeoJson(CascadedPolygonUnion.union(geoList));
    }

    public static Feature mergeFeatures(List<Feature> features) {
        if (features == null || features.size() == 0) {
            return null;
        }
        Geometry geometry = (Geometry) unionGeoJson(features);
        JsonObject attribute = features.get(0).properties().deepCopy();
        String id = features.get(0).id();
        BoundingBox boundingBox = features.get(0).bbox();
        return Feature.fromGeometry(geometry, attribute, id, boundingBox);
    }

    /**
     * ??????  ????????????
     *
     * @param features
     * @param geometry
     * @return
     */
    public static List<Feature> divisionFeatures(List<Feature> features, Geometry geometry) {
        if (geometry == null) {
            return features;
        }
        if (features == null) {
            return features;
        }
        try {
            List<Feature> res = new ArrayList<>();
            com.vividsolutions.jts.geom.Geometry cutGeometry = geoJson2JstGeometry(geometry);
            com.vividsolutions.jts.geom.Geometry objGeometry;
            for (int i = 0; i < features.size(); i++) {
                objGeometry = geoJson2JstGeometry(features.get(i).geometry());
                //modify by clj 20190316 ????????????????????????????????????????????????????????????????????????
                cutGeometry = cutGeometry.buffer(0.001 * m2jwd);
                objGeometry = objGeometry.difference(cutGeometry);
                if (objGeometry instanceof com.vividsolutions.jts.geom.MultiPolygon) {
                    for (int j = 0; j < objGeometry.getNumGeometries(); j++) {
                        res.add(Feature.fromGeometry((Geometry) jstGeometry2GeoJson(objGeometry.getGeometryN(j)), features.get(i).properties().deepCopy()));
                    }
                } else {
                    res.add(Feature.fromGeometry((Geometry) jstGeometry2GeoJson(objGeometry), features.get(i).properties().deepCopy()));
                }
            }
            return res;
        } catch (Exception e) {
            e.printStackTrace();
            return features;
        }
    }


    /**
     * ??????  ????????????,????????????????????????
     *
     * @param features
     * @param geometry
     * @return
     */
    static double buffer = 0.001 * m2jwd;

    public static List<Geometry> divFeatures(List<Feature> features, Geometry geometry) {
        try{
            List<Geometry> res = new ArrayList<>();
            if (geometry == null) {
                for (int i = 0; i < features.size(); i++) {
                    res.add(features.get(i).geometry());
                }
            }
            if (features == null) {
                return null;
            }
            com.vividsolutions.jts.geom.Geometry cutGeometry = geoJson2JstGeometry(geometry);
            cutGeometry = cutGeometry.buffer(buffer);
            com.vividsolutions.jts.geom.Geometry objGeometry;
            for (int i = 0; i < features.size(); i++) {
                objGeometry = geoJson2JstGeometry(features.get(i).geometry());
                objGeometry = objGeometry.difference(cutGeometry);
                if (objGeometry instanceof com.vividsolutions.jts.geom.MultiPolygon) {
                    for (int j = 0; j < objGeometry.getNumGeometries(); j++) {
                        res.add(jstGeometry2Geometry(objGeometry.getGeometryN(j)));
                    }
                } else {
                    res.add(jstGeometry2Geometry(objGeometry));
                }
            }
            return res;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }

    }


    public static Geometry diffFeature(Geometry geometry, Geometry diffGeometry) {
        try {
            com.vividsolutions.jts.geom.Geometry jtsOtherGeometry = geoJson2JstGeometry(diffGeometry);
            com.vividsolutions.jts.geom.Geometry jtsGeometry = geoJson2JstGeometry(geometry);
//          if(!jtsOtherGeometry.isValid()||!jtsGeometry.isValid())return null;
            // ?????????????????? MultiPolygon ????????????????????????????????????
            if(!jtsOtherGeometry.isValid()) {
                return null;
            }
            com.vividsolutions.jts.geom.Geometry jtsResultGeometry = jtsGeometry.difference(jtsOtherGeometry);
            return (Geometry) jstGeometry2GeoJson(jtsResultGeometry);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * GeoJson String ???JTS Geometry
     *
     * @param geoJSON
     * @return
     */
    public static com.vividsolutions.jts.geom.Geometry geoJsonStr2JstGeometry(String geoJSON) {
        if (geoJSON == null) {
            return null;
        }
        com.vividsolutions.jts.geom.Geometry geometry;
        Reader reader = new StringReader(geoJSON);
        try {
            geometry = geometryJSON.read(reader);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return geometry;
    }

    public static GeoJson geoJsonStr2GeoJson(String geoJSON) {
        com.vividsolutions.jts.geom.Geometry geometry = geoJsonStr2JstGeometry(geoJSON);
        return jstGeometry2GeoJson(geometry);
    }



    /**
     * jstGeometry???GeoJson
     *
     * @param geometry
     * @return
     */
    public static GeoJson jstGeometry2GeoJson(com.vividsolutions.jts.geom.Geometry geometry) {
        if (geometry == null) {
            return null;
        }
        String wkt = jstGeometry2Wkt(geometry);
        return wkt2BoxGeometry(wkt);
    }

    public static Geometry jstGeometry2Geometry(com.vividsolutions.jts.geom.Geometry geometry) {
        if (geometry == null) {
            return null;
        }
        return wkt2BoxGeometry(jstGeometry2Wkt(geometry));
    }

    public static String jstGeometry2Wkt(com.vividsolutions.jts.geom.Geometry geometry) {
        try {
            if (geometry == null) {
                return null;
            }
            StringWriter stringWriter = new StringWriter();
            wktWriter.write(geometry, stringWriter);
            String wkt = stringWriter.toString();
            return wkt;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * GeoJson???wkt
     *
     * @param geometry
     * @return
     */
    public static String boxGeometry2Wkt(GeoJson geometry) {
        if (geometry == null) {
            return null;
        }
        String _geoJSON = geometry.toJson();
        com.vividsolutions.jts.geom.Geometry _geometry = geoJsonStr2JstGeometry(_geoJSON);
        return _geometry.toText();
    }

    public static List<String> boxGeometry2Wkts(List<GeoJson> geometry) {
        List<String> wkts = new ArrayList<>();
        for (int i = 0; geometry != null && i < geometry.size(); i++) {
            wkts.add(boxGeometry2Wkt(geometry.get(i)));
        }
        return wkts;
    }

    public static List<String> boxGeometries2wKts(List<Geometry> geometries) {
        List<String> wkts = new ArrayList<>();
        for (int i = 0; geometries != null && i < geometries.size(); i++) {
            wkts.add(boxGeometry2Wkt(geometries.get(i)));
        }
        return wkts;
    }

    public static List<String> boxFeature2Wkts(List<Feature> features) {
        List<String> wkts = new ArrayList<>();
        for (int i = 0; features != null && i < features.size(); i++) {
            wkts.add(boxGeometry2Wkt(features.get(i).geometry()));
        }
        return wkts;
    }

    public static List<String> boxFeature2PointWkts(List<Feature> features) {
        List<String> wkts = new ArrayList<>();
        Geometry geometry;
        for (int i = 0; features != null && i < features.size(); i++) {
            geometry = features.get(i).geometry();
            BoxGeometryType type = getBoxGeometryType(geometry);
            if (type != BoxGeometryType.POINT) {
                wkts.add(boxGeometry2Wkt(geoJson2CenterPoint(geometry)));
            }
        }
        return wkts;
    }

    /**
     * WKB ??? GeoJson MultiPolygon
     *
     * @param bytes
     * @return
     * @throws Exception
     */
    public static GeoJson wkb2BoxMultiPolygon(byte[] bytes) throws Exception {
        StringWriter writer = new StringWriter();
        geometryJSON.write(wkbReader.read(bytes), writer);
        return MultiPolygon.fromJson(writer.toString());
    }

    /**
     * WKB ??? GeoJson MultiPolygon
     *
     * @param bytes
     * @return
     * @throws Exception
     */
    public static GeoJson wkb2BoxMultiPolygon(WKBReader geomReader, GeometryJSON geometryJSON, byte[] bytes) throws Exception {
        StringWriter writer = new StringWriter();
        geometryJSON.write(geomReader.read(bytes), writer);
        return MultiPolygon.fromJson(writer.toString());
    }

    /**
     * wkb???GeoJson?????????
     */
    public static String wkb2BoxJsonString(WKBReader geomReader, GeometryJSON geometryJSON, byte[] bytes) throws Exception {
        StringWriter writer = new StringWriter();
        geometryJSON.write(geomReader.read(bytes), writer);
        return writer.toString();
    }

    public static String wkb2BoxJsonString(byte[] bytes) throws Exception {
        StringWriter writer = new StringWriter();
        com.vividsolutions.jts.geom.Geometry geometry = wkbReader.read(bytes);
        geometryJSON.write(geometry, writer);
        return writer.toString();
    }

    /**
     * wkt???GeoJson
     *
     * @param wkt
     * @return
     */
    public static Geometry wkt2BoxGeometry(String wkt) {
        if (wkt == null) {
            return null;
        }
        String upperWkt = wkt.toUpperCase();
        try {
            if (upperWkt.startsWith("MULTIPOLYGON")) {
                return wkt2BoxMultiPolygon(wkt);
            }
            if (upperWkt.startsWith("POLYGON")) {
                return wkt2BoxPolygon(wkt);
            }
            if (upperWkt.startsWith("MULTIPOINT")) {
                return wkt2BoxMultiPoint(wkt);
            }
            if (upperWkt.startsWith("POINT")) {
                return wkt2BoxPoint(wkt);
            }
            if (upperWkt.startsWith("MULTILINESTRING")) {
                return wkt2BoxMultiLineString(wkt);
            }
            if (upperWkt.startsWith("LINESTRING")) {
                return wkt2BoxLineString(wkt);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static enum BoxGeometryType {
        MULTIPOLYGON,
        POLYGON,
        MULTIPOINT,
        POINT,
        MULTILINESTRING,
        LINESTRING
    }

    public static BoxGeometryType getBoxGeometryType(GeoJson geoJson) {
        String wkt = boxGeometry2Wkt(geoJson);
        if (wkt == null) {
            return null;
        }
        String upperWkt = wkt.toUpperCase();
        try {
            if (upperWkt.startsWith("MULTIPOLYGON")) {
                return BoxGeometryType.MULTIPOLYGON;
            }
            if (upperWkt.startsWith("POLYGON")) {
                return BoxGeometryType.POLYGON;
            }
            if (upperWkt.startsWith("MULTIPOINT")) {
                return BoxGeometryType.MULTIPOINT;
            }
            if (upperWkt.startsWith("POINT")) {
                return BoxGeometryType.POINT;
            }
            if (upperWkt.startsWith("MULTILINESTRING")) {
                return BoxGeometryType.MULTILINESTRING;
            }
            if (upperWkt.startsWith("LINESTRING")) {
                return BoxGeometryType.LINESTRING;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static List<LatLng> boxPolygon2Points(Geometry geometry) {
        if (geometry == null) {
            return null;
        }
        if (geometry instanceof Polygon) {
            List<LatLng> latLngs = new ArrayList<>();
            Polygon polygon = (Polygon) geometry;
            List<List<Point>> coordinates = polygon.coordinates();
            List<Point> tempList;
            Point tempPoint;
            for (int i = 0; i < coordinates.size(); i++) {
                tempList = coordinates.get(i);
                if (tempList == null) continue;
                for (int j = 0; j < tempList.size(); j++) {
                    tempPoint = tempList.get(j);
                    latLngs.add(new LatLng(tempPoint.latitude(), tempPoint.longitude()));
                }
            }
            return latLngs;
        }
        return null;
    }

    public static LatLngBounds wkt2BoxLatLngBounds(String wkt) {
        try {
            com.vividsolutions.jts.geom.Geometry geometry = wkt2JtsGeometry(wkt);
            com.vividsolutions.jts.geom.Envelope envelope = geometry.getEnvelopeInternal();
            LatLngBounds latLngBounds = LatLngBounds.from(envelope.getMaxY(), envelope.getMaxX(), envelope.getMinY(), envelope.getMinX());
            return latLngBounds;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static BoundingBox mergeBoxFeatureBound(double ratio, GeoJson... geoJsons) {
        if (geoJsons == null || geoJsons.length == 0) return null;
        GeoJson geoJson;
        BoundingBox boundingBox;
        double east = Double.MIN_VALUE, north = Double.MIN_VALUE, west = 180, south = 90;
        for (int i = 0; i < geoJsons.length; i++) {
            geoJson = geoJsons[i];
            boundingBox = getGeoJsonBox(geoJson);
            if (east < boundingBox.east()) {
                east = boundingBox.east();
            }
            if (north < boundingBox.north()) {
                north = boundingBox.north();
            }
            if (west > boundingBox.west()) {
                west = boundingBox.west();
            }
            if (south > boundingBox.south()) {
                south = boundingBox.south();
            }
        }
        BoundingBox box = BoundingBox.fromLngLats(west, south, east, north);
        return getBoxRatio(box, ratio);
    }

    public static BoundingBox getBoxRatio(BoundingBox boundingBox, double ratio) {
        double west = boundingBox.west();
        double east = boundingBox.east();
        double south = boundingBox.south();
        double north = boundingBox.north();
        double dixX, dixY;
        if (ratio >= 1) { //????????????
            dixX = (ratio - 1) * (east - west) / 2;
            dixY = (ratio - 1) * (north - south) / 2;
            return BoundingBox.fromLngLats(west - dixX, south - dixY, east + dixX, north + dixY);
        } else { //????????????
            dixX = (1 - ratio) * (east - west) / 2;
            dixY = (1 - ratio) * (north - south) / 2;
            return BoundingBox.fromLngLats(west + dixX, south + dixY, east - dixX, north - dixY);
        }
    }

    public static LatLngBounds geometry2BoxLatLngBounds(Geometry geometry) {
        try {
            com.vividsolutions.jts.geom.Geometry jstGeometry = geoJsonStr2JstGeometry(geometry.toJson());
            Envelope envelope = jstGeometry.getEnvelopeInternal();
            LatLngBounds latLngBounds = LatLngBounds.from(envelope.getMaxY(), envelope.getMaxX(), envelope.getMinY(), envelope.getMinX());
            return latLngBounds;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static LatLngBounds features2BoxLatLngBounds(List<Feature> features) {
        if (features == null || features.size() == 0) {
            return null;
        }
        try {
            Feature feature;
            com.vividsolutions.jts.geom.Geometry geometry;
            Envelope envelope;
            double maxX = -180, maxY = -90, minX = 180, minY = 90;
            for (int i = 0; i < features.size(); i++) {
                feature = features.get(i);
                geometry = geoJson2JstGeometry(feature.geometry());
                envelope = geometry.getEnvelopeInternal();
                if (maxX < envelope.getMaxX()) {
                    maxX = envelope.getMaxX();
                }
                if (maxY < envelope.getMaxY()) {
                    maxY = envelope.getMaxY();
                }
                if (minX > envelope.getMinX()) {
                    minX = envelope.getMinX();
                }
                if (minY > envelope.getMinY()) {
                    minY = envelope.getMinY();
                }
            }
            LatLngBounds latLngBounds = LatLngBounds.from(maxY + 0.0001, maxX + 0.0001, minY - 0.0001, minX - 0.0001);
            return latLngBounds;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static LatLngBounds wkts2BoxLatLngBounds(String... wkts) {
        if (wkts == null || wkts.length == 0) {
            return null;
        }
        try {
            String wkt;
            com.vividsolutions.jts.geom.Geometry geometry;
            Envelope envelope;
            double maxX = -180, maxY = -90, minX = 180, minY = 90;
            for (int i = 0; i < wkts.length; i++) {
                wkt = wkts[i];
                geometry = wkt2JtsGeometry(wkt);
                envelope = geometry.getEnvelopeInternal();
                if (maxX < envelope.getMaxX()) {
                    maxX = envelope.getMaxX();
                }
                if (maxY < envelope.getMaxY()) {
                    maxY = envelope.getMaxY();
                }
                if (minX > envelope.getMinX()) {
                    minX = envelope.getMinX();
                }
                if (minY > envelope.getMinY()) {
                    minY = envelope.getMinY();
                }
            }
            LatLngBounds latLngBounds = LatLngBounds.from(maxY, maxX, minY, minX);
            return latLngBounds;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static List<Geometry> wkts2BoxGeometry(String... wkts) {
        if (wkts == null || wkts.length == 0) {
            return null;
        }
        Geometry geometry;
        List<Geometry> geometries = new ArrayList<>();
        for (int i = 0; i < wkts.length; i++) {
            geometry = wkt2BoxGeometry(wkts[i]);
            geometries.add(geometry);
        }
        return geometries;
    }


    public static LatLngBounds feature2BoxLatLngBounds(Feature feature) {
        try {
            com.vividsolutions.jts.geom.Geometry geometry = geoJsonStr2JstGeometry(feature.geometry().toJson());
            Envelope envelope = geometry.getEnvelopeInternal();
            LatLngBounds latLngBounds = LatLngBounds.from(envelope.getMaxY(), envelope.getMaxX(), envelope.getMinY(), envelope.getMinX());
            return latLngBounds;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Geometry wkt2BoxPolygonCenter(String wkt) {
        try {
            com.vividsolutions.jts.geom.Geometry geometry = wkt2JtsGeometry(wkt);
            com.vividsolutions.jts.geom.Point point = geometry.getCentroid();
            return Point.fromLngLat(point.getX(), point.getY());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static com.vividsolutions.jts.geom.Geometry wkt2JtsGeometry(String wkt) throws Exception {
        return wktReader.read(wkt);
    }

    /**
     * WKT ??? GeoJson MultiPolygon
     *
     * @param wkt
     * @return
     * @throws Exception
     */
    public static MultiPolygon wkt2BoxMultiPolygon(String wkt) throws Exception {
        StringWriter writer = new StringWriter();
        geometryJSON.write(wktReader.read(wkt), writer);
        return MultiPolygon.fromJson(writer.toString());
    }

    public static Polygon wkt2BoxPolygon(String wkt) throws Exception {
        Writer writer = new StringWriter();
        geometryJSON.write(wktReader.read(wkt), writer);
        return Polygon.fromJson(writer.toString());
    }

    public static Point wkt2BoxPoint(String wkt) throws Exception {
        StringWriter writer = new StringWriter();
        geometryJSON.write(wktReader.read(wkt), writer);
        return Point.fromJson(writer.toString());
    }

    public static MultiPoint wkt2BoxMultiPoint(String wkt) throws Exception {
        StringWriter writer = new StringWriter();
        geometryJSON.write(wktReader.read(wkt), writer);
        return MultiPoint.fromJson(writer.toString());
    }

    public static LineString wkt2BoxLineString(String wkt) throws Exception {
        StringWriter writer = new StringWriter();
        geometryJSON.write(wktReader.read(wkt), writer);
        return LineString.fromJson(writer.toString());
    }

    public static MultiLineString wkt2BoxMultiLineString(String wkt) throws Exception {
        StringWriter writer = new StringWriter();
        geometryJSON.write(wktReader.read(wkt), writer);
        return MultiLineString.fromJson(writer.toString());
    }

    public static Feature _2Feature(String wkt, Map<String, Object> map) {
        Geometry geometry = wkt2BoxGeometry(wkt);
        JsonObject properties = map2JsonObject(map);
        return Feature.fromGeometry(geometry, properties);
    }

    /**
     * ??????????????? GeoJson
     *
     * @param annotation
     * @return
     */
    public static GeoJson boxAnnotation2Geometry(Annotation annotation) {
        Geometry geometry = null;
        if (annotation == null) {
            return null;
        }
        if (annotation instanceof com.mapbox.mapboxsdk.annotations.Polygon) {
            com.mapbox.mapboxsdk.annotations.Polygon polygon = (com.mapbox.mapboxsdk.annotations.Polygon) annotation;

            List<LatLng> outs = polygon.getPoints();
            List<List<LatLng>> holes = polygon.getHoles();
            List<List<Point>> coordinates = new ArrayList<>();
            LatLng outLatLngs;
            List<LatLng> latLngs;
            List<Point> points;
            points = new ArrayList<>();
            for (int i = 0; i < outs.size(); i++) {
                outLatLngs = outs.get(i);
                points.add(Point.fromLngLat(outLatLngs.getLongitude(), outLatLngs.getLatitude(), outLatLngs.getAltitude()));
            }
            outLatLngs = outs.get(0);
            points.add(Point.fromLngLat(outLatLngs.getLongitude(), outLatLngs.getLatitude(), outLatLngs.getAltitude()));
            coordinates.add(points);
            for (int i = 0; i < holes.size(); i++) {
                points = new ArrayList<>();
                latLngs = holes.get(i);
                for (int k = 0; k < latLngs.size(); k++) {
                    points.add(Point.fromLngLat(latLngs.get(k).getLongitude(), latLngs.get(k).getLatitude(), latLngs.get(k).getAltitude()));
                }
                coordinates.add(points);
            }
            geometry = Polygon.fromLngLats(coordinates);
        }
        if (annotation instanceof com.mapbox.mapboxsdk.annotations.Polyline) {
            com.mapbox.mapboxsdk.annotations.Polyline polyline = (com.mapbox.mapboxsdk.annotations.Polyline) annotation;
            List<LatLng> list = polyline.getPoints();
            List<Point> coordinates = new ArrayList<>();
            for (int i = 0; i < list.size(); i++) {
                coordinates.add(Point.fromLngLat(list.get(i).getLongitude(), list.get(i).getLatitude(), list.get(i).getAltitude()));
            }
            geometry = LineString.fromLngLats(coordinates);
        }

        if (annotation instanceof com.mapbox.mapboxsdk.annotations.Marker) {
            com.mapbox.mapboxsdk.annotations.Marker marker = (com.mapbox.mapboxsdk.annotations.Marker) annotation;
            LatLng position = marker.getPosition();
            geometry = Point.fromLngLat(position.getLongitude(), position.getLatitude(), position.getAltitude());
        }
        return geometry;
    }

    public static GeoJson listPoint2SimplePolygon(List<LatLng> latLngs) {
        if (latLngs == null || latLngs.size() < 3) return null;
        Geometry geometry = null;
        List<List<Point>> coordinates = new ArrayList<>();
        List<Point> points = new ArrayList<>();
        for (int k = 0; k < latLngs.size(); k++) {
            points.add(Point.fromLngLat(latLngs.get(k).getLongitude(), latLngs.get(k).getLatitude(), latLngs.get(k).getAltitude()));
        }
        coordinates.add(points);
        geometry = Polygon.fromLngLats(coordinates);
        return geometry;
    }

    /**
     * ???????????????????????????
     *
     * @param geometry
     * @return
     */
    public static int checkGeometry(Geometry geometry) {
        com.vividsolutions.jts.geom.Geometry jtsGeometry = geoJson2JstGeometry(geometry);
        if (jtsGeometry == null) {
            return -1;
        }
        boolean isCorrect = IsValidOp.isValid(jtsGeometry);
        if (isCorrect) {
            return 100;
        }else {
            IsValidOp isValidOp = new IsValidOp(jtsGeometry);
            TopologyValidationError validationError = isValidOp.getValidationError();
            return validationError.getErrorType();
        }
    }

    public final static String[] GEOMETRY_ERROR_TYPE = new String[]{"??????????????????", "?????????", "????????????", "????????????", "????????????", "??????????????????", "?????????", "?????????", "?????????", "??????????????????????????????", "????????????", "????????????"};

    /**
     * ??????????????????
     *
     * @param points
     * @return
     */
    public static int checkGeometry(List<LatLng> points) {
        GeoJson geoJson = listPoint2SimplePolygon(points);
        com.vividsolutions.jts.geom.Geometry jtsGeometry = geoJson2JstGeometry(geoJson);
        System.out.println("wkt:" + jstGeometry2Wkt(jtsGeometry));
        if (jtsGeometry == null) {
            return -1;
        }
        boolean isCorrect = IsValidOp.isValid(jtsGeometry);
        if (isCorrect) {
            return 100;
        } else {
            IsValidOp isValidOp = new IsValidOp(jtsGeometry);
            TopologyValidationError validationError = isValidOp.getValidationError();
            return validationError.getErrorType();
        }
    }

    public static boolean isGeoLegal(Geometry geometry) {
        try {
            com.vividsolutions.jts.geom.Geometry jtsGeometry = geoJson2JstGeometry(geometry);
            return jtsGeometry.isValid();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * ??????Geometries????????????
     *
     * @param tree
     * @return
     */
    public static boolean checkGeometriesIntersect(List<Geometry> tree) {

        com.vividsolutions.jts.geom.Geometry jtsGeometry = null;
        com.vividsolutions.jts.geom.Geometry otherJtsGeometry = null;

        //Stack<>????????????
        List<Geometry> searchTree = new ArrayList<>();
        List<Geometry> searchRes = new ArrayList<>();
        //???????????????
        int startIndex = 0;
        Geometry rootNode = tree.get(startIndex);
        searchTree.add(rootNode);
        Geometry nodeData;
        Geometry tempNode;
        while (searchTree.size() != 0) {
            nodeData = searchTree.get(startIndex);
            searchRes.add(nodeData);
            searchTree.remove(startIndex);
            for (int i = 0; i < tree.size(); i++) {
                tempNode = tree.get(i);
                if (ListUtil.listHasItem(searchRes, tempNode)) {
                    continue;
                }
                jtsGeometry = geoJson2JstGeometry(nodeData);
                otherJtsGeometry = geoJson2JstGeometry(tempNode);
                if (jtsGeometry.intersects(otherJtsGeometry)) {
                    if (ListUtil.listHasItem(searchTree, tempNode)) {
                        continue;
                    }
                    searchTree.add(0, tempNode);
                }
            }
        }
        return searchRes.size() == tree.size();
    }
    /**
     * ??????????????????????????????????????????????????????30%?????????
     *
     * @param tree
     * @return
     */
    public static List<com.vividsolutions.jts.geom.Geometry> getCheckGeometrieIntersect(Geometry geometry, List<Feature> tree) {
        if(tree==null) {
            return null;
        }
        List<com.vividsolutions.jts.geom.Geometry> geometries = new ArrayList<>();
        com.vividsolutions.jts.geom.Geometry jtsGeometry = geoJson2JstGeometry(geometry);
        for(Feature feature :tree){
            com.vividsolutions.jts.geom.Geometry otherJtsGeometry = geoJson2JstGeometry(feature.geometry());
            boolean intersects = jtsGeometry.intersects(otherJtsGeometry);
            if(intersects) {
                geometries.add(otherJtsGeometry);
            }
        }
        return geometries;
    }

    public static boolean checkGeometrieIntersect(Geometry geometry, List<Feature> tree) {
        if(tree==null) {
            return false;
        }
        com.vividsolutions.jts.geom.Geometry jtsGeometry = geoJson2JstGeometry(geometry);
        for(Feature feature :tree){
            com.vividsolutions.jts.geom.Geometry otherJtsGeometry = geoJson2JstGeometry(feature.geometry());
            boolean intersects = jtsGeometry.intersects(otherJtsGeometry);
            if(intersects) {
                return true;
            }
        }
        return false;
    }

    /**
     * ???????????????????????????
     * @param geometry
     * @param tree
     * @return
     */
    public static boolean checkGeometrieIntersectArea(Geometry geometry, List<Feature> tree) {
        if(tree==null) {
            return false;
        }
        com.vividsolutions.jts.geom.Geometry jtsGeometry = geoJson2JstGeometry(geometry);
        jtsGeometry =jtsGeometry.buffer(-2* m2jwd);
       // double d =  Pro4jUtil.pro84To2000(jtsGeometry).getArea();
        for(Feature feature :tree){
            com.vividsolutions.jts.geom.Geometry otherJtsGeometry = geoJson2JstGeometry(feature.geometry());
            boolean  b= jtsGeometry.overlaps(otherJtsGeometry);
           /* try{
                double inArea = Pro4jUtil.pro84To2000(inG).getArea();
                if((d-inArea)/d>fz)return true;
            }catch (Exception e){
                e.printStackTrace();
            }*/
           if(b) {
               return true;
           }
        }
        return false;
    }

    /**
     * ??????????????????
     * ??????????????????????????????????????????
     * @param renderedFeatures
     * @param
     * @return
     */
    public static double getGeometrieIntersectArea(Feature feature, List<Feature> renderedFeatures) {
        if (renderedFeatures == null) {
            return 0d;
        }
        com.vividsolutions.jts.geom.Geometry jtsGeometry = geoJson2JstGeometry(feature.geometry());
        if (jtsGeometry == null) {
            return 0d;
        }
        double area = 0d;

        for (Feature renderedFeature : renderedFeatures) {
            com.vividsolutions.jts.geom.Geometry otherJtsGeometry = geoJson2JstGeometry(renderedFeature.geometry());
            if (otherJtsGeometry == null) {
                continue;
            }
            try {
                // ???????????????????????????
                com.vividsolutions.jts.geom.Geometry intersection = jtsGeometry.intersection(otherJtsGeometry);
                if (intersection == null) {
                    continue;
                }
                intersection = Pro4jUtil.pro84To2000(intersection);
                //????????????
                double inArea = intersection.getArea() * 0.0015;
                //double inArea = intersection.getArea();

                area = BigDecimal.valueOf(area).add(BigDecimal.valueOf(inArea)).doubleValue();
            } catch (Exception e){
                e.printStackTrace();
                return 0d;
            }
        }
        return area;
    }


    /**
     * ????????????
     * ???????????????????????????????????? ???????????????(y??)???
     * @param geometry
     * @param renderedFeatures
     * @return
     */
    public static boolean checkGeometrieIntersectArea1(Geometry geometry, List<Feature> renderedFeatures) {
        double yz = 10/100d;
        if (renderedFeatures == null) {
            return false;
        }
        com.vividsolutions.jts.geom.Geometry jtsGeometry = geoJson2JstGeometry(geometry);
        if (jtsGeometry == null) {
            return false;
        }
        double area = Pro4jUtil.pro84To2000(jtsGeometry).getArea();

        for (Feature renderedFeature : renderedFeatures) {
            com.vividsolutions.jts.geom.Geometry otherJtsGeometry = geoJson2JstGeometry(renderedFeature.geometry());
            if (otherJtsGeometry == null) {
                continue;
            }
            double renderedArea = Pro4jUtil.pro84To2000(otherJtsGeometry).getArea();
            // ??????
            //jtsGeometry.contains(otherJtsGeometry);
            //otherJtsGeometry.contains(jtsGeometry);
            // ??????
            //boolean b = jtsGeometry.overlaps(otherJtsGeometry);
            //if (b) {
                // ???????????????????????????
            com.vividsolutions.jts.geom.Geometry intersection = jtsGeometry.intersection(otherJtsGeometry);
                if (intersection == null) {
                    continue;
                }
                try {
                    //double inArea = Pro4jUtil.pro84To2000(intersection).getArea();
                    double inArea = intersection.getArea();
                    if (area > renderedArea) {
                        if (inArea/renderedArea > yz) {
                            return true;
                        }
                    } else {
                        if (inArea/area > yz) {
                            return true;
                        }
                    }
                } catch (Exception e){
                    e.printStackTrace();
                }
            //}
        }
        return false;
    }

    /**
     * ????????????
     * ?????????????????????????????????????????? ????????????????????????
     * @param renderedFeatures
     * @param yz  ?????????(y??)???
     * @return
     */
    public static boolean checkGeometrieIntersectArea(Feature feature, List<Feature> renderedFeatures, double yz) {
        if (renderedFeatures == null) {
            return false;
        }
        com.vividsolutions.jts.geom.Geometry jtsGeometry = geoJson2JstGeometry(feature.geometry());
        if (jtsGeometry == null) {
            return false;
        }
        double area = Pro4jUtil.pro84To2000(jtsGeometry).getArea();

        for (Feature renderedFeature : renderedFeatures) {
            Log.i("aaa  fromLayerId  ", feature.getStringProperty("fromLayerId") + "  " + renderedFeature.getStringProperty("fromLayerId"));
            Log.i("aaa  objectid     ", feature.getStringProperty("objectid") + "  " + renderedFeature.getStringProperty("objectid"));
            // ??????????????????
            if (renderedFeature.hasProperty("fromLayerId") && renderedFeature.getStringProperty("fromLayerId").equals("????????????")) {
                continue;
            }
            if (renderedFeature.hasProperty("fromLayerId") && renderedFeature.getStringProperty("fromLayerId").equals("tempSource_polygon_querylayer")) {
                if (feature.hasProperty("objectid") && renderedFeature.hasProperty("objectid")) {
                    if (feature.getNumberProperty("objectid").doubleValue() == renderedFeature.getNumberProperty("objectid").doubleValue()) {
                        continue;
                    }
                }
            }
            Log.i("????????????  ??????=", yz +
                    "  objectid=" + renderedFeature.getStringProperty("objectid") +
                    "  dkbm=" + renderedFeature.getStringProperty("dkbm") +
                    "  mj=" + renderedFeature.getStringProperty("mj") +
                    "  area=" + compute84GeoJsonArea(renderedFeature.geometry()));

            com.vividsolutions.jts.geom.Geometry otherJtsGeometry = geoJson2JstGeometry(renderedFeature.geometry());
            if (otherJtsGeometry == null) {
                continue;
            }
            double renderedArea = Pro4jUtil.pro84To2000(otherJtsGeometry).getArea();
            try {
                // ???????????????????????????
                com.vividsolutions.jts.geom.Geometry intersection = jtsGeometry.intersection(otherJtsGeometry);
                if (intersection == null) {
                    continue;
                }
                double inArea = intersection.getArea();
                if (area > renderedArea) {
                    DecimalFormat df = new DecimalFormat("0.00");
                    Log.i("????????????  ", df.format(inArea* 0.0015) + "  /" + df.format(renderedArea* 0.0015));

                    if (inArea/renderedArea > yz) {
                        return true;
                    }
                } else {
                    DecimalFormat df = new DecimalFormat("0.00");
                    Log.i("????????????  ", df.format(inArea* 0.0015) + "  /" + df.format(area* 0.0015));

                    if (inArea/area > yz) {
                        return true;
                    }
                }
            } catch (Exception e){
                e.printStackTrace();
                return false;
            }
        }
        return false;
    }
    /**
     * ????????????
     * ?????????????????????????????????????????? ????????????????????????
     * @param yz  ?????????(y??)???
     * @return
     */
    public static boolean checkGeometrieIntersectArea(Feature feature1, Feature feature2, double yz) {
        com.vividsolutions.jts.geom.Geometry jtsGeometry1 = geoJson2JstGeometry(feature1.geometry());
        com.vividsolutions.jts.geom.Geometry jtsGeometry2 = geoJson2JstGeometry(feature2.geometry());
        if (jtsGeometry1 == null) {
            return false;
        }
        if (jtsGeometry2 == null) {
            return false;
        }
        try {
            double area1 = Pro4jUtil.pro84To2000(jtsGeometry1).getArea();
            double area2 = Pro4jUtil.pro84To2000(jtsGeometry2).getArea();
            // ???????????????????????????
            com.vividsolutions.jts.geom.Geometry intersection = jtsGeometry1.intersection(jtsGeometry2);
            if (intersection == null) {
                return false;
            }
            double inArea = intersection.getArea();
            if (area1 > area2) {
                DecimalFormat df = new DecimalFormat("0.00");
                Log.i("????????????  ", df.format(inArea* 0.0015) + "  /" + df.format(area2* 0.0015));

                if (inArea/area2 > yz) {
                    return true;
                }
            } else {
                DecimalFormat df = new DecimalFormat("0.00");
                Log.i("????????????  ", df.format(inArea* 0.0015) + "  /" + df.format(area1* 0.0015));

                if (inArea/area1 > yz) {
                    return true;
                }
            }
        } catch (Exception e){
            e.printStackTrace();
            return false;
        }
        return false;
    }

    public static com.vividsolutions.jts.geom.Geometry mergeGeometries(List<Geometry> geometries, double tolerance) {
        try {
            List<String> indexList = new ArrayList<>();
            List< com.vividsolutions.jts.geom.Geometry> geoList = new ArrayList<>();
            tolerance = tolerance * m2jwd / 2;//????????????????????????

            for (int i = 0; i < geometries.size(); i++) {
                com.vividsolutions.jts.geom.Geometry jtsGeometry1 = geoJson2JstGeometry(geometries.get(i));
                jtsGeometry1 = jtsGeometry1.buffer(tolerance);
                geoList.add(jtsGeometry1);
                boolean bValid = false;
                if (indexList.contains(String.format("%s", i))) {
                    continue;
                }
                for (int j = 0; j < geometries.size(); j++) {
                    if (i == j) {
                        continue;
                    }
                    com.vividsolutions.jts.geom.Geometry jtsGeometry2 = geoJson2JstGeometry(geometries.get(j));
                    jtsGeometry2 = jtsGeometry2.buffer(tolerance);
                    boolean b = jtsGeometry1.intersects(jtsGeometry2);
                    if (b) {
                        indexList.add(String.format("%s", i));
                        indexList.add(String.format("%s", j));
                        bValid = true;
                        break;
                    }
                }
                if (!bValid) {
                    return null;//????????????????????????????????????????????????
                }
            }
            //???????????????
            com.vividsolutions.jts.geom.Geometry unionGeo = CascadedPolygonUnion.union(geoList);
            unionGeo = unionGeo.buffer(-tolerance);
            return unionGeo;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * ???????????????
     *
     * @param geometry
     * @return
     */
    public static GeoJson rectifyGeometry(GeoJson geometry) {
        com.vividsolutions.jts.geom.Geometry jtsGeometry = geoJson2JstGeometry(geometry);
        /*if(jtsGeometry instanceof com.vividsolutions.jts.geom.Polygon){
            com.vividsolutions.jts.geom.Polygon polygon = (com.vividsolutions.jts.geom.Polygon) jtsGeometry;
            polygon.normalize();
        }*/
        jtsGeometry.normalize();
        return jstGeometry2GeoJson(jtsGeometry);
    }

    public static BoundingBox boxGeometry2BoxLatLngBounds(Geometry geo) {
        try {
            com.vividsolutions.jts.geom.Geometry geometry = wkt2JtsGeometry(boxGeometry2Wkt(geo));
            Envelope envelope = geometry.getEnvelopeInternal();
            BoundingBox latLngBounds = BoundingBox.fromLngLats(envelope.getMinX(), envelope.getMinY(), envelope.getMaxX(), envelope.getMaxY());
            return latLngBounds;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Double compute84GeoJsonArea(Object geoJson) {
        Double area = null;
        if (geoJson == null) {
            return null;
        }
        Geometry geometry = null;
        if (geoJson instanceof Feature) {
            Feature feature = (Feature) geoJson;
            geometry = feature.geometry();
        } else if (geoJson instanceof Geometry) {
            geometry = (Geometry) geoJson;
        } else if (geoJson instanceof String) {
            geometry = (Geometry) geoJsonStr2GeoJson(geoJson.toString());
        }
        if (geometry == null) {
            return null;
        }
        com.vividsolutions.jts.geom.Geometry jtsGeometry = geoJson2JstGeometry(geometry);
        if (jtsGeometry == null) {
            return null;
        }
        //84???2000
        jtsGeometry = Pro4jUtil.pro84To2000(jtsGeometry);
        //????????????
        area = jtsGeometry.getArea() * 0.0015;
        return area;
    }

    /**
     * ????????????
     * @param geoJson
     * @return
     */
    public static Double compute84GeoJsonLen(Object geoJson) {
        Double len = null;
        if (geoJson == null) {
            return null;
        }
        Geometry geometry = null;
        if (geoJson instanceof Feature) {
            Feature feature = (Feature) geoJson;
            geometry = feature.geometry();
        } else if (geoJson instanceof Geometry) {
            geometry = (Geometry) geoJson;
        } else if (geoJson instanceof String) {
            geometry = (Geometry) geoJsonStr2GeoJson(geoJson.toString());
        }
        if (geometry == null) {
            return null;
        }
        com.vividsolutions.jts.geom.Geometry jtsGeometry = geoJson2JstGeometry(geometry);
        if (jtsGeometry == null) {
            return null;
        }
        //84???2000
        jtsGeometry = Pro4jUtil.pro84To2000(jtsGeometry);
        //???
        len = jtsGeometry.getLength();
        return len;
    }
    /**
     * ????????????????????????0??????1???
     * @param geoJson
     * @return
     */
    public static Double[] compute84GeoJsonLenAndArea(Object geoJson) {
        Double len = null;
        Double area = null;
        if (geoJson == null) {
            return null;
        }
        Geometry geometry = null;
        if (geoJson instanceof Feature) {
            Feature feature = (Feature) geoJson;
            geometry = feature.geometry();
        } else if (geoJson instanceof Geometry) {
            geometry = (Geometry) geoJson;
        } else if (geoJson instanceof String) {
            geometry = (Geometry) geoJsonStr2GeoJson(geoJson.toString());
        }
        if (geometry == null) {
            return null;
        }
        com.vividsolutions.jts.geom.Geometry jtsGeometry = geoJson2JstGeometry(geometry);
        if (jtsGeometry == null) {
            return null;
        }
        //84???2000
        jtsGeometry = Pro4jUtil.pro84To2000(jtsGeometry);
        //???
        len = jtsGeometry.getLength();
        area = jtsGeometry.getArea() * 0.0015;
        return new Double[]{len,area};
    }
    /**
     * ??????????????????????????????Geometry?????????
     *
     * @param geometry
     * @return
     */
    public static Double[] compute84GeoJsonRunTime(Geometry geometry) {
        try{
            Double arg0 = null;
            Double arg1 = null;
            com.vividsolutions.jts.geom.Geometry jtsGeometry;
            if (geometry == null) {
                return null;
            }
            //geometry??????????????????????????????????????????
            if (geometry instanceof Polygon || geometry instanceof MultiPolygon) {
                Point[] points = geom2PointArray(geometry);
                if (points != null && points.length == 3) {
                    List<Point> line = new ArrayList<>();
                    line.add(points[1]);
                    line.add(points[2]);
                    LineString lineString = LineString.fromLngLats(line);
                    jtsGeometry = geoJson2JstGeometry(lineString);
                    jtsGeometry = Pro4jUtil.pro84To2000(jtsGeometry);
                    arg0 = jtsGeometry == null ? 0 : jtsGeometry.getLength();
                    return new Double[]{arg0, arg1};
                }
            }

            jtsGeometry = geoJson2JstGeometry(geometry);
            if (jtsGeometry == null) {
                return null;
            }
            jtsGeometry = Pro4jUtil.pro84To2000(jtsGeometry);


            //????????????
            if (jtsGeometry instanceof com.vividsolutions.jts.geom.Polygon
                    || jtsGeometry instanceof com.vividsolutions.jts.geom.MultiPolygon) {
                arg1 = jtsGeometry.getArea() * 0.0015;
            }

            //????????????
            if (jtsGeometry instanceof com.vividsolutions.jts.geom.LineString
                    || jtsGeometry instanceof com.vividsolutions.jts.geom.MultiLineString) {
                arg1 = jtsGeometry.getLength();
            }

            if (!(jtsGeometry instanceof com.vividsolutions.jts.geom.Point)
                    || !(jtsGeometry instanceof com.vividsolutions.jts.geom.MultiPoint)) {
               // com.vividsolutions.jts.geom.LineSegment lineSegment = getGeometryLastSegment(jtsGeometry);
                arg0 = jtsGeometry == null ? 0 : jtsGeometry.getLength();
            }


            return new Double[]{arg0, arg1};
        }catch (Exception e){
            e.printStackTrace();
        }
      return null;
    }

    public static com.vividsolutions.jts.geom.LineSegment getGeometryLastSegment(com.vividsolutions.jts.geom.Geometry geometry) {
        if (geometry == null) {
            return null;
        }
        if (geometry instanceof com.vividsolutions.jts.geom.Polygon) {
            return getLineStringLastSegment((com.vividsolutions.jts.geom.Polygon) geometry);
        }
        if (geometry instanceof com.vividsolutions.jts.geom.LineString) {
            com.vividsolutions.jts.geom.LineString lineString = (com.vividsolutions.jts.geom.LineString) geometry;
            int num = lineString.getCoordinates().length;
            return new com.vividsolutions.jts.geom.LineSegment(lineString.getCoordinateN(num - 1), lineString.getCoordinateN(num - 2));
        }
        return null;
    }

    public static com.vividsolutions.jts.geom.LineSegment getLineStringLastSegment(com.vividsolutions.jts.geom.Polygon polygon) {
        int inRingCount = polygon.getNumInteriorRing();
        com.vividsolutions.jts.geom.LineString lineString;
        if (inRingCount == 0) {
            lineString = polygon.getExteriorRing();
        } else {
            lineString = polygon.getInteriorRingN(inRingCount - 1);
        }
        int num = lineString.getCoordinates().length;
        com.vividsolutions.jts.geom.LineSegment lineSegment = new com.vividsolutions.jts.geom.LineSegment(lineString.getCoordinateN(num - 2), lineString.getCoordinateN(num - 3));
        return lineSegment;
    }

    public static GeoJson geoJson2CenterPoint(GeoJson geoJson) {
        if (geoJson == null) {
            return null;
        }
        com.vividsolutions.jts.geom.Geometry geometry = geoJson2JstGeometry(geoJson);
        if (geometry == null) {
            return null;
        }
        com.vividsolutions.jts.geom.Geometry center = geometry.getCentroid();
        return jstGeometry2GeoJson(center);
    }
    public static com.vividsolutions.jts.geom.Geometry geoJson2CenterJtsGeometry(GeoJson geoJson) {
        if (geoJson == null) {
            return null;
        }
        com.vividsolutions.jts.geom.Geometry geometry = geoJson2JstGeometry(geoJson);
        if (geometry == null) {
            return null;
        }
        com.vividsolutions.jts.geom.Geometry center = geometry.getCentroid();
        return center;
    }
    //????????????????????????????????????????????????????????????????????????
    public static String calTextAnchor(Point lblPt, Point centPt) {
        String anchor = "center";
        double x, y, cx, cy;
        x = lblPt.longitude();
        y = lblPt.latitude();
        cx = centPt.longitude();
        cy = centPt.latitude();

        if (x <= cx) {
            if (y <= cy) {
                anchor = "top-right";
            } else {
                anchor = "bottom-right";
            }
        } else {
            if (y <= cy) {
                anchor = "top-left";
            } else {
                anchor = "bottom-left";
            }
        }
        return anchor;
    }

    public static String geoJson2NodeSymbol(GeoJson geoJson, String centLabel) {
        if (geoJson == null) {
            return null;
        }
        if (geoJson instanceof Feature) {
            geoJson = ((Feature) geoJson).geometry();
        }
        com.vividsolutions.jts.geom.Geometry _geometry = geoJson2JstGeometry(geoJson);
        if (_geometry == null) {
            return null;
        }

        //???????????????????????????????????????
        com.vividsolutions.jts.geom.Geometry _convexHull = _geometry.convexHull();
        Coordinate[] nodeCoordinate = _convexHull.getCoordinates();

        List<Feature> list = new ArrayList<>();
        Feature feature;
        Point point, centPt;
        JsonObject jsonObject;
        centPt = Point.fromLngLat(_convexHull.getCentroid().getX(), _convexHull.getCentroid().getY());//???????????????
        //??????????????????????????????????????????
        if (centLabel != "") {
            jsonObject = new JsonObject();
            jsonObject.addProperty("label", centLabel);
            jsonObject.addProperty("anchor", "center");
            feature = Feature.fromGeometry(centPt, jsonObject);
            list.add(feature);//?????????????????????
        }
        for (int i = 0; i < nodeCoordinate.length - 1; i++) {
            point = Point.fromLngLat(nodeCoordinate[i].x, nodeCoordinate[i].y);
            jsonObject = new JsonObject();
            jsonObject.addProperty("label", String.format("%.6f\n%.6f", point.longitude(), point.latitude()));
            jsonObject.addProperty("anchor", calTextAnchor(point, centPt));
            feature = Feature.fromGeometry(point, jsonObject);
            list.add(feature);
        }
        FeatureCollection featureCollection = FeatureCollection.fromFeatures(list);
        return featureCollection.toJson();
    }
    public static FeatureCollection geoJson2NodeSymbolFeatureCollection(GeoJson geoJson, String centLabel) {
        if (geoJson == null) {
            return null;
        }
        if (geoJson instanceof Feature) {
            geoJson = ((Feature) geoJson).geometry();
        }
        com.vividsolutions.jts.geom.Geometry _geometry = geoJson2JstGeometry(geoJson);
        if (_geometry == null) {
            return null;
        }

        //???????????????????????????????????????
        com.vividsolutions.jts.geom.Geometry _convexHull = _geometry.convexHull();
        Coordinate[] nodeCoordinate = _convexHull.getCoordinates();

        List<Feature> list = new ArrayList<>();
        Feature feature;
        Point point, centPt;
        JsonObject jsonObject;
        centPt = Point.fromLngLat(_convexHull.getCentroid().getX(), _convexHull.getCentroid().getY());//???????????????
        //??????????????????????????????????????????
        if (centLabel != "") {
            jsonObject = new JsonObject();
            jsonObject.addProperty("label", centLabel);
            jsonObject.addProperty("anchor", "center");
            feature = Feature.fromGeometry(centPt, jsonObject);
            list.add(feature);//?????????????????????
        }
        for (int i = 0; i < nodeCoordinate.length - 1; i++) {
            point = Point.fromLngLat(nodeCoordinate[i].x, nodeCoordinate[i].y);
            jsonObject = new JsonObject();
            jsonObject.addProperty("label", String.format("%.6f\n%.6f", point.longitude(), point.latitude()));
            jsonObject.addProperty("anchor", calTextAnchor(point, centPt));
            feature = Feature.fromGeometry(point, jsonObject);
            list.add(feature);
        }
        FeatureCollection featureCollection = FeatureCollection.fromFeatures(list);
        return featureCollection;
    }
    /**
     * ??????source
     * @param geoJsons
     * @param Label
     * @return
     */
    public static String geoJson2NodeSymbolLabel(List<Geometry> geoJsons, String Label) {
        if (geoJsons == null) {
            return null;
        }
        List<Feature> features = new ArrayList<>();
        for(Geometry geoJson : geoJsons){
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("label", Label);
            Feature feature = Feature.fromGeometry(geoJson, jsonObject);
            features.add(feature);
        }
        FeatureCollection featureCollection = FeatureCollection.fromFeatures(features);
        return featureCollection.toJson();
    }
    /**
     * @param feature
     * @param co
     * @return
     */
    public static String getVByFeature(Feature feature, String co) {
        if (feature == null) {
            return "";
        }
        if (feature.hasProperty(co)) {
            return feature.getStringProperty(co);
        } else {
            return "";
        }

    }

    /**
     * ????????????????????????
     *
     * @param wkt
     * @param x   ,y
     * @return
     */
    public static boolean isWithinJTS(String wkt, double x, double y) {
        try {
            GeometryFactory geometryFactory = new GeometryFactory();
            com.vividsolutions.jts.geom.Geometry geometry =wkt2JtsGeometry(wkt);
            Coordinate coord = new Coordinate(x, y);
            com.vividsolutions.jts.geom.Point point = geometryFactory.createPoint(coord);
            return point.within(geometry);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * ????????????????????????
     *
     * @param geometry
     * @param point
     * @return
     */
    public static boolean isWithinJTS(Geometry geometry, Point point) throws Exception {
        GeometryFactory geometryFactory = new GeometryFactory();
        com.vividsolutions.jts.geom.Geometry geometryJts = wkt2JtsGeometry(boxGeometry2Wkt(geometry));
        Coordinate coord = new Coordinate(point.longitude(), point.latitude());
        com.vividsolutions.jts.geom.Point pointJts = geometryFactory.createPoint(coord);
        return pointJts.within(geometryJts);
    }

    public static Geometry geometryTry2MultiGeometry(Geometry geometry) {
        if (geometry == null) {
            return null;
        }
        String type = geometry.type();
        if (type.equals("MultiPoint") || type.equals("MultiLineString") || type.equals("MultiPolygon")) {
            return geometry;
        }
        if (type.equals("Point")) {
            List<Point> points = new ArrayList<>();
            points.add((Point) geometry);
            geometry = MultiPoint.fromLngLats(points);
        }
        if (type.equals("LineString")) {
            geometry = MultiLineString.fromLineString((LineString) geometry);
        }
        if (type.equals("Polygon")) {
            geometry = MultiPolygon.fromPolygon((Polygon) geometry);
        }
        return geometry;
    }


    public static Point[] geom2PointArray(Geometry geometry) {
        List<Point> points = new ArrayList<>();
        if (geometry == null) {
            return null;
        }
        //geometry??????????????????????????????????????????
        if ((geometry instanceof MultiPolygon)) {
            List<List<List<Point>>> ps = ((MultiPolygon) geometry).coordinates();
            List<List<Point>> temp;
            for (int i = 0, lenI = ps.size(); i < lenI; i++) {
                temp = ps.get(i);
                for (int j = 0, lenJ = temp.size(); j < lenJ; j++) {
                    points.addAll(temp.get(j));
                }
            }
        }
        if ((geometry instanceof Polygon)) {
            List<List<Point>> ps = ((Polygon) geometry).coordinates();
            for (int i = 0, lenI = ps.size(); i < lenI; i++) {
                points.addAll(ps.get(i));
            }
        }
        if ((geometry instanceof MultiLineString)) {
            List<List<Point>> ps = ((MultiLineString) geometry).coordinates();
            for (int i = 0, lenI = ps.size(); i < lenI; i++) {
                points.addAll(ps.get(i));
            }
        }
        if ((geometry instanceof LineString)) {
            points = ((LineString) geometry).coordinates();
        }
        if ((geometry instanceof MultiPoint)) {
            points = ((MultiPoint) geometry).coordinates();
        }
        if ((geometry instanceof Point)) {
            points.add((Point) geometry);
        }
        Point[] ps = CollectUtil.list2TArray(points, Point.class);
        return ps;
    }

    /**
     * ????????????????????????9???
     *
     * @param mapBoxObj
     * @return
     */
    public static String mapBoxObj2Json(Object mapBoxObj) {
        if (mapBoxObj == null) {
            return null;
        }
        Gson gson =GsonFactory.getFactory().getComMapBoxGson();
        if (mapBoxObj instanceof Feature) {
            Feature feature = (Feature) mapBoxObj;
            //if (feature.properties().size() == 0)
                //feature = Feature.fromGeometry(feature.geometry(), null, feature.id(), feature.bbox());
            return gson.toJson(feature);
        } else if (mapBoxObj instanceof Point) {
            return ((Point) mapBoxObj).toJson();

        } else if (mapBoxObj instanceof FeatureCollection) {
            return ((FeatureCollection) mapBoxObj).toJson();
        }else {
            return gson.toJson(mapBoxObj);
        }
    }

    public static String mapBoxObj2Json(Object mapBoxObj, int precision) {
        if (mapBoxObj == null) {
            return null;
        }
        GsonBuilder gson = new GsonBuilder();
        gson.registerTypeAdapter(Point.class, new CustomPointSerializer(precision));
        gson.registerTypeAdapter(BoundingBox.class, new BoundingBoxTypeAdapter());
        if (mapBoxObj instanceof Feature) {
            Feature feature = (Feature) mapBoxObj;
            if (feature.properties().size() == 0) {
                feature = Feature.fromGeometry(feature.geometry(), null, feature.id(), feature.bbox());
            }
            return gson.create().toJson(feature);
        } else if (mapBoxObj instanceof Point) {
            return ((Point) mapBoxObj).toJson();
        } else if (mapBoxObj instanceof FeatureCollection) {
            return ((FeatureCollection) mapBoxObj).toJson();
        }else {
            return gson.create().toJson(mapBoxObj);
        }
    }


    /**
     * ************************************************?????????????????????????????????????????????*********************************************
     */

    public static JsonObject map2JsonObject(Map<String, Object> map) {
        JsonObject properties = new JsonObject();
        Object val;
        for (String key : map.keySet()) {
            val = map.get(key);
            if (val == null) {
                continue;
            }
            if (val instanceof String) {
                properties.addProperty(key, (String) val);
            }
            if (val instanceof Double) {
                properties.addProperty(key, (Double) val);
            }
            if (val instanceof Float) {
                properties.addProperty(key, (Float) val);
            }
            if (val instanceof Integer) {
                properties.addProperty(key, (Integer) val);
            }
            if (val instanceof Number) {
                properties.addProperty(key, (Number) val);
            }
            if (val instanceof Boolean) {
                properties.addProperty(key, (Boolean) val);
            }
            if (val instanceof Character) {
                properties.addProperty(key, (Character) val);
            }
        }
        return properties;
    }


    //??????Json
    public static Object analysisJsonObject(JsonObject jsonObject) {
        if (jsonObject == null || jsonObject.isJsonNull()) {
            return null;
        }
        JsonElement jsonElement;
        for (String key : jsonObject.keySet()) {
            jsonElement = jsonObject.get(key);
            System.out.println("-------------------------------------");
            System.out.println(key + ":");
            Object val = analysisJsonElement(jsonElement);
        }
        return null;
    }


    /**
     * analysisJsonArray ??????JsonElement
     *
     * @param jsonArray
     * @return
     */
    public static Object analysisJsonArray(JsonArray jsonArray) {
        if (jsonArray == null || jsonArray.isJsonNull()) {
            return null;
        }
        Iterator<JsonElement> jsonArrayElements = jsonArray.iterator();
        JsonElement jsonElementItem;
        while (jsonArrayElements.hasNext()) {
            jsonElementItem = jsonArrayElements.next();
            analysisJsonElement(jsonElementItem);
        }
        return null;
    }

    public static Object analysisJsonElement(JsonElement jsonElement) {
        if (jsonElement == null || jsonElement.isJsonNull()) {
            return null;
        }
        if (jsonElement.isJsonObject()) {
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            analysisJsonObject(jsonObject);
        }
        if (jsonElement.isJsonArray()) {
            JsonArray jsonArray = jsonElement.getAsJsonArray();
            analysisJsonArray(jsonArray);
        }
        if (jsonElement.isJsonPrimitive()) {
            JsonPrimitive jsonPrimitive = jsonElement.getAsJsonPrimitive();
            return analysisJsonPrimitive(jsonPrimitive);
        }
        return null;
    }

    public static Object analysisJsonPrimitive(JsonPrimitive jsonPrimitive) {
        if (jsonPrimitive == null || jsonPrimitive.isJsonNull()) {
            return null;
        }
        if (jsonPrimitive.isJsonArray()) {
            analysisJsonArray(jsonPrimitive.getAsJsonArray());
            return null;
        }
        if (jsonPrimitive.isJsonObject()) {
            analysisJsonObject(jsonPrimitive.getAsJsonObject());
            return null;
        }
        System.out.println(jsonPrimitive);
        if (jsonPrimitive.isNumber()) {
            return jsonPrimitive.getAsNumber();
        }
        if (jsonPrimitive.isString()) {
            return jsonPrimitive.getAsString();
        }
        if (jsonPrimitive.isBoolean()) {
            return jsonPrimitive.getAsBoolean();
        }
        return null;
    }
    public static Point comWkt2BoxPoint(Geometry geometry) {
        if (geometry instanceof Point) {
            return (Point) geometry;
        } else {
            geometry = (Geometry) geoJson2CenterPoint(geometry);
            return (Point) geometry;
        }
    }


    public static List<BoundingBox> getBoxRatios(List<Feature> list, double ratio) {
        if (list == null) {
            return null;
        }
        List<BoundingBox> boxes = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            boxes.add(getBoxRatio(list.get(i), ratio));
        }
        return boxes;
    }
    /**
     * ??????????????????(???????????????????????????????????????)
     *
     * @param geoJson
     * @param ratio   ?????????(??????)
     * @return
     */
    public static BoundingBox getBoxRatio(GeoJson geoJson, double ratio) {
        BoundingBox boundingBox = getGeoJsonBox(geoJson);
        double minX = boundingBox.west();
        double maxX = boundingBox.east();
        double minY = boundingBox.south();
        double maxY = boundingBox.north();

        double dixX, dixY;
        if (ratio >= 1) { //????????????
            dixX = (ratio - 1) * (maxX - minX) / 2;
            dixY = (ratio - 1) * (maxY - minY) / 2;
            return BoundingBox.fromLngLats(minX - dixX, minY - dixY, maxX + dixX, maxY + dixY);
        } else { //????????????
            dixX = (1 - ratio) * (maxX - minX) / 2;
            dixY = (1 - ratio) * (maxY - minY) / 2;
            return BoundingBox.fromLngLats(minX + dixX, minY + dixY, maxX - dixX, maxY - dixY);
        }
    }

    public static BoundingBox getGeoJsonBox(GeoJson geoJson) {
        GeoJson _geoJson = null;
        if (geoJson instanceof Feature) {
            _geoJson = ((Feature) geoJson).geometry();
        } else if (geoJson instanceof Geometry) {
            _geoJson = geoJson;
        }
        if (_geoJson == null) {
            return null;
        }
        com.vividsolutions.jts.geom.Geometry geometry = geoJson2JstGeometry(_geoJson);
        Envelope envelope = null;
        if (geometry instanceof com.vividsolutions.jts.geom.Point) {
            com.vividsolutions.jts.geom.Point point = (com.vividsolutions.jts.geom.Point) geometry;
        } else {
            envelope = geometry.getEnvelopeInternal();
        }
        return BoundingBox.fromLngLats(envelope.getMinX(), envelope.getMinY(), envelope.getMaxX(), envelope.getMaxY());
    }


    /**
     * ???Geometry?????????
     * * Get / create a valid version of the geometry given. If the geometry is a polygon or multi polygon, self intersections
     * * inconsistencies are fixed. Otherwise the geometry is returned.
     * * @param geom * @return a geometry
     **/

    public static com.vividsolutions.jts.geom.Geometry validate(com.vividsolutions.jts.geom.Geometry geom) {
        if (geom instanceof com.vividsolutions.jts.geom.Polygon) {
            if (geom.isValid()) {
                geom.normalize();
                return geom;
            }
            Polygonizer polygonizer = new Polygonizer();
            addPolygon((com.vividsolutions.jts.geom.Polygon) geom, polygonizer);
            List<com.vividsolutions.jts.geom.Polygon> polygons = new ArrayList<>(polygonizer.getPolygons());
            return toPolygonGeometry(polygons, geom.getFactory());
        } else if (geom instanceof com.vividsolutions.jts.geom.MultiPolygon) {
            if (geom.isValid()) {
                geom.normalize();
                return geom;
            }
            Polygonizer polygonizer = new Polygonizer();
            for (int n = geom.getNumGeometries(); n-- > 0; ) {
                addPolygon((com.vividsolutions.jts.geom.Polygon) geom.getGeometryN(n), polygonizer);
            }
            return toMultiPolygonGeometry(polygonizer.getPolygons(), geom.getFactory());
        } else {
            return geom;
        }
    }

    /**
     * * Add all line strings from the polygon given to the polygonizer given
     * * @param polygon polygon from which to extract line strings
     * * @param polygonizer polygonizer
     */
    private static void addPolygon(com.vividsolutions.jts.geom.Polygon polygon, Polygonizer polygonizer) {
        addLineString(polygon.getExteriorRing(), polygonizer);
        for (int n = polygon.getNumInteriorRing(); n-- > 0; ) {
            addLineString(polygon.getInteriorRingN(n), polygonizer);
        }
    }

    /**
     * Add the linestring given to the polygonizer
     *
     * @param lineString  line string
     * @param polygonizer polygonizer
     */
    private static void addLineString(com.vividsolutions.jts.geom.LineString lineString, Polygonizer polygonizer) {
        if (lineString instanceof LinearRing) {
            lineString = lineString.getFactory().createLineString(lineString.getCoordinateSequence());
            com.vividsolutions.jts.geom.Point point = lineString.getFactory().createPoint(lineString.getCoordinateN(0));
            com.vividsolutions.jts.geom.Geometry toAdd = lineString.union(point);
            polygonizer.add(toAdd);
        }
    }

    /**
     * * Get a geometry from a collection of polygons.
     * * @param polygons collection
     * * @param factory factory to generate MultiPolygon if required
     * * @return null if there were no polygons, the polygon if there was only one, or a MultiPolygon containing all polygons otherwise
     */
    private static com.vividsolutions.jts.geom.Geometry toPolygonGeometry(List<com.vividsolutions.jts.geom.Polygon> polygons, GeometryFactory factory) {
        switch (polygons.size()) {
            case 0:
                return null;
            case 1:
                return polygons.iterator().next();
            default:
                LinearRing outShell = simplePolygon2LinearRing(polygons.get(0));
                polygons.remove(0);
                LinearRing[] inRing = simplePolygon2LinearRings(polygons);
                return factory.createPolygon(outShell, inRing);
        }
    }

    private static com.vividsolutions.jts.geom.Geometry toMultiPolygonGeometry(Collection<com.vividsolutions.jts.geom.Polygon> polygons, GeometryFactory factory) {
        switch (polygons.size()) {
            case 0:
                return null;
            case 1:
                return polygons.iterator().next();
            default:
                return factory.createMultiPolygon(polygons.toArray(new com.vividsolutions.jts.geom.Polygon[polygons.size()]));
        }
    }

    public static LinearRing simplePolygon2LinearRing(com.vividsolutions.jts.geom.Polygon polygon) {
        com.vividsolutions.jts.geom.LineString lineString = polygon.getExteriorRing();
        return new LinearRing(lineString.getCoordinateSequence(), polygon.getFactory());
    }

    public static LinearRing[] simplePolygon2LinearRings(List<com.vividsolutions.jts.geom.Polygon> polygons) {
        LinearRing[] rings = new LinearRing[polygons.size()];
        for (int i = 0; i < polygons.size(); i++) {
            rings[i] = simplePolygon2LinearRing(polygons.get(i));
        }
        return rings;
    }

    /**
     * ????????????buffer??????????????????
     * @param wkt
     * @param zoom
     * @param buffer
     * @return int
     */
    public static Integer wktTransfWidthHeightPx(String wkt,double zoom,int buffer){
        try {
            com.vividsolutions.jts.geom.Geometry geometry = wkt2JtsGeometry(wkt);
            geometry = geometry.buffer(buffer*m2jwd);
            Envelope envelope = geometry.getEnvelopeInternal();
            Double maxX = envelope.getMaxX();
            Double minX = envelope.getMinX();
            Double maxY = envelope.getMaxY();
            Double minY = envelope.getMinY();
            maxX = MercatorProjection.longitudeToPixelX(maxX,(byte)zoom);
            minX = MercatorProjection.longitudeToPixelX(minX,(byte)zoom);
            maxY = MercatorProjection.latitudeToPixelY(maxY,(byte)zoom);
            minY = MercatorProjection.latitudeToPixelY(minY,(byte)zoom);
            return maxX-minX>minY-maxY?new Double(maxX-minX).intValue():new Double(minY-maxY).intValue();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static com.vividsolutions.jts.geom.Geometry buffer(com.vividsolutions.jts.geom.Geometry geometry,int buffer){
        geometry = geometry.buffer(buffer*m2jwd);
        return geometry;
    }
}
