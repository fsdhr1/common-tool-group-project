package com.gykj.signature;

import java.io.File;
import java.io.FileOutputStream;

/**
 * Created by jyh on 2021-02-24
 */
public class BitmapUtil {

    /**
     * 成功返回相对路径
     *
     * @param bitmapArray
     * @param path        绝对路径
     * @return
     */
    public static String byte2File(byte[] bitmapArray, String path) {
        try {
            if (bitmapArray != null) {
                /**
                 * 创建父路径
                 */
                int lastIndex = path.lastIndexOf("/");
                String pathPath = path.substring(0, lastIndex);
                File f = new File(pathPath);
                if (!f.exists()) {
                    f.mkdirs();
                }
                File file = new File(path);
                if (file.exists()) {
                    file.delete();
                }
                File file1 = new File(path);
                FileOutputStream outputStream = new FileOutputStream(file1);
                outputStream.write(bitmapArray, 0, bitmapArray.length);
                outputStream.close();
                return path;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
