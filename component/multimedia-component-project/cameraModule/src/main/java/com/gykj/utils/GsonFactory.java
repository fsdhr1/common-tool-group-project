package com.gykj.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Created by zy on 2018/8/4.
 */

public final class GsonFactory {

    private static volatile GsonFactory singleton;

    private GsonFactory() {
    }

    public static GsonFactory getFactory() {
        if (singleton == null) {
            synchronized (GsonFactory.class) {
                if (singleton == null) {
                    singleton = new GsonFactory();
                }
            }
        }
        return singleton;
    }

    private Gson gson;

    public Gson getComGson() {
        if (gson != null) {
            return gson;
        }
        gson = new GsonBuilder().registerTypeAdapter(new TypeToken<TreeMap<String, Object>>() {
                }.getType(), new JsonDeserializer<TreeMap<String, Object>>() {
                    @Override
                    public TreeMap<String, Object> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                        TreeMap<String, Object> treeMap = new TreeMap<>();
                        JsonObject jsonObject = json.getAsJsonObject();
                        Set<Map.Entry<String, JsonElement>> entrySet = jsonObject.entrySet();
                        for (Map.Entry<String, JsonElement> entry : entrySet) {
                            treeMap.put(entry.getKey(), entry.getValue());
                        }
                        return treeMap;
                    }
                }
        ).setDateFormat("yyyy-MM-dd HH:mm:ss").create();
        gson.serializeNulls();
        return gson;
    }
}
