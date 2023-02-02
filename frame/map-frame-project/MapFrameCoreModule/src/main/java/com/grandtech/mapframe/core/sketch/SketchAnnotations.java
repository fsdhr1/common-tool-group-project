package com.grandtech.mapframe.core.sketch;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Rect;
import android.view.MotionEvent;
import com.grandtech.mapframe.core.enumeration.GeometryType;
import com.grandtech.mapframe.core.maps.GMapView;
import com.grandtech.mapframe.core.sketch.geometry.SketchGeometry;
import com.grandtech.mapframe.core.util.FeatureSet;
import com.grandtech.mapframe.core.util.MeasureUtil;
import com.grandtech.mapframe.core.util.Pro4jUtil;
import com.grandtech.mapframe.core.util.Transformation;
import com.mapbox.geojson.Geometry;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.MultiLineString;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.vividsolutions.jts.geom.Coordinate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;


/**
 * Created by zy on 2018/12/18.
 * 绘制核心，负责在MapView上加载
 */

public class SketchAnnotations extends SketchMapEvnet implements ISketch {
    SketchHelpView sketchHelpView;
    SketchSetting sketchSetting = new SketchSetting("drawBar", GeometryType.polygon);
    /**
     * 填充色
     */
    //int fillColor = Color.parseColor("#00ffffff");
    int fillColor = Color.parseColor("#FF0000");
    /**
     *
     */
    int strokeColor = Color.parseColor("#FF0000");
    /**
     * 面透明度
     */
    float alpha = 0.1f;
    /**
     * 回退栈
     */
    List<SketchGeometry> backStack;
    List<SketchGeometry> cacheStack;


    /**
     * 地图盒子
     */
    private MapboxMap _mapBoxMap;

    private final Context _context;

    private final GMapView _gMapView;
    /**
     * 判断当前是否启动
     */
    private boolean isStartSketch;

    public SketchAnnotations(GMapView _gMapView,Context context) {
        this._mapBoxMap = _gMapView.getMapBoxMap();
        this._context = context;
        this._gMapView = _gMapView;
    }

    public SketchAnnotations(GMapView _gMapView,Context context,SketchSetting sketchSetting) {
        this._mapBoxMap = _gMapView.getMapBoxMap();
        this._context = context;
        this._gMapView = _gMapView;
        this.sketchSetting = sketchSetting;
    }
    boolean drawRubberLine = false;

    /**
     * 设置绘制类型 点线面
     * @param geometryType
     */
    @Override
    public void setSketchType(@NonNull GeometryType geometryType){
        if(this.sketchSetting == null){
            sketchSetting = new SketchSetting("drawBar", geometryType);
        }else {
            sketchSetting.setSketchType(geometryType);
        }
    }

    public SketchSetting getSketchSetting() {
        return sketchSetting;
    }

    /**
     * 添加屏幕中点(橡皮绳加点)
     */
    public PointF addScreenCenterPoint() {
        drawRubberLine = true;// 加点工具，屏幕中心点加点
        PointF pointF = sketchHelpView.getScreenCenterPoint();
        LatLng latLng = _mapBoxMap.getProjection().fromScreenLocation(pointF);
        Point point = Point.fromLngLat(latLng.getLongitude(), latLng.getLatitude());
        SketchGeometry sketchGeometry = SketchGeometry.create(_mapBoxMap, sketchSetting.getSketchType(), fillColor, strokeColor, alpha, 3);
        if (cacheStack.size() == 0) {
            sketchGeometry.smartCreateGeometry(point);
        } else {
            SketchGeometry lastSketchGeometry = cacheStack.get(cacheStack.size() - 1);
            if (lastSketchGeometry.getSketchGeometry() != null) {
                sketchGeometry.appendPoint2Geometry(lastSketchGeometry.getSketchGeometry(), point);
            } else {
                sketchGeometry.smartCreateGeometry(point);
            }
        }
        cacheStack.add(sketchGeometry);
        handleGeometry(sketchGeometry);
        return pointF;
    }

