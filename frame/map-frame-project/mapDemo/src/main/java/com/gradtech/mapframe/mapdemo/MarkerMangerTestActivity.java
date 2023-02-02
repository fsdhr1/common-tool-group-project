package com.gradtech.mapframe.mapdemo;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.SizeUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.google.gson.JsonElement;
import com.gradtech.mapframev10.core.GMap;
import com.gradtech.mapframev10.core.enumeration.GeometryType;
import com.gradtech.mapframev10.core.event.IMapEvent;
import com.gradtech.mapframev10.core.maps.GMapView;
import com.gradtech.mapframev10.core.marker.MarkerView;
import com.gradtech.mapframev10.core.util.StringEngine;
import com.gradtech.mapframev10.core.util.StyleJsonBuilder;
import com.mapbox.common.DownloadOptions;
import com.mapbox.common.HttpRequest;
import com.mapbox.common.HttpResponse;
import com.mapbox.common.HttpServiceInterceptorInterface;
import com.mapbox.geojson.Geometry;
import com.mapbox.geojson.Point;
import com.mapbox.maps.Style;
import com.mapbox.maps.extension.observable.eventdata.MapLoadingErrorEventData;
import com.mapbox.maps.extension.style.StyleExtensionImpl;
import com.mapbox.maps.plugin.Plugin;
import com.mapbox.maps.plugin.annotation.generated.OnPointAnnotationClickListener;
import com.mapbox.maps.plugin.annotation.generated.OnPointAnnotationLongClickListener;
import com.mapbox.maps.plugin.annotation.generated.PointAnnotation;
import com.mapbox.maps.plugin.delegates.listeners.OnMapLoadErrorListener;
import com.mapbox.maps.plugin.gestures.GesturesPluginImpl;
import com.mapbox.maps.plugin.logo.LogoViewPlugin;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

/**
 * @ClassName MarkerMangerTestActivity
 * @Description TODO
 * @Author: fs
 * @Date: 2022/12/28 14:35
 * @Version 2.0
 */
public class MarkerMangerTestActivity extends AppCompatActivity implements IMapEvent {

    GMapView gMapView;
    private Button btnLocation,btnMoveToLocation,btnHz,btnMoveToLocationByGeometry,btnAddMarkerview,btnAddMarker,btnAddSimpleMarker;

