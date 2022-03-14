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

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Environment;

public class Constants {

    public static int RGB_MODEL = 0;
    public static int SLAM_MODEL = 10;

    private String rootPath;

    public Constants(Context mContext) {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            rootPath = mContext.getExternalFilesDir(null) + "/3dModeling/";
        } else {
            rootPath = mContext.getFilesDir().getPath() + "/3dModeling/";
        }
    }

    public String getRgbDownFile() {
        return rootPath + "model/download/";
    }

    public String getMaterialDownFile() {
        return rootPath + "material/download/";
    }

    public String getCaptureImageFile() {
        return rootPath + "picture/3dModeling/";
    }

    public String getCaptureMaterialImageFile() {
        return rootPath + "picture/material/";
    }


}
