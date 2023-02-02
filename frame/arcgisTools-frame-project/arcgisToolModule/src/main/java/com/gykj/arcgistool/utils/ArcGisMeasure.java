package com.gykj.arcgistool.utils;

import android.content.Context;

import com.esri.arcgisruntime.geometry.GeodeticCurveType;
import com.esri.arcgisruntime.geometry.GeometryEngine;
import com.esri.arcgisruntime.geometry.Point;
import com.esri.arcgisruntime.geometry.PolygonBuilder;
import com.esri.arcgisruntime.geometry.PolylineBuilder;
import com.esri.arcgisruntime.geometry.SpatialReference;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.gykj.arcgistool.common.DrawType;
import com.gykj.arcgistool.common.Measure;
import com.gykj.arcgistool.entity.DrawEntity;

import java.util.ArrayList;
import java.util.List;

public class ArcGisMeasure extends Draw{
    private Context context;
    private MapView mapView;
    private DrawType drawType=null;
    private Measure measureLengthType=Measure.M;
    private Measure measureAreaType=Measure.M2;
    private double lineLength=0;
    private List<Double> lengthList;
    private List<Double> tmpLengthList;
    public ArcGisMeasure(Context context, MapView mapView) {
        super(context, mapView);
        this.context=context;
        this.mapView=mapView;
        lengthList=new ArrayList<>();
        tmpLengthList=new ArrayList<>();
    }

    public void startMeasuredLength(float screenX,float screenY){
        if(drawType==null) {
            super.startLine();
            drawType=DrawType.LINE;
        }
        drawScreenPoint(screenX,screenY);
    }

    public void startMeasuredLength(android.graphics.Point screenPoint){
        if(drawType==null) {
            super.startLine();
            drawType=DrawType.LINE;
        }
        drawScreenPoint(screenPoint);
    }
    public void startMeasuredArea(float screenX,float screenY){
        if(drawType==null) {
            super.startPolygon();
            drawType=DrawType.POLYGON;
        }
        drawScreenPoint(screenX,screenY);
    }
    public void startMeasuredArea(android.graphics.Point screenPoint){
        if(drawType==null) {
            super.startPolygon();
            drawType=DrawType.POLYGON;
        }
        drawScreenPoint(screenPoint);
    }



    @Override
    public boolean prevDraw(){
        if(lengthList.size()>1) {
            lengthList.remove(lengthList.size() - 1);
            lineLength=lengthList.get(lengthList.size()-1);
        }else{
            lengthList.clear();
            lineLength=0;
        }
        return super.prevDraw();
    }

    @Override
    public boolean nextDraw(){
        if(lengthList.size()>0 && lengthList.size()<tmpLengthList.size()) {
            lengthList.add(tmpLengthList.get(lengthList.size()));
            lineLength=lengthList.get(lengthList.size()-1);
        }
        return super.nextDraw();
    }

    public DrawEntity endMeasure(){
        drawType=null;
        lineLength=0;
        tmpLengthList.clear();
        lengthList.clear();
        return super.endDraw();
    }
    public DrawEntity clearMeasure(){
        drawType=null;
        lineLength=0;
        tmpLengthList.clear();
        lengthList.clear();
        return super.clear();
    }
    @Override
    public void setSpatialReference(SpatialReference spatialReference) {
        super.setSpatialReference(spatialReference);
    }

    public void setLengthType(Measure type){
        this.measureLengthType=type;
    }
    public void setAreaType(Measure type){
        this.measureAreaType=type;
    }
    private void drawScreenPoint(float x, float y){
        Point point=super.screenToPoint(x,y);
        if(mapView.getSpatialReference().getWkid()==4490 || mapView.getSpatialReference().getWkid()==4326){
            point = (Point) GeometryEngine.project(point ,SpatialReference.create(102100));
            super.setSpatialReference(SpatialReference.create(102100));
        }
        if( drawType==DrawType.LINE){
            PolylineBuilder line=(PolylineBuilder)super.drawByGisPoint(point);
            showLength(line,point);
        }else if(drawType==DrawType.POLYGON){
            PolygonBuilder polygon=(PolygonBuilder)super.drawByGisPoint(point);
            showArea(polygon);
        }

    }
    private void drawScreenPoint(android.graphics.Point screenPoint){
        Point point=super.screenToPoint(screenPoint.x,screenPoint.y);
        if( drawType==DrawType.LINE){
            PolylineBuilder line=(PolylineBuilder)super.drawByScreenPoint(screenPoint);
            showLength(line,point);
        }else if(drawType==DrawType.POLYGON){
            PolygonBuilder polygon=(PolygonBuilder)super.drawByScreenPoint(screenPoint);
            showArea(polygon);
        }
    }
    private void showLength(PolylineBuilder line,Point point){
        if(line!=null) {
            double length = GeometryEngine.lengthGeodetic(line.toGeometry(),null, GeodeticCurveType.SHAPE_PRESERVING);
            lineLength+=length;
            lengthList.add(lineLength);
            tmpLengthList.clear();
            tmpLengthList.addAll(lengthList);
            String text=Util.lengthFormat(lineLength);
            super.drawText(point,text,false);
        }
    }

    private void showArea(PolygonBuilder polygon){
        if(polygon!=null) {
            double area = GeometryEngine.areaGeodetic(polygon.toGeometry(),null, GeodeticCurveType.SHAPE_PRESERVING);
            String text=Util.areaFormat(area);
            super.drawText(polygon.toGeometry().getExtent().getCenter(),text,true);
        }
    }
}
