/**
 * Copyright 2021. Huawei Technologies Co., Ltd. All rights reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * https://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.huawei.hms.magicresource.util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import androidx.annotation.RequiresApi;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class Utils {

    public static int dip2px(Context context, float dpValue) {
        float scale = getScreenDendity(context);
        return (int) ((dpValue * scale) + 0.5f);
    }

    public static float getScreenDendity(Context context) {
        return context.getResources().getDisplayMetrics().density;
    }

    public static String systemCurrentToData(long currentTimeMillis) {
        Date date = new Date(currentTimeMillis);
        String str = "yyyMMddHH-mmss";
        SimpleDateFormat sdf = new SimpleDateFormat(str);
        return sdf.format(date);
    }

    /**
     * Whether it is huawei
     */
    public static boolean isHUAWEI() {
        return android.os.Build.MANUFACTURER.equals("HUAWEI");
    }

    public static String getEMUI() {
        Class<?> classType = null;
        String buildVersion = null;
        try {
            classType = Class.forName("android.os.SystemProperties");
            Method getMethod = classType.getDeclaredMethod("get", new Class<?>[]{String.class});
            buildVersion = (String) getMethod.invoke(classType, new Object[]{"ro.build.version.emui"});
        } catch (Exception e) {
            e.printStackTrace();
        }
        return buildVersion;
    }

    public static boolean haveHuaWeiAccount(Context context){
        PackageManager packageManager = context.getPackageManager();
        List<PackageInfo> pinfo = packageManager.getInstalledPackages(0);
        for (PackageInfo pkif : pinfo) {
            String pn = pkif.packageName;
            if ("com.huawei.hwid".equals(pn)||"com.huawei.hms".equals(pn)) {
                return true;
            }
        }
        return false;
    }


    public static boolean getCurrentEmotionUIAbove9() {
        String emUi = getEMUI();
        if (emUi != null) {
            if (emUi.contains("EmotionUI_")) {
                int code = Integer.parseInt(emUi.substring(10).replace(".", ""));
                if (code >= 1000) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean deleteDirectory(String dir) {
        if (!dir.endsWith(File.separator))
            dir = dir + File.separator;
        File dirFile = new File(dir);
        if ((!dirFile.exists()) || (!dirFile.isDirectory())) {
            return false;
        }
        boolean flag = true;
        File[] files = dirFile.listFiles();
        for (int i = 0; i < files.length; i++) {
            if (files[i].isFile()) {
                flag = deleteFile(files[i].getAbsolutePath());
                if (!flag)
                    break;
            } else if (files[i].isDirectory()) {
                flag = deleteDirectory(files[i]
                        .getAbsolutePath());
                if (!flag)
                    break;
            }
        }
        if (!flag) {
            return false;
        }
        if (dirFile.delete()) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean deleteFile(String fileName) {
        File file = new File(fileName);
        if (file.exists() && file.isFile()) {
            if (file.delete()) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

}


