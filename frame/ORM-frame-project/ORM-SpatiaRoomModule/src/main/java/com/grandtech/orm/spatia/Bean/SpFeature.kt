package com.grandtech.orm.spatia.Bean

import androidx.room.util.TableInfo
import com.mapbox.geojson.Feature

/**
 * @ClassName SpFeature
 * @Description TODO
 * @Author: fs
 * @Date: 2021/8/23 8:52
 * @Version 2.0
 */
@Suppress("UNREACHABLE_CODE")
class SpFeature{

    var  mTableName:String = ""

    var mFeature: Feature? = null

    var mFeatures:List<Feature>? = null

    var mTableInfo : TableInfo

    var mSrid :Int = 4326


    constructor(tableName:String,feature: Feature,tableInfo : TableInfo){
        mTableName = tableName
        mFeature = feature
        mTableInfo = tableInfo
    }

    constructor(tableName:String,features: List<Feature>,tableInfo : TableInfo){
        mFeatures = features
        mTableName = tableName
        mTableInfo = tableInfo
    }




}