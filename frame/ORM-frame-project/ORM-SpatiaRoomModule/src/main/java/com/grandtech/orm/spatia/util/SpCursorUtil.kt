package com.grandtech.orm.spatia.util

import android.database.Cursor
import com.grandtech.mapframe.core.util.Transformation.wkb2BoxJsonString
import com.vividsolutions.jts.io.WKBReader
import org.geotools.geojson.geom.GeometryJSON
import org.json.JSONArray
import org.json.JSONObject
import java.io.StringWriter

/**
 * @ClassName CursorUtil
 * @Description TODO 游标转换Feature
 * @Author: fs
 * @Date: 2021/8/25 9:05
 * @Version 2.0
 */
object SpCursorUtil {


    //空间转换

    //空间转换
    fun cursor2GeoJsonCollect(cursor: Cursor?): String? {
        if (cursor == null) return null
        var columnLength: Int
        var columnType: Int
        var columnName: String?
        var columnVal: Any?
        val result = JSONObject()
        val jsonArray = JSONArray()
        var feature: JSONObject? = null
        var properties: JSONObject? = null
        var geometryString: String
        val wkbReader = WKBReader()
        val geometryJSON = GeometryJSON(12)
        try {
            while (cursor.moveToNext()) {
                columnLength = cursor.columnCount
                feature = JSONObject()
                properties = JSONObject()
                feature.put("type", "Feature")
                feature.put("properties", properties)
                for (i in 0 until columnLength) {
                    columnName = cursor.getColumnName(i)
                    columnType = cursor.getType(i)
                    when (columnType) {
                        Cursor.FIELD_TYPE_STRING -> {
                            if(columnName.equals("GEOJSON")){
                                feature.put("geometry", JSONObject(cursor.getString(i)))
                            }else{
                                columnVal = cursor.getString(i)
                                properties.put(columnName, columnVal)
                            }
                        }
                        Cursor.FIELD_TYPE_FLOAT -> {
                            columnVal = cursor.getFloat(i)
                            properties.put(columnName, columnVal)
                        }
                        Cursor.FIELD_TYPE_INTEGER -> {
                            columnVal = cursor.getInt(i)
                            properties.put(columnName, columnVal)
                        }
                        Cursor.FIELD_TYPE_BLOB -> {

                        }
                        Cursor.FIELD_TYPE_NULL -> {
                            columnVal = cursor.getString(i)
                            properties.put(columnName, columnVal)
                        }
                        else -> {
                            columnVal = cursor.getString(i)
                            properties.put(columnName, columnVal)
                        }
                    }
                }
                jsonArray.put(feature)
            }
            result.put("type", "FeatureCollection")
            result.put("features", jsonArray)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return result.toString()
    }

    fun cursor2GeoJson(cursor: Cursor?): String? {
        if (cursor == null) return null
        val columnLength: Int
        var columnType: Int
        var columnName: String?
        var columnVal: Any?
        var feature: JSONObject? = null
        var properties: JSONObject? = null
        var geometryString: String
        val wkbReader = WKBReader()
        val geometryJSON = GeometryJSON(12)
        try {
            while (cursor.moveToNext()) {
                columnLength = cursor.columnCount
                feature = JSONObject()
                properties = JSONObject()
                feature.put("type", "Feature")
                feature.put("properties", properties)
                for (i in 0 until columnLength) {
                    columnName = cursor.getColumnName(i)
                    columnType = cursor.getType(i)
                    when (columnType) {
                        Cursor.FIELD_TYPE_STRING -> {
                            if(columnName.equals("GEOJSON")){
                                feature.put("geometry", JSONObject(cursor.getString(i)))
                            }else{
                                columnVal = cursor.getString(i)
                                properties.put(columnName, columnVal)
                            }
                        }
                        Cursor.FIELD_TYPE_FLOAT -> {
                            columnVal = cursor.getFloat(i)
                            properties.put(columnName, columnVal)
                        }
                        Cursor.FIELD_TYPE_INTEGER -> {
                            columnVal = cursor.getInt(i)
                            properties.put(columnName, columnVal)
                        }
                        Cursor.FIELD_TYPE_BLOB -> {

                        }
                        Cursor.FIELD_TYPE_NULL -> {
                            properties.put(columnName, JSONObject.NULL)
                            properties.put(columnName, JSONObject.NULL)
                        }
                        else -> properties.put(columnName, JSONObject.NULL)
                    }
                }
                break
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return feature.toString()
    }
}