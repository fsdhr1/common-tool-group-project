package com.grandtech.mapframe.core.util;

import java.util.List;

public final class ListUtil {
    private ListUtil() {
    }
    public static boolean listHasItem(List list, Object obj) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).hashCode() == obj.hashCode()) {
                return true;
            }
        }
        return false;
    }
}
