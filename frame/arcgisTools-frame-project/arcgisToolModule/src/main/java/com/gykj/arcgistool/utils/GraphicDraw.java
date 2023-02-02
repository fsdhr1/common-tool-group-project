package com.gykj.arcgistool.utils;

import android.content.Context;
import android.graphics.Color;

import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.PointCollection;
import com.esri.arcgisruntime.geometry.PolygonBuilder;
import com.esri.arcgisruntime.geometry.Polyline;
import com.esri.arcgisruntime.geometry.SpatialReference;
import com.esri.arcgisruntime.mapping.view.Graphic;
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.esri.arcgisruntime.symbology.SimpleFillSymbol;
import com.esri.arcgisruntime.symbology.SimpleLineSymbol;
import com.esri.arcgisruntime.symbology.SimpleMarkerSymbol;
import com.gykj.arcgistool.common.GraphType;
import com.gykj.arcgistool.common.Variable;
import com.gykj.arcgistool.listener.IDrawListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Graphic绘制工具
 */
public class GraphicDraw {
    private Context context;
    private MapView mapView;
    private GraphicsOverlay drawGraphicOverlay=null;//绘制面板
    private GraphicsOverlay pointGraphicOverlay=null;
    private List<Point> pointList=null;//每次绘制点的集合
    private SimpleMarkerSymbol pointSymbol= null;//点样式
    private SimpleLineSymbol lineSymbol = null;//线样式
    private SimpleFillSymbol simpleFillSymbol = null;//
    private GraphType graphType;
    private IDrawListener drawListener;
    public GraphicDraw(Context context,MapView mapView) {
        this.context=context;
        this.mapView=mapView;
    }
    public void init(){
        pointList=new ArrayList<>();
        pointSymbol = new SimpleMarkerSymbol(SimpleMarkerSymbol.Style.CIRCLE, Color.GRAY, 8);
        lineSymbol = new SimpleLineSymbol(SimpleLineSymbol.Style.SOLID, Color.parseColor("#FC8145"), 3.0f);
        simpleFillSymbol = new SimpleFillSymbol(SimpleFillSymbol.Style.SOLID, Color.parseColor("#33e97676"), lineSymbol);
        drawGraphicOverlay = new GraphicsOverlay();
        pointGraphicOverlay= new GraphicsOverlay();
        mapView.getGraphicsOverlays().add(drawGraphicOverlay);
        mapView.getGraphicsOverlays().add(pointGraphicOverlay);
    }
    public void startDraw(GraphType graphType){
        clearGraphics();
        this.graphType=graphType;
        if(drawListener!=null){
            drawListener.onDrawStart(graphType);
        }
    }
    public void drawing(float x,float y){
        if(graphType== GraphType.LINE){
            Point point = mapView.screenToLocation(new android.graphics.Point(Math.round(x), Math.round(y)));
            pointList.add(point);
            Graphic pointGraphic = new Graphic(point, pointSymbol);
            pointGraphicOverlay.getGraphics().add(pointGraphic);
            PointCollection mPointCollection =new PointCollection(mapView.getSpatialReference());
            mPointCollection.addAll(pointList);
            Polyline polyline = new Polyline(mPointCollection);
            Graphic graphic = new Graphic(polyline, lineSymbol);
            drawGraphicOverlay.getGraphics().add(graphic);
        }
        if(graphType == GraphType.POLYGON){
            Point location = mapView.screenToLocation(new android.graphics.Point(Math.round(x), Math.round(y)));
            pointList.add(location);
            Graphic pointGraphic = new Graphic(location, pointSymbol);
            pointGraphicOverlay.getGraphics().add(pointGraphic);

            PolygonBuilder polygonGeometry = new PolygonBuilder(mapView.getSpatialReference());
            for(Point point:pointList){
                polygonGeometry.addPoint(point);
            }
            drawGraphicOverlay.getGraphics().clear();
            Graphic polygonGraphic = new Graphic(polygonGeometry.toGeometry(),simpleFillSymbol);
            drawGraphicOverlay.getGraphics().add(polygonGraphic);
        }
        if(graphType == GraphType.BOX){
            Point point=mapView.screenToLocation(new android.graphics.Point(Math.round(x), Math.round(y)));
            pointList.add(point);
            Graphic pointGraphic = new Graphic(point, pointSymbol);
            pointGraphicOverlay.getGraphics().add(pointGraphic);
            if(pointList.size()==2){
                drawBox(pointList.get(0),pointList.get(1));
                pointList.clear();
                if(drawListener!=null){
                    Graphic graphic=null;
                    if(!drawGraphicOverlay.getGraphics().isEmpty()) {
                        graphic = drawGraphicOverlay.getGraphics().get(0);
                    }
                    drawListener.onDrawEnd(graphType,graphic);
                }
            }
        }
    }
    public void drawBox(Point point1,Point point2) {
        PolygonBuilder polygonGeometry = new PolygonBuilder(mapView.getSpatialReference());
        polygonGeometry.addPoint(point1);
        polygonGeometry.addPoint(new Point(point1.getX(),point2.getY()));
        polygonGeometry.addPoint(point2);
        polygonGeometry.addPoint(new Point(point2.getX(),point1.getY()));
        Graphic polygonGraphic = new Graphic(polygonGeometry.toGeometry(),simpleFillSymbol);
        drawGraphicOverlay.getGraphics().add(polygonGraphic);
    }
    public boolean hasEnd(){
        return graphType==null;
    }
    public Graphic endDraw(){
        Graphic graphic=null;
        if(graphType==GraphType.POLYGON) {
            if(!drawGraphicOverlay.getGraphics().isEmpty()) {
                graphic = drawGraphicOverlay.getGraphics().get(0);
            }
        }
        if(graphType== GraphType.LINE){
            if(!drawGraphicOverlay.getGraphics().isEmpty()) {
                graphic = drawGraphicOverlay.getGraphics().get(0);
            }
        }
        graphType=null;
        drawGraphicOverlay.getGraphics().clear();
        pointGraphicOverlay.getGraphics().clear();
        pointList.clear();
        return graphic;
    }

    public GraphType getGraphType() {
        return graphType;
    }

    public void setGraphType(GraphType graphType) {
        this.graphType = graphType;
    }

    public void clearGraphics(){
        drawGraphicOverlay.getGraphics().clear();
        pointGraphicOverlay.getGraphics().clear();
        pointList.clear();
    }

    public IDrawListener getDrawListener() {
        return drawListener;
    }

    public void setDrawListener(IDrawListener drawListener) {
        this.drawListener = drawListener;
    }
}