    /**
     * 加点,手动点击屏幕加点
     */
    public void addPoint(LatLng latLng) {
        drawRubberLine = false;// 扣除、分割、绘制，手动点击屏幕加点
        Point point = Point.fromLngLat(latLng.getLongitude(), latLng.getLatitude());
        SketchGeometry sketchGeometry = SketchGeometry.create(_mapBoxMap, sketchSetting.getSketchType(), fillColor, strokeColor, alpha, 2);
        if (cacheStack.size() == 0) {
            sketchGeometry.smartCreateGeometry(point);
        } else {
            SketchGeometry lastSketchGeometry = cacheStack.get(cacheStack.size() - 1);
            if (lastSketchGeometry.getSketchGeometry() != null) {
                sketchGeometry.appendPoint2Geometry(lastSketchGeometry.getSketchGeometry(), point);
            } else {
                sketchGeometry.smartCreateGeometry(point);
            }
        }
        cacheStack.add(sketchGeometry);
        handleGeometry(sketchGeometry);
    }


    /**
     * 加点,长按拖拽编辑节点
     * @param geometry
     */
    public void addGeometry(Geometry geometry) {
        if (geometry == null) {
            return;
        }
        drawRubberLine = false;// 长按拖拽编辑节点
        SketchGeometry sketchGeometry = SketchGeometry.create(_mapBoxMap, sketchSetting.getSketchType(), fillColor, strokeColor, alpha, 2);
        sketchGeometry.addGeometry(geometry);
        cacheStack.add(sketchGeometry);
        handleGeometry(sketchGeometry);
        //computerAreaAndLen(geometry);
    }

    /**
     * 编辑节点
     *
     * @param latLng
     */
    public void editPoint(LatLng latLng) {
        handleGeometry(null);
    }

    /**
     * 移除节点
     *
     * @param latLng
     */
    public void removePoint(LatLng latLng) {
        handleGeometry(null);
    }

    private void handleGeometry(SketchGeometry sketchGeometry) {
        if (cacheStack.size() == 0) {
            return;
        }
        if (cacheStack.size() > 1) {
            SketchGeometry lastSketchGeometry = cacheStack.get(cacheStack.size() - 2);
            lastSketchGeometry.clearFromMap(_mapBoxMap);
        }
        sketchGeometry.drawToMap(_mapBoxMap);
        drawRubberLine();

    }


    public SketchAnnotations activate() {
        isStartSketch = true;
        if(sketchHelpView == null) {
            sketchHelpView = new SketchHelpView(_context);
        }
        this.backStack = new ArrayList<>();
        this.cacheStack = new ArrayList<>();
        _gMapView.removeView(sketchHelpView);
        _gMapView.addView(sketchHelpView);
        return this;
    }


    public void deactivate() {
        isStartSketch = false;
        sketchHelpView.clear(false);
        sketchHelpView.setText(null);
        _gMapView.removeView(sketchHelpView);
        // iListener = null;
        clearSketch();
    }

    /**
     * 清空绘制
     */
    public void clearSketch() {
        clearLastSketch();
        sketchHelpView.clear(false);
        if (this.backStack != null) {
            backStack.clear();
        }
        if (this.cacheStack != null) {
            cacheStack.clear();
        }
    }

    /**
     * 加一个空图形到缓存队列，并绘制
     */
    public void nullSketch() {
        clearLastSketch();
        SketchGeometry sketchGeometry = SketchGeometry.create(_mapBoxMap, sketchSetting.getSketchType(), fillColor, strokeColor, alpha, 2);
        cacheStack.add(sketchGeometry);
    }

