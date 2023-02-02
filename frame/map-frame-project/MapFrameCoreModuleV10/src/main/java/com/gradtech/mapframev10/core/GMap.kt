package com.gradtech.mapframev10.core

import android.content.Context
import com.gradtech.mapframev10.R
import com.mapbox.maps.ResourceOptionsManager
import com.mapbox.maps.TileStoreUsageMode

/**
 * @ClassName GMap
 * @Description TODO
 * @Author: fs
 * @Date: 2022/8/2 10:25
 * @Version 2.0
 */
object GMap {

    @JvmStatic
    fun getInstance( context : Context){
        ResourceOptionsManager.getDefault(context, context.getResources().getString(R.string.mapbox_access_token)).update {
            tileStoreUsageMode(TileStoreUsageMode.READ_ONLY)
        }
    }
}