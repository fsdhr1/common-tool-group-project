package com.grandtech.mapframe.core.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.mapbox.geojson.Point;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * @author fs
 */
public class CustomPointSerializer implements JsonSerializer<Point> {

    int precision = 7;

    public CustomPointSerializer() {
    }

    public CustomPointSerializer(int precision) {
        this.precision = precision;
    }

    @Override
    public JsonElement serialize(Point src, Type typeOfSrc, JsonSerializationContext context) {
        JsonArray rawCoordinates = new JsonArray();
        BigDecimal lat = BigDecimal.valueOf(src.latitude());
        String latString = lat.setScale(precision, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString();
        BigDecimal lon = BigDecimal.valueOf(src.longitude());
        String lonString = lon.setScale(precision, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString();
        rawCoordinates.add(new JsonPrimitive(Double.valueOf(lonString)));
        rawCoordinates.add(new JsonPrimitive(Double.valueOf(latString)));
        if (src.hasAltitude()) {
            rawCoordinates.add(new JsonPrimitive(src.altitude()));
        }

        return rawCoordinates;
    }
}
