package com.grandtech.mapframe.core.util;

import android.graphics.Color;

import com.mapbox.mapboxsdk.style.layers.CircleLayer;
import com.mapbox.mapboxsdk.style.layers.FillLayer;
import com.mapbox.mapboxsdk.style.layers.LineLayer;
import com.mapbox.mapboxsdk.style.layers.PropertyFactory;

public final class LayerUtil {

    public static LineLayer fillRenderLine(FillLayer fillLayer) {
        LineLayer lineLayer = new LineLayer(fillLayer.getId() + "_render", fillLayer.getSourceId());
        lineLayer.withSourceLayer(fillLayer.getSourceLayer()).withProperties(PropertyFactory.lineColor(Color.TRANSPARENT), PropertyFactory.lineWidth(3f), PropertyFactory.lineOpacity(1f));
        if (fillLayer.getFilter() != null) {
            lineLayer.setFilter(fillLayer.getFilter());
        }
        lineLayer.setMaxZoom(fillLayer.getMaxZoom());
        lineLayer.setMinZoom(fillLayer.getMinZoom());
        return lineLayer;
    }


    public static LineLayer lineRenderLine(LineLayer lineLayerV) {
        LineLayer lineLayer = new LineLayer(lineLayerV.getId() + "_render", lineLayerV.getSourceId());
        lineLayer.withSourceLayer(lineLayerV.getSourceLayer()).withProperties(PropertyFactory.lineColor(Color.TRANSPARENT), PropertyFactory.lineWidth(3f), PropertyFactory.lineOpacity(1f));
        if (lineLayerV.getFilter() != null) {
            lineLayer.setFilter(lineLayerV.getFilter());
        }
        lineLayer.setMaxZoom(lineLayerV.getMaxZoom());
        lineLayer.setMinZoom(lineLayerV.getMinZoom());
        return lineLayer;
    }

    public static CircleLayer circleRenderCircle(CircleLayer circleLayerV) {
        CircleLayer circleLayer = new CircleLayer(circleLayerV.getId() + "_render", circleLayerV.getSourceId());
        circleLayer.withSourceLayer(circleLayerV.getSourceLayer()).withProperties(PropertyFactory.circleColor(Color.TRANSPARENT), PropertyFactory.circleRadius(4f), PropertyFactory.circleOpacity(1f));
        if (circleLayerV.getFilter() != null) {
            circleLayer.setFilter(circleLayerV.getFilter());
        }
        circleLayerV.setMaxZoom(circleLayerV.getMaxZoom());
        circleLayerV.setMinZoom(circleLayerV.getMinZoom());
        return circleLayer;
    }
}
