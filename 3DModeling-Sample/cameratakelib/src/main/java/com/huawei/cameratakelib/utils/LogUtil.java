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

import android.text.TextUtils;
import android.util.Log;

public class LogUtil {

    public static final String TAG_PREFIX = "";
    public static final boolean SHOW_V = true;
    public static final boolean SHOW_D = true;
    public static final boolean SHOW_I = true;
    public static final boolean SHOW_W = true;
    public static final boolean SHOW_E = true;

    /**
     * Obtain the tag (class where the tag is located. Method (L: Row)
     *
     * @return String
     */
    private static String generateTag() {
        StackTraceElement stackTraceElement = Thread.currentThread().getStackTrace()[4];
        String callerClazzName = stackTraceElement.getClassName();
        callerClazzName = callerClazzName.substring(callerClazzName.lastIndexOf(".") + 1);
        String tag = "%s.%s(L:%d)";
        tag = String.format(tag, new Object[]{callerClazzName, stackTraceElement.getMethodName(), Integer.valueOf(stackTraceElement.getLineNumber())});
        // Set a prefix for a tag.
        tag = TextUtils.isEmpty(TAG_PREFIX) ? tag : TAG_PREFIX + ":" + tag;
        return tag;
    }

    public static void v(String msg) {
        if (SHOW_V) {
            String tag = generateTag();
            Log.v(tag, msg);
        }
    }

    public static void v(String msg, Throwable tr) {
        if (SHOW_V) {
            String tag = generateTag();
            Log.v(tag, msg, tr);
        }
    }

    public static void d(String msg) {
        if (SHOW_D) {
            String tag = generateTag();
            Log.d(tag, msg);
        }
    }

    public static void d(String msg, Throwable tr) {
        if (SHOW_D) {
            String tag = generateTag();
            Log.d(tag, msg, tr);
        }
    }

    public static void i(String msg) {
        if (SHOW_I) {
            String tag = generateTag();
            Log.i(tag, msg);
        }
    }

    public static void i(String msg, Throwable tr) {
        if (SHOW_I) {
            String tag = generateTag();
            Log.i(tag, msg, tr);
        }
    }

    public static void w(String msg) {
        if (SHOW_W) {
            String tag = generateTag();
            Log.w(tag, msg);
        }
    }

    public static void w(String msg, Throwable tr) {
        if (SHOW_W) {
            String tag = generateTag();
            Log.w(tag, msg, tr);
        }
    }

    public static void e(String msg) {
        if (SHOW_E) {
            String tag = generateTag();
            Log.e(tag, msg);

        }
    }

    public static void e(String msg, Throwable tr) {
        if (SHOW_E) {
            String tag = generateTag();
            Log.e(tag, msg, tr);
        }
    }
}
