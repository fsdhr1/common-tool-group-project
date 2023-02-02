package com.grandtech.orm.spatia.builder

import android.content.Context
import androidx.annotation.Keep
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteOpenHelper
import com.grandtech.orm.spatia.db.SpatiaHelperFactory
import java.util.concurrent.Executor
@Keep
class SpatiaBuilder<T : RoomDatabase?> (
    context: Context,
    klass: Class<T>,
    name: String,
    password: String
): SpatiaRoom.Builder<T> {

    //当数据库不存在是，根据模板spatia_db_template.sqlite创建一个普通空间库
   // private val templateDb = "spatia_db_template.sqlite"

    private val roomBuilder = Room.databaseBuilder(
        context.applicationContext,
        klass,
        name
    )//.createFromAsset(templateDb)
        .openHelperFactory(SpatiaHelperFactory(password))

    override fun createFromAsset(databaseFilePath: String): SpatiaRoom.Builder<T> {
        roomBuilder.createFromAsset(databaseFilePath)
        return this
    }

    override fun openHelperFactory(factory: SupportSQLiteOpenHelper.Factory?): SpatiaRoom.Builder<T> {
        roomBuilder.openHelperFactory(factory)
        return this
    }

    override fun addMigrations(vararg migrations: Migration): SpatiaRoom.Builder<T> {
        roomBuilder.addMigrations(*migrations)
        return this
    }

    override fun allowMainThreadQueries(): SpatiaRoom.Builder<T> {
        roomBuilder.allowMainThreadQueries()
        return this
    }

    override fun setJournalMode(journalMode: RoomDatabase.JournalMode): SpatiaRoom.Builder<T> {
        roomBuilder.setJournalMode(journalMode)
        return this
    }

    override fun setQueryExecutor(executor: Executor): SpatiaRoom.Builder<T> {
        roomBuilder.setQueryExecutor(executor)
        return this
    }

    override fun setTransactionExecutor(executor: Executor): SpatiaRoom.Builder<T> {
        roomBuilder.setTransactionExecutor(executor)
        return this
    }

    override fun enableMultiInstanceInvalidation(): SpatiaRoom.Builder<T> {
        roomBuilder.enableMultiInstanceInvalidation()
        return this
    }

    override fun fallbackToDestructiveMigration(): SpatiaRoom.Builder<T> {
        roomBuilder.fallbackToDestructiveMigration()
        return this
    }

    override fun fallbackToDestructiveMigrationOnDowngrade(): SpatiaRoom.Builder<T> {
        roomBuilder.fallbackToDestructiveMigrationOnDowngrade()
        return this
    }

    override fun fallbackToDestructiveMigrationFrom(vararg startVersions: Int): SpatiaRoom.Builder<T> {
        roomBuilder.fallbackToDestructiveMigrationFrom(*startVersions)
        return this
    }

    override fun addCallback(callback: RoomDatabase.Callback): SpatiaRoom.Builder<T> {
        roomBuilder.addCallback(callback)
        return this
    }

    override fun build(): T = roomBuilder.build()


}
