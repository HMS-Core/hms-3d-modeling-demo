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
package com.huawei.hms.modeling3d.utils;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Build;
import android.text.format.Formatter;
import android.util.Base64;
import android.util.Log;
import android.view.View;

import com.huawei.hms.magicresource.util.Utils;
import com.huawei.hms.modeling3d.model.ConstantBean;
import com.huawei.hms.modeling3d.model.UserBean;
import com.huawei.hms.modeling3d.ui.activity.NewScanActivity;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.charset.Charset;

import static android.content.Context.MODE_PRIVATE;

public class BaseUtils {

    public static void saveUser(Context context, UserBean user) {
        if (user != null) {
            SharedPreferences sharedPreferences = context.getSharedPreferences(ConstantBean.PREFERENCE_USER_NAME, MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try {
                ObjectOutputStream oos = new ObjectOutputStream(baos);
                oos.writeObject(user);
                String temp = new String(Base64.encode(baos.toByteArray(), Base64.DEFAULT));
                editor.putString(ConstantBean.PREFERENCE_USER_KEY, temp);
                editor.apply();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static UserBean getUser(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(ConstantBean.PREFERENCE_USER_NAME, MODE_PRIVATE);
        String temp = sharedPreferences.getString(ConstantBean.PREFERENCE_USER_KEY, "");
        ByteArrayInputStream bais = new ByteArrayInputStream(Base64.decode(temp.getBytes(Charset.defaultCharset()), Base64.DEFAULT));
        UserBean user = null;
        try {
            ObjectInputStream ois = new ObjectInputStream(bais);
            user = (UserBean) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return user;
    }

    public static boolean getMemorySizeAndVersion(Context context) {
        return false;
    }

    public static boolean isHarmonyOs() {
        try {
            Class<?> buildExClass = Class.forName("com.huawei.system.BuildEx");
            Object osBrand = buildExClass.getMethod("getOsBrand").invoke(buildExClass);
            return "harmony".equalsIgnoreCase(osBrand.toString());
        } catch (Throwable x) {
            return false;
        }
    }


    public static Bitmap takeScreenShot(Activity act) {

        if (act == null || act.isFinishing()) {
            return null;

        }
        // Get the view of the current view
        View scrView = act.getWindow().getDecorView();

        scrView.setDrawingCacheEnabled(true);

        scrView.buildDrawingCache(true);
        // Get the status bar height
        Rect statuBarRect = new Rect();

        scrView.getWindowVisibleDisplayFrame(statuBarRect);

        int statusBarHeight = statuBarRect.top;

        int width = act.getWindowManager().getDefaultDisplay().getWidth();

        int height = act.getWindowManager().getDefaultDisplay().getHeight();

        Bitmap scrBmp = null;

        try {
            // screenshot with title bar removed
            scrBmp = Bitmap.createBitmap(scrView.getDrawingCache(), 0, statusBarHeight,

                    width, height - statusBarHeight);

        } catch (IllegalArgumentException e) {
            Log.d("", "#### Rotating the screen causes the status bar to fail to be removed");

        }

        scrView.setDrawingCacheEnabled(false);

        scrView.destroyDrawingCache();

        return scrBmp;

    }

    public static void saveBitmap(Bitmap bitmap, Context context) {
        File filePic;
        try {
            filePic = new File("/sdcard/3dmodelingkit" + "/" + System.currentTimeMillis() + ".jpg");
            if (!filePic.exists()) {
                filePic.getParentFile().mkdirs();
                filePic.createNewFile();
            }
            FileOutputStream fos = new FileOutputStream(filePic);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 95, fos);
            fos.flush();
            fos.close();
            ToastUtil.showToast(context, "save successFul");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
