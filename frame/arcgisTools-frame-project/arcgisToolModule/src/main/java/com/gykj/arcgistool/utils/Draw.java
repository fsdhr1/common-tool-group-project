package com.gykj.arcgistool.utils;

import android.content.Context;
import android.graphics.Color;

import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.PolygonBuilder;
import com.esri.arcgisruntime.geometry.PolylineBuilder;
import com.esri.arcgisruntime.geometry.SpatialReference;
import com.esri.arcgisruntime.mapping.view.Graphic;
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.symbology.SimpleFillSymbol;
import com.esri.arcgisruntime.symbology.SimpleLineSymbol;
import com.esri.arcgisruntime.symbology.SimpleMarkerSymbol;
import com.esri.arcgisruntime.symbology.TextSymbol;
import com.gykj.arcgistool.R;
import com.gykj.arcgistool.common.DrawType;
import com.gykj.arcgistool.entity.DrawEntity;

import java.util.ArrayList;
import java.util.List;

import androidx.core.content.ContextCompat;

/**
 * 绘制工具
 * @author jzq
 * @date 2021/4/23
 */
public class Draw {
    private Context context;
    private MapView mapView;
    private SpatialReference spatialReference;
    //绘制面板
    private GraphicsOverlay drawTextGraphicOverlay=null;
    private GraphicsOverlay drawPointGraphicOverlay=null;
    private GraphicsOverlay drawLineGraphicOverlay=null;
    private GraphicsOverlay drawPolygonGraphicOverlay=null;
    //文字集合
    private List<GraphicsOverlay> textGraphic=null;
    //面集合
    private List<GraphicsOverlay> polygonGraphic=null;
    //线集合
    private List<GraphicsOverlay> lineGraphic=null;
    //点集合
    private List<GraphicsOverlay> pointGraphic=null;
    //标注样式
    private TextSymbol textSymbol=null;
    //点样式
    private SimpleMarkerSymbol pointSymbol=null;
    //线样式
    private SimpleLineSymbol lineSymbol=null;
    //面样式
    private SimpleFillSymbol polygonSymbol =null;
    //绘制点的集合的集合
    private List<List<Point>>  pointGroup=null;
    //每次绘制点的集合
    private List<Point> pointList=null;
    //撤销、恢复临时点集合
    private List<Point> tmpPointList=null;
    //每次绘制文字的集合
    private List<TextSymbol> textList=null;
    //撤销、恢复临时文字集合
    private List<TextSymbol> tmpTextList=null;
    //每次绘制文字的集合
    private List<Graphic> textPointList=null;
    //撤销、恢复临时文字集合
    private List<Graphic> tmpTextPointList=null;
    //绘制类型
    private DrawType drawType=null;
    //是否在恢复或者撤销
    private boolean isNext=false,isPrev=false;
    private static final int MAX_POINT_COUNT = 3;
    public Draw(Context context, MapView mapView){
        this.context=context;
        this.mapView=mapView;
        init();
    }