    /**
     * 清空上次绘制
     */
    public void clearLastSketch() {
        if (cacheStack == null || cacheStack.size() == 0) {
            return;
        }
        SketchGeometry lastSketchGeometry = cacheStack.get(cacheStack.size() - 1);
        lastSketchGeometry.clearFromMap(_mapBoxMap);
    }

    /**
     * 检查geometry是否合法
     *
     * @return
     */
    public String checkSketchGeometry() {
        try {
            if (cacheStack.size() == 0) {
                return "未能获取绘制的图形";
            }
            SketchGeometry sketchGeometry = cacheStack.get(cacheStack.size() - 1);
            if (sketchGeometry.getGeometry() == null) {
                return "未能获取绘制的图形";
            }
            int infoIndex = Transformation.checkGeometry(sketchGeometry.getGeometry());
            if (infoIndex == -1) {
                return "无法构成" + sketchSetting.getSketchType();
            }
            Coordinate[] coordinates = Transformation.geoJson2JstGeometry(sketchGeometry.getGeometry()).getCoordinates();
            if(coordinates!=null &&(sketchSetting.getSketchType().equals(GeometryType.polygon)
                    ||sketchSetting.getSketchType().equals(GeometryType.multiPolygon))){
                if(coordinates.length<5){
                    return "不可绘制三角形地块";
                }
            }
            if (infoIndex == 100) {
                return null;
            }  else {
                return Transformation.GEOMETRY_ERROR_TYPE[infoIndex];
            }
        } catch (Exception e) {
            return e.getMessage();
        }
    }

    /**
     * 获取绘制的geometry
     *
     * @return
     */
    public Geometry getGeometry() {
        if (cacheStack.size() == 0) {
            return null;
        }
        SketchGeometry sketchGeometry = cacheStack.get(cacheStack.size() - 1);
        return sketchGeometry.getGeometry();
    }

    /**
     * ReDo
     */
    public void tryReDo() {
        try {
            if (backStack.size() == 0) {
                return;
            }
            clearLastSketch();
            int lastIndex = backStack.size() - 1;
            SketchGeometry sketchGeometry = backStack.get(lastIndex);
            cacheStack.add(sketchGeometry);
            backStack.remove(lastIndex);
            sketchGeometry.drawToMap(_mapBoxMap);
        } catch (Exception e) {
            System.err.println("ReDo无法继续操作");
        } finally {
            drawRubberLine();
        }
    }

    /**
     * UnDo
     */
    public void tryUnDo() {
        try {
            clearLastSketch();
            SketchGeometry last = cacheStack.get(cacheStack.size() - 1);
            cacheStack.remove(cacheStack.size() - 1);
            backStack.add(last);
            SketchGeometry sketchGeometry = cacheStack.get(cacheStack.size() - 1);
            sketchGeometry.drawToMap(_mapBoxMap);
        } catch (Exception e) {
            System.err.println("UnDo无法继续操作");
        } finally {
            drawRubberLine();
        }
    }

    /**
     *
     */
    public GeoTouchAction geoTouchCheck(LatLng latLng) {
        PointF pointF = _mapBoxMap.getProjection().toScreenLocation(latLng);
        if (cacheStack == null || cacheStack.size() == 0) {
            return GeoTouchAction.createEvent();
        }
        SketchGeometry sketchGeometry = cacheStack.get(cacheStack.size() - 1);
        return sketchGeometry.checkGeoTouchMode(_mapBoxMap, pointF);
    }

    /**
     * *****************************************有关橡皮绳开始**************************************
     */

