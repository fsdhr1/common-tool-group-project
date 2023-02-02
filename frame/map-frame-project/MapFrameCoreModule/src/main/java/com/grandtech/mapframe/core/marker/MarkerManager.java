package com.grandtech.mapframe.core.marker;

import androidx.annotation.NonNull;

import com.grandtech.mapframe.core.maps.GMapView;
import com.grandtech.mapframe.core.marker.bean.MarkerSetting;
import com.grandtech.mapframe.core.rules.Rules;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapboxMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @ClassName MakerManager
 * @Description TODO Maker管理
 * @Author: fs
 * @Date: 2021/6/11 8:38
 * @Version 2.0
 */
public class MarkerManager implements Rules {

    private GMapView mGMapView;

    private Map<String,Marker> mMarkerMap;

    private Map<String,List<Marker>> mMarkerGroups;

    public MarkerManager(@NonNull GMapView gMapView){
        this.mGMapView = gMapView;
    }

    /**
     * 添加单个marker
     * @param markerSetting
     * @return
     */
    public Marker addMarker(@NonNull MarkerSetting markerSetting){
        MarkerOptions markerOptions = new MarkerOptions();
        if(markerSetting.getIcon() != null){
            markerOptions.setIcon(IconFactory.getInstance(mGMapView.getContext()).fromResource(markerSetting.getIcon()));
        }
        markerOptions.setPosition(markerSetting.getLatLng());
        markerOptions.setTitle(markerSetting.getTitle());
        markerOptions.setSnippet(markerSetting.getSnippet());
        Marker marker = mGMapView.getMapBoxMap().addMarker(markerOptions);
        if(mMarkerMap == null){
            mMarkerMap = new HashMap<>();
        }
        mMarkerMap.put(markerSetting.getMarkerId(),marker);
        if(markerSetting.isMove()){
            mGMapView.moveToLocation(markerSetting.getLatLng().getLatitude(),markerSetting.getLatLng().getLongitude(),16.0,null);
        }
        return marker;
    }

    /**
     * 根据markerId移除marker
     * @param markerId
     * @return
     */
    public boolean removeMarker(@NonNull String markerId){
        if(mMarkerMap == null){
           return false;
        }
        if(mMarkerMap.containsKey(markerId)){
            mGMapView.getMapBoxMap().removeMarker(mMarkerMap.get(markerId));
            mMarkerMap.remove(markerId);
            return true;
        }
        return false;
    }

    /**
     * 根据markerSetting移除marker
     * @param markerSetting
     * @return
     */
    public boolean removeMarker(@NonNull MarkerSetting markerSetting){
        if(mMarkerMap == null){
            return false;
        }
        if(mMarkerMap.containsKey(markerSetting.getMarkerId())){
            mGMapView.getMapBoxMap().removeMarker(mMarkerMap.get(markerSetting.getMarkerId()));
            mMarkerMap.remove(markerSetting.getMarkerId());
            return true;
        }
        return false;
    }

    /**
     * 添加marker组
     * @param markerGroupId
     * @param markerSettings
     * @return
     */
    public List<Marker> addMarkers(String markerGroupId , List<MarkerSetting> markerSettings){
        if(markerGroupId == null || markerSettings == null){
            return null;
        }
        List<Marker> markers = new ArrayList<>(markerSettings.size());
        for(MarkerSetting markerSetting : markerSettings){
            MarkerOptions markerOptions = new MarkerOptions();
            if(markerSetting.getIcon() != null){
                markerOptions.setIcon(IconFactory.getInstance(mGMapView.getContext()).fromResource(markerSetting.getIcon()));
            }
            markerOptions.setPosition(markerSetting.getLatLng());
            markerOptions.setTitle(markerSetting.getTitle());
            markerOptions.setSnippet(markerSetting.getSnippet());
            Marker marker = mGMapView.getMapBoxMap().addMarker(markerOptions);
            markers.add(marker);
        }
        if(mMarkerGroups == null){
            mMarkerGroups = new HashMap<>();
        }
        mMarkerGroups.put(markerGroupId,markers);
        return markers;
    }


    /**
     * 根据markerGroupId移除markerGroup
     * @param markerGroupId
     * @return
     */
    public boolean removeMarkerGroup(@NonNull String markerGroupId){
        if(mMarkerGroups == null){
            return false;
        }
        if(mMarkerGroups.containsKey(markerGroupId)){
            List<Marker> markers = mMarkerGroups.get(markerGroupId);
            if(markers != null){
                for(Marker marker : markers){
                    mGMapView.getMapBoxMap().removeMarker(marker);
                }
            }
            mMarkerGroups.remove(markerGroupId);
            return true;
        }
        return false;
    }

    /**
     * 移除所有marker
     */
    public void removeAllMarker(){
        if(mMarkerMap!=null){
            for(Map.Entry<String,Marker> entry : mMarkerMap.entrySet()){
                if(mMarkerMap == null){
                    return ;
                }
                if(mMarkerMap.containsKey(entry.getKey())){
                    mGMapView.getMapBoxMap().removeMarker(mMarkerMap.get(entry.getKey()));
                }
            }
            mMarkerMap.clear();
        }
        if(mMarkerGroups != null){
            for(Map.Entry<String,List<Marker>> entry : mMarkerGroups.entrySet()){
                if(mMarkerGroups == null){
                    return ;
                }
                if(mMarkerGroups.containsKey(entry.getKey())){
                    List<Marker> markers = mMarkerGroups.get(entry.getKey());
                    if(markers != null){
                        for(Marker marker : markers){
                            mGMapView.getMapBoxMap().removeMarker(marker);
                        }
                    }
                }
            }
            mMarkerGroups.clear();
        }
    }

}
