package com.grandtech.mapframe.orm.data

import android.content.Context
import android.os.Environment
import androidx.room.Database
import androidx.room.RoomDatabase
import com.grandtech.orm.spatia.builder.SpatiaRoom
import com.grandtech.mapframe.orm.data.dao.PostsDao
import com.grandtech.mapframe.orm.data.model.Post


@Database(
    entities = [Post::class],
    version = 1,
        exportSchema = false
)
/**
 * 普通不加密
 */
abstract class SpatiaDatabase : RoomDatabase() {

    /**
     * @return [PostsDao] Foodium Posts Data Access Object.
     */
    abstract fun getPostsDao(): PostsDao
    companion object {
       /* var ROOT_PATH =
            Environment.getExternalStorageDirectory().absolutePath.toString()
        var APP_PATH = "$ROOT_PATH/IntellG"
         val DB_NAME = "$APP_PATH/geo_database.db"*/
        val DB_NAME = "geo_database.db"
        @Volatile
        private var INSTANCE: SpatiaDatabase? = null

        fun getInstance(context: Context): SpatiaDatabase {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }

            synchronized(this) {
                val instance = SpatiaRoom.databaseBuilder(
                    context.applicationContext,
                        SpatiaDatabase::class.java,
                    DB_NAME
                ).createFromAsset("spatia_db_template.sqlite").build()

                INSTANCE = instance
                return instance
            }
        }

    }
}