    public void drawRubberLine() {
        if (!drawRubberLine) {
            sketchHelpView.clear(false);
            if (cacheStack == null || cacheStack.size() == 0) {
                return;
            }
            SketchGeometry sketchGeometry = cacheStack.get(cacheStack.size() - 1);
            computerAreaAndLen(sketchGeometry.getGeometry());
        } else {
            System.out.println("绘制橡皮绳");
            if (sketchSetting == null) {
                return;
            }
            if (cacheStack == null || cacheStack.size() == 0) {
                sketchHelpView.clear(false);
                return;
            }
            SketchGeometry sketchGeometry = cacheStack.get(cacheStack.size() - 1);
            //获取橡皮绳的挂点
            Point[] anchorPoints = sketchGeometry.getRubberAnchor();
            if (anchorPoints == null) {
                System.out.println("未能获取橡皮绳挂点");
                return;
            }
            if (anchorPoints[0] != null && anchorPoints[2] != null) {
                PointF pointFa = _mapBoxMap.getProjection().toScreenLocation(new LatLng(anchorPoints[0].latitude(), anchorPoints[0].longitude()));
                PointF pointFc = _mapBoxMap.getProjection().toScreenLocation(new LatLng(anchorPoints[2].latitude(), anchorPoints[2].longitude()));
                sketchHelpView.setPoints(pointFa, pointFc);
            }
            if (anchorPoints[0] != null && anchorPoints[2] == null) {
                PointF pointFa = _mapBoxMap.getProjection().toScreenLocation(new LatLng(anchorPoints[0].latitude(), anchorPoints[0].longitude()));
                sketchHelpView.setPoints(pointFa);
            }
            LatLng center = _mapBoxMap.getProjection().fromScreenLocation(sketchHelpView.getScreenCenterPoint());
            Geometry anchorGeometry = sketchGeometry.getRubberAnchorGeometry(Point.fromLngLat(center.getLongitude(), center.getLatitude()));
            computerAreaAndLen(anchorGeometry);
        }
        if (sketchSetting.getSketchType() == GeometryType.line) {
            if (cacheStack == null || cacheStack.size() == 0) {
                return;
            }
            SketchGeometry sketchGeometry = cacheStack.get(cacheStack.size() - 1);
            computeSplitArea(sketchGeometry);
        }
    }

    /**
     * 拖动点绘制橡皮绳
     */
    GeoTouchAction geoTouchAction;
    boolean interceptMapScrollEvent;
    private PointF longStartP;

    public void beforeDragPoint(LatLng latLng) {
        longStartP = _mapBoxMap.getProjection().toScreenLocation(latLng);
        geoTouchAction = geoTouchCheck(latLng);
        if (geoTouchAction.getGeoTouchMode() != GeoTouchAction.GeoTouchMode.TouchOnNull) {
            interceptMapScrollEvent = true;
            PointF longTouchPoint = _mapBoxMap.getProjection().toScreenLocation(latLng);
            if (geoTouchAction.getGeoTouchMode() == GeoTouchAction.GeoTouchMode.TouchOnLine) {
                //拖拽线
            } else if (geoTouchAction.getGeoTouchMode() == GeoTouchAction.GeoTouchMode.TouchOnPolygon) {
                //拖拽面
            }
            //拖拽顶点
            else if (geoTouchAction.getGeoTouchMode() == GeoTouchAction.GeoTouchMode.TouchOnPoint) {
                sketchHelpView.setTargetPoint(longTouchPoint).drawTargetView();
            }
            //拖拽中点
            else if (geoTouchAction.getGeoTouchMode() == GeoTouchAction.GeoTouchMode.TouchOnMidPoint) {
                sketchHelpView.setTargetPoint(longTouchPoint).drawTargetView();
            }
        }
    }

    LatLng newLatLng;
    PointF drawPointF;
    //拖动阈值
    double drawDis = 150;
    float[] vXY;