    private void init(){
        pointList=new ArrayList<>();
        tmpPointList=new ArrayList<>();
        textList=new ArrayList<>();
        tmpTextList=new ArrayList<>();
        textPointList=new ArrayList<>();
        tmpTextPointList=new ArrayList<>();
        textGraphic=new ArrayList<>();
        polygonGraphic=new ArrayList<>();
        lineGraphic=new ArrayList<>();
        pointGraphic=new ArrayList<>();
        pointGroup=new ArrayList<>();
        pointSymbol = new SimpleMarkerSymbol(SimpleMarkerSymbol.Style.CIRCLE, Color.GRAY, 8);
        lineSymbol = new SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.RED, 2);
        polygonSymbol = new SimpleFillSymbol(SimpleFillSymbol.Style.SOLID, Color.argb(40,0,0,0), null);
        textSymbol = new TextSymbol(12,"", R.color.colorMain, TextSymbol.HorizontalAlignment.LEFT,TextSymbol.VerticalAlignment.BOTTOM);
    }
    protected void startPoint(){
        drawType= DrawType.POINT;
    }
    protected void startLine(){
        drawType=DrawType.LINE;
    }
    protected void startPolygon(){
        drawType=DrawType.POLYGON;
    }
    protected void setSpatialReference(SpatialReference spatialReference) {
        this.spatialReference = spatialReference;
    }

    private SpatialReference getSpatialReference() {
        if(spatialReference==null){
            return mapView.getSpatialReference();
        }
        return spatialReference;
    }

    protected Object drawByScreenPoint(android.graphics.Point point){
        if(drawType==DrawType.POINT){
            return drawPointByScreenPoint(point);
        }else if(drawType==DrawType.LINE){
            return drawLineByScreenPoint(point);
        }else if(drawType==DrawType.POLYGON){
            return drawPolygonByScreenPoint(point);
        }
        return null;
    }

    protected Object drawByGisPoint(Point point){
        if(drawType==DrawType.POINT){
            return drawPointByGisPoint(point);
        }else if(drawType==DrawType.LINE){
            return drawLineByGisPoint(point);
        }else if(drawType==DrawType.POLYGON){
            return drawPolygonByGisPoint(point);
        }
        return null;
    }

    protected Object drawByScreenPoint(float x, float y){
        if(drawType==DrawType.POINT){
            return drawPointByScreenPoint(x,y);
        }else if(drawType==DrawType.LINE){
            return drawLineByScreenPoint(x,y);
        }else if(drawType==DrawType.POLYGON){
            return drawPolygonByScreenPoint(x, y);
        }
        return null;
    }
    protected void drawPointByGisPoint(float x, float y){
        if(drawType==DrawType.POINT){
            drawPointByGisPoint(x,y);
        }else if(drawType==DrawType.LINE){
            drawLineByGisPoint(x,y);
        }else if(drawType==DrawType.POLYGON){
            drawPolygonByGisPoint(x, y);
        }
    }
    private void drawText(Point point){
        if(drawTextGraphicOverlay==null){
            drawTextGraphicOverlay = new GraphicsOverlay();
            mapView.getGraphicsOverlays().add(drawTextGraphicOverlay);
            textGraphic.add(drawTextGraphicOverlay);
        }
        Graphic pointGraphic = new Graphic(point,textSymbol);
        drawTextGraphicOverlay.getGraphics().add(pointGraphic);
        if(!isPrev) {
            textList.add(textSymbol);
        }
        if(!isNext && !isPrev) {
            tmpTextList.clear();
            tmpTextList.addAll(textList);
        }
    }

    private void drawPoint(Point point){
        if(drawPointGraphicOverlay==null){
            drawPointGraphicOverlay = new GraphicsOverlay();
            mapView.getGraphicsOverlays().add(drawPointGraphicOverlay);
            pointGraphic.add(drawPointGraphicOverlay);
        }
        Graphic pointGraphic = new Graphic(point,pointSymbol);
        drawPointGraphicOverlay.getGraphics().add(pointGraphic);
        pointList.add(point);
        if(!isNext) {
            tmpPointList.clear();
            tmpPointList.addAll(pointList);
        }
    }

    private PolylineBuilder drawLine(Point point1, Point point2){
        //绘制面板为空，说明重新绘制一个line，在地图和线集合里添加一个新line
        if(drawLineGraphicOverlay==null){
            drawLineGraphicOverlay = new GraphicsOverlay();
            mapView.getGraphicsOverlays().add(drawLineGraphicOverlay);
            lineGraphic.add(drawLineGraphicOverlay);
        }

        PolylineBuilder lineGeometry = new PolylineBuilder(getSpatialReference());
        lineGeometry.addPoint(point1);
        lineGeometry.addPoint(point2);
        Graphic lineGraphic = new Graphic(lineGeometry.toGeometry(),lineSymbol);
        drawLineGraphicOverlay.getGraphics().add(lineGraphic);
        return lineGeometry;
    }
    private PolygonBuilder drawPolygon(){
        //绘制面板为空，说明重新绘制一个Polyline，在地图和面集合里添加一个新Polyline
        if(drawPolygonGraphicOverlay==null){
            drawPolygonGraphicOverlay = new GraphicsOverlay();
            mapView.getGraphicsOverlays().add(drawPolygonGraphicOverlay);
            polygonGraphic.add(drawPolygonGraphicOverlay);
        }
        PolygonBuilder polygonGeometry = new PolygonBuilder(getSpatialReference());
        for(Point point:pointList){
            polygonGeometry.addPoint(point);
        }
        drawPolygonGraphicOverlay.getGraphics().clear();
        Graphic polygonGraphic = new Graphic(polygonGeometry.toGeometry(),polygonSymbol);
        drawPolygonGraphicOverlay.getGraphics().add(polygonGraphic);
        return polygonGeometry;

    }
    private Point drawPointByScreenPoint(android.graphics.Point point){
        Point center = mapView.screenToLocation(point);
        this.drawPoint(center);
        return center;
    }

    private Point drawPointByGisPoint(Point point){
        this.drawPoint(point);
        return point;
    }
    private Point drawPointByScreenPoint(float x, float y){
        android.graphics.Point point=new android.graphics.Point(Math.round(x),Math.round(y));
        Point center = mapView.screenToLocation(point);
        this.drawPoint(center);
        return center;
    }
    private Point drawPointByGisPoint(double x, double y){
        Point center = new Point(x, y);
        this.drawPoint(center);
        return center;
    }
    private PolylineBuilder drawLineByScreenPoint(android.graphics.Point point){
        Point nextPoint=this.drawPointByScreenPoint(point);
        if(getPointSize()>1) {
            Point prvPoint=getLastPoint();
            return this.drawLine(prvPoint,nextPoint);
        }
        return null;
    }
    private PolylineBuilder drawLineByGisPoint(Point point){
        Point nextPoint=this.drawPointByGisPoint(point);
        if(getPointSize()>1) {
            Point prvPoint=getLastPoint();
            return this.drawLine(prvPoint,nextPoint);
        }
        return null;
    }
    private PolylineBuilder drawLineByGisPoint(Point point1,Point point2){
        return this.drawLine(point1,point2);
    }

    private PolylineBuilder drawLineByScreenPoint(float x, float y){
        Point nextPoint=this.drawPointByScreenPoint(x,y);
        if(getPointSize()>1) {
            Point prvPoint=getLastPoint();
            return this.drawLine(prvPoint,nextPoint);
        }
        return null;
    }
    private PolylineBuilder drawLineByGisPoint(double x, double y){
        Point nextPoint=this.drawPointByGisPoint(x,y);
        if(getPointSize()>1) {
            Point prvPoint=getLastPoint();
            return this.drawLine(prvPoint,nextPoint);
        }
        return null;
    }

    private PolygonBuilder drawPolygonByScreenPoint(float x, float y){
        drawLineByScreenPoint(x,y);
        if(getPointSize()>= MAX_POINT_COUNT) {
            return drawPolygon();
        }
        return null;
    }

    private PolygonBuilder drawPolygonByScreenPoint(android.graphics.Point point){
        drawLineByScreenPoint(point);
        if(getPointSize()>= MAX_POINT_COUNT) {
            return drawPolygon();
        }
        return null;
    }

    private PolygonBuilder drawPolygonByGisPoint(double x, double y){
        drawLineByGisPoint(x,y);
        if(getPointSize()>= MAX_POINT_COUNT) {
            return drawPolygon();
        }
        return null;
    }

    private PolygonBuilder drawPolygonByGisPoint(Point point){
        drawLineByGisPoint(point);
        if(getPointSize()>= MAX_POINT_COUNT) {
            return drawPolygon();
        }
        return null;
    }
    protected Point screenToPoint(float x, float y){
        android.graphics.Point point=new android.graphics.Point(Math.round(x),Math.round(y));
        return mapView.screenToLocation(point);
    }

    protected void drawText(Point point,String text,boolean replaceOld){
        textSymbol = getTextSymbol();
        if(replaceOld) {
            textSymbol.setHorizontalAlignment(TextSymbol.HorizontalAlignment.CENTER);
            if(drawTextGraphicOverlay!=null) {
                drawTextGraphicOverlay.getGraphics().clear();
            }
        }
        textSymbol.setText(text);
        drawText(point);
    }
    protected boolean prevDraw(){
        if(pointList.size()>1){
            pointList.remove(pointList.size()-1);
            if(textList.size()>0) {
                textList.remove(textList.size() - 1);
            }
            removePrevGraphics(drawPointGraphicOverlay);
            removePrevGraphics(drawLineGraphicOverlay);
            if(drawType==DrawType.LINE){
                removePrevGraphics(drawTextGraphicOverlay);
            }else if(drawType==DrawType.POLYGON){
                if(textList.size()>1) {
                    textSymbol = tmpTextList.get(textList.size() - 1);
                }
                PolygonBuilder pb=drawPolygon();
                isPrev=true;
                if(drawTextGraphicOverlay!=null) {
                    drawTextGraphicOverlay.getGraphics().clear();
                }
                if(pointList.size()>2) {
                    drawText(pb.toGeometry().getExtent().getCenter());
                }
                isPrev=false;
            }
        }
        return pointList.size()>1?true:false;
    }
    protected boolean nextDraw(){
        isNext=true;
        if(pointList.size()>0 && pointList.size()<tmpPointList.size()){
            int index=pointList.size();
            textSymbol=tmpTextList.get(textList.size());
            if(drawType==DrawType.LINE) {
                drawLineByGisPoint(tmpPointList.get(index));
                drawText(tmpPointList.get(index));
            }else if(drawType==DrawType.POLYGON){
                PolygonBuilder pb=drawPolygonByGisPoint(tmpPointList.get(index));
                if(drawTextGraphicOverlay!=null) {
                    drawTextGraphicOverlay.getGraphics().clear();
                }
                if(pointList.size()>2) {
                    drawText(pb.toGeometry().getExtent().getCenter());
                }
            }

        }
        isNext=false;
        return (pointList.size()>0 && pointList.size()<tmpPointList.size())?true:false;
    }
    protected DrawEntity endDraw(){
        if(drawType==DrawType.POLYGON) {
            if (getPointSize() >= MAX_POINT_COUNT) {
                this.drawLineByGisPoint(getEndPoint(), getFristPoint());
            }
        }
        if(pointList.size()>0) {
            pointGroup.add(pointList);
        }
        DrawEntity de=allDraw();
        drawType=null;
        drawPolygonGraphicOverlay=null;
        drawLineGraphicOverlay=null;
        drawPointGraphicOverlay=null;
        drawTextGraphicOverlay=null;
        pointList=new ArrayList<>();
        return de;
    }

    protected DrawEntity clear(){
        DrawEntity de=allDraw();
        removeAllGraphics(lineGraphic);
        removeAllGraphics(polygonGraphic);
        removeAllGraphics(pointGraphic);
        removeAllGraphics(textGraphic);
        drawPolygonGraphicOverlay=null;
        drawLineGraphicOverlay=null;
        drawPointGraphicOverlay=null;
        drawTextGraphicOverlay=null;
        drawType=null;
        pointGroup.clear();
        pointList.clear();
        lineGraphic.clear();
        pointGraphic.clear();
        textGraphic.clear();
        pointGraphic.clear();
        pointGroup=new ArrayList<>();
        pointList=new ArrayList<>();
        lineGraphic=new ArrayList<>();
        pointGraphic=new ArrayList<>();
        textGraphic=new ArrayList<>();
        pointGraphic=new ArrayList<>();
        return de;
    }

    private DrawEntity allDraw(){
        DrawEntity de=new DrawEntity();
        de.setLineGraphic(lineGraphic);
        de.setPointGraphic(pointGraphic);
        de.setTextGraphic(textGraphic);
        de.setPolygonGraphic(polygonGraphic);
        de.setPointGroup(pointGroup);
        return de;
    }

    private void removeAllGraphics(List<GraphicsOverlay> go){
        if(go.size()>0){
            for(GraphicsOverlay graphics:go){
                mapView.getGraphicsOverlays().remove(graphics);
            }
        }
    }
    private void removePrevGraphics(GraphicsOverlay gho){
        gho.getGraphics().remove(gho.getGraphics().size()-1);
    }

    private TextSymbol getTextSymbol(){
        TextSymbol textSymbol = new TextSymbol(12,"", Color.BLACK, TextSymbol.HorizontalAlignment.LEFT,TextSymbol.VerticalAlignment.BOTTOM);
        textSymbol.setColor(Color.WHITE);
        textSymbol.setHaloColor(Color.WHITE);
        textSymbol.setHaloWidth(1);
        textSymbol.setOutlineColor(ContextCompat.getColor(context,R.color.color444));
        textSymbol.setOutlineWidth(1);
        return textSymbol;
    }
    private int getPointSize(){
        return pointList.size();
    }

    public Point getEndPoint(){
        int index=getPointSize()>1?getPointSize()-1:0;
        Point point=pointList.get(index);
        return point;
    }

    private Point getLastPoint(){
        int index=getPointSize()>1?getPointSize()-2:0;
        Point point=pointList.get(index);
        return point;
    }
    private Point getFristPoint(){
        if(getPointSize()==0) {
            return null;
        }
        Point point=pointList.get(0);
        return point;
    }
}
