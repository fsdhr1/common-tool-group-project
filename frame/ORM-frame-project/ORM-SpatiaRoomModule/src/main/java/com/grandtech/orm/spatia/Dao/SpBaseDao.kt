package com.grandtech.orm.spatia.Dao

import android.annotation.SuppressLint
import android.util.Log
import androidx.annotation.Keep
import androidx.room.*
import androidx.room.util.DBUtil
import androidx.room.util.TableInfo
import com.grandtech.orm.spatia.Bean.SpFeature
import com.grandtech.orm.spatia.sql.SpSqlBuilder
import com.grandtech.orm.spatia.util.SpCursorUtil
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.ObservableOnSubscribe

/**
 * @ClassName SpBaseDao
 * @Description TODO
 * @Author: fs
 * @Date: 2021/8/23 10:29
 * @Version 2.0
 */
@Keep
@SuppressWarnings("unchecked", "deprecation")
class SpBaseDao(var db: RoomDatabase) {

    /**
     * 单个要素插入
     * @param allowUpdates ture为允许唯一值存在的情况下就更新纪录
     */
    @SuppressLint("RestrictedApi")
    fun insert(tableName: String, feature: Feature, allowUpdates: Boolean): Observable<Long> {
        val existingDtCbdk = TableInfo.read(db.openHelper.writableDatabase, tableName)
        var spFeature = SpFeature(tableName, feature, existingDtCbdk);
        return Observable.create<Long>(object : ObservableOnSubscribe<Long?> {
            override fun subscribe(emitter: ObservableEmitter<Long?>) {
                db.assertNotSuspendingTransaction()
                db.beginTransaction()
                try {
                    var sql = SpSqlBuilder.createInsertSQL(spFeature, allowUpdates);
                    Log.i("orm.spatia.Dao", sql)
                    db.openHelper.writableDatabase.execSQL(sql)
                    emitter.onNext(1)
                    db.setTransactionSuccessful()
                } catch (e: Exception) {
                    e.printStackTrace();
                    emitter.onError(e);
                } finally {
                    db.endTransaction()
                    emitter.onComplete()
                }

            }
        })

    }

    /**
     * 批量插入
     *@param allowUpdates ture为允许唯一值存在的情况下就更新纪录
     */
    @SuppressLint("RestrictedApi")
    fun inserts(tableName: String, features: List<Feature>, allowUpdates: Boolean): Observable<Long> {
        val existingDtCbdk = TableInfo.read(db.openHelper.writableDatabase, tableName)
        var spFeature = SpFeature(tableName, features, existingDtCbdk);
        return Observable.create<Long>(object : ObservableOnSubscribe<Long?> {
            override fun subscribe(emitter: ObservableEmitter<Long?>) {
                db.assertNotSuspendingTransaction()
                db.beginTransaction()
                try {
                    var sql = SpSqlBuilder.createInsertSQL(spFeature, allowUpdates);
                    Log.i("orm.spatia.Dao", sql)
                    db.openHelper.writableDatabase.execSQL(sql)
                    emitter.onNext(features.size.toLong())
                    db.setTransactionSuccessful()
                } catch (e: Exception) {
                    e.printStackTrace();
                    emitter.onError(e);
                } finally {
                    db.endTransaction()
                    emitter.onComplete()
                }

            }
        })

    }

    /**
     * 使用sql直接进行查询返回要素集合
     */
    @SuppressLint("RestrictedApi")
    fun queryBySql(sql: String): Observable<MutableList<Feature?>> {
        val _sql = sql
        val _statement = RoomSQLiteQuery.acquire(_sql, 0)
        return Observable.create<MutableList<Feature?>>(object : ObservableOnSubscribe<MutableList<Feature?>> {
            override fun subscribe(emitter: ObservableEmitter<MutableList<Feature?>>) {
                var featurelist: MutableList<Feature?> = mutableListOf<Feature>().toMutableList()
                val _cursor = DBUtil.query(db, _statement, false, null)
                try {
                    val result = SpCursorUtil.cursor2GeoJsonCollect(_cursor);
                    if (result != null) {
                        featurelist = FeatureCollection.fromJson(result).features()!!;
                    }
                    emitter.onNext(featurelist);
                } catch (e: Exception) {
                    e.printStackTrace();
                    emitter.onError(e);
                } finally {
                    _cursor.close()
                    _statement.release()
                    emitter.onComplete()
                }
            }
        })
    }