    public boolean underDragPoint(MotionEvent motionEvent) {
        if (interceptMapScrollEvent) {
            //获取起始点
            if (longStartP == null) {
                return false;
            }
            float vx = motionEvent.getX() - longStartP.x;
            float vy = motionEvent.getY() - longStartP.y;
            double dis = Math.sqrt(vx * vx + vy * vy);
            //if (dis < drawDis) return true;
            //拖拽橡皮绳
            if (geoTouchAction == null) {
                return false;
            }
            if (cacheStack == null || cacheStack.size() == 0) {
                return false;
            }
            String geoId = geoTouchAction.getGeoIp();
            SketchGeometry sketchGeometry = cacheStack.get(cacheStack.size() - 1);
            //拖拽线
            if (geoTouchAction.getGeoTouchMode() == GeoTouchAction.GeoTouchMode.TouchOnLine) {
                //Toast.makeText(context, "拖拽线", Toast.LENGTH_LONG).show();

            }
            //拖拽面
            else if (geoTouchAction.getGeoTouchMode() == GeoTouchAction.GeoTouchMode.TouchOnPolygon) {
                //Toast.makeText(context, "拖拽面", Toast.LENGTH_LONG).show();

            }
            //拖拽点
            else if (geoTouchAction.getGeoTouchMode() == GeoTouchAction.GeoTouchMode.TouchOnPoint
                    || geoTouchAction.getGeoTouchMode() == GeoTouchAction.GeoTouchMode.TouchOnMidPoint) {
                //if (vXY == null) vXY = new float[]{vx, vy};
                //drawPointF = new PointF(motionEvent.getX() - vXY[0], motionEvent.getY() - vXY[1]);
                drawPointF = new PointF(motionEvent.getX(), motionEvent.getY());
                newLatLng = _mapBoxMap.getProjection().fromScreenLocation(drawPointF);
                sketchHelpView.dragNodeDraw(geoTouchAction.getAPointF(), geoTouchAction.getBPointF(), geoTouchAction.getCPointF(), drawPointF).dragNodeDraw();
                editNodePoint(sketchGeometry.getSketchGeometry(), Point.fromLngLat(newLatLng.getLongitude(), newLatLng.getLatitude()), geoId, false);
            }
            return true;
        } else {
            drawRubberLine();
            return false;
        }
    }

    public void afterDragPoint() {
        if (geoTouchAction == null) {
            return;
        }
        if (cacheStack == null || cacheStack.size() == 0) {
            return;
        }
        String geoId = geoTouchAction.getGeoIp();
        SketchGeometry sketchGeometry = cacheStack.get(cacheStack.size() - 1);
        if (geoTouchAction.getGeoTouchMode() == GeoTouchAction.GeoTouchMode.TouchOnLine) {
        }
        if (geoTouchAction.getGeoTouchMode() == GeoTouchAction.GeoTouchMode.TouchOnPolygon) {
        }
        if (geoTouchAction.getGeoTouchMode() == GeoTouchAction.GeoTouchMode.TouchOnPoint) {
            PointF editPoint = sketchHelpView.getEditPoint();
            //拖拽后释放的点
            LatLng editLatLng = _mapBoxMap.getProjection().fromScreenLocation(editPoint);
            editNodePoint(sketchGeometry.getSketchGeometry(), Point.fromLngLat(editLatLng.getLongitude(), editLatLng.getLatitude()), geoId, true);
        }
        if (geoTouchAction.getGeoTouchMode() == GeoTouchAction.GeoTouchMode.TouchOnMidPoint) {
            PointF editPoint = sketchHelpView.getEditPoint();
            //拖拽后释放的点
            LatLng editLatLng = _mapBoxMap.getProjection().fromScreenLocation(editPoint);
            editNodePoint(sketchGeometry.getSketchGeometry(), Point.fromLngLat(editLatLng.getLongitude(), editLatLng.getLatitude()), geoId, true);
        }
        if (geoTouchAction.getGeoTouchMode() != GeoTouchAction.GeoTouchMode.TouchOnNull) {
            boolean holdSplitArea = false;
            if (sketchSetting.getSketchType() == GeometryType.line) {
                holdSplitArea = true;
            }
            sketchHelpView.clear(holdSplitArea);
        }
        geoTouchAction = null;
        interceptMapScrollEvent = false;
        vXY = null;
    }


