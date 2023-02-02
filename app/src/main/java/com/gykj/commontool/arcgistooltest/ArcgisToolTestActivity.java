package com.gykj.commontool.arcgistooltest;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.esri.arcgisruntime.data.ShapefileFeatureTable;
import com.esri.arcgisruntime.layers.ArcGISTiledLayer;
import com.esri.arcgisruntime.layers.FeatureLayer;
import com.esri.arcgisruntime.loadable.LoadStatus;
import com.esri.arcgisruntime.mapping.ArcGISMap;
import com.esri.arcgisruntime.mapping.Viewpoint;
import com.esri.arcgisruntime.mapping.view.Graphic;
import com.esri.arcgisruntime.mapping.view.MapView;
import com.gykj.arcgistool.common.GraphType;
import com.gykj.arcgistool.listener.IDrawGraphListener;
import com.gykj.arcgistool.views.ArcGisZoomView;
import com.gykj.arcgistool.views.DrawGraphView;
import com.gykj.arcgistool.views.MeasureToolView;

import com.gykj.commontool.R;

import androidx.appcompat.app.AppCompatActivity;

public class ArcgisToolTestActivity extends AppCompatActivity {
    private final static String TAG = ArcgisToolTestActivity.class.getSimpleName();
    private MapView mMapView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_arcgistooltest);
        // inflate MapView from layout
        mMapView = (MapView) findViewById(R.id.mapView);
        ArcGISMap map = new ArcGISMap();
        mMapView.setMap(map);
        String MapUrl="https://map.geoq.cn/ArcGIS/rest/services/ChinaOnlineCommunity/MapServer";
        //图层的创建
        ArcGISTiledLayer mapServiceLayer = new ArcGISTiledLayer(MapUrl);
        //图层的添加
        map.getOperationalLayers().add(mapServiceLayer);
        //初始化工具条
        ArcGisZoomView zoomBtn=(ArcGisZoomView)findViewById(R.id.arcgis_zoom_btn);
        zoomBtn.init(mMapView);

        MeasureToolView measureToolView=(MeasureToolView)findViewById(R.id.arcgis_measure);
        measureToolView.init(mMapView);

        DrawGraphView graphView=findViewById(R.id.arcgis_draw_tool);
        graphView.init(mMapView);
        graphView.setDrawGraphListener(new IDrawGraphListener() {
            @Override
            public void drawEnd(GraphType graphType, Graphic graphic) {
                if(graphic!=null) {
                    Toast.makeText(ArcgisToolTestActivity.this, graphic.getGeometry().toJson(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void drawStart(GraphType graphType) {

            }
        });
    }
    @Override
    protected void onPause() {
        super.onPause();
        mMapView.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mMapView.resume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMapView.dispose();
    }
}
