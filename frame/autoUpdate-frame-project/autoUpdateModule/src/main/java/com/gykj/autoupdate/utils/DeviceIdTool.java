package com.gykj.autoupdate.utils;

import android.content.Context;
import android.os.Environment;

import com.blankj.utilcode.util.AppUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.UUID;

/**
 * @author zyp
 * 11.5.20
 */
public class DeviceIdTool {
    private static final String DEVICEID_FILE = "_";
    private static String mSopHixTextPath;
    private static String mSopHixFileName = AppUtils.getAppVersionName();

    public static String getDeviceId() {
        String offlinePath = Environment.getExternalStorageDirectory().getAbsolutePath().toString() + "/.com.grendtech.offlinestand/";
        File offlinePathFile = new File(offlinePath);
        if (!offlinePathFile.exists() || !offlinePathFile.isDirectory()) {
            offlinePathFile.mkdir();
        }
        File deviceIdFile = new File(offlinePath + DEVICEID_FILE);
        try {
            if (!deviceIdFile.exists()) {
                deviceIdFile.createNewFile();
                FileWriter fileWriter = new FileWriter(deviceIdFile);
                fileWriter.write(UUID.randomUUID().toString());
                fileWriter.close();
            }
            FileReader fileReader = new FileReader(deviceIdFile);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String uuid = bufferedReader.readLine();
            bufferedReader.close();
            fileReader.close();
            return uuid;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static int getSopHixNumber(Context mContext) {
        mSopHixTextPath = mContext.getExternalCacheDir().getPath() + "/sopHixNum/";
        File offlinePathFile = new File(mSopHixTextPath);
        if (!offlinePathFile.exists() || !offlinePathFile.isDirectory()) {
            offlinePathFile.mkdirs();
        }
        File deviceIdFile = new File(mSopHixTextPath + mSopHixFileName);
        try {
            if (!deviceIdFile.exists()) {
                deviceIdFile.createNewFile();
                FileWriter fileWriter = new FileWriter(deviceIdFile);
                fileWriter.write("0");
                fileWriter.close();
                return 0;
            }
            FileReader fileReader = new FileReader(deviceIdFile);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String hotFixNum = bufferedReader.readLine();
            bufferedReader.close();
            fileReader.close();
            return Integer.parseInt(hotFixNum);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public static void toImproveSopHixNum(Context mContext, String number) {
        mSopHixTextPath = mContext.getExternalCacheDir().getPath() + "/sopHixNum/";
        File deviceIdFile = new File(mSopHixTextPath + mSopHixFileName);
        try {
            FileWriter fileWriter = new FileWriter(deviceIdFile);
            fileWriter.write(number);
            fileWriter.close();
        } catch (IOException mE) {
            mE.printStackTrace();
        }
    }
}
