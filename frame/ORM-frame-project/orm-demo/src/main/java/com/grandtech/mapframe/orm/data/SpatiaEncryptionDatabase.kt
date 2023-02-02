package com.grandtech.mapframe.orm.data

import android.content.Context
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.grandtech.mapframe.orm.data.dao.CbdkDao
import com.grandtech.mapframe.orm.data.dao.PostsDao
import com.grandtech.mapframe.orm.data.model.CKDK
import com.grandtech.mapframe.orm.data.model.Converters
import com.grandtech.mapframe.orm.data.model.Post
import com.grandtech.orm.spatia.builder.SpRoomDatabase
import com.grandtech.orm.spatia.builder.SpatiaRoom

/**
 * @ClassName SpatiaEncryptionDatabase
 * @Description TODO 加密创建
 * @Author: fs
 * @Date: 2021/8/17 16:04
 * @Version 2.0
 */
@Database(
        entities = [Post::class],
        version = 2,
        exportSchema = false
)
@TypeConverters(Converters::class)
abstract class SpatiaEncryptionDatabase : SpRoomDatabase() {


    abstract fun getPostsDao(): PostsDao
    abstract fun getCbdkDao(): CbdkDao
    class Migration1_2(startVersion: Int, endVersion: Int) : Migration(startVersion, endVersion){
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("CREATE TABLE IF NOT EXISTS `geo_posts` (`id` INTEGER, `title` TEXT, `author` TEXT, `body` TEXT, `imageUrl` TEXT, PRIMARY KEY(`id`))")
            database.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)")
            database.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, 'f7202404ebcc0b342990bf6faab0532e')")
        }

    }
    class Migration2_3(startVersion: Int, endVersion: Int) : Migration(startVersion, endVersion){
        override fun migrate(database: SupportSQLiteDatabase) {

        }

    }
    companion object {
        /* var ROOT_PATH =
             Environment.getExternalStorageDirectory().absolutePath.toString()
         var APP_PATH = "$ROOT_PATH/IntellG"
          val DB_NAME = "$APP_PATH/encryption.db"*/
        val DB_NAME = "encryption_test1.db"

        @Volatile
        private var INSTANCE: SpatiaEncryptionDatabase? = null

        fun getInstance(context: Context): SpatiaEncryptionDatabase {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }

            synchronized(this) {
                val instance = SpatiaRoom.databaseBuilder(
                        context.applicationContext,
                        SpatiaEncryptionDatabase::class.java,
                        DB_NAME,
                        "6e8eb4"
                ).addMigrations(Migration1_2(1,2)).createFromAsset("244a9043-1a4c-446d-97fd-493a5d4f75ef.db").build()

                INSTANCE = instance
                return instance
            }
        }




    }
}