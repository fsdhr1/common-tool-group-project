package com.gykj.commontool.maptest;

import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.gykj.commontool.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.expressions.Expression;
import com.mapbox.mapboxsdk.style.layers.FillLayer;
import com.mapbox.mapboxsdk.style.layers.PropertyFactory;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.mapboxsdk.style.sources.VectorSource;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;

import static com.mapbox.mapboxsdk.style.expressions.Expression.eq;
import static com.mapbox.mapboxsdk.style.expressions.Expression.get;
import static com.mapbox.mapboxsdk.style.expressions.Expression.interpolate;
import static com.mapbox.mapboxsdk.style.expressions.Expression.literal;
import static com.mapbox.mapboxsdk.style.expressions.Expression.stop;
import static com.mapbox.mapboxsdk.style.layers.Property.VISIBLE;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.fillColor;

/**
 * @ClassName AddRainFallStyleActivity
 * @Description TODO 数据延时
 * @Author: fs
 * @Date: 2021/8/5 9:41
 * @Version 2.0
 */
public class AddRainFallStyleActivity extends AppCompatActivity implements OnMapReadyCallback {
    public static final String ID_SOURCE = "source-id";
    public static final String ID_LAYER = "layer-id";
    public static final String SOURCE_URL = "mapbox://examples.dwtmhwpu";
    private MapView mapView;
    private Handler handler;
    private FillLayer layer;
    private int index = 1;
    private RefreshGeoJsonRunnable refreshGeoJsonRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

// Mapbox access token is configured here. This needs to be called either in your application
// object or in the same activity which contains the mapview.
        Mapbox.getInstance(this, getString(R.string.mapbox_access_token));

// This contains the MapView in XML and needs to be called after the account manager
        setContentView(R.layout.activity_map_style_rainfall);

        handler = new Handler();
        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
    }

    @Override
    public void onMapReady(@NonNull final MapboxMap mapboxMap) {
        String url ="http://gykj123.cn:9035/api/v1/styles/gykj/rJg3x0CN_7";
        mapboxMap.setStyle(url, new Style.OnStyleLoaded() {
            @Override
            public void onStyleLoaded(@NonNull Style style) {
                addRadarData(style);
                refreshGeoJsonRunnable = new RefreshGeoJsonRunnable();
                do {
                    handler.postDelayed(refreshGeoJsonRunnable, 1000);
                }
                while (index == 4);
            }
        });
    }

    private class RefreshGeoJsonRunnable implements Runnable {
        @Override
        public void run() {
            layer.setFilter(eq((Expression.get("sj")), literal(index)));
            index++;
            if (index == 4) {
                index = 1;
            }
            handler.postDelayed(this, 1000);
        }
    }

    private void addRadarData(@NonNull Style loadedMapStyle)  {
      /*  VectorSource vectorSource = new VectorSource(
                ID_SOURCE,loadJsonFromAsset("geojson.json")
                SOURCE_URL
        );*/
        try {
            GeoJsonSource geoJsonSource = new GeoJsonSource(ID_SOURCE,new URI("asset://geojson.json"));
            loadedMapStyle.addSource(geoJsonSource);
            //  geoJsonSource.setUri("asset://geojson.json");
            layer = loadedMapStyle.getLayerAs(ID_LAYER);
            if (layer == null) {
                layer = new FillLayer(ID_LAYER, ID_SOURCE);
                //    layer.withSourceLayer(ID_SOURCE);
                    layer.setFilter(eq((get("sj")), literal(1)));
                layer.setProperties(PropertyFactory.visibility(VISIBLE),
                        fillColor(interpolate(Expression.exponential(1f),
                                get("wd"),
                                stop(15, Expression.rgb(3, 12, 242)),
                                stop(17, Expression.rgb(135, 250, 80)),
                                stop(18, Expression.rgb(250, 250, 0)),
                                stop(19, Expression.rgb(250, 180, 0)),
                                stop(20, Expression.rgb(250, 110, 0)),
                                stop(21, Expression.rgb(250, 40, 0)),
                                stop(22, Expression.rgb(180, 40, 40)),
                                stop(23, Expression.rgb(110, 40, 80)),
                                stop(24, Expression.rgb(80, 40, 110)),
                                stop(25, Expression.rgb(50, 40, 140)),
                                stop(26, Expression.rgb(239, 11, 7))

                                )
                        ),
                        PropertyFactory.fillOpacity(0.7f));
                loadedMapStyle.addLayer(layer);
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }
    private String loadJsonFromAsset(String filename) {
        // Using this method to load in GeoJSON files from the assets folder.
        try {
            InputStream is = getAssets().open(filename);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            return new String(buffer, Charset.forName("UTF-8"));

        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
    }
    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
        handler.removeCallbacks(refreshGeoJsonRunnable);
        refreshGeoJsonRunnable = null;
        handler = null;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }
}