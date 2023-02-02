package com.grandtech.mapframe.core.snapshot.custom;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.util.Log;


import com.grandtech.mapframe.core.rules.Rules;
import com.grandtech.mapframe.core.util.BitmapUtil;
import com.grandtech.mapframe.core.util.MeasureScreen;
import com.grandtech.mapframe.core.util.MercatorProjection;
import com.grandtech.mapframe.core.util.ThreadPoolUtil;
import com.grandtech.mapframe.core.util.TileUtil;
import com.grandtech.mapframe.core.util.Transformation;
import com.mapbox.geojson.BoundingBox;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Geometry;
import com.mapbox.geojson.MultiPolygon;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.camera.CameraUpdate;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

/**
 * @ClassName MapScreenshotTool
 * @Description TODO
 * @Author: fs
 * @Date: 2022/8/10 9:06
 * @Version 2.0
 */
public class MapScreenshotTool implements Rules {

    public interface ICallBack {

        void onError();

        void complete(ScreenshotsSetting screenshotsSetting,String path);
    }
    //图片裁剪的时候开阔的值
    private  int cutBuffer = 20;

    private  boolean stop = false;

    public void createMapScreenshotsAsync(ScreenshotsSetting screenshotsSetting,String path,ICallBack iCallBack){
        new DbCommand<Boolean>(){
            @Override
            protected Boolean doInBackground() {
                if(stop){
                    return false;
                }
                Bitmap mapScreenshots = createMapScreenshots(screenshotsSetting);
                if(screenshotsSetting==null){
                    return false;
                }
                Log.i("坐落图生成p：",path);
                return BitmapUtil.bitmap2File(mapScreenshots, path, 100);
            }

            @Override
            protected void onPostExecute(Boolean result) {
                super.onPostExecute(result);
                if(iCallBack == null) {
                    return;
                }
                if(result){
                    iCallBack.complete(screenshotsSetting,path);
                }else {
                    iCallBack.onError();
                }
            }
        }.execute();
    }

    /**
     * 停止所有即将执行的任务，正在执行的会执行完
     */
    public  void stopCreateMapScreenshotsTask(){
        stop = true;
    }
    /**
     * 恢复可执行状态
     */
    public  void reStartCreateMapScreenshotsTask(){
        stop = false;
    }