    /**
     * 根据表名进行查询返回要素集合
     * @param tableName 空间表名称
     * @param condition where后面的语句
     * @ignoreColumns 忽略的查询字段如多个字段则拼接即可：qhdmqhmcdkbm
     */
    @SuppressLint("RestrictedApi")
    fun queryByCondition(tableName: String, condition: String, ignoreColumns: String?): Observable<MutableList<Feature?>> {
        val existingDtCbdk = TableInfo.read(db.openHelper.writableDatabase, tableName)
        var sql = SpSqlBuilder.createQuerySQLMouldWhere(existingDtCbdk, condition, ignoreColumns)
        Log.i("orm.spatia.Dao", sql)
        return queryBySql(sql!!);
    }

    /**
     *更新Feature
     * @param tableName 空间表名称
     * @param feature where后面的语句要更新的要素
     * @param conditions 条件，不传的话根据主键更新
     * @ignoreColumns 忽略的更新字段如多个字段则拼接即可：qhdmqhmcdkbm
     */
    @SuppressLint("RestrictedApi")
    fun update(tableName: String, feature: Feature, conditions: MutableList<String>?, ignoreColumns: String?): Observable<Long> {
        val existingDtCbdk = TableInfo.read(db.openHelper.writableDatabase, tableName)
        var spFeature = SpFeature(tableName, feature, existingDtCbdk);
        return Observable.create<Long>(object : ObservableOnSubscribe<Long?> {
            override fun subscribe(emitter: ObservableEmitter<Long?>) {
                db.assertNotSuspendingTransaction()
                db.beginTransaction()
                try {
                    var sql = SpSqlBuilder.createUpdateSQL(spFeature, conditions, ignoreColumns);
                    Log.i("orm.spatia.Dao", sql)
                    db.openHelper.writableDatabase.execSQL(sql)
                    emitter.onNext(1)
                    db.setTransactionSuccessful()
                } catch (e: Exception) {
                    e.printStackTrace();
                    emitter.onError(e);
                } finally {
                    db.endTransaction()
                    emitter.onComplete()
                }
            }
        })
    }

    /**
     * 删除
     * @param tableName表名
     * @param whereClause where条件KEY_CONTENT2 + "= '" + content + "'"
     */
    @SuppressLint("RestrictedApi")
    fun delete(tableName: String, whereClause: String): Observable<Long> {
        return Observable.create<Long>(object : ObservableOnSubscribe<Long?> {
            override fun subscribe(emitter: ObservableEmitter<Long?>) {
                db.assertNotSuspendingTransaction()
                db.beginTransaction()
                try {
                    var r = db.openHelper.writableDatabase.delete(tableName, whereClause, null)
                    emitter.onNext(r.toLong())
                    db.setTransactionSuccessful()
                } catch (e: Exception) {
                    e.printStackTrace();
                    emitter.onError(e);
                } finally {
                    db.endTransaction()
                    emitter.onComplete()
                }
            }
        })
    }

    /**
     * 根据表主键删除，没有主键或唯一键则删除失败
     *@param tableName 表名
     *@param feature 要删除的要素
     */
    @SuppressLint("RestrictedApi")
    fun delete(tableName: String, feature: Feature): Observable<Long> {
        val existingDtCbdk = TableInfo.read(db.openHelper.writableDatabase, tableName)
        var spFeature = SpFeature(tableName, feature, existingDtCbdk);
        return Observable.create<Long>(object : ObservableOnSubscribe<Long?> {
            override fun subscribe(emitter: ObservableEmitter<Long?>) {
                db.assertNotSuspendingTransaction()
                db.beginTransaction()
                try {
                    var whereClause = SpSqlBuilder.createDeleteWhereClauseSQL(spFeature);
                    //  Log.i("orm.spatia.Dao",sql)
                    var r = db.openHelper.writableDatabase.delete(tableName, whereClause, null)
                    emitter.onNext(r.toLong())
                    db.setTransactionSuccessful()
                } catch (e: Exception) {
                    e.printStackTrace();
                    emitter.onError(e);
                } finally {
                    db.endTransaction()
                    emitter.onComplete()
                }
            }
        })
    }
}