package com.grandtech.orm.spatia.sql

import android.util.Log
import androidx.annotation.Keep
import androidx.room.util.TableInfo
import com.grandtech.mapframe.core.util.Transformation
import com.grandtech.orm.spatia.Bean.SpFeature
import com.grandtech.orm.spatia.util.PropertyUtil

/**
 * @ClassName SpSqlBuilder
 * @Description TODO 空间表feature转换sql
 * @Author: fs
 * @Date: 2021/8/23 8:13
 * @Version 2.0
 */
@Keep
object SpSqlBuilder {

    /**
     * 添加空间索引
     * @param layerInfo
     * @return
     */
    fun createSpatialIndex(layerInfo: Map<String?, Any?>?): String? {
        return try {
            if (layerInfo == null || layerInfo.size == 0) return null
            val tableName = layerInfo["tableName"] as String?
            val tableInfo = layerInfo["tableInfo"] as Map<*, *>?
            val pkName = tableInfo!!["pkName"].toString()
            val pkType = tableInfo["pkType"].toString()
            val geoColumnName = tableInfo["geoColumnName"].toString()
            val geoColumnType = tableInfo["geoColumnType"].toString()
            val sqlBuilder = StringBuilder("select CreateSpatialIndex('")
            sqlBuilder.append(tableName).append("','").append(geoColumnName).append("')")
            sqlBuilder.toString()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun createSpatialIndex(tableName: String?, geoColumnName: String?): String? {
        return try {
            val sqlBuilder = java.lang.StringBuilder("select CreateSpatialIndex('")
            sqlBuilder.append(tableName).append("','").append(geoColumnName).append("')")
            sqlBuilder.toString()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * 创建插入sql
     */
    fun createInsertSQL(spFeature: SpFeature, allowUpdates: Boolean): String? {
        return try {
            var sqlBuilderK = StringBuilder("INSERT into ");
            if (allowUpdates) {
                sqlBuilderK = StringBuilder("INSERT OR REPLACE into ");
            }
            sqlBuilderK.append(spFeature.mTableName);
            sqlBuilderK.append(" (");
            val sqlBuilderV = StringBuilder(" values  ");
            var feature = spFeature.mFeature;
            var features = spFeature.mFeatures;
            var tableInfo = spFeature.mTableInfo;
            var columns = tableInfo.columns
            //单条插入
            if (feature != null) {
                sqlBuilderV.append(" ( ")
                for (en in columns.entries) {
                    if (en.value.type.equals("GEOMETRY")||en.value.type.equals("MULTIPOLYGON")||
                            en.value.type.equals("POLYGON")||en.value.type.equals("POINT")
                            ||en.value.type.equals("MULTIPOINT")||en.value.type.equals("LINESTRING")
                            ||en.value.type.equals("MULTILINE")) {
                        sqlBuilderK.append(en.key).append(",")
                        sqlBuilderV.append(" GeomFromText('").append(Transformation.boxGeometry2Wkt(feature.geometry())).append("',").append(spFeature.mSrid).append("),")
                    } else {
                        var columnValue = PropertyUtil.getFeatureAttributeAsObject(feature.properties(), en.key);
                        if (columnValue == null) continue
                        sqlBuilderK.append(en.key).append(",")
                        if (columnValue is String) {
                            sqlBuilderV.append("'").append(columnValue).append("'").append(",")
                        } else {
                            sqlBuilderV.append(columnValue).append(",")
                        }
                    }
                }
                if (sqlBuilderK != null && sqlBuilderK.toString().endsWith(",")) {
                    sqlBuilderK.delete(sqlBuilderK.toString().lastIndexOf(","), sqlBuilderK.toString().length)
                }
                sqlBuilderK.append(" ) ")
                if (sqlBuilderV != null && sqlBuilderV.toString().endsWith(",")) {
                    sqlBuilderV.delete(sqlBuilderV.toString().lastIndexOf(","), sqlBuilderV.toString().length)
                }
                sqlBuilderV.append(" ) ")
            } else if (features != null) {
                for (i in features.indices) {
                    var featureItem = features[i];
                    sqlBuilderV.append(" ( ")
                    for (en in columns.entries) {
                        if (en.value.type.equals("GEOMETRY")) {
                            if (i == 0) {
                                sqlBuilderK.append(en.key).append(",")
                            }
                            sqlBuilderV.append(" ST_GeomFromText('").append(Transformation.boxGeometry2Wkt(featureItem.geometry())).append("',").append(spFeature.mSrid).append("),")
                        } else {
                            var columnValue = PropertyUtil.getFeatureAttributeAsObject(featureItem.properties(), en.key);
                            if (columnValue == null) continue
                            if (i == 0) {
                                sqlBuilderK.append(en.key).append(",")
                            }
                            if (columnValue is String) {
                                sqlBuilderV.append("'").append(columnValue).append("'").append(",")
                            } else {
                                sqlBuilderV.append(columnValue).append(",")
                            }
                        }
                    }
                    if (sqlBuilderK != null && sqlBuilderK.toString().endsWith(",")) {
                        sqlBuilderK.delete(sqlBuilderK.toString().lastIndexOf(","), sqlBuilderK.toString().length)
                    }
                    if (i == 0) {
                        sqlBuilderK.append(")")
                    }
                    if (sqlBuilderV != null && sqlBuilderV.toString().endsWith(",")) {
                        sqlBuilderV.delete(sqlBuilderV.toString().lastIndexOf(","), sqlBuilderV.toString().length)
                    }
                    sqlBuilderV.append("),")
                }
            } else {
                null
            }
            if (sqlBuilderV != null && sqlBuilderV.toString().endsWith(",")) {
                sqlBuilderV.delete(sqlBuilderV.toString().lastIndexOf(","), sqlBuilderV.toString().length)
            }
            sqlBuilderK.append(sqlBuilderV).toString()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * 构建空间表查询语句
     * @param TableInfo 空间表结构信息
     * @param condition where后面的语句
     * @ignore 忽略的查询字段如多个字段则拼接即可：qhdmqhmcdkbm
     */
    fun createQuerySQLMouldWhere(tableInfo: TableInfo, condition: String?, ignore: String?): String? {
        return try {
            var columns = tableInfo.columns
            var sql = StringBuilder("select ");
            for (en in columns.entries) {
                //判断字段是否忽略
                if (ignore != null && en.key in ignore) {
                    Log.i("忽略",ignore);
                    continue
                }
                if (en.value.type.equals("GEOMETRY")) {
                    sql.append(" AsGeoJSON(").append(en.key).append(") as GEOJSON ")
                    sql.append(" ,")
                } else {
                    sql.append(en.key)
                    sql.append(" ,")
                }
                Log.i("忽略"+en.key,sql.toString());
            }
            if (sql != null && sql.toString().endsWith(",")) {
                sql.delete(sql.toString().lastIndexOf(","), sql.toString().length)
            }
            sql.append(" from ")
            var tableName = tableInfo.name
            sql.append(tableName)
            //如果条件没有，则默认查一百条
            if (condition == null) {
                sql.append(" limit 100 ")
                sql.toString()
            } else if (!condition.startsWith("where") && !condition.startsWith("WHERE")) {
                sql.append(" where ")
            }
            sql.append(condition)
            sql.toString()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * 生成updateSQL
     */
    fun createUpdateSQL(spFeature: SpFeature, conditions: MutableList<String>?, ignoreColumns: String?): String? {
        return try {
            var sqlBuilderK = StringBuilder("update ");
            var sqlBuilderW = StringBuilder(" WHERE  ");
            sqlBuilderK.append(spFeature.mTableName);
            sqlBuilderK.append(" set ");
            var feature = spFeature.mFeature;
            var tableInfo = spFeature.mTableInfo;
            var columns = tableInfo.columns
            var pks = mutableListOf<String>()
            //单条插入
            if (feature != null) {
                for (en in columns.entries) {
                    //判断字段是否忽略
                    if (ignoreColumns != null && ignoreColumns.contains(en.key)) {
                        continue
                    }
                    if (en.value.isPrimaryKey) {
                        pks.add(en.key)
                        continue
                    }
                    if (en.value.type.equals("GEOMETRY")) {
                        sqlBuilderK.append(en.key).append(" = ")
                        sqlBuilderK.append(" GeomFromText('").append(Transformation.boxGeometry2Wkt(feature.geometry())).append("',").append(spFeature.mSrid).append("),")
                    } else {
                        var columnValue = PropertyUtil.getFeatureAttributeAsObject(feature.properties(), en.key);
                        if (columnValue == null) continue
                        sqlBuilderK.append(en.key).append(" = ")
                        if (columnValue is String) {
                            sqlBuilderK.append("'").append(columnValue).append("'").append(",")
                        } else {
                            sqlBuilderK.append(columnValue).append(",")
                        }
                    }
                }
                if (sqlBuilderK != null && sqlBuilderK.toString().endsWith(",")) {
                    sqlBuilderK.delete(sqlBuilderK.toString().lastIndexOf(","), sqlBuilderK.toString().length)
                }
                if (conditions == null) {
                    for (c in pks) {
                        var columnValue = PropertyUtil.getFeatureAttributeAsObject(feature.properties(), c);
                        if (columnValue == null) continue
                        sqlBuilderW.append(c).append(" = ")
                        if (columnValue is String) {
                            sqlBuilderW.append("'").append(columnValue).append("'").append(" and")
                        } else {
                            sqlBuilderW.append(columnValue).append(" and")
                        }
                    }
                } else {
                    for (c in conditions) {
                        var column = columns.get(c)
                        if ("GEOMETRY".equals(column?.type)) {
                            sqlBuilderW.append(c).append(" = ")
                            sqlBuilderW.append(" GeomFromText('").append(Transformation.boxGeometry2Wkt(feature.geometry())).append("',").append(spFeature.mSrid).append(") and")
                        } else {
                            var columnValue = PropertyUtil.getFeatureAttributeAsObject(feature.properties(), c);
                            if (columnValue == null) continue
                            sqlBuilderW.append(c).append(" = ")
                            if (columnValue is String) {
                                sqlBuilderW.append("'").append(columnValue).append("'").append(" and")
                            } else {
                                sqlBuilderW.append(columnValue).append(" and")
                            }
                        }

                    }
                }
                if (sqlBuilderW != null && sqlBuilderW.toString().endsWith("and")) {
                    sqlBuilderW.delete(sqlBuilderW.toString().lastIndexOf("and"), sqlBuilderW.toString().length)
                }
            } else {
                null
            }
            sqlBuilderK.append(sqlBuilderW).toString()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * 创建删除的where条件
     */
    fun createDeleteWhereClauseSQL(spFeature: SpFeature): String? {
        return try {
            var stringBuilder = java.lang.StringBuilder();
            var feature = spFeature.mFeature;
            var tableInfo = spFeature.mTableInfo;
            var columns = tableInfo.columns
            for (en in columns.entries) {
                if (en.value.isPrimaryKey) {
                    if ("GEOMETRY".equals(en.value.type)) {
                        stringBuilder.append(en.key).append(" = ")
                        stringBuilder.append(" GeomFromText('").append(Transformation.boxGeometry2Wkt(feature!!.geometry())).append("',").append(spFeature.mSrid).append(") and")
                    } else {
                        var columnValue = PropertyUtil.getFeatureAttributeAsObject(feature!!.properties(), en.key);
                        if (columnValue == null) continue
                        stringBuilder.append(en.key).append(" = ")
                        if (columnValue is String) {
                            stringBuilder.append("'").append(columnValue).append("'").append(" and")
                        } else {
                            stringBuilder.append(columnValue).append(" and")
                        }
                    }
                }
            }
            if (stringBuilder != null && stringBuilder.toString().endsWith("and")) {
                stringBuilder.delete(stringBuilder.toString().lastIndexOf("and"), stringBuilder.toString().length)
            }
            stringBuilder.toString()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}