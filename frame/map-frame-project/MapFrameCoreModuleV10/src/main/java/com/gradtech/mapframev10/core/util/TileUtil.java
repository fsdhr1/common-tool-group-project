package com.gradtech.mapframev10.core.util;

import com.mapbox.geojson.BoundingBox;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class TileUtil {

    public static String parseXyz2Bound(long x, long y, long z) {
        StringBuilder sb = new StringBuilder("POLYGON ((");
        byte b = (byte) z;
        double lngLeft = MercatorProjection.tileXToLongitude(x, b);
        double latUp = MercatorProjection.tileYToLatitude(y, b);
        double lngRight = MercatorProjection.tileXToLongitude(x + 1, b);
        double latDown = MercatorProjection.tileYToLatitude(y + 1, b);
        sb.append(lngLeft).append(" ").append(latUp).append(", ");
        sb.append(lngRight).append(" ").append(latUp).append(", ");
        sb.append(lngRight).append(" ").append(latDown).append(", ");
        sb.append(lngLeft).append(" ").append(latDown).append(", ");
        sb.append(lngLeft).append(" ").append(latUp).append(")) ");
        return sb.toString();
    }

    /**
     * 瓦片的Envelope 地理坐标的
     *
     * @param x
     * @param y
     * @param z
     * @return
     */
    public static Envelope xyz2Envelope(long x, long y, long z) {
        byte b = (byte) z;
        double lngLeft = MercatorProjection.tileXToLongitude(x, b);
        double latUp = MercatorProjection.tileYToLatitude(y, b);
        double lngRight = MercatorProjection.tileXToLongitude(x + 1, b);
        double latDown = MercatorProjection.tileYToLatitude(y + 1, b);
        return new Envelope(lngLeft, lngRight, latUp, latDown);
    }

    /**
     * 瓦片的Envelope 像素坐标的
     *
     * @param x
     * @param y
     * @param z
     * @return
     */
    public static Envelope xyz2Envelope2Pixel(long x, long y, long z) {
        byte b = (byte) z;
        double lngLeft = MercatorProjection.tileXToLongitude(x, b);
        double latUp = MercatorProjection.tileYToLatitude(y, b);
        double lngRight = MercatorProjection.tileXToLongitude(x + 1, b);
        double latDown = MercatorProjection.tileYToLatitude(y + 1, b);

        double px = MercatorProjection.tileXToPixelX(x);
        double py = MercatorProjection.tileYToPixelY(y);

        lngLeft = ((MercatorProjection.longitudeToPixelX(lngLeft, b)) - px) * 16;
        lngRight = ((MercatorProjection.longitudeToPixelX(lngRight, b)) - px) * 16;
        latUp = ((MercatorProjection.latitudeToPixelY(latUp, b)) - py) * 16;
        latDown = ((MercatorProjection.latitudeToPixelY(latDown, b)) - py) * 16;
        return new Envelope(lngLeft, lngRight, latUp, latDown);
    }

    /**
     * 瓦片的Envelope 像素坐标的
     *
     * @param tiles

     * @return
     */
    public static Envelope xyzs2Envelope2Pixel(List<long[]> tiles) {
        double maxX=0;
        double minX=0;
        double maxY=0;
        double minY=0;
        for(long[] tile : tiles){
            double px = MercatorProjection.tileXToPixelX(tile[1]);
            double py = MercatorProjection.tileYToPixelY(tile[2]);
            double px1 = MercatorProjection.tileXToPixelX(tile[1]+1);
            double py2 = MercatorProjection.tileYToPixelY(tile[2]+1);
            maxX = px1>maxX?px1:maxX;
            minX = px<minX||minX==0?px:minX;
            maxY = py2>maxY?py2:maxY;
            minY = py<minY||minY==0?py:minY;
        }


        return new Envelope(minX, maxX, minY, maxY);
    }
    /**
     * 瓦片的Envelope 像素坐标的
     *
     * @param tile
     * @return
     */
    public static Envelope xyz2Envelope2Pixel(long[] tile) {
        double minX = MercatorProjection.tileXToPixelX(tile[1]);
        double minY = MercatorProjection.tileYToPixelY(tile[2]);
        double maxX = MercatorProjection.tileXToPixelX(tile[1]+1);
        double maxY = MercatorProjection.tileYToPixelY(tile[2]+1);
        return new Envelope(minX, maxX, minY, maxY);
    }
    /**
     * 瓦片的Envelope 像素坐标的
     *
     * @param boundingBox
     * @return
     */
    public static Envelope bound2PixelEnvelope(BoundingBox boundingBox,long zoom) {
        byte b = (byte) zoom;
        double lngLeft = MercatorProjection.longitudeToPixelX(boundingBox.west(), b);
        double latUp = MercatorProjection.latitudeToTileY(boundingBox.north(), b);
        double lngRight = MercatorProjection.longitudeToPixelX(boundingBox.east(), b);
        double latDown = MercatorProjection.latitudeToTileY(boundingBox.south(), b);

        return new Envelope(lngLeft, lngRight, latUp, latDown);
    }

    /**
     * 地理Geometry转换成瓦片上的像素Geometry
     *
     * @param x
     * @param y
     * @param z
     * @param geom
     */
    public static void convert2Pixel(long x, long y, long z, Geometry geom) {
        double px = MercatorProjection.tileXToPixelX(x);
        double py = MercatorProjection.tileYToPixelY(y);
        Coordinate[] cs = geom.getCoordinates();
        byte zoom = (byte) z;
        Coordinate c;
        for (int i = 0, len = cs.length; i < len; i++) {
            c = cs[i];
            c.x = (int) ((MercatorProjection.longitudeToPixelX(c.x, zoom)) - px) * 16;
            c.y = (int) ((MercatorProjection.latitudeToPixelY(c.y, zoom)) - py) * 16;
        }
    }

    /**
     * 抽稀要素
     *
     * @param x
     * @param y
     * @param z
     * @param geom
     * @param buffer
     */
    public static void convert2Pixel(long x, long y, long z, Geometry geom, int buffer) {
        double px = MercatorProjection.tileXToPixelX(x);
        double py = MercatorProjection.tileYToPixelY(y);
        Coordinate[] cs = geom.getCoordinates();
        byte zoom = (byte) z;
        for (Coordinate c : cs) {
            c.x = (int) ((MercatorProjection.longitudeToPixelX(c.x, zoom)) - px) * buffer;
            c.y = (int) ((MercatorProjection.latitudeToPixelY(c.y, zoom)) - py) * buffer;
        }
    }

    public static String parseXyz2Bound(int x, int y, int z) {
        StringBuilder sb = new StringBuilder("POLYGON ((");
        byte b = (byte) z;
        double lngLeft = MercatorProjection.tileXToLongitude(x, b) - 0.00105;
        double latUp = MercatorProjection.tileYToLatitude(y, b) + 0.00105;
        double lngRight = MercatorProjection.tileXToLongitude(x + 1, b) + 0.00105;
        double latDown = MercatorProjection.tileYToLatitude(y + 1, b) - 0.00105;
        sb.append(lngLeft + " " + latUp + ", ");
        sb.append(lngRight + " " + latUp + ", ");
        sb.append(lngRight + " " + latDown + ", ");
        sb.append(lngLeft + " " + latDown + ", ");
        sb.append(lngLeft + " " + latUp + ")) ");
        return sb.toString();
    }


    /**
     * 判断一个瓦片是否咋某个经纬度范围
     *
     * @param z
     * @param x
     * @param y
     * @param west
     * @param south
     * @param east
     * @param north
     * @return
     */
    public static boolean isInTileRegion(long z, long x, long y, double west, double south, double east, double north) {
        byte b = (byte) z;
        long sx = MercatorProjection.longitudeToTileX(west, b);
        long ey = MercatorProjection.latitudeToTileY(south, b);
        long ex = MercatorProjection.longitudeToTileX(east, b);
        long sy = MercatorProjection.latitudeToTileY(north, b);
        return (sx <= x && ex >= x && sy <= y && ey >= y);
    }


    /**
     * @param west
     * @param south
     * @param east
     * @param north
     * @param z
     * @return
     */
    public static List<long[]> getBoundTile(double west, double south, double east, double north, long z) {
        long minX, maxX, minY, maxY;
        byte b = (byte) z;
        minX = MercatorProjection.longitudeToTileX(west, b);
        maxX = MercatorProjection.longitudeToTileX(east, b);
        minY = MercatorProjection.latitudeToTileY(north, b);
        maxY = MercatorProjection.latitudeToTileY(south, b);
        List<long[]> tiles = new ArrayList();
        for (long x = minX; x <= maxX; x++) {
            for (long y = minY; y <= maxY; y++) {
                tiles.add(new long[]{z, x, y});
            }
        }
        return tiles;
    }

    /**
     * @param west
     * @param south
     * @param east
     * @param north
     * @param z
     * @return
     */
    public static List<long[]> getBoundTileV2(double west, double south, double east, double north, long z) {
        long minX, maxX, minY, maxY;
        byte b = (byte) z;
        double pminx = MercatorProjection.longitudeToPixelX(west,b)-100;
        double pmaxX = MercatorProjection.longitudeToPixelX(east,b)+100;
        double pminY = MercatorProjection.latitudeToPixelY(north,b)-100;
        double pmaxY = MercatorProjection.latitudeToPixelY(south,b)+100;
        if(pmaxX-pminx>pmaxY-pminY){
            double cz = (pmaxX-pminx) - (pmaxY-pminY);
            pmaxY = pmaxY+cz/2;
            pminY = pminY-cz/2;
        }else {
            double cz =  (pmaxY-pminY)-(pmaxX-pminx);
            pmaxX = pmaxX+cz/2;
            pminx = pminx-cz/2;
        }
        minX = MercatorProjection.pixelXToTileX(pminx,b);
        maxX = MercatorProjection.pixelXToTileX(pmaxX,b);
        minY = MercatorProjection.pixelYToTileY(pminY,b);
        maxY = MercatorProjection.pixelYToTileY(pmaxY, b);
        List<long[]> tiles = new ArrayList<long[]>();
        for (long x = minX; x <= maxX; x++) {
            for (long y = minY; y <= maxY; y++) {
                tiles.add(new long[]{z, x, y});
            }
        }
        return tiles;
    }

    /**
     * 获取 z级别下boxes范围内的  瓦片
     *
     * @param boxes
     * @param z
     * @return
     */
    public static List<long[]> getBoundTile(List<BoundingBox> boxes, long z) {
        BoundingBox box;
        List<long[]> temp;
        Map<String, long[]> tiles = new HashMap<>();
        long[] tile;
        for (int i = 0, lenI = boxes.size(); i < lenI; i++) {
            box = boxes.get(i);
            temp = getBoundTile(box.west(), box.south(), box.east(), box.north(), z);
            for (int j = 0, lenJ = temp.size(); j < lenJ; j++) {
                tile = temp.get(j);
                tiles.put(tile[0] + "_" + tile[1] + "_" + tile[2], tile);
            }
        }
        List<long[]> res = new ArrayList<>();
        Iterator<long[]> iterator = tiles.values().iterator();
        while (iterator.hasNext()) {
            res.add(iterator.next());
        }
        return res;
    }

    /**
     * 计算minZ - maxZ 的局部瓦片
     *
     * @param boxes
     * @param minZ
     * @param maxZ
     * @return
     */
    public static Map<String, List<long[]>> getTreeBoundTile(List<BoundingBox> boxes, long minZ, long maxZ) {
        Map<Long, List<long[]>> tiles = new LinkedHashMap<>();
        List<long[]> temp;
        for (long z = minZ; z <= maxZ; z++) {
            temp = TileUtil.getBoundTile(boxes, z);
            tiles.put(z, temp);
        }
        Map<String, List<long[]>> treeTiles = new LinkedHashMap<>();
        List<long[]> roots = tiles.get(minZ);
        List<long[]> nodes;
        long rootX, rootY;
        long pow;
        long x, y;
        long[] tile;
        StringBuilder index = new StringBuilder();
        for (int i = 0, len = roots.size(); i < len; i++) {
            nodes = new ArrayList<>();
            nodes.add(roots.get(i));
            rootX = roots.get(i)[1];
            rootY = roots.get(i)[2];
            for (long z = minZ + 1; z <= maxZ; z++) {
                temp = tiles.get(z);
                for (int j = 0, lenJ = temp.size(); j < lenJ; j++) {
                    //pow = ((Double) Math.pow(2, z - minZ)).longValue();
                    pow = 2 << (z - minZ - 1);
                    tile = temp.get(j);
                    x = tile[1];
                    y = tile[2];
                    if ((x / pow == rootX) && (y / pow == rootY)) {
                        nodes.add(tile);
                    }
                }
            }
            index.setLength(0);
            index.append(roots.get(i)[0]).append("_").append(roots.get(i)[1]).append("_").append(roots.get(i)[2]);
            treeTiles.put(index.toString(), nodes);
        }
        return treeTiles;
    }

    public static List<long[]> getTreeBoundTileV1(List<BoundingBox> boxes, long minZ, long maxZ) {
        Map<Long, List<long[]>> tiles = new LinkedHashMap<>();
        List<long[]> temp;
        for (long z = minZ; z <= maxZ; z++) {
            temp = TileUtil.getBoundTile(boxes, z);
            tiles.put(z, temp);
        }
        List<long[]> treeTiles = new ArrayList<>();
        List<long[]> roots = tiles.get(minZ);
        long rootX, rootY;
        long pow;
        long x, y;
        long[] tile;
        for (int i = 0, len = roots.size(); i < len; i++) {
            treeTiles.add(roots.get(i));
            rootX = roots.get(i)[1];
            rootY = roots.get(i)[2];
            for (long z = minZ + 1; z <= maxZ; z++) {
                temp = tiles.get(z);
                for (int j = 0, lenJ = temp.size(); j < lenJ; j++) {
                    pow = 2 << (z - minZ - 1);
                    tile = temp.get(j);
                    x = tile[1];
                    y = tile[2];
                    if ((x / pow == rootX) && (y / pow == rootY)) {
                        treeTiles.add(tile);
                    }
                }
            }
        }
        return treeTiles;
    }

    /**
     * 计算指定瓦片下的所有瓦片
     *
     * @param z
     * @param x
     * @param y
     * @param nextZ
     * @return
     */
    public static Map<Long, Long[]> computeAllZTileRegion(long z, long x, long y, long nextZ) {
        if (nextZ < z) {
            throw new RuntimeException("nextZ Must big than z");
        }
        Map<Long, Long[]> tileInfo = new HashMap<>();
        tileInfo.put(z, new Long[]{x, x, y, y});
        if (z == nextZ) {
            return tileInfo;
        }
        long sx = x << 1;
        long sy = y << 1;
        long ex = (x << 1) + 1;
        long ey = (y << 1) + 1;
        ++z;
        tileInfo.put(z, new Long[]{sx, ex, sy, ey});
        while (++z <= nextZ) {
            sx = sx << 1;
            sy = sy << 1;
            ex = (ex << 1) + 1;
            ey = (ey << 1) + 1;
            tileInfo.put(z, new Long[]{sx, ex, sy, ey});
        }
        return tileInfo;
    }

    /**
     * 指定一个瓦片获取第nextZ级别瓦片的范围
     *
     * @param z
     * @param x
     * @param y
     * @param nextZ
     * @return
     */
    public static long[] computeZTileRegion(long z, long x, long y, long nextZ) {
        if (nextZ < z) {
            throw new RuntimeException("nextZ Must big than z");
        }
        if (z == nextZ) {
            return new long[]{x, x, y, y};
        }
        long sx = x << 1;
        long sy = y << 1;
        long ex = (x << 1) + 1;
        long ey = (y << 1) + 1;
        ++z;
        if (z == nextZ) {
            return new long[]{sx, ex, sy, ey};
        }
        while (++z <= nextZ) {
            sx = sx << 1;
            sy = sy << 1;
            ex = (ex << 1) + 1;
            ey = (ey << 1) + 1;
            if (z == nextZ) {
                return new long[]{sx, ex, sy, ey};
            }
        }
        return null;
    }


    /**
     * 计算 单块瓦片下的所有瓦片数量和
     *
     * @param sZ
     * @param eZ
     * @return
     */
    public static long computeTileCount(long sZ, long eZ) {
        long sum = 0;
        double count;
        for (long i = 0; i <= eZ - sZ; i++) {
            count = Math.pow(4, i);
            sum += count;
        }
        return sum;
    }

    public static long computeRangeTileCount(long sX, long eX, long sY, long eY, long sZ, long eZ) {
        return (eX - sX + 1) * (eY - sY + 1) * computeTileCount(sZ, eZ);
    }


    public static Envelope getTileEnvelope(long z, long x, long y) {
        byte b = (byte) z;
        double gridWest = MercatorProjection.tileXToLongitude(x, b);
        double gridSouth = MercatorProjection.tileYToLatitude(y + 1, b);
        double gridEast = MercatorProjection.tileXToLongitude(x + 1, b);
        double gridNorth = MercatorProjection.tileYToLatitude(y, b);
        return new Envelope(gridWest, gridEast, gridSouth, gridNorth);
    }


}
