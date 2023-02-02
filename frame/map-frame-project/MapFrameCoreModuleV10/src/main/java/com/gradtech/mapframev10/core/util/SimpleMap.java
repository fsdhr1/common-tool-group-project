package com.gradtech.mapframev10.core.util;

import com.google.gson.Gson;

import java.io.Serializable;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zy on 2018/5/29.
 */

public class SimpleMap<K, V> extends LinkedHashMap<K, V> implements Serializable {


    @Override
    public V put(K key, V value) {
        return super.put(key, value);
    }

    public <T> T obtainT(K key) {
        return super.get(key) == null ? null : (T) super.get(key);
    }

    public SimpleMap<K, V> push(K key, V value) {
        super.put(key, value);
        return this;
    }


    public SimpleMap<K, V> pushAll(Map<? extends K, ? extends V> m) {
        if (m != null) {
            super.putAll(m);
        }
        return this;
    }

    /*public SimpleMap<K, V> cloneAll(Map<? extends K, ? extends V> m) {
        for (K k : m.keySet()) {
            super.put(k, m.get(k));
        }
        return this;
    }*/

    public K obtainFirstKByV(V value) {
        if (this != null && this.size() > 0) {
            for (K k : this.keySet()) {
                V v = this.get(k);
                if (v != null && value != null && v.toString().equals(value.toString())) {
                    return k;
                }
            }
        }
        return null;
    }

    public K obtainPositionK(int position) {
        if (this != null && this.size() > 0) {
            int i = 0;
            for (K k : this.keySet()) {
                if (i == position) {
                    return k;
                }
            }
        }
        return null;
    }

    public V obtainPositionV(int position) {
        if (this != null && this.size() > 0) {
            int i = 0;
            for (K k : this.keySet()) {
                if (i == position) {
                    return this.get(k);
                }
            }
        }
        return null;
    }

    public List<K> obtainAllK() {
        if (this != null && this.size() > 0) {
            List<K> list = new ArrayList<>();
            for (K k : this.keySet()) {
                list.add(k);
            }
            return list;
        }
        return null;
    }

    public List<V> obtainAllV() {
        if (this != null && this.size() > 0) {
            List<V> list = new ArrayList<>();
            for (K k : this.keySet()) {
                list.add(this.get(k));
            }
            return list;
        }
        return null;
    }

    public Map toMap() {
        return this;
    }

    public String toJson() {
        Gson gson = GsonFactory.getFactory().getComGson();
        return gson.toJson(this);
    }

    public static SimpleMap fromJson(String json) {
        if (json == null) {
            return null;
        }
        Gson gson = GsonFactory.getFactory().getComGson();
        return gson.fromJson(json, SimpleMap.class);
    }

    public static SimpleMap fromJson(Object json) {
        if (json == null) {
            return null;
        }
        Gson gson = GsonFactory.getFactory().getComGson();
        return gson.fromJson(json.toString(), SimpleMap.class);
    }

    public static <T> T convert(Object obj, Type type) {
        if (obj instanceof List) {
            Gson gson = GsonFactory.getFactory().getComGson();
            String json = gson.toJson(obj);
            return (T) gson.fromJson(json, type);
        } else {
            if (obj == null) {
                return null;
            }
            Gson gson = GsonFactory.getFactory().getComGson();
            String json = gson.toJson(obj);
            return (T) gson.fromJson(json, type);
        }
    }
}
