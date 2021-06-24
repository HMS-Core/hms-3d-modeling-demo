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

package com.huawei.hms.modelingresource.materialdb;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;


/**
 * @Description: DatabaseUtils
 * @Since: 2021-03-01
 */
public class DatabaseMaterialAppUtils {
    private static final String TAG = DatabaseMaterialAppUtils.class.getSimpleName();

    private static final Object LOCK = new Object();

    private static final String TABLE = "sqlite_master";

    private static final String NAME = "name=?";

    private static DatabaseMaterialAppHelper databaseHelper;

    private static SQLiteDatabase database;

    public static void initDatabase(Context context) {
        synchronized (LOCK) {
            if (context == null) {
                return;
            }

            if ((database == null) || !database.isOpen()) {
                databaseHelper = new DatabaseMaterialAppHelper(context);
                database = databaseHelper.getWritableDatabase();
            }
        }
    }

    public static SQLiteDatabase getDatabase() {
        return database;
    }

    public static boolean isTableExist(String tableName) {
        boolean isExisted = false;
        if (database == null) {
            return isExisted;
        }
        Cursor cursor = null;
        try {
            cursor = database.query(TABLE, null, NAME, new String[]{tableName}, null, null, null);
            if (cursor != null && cursor.getCount() > 0) {
                isExisted = true;
            }
            return isExisted;
        } finally {
            closeCursor(cursor);
        }
    }

    public static void closeCursor(Cursor cursor) {
        if (cursor != null) {
            cursor.close();
        } else {
            Log.e(TAG, "close null cursor!");
        }
    }

    public static void closeDatabase() {
        Log.i(TAG, "closeDatabase");
        synchronized (LOCK) {
            if (database == null) {
                return;
            }
            database.close();
            try {
                if (databaseHelper != null) {
                    databaseHelper.close();
                }
            } catch (IllegalStateException e) {
                Log.e(TAG, "close database failed");
            }
            database = null;
        }
    }
}
