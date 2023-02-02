package com.grandtech.mapframe.core.util;


import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;


import org.osgeo.proj4j.CRSFactory;
import org.osgeo.proj4j.CoordinateReferenceSystem;
import org.osgeo.proj4j.CoordinateTransform;
import org.osgeo.proj4j.CoordinateTransformFactory;
import org.osgeo.proj4j.ProjCoordinate;
import org.osgeo.proj4j.io.Proj4FileReader;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by zy on 2018/7/16.
 */

public final class Pro4jUtil {


    private final static String _wgs84_param = "+title=long/lat:WGS84 +proj=longlat +ellps=WGS84 +datum=WGS84 +units=degress";

    /**
     * 中国区分带
     */
    public final static Map<Integer, String> WKID = new SimpleMap<Integer, String>()
            .push(75, "4513").push(78, "4514").push(81, "4515").push(84, "4516")
            .push(87, "4517").push(90, "4518").push(93, "4519").push(96, "4520")
            .push(99, "4521").push(102, "4522").push(105, "4523").push(108, "4524")
            .push(111, "4525").push(114, "4526").push(117, "4527").push(120, "4528")
            .push(123, "4529").push(126, "4530").push(129, "4531").push(132, "4532")
            .push(135, "4533");


    /**
     * 快速计算面积（提升效率）
     *
     * @param latLngs
     * @return
     */
    static CRSFactory _crsFactory = new CRSFactory();
    static CoordinateReferenceSystem _wgs84 = _crsFactory.createFromParameters("WGS84", _wgs84_param);
    static Proj4FileReader _proj4FileReader = new Proj4FileReader();
    static CoordinateTransformFactory _ctf = new CoordinateTransformFactory();


    /**
     * 计算投影带
     *
     * @param lon
     * @return
     */
    public static String getsId(Double lon) {
        lon += 1.5;//加上1.5度，换算3度带带号
        int blt = (int) Math.floor(lon / 3);//3度带号
        int midLon = blt * 3;//中央经线
        return WKID.get(midLon);
    }

    /**
     * 84点投影到2000上
     *
     * @param latLng
     * @return
     */
    public final static Map<String, String[]> proParamCache = new HashMap<>();
    public final static Map<String, CoordinateReferenceSystem> proRefCache = new HashMap<>();
    public final static Map<String, CoordinateTransform> proTrfCache = new HashMap<>();

    public static Coordinate pro84To2000(String sID, Coordinate coordinate) {
        try {
            if (proParamCache.get(sID) == null) {
                proParamCache.put(sID, _proj4FileReader.readParametersFromFile("epsg", sID));
            }
        } catch (Exception e) {
            return null;
        }
        String[] paramPoj = proParamCache.get(sID);
        if (proRefCache.get(sID) == null) {
            proRefCache.put(sID, _crsFactory.createFromParameters(sID, paramPoj));
        }
        CoordinateReferenceSystem target = proRefCache.get(sID);
        if (proTrfCache.get(sID) == null) {
            proTrfCache.put(sID, _ctf.createTransform(_wgs84, target));
        }
        CoordinateTransform transform = proTrfCache.get(sID);
        if (transform != null) {
            ProjCoordinate proCoordinate84 = new ProjCoordinate(coordinate.x, coordinate.y);
            ProjCoordinate proCoordinate20000 = new ProjCoordinate();
            transform.transform(proCoordinate84, proCoordinate20000);
            coordinate.x = proCoordinate20000.x;
            coordinate.y = proCoordinate20000.y;
            return coordinate;
        }
        return null;

    }

    /**
     * 84投影Geometry转2000
     *
     * @param geometry
     * @return
     */
    public static Geometry pro84To2000(Geometry geometry) {
        if (geometry == null) {
            return null;
        }
        Point center = geometry.getCentroid();
        String sID = getsId(center.getX());
        Coordinate[] coordinates = geometry.getCoordinates();
        Coordinate c;
        for (int i = 0, len = coordinates.length; i < len; i++) {
            c = coordinates[i];
            pro84To2000(sID, c);
        }
        return geometry;
    }

}
