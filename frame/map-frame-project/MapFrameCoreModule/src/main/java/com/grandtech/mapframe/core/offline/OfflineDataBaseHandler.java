package com.grandtech.mapframe.core.offline;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import androidx.annotation.Nullable;

import com.grandtech.mapframe.core.rules.Rules;

/**
 * @ClassName OfflineDataBaseHander
 * @Description TODO mapbox离线数据库操作类
 * @Author: fs
 * @Date: 2021/10/21 13:39
 * @Version 2.0
 */
public class OfflineDataBaseHandler implements Rules {

    private Context context;

    private OfflineDataBaseHelper offlineDataBaseHelper ;

    public OfflineDataBaseHandler(@Nullable Context context) {
        this.context = context;
        offlineDataBaseHelper = OfflineDataBaseHelper.create(context);
        offlineDataBaseHelper.close();
    }

    /**
     * 清空对应source的瓦片缓存
     * @param sourceLayer
     * @param iExecCallBack
     */
    public void clearTileCacheBySourceLayer(@Nullable String sourceLayer,IExecCallBack iExecCallBack){
        new DbCommand<Boolean>() {
            @Override
            protected Boolean doInBackground() {
                Boolean  r = clearTileCacheBySourceLayer(sourceLayer);
                return r;
            }

            @Override
            protected void onPostExecute(Boolean result) {
                super.onPostExecute(result);
                if (result) {
                    iExecCallBack.onSuccess();
                } else {
                    iExecCallBack.onError("失败");
                }
            }
        }.execute();
    }


    private Boolean clearTileCacheBySourceLayer( @Nullable String sourceLayer){
        SQLiteDatabase sqLiteDatabase  = offlineDataBaseHelper.getWritableDatabase();;
        Boolean r = true;
        try {
            sqLiteDatabase.execSQL("delete from tiles where url_template = '"+sourceLayer+"';");
        }catch (Exception e){
            e.printStackTrace();
            r = false;
        }finally {
            sqLiteDatabase.close();
        }
        return r;
    }


    public void onDestroy(){
        if(offlineDataBaseHelper!=null) {
            offlineDataBaseHelper.close();
        }
    }

    public interface IExecCallBack{
       default void onSuccess(){};
        default void onError(String msg){};
    }
}
