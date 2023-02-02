package com.grandtech.mapframe.core.layer;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PointF;
import android.net.Uri;
import android.util.Log;
import android.widget.ListView;

import androidx.annotation.NonNull;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.grandtech.mapframe.core.R;
import com.grandtech.mapframe.core.enumeration.GeometryType;
import com.grandtech.mapframe.core.rules.Rules;
import com.grandtech.mapframe.core.util.CollectUtil;
import com.grandtech.mapframe.core.util.LayerUtil;
import com.grandtech.mapframe.core.util.StringEngine;
import com.grandtech.mapframe.core.util.Transformation;
import com.mapbox.geojson.BoundingBox;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Geometry;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngQuad;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.style.expressions.Expression;
import com.mapbox.mapboxsdk.style.layers.CircleLayer;
import com.mapbox.mapboxsdk.style.layers.FillExtrusionLayer;
import com.mapbox.mapboxsdk.style.layers.FillLayer;
import com.mapbox.mapboxsdk.style.layers.HeatmapLayer;
import com.mapbox.mapboxsdk.style.layers.Layer;
import com.mapbox.mapboxsdk.style.layers.LineLayer;
import com.mapbox.mapboxsdk.style.layers.Property;
import com.mapbox.mapboxsdk.style.layers.PropertyFactory;
import com.mapbox.mapboxsdk.style.layers.PropertyValue;
import com.mapbox.mapboxsdk.style.layers.RasterLayer;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.mapboxsdk.style.sources.ImageSource;
import com.mapbox.mapboxsdk.style.sources.Source;
import com.mapbox.mapboxsdk.style.sources.TileSet;
import com.mapbox.mapboxsdk.style.sources.VectorSource;
import com.vividsolutions.jts.geom.Envelope;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineCap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineJoin;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineOpacity;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineWidth;

/**
 * @ClassName LayerManager
 * @Description TODO 地图style解析以及图层数据源过滤
 * @Author: fs
 * @Date: 2021/6/3 10:44
 * @Version 2.0
 */
public class StyleLayerManager implements Rules {

    private MapboxMap mMapBoxMap;
    /**
     * 存储配图的layer的json
     */
    private HashMap<String,String> mLayerJsonMap;

    /**
     * 存储配图的source的json
     */
    private HashMap<String,String> mSourceJsonMap;

