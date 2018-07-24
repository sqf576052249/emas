package com.taobao.demo;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.taobao.demo.config.EMASInfo;
import com.taobao.demo.config.PrivateCloudEmasConfig;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by jason on 18/4/10.
 */

public class Utils {

    private static final String EMAS_INFO_FILENAME = "aliyun-emas-services.json";
    private static final String EMAS_INFO_FILE = "EMAS";
    private static final String EMAS_INFO_KEY = "emas_info";
    private static final String EMAS_FILE_PATH = "/sdcard/Android/data/";
    private static final String TAG = "Utils";

    public static EMASInfo parseEmasInfo(Context context){
        EMASInfo einfo = null;
        String emasconfig = null;
        try {
            byte[] data = readFile(EMAS_FILE_PATH + EMAS_INFO_FILENAME);
            if (data != null) {
                emasconfig = new String(data, "utf-8");
            }

            if (TextUtils.isEmpty(emasconfig)) {
                SharedPreferences sp = context.getSharedPreferences(EMAS_INFO_FILE, Context.MODE_PRIVATE);
                emasconfig = sp.getString(EMAS_INFO_KEY, null);
            }

            if (TextUtils.isEmpty(emasconfig)) {
                emasconfig = getFromAssets(EMAS_INFO_FILENAME, context);
            }
            PrivateCloudEmasConfig config = JSON.parseObject(emasconfig, new TypeReference<PrivateCloudEmasConfig>(){});
            einfo = config.private_cloud_config;
            Log.i(TAG, "parseEmasInfo " + einfo.toString());
        } catch (Throwable t) {
            Log.e(TAG, "parseEmasInfo", t);
        }
        return einfo;
    }

    public static void setEmasInfo(Context context, PrivateCloudEmasConfig config) {
        try {
            EMASInfo einfo = config != null ? config.private_cloud_config : null;
            if (einfo != null) {
                Log.i(TAG, "setEmasInfo " + einfo.toString());
                String emasJson = getFromAssets(EMAS_INFO_FILENAME, context);
                EMASInfo emas = JSON.parseObject(emasJson, new TypeReference<EMASInfo>(){});
                if (!TextUtils.isEmpty(einfo.AppKey)) {
                    Log.i(TAG, "setAppKey:" + einfo.AppKey);
                    emas.AppKey = einfo.AppKey;
                }
                if (!TextUtils.isEmpty(einfo.AppSecret)) {
                    Log.i(TAG, "setAppSecret:" + einfo.AppSecret);
                    emas.AppSecret = einfo.AppSecret;
                }
                if (einfo.Network.IPStrategy != null && einfo.Network.IPStrategy.size() > 0) {
                    Log.i(TAG, "setIPStrategy:" + einfo.Network.IPStrategy);
                    emas.Network.IPStrategy = einfo.Network.IPStrategy;
                }
                if (!TextUtils.isEmpty(einfo.ACCS.Domain)) {
                    Log.i(TAG, "setACCSDoman:" + einfo.ACCS.Domain);
                    emas.ACCS.Domain = einfo.ACCS.Domain;
                }
                if (!TextUtils.isEmpty(einfo.HA.UniversalHost)) {
                    Log.i(TAG, "setHAUniversalHost:" + einfo.HA.UniversalHost);
                    emas.HA.UniversalHost = einfo.HA.UniversalHost;
                }
                if (!TextUtils.isEmpty(einfo.MTOP.Domain)) {
                    Log.i(TAG, "setMTOPDoman:" + einfo.MTOP.Domain);
                    emas.MTOP.Domain = einfo.MTOP.Domain;
                }
                if (!TextUtils.isEmpty(einfo.ZCache.URL)) {
                    Log.i(TAG, "setCacheURL:" + einfo.ZCache.URL);
                    emas.ZCache.URL = einfo.ZCache.URL;
                }
                if (!TextUtils.isEmpty(einfo.HA.OSSBucketName)) {
                    Log.i(TAG, "setHAOSSBucketName:" + einfo.HA.OSSBucketName);
                    emas.HA.OSSBucketName = einfo.HA.OSSBucketName;
                }
                if (!TextUtils.isEmpty(einfo.HA.RSAPublicKey)) {
                    Log.i(TAG, "setHARSAPublicKey:" + einfo.HA.RSAPublicKey);
                    emas.HA.RSAPublicKey = einfo.HA.RSAPublicKey;
                }
                if (!TextUtils.isEmpty(einfo.ChannelID)) {
                    Log.i(TAG, "setChannelID:" + einfo.ChannelID);
                    emas.ChannelID = einfo.ChannelID;
                }
                if (!TextUtils.isEmpty(einfo.StartActivity)) {
                    Log.i(TAG, "setStartActivity:" + einfo.StartActivity);
                    emas.StartActivity = einfo.StartActivity;
                }

                SharedPreferences sp = context.getSharedPreferences(EMAS_INFO_FILE, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor  = sp.edit();
                config.private_cloud_config = emas;
                String data = JSON.toJSONString(config);
                editor.putString(EMAS_INFO_KEY, data);
                editor.commit();

            }
        } catch (Throwable t) {
            Log.e(TAG, "parseEmasInfo", t);
        }
    }


    private static String getFromAssets(String fileName, Context context) {
        BufferedReader bufReader = null;
        try {
            InputStreamReader inputReader = new InputStreamReader(context.getResources().getAssets().open(fileName), "UTF-8");
            bufReader = new BufferedReader(inputReader);
            String line = "";
            String result = "";
            while ((line = bufReader.readLine()) != null)
                result += line;
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            if (bufReader != null) {
                try {
                    bufReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static byte[] readFile(String fileName) {
        File file = new File(fileName);
        return readFile(file);
    }

    public static byte[] readFile(File file) {
        if (!file.exists()) {
            return null;
        }

        if (!file.isFile()) {
            throw new RuntimeException(file + ": not a file");
        }

        if (!file.canRead()) {
            throw new RuntimeException(file + ": file not readable");
        }

        long longLength = file.length();
        int length = (int) longLength;
        if (length != longLength) {
            throw new RuntimeException(file + ": file too long");
        }

        byte[] result = new byte[length];

        try {
            FileInputStream in = new FileInputStream(file);
            int at = 0;
            while (length > 0) {
                int amt = in.read(result, at, length);
                if (amt == -1) {
                    throw new RuntimeException(file + ": unexpected EOF");
                }
                at += amt;
                length -= amt;
            }
            in.close();
        } catch (IOException ex) {
            throw new RuntimeException(file + ": trouble reading", ex);
        }

        return result;
    }
}
