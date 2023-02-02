package com.gradtech.mapframev10.core.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.gradtech.mapframev10.core.rules.Rules;


/**
 * @ClassName StyleJsonBuilder
 * @Description TODO StyleJson构建
 * @Author: fs
 * @Date: 2021/7/29 16:01
 * @Version 2.0
 */
public class StyleJsonBuilder implements Rules {

    public static final String  DK_SOURCE = "dksource";

    public static final String SYM_SOURCE = "symsource";

    private String styleJson = "{\n" +
            "  \"updatedAt\": \"2018-10-25T07:31:08.240Z\",\n" +
            "  \"createdAt\": \"2018-10-22T08:43:43.949Z\",\n" +
            "  \"owner\": \"gykj\",\n" +
            "  \"name\": \"坐落图底图\",\n" +
            "  \"zoom\": 20,\n" +
            "  \"sources\": {},\n" +
            "  \"glyphs\": \"https://api.grandtechmap.com/grandtech-middleground-vectile-wgs/fonts/gykj/{fontstack}/{range}.pbf\",\n" +
            "  \"layers\": [\n" +
            "    {\n" +
            "      \"id\": \"背景\",\n" +
            "      \"type\": \"background\",\n" +
            "      \"paint\": {\n" +
            "        \"background-color\": \"#0a0a0a\"\n" +
            "      }\n" +
            "    }\n" +
            "  ],\n" +
            "  \"transition\": {\n" +
            "    \"delay\": 0,\n" +
            "    \"duration\": 50\n" +
            "  },\n" +
            "  \"pitch\": 0,\n" +
            "  \"bearing\": 0,\n" +
            "  \"center\": [\n" +
            "    130.0448710055212,\n" +
            "    38.26746694145149\n" +
            "  ],\n" +
            "  \"version\": 8,\n" +
            "  \"type\": \"normal\",\n" +
            "  \"scope\": \"private\",\n" +
            "  \"style_id\": \"BkdpaZoi7\"\n" +
            "}";

    private JsonObject styleJsonObject = null;

    public static StyleJsonBuilder create(){
        return new StyleJsonBuilder();
    }
    public static StyleJsonBuilder create(String styleJson){
        return new StyleJsonBuilder(styleJson);
    }

    public StyleJsonBuilder() {
        try {
            //复制一个stylejson对象
            JsonParser jsonParser = new JsonParser();
            styleJsonObject = jsonParser.parse(styleJson).getAsJsonObject();

            //增加卫星影像层
            JsonObject source = styleJsonObject.getAsJsonObject("sources");
            String satelliteSourceStr = String.format("{\n" +
                    "            \"type\":\"raster\",\n" +
                    "             \"tiles\": [\"%s\"],\n" +
                    "             \"minzoom\": 5,\n" +
                    "             \"maxzoom\": %s,\n" +
                    "            \"tileSize\":256\n" +
                    "        }", "http://t0.tianditu.gov.cn/DataServer?T=img_w&x={x}&y={y}&l={z}&tk=583e63953a6ed6bf304e68120db4c512",16);

            source.add("satellite", jsonParser.parse(satelliteSourceStr));

            String satelliteLayerStr = "{\n" +
                    "      \"id\": \"卫星影像\",\n" +
                    "      \"type\": \"raster\",\n" +
                    "      \"source\": \"satellite\",\n" +
                    "      \"layout\": {\n" +
                    "        \"visibility\": \"visible\"\n" +
                    "      }      \n" +
                    "   }";
            JsonArray layers = styleJsonObject.getAsJsonArray("layers");
            layers.add(jsonParser.parse(satelliteLayerStr));



            //增加地块source和边界linelayer
            String geoSourceStr = "{\n" +
                    "    \"type\": \"geojson\",\n" +
                    "    \"data\": {}\n" +
                    "     }";
            source.add(DK_SOURCE, jsonParser.parse(geoSourceStr));
            String dkLineLayerStr = "{\n" +
                    "      \"id\": \"地块\",\n" +
                    "      \"source\": \"dksource\",\n" +
                    "      \"type\": \"line\",\n" +
                    "      \"layout\": {\n" +
                    "        \"visibility\": \"visible\"\n" +
                    "      },\n" +
                    "      \"paint\": {\n" +
                    "        \"line-color\": \"red\",\n" +
                    "        \"line-opacity\": 1,\n" +
                    "        \"line-width\": 2\n" +
                    "      }\n" +
                    "    }";
            //"      \"minzoom\": 5,\n" +
            //"      \"maxzoom\": 18,\n" +
            layers.add(jsonParser.parse(dkLineLayerStr));

            //增加标注点source和标注symbollayer
            source.add(SYM_SOURCE, jsonParser.parse(geoSourceStr));
            String dkSymbolLayerStr = "{\n" +
                    "      \"id\": \"标注\",\n" +
                    "      \"source\": \"symsource\",\n" +
                    "      \"type\": \"symbol\",\n" +
                    "      \"layout\": {\n" +
                    "        \"text-field\": \"{label}\",\n" +
                    "        \"text-font\": [\"FZHei-B01S Regular\"],\n" +
                    "        \"text-size\": 14,\n" +
                    "        \"text-anchor\": [\"get\", \"anchor\"],\n" +
                    "        \"visibility\": \"visible\"\n" +
                    "      },\n" +
                    "      \"paint\": {\n" +
                    "        \"text-color\": \"#ffffff\"\n" +
                    "      }    \n" +
                    "    }";
            layers.add(jsonParser.parse(dkSymbolLayerStr));

            // 新建图层（当前位置星标标记层）
            source.add("starsource", jsonParser.parse(geoSourceStr));
            String starSymbolLayerStr = "{\n" +
                    "      \"id\": \"星标\",\n" +
                    "      \"source\": \"starsource\",\n" +
                    "      \"type\": \"symbol\",\n" +
                    "      \"layout\": {\n" +
                    "        \"text-field\": \"{label}\",\n" +
                    "        \"text-font\": [\"FZHei-B01S Regular\"],\n" +
                    "        \"text-size\": 14,\n" +
                    "        \"text-anchor\": [\"get\", \"anchor\"],\n" +
                    "        \"visibility\": \"visible\"\n" +
                    "      },\n" +
                    "      \"paint\": {\n" +
                    "        \"text-color\": \"#FF0000\"\n" +
                    "      }    \n" +
                    "    }";
            layers.add(jsonParser.parse(starSymbolLayerStr));

        } catch (JsonParseException e) {
            e.printStackTrace();
        }
    }

