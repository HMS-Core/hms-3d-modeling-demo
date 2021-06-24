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

package com.huawei.cameratakelib.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.os.StatFs;

import com.huawei.hms.modelingresource.util.Constants;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Objects;

public class FileUtil {
    /**
     * Calculate the remaining size of the SDcard.
     *
     * @return MB
     */
    public static long getAvailableSize() {
        StatFs statFs;
        File file = Environment.getExternalStorageDirectory();
        statFs = new StatFs(file.getPath());
        long blockSize = statFs.getBlockSize();
        long blockavailable = statFs.getAvailableBlocks();
        long blockavailableTotal = blockSize * blockavailable / 1024 / 1024;
        return blockavailableTotal;
    }

    /**
     * Save the bitmap to the local host.
     *
     * @param context Context
     * @param bitmap  bitmap
     * @param createTime createTime
     * @param index index
     * @return file
     */
    public static File saveBitmap(Context context, Bitmap bitmap, String createTime, Integer index) {
        String savePath = new Constants(context).getCaptureImageFile() + createTime + "/";
        File filePic;
        FileOutputStream fos = null ;
        try {
            filePic = new File(savePath + index + ".jpg");
            if (!filePic.exists()) {
                if (!Objects.requireNonNull(filePic.getParentFile()).exists()) {
                    boolean isParentMk = filePic.getParentFile().mkdirs();
                    if (isParentMk){
                        LogUtil.d("File created successfully");
                    }
                }
                boolean result = filePic.createNewFile();
                if (result){
                    LogUtil.d("File created successfully");
                }
            }
            fos = new FileOutputStream(filePic);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, fos);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            if (fos!=null){
                try {
                    fos.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
            LogUtil.d("saveBitmap: return");
            return null;
        } finally {
            if (fos!=null){
                try {
                    fos.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
        LogUtil.d("saveBitmap: " + filePic.getAbsolutePath());
        return filePic;
    }

    /**
     * Compressed Picture
     *
     * @param image image
     * @return Bitmap
     */
    public static Bitmap compressImage(Bitmap image) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());
        Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);
        return bitmap;
    }

    public static boolean deleteDirectory(String dir) {
        if (!dir.endsWith(File.separator)) {
            dir = dir + File.separator;
        }
        File dirFile = new File(dir);
        if ((!dirFile.exists()) || (!dirFile.isDirectory())) {
            return false;
        }
        boolean flag = true;
        File[] files = dirFile.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile()) {
                    flag = deleteFile(file.getAbsolutePath());
                    if (!flag) {
                        break;
                    }
                } else if (file.isDirectory()) {
                    flag = deleteDirectory(file.getAbsolutePath());
                    if (!flag) {
                        break;
                    }
                }
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
