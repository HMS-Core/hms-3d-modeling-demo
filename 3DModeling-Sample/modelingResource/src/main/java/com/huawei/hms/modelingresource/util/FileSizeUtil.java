/**
 * Copyright 2021. Huawei Technologies Co., Ltd. All rights reserved.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.huawei.hms.modelingresource.util;

import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import java.io.File;
import java.io.FileInputStream;
import java.text.DecimalFormat;

public class FileSizeUtil {
    public static final String TAG = "FileSizeUtil";
    public static final int SIZETYPE_B = 1; // Obtains the double value of the file size in the unit of B.
    public static final int SIZETYPE_KB = 2; // Obtains the double value of the file size in KB.
    public static final int SIZETYPE_MB = 3; // Obtains the double value of the file size in MB.
    public static final int SIZETYPE_GB = 4; // Obtains the double value of the file size in GB.

    public static double getFileOrFilesSize(String filePath, int sizeType) {
        File file = new File(filePath);
        long blockSize = 0;
        try {
            if (file.isDirectory()) {
                blockSize = getFileSizes(file);
            } else {
                blockSize = getFileSize(file);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "Obtaining failed.");
        }
        return formetFileSize(blockSize, sizeType);
    }

    private static long getFileSizes(File f) throws Exception {
        long size = 0;
        File[] flist = f.listFiles();
        if (flist != null) {
            for (File file : flist) {
                if (file.isDirectory()) {
                    size = size + getFileSizes(file);
                } else {
                    size = size + getFileSize(file);
                }
            }
        }
        return size;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private static long getFileSize(File file) throws Exception {
        long size = 0;
        if (file.exists()) {
            try(FileInputStream fis = new FileInputStream(file)) {
                size = fis.available();
            }
        } else {
            Log.e("Obtain the file size.", "The file does not exist.");
        }
        return size;
    }

    private static double formetFileSize(long fileS, int sizeType) {
        DecimalFormat df = new DecimalFormat("#.00");
        double fileSizeLong = 0;
        switch (sizeType) {
            case SIZETYPE_B:
                fileSizeLong = Double.valueOf(df.format((double) fileS));
                break;
            case SIZETYPE_KB:
                fileSizeLong = Double.valueOf(df.format((double) fileS / 1024));
                break;
            case SIZETYPE_MB:
                fileSizeLong = Double.valueOf(df.format((double) fileS / 1048576));
                break;
            case SIZETYPE_GB:
                fileSizeLong = Double.valueOf(df.format((double) fileS / 1073741824));
                break;
            default:
                break;
        }
        return fileSizeLong;
    }
}