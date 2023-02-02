package com.gradtech.mapframev10.core.util;

import android.graphics.Color;

import com.mapbox.maps.extension.style.layers.generated.CircleLayer;
import com.mapbox.maps.extension.style.layers.generated.FillLayer;
import com.mapbox.maps.extension.style.layers.generated.LineLayer;


public final class LayerUtil {

    public static LineLayer fillRenderLine(FillLayer fillLayer) {
        LineLayer lineLayer = new LineLayer(fillLayer.getLayerId() + "_render", fillLayer.getSourceId());
        lineLayer.sourceLayer(fillLayer.getSourceLayer())
        .lineColor(Color.TRANSPARENT)
        .lineWidth(3f)
        .lineOpacity(1f);
        if (fillLayer.getFilter() != null) {
            lineLayer.filter(fillLayer.getFilter());
        }
        lineLayer.maxZoom((fillLayer.getMaxZoom().isInfinite())?18:fillLayer.getMaxZoom());
        lineLayer.minZoom((fillLayer.getMinZoom().isInfinite())?13:fillLayer.getMinZoom());
        return lineLayer;
    }


    public static LineLayer lineRenderLine(LineLayer lineLayerV) {
        LineLayer lineLayer = new LineLayer(lineLayerV.getLayerId() + "_render", lineLayerV.getSourceId());
        lineLayer.sourceLayer(lineLayerV.getSourceLayer())
                .lineColor(Color.TRANSPARENT)
                .lineWidth(3f)
                .lineOpacity(1f);
        if (lineLayerV.getFilter() != null) {
            lineLayer.filter(lineLayerV.getFilter());
        }
        lineLayer.maxZoom((lineLayerV.getMaxZoom().isInfinite())?18:lineLayerV.getMaxZoom());
        lineLayer.minZoom((lineLayerV.getMinZoom().isInfinite())?13:lineLayerV.getMinZoom());
        return lineLayer;
    }

    public static CircleLayer circleRenderCircle(CircleLayer circleLayerV) {
        CircleLayer circleLayer = new CircleLayer(circleLayerV.getLayerId() + "_render", circleLayerV.getSourceId());
        circleLayer.sourceLayer(circleLayerV.getSourceLayer())
                .circleRadius(4f).circleColor(Color.TRANSPARENT)
                .circleOpacity(1f);
        if (circleLayerV.getFilter() != null) {
            circleLayer.filter(circleLayerV.getFilter());
        }
        circleLayerV.maxZoom((circleLayerV.getMaxZoom().isInfinite())?18:circleLayerV.getMaxZoom());
        circleLayerV.minZoom((circleLayerV.getMinZoom().isInfinite())?13:circleLayerV.getMinZoom());
        return circleLayer;
    }
}
