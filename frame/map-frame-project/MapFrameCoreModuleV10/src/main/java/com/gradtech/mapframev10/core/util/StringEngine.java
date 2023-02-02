package com.gradtech.mapframev10.core.util;

import android.util.Log;


import com.gradtech.mapframev10.core.rules.Rules;

import java.util.UUID;

/**
 * @ClassName StringEngine
 * @Description TODO
 * @Author: fs
 * @Date: 2021/4/20 14:19
 * @Version 2.0
 */
public class StringEngine implements Rules {


    /**
     * 自动生成32位的UUid，对应数据库的主键id进行插入用。
     *
     * @return
     */
    public static String get32UUID() {
        return UUID.randomUUID().toString().replace("-", "").toUpperCase();
    }

    /**
     * 提取最小的json结构体
     *
     * @param jsonString
     * @param keyWord
     * @return
     */
    public static String getMinJsonStructure(String jsonString, String keyWord) {
        try {
            int indexStart = jsonString.indexOf(keyWord);
            indexStart = indexStart + keyWord.length();
            String leftString = jsonString.substring(0, jsonString.indexOf(keyWord));
            int keyWordLeft = leftString.lastIndexOf("{");
            String rightString = jsonString.substring(indexStart);
            int rightLen = rightString.length();
            int keyWordRight = 0;
            int sum = 1;
            char c;
            for (int i = 0; i < rightLen; i++) {
                c = rightString.charAt(i);
                if (c == '{') {
                    sum++;
                }
                if (c == '}') {
                    sum--;
                }
                if (sum == 0) {
                    keyWordRight = i;
                    break;
                }
            }
            String resLeft = jsonString.substring(keyWordLeft, indexStart);
            String resRight = rightString.substring(0, keyWordRight + 1);
            String res = resLeft + resRight;
            return res;
        } catch (Exception e) {
            e.printStackTrace();
            Log.i("gmbgl", "getMinJsonStructure解析失敗" + keyWord+ jsonString );
            return null;
        }
    }


    /**
     * 提取最小的json数组
     *
     * @param jsonString
     * @param keyWord
     * @return
     */
    public static String getMinJsonArray(String jsonString, String keyWord) {
        try {
            int indexStart = jsonString.indexOf(keyWord);
            indexStart = indexStart + keyWord.length();
            String rightString = jsonString.substring(indexStart);
            int rightLen = rightString.length();
            int keyWordRight = 0;
            int keyWordLeft = rightString.indexOf("[");
            int sum = 1;
            char c;
            for (int i = 0; i < rightLen; i++) {
                c = rightString.charAt(i);
                if (c == '[') {
                    sum++;
                }
                if (c == ']') {
                    sum--;
                }
                if (sum == 1 && c == ']') {
                    keyWordRight = i + 1;
                    break;
                }
            }
            String res = rightString.substring(keyWordLeft, keyWordRight);
            return res;
        } catch (Exception e) {
            e.printStackTrace();
            Log.i("gmbgl", "getMinJsonArray解析失敗" + keyWord+jsonString  );
            return null;
        }
    }
}