    /**
     * 多地块生成坐落图
     * @param screenshotsSetting
     * @return
     */
    public  Bitmap createMapScreenshots(ScreenshotsSetting screenshotsSetting) {
        Bitmap bitmap = null;
        try {
            FeatureCollection featureCollection = ScreenshotsSetting.getFeatureCollections(screenshotsSetting.getScreenshotsFeatureStyes());
            //LatLngBounds latLngBounds = Transformation.features2BoxLatLngBounds(featureCollection.features());
            List<Feature> features = featureCollection.features();
            List<Geometry> geometries = new ArrayList<>();
            for (Feature feature : features) {
                geometries.add(feature.geometry());
            }
            BoundingBox boundingBox =  Transformation.mergeBoxFeatureBound(1.02D, geometries.toArray(new Geometry[geometries.size()]));
            Log.i("boundingBox",boundingBox.toJson());
            int[] arr = new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17};
            List<long[]> tiles = searchZoomTiles(arr, boundingBox,screenshotsSetting.getTileDensity());
            Envelope envelope = TileUtil.xyzs2Envelope2Pixel(tiles);
            Double w = envelope.getMaxX() - envelope.getMinX();
            Double h = envelope.getMaxY()-envelope.getMinY();
            bitmap = Bitmap.createBitmap(w.intValue(), h.intValue(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            Paint paint = new Paint();
            paint.setAntiAlias(true);
            List<FutureTask<String>> futureTasks = drawTileImageAsync(canvas,paint,tiles, envelope,screenshotsSetting.getImageUrl());
            for (FutureTask<String> futureTask : futureTasks) {
                futureTask.get();
            }
            List<String> otherImageUrls = screenshotsSetting.getOtherImageUrls();
            for (String otherImageUrl : otherImageUrls) {
                //绘制影像
                List<FutureTask<String>> futureTasksOther =drawTileImageAsync(canvas,paint,tiles,envelope,otherImageUrl);
                for (FutureTask<String> futureTask : futureTasksOther) {
                    futureTask.get();
                }
            }
            drawPolygon(screenshotsSetting.getScreenshotsFeatureStyes(),envelope,tiles.get(0)[0],canvas,paint);
            if(screenshotsSetting.isDrawRect()){
                //绘制外框以及四至坐标点
                drawRectAndSzzb(boundingBox,canvas,paint,tiles.get(0)[0],envelope);
            }
            //对完成的图片进行裁剪，保证图形的位置在图片的中心
             bitmap = cutImageWithPolygonCenter(bitmap,boundingBox,tiles.get(0)[0],envelope);
             canvas.save();
             canvas.restore();
             canvas = new Canvas(bitmap);
            //绘制水印
            drawWaters(canvas,paint,screenshotsSetting.getWaters());
            canvas.save();
            canvas.restore();
            return bitmap;
        }catch (Exception e){
            e.printStackTrace();
            Log.e("MapScreenshotTool",e.getMessage());
            bitmap.recycle();
        }
        return null;
    }


    private  void drawPolygon(List<ScreenshotsFeatureStye> screenshotsFeatureStyes, Envelope envelope, long zoom, Canvas canvas, Paint paint)throws Exception{
       for (ScreenshotsFeatureStye screenshotsFeatureStye : screenshotsFeatureStyes) {
           if(ScreenshotsFeatureStye.NULL.equals(screenshotsFeatureStye.getFillType())){
               paint.setStyle(Paint.Style.STROKE);
           }else {
               paint.setStyle(Paint.Style.FILL_AND_STROKE);
           }
           paint.setColor(screenshotsFeatureStye.getColor());
           paint.setStrokeWidth(screenshotsFeatureStye.getStrokeSize());
           List<List<Point>> cs = new ArrayList<>();
           Geometry geometry = screenshotsFeatureStye.getFeature().geometry();
           if (geometry instanceof com.mapbox.geojson.Polygon) {
               cs = ((com.mapbox.geojson.Polygon) geometry).coordinates();
           }
           if (geometry instanceof MultiPolygon) {
               List<List<List<Point>>> csss = ((MultiPolygon) geometry).coordinates();
               for (List<List<Point>> css : csss) {
                   cs.addAll(css);
               }
           }
           for (List<Point> c : cs) {
               Path path = new Path();
               for (int i = 0;i<c.size();i++) {
                   Point point = c.get(i);
                   Double x = MercatorProjection.longitudeToPixelX(point.longitude(), (byte) zoom)- envelope.getMinX();
                   Double y = MercatorProjection.latitudeToPixelY(point.latitude(),(byte) zoom)- envelope.getMinY();
                   if(i==0){
                       path.moveTo(x.floatValue(),y.floatValue());
                   }else {
                       path.lineTo(x.floatValue(),y.floatValue());
                   }
               }
               canvas.drawPath(path,paint);
           }
           if(screenshotsFeatureStye.isDrawPolygonMark()){
               drawPolygonMark(canvas,geometry,paint,envelope,zoom);
           }
       }
   }

    private  void drawPolygonMark(Canvas canvas, Geometry geometry, Paint paint, Envelope envelope, long zoom) throws Exception {
        List<Envelope> envelopes = new ArrayList<Envelope>();
        //绘制标注需要做避让
        paint.setColor(Color.rgb(255, 255, 255));
       // paint.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD_ITALIC));
        paint.setStrokeWidth(1);
        paint.setFakeBoldText(true);
        paint.setStyle(Paint.Style.FILL);
        //得到多边形的最小凸壳多边形
        com.vividsolutions.jts.geom.Geometry _convexHull = Transformation.wkt2JtsGeometry(Transformation.boxGeometry2Wkt(geometry)).convexHull();
        Coordinate[] _convexHullcs = _convexHull.getCoordinates();
        for (int j = 0, len = _convexHullcs.length; j < len; j++) {
            Coordinate c = _convexHullcs[j];
            Point point = Point.fromLngLat(c.x, c.y);
            Point pointCenter = Point.fromLngLat(_convexHull.getCentroid().getX(), _convexHull.getCentroid().getY());
            String anchor = Transformation.calTextAnchor(point, pointCenter);
            double pixelx = MercatorProjection.longitudeToPixelX(c.x, (byte) zoom);
            double pixely = MercatorProjection.latitudeToPixelY(c.y, (byte)zoom);
            Rect textRect = new Rect();
            String text = "";
            if(String.valueOf(c.x).length()> String.valueOf(c.y).length()){
                text = String.valueOf(c.x);
            }else {
                text = String.valueOf(c.y);
            }
            paint.getTextBounds(text, 0,text.length(), textRect);
            if(cutBuffer<textRect.width()){
                cutBuffer = textRect.width();
            }
            if (anchor.equals("top-right") || anchor.equals("bottom-right")) {
                paint.setTextAlign(Paint.Align.LEFT);
                double xp1 = pixelx - textRect.width();
                double yp1 = pixely - textRect.height() * 2;
                Envelope e = new Envelope(MercatorProjection.pixelXToLongitude(xp1, (byte)zoom), c.x, MercatorProjection.pixelYToLatitude(yp1, (byte)zoom), c.y);
                boolean isIn = false;
                for (Envelope envelope1 : envelopes) {
                    if (envelope1.intersects(e)) {
                        isIn = true;
                        break;
                    }
                }
                if (!isIn) {
                    envelopes.add(e);
                    canvas.drawText(c.x + "",(int) (pixelx - envelope.getMinX())-textRect.width(), (int) (pixely - envelope.getMinY()),paint);
                    canvas.drawText(c.y + "",(int) (pixelx - envelope.getMinX())-textRect.width(), (int) (pixely - envelope.getMinY())+textRect.height()+3,paint);
                }

            }
            if (anchor.equals("top-left") || anchor.equals("bottom-left")) {
                paint.setTextAlign(Paint.Align.LEFT);
                double xp1 = pixelx + textRect.width();
                double yp1 = pixely + textRect.height() * 2;
                Envelope e = new Envelope(c.x, MercatorProjection.pixelXToLongitude(xp1,(byte) zoom), c.y, MercatorProjection.pixelYToLatitude(yp1,(byte) zoom));
                boolean isIn = false;
                for (Envelope envelope1 : envelopes) {
                    if (envelope1.intersects(e)) {
                        isIn = true;
                        break;
                    }
                }
                if (!isIn) {
                    envelopes.add(e);
                    canvas.drawText(c.x + "",
                            (int) (pixelx - envelope.getMinX()),
                            (int) (pixely - envelope.getMinY()),paint);
                    canvas.drawText(c.y + "",
                            (int) (pixelx - envelope.getMinX()),
                            (int) (pixely - envelope.getMinY()) + textRect.height()+3,paint);
                }
            }
        }
    }

    /**
     * 根据面积计算合适的瓦片
     *
     * @param latLngBounds
     * @return
     */
    /*private  List<long[]> searchZoomTiles(MapboxMap mapboxMap,LatLngBounds latLngBounds) {
        long zoom = zoom(mapboxMap, latLngBounds);
        List<long[]> boundTile = TileUtil.getBoundTile(latLngBounds.getLonWest(), latLngBounds.getLatSouth(), latLngBounds.getLonEast(), latLngBounds.getLatNorth(), zoom);
        return boundTile;
    }*/

   /* private  long zoom(MapboxMap mapboxMap, LatLngBounds latLngBounds){
        double wd = latLngBounds.getLongitudeSpan();
        double ht = latLngBounds.getLatitudeSpan();
        int width = MeasureScreen.getScreenWidth();
        int height = MeasureScreen.getScreenHeight(Utils.getApp());
        int padWidth = 200;
        float baseDensity = 2;
        float density = ScreenUtils.getScreenDensity();
        LogUtils.i("密度1",density);
        padWidth = Math.round(density / baseDensity * padWidth);
        Rect padRect = new Rect(0, (height - width) / 2, width, (height + width) / 2);
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(latLngBounds,
                (wd > ht) ? padWidth : 0,
                (wd > ht) ? 0 : padRect.top + padWidth,
                (wd > ht) ? padWidth : 0,
                (wd > ht) ? 0 : padRect.top + padWidth);
        double zoom = cameraUpdate.getCameraPosition(mapboxMap).zoom;
        LogUtils.i("坐落图生成",zoom,wd,ht,width,height);
        return Math.round(zoom>=17?17:zoom);
    }*/
    /**
     * 根据面积计算合适的瓦片
     *
     * @param arr
     * @param boundingBox
     * @param key
     * @return
     */
    public  List<long[]> searchZoomTiles(int[] arr, BoundingBox boundingBox, double key) {
        List<long[]> lastTiles = new ArrayList<>();
        double ratiolast = 0;
        for (int i = arr.length - 1; i >= 0; i--) {
            int zoom = arr[i];
            List<long[]> tiles = TileUtil.getBoundTileV2(boundingBox.west(), boundingBox.south(), boundingBox.east(), boundingBox.north(), zoom);
            Envelope envelope = TileUtil.xyzs2Envelope2Pixel(tiles);
            byte b = (byte) zoom;
            double pminx = MercatorProjection.longitudeToPixelX(boundingBox.west(), b);
            double pmaxX = MercatorProjection.longitudeToPixelX(boundingBox.east(), b);
            double pminY = MercatorProjection.latitudeToPixelY(boundingBox.north(), b);
            double pmaxY = MercatorProjection.latitudeToPixelY(boundingBox.south(), b);
            double pAreaLength = (pmaxX - pminx) > (pmaxY - pminY) ? pmaxX - pminx : pmaxY - pminY;
            double pTileLength = (pmaxX - pminx) > (pmaxY - pminY) ? (envelope.getMaxX() - envelope.getMinX()) : (envelope.getMaxY() - envelope.getMinY());
            Log.i("坐落图生成","级别：" + zoom + "占比：" + (pAreaLength / pTileLength));
            if (lastTiles.size() == 0) {
                ratiolast = pAreaLength / pTileLength;
                lastTiles = tiles;
            }
            if (Math.abs(pAreaLength / pTileLength - key) <= Math.abs(ratiolast - key)) {
                lastTiles = tiles;
                ratiolast = pAreaLength / pTileLength;
            }
        }
        return lastTiles;
    }
    /**
     *
     *
     * @param canvas
     * @param paint
     * @param tiles
     * @param envelope
     * @param imgUrlTemp 例 https://t0.tianditu.gov.cn/DataServer?T=img_w&x={x}&y={y}&l={z}&tk=583e63953a6ed6bf304e68120db4c512
     * @return
     */
    private  List<FutureTask<String>> drawTileImageAsync(Canvas canvas, Paint paint, List<long[]> tiles, Envelope envelope, String imgUrlTemp){
        List<FutureTask<String>> futureTasks = new ArrayList<>();

        for (long[] tile : tiles) {
            FutureTask<String> futureTask = new FutureTask<String>(new Callable<String>() {
                @Override
                public String call() throws Exception {
                    String imgUrl = imgUrlTemp.replace("{x}", String.valueOf(tile[1])).replace("{y}", String.valueOf(tile[2])).replace("{z}", String.valueOf(tile[0]));
                    Bitmap bitmap = toRequestImgTile(imgUrl);
                    if(bitmap == null){
                        return "000";
                    }
                    Envelope envelopet = TileUtil.xyz2Envelope2Pixel(tile);
                    int wt = (int) (envelopet.getMinX() - envelope.getMinX());
                    int ht = (int) (envelopet.getMinY() - envelope.getMinY());
                    canvas.drawBitmap(bitmap,wt,ht,paint);
                    bitmap.recycle();
                    return "null";
                }
            });
            futureTasks.add(futureTask);
            ThreadPoolUtil.execute(futureTask);
        }
        return futureTasks;
    }
    /**
     * 多瓦片并行请求，每个瓦片先请求天地图 6-0，每个2秒，还是不行的话就请求mapbox的影像地图
     * @param imgUrl
     * @return
     */
    private  Bitmap toRequestImgTile(String imgUrl)  {
        Bitmap imgTile = null;
        for (int i = 6;i>=0&&i<=6;i--){
            String imgUrl1 = imgUrl.replace("https://t0.","https://t"+i+".");
            imgTile = getImage(imgUrl1);
            if(imgTile!=null) {
                return imgTile;
            }
        }
        return null;
    }

    /**
     *
     * @param urlSpec 瓦片地址
     * @return
     */
    private  Bitmap getImage(String urlSpec){
        HttpURLConnection httpURLConnection = null;
        BufferedReader reader = null;
        try{
            URL url = new URL(urlSpec);
            httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestMethod("GET");
            httpURLConnection.setConnectTimeout(2000);
            httpURLConnection.setReadTimeout(2000);
            InputStream in = httpURLConnection.getInputStream();
            /*//对获得的流进行读取
            reader = new BufferedReader(new InputStreamReader(in));
            StringBuilder response = new StringBuilder();
            String line;
            while((line = reader.readLine()) != null){
                response.append(line);
            }*/
            //处理相应
           // handleResponse(response.toString());
            Bitmap bit= BitmapFactory.decodeStream(in);
            return bit;
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if(reader != null){
                try{
                    reader.close();
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
            if(httpURLConnection != null){
                httpURLConnection.disconnect();
            }
        }
        return null;
    }

    private  void drawRectAndSzzb(BoundingBox boundingBox, Canvas canvas, Paint paint, long zoom, Envelope envelope) {
        double pminx = MercatorProjection.longitudeToPixelX(boundingBox.west(), (byte) zoom)  - envelope.getMinX();
        double pmaxX = MercatorProjection.longitudeToPixelX(boundingBox.east(), (byte) zoom)  - envelope.getMinX();
        double pminY = MercatorProjection.latitudeToPixelY(boundingBox.north(), (byte) zoom) - envelope.getMinY();
        double pmaxY = MercatorProjection.latitudeToPixelY(boundingBox.south(),(byte) zoom)  - envelope.getMinY();
        Rect rect = new Rect((int)pminx,(int)pminY,(int)pmaxX,(int)pmaxY);
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawRect(rect,paint);
    }


    private  Bitmap cutImageWithPolygonCenter(Bitmap bitmap, BoundingBox boundingBox, long zoom, Envelope envelope) {
        double pminx = MercatorProjection.longitudeToPixelX(boundingBox.west(), (byte) zoom) - cutBuffer - envelope.getMinX();
        double pmaxX = MercatorProjection.longitudeToPixelX(boundingBox.east(), (byte) zoom) + cutBuffer - envelope.getMinX();
        double pminY = MercatorProjection.latitudeToPixelY(boundingBox.north(), (byte) zoom)- cutBuffer - envelope.getMinY();
        double pmaxY = MercatorProjection.latitudeToPixelY(boundingBox.south(),(byte) zoom) + cutBuffer - envelope.getMinY();
        if (pmaxX - pminx > pmaxY - pminY) {
            double cz = (pmaxX - pminx) - (pmaxY - pminY);
            pmaxY = pmaxY + cz / 2;
            pminY = pminY - cz / 2;
            if(pminY<0){
                double c = 0-pminY;
                pminY = 0;
                pmaxY = pmaxY+c;
            }
            pmaxY = pmaxY>bitmap.getHeight()?bitmap.getHeight():pmaxY;
        } else {
            double cz = (pmaxY - pminY) - (pmaxX - pminx);
            pmaxX = pmaxX + cz / 2;
            pminx = pminx - cz / 2;
            if(pminx<0){
                double c = 0-pminx;
                pminx = 0;
                pmaxX= pmaxX+c;
            }
            pmaxX = pmaxX>bitmap.getWidth()?bitmap.getWidth():pmaxX;
        }
        if(pmaxX - pminx<256&&bitmap.getWidth()>=256){
            double cz = 256 - (pmaxX - pminx);
            pmaxX = pmaxX + cz / 2;
            pminx = pminx - cz / 2;
            if(pminx<0){
                double c = 0-pminx;
                pminx = 0;
                pmaxX= pmaxX+c;
            }
            pmaxX = pmaxX>bitmap.getWidth()?bitmap.getWidth():pmaxX;
        }
        if(pmaxY - pminY<256&&bitmap.getHeight()>=256){
            double cz = 256 - (pmaxY - pminY);
            pmaxY = pmaxY + cz / 2;
            pminY = pminY - cz / 2;
            if(pminY<0){
                double c = 0-pminY;
                pminY = 0;
                pmaxY = pmaxY+c;
            }
            pmaxY = pmaxY>bitmap.getHeight()?bitmap.getHeight():pmaxY;
        }
        //把图片裁剪成最佳状态 即：地块加坐标点的外接矩形为截图的边界
        //写入图像内容
        Rect rect = new Rect((int)pminx,(int)pminY,(int)pmaxX,(int)pmaxY);

        int w = rect.width();
        int h = rect.height();
        Log.i("裁剪尺寸",cutBuffer+","+(pmaxX-pminx)+","+(pmaxY-pminY)+","+rect.left+","+ rect.top+","+w+","+ h);
        Bitmap bitmap1 = Bitmap.createBitmap(bitmap, rect.left, rect.top, w, h);
        bitmap.recycle();
        return bitmap1;
    }


    private  void drawWaters(Canvas canvas, Paint paint, List<String> waters) {
        if(waters == null){
            return;
        }
        int destWidth = canvas.getWidth();
        int destHeight = canvas.getHeight();
        double fontSize = destWidth * 0.03;
        paint.setTextSize((float)fontSize);//字体大小
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setColor(Color.WHITE);//采用的颜色
        int anchor = destHeight - 10;
        int textHt = (int) fontSize + 3;
        for (int i = waters.size() - 1; i >= 0; i--) {
            canvas.drawText(waters.get(i), 10, anchor - textHt * (waters.size()- i - 1), paint);
        }
    }




}
