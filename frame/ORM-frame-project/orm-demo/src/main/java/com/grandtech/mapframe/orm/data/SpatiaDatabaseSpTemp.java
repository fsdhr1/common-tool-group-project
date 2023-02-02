package com.grandtech.mapframe.orm.data;

import android.content.Context;
import android.os.Environment;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.grandtech.mapframe.orm.data.model.Post;
import com.grandtech.orm.spatia.builder.SpRoomDatabase;
import com.grandtech.orm.spatia.builder.SpatiaRoom;

/**
 * @ClassName SpatiaDatabaseSpTemp
 * @Description TODO
 * @Author: fs
 * @Date: 2022/6/22 8:39
 * @Version 2.0
 */
@Database(
        entities = {Post.class},
        version = 1,
        exportSchema = false
)
public abstract  class SpatiaDatabaseSpTemp extends SpRoomDatabase {

    //我们对数据的操作为了避免被滥用实例化，使用单例模式，singleTon，并且加锁
    private  static SpatiaDatabaseSpTemp INSTANCE;
    //创建一个方法，这个方法返回一个BookDatabase
    public  synchronized  static SpatiaDatabaseSpTemp getDatabase(Context context){
        if (INSTANCE==null){
            String ROOT_PATH =
                    Environment.getExternalStorageDirectory().toString();
            //如果为创建这个数据库，那么就呼叫builder来创建数据库
            INSTANCE = SpatiaRoom.
                    databaseBuilder(context.getApplicationContext(),
                            SpatiaDatabaseSpTemp.class,ROOT_PATH+"/sptest/"+"sptemp.db")
                    .build();
        }
        return INSTANCE;
    }

}