    /**
     * 传入自定义styleJson
     * @param styleJson
     */
    public StyleJsonBuilder(String styleJson) {
        try {
            this.styleJson = styleJson;
            //复制一个stylejson对象
            JsonParser jsonParser = new JsonParser();
            styleJsonObject = jsonParser.parse(styleJson).getAsJsonObject();
        } catch (JsonParseException e) {
            e.printStackTrace();
        }
    }

    /**
     * 添加source
     * @param sourceId
     * @param sourceJson 传null则是默认json如下：
     * "{\n" +
     * "    \"type\": \"geojson\",\n" +
     * "    \"data\": {}\n" +
     *  "     }";
     * @return
     */
    public StyleJsonBuilder buildSourceJson(String sourceId ,String sourceJson){
        try {
            if(sourceJson == null){
                sourceJson = "{\n" +
                        "    \"type\": \"geojson\",\n" +
                        "    \"data\": {}\n" +
                        "     }";
            }
            JsonParser jsonParser = new JsonParser();
            JsonObject source = styleJsonObject.getAsJsonObject("sources");
            source.add(sourceId, jsonParser.parse(sourceJson));
        }catch (Exception e){
            e.printStackTrace();
        }
        return this;
    }
    /**
     * 添加source
     * @param sourceId
     * @return
     */
    public StyleJsonBuilder buildSourceJson(String sourceId ){
        try {
            String sourceJson = "{\n" +
                    "    \"type\": \"geojson\",\n" +
                    "    \"data\": {}\n" +
                    "     }";
            JsonParser jsonParser = new JsonParser();
            JsonObject source = styleJsonObject.getAsJsonObject("sources");
            source.add(sourceId, jsonParser.parse(sourceJson));
        }catch (Exception e){
            e.printStackTrace();
        }
        return this;
    }
    /**
     * 添加layer
     * @param layerJson
     * "{\n" +
     *                     "      \"id\": \"星标\",\n" +
     *                     "      \"source\": \"starsource\",\n" +
     *                     "      \"type\": \"symbol\",\n" +
     *                     "      \"layout\": {\n" +
     *                     "        \"text-field\": \"{label}\",\n" +
     *                     "        \"text-font\": [\"FZHei-B01S Regular\"],\n" +
     *                     "        \"text-size\": 14,\n" +
     *                     "        \"text-anchor\": [\"get\", \"anchor\"],\n" +
     *                     "        \"visibility\": \"visible\"\n" +
     *                     "      },\n" +
     *                     "      \"paint\": {\n" +
     *                     "        \"text-color\": \"#FF0000\"\n" +
     *                     "      }    \n" +
     *                     "    }";
     * @return
     */
    public StyleJsonBuilder buildLayerJson(String layerJson){
        try {
            JsonParser jsonParser = new JsonParser();
            JsonArray layers = styleJsonObject.getAsJsonArray("layers");
            layers.add(jsonParser.parse(layerJson));
        }catch (Exception e){
            e.printStackTrace();
        }
        return this;
    }

    /**
     * 返回StyleJson
     * @return
     */
    public String builder() {
        return styleJsonObject.toString();
    }
}
