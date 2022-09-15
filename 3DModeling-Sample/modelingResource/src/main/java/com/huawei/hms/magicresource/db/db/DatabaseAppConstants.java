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
package com.huawei.hms.magicresource.db;

import android.provider.BaseColumns;

/**
 * @ignore Database Table
 * @since 2020-03-12
 */
public class DatabaseAppConstants {
    public static final class Modeling3dReconstruct implements BaseColumns {
        public static final String TABLE_NAME = "objreconstruct";

        public static final String COLUMN_ID = "id";

        public static final String COLUMN_SAVEPATH = "fileSavePath";

        public static final String COLUMN_UPLOADPATH = "fileUploadPath";

        public static final String COLUMN_TASKID = "taskId";

        public static final String COLUMN_ISDOWNLOAD = "isDownload";

        public static final String COLUMN_STATES = "status";

        public static final String COLUMN_CREATE_TIME = "create_time";

        public static final String MODEL_TYPE = "modelType";

        public static final String SQL_CREATE_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" + COLUMN_ID
            + " INTEGER PRIMARY KEY AUTOINCREMENT, " + COLUMN_TASKID + " TEXT ," +
                COLUMN_SAVEPATH + " TEXT ," + COLUMN_UPLOADPATH + " TEXT ," + COLUMN_ISDOWNLOAD + " INTEGER ," +
                COLUMN_STATES + " INTEGER ," + COLUMN_CREATE_TIME + " INTEGER ," + MODEL_TYPE+" TEXT" +")";
    }
}