    private void computeSplitArea(SketchGeometry sketchGeometry) {
        Map<String, FeatureSet> set = _gMapView.getSelectSet();
        List<FeatureSet> featureSets = FeatureSet.convert2FeatureSets(set);
        Geometry _geometry;
        List<Geometry> geometries;
        Geometry line = sketchGeometry.getGeometry();
        if (checkSketchGeometry() != null) return;
        com.vividsolutions.jts.geom.Geometry _jtsGeometry;
        Map<PointF, Double> info = new HashMap<>();
        PointF pointF;
        if(featureSets == null){
            return;
        }
        for (int i = 0; i < featureSets.size(); i++) {
            geometries = Transformation.divFeatures(featureSets.get(i).features(), line);
            if (geometries == null) continue;
            for (int j = 0; j < geometries.size(); j++) {
                _geometry = geometries.get(j);
                _jtsGeometry = Transformation.geoJson2JstGeometry(_geometry);
                _geometry = Transformation.jstGeometry2Geometry(_jtsGeometry.getCentroid());
                pointF = _gMapView.getMapBoxMap().getProjection().toScreenLocation(new LatLng(((Point) _geometry).latitude(), ((Point) _geometry).longitude()));
                _jtsGeometry = Pro4jUtil.pro84To2000(_jtsGeometry);
                //输出分割面积
                info.put(pointF, _jtsGeometry.getArea() * 0.0015);
                sketchHelpView.splitInfo(info);
            }
        }
    }

    private void editNodePoint(Geometry geometry, Point newPoint, String geoId, boolean push2Stack) {
        SketchGeometry sketchGeometry = SketchGeometry.create(_mapBoxMap, sketchSetting.getSketchType(), fillColor, strokeColor, alpha, 2);
        sketchGeometry.editGeometry(geometry, newPoint, geoId);
        if (push2Stack) {
            cacheStack.add(sketchGeometry);
            handleGeometry(sketchGeometry);
        }
        //重新计算面积
        computerAreaAndLen(sketchGeometry.getGeometry());
        //通过绘制类型来判断是不是分割功能，分割面积计算
        if (sketchSetting.getSketchType() == GeometryType.line) {
            computeSplitArea(sketchGeometry);
        }
    }

