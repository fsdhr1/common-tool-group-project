package com.gradtech.mapframe.mapdemo;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.gradtech.mapframev10.core.GMap;
import com.gradtech.mapframev10.core.maps.GMapView;
import com.mapbox.common.DownloadOptions;
import com.mapbox.common.HttpRequest;
import com.mapbox.common.HttpResponse;
import com.mapbox.common.HttpServiceInterceptorInterface;
import com.mapbox.maps.Style;
import com.mapbox.maps.extension.observable.eventdata.MapLoadingErrorEventData;
import com.mapbox.maps.extension.style.StyleExtensionImpl;
import com.mapbox.maps.plugin.delegates.listeners.OnMapLoadErrorListener;

import org.jetbrains.annotations.NotNull;

/**
 * @ClassName NetTestActivity
 * @Description TODO
 * @Author: fs
 * @Date: 2022/12/28 13:07
 * @Version 2.0
 */
public class NetTestActivity extends AppCompatActivity {
    GMapView gMapView;
    private Button btnNetTest;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        GMap.getInstance(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_net_test);
        gMapView = findViewById(R.id.mapView);
        btnNetTest = findViewById(R.id.btn_net_interceptor);
        btnNetTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gMapView.getBoxHttpClient().addInterceptors(new HttpServiceInterceptorInterface() {
                    @NonNull
                    @Override
                    public HttpRequest onRequest(@NonNull HttpRequest request) {
                        Log.i("mapboxv10","HttpRequest");
                        String url = request.getUrl();
                        if(url.contains("a.tiles.mapbox.com")){
                            url = url.replace("sk.eyJ1IjoiZnMxOTk1MDMwMSIsImEiOiJja2xhZ210czgzamVuMnNxb2dkcTN6ZXRyIn0.OjKkjFdRWrZBP432CSAGHw1111","sk.eyJ1IjoiZnMxOTk1MDMwMSIsImEiOiJja2xhZ210czgzamVuMnNxb2dkcTN6ZXRyIn0.OjKkjFdRWrZBP432CSAGHw");
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
        });
        String MAP_OFFLINE_STYLE_JSON ="asset://style.json";
        StyleExtensionImpl.Builder styleExtension = new StyleExtensionImpl.Builder(MAP_OFFLINE_STYLE_JSON);
        gMapView.getMapboxMap().loadStyle(styleExtension.build(), new Style.OnStyleLoaded() {
            @Override
            public void onStyleLoaded(@NotNull Style style) {

            }

        }, new OnMapLoadErrorListener() {
            @Override
            public void onMapLoadError(@NotNull MapLoadingErrorEventData mapLoadingErrorEventData) {
                Log.i("getMapboxMap", mapLoadingErrorEventData.toString());
            }
        });
    }
}
