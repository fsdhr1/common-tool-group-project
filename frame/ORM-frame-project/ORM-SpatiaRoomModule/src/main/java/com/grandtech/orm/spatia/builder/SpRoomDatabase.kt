package com.grandtech.orm.spatia.builder

import androidx.annotation.Keep
import androidx.room.DatabaseConfiguration
import androidx.room.InvalidationTracker
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import com.grandtech.orm.spatia.Dao.SpBaseDao

/**
 * @ClassName SpRoomDatabase
 * @Description TODO
 * @Author: fs
 * @Date: 2021/8/23 10:26
 * @Version 2.0
 */
@Keep
abstract class SpRoomDatabase : RoomDatabase() {

   private var spBaseDao:SpBaseDao? = null

    fun getSpBaseDao(): SpBaseDao{
        if(spBaseDao ==null){
            spBaseDao = SpBaseDao(this)
        }
        return this.spBaseDao!!;
    }
}