    /**
     * 计算面积和长度
     *
     * @param geometry
     */
    private void computerAreaAndLen(final Geometry geometry) {
        Observable.create((ObservableOnSubscribe< List<Object>>) emitter -> {
            if (geometry == null) {
                return;
            }
            List<Object> params = new ArrayList<>();
            Double[] paramsD = Transformation.compute84GeoJsonRunTime(geometry);
            params.add(paramsD);
            List<com.vividsolutions.jts.geom.Geometry> _info = new ArrayList<>();
            Point[] points = Transformation.geom2PointArray(geometry);
            if (points != null &&points.length>=2) {
                for(int i = 0;i<points.length;i++){
                    if(i == points.length-1){
                        break;
                    }
                    List<Point> line = new ArrayList<>();
                    line.add(points[i]);
                    line.add(points[i+1]);
                    LineString lineString = LineString.fromLngLats(line);
                    com.vividsolutions.jts.geom.Geometry jtsGeometry = Transformation.geoJson2JstGeometry(lineString);

                    _info.add(jtsGeometry);
                }
            }
            params.add(_info);
            emitter.onNext(params);
            emitter.onComplete();
        }).subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer< List<Object>>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {

                    }

                    @Override
                    public void onNext(@NonNull  List<Object> d) {
                        if (d == null) {
                            sketchHelpView.setText(new String[]{"", ""});
                        } else {
                            for(Object o : d){
                                if(o instanceof Double[] ){
                                    Double[] args = (Double[])o;
                                    String arg0 = args[0] == null ? "0.00" : String.format("%.2f", args[0]);
                                    String arg1 = args[1] == null ? "0.00" : String.format("%.2f", args[1]);
                                    if (sketchSetting.getSketchType() == GeometryType.multiPolygon
                                            || sketchSetting.getSketchType() == GeometryType.polygon) {
                                        sketchHelpView.setText(new String[]{"长度:" + arg0 + "米", "面积:" + arg1 + "亩"});
                                    } else {
                                        //sketchHelpView.setText(new String[]{"长度:" + arg0 + "米", "总长:" + arg1 + "米"});
                                        sketchHelpView.setText(new String[]{ "总长:" + arg1 + "米",""});
                                    }
                                }
                                if(o instanceof List){
                                    List<com.vividsolutions.jts.geom.Geometry> geometryLineInfo = (List<com.vividsolutions.jts.geom.Geometry>)o;
                                    if(geometryLineInfo == null){
                                        continue;
                                    }
                                    Map<Path,Double> lineInfo = new HashMap<>();
                                    for (com.vividsolutions.jts.geom.Geometry jtsG : geometryLineInfo){
                                        if(Double.isNaN(jtsG.getCentroid().getY())){
                                            continue;
                                        }
                                        Coordinate[] coordinates = jtsG.getCoordinates();
                                        Path path = new Path();
                                        Paint paint = new Paint();
                                        paint.setTextSize( MeasureUtil.dp2px(_context,15));
                                        PointF pointF1 = _mapBoxMap.getProjection().toScreenLocation(new LatLng(coordinates[1].y, coordinates[1].x));
                                        PointF pointF2 = _mapBoxMap.getProjection().toScreenLocation(new LatLng(coordinates[0].y, coordinates[0].x));
                                        com.vividsolutions.jts.geom.Geometry jtsGeometry = Pro4jUtil.pro84To2000(jtsG);
                                        //为了反向绘制的时候标注长度方向是正的，path按x由小到大
                                        if(pointF2.x<pointF1.x){
                                            path.moveTo(pointF2.x,pointF2.y);
                                            path.lineTo(pointF1.x,pointF1.y);
                                        }else {
                                            path.moveTo(pointF1.x,pointF1.y);
                                            path.lineTo(pointF2.x,pointF2.y);
                                        }
                                        lineInfo.put(path,jtsGeometry.getLength());
                                    }
                                    sketchHelpView.setLineInfo(lineInfo);
                                }
                            }

                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        e.fillInStackTrace();
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    /**
     * *****************************************有关橡皮绳结束**************************************
     */
    @Override
    public void startSketch(){
        activate();
    }
    @Override
    public void stopSketch() {
        deactivate();
    }

    @Override
    public Boolean isStartSketch() {
        return isStartSketch;
    }

    @Override
    public Geometry getSketchGeometry() {
        return getGeometry();
    }

    @Override
    public void addSketchGeometry(@androidx.annotation.NonNull Geometry geometry) {
        addGeometry(geometry);
    }


    public void onDestroy() {
        if (this.backStack != null) {
            backStack.clear();
        }
        if (this.cacheStack != null) {
            cacheStack.clear();
        }
        backStack = null;
        cacheStack = null;
    }


    public void refresh() {
        if (this.backStack != null) {
            backStack.clear();
        }
        if (this.cacheStack != null) {
            cacheStack.clear();
        }
    }


    @Override
    public boolean onMapClick(@androidx.annotation.NonNull LatLng point) {
        addPoint(point);
        return true;
    }

    @Override
    public boolean onMapLongClick(@androidx.annotation.NonNull LatLng point) {
        beforeDragPoint(point);
        return true;
    }

    @Override
    public boolean onTouchStart(MotionEvent motionEvent) {
        return false;
    }

    @Override
    public boolean onTouchMoving(MotionEvent motionEvent) {
        return underDragPoint(motionEvent);
    }

    @Override
    public boolean onTouchCancel(MotionEvent motionEvent) {
        afterDragPoint();
        return false;
    }
}