    private boolean isAddMarker = false;
    private boolean isAddAnnotationMarker = false;
    boolean isAddSimpleMarker = false;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        GMap.getInstance(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_marker_test);
        gMapView = findViewById(R.id.mapView);
        btnLocation = findViewById(R.id.btn_location);
        btnMoveToLocation = findViewById(R.id.btn_moveToLocation);
        btnHz = findViewById(R.id.btn_hz);
        btnMoveToLocationByGeometry = findViewById(R.id.btn_moveToLocationByGeometry);
        btnAddMarkerview = findViewById(R.id.btn_add_markerview);
        btnAddMarker = findViewById(R.id.btn_add_marker);
        btnAddSimpleMarker = findViewById(R.id.btn_add_simple_marker);
        btnLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gMapView.setLocation(41.8218980192575,123.437377173488);
                gMapView.moveToCurrentLocation(15);
            }
        });
        btnMoveToLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gMapView.moveToLocation(41.8218980192575,123.437377173488,15);
            }
        });
        btnHz.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(gMapView.isStartSketch()){
                    gMapView.stopSketch();
                    return;
                }
                gMapView.startSketch(GeometryType.polygon);
            }
        });
        btnMoveToLocationByGeometry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Geometry sketchGeometry = gMapView.getSketchGeometry();
                gMapView.moveToLocation(sketchGeometry);
            }
        });
        btnAddMarkerview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isAddMarker = !isAddMarker;

            }
        });

        btnAddMarker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isAddAnnotationMarker = !isAddAnnotationMarker;
            }
        });
        btnAddSimpleMarker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isAddSimpleMarker = !isAddSimpleMarker;
            }
        });
        addInterceptor();
        //String MAP_OFFLINE_STYLE_JSON ="asset://style.json";
       
        String MAP_OFFLINE_STYLE_JSON ="https://api.grandtechmap.com/mapeditor/styles/ASZhSsCC6?access_token=tiletoken.mtyynje0mtm4mjyzotzjztgzmmqzzdmzzjrmogjhndvjowqwowjjy2m2njrk";
        StyleExtensionImpl.Builder styleExtension = new StyleExtensionImpl.Builder(MAP_OFFLINE_STYLE_JSON);
        gMapView.getMapboxMap().loadStyle(styleExtension.build(), new Style.OnStyleLoaded() {
            @Override
            public void onStyleLoaded(@NotNull Style style) {
                LogoViewPlugin plugin = gMapView.getPlugin(Plugin.MAPBOX_LOGO_PLUGIN_ID);
                plugin.setEnabled(false);
                GesturesPluginImpl plugin1 = gMapView.getPlugin(Plugin.MAPBOX_GESTURES_PLUGIN_ID);

                plugin1.setRotateEnabled(false);
                gMapView.addMapEvent(MarkerMangerTestActivity.this);

                gMapView.getMarkerManager().addClickListener(new OnPointAnnotationClickListener() {
                    @Override
                    public boolean onAnnotationClick(@NotNull PointAnnotation pointAnnotation) {
                        JsonElement data = pointAnnotation.getData();
                        LogUtils.i(data.toString());
                        return false;
                    }
                });
                gMapView.getMarkerManager().addLongClickListener(new OnPointAnnotationLongClickListener() {
                    @Override
                    public boolean onAnnotationLongClick(@NotNull PointAnnotation pointAnnotation) {
                        gMapView.getMarkerManager().removeMarker(pointAnnotation);
                        return false;
                    }
                });

                gMapView.moveToLocation(32.4868574154374,112.926891084656, 15.0);
                LogUtils.i("dt_sy",gMapView.getStyleLayerManager().getSourceUri("dt_sy"));
                gMapView.getStyleLayerManager().highLightRegion("POLYGON((112.926891084656 32.4868574154374,112.926946393861 32.4867183622626,112.92693113956 32.4866900587992,112.926837917636 32.4866530597909,112.926799357404 32.4866537522689,112.926689010589 32.4866848841002,112.926562600983 32.4867151345958,112.926399989168 32.4867409019711,112.926237005034 32.486736819049,112.926106399191 32.4867016798387,112.926088369583 32.4867153594263,112.926107854294 32.4869572114062,112.926045040246 32.4870055490669,112.926095347422 32.4872266815661,112.926118639863 32.4872599304015,112.92616078839 32.4873539554208,112.926183419829 32.487554800114,112.926241331672 32.487583029833,112.926268496594 32.4875724232288,112.926350471597 32.4874808938283,112.926646290994 32.4873106602588,112.926775441733 32.4872578565649,112.926901871125 32.4872417317206,112.92690963767 32.4872288938984,112.926862839648 32.4871497283773,112.926848993686 32.4870129181118,112.926855102781 32.4869258475499,112.926891084656 32.4868574154374))",null);

            }

        }, new OnMapLoadErrorListener() {
            @Override
            public void onMapLoadError(@NotNull MapLoadingErrorEventData mapLoadingErrorEventData) {
                Log.i("getMapboxMap", mapLoadingErrorEventData.toString());
            }
        });
    }

    private void addInterceptor() {
        gMapView.getBoxHttpClient().addInterceptors(new HttpServiceInterceptorInterface() {
            @NonNull
            @Override
            public HttpRequest onRequest(@NonNull HttpRequest request) {

                String url = request.getUrl();
                if(url.contains("a.tiles.mapbox.com")){
                    Log.i("mapboxv10","HttpRequest");
                    url = url.replace("sk.eyJ1IjoiZnMxOTk1MDMwMSIsImEiOiJja2xhZ210czgzamVuMnNxb2dkcTN6ZXRyIn0.OjKkjFdRWrZBP432CSAGHw1111","sk.eyJ1IjoiZnMxOTk1MDMwMSIsImEiOiJja2xhZ210czgzamVuMnNxb2dkcTN6ZXRyIn0.OjKkjFdRWrZBP432CSAGHw");
                    Log.i("mapboxv10",url);
                    HttpRequest build = request.toBuilder().url(url).build();
                    return build;
                }

                return request;
            }

            @NonNull
            @Override
            public DownloadOptions onDownload(@NonNull DownloadOptions download) {
                Log.i("mapboxv10","download");
                return download;
            }

            @NonNull
            @Override
            public HttpResponse onResponse(@NonNull HttpResponse response) {

                Log.i("mapboxv10","response");
                return response;
            }
        });
    }


    View createMarkerView(){
        View customView = LayoutInflater.from(this).inflate(R.layout.activity_map_marker_view, null);
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(WRAP_CONTENT,WRAP_CONTENT);
        customView.setLayoutParams(layoutParams);
        LinearLayout llhv = customView.findViewById(R.id.ll_hv);
        llhv.setVisibility(View.INVISIBLE);
        return customView;
    }

    @Override
    public boolean onMapClick(@NonNull Point point) {
        if(isAddMarker){
            View view = createMarkerView();
            MarkerView markerView =  new MarkerView(StringEngine.get32UUID(),point,view);
            markerView.setOffsetX(-SizeUtils.dp2px(220)/2);
            markerView.setOffsetY(-SizeUtils.dp2px(197));
            markerView.setOnClickListener(R.id.ll_triangle,new MarkerView.OnClickListener() {
                @Override
                public void onclick(View view, MarkerView markerView) {
                    LinearLayout llhv = markerView.getView().findViewById(R.id.ll_hv);
                    llhv.setVisibility(llhv.getVisibility() == View.INVISIBLE?View.VISIBLE:View.INVISIBLE);
                }

                @Override
                public void onLongCick(View view, MarkerView markerView) {
                    gMapView.getMarkerViewManager().removeMarker(markerView);
                }

            });
            gMapView.getMarkerViewManager().addMarker(markerView);
        }

        if(isAddAnnotationMarker){
            Drawable bitmapDrawable6 = getResources().getDrawable(R.mipmap.acq_location_green);
            AAA aaa =new AAA();
            aaa.setAaa("ddd");
            aaa.setBbb("dddffg");
            List<Double> doubleList = new ArrayList<>();
            doubleList.add(0.0);
            doubleList.add(3.0);
            gMapView.getMarkerManager().addMarker("ee",point,BitampUtil.drawableToBitamp(bitmapDrawable6,22,31),aaa,"22",doubleList,"#333333");

        }

        if(isAddSimpleMarker){
            AAA aaa =new AAA();
            aaa.setAaa("ddd");
            aaa.setBbb("dddffg");
            gMapView.getMarkerManager().addSimpleAnnotation(StringEngine.get32UUID(),point,"当前信息","冬小麦大豆互欢\n还接口和交互\n接口fdsfs\ndddd\nddddada",false,aaa);
        }
        return true;
    }

}
