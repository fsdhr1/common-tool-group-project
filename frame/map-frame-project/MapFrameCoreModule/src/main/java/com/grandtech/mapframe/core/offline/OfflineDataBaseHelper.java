package com.grandtech.mapframe.core.offline;

import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Build;
import android.os.Environment;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import java.io.File;

/**
 * @ClassName OfflineDataBaseHelper
 * @Description TODO 操作mapbox离线数据库
 * @Author: fs
 * @Date: 2021/10/21 13:30
 * @Version 2.0
 */
public class OfflineDataBaseHelper extends SQLiteOpenHelper {

    public static OfflineDataBaseHelper create(@Nullable Context context){
        String mDirPath = context.getFilesDir().getAbsolutePath()+ File.separator+"mbgl-offline.db";
        //String mDirPath = Environment.getExternalStorageDirectory().getAbsolutePath()+ File.separator+"toolmapframe" + File.separator+"mbgl-offline.db";
        OfflineDataBaseHelper offlineDataBaseHelper = new OfflineDataBaseHelper(context,mDirPath,null,100);
        return offlineDataBaseHelper;
    }

    public OfflineDataBaseHelper(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    public OfflineDataBaseHelper(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version, @Nullable DatabaseErrorHandler errorHandler) {
        super(context, name, factory, version, errorHandler);
    }

    @RequiresApi(api = Build.VERSION_CODES.P)
    public OfflineDataBaseHelper(@Nullable Context context, @Nullable String name, int version, @NonNull SQLiteDatabase.OpenParams openParams) {
        super(context, name, version, openParams);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

}