    public StyleLayerManager(@NonNull MapboxMap mapBoxMap){
        this.mMapBoxMap = mapBoxMap;
    }
    /**
     * @Description 获取地图样式
     * @return
     */
    public String getMapStyleStr() {
        if (this.mMapBoxMap == null) {
            return null;
        }
        return Objects.requireNonNull(this.mMapBoxMap.getStyle()).getJson();
    }
    /**
     * 根据itme 获取到最底层地图样式
     *
     * @return
     */
    public <T> T getStylePropertyItem(String... item) {
        try {
            String mapStyle = getMapStyleStr();
            if (item == null) {
                return (T) mapStyle;
            }
            String property = mapStyle;
            String propertyItem;
            for (int i = 0; i < item.length; i++) {
                propertyItem = item[i];
                if (propertyItem instanceof String) {
                    property = StringEngine.getMinJsonStructure(property, "\"" + propertyItem + "\"");
                } else {
                    property = StringEngine.getMinJsonStructure(property, propertyItem.toString());
                }
            }
            JsonObject styleJsonObj = new JsonParser().parse(property).getAsJsonObject();
            JsonPrimitive jsonPrimitive = styleJsonObj.getAsJsonPrimitive(item[item.length - 1].toString());
            return (T) Transformation.analysisJsonPrimitive(jsonPrimitive);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    public String getJson(String fileName, Context context) {
        //将json数据变成字符串
        StringBuilder stringBuilder = new StringBuilder();
        try {
            //获取assets资源管理器
            AssetManager assetManager = context.getAssets();
            //通过管理器打开文件并读取
            BufferedReader bf = new BufferedReader(new InputStreamReader(
                    assetManager.open(fileName)));
            String line;
            while ((line = bf.readLine()) != null) {
                stringBuilder.append(line);
            }
            bf.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stringBuilder.toString();
    }
    /**
     * 获取layer:groups 异步
     * @return
     */
    public void getStyleLayerGroupsAsync(IDoneListenerCallBack<LinkedHashMap<String,List<Layer>>> iDoneListenerCallBack) {
        String mapStyle = getMapStyleStr();//"{\"transition\":{\"duration\":300,\"delay\":0},\"scope\":\"private\",\"is_deleted\":false,\"type\":\"normal\",\"tags\":[],\"version\":8,\"center\":[114.07516505496403,-53.04621767959836],\"bearing\":0,\"pitch\":0,\"layers\":[{\"id\":\"背景\",\"type\":\"background\",\"metadata\":{\"mapbox:group\":\"92ca48f13df25\"},\"paint\":{\"background-color\":\"#87c2e4\"}},{\"id\":\"天地图\",\"type\":\"raster\",\"source\":\"tiandituapp\",\"metadata\":{\"mapbox:group\":\"5cedd950e603b\"},\"minzoom\":1,\"maxzoom\":20,\"paint\":{\"raster-opacity\":1}},{\"id\":\"省域\",\"type\":\"line\",\"source\":\"dt_sy\",\"source-layer\":\"dt_sy\",\"metadata\":{\"mapbox:group\":\"73346d0482376\"},\"minzoom\":3,\"maxzoom\":7,\"paint\":{\"line-color\":\"#f5e205\",\"line-opacity\":1,\"line-width\":2}},{\"id\":\"省点\",\"type\":\"symbol\",\"source\":\"dt_sy\",\"source-layer\":\"dt_sy\",\"metadata\":{\"mapbox:group\":\"73346d0482376\"},\"minzoom\":3,\"maxzoom\":5,\"layout\":{\"text-field\":\"{qhmc}\",\"text-font\":[\"SimSun Bold\"],\"text-size\":18,\"text-allow-overlap\":true,\"text-ignore-placement\":true},\"paint\":{\"text-opacity\":1,\"text-color\":\"#030003\",\"text-halo-color\":\"#ffffff\",\"text-halo-width\":1.5}},{\"id\":\"地市域\",\"type\":\"line\",\"source\":\"dt_dsy_pt\",\"source-layer\":\"dt_dsy_pt\",\"metadata\":{\"mapbox:group\":\"73346d0482376\"},\"minzoom\":6,\"maxzoom\":8,\"paint\":{\"line-color\":\"#c98f22\",\"line-opacity\":1,\"line-width\":1.7}},{\"id\":\"地市点\",\"type\":\"symbol\",\"source\":\"dt_dsy_pt\",\"source-layer\":\"dt_dsy_pt\",\"metadata\":{\"mapbox:group\":\"73346d0482376\"},\"minzoom\":6,\"maxzoom\":8,\"layout\":{\"text-field\":\"{qhmc}\",\"text-font\":[\"SimSun Bold\"],\"text-size\":17,\"icon-allow-overlap\":true,\"icon-ignore-placement\":true},\"paint\":{\"text-opacity\":1,\"text-color\":\"#ffffff\",\"text-halo-color\":\"#8f2da1\",\"text-halo-width\":1.5,\"text-halo-blur\":1}},{\"id\":\"县域\",\"type\":\"line\",\"source\":\"dt_xy\",\"source-layer\":\"dt_xy\",\"metadata\":{\"mapbox:group\":\"73346d0482376\"},\"minzoom\":8,\"maxzoom\":20,\"filter\":[\"any\",[\"has\",\"qhmc\"]],\"paint\":{\"line-color\":\"#64d9fa\",\"line-opacity\":1,\"line-width\":1.5}},{\"id\":\"县点\",\"type\":\"symbol\",\"source\":\"dt_xy_pt\",\"source-layer\":\"dt_xy_pt\",\"metadata\":{\"mapbox:group\":\"73346d0482376\"},\"minzoom\":6,\"maxzoom\":10,\"layout\":{\"text-field\":\"{qhmc}\",\"text-font\":[\"SimSun Bold\"],\"text-size\":16},\"paint\":{\"text-opacity\":1,\"text-color\":\"#ffffff\",\"text-halo-color\":\"#ba5507\",\"text-halo-width\":1,\"text-halo-blur\":0.5}},{\"id\":\"国界\",\"type\":\"line\",\"source\":\"dt_gj\",\"source-layer\":\"dt_gj\",\"metadata\":{\"mapbox:group\":\"73346d0482376\"},\"minzoom\":1,\"maxzoom\":4,\"paint\":{\"line-color\":\"#ffffff\",\"line-opacity\":1,\"line-width\":1.5}},{\"id\":\"机井\",\"type\":\"circle\",\"source\":\"ntsl_jj\",\"source-layer\":\"ntsl_jj\",\"metadata\":{\"mapbox:group\":\"0ac8ab315954\"},\"minzoom\":5,\"maxzoom\":18,\"filter\":[\"any\",[\"==\",\"isused\",\"0\"],[\"!=\",\"jjwttype\",\"null\"]],\"layout\":{\"visibility\":\"none\"},\"paint\":{\"circle-color\":\"#1ce6ba\",\"circle-radius\":4,\"circle-opacity\":0.3}},{\"id\":\"生鲜乳收购站\",\"type\":\"circle\",\"source\":\"xm_ranch\",\"source-layer\":\"xm_ranch\",\"metadata\":{\"mapbox:group\":\"7c46dadc0e6f\"},\"minzoom\":11,\"maxzoom\":17,\"layout\":{\"visibility\":\"none\"},\"paint\":{\"circle-color\":\"#c321cc\",\"circle-radius\":5,\"circle-opacity\":1}},{\"id\":\"屠宰场\",\"type\":\"symbol\",\"source\":\"xm_tzc\",\"source-layer\":\"xm_tzc\",\"metadata\":{\"mapbox:group\":\"7c46dadc0e6f\"},\"minzoom\":11,\"maxzoom\":18,\"layout\":{\"text-field\":\"{name}\",\"text-font\":[\"STJTBZ Regular\"],\"text-size\":10,\"text-anchor\":\"bottom\",\"icon-size\":6,\"icon-allow-overlap\":true},\"paint\":{\"text-opacity\":1,\"text-color\":\"#f2f222\",\"text-halo-width\":1,\"icon-color\":\"#eb28eb\",\"icon-halo-color\":\"#cc1fcc\"}},{\"id\":\"两区信息\",\"type\":\"fill\",\"source\":\"lq_lq\",\"source-layer\":\"lq_lq\",\"metadata\":{\"mapbox:group\":\"f349e84abac52\"},\"minzoom\":7,\"maxzoom\":18,\"paint\":{\"fill-color\":\"#A08C1C\",\"fill-opacity\":0.6}}],\"_id\":\"60de74c466f984042c6cc6c6\",\"name\":\"河南农业时空一张图app\",\"zoom\":8.197179566292915,\"metadata\":{\"template\":{\"type\":\"\",\"level\":0,\"des\":\"0代表全国，1代表省，2代表市，3代表县\"},\"mapbox:groups\":{\"92ca48f13df25\":{\"name\":\"参考影像\",\"collapsed\":false},\"5cedd950e603b\":{\"name\":\"天地图\",\"collapsed\":true},\"73346d0482376\":{\"name\":\"行政区划\",\"collapsed\":true},\"0ac8ab315954\":{\"name\":\"农田水利设施\",\"collapsed\":true},\"7c46dadc0e6f\":{\"name\":\"畜牧\",\"collapsed\":true},\"f349e84abac52\":{\"name\":\"两区片块信息\",\"collapsed\":true}}},\"sources\":{\"tiandituapp\":{\"type\":\"raster\",\"tileSize\":256,\"tiles\":[\"https://t0.tianditu.gov.cn/DataServer?T=img_w&x={x}&y={y}&l={z}&tk=f56a670ca60f24596022a5b1d2608660\"]},\"dt_gj\":{\"type\":\"vector\",\"tiles\":[\"http://10.1.1.155:8849/grandtech-middleground-vectile-wgs/api/v1/tilesets/gykj/dt_gj/{z}/{x}/{y}.pbf\"]},\"dt_sy\":{\"type\":\"vector\",\"tiles\":[\"http://10.1.1.155:8849/grandtech-middleground-vectile-wgs/api/v1/tilesets/gykj/dt_sy/{z}/{x}/{y}.pbf\"]},\"dt_dsy_pt\":{\"type\":\"vector\",\"tiles\":[\"http://10.1.1.155:8849/grandtech-middleground-vectile-wgs/api/v1/tilesets/gykj/dt_dsy_pt/{z}/{x}/{y}.pbf\"]},\"dt_xy\":{\"type\":\"vector\",\"tiles\":[\"http://10.1.1.155:884
        List<Layer> layers = mMapBoxMap.getStyle().getLayers();
        int size = new Double(layers.size()/0.75).intValue()+1;
        LinkedHashMap<String,Layer> layerMap = new LinkedHashMap<>(size);
        for(Layer layer : layers){
            layerMap.put(layer.getId(),layer);
        }
        Observable.create((ObservableOnSubscribe<LinkedHashMap<String,List<Layer>>>) emitter -> {
            LinkedHashMap<String,List<Layer>> listLinkedHashMap = getStyleLayerGroups(mapStyle,layerMap);
            emitter.onNext(listLinkedHashMap);
            emitter.onComplete();
        }).subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<LinkedHashMap<String,List<Layer>>>() {
                    @Override
                    public void onSubscribe(@io.reactivex.annotations.NonNull Disposable d) {

                    }

                    @Override
                    public void onNext( LinkedHashMap<String,List<Layer>> listLinkedHashMap) {
                        iDoneListenerCallBack.onDone(listLinkedHashMap);
                    }

                    @Override
                    public void onError(@io.reactivex.annotations.NonNull Throwable e) {
                        e.fillInStackTrace();
                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }


    /**
     * 获取layer:groups
     * @return
     */
    public LinkedHashMap<String,List<Layer>> getStyleLayerGroups() {
        String mapStyle = getMapStyleStr();
        JsonObject mapStyleStyleJsonObj = new JsonParser().parse(mapStyle).getAsJsonObject();
        String propertyItem = "mapbox:groups";
        JsonArray layerJsonArray = mapStyleStyleJsonObj.get("layers").getAsJsonArray();
        JsonObject styleJsonObj = mapStyleStyleJsonObj.get("metadata").getAsJsonObject();
        JsonObject groupsJsonObject = styleJsonObj.get(propertyItem).getAsJsonObject();
        List<String> layerJsonStringList = new ArrayList<>();
        List<String> groupIds = new ArrayList<>();
        for(int i =0;i<layerJsonArray.size();i++){
            JsonElement jsonElement = layerJsonArray.get(i);
            if(jsonElement == null){
                continue;
            }
            layerJsonStringList.add(jsonElement.toString());
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            if(jsonObject.has("metadata")){
                JsonObject  metadata = jsonObject.get("metadata").getAsJsonObject();
                if(metadata.has("mapbox:group")){
                    String groupName =  metadata.get("mapbox:group").getAsString();
                    if(groupIds.contains(groupName)){
                        continue;
                    }
                    groupIds.add(groupName);
                }
            }
        }
        LinkedHashMap<String,List<Layer>> groups = new LinkedHashMap<>();
        for(String groupNameId : groupIds){
            List<Layer> layerList = new ArrayList<>();
            if(mLayerJsonMap == null){
                int size = new Double(mMapBoxMap.getStyle().getLayers().size()/0.75).intValue()+1;
                mLayerJsonMap = new HashMap<>(size);
            }
            for(Layer layer : mMapBoxMap.getStyle().getLayers()){
                long start = System.currentTimeMillis();
                String layerjson = null;
               for(int i =0;i<layerJsonStringList.size();i++){
                   layerjson = layerJsonStringList.get(i);
                   if(!layerjson.contains(layer.getId())){
                       layerjson = null;
                       continue;
                   }
                   break;
               }
                long end = System.currentTimeMillis();
                Log.i("gmbgl","耗时："+(end-start));
                if(layerjson == null||!layerjson.contains(groupNameId)){
                    continue;
                }
                mLayerJsonMap.put(layer.getId(),layerjson);
                Log.i("gmbgl","layer.getId()："+layer.getId());
                layerList.add(layer);
            }
            if("undefined".equals(groupNameId)){
                continue;
            }
            String groupName = groupsJsonObject.get(groupNameId).getAsJsonObject().get("name").getAsString();
            groups.put(groupName,layerList );
        }
        return groups;
    }
    /**
     * 获取layer:groups
     * @return
     */
    private LinkedHashMap<String,List<Layer>> getStyleLayerGroups(String mapStyleJson,LinkedHashMap<String,Layer> layerMap) {
        String mapStyle = mapStyleJson;
        JsonObject mapStyleStyleJsonObj = new JsonParser().parse(mapStyle).getAsJsonObject();
        String propertyItem = "mapbox:groups";
        JsonArray layerJsonArray = mapStyleStyleJsonObj.get("layers").getAsJsonArray();
        JsonObject styleJsonObj = mapStyleStyleJsonObj.get("metadata").getAsJsonObject();
        JsonObject groupsJsonObject = styleJsonObj.get(propertyItem).getAsJsonObject();
        List<String> groupIds = new ArrayList<>();
        List<String> layerJsonStringList = new ArrayList<>();
        for(int i =0;i<layerJsonArray.size();i++){
            JsonElement jsonElement = layerJsonArray.get(i);
            if(jsonElement == null){
                continue;
            }
            layerJsonStringList.add(jsonElement.toString());
            JsonObject jsonObject = jsonElement.getAsJsonObject();
            if(jsonObject.has("metadata")){
                JsonObject  metadata = jsonObject.get("metadata").getAsJsonObject();
                if(metadata.has("mapbox:group")){
                   String groupName =  metadata.get("mapbox:group").getAsString();
                   if(groupIds.contains(groupName)){
                       continue;
                   }
                    groupIds.add(groupName);
                }
            }
        }
        LinkedHashMap<String,List<Layer>> groups = new LinkedHashMap<>();
        for(String groupNameId : groupIds){
            List<Layer> layerList = new ArrayList<>();
            if(mLayerJsonMap == null){
                int size = new Double(layerMap.size()/0.75).intValue()+1;
                mLayerJsonMap = new HashMap<>(size);
            }
            for(Map.Entry<String, Layer> entry  : layerMap.entrySet()){
                long start = System.currentTimeMillis();
                String layerjson = null;
                for(int i =0;i<layerJsonStringList.size();i++){
                    layerjson = layerJsonStringList.get(i);
                    if(!layerjson.contains(entry.getKey())){
                        layerjson = null;
                        continue;
                    }
                    break;
                }
                long end = System.currentTimeMillis();
                Log.i("gmbgl","耗时："+(end-start));
                if(layerjson == null||!layerjson.contains(groupNameId)){
                    continue;
                }
                mLayerJsonMap.put(entry.getKey(),layerjson);
                Log.i("gmbgl","layer.getId()："+entry.getKey());
                layerList.add(entry.getValue());
            }
            if("undefined".equals(groupNameId)){
                continue;
            }
            String groupName = groupsJsonObject.get(groupNameId).getAsJsonObject().get("name").getAsString();
            groups.put(groupName,layerList );
        }
        return groups;
    }

    /**
     * TODO 根据layerId返回layer在原始配图中的json
     * @param layerId
     * @return
     */
    public String getLayerJson(String layerId) {
        if(mLayerJsonMap == null){
            getStyleLayerGroups();
        }
        if(mLayerJsonMap == null){
            return null;
        }
        return mLayerJsonMap.get(layerId);
    }


    /**
     * 添加一个图层到地图上
     *
     * @param layer
     */
    public void addLayer(Layer layer) {
        if (mMapBoxMap == null) {
            return;
        }
        if (!hasLayer(layer)) {
            Objects.requireNonNull(mMapBoxMap.getStyle()).addLayer(layer);
        }
    }
    /**
     * 覆盖在指定图层上
     *
     * @param layer
     * @param aboveLayerId
     */
    public void addLayerAbove(Layer layer, String aboveLayerId) {
        List<Layer> layers = mMapBoxMap.getStyle().getLayers();
        if (mMapBoxMap== null) {
            return;
        }
        Layer _layer;
        int index = -1;
        for (int i = 0; i < layers.size(); i++) {
            _layer = layers.get(i);
            if (_layer.getId().equals(aboveLayerId)) {
                index = i;
                break;
            }
        }
        if (index == -1) {
            if (!hasLayer(layer)) {
                mMapBoxMap.getStyle().addLayer(layer);
            }
        } else {
            if (!hasLayer(layer)) {
                mMapBoxMap.getStyle().addLayerAbove(layer, aboveLayerId);
            }
        }
    }
    /**
     * 移除layer
     */
    public void removeLayer(Layer layer){
        if (mMapBoxMap == null) {
            return;
        }
        if (hasLayer(layer)) {
            Objects.requireNonNull(mMapBoxMap.getStyle()).removeLayer(layer);
        }
    }
    /**
     * 移除layer
     */
    public void removeLayer(String layerId){
        if (mMapBoxMap == null) {
            return;
        }
        if (hasLayer(layerId)) {
            Objects.requireNonNull(mMapBoxMap.getStyle()).removeLayer(layerId);
        }
    }
    /**
     * 判断是否包含某一个图层
     *
     * @param layer
     * @return
     */
    public boolean hasLayer(Layer layer) {
        if (layer == null) {
            return false;
        }
        List<Layer> layers = mMapBoxMap.getStyle().getLayers();
        if ( layers == null) {
            return false;
        }
        for (Layer _layer : layers) {
            if (_layer.getId().equals(layer.getId())) {
                return true;
            }
        }
        return false;
    }
    /**
     *
     * 获取layer
     *
     * @param layerId
     * @return
     */
    public Layer getLayer(String layerId) {
        if (layerId == null) {
            return null;
        }
        Layer layer = mMapBoxMap.getStyle().getLayer(layerId);
        return layer;
    }
    /**
     * 判断是否包含某一个图层
     *
     * @param layerId
     * @return
     */
    public boolean hasLayer(String  layerId) {
        if (layerId == null) {
            return false;
        }
        List<Layer> layers = mMapBoxMap.getStyle().getLayers();
        if ( layers == null) {
            return false;
        }
        for (Layer _layer : layers) {
            if (_layer.getId().equals(layerId)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取图层的过滤条件
     * @param layer
     * @return
     */
    public Expression getLayerFilter(Layer layer) {
        if (layer == null) {
            return null;
        }
        if (layer instanceof FillLayer) {
            FillLayer fillLayer = (FillLayer) layer;
            return fillLayer.getFilter();
        }
        if (layer instanceof LineLayer) {
            LineLayer lineLayer = (LineLayer) layer;
            return lineLayer.getFilter();
        }
        if (layer instanceof SymbolLayer) {
            SymbolLayer symbolLayer = (SymbolLayer) layer;
            return symbolLayer.getFilter();
        }
        return null;
    }
    /**
     * 获取图层的过滤条件
     * @param layerId
     * @return
     */
    public Expression getLayerFilter(String layerId) {
        Layer layer = mMapBoxMap.getStyle().getLayer(layerId);
        if (layer == null) {
            return null;
        }
        if (layer instanceof FillLayer) {
            FillLayer fillLayer = (FillLayer) layer;
            return fillLayer.getFilter();
        }
        if (layer instanceof LineLayer) {
            LineLayer lineLayer = (LineLayer) layer;
            return lineLayer.getFilter();
        }
        if (layer instanceof SymbolLayer) {
            SymbolLayer symbolLayer = (SymbolLayer) layer;
            return symbolLayer.getFilter();
        }
        return null;
    }
    /**
     * 设置图层的过滤条件
     * @param layer
     * @param expression
     */
    public void setFilterByLayer(Layer layer, Expression expression) {
        if (layer == null) {
            return;
        }
        if (expression == null) {
            return;
        }
        if (layer instanceof FillLayer) {
            ((FillLayer) layer).setFilter(expression);

            return;
        }
        if (layer instanceof LineLayer) {
            ((LineLayer) layer).setFilter(expression);
            return;
        }
        if (layer instanceof SymbolLayer) {
            ((SymbolLayer) layer).setFilter(expression);
            return;
        }
        /*if (layer instanceof FillExtentLayer) {
            ((FillExtentLayer) layer).setFilter(expression);
            return;
        }*/
        if (layer instanceof CircleLayer) {
            ((CircleLayer) layer).setFilter(expression);
            return;
        }
    }
    /**
     * 设置图层的过滤条件
     * @param layer
     * @param expression
     */
    public void setFilter2Layer(Layer layer, Expression expression) {
        if (layer == null) {
            return;
        }
        if (expression == null) {
            return;
        }
        if (layer instanceof FillLayer) {
            ((FillLayer) layer).setFilter(expression);

            return;
        }
        if (layer instanceof LineLayer) {
            ((LineLayer) layer).setFilter(expression);
            return;
        }
        if (layer instanceof SymbolLayer) {
            ((SymbolLayer) layer).setFilter(expression);
            return;
        }
        /*if (layer instanceof FillExtentLayer) {
            ((FillExtentLayer) layer).setFilter(expression);
            return;
        }*/
        if (layer instanceof CircleLayer) {
            ((CircleLayer) layer).setFilter(expression);
            return;
        }
    }
    /**
     * 验证Expression有效性
     * @param exp
     * @return
     */
    private Expression verifyExpression(Expression exp) {
        String expStr = exp.toString();
        String jsonArrayStr = expStr.replace("filter-", "");
        JsonArray jsonArray = new JsonParser().parse(jsonArrayStr).getAsJsonArray();
        return Expression.Converter.convert(jsonArray);
    }

    /**
     * 替换旧的过滤
     * @return
     */
    public Expression replaceExpression(Expression oldExp,Expression item){
        //return newExp;
        return com.grandtech.mapframe.core.util.Expression.replace(oldExp,item);
    }
    /**
     * 追加条件不存在，则给图层追加过滤条件
     * 已存在，则替换旧的过滤条件
     * @param layer
     * @param addExpression
     */
    public Expression addorReplace2filterByLayer(Layer layer, Expression addExpression) {
        Expression expression = null;
        if (layer == null) {
            return expression;
        }
        if (layer instanceof FillLayer) {
            expression = replaceExpression(((FillLayer)layer).getFilter(), addExpression);
        }
        if (layer instanceof SymbolLayer ) {
            expression = replaceExpression(((SymbolLayer)layer).getFilter(), addExpression);
        }
        if (layer instanceof LineLayer) {
            expression = replaceExpression(((LineLayer)layer).getFilter(), addExpression);
        }
        if (layer instanceof CircleLayer) {
            expression = replaceExpression(((CircleLayer)layer).getFilter(), addExpression);
        }
        Log.i("iii", layer.getId() + ", " + (expression == null ? "图层无过滤" : expression.toString()));
        setFilterByLayer(layer,expression);
        return expression;
    }
    /**
     * 追加条件不存在，则给图层追加过滤条件
     * 已存在，则替换旧的过滤条件
     * @param layerId
     * @param addExpression
     */
    public Expression addorReplace2filterByLayer(String layerId, Expression addExpression) {
        Layer layer = mMapBoxMap.getStyle().getLayer(layerId);
        Expression expression = null;
        if (layer == null) {
            return expression;
        }
        if (layer instanceof FillLayer) {
            expression = replaceExpression(((FillLayer)layer).getFilter(), addExpression);
        }
        if (layer instanceof SymbolLayer ) {
            expression = replaceExpression(((SymbolLayer)layer).getFilter(), addExpression);
        }
        if (layer instanceof LineLayer) {
            expression = replaceExpression(((LineLayer)layer).getFilter(), addExpression);
        }
        if (layer instanceof CircleLayer) {
            expression = replaceExpression(((CircleLayer)layer).getFilter(), addExpression);
        }
        Log.i("iii", layer.getId() + ", " + (expression == null ? "图层无过滤" : expression.toString()));
        setFilterByLayer(layer,expression);
        return expression;
    }

    /**
     * 通过图层id获取sourceId
     *
     * @param layerId
     * @return
     */
    public String getSourceIdByLayerId(String layerId) {
        Layer layer = mMapBoxMap.getStyle().getLayer(layerId);

        if (layer instanceof FillLayer) {
            return ((FillLayer) layer).getSourceId();
        }
        if (layer instanceof LineLayer) {
            return ((LineLayer) layer).getSourceId();
        }
        if (layer instanceof SymbolLayer) {
            return ((SymbolLayer) layer).getSourceId();
        }
        if (layer instanceof CircleLayer) {
            return ((CircleLayer) layer).getSourceId();
        }
        if (layer instanceof HeatmapLayer) {
            return ((HeatmapLayer) layer).getSourceId();
        }
        if (layer instanceof FillExtrusionLayer) {
            return ((FillExtrusionLayer) layer).getSourceId();
        }
        if (layer instanceof RasterLayer) {
            return ((RasterLayer) layer).getSourceId();
        }
        return null;
    }
    /**
     * 添加数据源
     * @param source
     */
    public void addSource(Source source) {
        try {
            if (mMapBoxMap == null || source == null) {
                return;
            }
            if (!hasSource(source)) {
                mMapBoxMap.getStyle().addSource(source);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 判断source是否存在
     * @param source
     * @return
     */
    public boolean hasSource(Source source) {
        if (source == null) {
            return false;
        }
        List<Source> sources = getSources();
        if (sources == null) {
            return false;
        }
        for (Source _source : sources) {
            if (_source.getId().equals(source.getId())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断source是否存在
     * @param sourceId 数据源id
     * @return boolean
     */
    public boolean hasSource(String sourceId) {
        if (sourceId == null) {
            return false;
        }
        List<Source> sources = getSources();
        if (sources == null) {
            return false;
        }
        for (Source _source : sources) {
            if (_source.getId().equals(sourceId)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 解析stylejson获取souce组 key 代表sourceId value代表sourcejson
     * @return
     */
    public Map<String,String> getStyleSourceGroup(){
        try{
            if(mSourceJsonMap == null||mSourceJsonMap.size()==0) {
                String mapStyle = getMapStyleStr();
                JsonObject mapStyleStyleJsonObj = new JsonParser().parse(mapStyle).getAsJsonObject();
                JsonObject jsonObject = mapStyleStyleJsonObj.get("sources").getAsJsonObject();
                int size = new Double(getSources().size()/0.75).intValue()+1;
                mSourceJsonMap = new HashMap<>(size);
                for(Map.Entry<String, JsonElement> stringJsonElementEntry : jsonObject.entrySet()){
                    mSourceJsonMap.put(stringJsonElementEntry.getKey(),stringJsonElementEntry.getValue().toString());
                }
            }
            return mSourceJsonMap;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 获取SourceUri
     * @param SourceId
     * @return
     */
    public String getSourceUri(String SourceId){
        try{
            Map<String,String> sourceJsonMap = getStyleSourceGroup();
            if(sourceJsonMap!=null){
               String sourceJson = sourceJsonMap.get(SourceId);
               JsonObject sourceJsonObject = new JsonParser().parse(sourceJson).getAsJsonObject();
               if(sourceJsonObject.has("tiles")){
                   String uri =sourceJsonObject.get("tiles").getAsJsonArray().get(0).getAsString().replace("/{z}/{x}/{y}.pbf","");
                   return uri;
               }

            }
            return null;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 获取tileUrl
     * @param layerID
     * @return
     */
    public String getTileUrl(String layerID){
        try{
            Layer layer = getLayer(layerID);
            if(layer == null) {
                return null;
            }
            String sourceID = getSourceIdByLayerId(layerID);
            if(sourceID == null){
                return null;
            }
            String sourceLayerID = getSourceLayerId(layer);
            if(sourceLayerID == null){
                return null;
            }
            Map<String,String> sourceJsonMap = getStyleSourceGroup();
            if(sourceJsonMap!=null){
                String sourceJson = sourceJsonMap.get(sourceID);
                JsonObject sourceJsonObject = new JsonParser().parse(sourceJson).getAsJsonObject();
                if(sourceJsonObject.has("url")){
                    String tileUrl = sourceJsonObject.get("url").getAsString()+"/{z}/{x}/{y}.pbf";
                    return tileUrl;
                }
                if(sourceJsonObject.has("tiles")){
                    int size = sourceJsonObject.get("tiles").getAsJsonArray().size();
                    for (int i =0; i<size ;i++){
                        String tileUrl = sourceJsonObject.get("tiles").getAsJsonArray().get(0).getAsString();
                        if(tileUrl!=null && tileUrl.contains(sourceLayerID)){
                            return tileUrl;
                        }
                    }
                }

            }
            return null;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }
    /**
     * 根据sourceid获取source
     * @param sourceId 数据源id
     * @return
     */
    public Source getSource(String sourceId) {
        if (sourceId == null) {
            return null;
        }
        return mMapBoxMap.getStyle().getSource(sourceId);
    }
    /**
     * 获取所有source
     *
     * @return
     */
    public List<Source> getSources() {
        if (mMapBoxMap == null) {
            return null;
        }
        return Objects.requireNonNull(mMapBoxMap.getStyle()).getSources();
    }

    /**
     * 移除source
     * @param source
     * @return
     */
    public void removeSource(Source source) {
        if (mMapBoxMap == null) {
            return ;
        }
       mMapBoxMap.getStyle().removeSource(source);
    }
    /**
     * 移除source
     * @param sourceId
     * @return
     */
    public void removeSource(String sourceId) {
        if (mMapBoxMap == null) {
            return ;
        }
        mMapBoxMap.getStyle().removeSource(sourceId);
    }

    /**
     * 获取layer的SourceLayerId
     * @param layer
     * @return
     */
    public String getSourceLayerId(Layer layer){
        if (layer instanceof LineLayer) {
            return ((LineLayer) layer).getSourceLayer();
        }
        if (layer instanceof FillLayer) {
            return ((FillLayer) layer).getSourceLayer();
        }
        if (layer instanceof CircleLayer) {
            return ((CircleLayer) layer).getSourceLayer();
        }
        return null;
    }
    /**
     * 创建一个简单的基本的GeoJsonSource
     * @param sourceId
     * @return
     */
    public GeoJsonSource createSimpleGeoJsonSource(String sourceId){
        if(sourceId == null){
            return null;
        }
        GeoJsonSource simpleSource = new GeoJsonSource(sourceId);
        return simpleSource;
    }

    /**
     * 创建一个简单的基本的VectorSource
     * @param sourceId
     * @return
     */
    public VectorSource createSimpleVectorSource(String sourceId , String url){
        if(sourceId == null){
            return null;
        }
        TileSet mapillaryTileset = new TileSet("2.1.0", url);
        mapillaryTileset.setMinZoom(11);
        mapillaryTileset.setMaxZoom(18);
        VectorSource vectorSource = new VectorSource(sourceId,mapillaryTileset);
        return vectorSource;
    }
    /**
     * 创建一个简单的基本的ImageSource
     * @param sourceId
     * @return
     */
    public ImageSource createSimpleImageSource(String sourceId , Envelope envelope,String url) throws MalformedURLException {
        URL urlq =new URL(url);
        ImageSource imageSource = new ImageSource(sourceId,new LatLngQuad(
                new LatLng(envelope.getMaxY(),envelope.getMinX()),
                new LatLng(envelope.getMaxY(),envelope.getMaxX()),
                new LatLng(envelope.getMinY(),envelope.getMaxX()),
                new LatLng(envelope.getMinY(),envelope.getMinX()))
                , urlq);
        return imageSource;
    }
    /**
     * 创建一个简单的基本的ImageSource
     * @param sourceId
     * @return
     */
    public ImageSource createSimpleImageSource(String sourceId , BoundingBox boundingBox,String url) throws MalformedURLException {
        URL urlq =new URL(url);
        ImageSource imageSource = new ImageSource(sourceId,new LatLngQuad(
                new LatLng(boundingBox.north(),boundingBox.west()),
                new LatLng(boundingBox.north(),boundingBox.east()),
                new LatLng(boundingBox.south(),boundingBox.east()),
                new LatLng(boundingBox.south(),boundingBox.west()))
                , urlq);
        return imageSource;
    }

    /**
     * 将layer拷贝成样式透明的FillLayer
     * @param layer
     * @return FillLayer
     */
    public FillLayer layerClone2TransparentFillLayer(Layer layer){
        if(layer == null ){
            return null;
        }
        String cloneLayerId = String.format("%s_fill", layer.getId());
        Source source = getSource(getSourceIdByLayerId(layer.getId()));
        Expression expression = getLayerFilter(layer);
        FillLayer cloneLayer = new FillLayer(cloneLayerId, source.getId())
                .withSourceLayer(getSourceLayerId(layer) == null?source.getId():getSourceLayerId(layer))
                .withProperties(PropertyFactory.fillOpacity(0f));
        if (expression != null) {
            cloneLayer.setFilter(expression);
        }
        cloneLayer.setMaxZoom(layer.getMaxZoom());
        cloneLayer.setMinZoom(layer.getMinZoom());
        return cloneLayer;
    }

    /**
     * 高亮
     * @param wkt
     * @param lineColor 不传默认#a8ffff
     */
    public void highLightRegion(String wkt,Integer lineColor) {
        if (wkt == null) {
            return;
        }
        Geometry geometry = Transformation.wkt2BoxGeometry(wkt);
        highLightRegion(geometry,lineColor);
    }

    /**
     * 高亮当前图形
     * @param geometry
     * @param lineColor 不传默认#a8ffff
     */
    public void highLightRegion(Geometry geometry,Integer lineColor) {
        String highLightLayerId = "highLightLayerId";
        String highLightSourceId = "highLightSourceId";
        GeoJsonSource highLightSource;
        LineLayer highLightLayer;
        if (!hasSource(highLightSourceId)) {
            highLightSource = createSimpleGeoJsonSource(highLightSourceId);
        }else {
            highLightSource = (GeoJsonSource)getSource(highLightSourceId);
        }
        if (!hasLayer(highLightLayerId)) {
            if(lineColor == null){
                lineColor = Color.parseColor("#a8ffff");
            }
            highLightLayer = new LineLayer(highLightLayerId, highLightSourceId)
                    .withSourceLayer(highLightSourceId)
                    .withProperties(
                            PropertyFactory.lineColor(lineColor),
                            PropertyFactory.lineWidth(3f)
                    );
        }else {
            highLightLayer = (LineLayer)getLayer(highLightLayerId);
        }
        highLightSource.setGeoJson(geometry);
        addSource(highLightSource);
        if (!hasLayer(highLightLayer)) {
            addLayer(highLightLayer);
        }
    }
    /**
     * 高亮当前图形
     * @param featureCollection
     * @param lineColor 不传默认#a8ffff
     */
    public void highLightRegion(FeatureCollection featureCollection, Integer lineColor) {
        String highLightLayerId = "highLightLayerId";
        String highLightSourceId = "highLightSourceId";
        GeoJsonSource highLightSource;
        LineLayer highLightLayer;
        if (!hasSource(highLightSourceId)) {
            highLightSource = createSimpleGeoJsonSource(highLightSourceId);
        }else {
            highLightSource = (GeoJsonSource)getSource(highLightSourceId);
        }
        if (!hasLayer(highLightLayerId)) {
            if(lineColor == null){
                lineColor = Color.parseColor("#a8ffff");
            }
            highLightLayer = new LineLayer(highLightLayerId, highLightSourceId)
                    .withSourceLayer(highLightSourceId)
                    .withProperties(
                            PropertyFactory.lineColor(lineColor),
                            PropertyFactory.lineWidth(3f)
                    );
        }else {
            highLightLayer = (LineLayer)getLayer(highLightLayerId);
        }
        highLightSource.setGeoJson(featureCollection);
        addSource(highLightSource);
        if (!hasLayer(highLightLayer)) {
            addLayer(highLightLayer);
        }
    }
    /**
     * 取消高亮
     */
    public void cancelHighLightRegion(){
        String highLightLayerId = "highLightLayerId";
        String highLightSourceId = "highLightSourceId";
        removeLayer(highLightLayerId);
        removeSource(highLightSourceId);
    }

    /**
     * TODO  简单的根据远程链接显示瓦片数据
     * @param sourceLayer 传对应url瓦片的sourceLayer
     * @param url
     * @param geometryType
     * @throws MalformedURLException
     */
    public void showSimpleVector(String sourceLayer,String url,GeometryType geometryType) throws MalformedURLException {
        String layerId = sourceLayer+"_simpleShowVectorlayer";
        String sourceId = sourceLayer+"_simpleShowVectorsource";
        if(hasSource(sourceId)){
            if(hasLayer(layerId)){
                Layer layer = getLayer(layerId);
                layer.setProperties(PropertyFactory.visibility(Property.VISIBLE));
                return;
            }else {
                throw new RuntimeException("sourceId已存在且不存在对应的layer,请移除source或更改sourceId");
            }
        }
        VectorSource vectorSource = createSimpleVectorSource(sourceId,url);
        Layer layer = null;
        if(geometryType == GeometryType.point){
            layer = new CircleLayer(layerId ,sourceId).withSourceLayer(sourceLayer)
                    .withProperties(
                            PropertyFactory.circleColor("#94FDD6"),
                            PropertyFactory.circleRadius(5f),
                            PropertyFactory.circleOpacity(1f));;
        }
        if(geometryType == GeometryType.line){
            layer = new LineLayer(layerId, sourceId).withSourceLayer(sourceLayer)
                    .withProperties(PropertyFactory.lineColor(Color.parseColor("#94FDD6")),
                            PropertyFactory.lineWidth(3f),
                            PropertyFactory.lineOpacity(1f));
        }
        if(geometryType == GeometryType.polygon){
            layer = new FillLayer(layerId,sourceId).withSourceLayer(sourceLayer)
                    .withProperties(PropertyFactory.fillColor(Color.parseColor("#CC94FDD6")),
                            PropertyFactory.lineWidth(3f),
                            PropertyFactory.lineOpacity(1f));
        }

        if(vectorSource!= null && layer!=null){
            addSource(vectorSource);
            addLayer(layer);
        }
    }

    /**
     * 隐藏SimpleVector
     * @param sourceLayer 传对应url瓦片的sourceLayer
     */
    public void hideSimpleVector(String sourceLayer){
        String layerId = sourceLayer+"_simpleShowVectorlayer";
        String sourceId = sourceLayer+"_simpleShowVectorsource";
        if(!hasLayer(layerId)){
            return;
        }
        Layer layer = getLayer(layerId);
        layer.setProperties(PropertyFactory.visibility(Property.NONE));
    }

    /**
     * TODO 在地图上显示图片
     * @param sourceId
     * @param boundingBox 显示的范围
     * @param url
     */
    public void showSimpleImage(String sourceId, BoundingBox boundingBox, String url){
        try {
            hideSimpleImage(sourceId);
            String layerId = sourceId+"__simpleShowImagelayer";
            ImageSource imageSource = createSimpleImageSource(sourceId,boundingBox,url);
            RasterLayer rasterLayer =new RasterLayer(layerId,sourceId);
            addSource(imageSource);
            addLayer(rasterLayer);

        } catch (MalformedURLException e) {
            e.printStackTrace();
            Log.e("gmbgl","showSimpleImage,url错误MalformedURLException");
        }
    }
    /**
     * TODO 在地图上显示图片
     * @param sourceId
     * @param envelope 显示的范围
     * @param url
     */
    public void showSimpleImage(String sourceId,Envelope envelope,String url){
        try {
            hideSimpleImage(sourceId);
            String layerId = sourceId+"__simpleShowImagelayer";
            ImageSource imageSource = createSimpleImageSource(sourceId,envelope,url);
            RasterLayer rasterLayer =new RasterLayer(layerId,sourceId);
            addSource(imageSource);
            addLayer(rasterLayer);

        } catch (MalformedURLException e) {
            e.printStackTrace();
            Log.e("gmbgl","showSimpleImage,url错误MalformedURLException");
        }
    }
    /**
     * TODO 隐藏对应的Image根据sourceId
     * @param sourceId
     */
    public void hideSimpleImage(String sourceId){
       String layerId = sourceId+"__simpleShowImagelayer";
       removeLayer(layerId);
       removeSource(sourceId);
    }

    /**
     * TODO 根据KEY将对应的collection进行显示
     * @param key 唯一标识
     * @param collection 要显示的数据集
     * @param renderColor 渲染颜色
     * @param geometryType 显示类型 point line polygon
     */
    public void showSimpleGeoJson(@NonNull String key,@NonNull FeatureCollection collection,int renderColor,@NonNull GeometryType geometryType){
        if(key == null||collection == null||geometryType==null){
            return;
        }
        String layerId = key;
        String sourceId = key;
        if(hasSource(sourceId)){
            Source source = getSource(sourceId);
            if(source instanceof GeoJsonSource){
                ((GeoJsonSource) source).setGeoJson(collection);
            }else {
                Log.e("gmbgl","source已存在并且不是GeoJsonSource");
                return;
            }
        }else {
            GeoJsonSource geoJsonSource = createSimpleGeoJsonSource(sourceId);
            geoJsonSource.setGeoJson(collection);
            addSource(geoJsonSource);
        }
        if(hasLayer(layerId)){
            Layer layer = getLayer(layerId);
            layer.setProperties(PropertyFactory.visibility(Property.VISIBLE));
        }else {
            Layer layer = null;
            if(geometryType == GeometryType.point){
                layer = new CircleLayer(layerId ,sourceId).withSourceLayer(sourceId)
                        .withProperties(
                                PropertyFactory.circleColor(renderColor),
                                PropertyFactory.circleRadius(4f),
                                PropertyFactory.circleOpacity(1f));;
            }
            if(geometryType == GeometryType.line){
                layer = new LineLayer(layerId, sourceId).withSourceLayer(sourceId)
                        .withProperties(PropertyFactory.lineColor(renderColor),
                                PropertyFactory.lineWidth(3f),
                                PropertyFactory.lineOpacity(1f));
            }
            if(geometryType == GeometryType.polygon){
                layer = new FillLayer(layerId,sourceId).withSourceLayer(sourceId)
                        .withProperties(PropertyFactory.fillColor(renderColor),
                                PropertyFactory.lineWidth(3f),
                                PropertyFactory.lineOpacity(1f));
            }
            if(layer != null){
                addLayer(layer);
            }
        }
    }

    /**
     * TODO 根据KEY将对应的collection进行隐藏
     * @param key 唯一标识
     */
    public void hideSimpleGeoJson(String key){
        if(key == null){
            return;
        }
        Layer layer = getLayer(key);
        if(layer != null){
            layer.setProperties(PropertyFactory.visibility(Property.NONE));
        }

    }

    /**
     * TODO 根据KEY将对应的collection进行显示
     * @param key 唯一标识
     * @param collection 要显示的数据集
     * @param geometryType 显示类型 point line polygon
     * @param properties
     */
    public void showSimpleGeoJson(@NonNull String key, @NonNull FeatureCollection collection,  @NonNull GeometryType geometryType,@NonNull PropertyValue<?>... properties){
        if(key == null||collection == null||geometryType==null){
            return;
        }
        String layerId = key;
        String sourceId = key;
        if(hasSource(sourceId)){
            Source source = getSource(sourceId);
            if(source instanceof GeoJsonSource){
                ((GeoJsonSource) source).setGeoJson(collection);
            }else {
                Log.e("gmbgl","source已存在并且不是GeoJsonSource");
                return;
            }
        }else {
            GeoJsonSource geoJsonSource = createSimpleGeoJsonSource(sourceId);
            geoJsonSource.setGeoJson(collection);
            addSource(geoJsonSource);
        }
        if(hasLayer(layerId)){
            Layer layer = getLayer(layerId);
            layer.setProperties(PropertyFactory.visibility(Property.VISIBLE));
        }else {
            Layer layer = null;
            if(geometryType == GeometryType.point){
                layer = new CircleLayer(layerId ,sourceId).withSourceLayer(sourceId)
                        .withProperties(
                                properties);;
            }
            if(geometryType == GeometryType.line){
                layer = new LineLayer(layerId, sourceId).withSourceLayer(sourceId)
                        .withProperties(properties);
            }
            if(geometryType == GeometryType.polygon){
                layer = new FillLayer(layerId,sourceId).withSourceLayer(sourceId)
                        .withProperties(properties);
            }
            if(layer != null){
                addLayer(layer);
            }
        }
    }
}
