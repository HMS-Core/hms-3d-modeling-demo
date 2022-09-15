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
package com.huawei.hms.magicresource.materialdb;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;

/**
 * @ignore Task tools
 * @since 2020-03-12
 */
public class TaskInfoMaterialAppDbUtils {
    private static final String TAG = TaskInfoMaterialAppDbUtils.class.getSimpleName();

    private static final String NULL_STR = "null";

    private static final String SELECT_EQUAL = "=?";

    private static final String SELECT_SMALL_THAN = "<=";

    private TaskInfoMaterialAppDbUtils() {
    }


    public static long deleteByTaskId(String taskId) {
        long result = -1;
        if (!isValiable(taskId)) {
            return result;
        }
        String whereClause = DatabaseMaterialAppConstants.Modeling3dReconstruct.COLUMN_TASKID + SELECT_EQUAL;
        String[] whereArgs = new String[] {taskId};
        result =
                DatabaseMaterialAppUtils.getDatabase().delete(DatabaseMaterialAppConstants.Modeling3dReconstruct.TABLE_NAME, whereClause, whereArgs);
        return result;
    }

    public static long deleteByUploadFilePath(String filePath) {
        long result = -1;
        if (!isValiable(filePath)) {
            return result;
        }
        String whereClause = DatabaseMaterialAppConstants.Modeling3dReconstruct.COLUMN_UPLOADPATH + SELECT_EQUAL;
        String[] whereArgs = new String[] {filePath};
        result =
                DatabaseMaterialAppUtils.getDatabase().delete(DatabaseMaterialAppConstants.Modeling3dReconstruct.TABLE_NAME, whereClause, whereArgs);
        return result;
    }

    public static long insert(TaskInfoMaterialAppDb spec) {
        long result = -1;
        if (!isValiable(spec)) {
            return result;
        }
        ContentValues values = getValues(spec);

        result = DatabaseMaterialAppUtils.getDatabase()
                .insertWithOnConflict(DatabaseMaterialAppConstants.Modeling3dReconstruct.TABLE_NAME, null, values,
                        SQLiteDatabase.CONFLICT_IGNORE);
        return result;
    }

    public static long updateTaskIdAndStatusByPath(String path, String taskId, int status) {
        long result = -1;
        boolean isExisted = checkExistedAndUpdateTaskIdAndStatus(path, taskId, status);
        if (!isExisted) {
            result = 1;
        }
        return result;
    }

    public static long updatePathByTaskId(String taskId, String path) {
        long result = -1;
        boolean isExisted = checkExistedAndUpdatePath(taskId, path);
        if (!isExisted) {
            result = 1;
        }
        return result;
    }

    public static long updateStatusByTaskId(String taskId, int status) {
        long result = -1;
        boolean isExisted = checkExistedAndUpdateStatus(taskId, status);
        if (!isExisted) {
            result = 1;
        }
        return result;
    }

    public static long updateDownloadByTaskId(String taskId, int download) {
        long result = -1;
        boolean isExisted = checkExistedAndUpdateDownload(taskId, download);
        if (!isExisted) {
            result = 1;
        }
        return result;
    }

    public static TaskInfoMaterialAppDb getTasksByTaskId(String taskId) {
        if (!isValiable(taskId)) {
            return null;
        }
        String selection = DatabaseMaterialAppConstants.Modeling3dReconstruct.COLUMN_TASKID + SELECT_EQUAL;
        String[] selectionArgs = new String[]{taskId};
        Cursor cursor = null;
        try {
            cursor = DatabaseMaterialAppUtils.getDatabase()
                    .query(DatabaseMaterialAppConstants.Modeling3dReconstruct.TABLE_NAME, null, selection, selectionArgs, null, null,
                            null);
            if ((cursor == null) || (cursor.getCount() <= 0)) {
                return null;
            }
            while (cursor != null && cursor.moveToNext()) {
                TaskInfoMaterialAppDb info = parseFromCursor(cursor);
                return info;
            }
        } finally {
            DatabaseMaterialAppUtils.closeCursor(cursor);
        }
        return null;
    }


    public static long update(TaskInfoMaterialAppDb spec) {
        long result = -1;
        if (!isValiable(spec)) {
            return result;
        }
        boolean isExisted = isExisted(spec.getTaskId());
        if (!isExisted) {
            result = insert(spec);
        } else {
            ContentValues values = getValues(spec);
            String whereClause = DatabaseMaterialAppConstants.Modeling3dReconstruct.COLUMN_TASKID + SELECT_EQUAL;
            String[] whereClauseArgs = new String[]{spec.getTaskId()};
            result = DatabaseMaterialAppUtils.getDatabase()
                    .updateWithOnConflict(DatabaseMaterialAppConstants.Modeling3dReconstruct.TABLE_NAME, values, whereClause,
                            whereClauseArgs, SQLiteDatabase.CONFLICT_IGNORE);
        }
        return result;
    }

    public static long pathUpdate(TaskInfoMaterialAppDb spec) {
        long result = -1;
        if (!isValiable(spec)) {
            return result;
        }
        boolean isExisted = isExistedPath(spec.getFileUploadPath());
        if (!isExisted) {
            result = insert(spec);
        } else {
            ContentValues values = getValues(spec);
            String whereClause = DatabaseMaterialAppConstants.Modeling3dReconstruct.COLUMN_UPLOADPATH + SELECT_EQUAL;
            String[] whereClauseArgs = new String[]{spec.getFileUploadPath()};
            result = DatabaseMaterialAppUtils.getDatabase()
                    .updateWithOnConflict(DatabaseMaterialAppConstants.Modeling3dReconstruct.TABLE_NAME, values, whereClause,
                            whereClauseArgs, SQLiteDatabase.CONFLICT_IGNORE);
        }
        return result;
    }

    private static boolean checkExistedAndUpdatePath(String taskNum, String path) {
        if (!isValiable(taskNum)) {
            return false;
        }
        String selection = DatabaseMaterialAppConstants.Modeling3dReconstruct.COLUMN_TASKID + SELECT_EQUAL;
        String[] selectionArgs = new String[]{taskNum};
        Cursor cursor = null;
        try {
            cursor = DatabaseMaterialAppUtils.getDatabase()
                    .query(DatabaseMaterialAppConstants.Modeling3dReconstruct.TABLE_NAME, null, selection, selectionArgs, null, null,
                            null);
            if ((cursor == null) || (cursor.getCount() <= 0)) {
                return false;
            }
            while (cursor != null && cursor.moveToNext()) {
                TaskInfoMaterialAppDb info = parseFromCursor(cursor);
                if (info != null) {
                    info.setFileSavePath(path);
                    update(info);
                }
            }
            return true;
        } finally {
            DatabaseMaterialAppUtils.closeCursor(cursor);
        }
    }

    private static boolean checkExistedAndUpdateTaskIdAndStatus(String path, String taskNum, int status) {
        if (!isValiable(taskNum)) {
            return false;
        }
        String selection = DatabaseMaterialAppConstants.Modeling3dReconstruct.COLUMN_UPLOADPATH + SELECT_EQUAL;
        String[] selectionArgs = new String[]{path};
        Cursor cursor = null;
        try {
            cursor = DatabaseMaterialAppUtils.getDatabase()
                    .query(DatabaseMaterialAppConstants.Modeling3dReconstruct.TABLE_NAME, null, selection, selectionArgs, null, null,
                            null);
            if ((cursor == null) || (cursor.getCount() <= 0)) {
                return false;
            }
            while (cursor != null && cursor.moveToNext()) {
                TaskInfoMaterialAppDb info = parseFromCursor(cursor);
                if (info != null) {
                    info.setTaskId(taskNum);
                    info.setStatus(status);
                    pathUpdate(info);
                }
            }
            return true;
        } finally {
            DatabaseMaterialAppUtils.closeCursor(cursor);
        }
    }

    private static boolean checkExistedAndUpdateDownload(String taskNum, int download) {
        if (!isValiable(taskNum)) {
            return false;
        }
        String selection = DatabaseMaterialAppConstants.Modeling3dReconstruct.COLUMN_TASKID + SELECT_EQUAL;
        String[] selectionArgs = new String[]{taskNum};
        Cursor cursor = null;
        try {
            cursor = DatabaseMaterialAppUtils.getDatabase()
                    .query(DatabaseMaterialAppConstants.Modeling3dReconstruct.TABLE_NAME, null, selection, selectionArgs, null, null,
                            null);
            if ((cursor == null) || (cursor.getCount() <= 0)) {
                return false;
            }
            while (cursor != null && cursor.moveToNext()) {
                TaskInfoMaterialAppDb info = parseFromCursor(cursor);
                if (info != null) {
                    info.setIsDownload(download);
                    update(info);
                }
            }
            return true;
        } finally {
            DatabaseMaterialAppUtils.closeCursor(cursor);
        }
    }

    private static boolean checkExistedAndUpdateStatus(String taskNum, int status) {
        if (!isValiable(taskNum)) {
            return false;
        }
        String selection = DatabaseMaterialAppConstants.Modeling3dReconstruct.COLUMN_TASKID + SELECT_EQUAL;
        String[] selectionArgs = new String[]{taskNum};
        Cursor cursor = null;
        try {
            cursor = DatabaseMaterialAppUtils.getDatabase()
                    .query(DatabaseMaterialAppConstants.Modeling3dReconstruct.TABLE_NAME, null, selection, selectionArgs, null, null,
                            null);
            if ((cursor == null) || (cursor.getCount() <= 0)) {
                return false;
            }
            while (cursor != null && cursor.moveToNext()) {
                TaskInfoMaterialAppDb info = parseFromCursor(cursor);
                if (info != null) {
                    info.setStatus(status);
                    update(info);
                }
            }
            return true;
        } finally {
            DatabaseMaterialAppUtils.closeCursor(cursor);
        }
    }

    private static boolean isExisted(String taskNum) {
        if (!isValiable(taskNum)) {
            return false;
        }
        String selection = DatabaseMaterialAppConstants.Modeling3dReconstruct.COLUMN_TASKID + SELECT_EQUAL;
        String[] selectionArgs = new String[]{taskNum};
        Cursor cursor = null;
        try {
            cursor = DatabaseMaterialAppUtils.getDatabase()
                    .query(DatabaseMaterialAppConstants.Modeling3dReconstruct.TABLE_NAME, null, selection, selectionArgs, null, null,
                            null);
            if ((cursor == null) || (cursor.getCount() <= 0)) {
                return false;
            }
            return true;
        } finally {
            DatabaseMaterialAppUtils.closeCursor(cursor);
        }
    }

    public static boolean isExistedPath(String path) {
        if (!isValiable(path)) {
            return false;
        }
        String selection = DatabaseMaterialAppConstants.Modeling3dReconstruct.COLUMN_UPLOADPATH + SELECT_EQUAL;
        String[] selectionArgs = new String[]{path};
        Cursor cursor = null;
        try {
            cursor = DatabaseMaterialAppUtils.getDatabase()
                    .query(DatabaseMaterialAppConstants.Modeling3dReconstruct.TABLE_NAME, null, selection, selectionArgs, null, null,
                            null);
            if ((cursor == null) || (cursor.getCount() <= 0)) {
                return false;
            }
            return true;
        } finally {
            DatabaseMaterialAppUtils.closeCursor(cursor);
        }
    }


    private static ContentValues getValues(TaskInfoMaterialAppDb entity) {
        ContentValues values = new ContentValues();
        try {
            values.put(DatabaseMaterialAppConstants.Modeling3dReconstruct.COLUMN_ISDOWNLOAD, Integer.valueOf(entity.getIsDownload()));
            values.put(DatabaseMaterialAppConstants.Modeling3dReconstruct.COLUMN_STATES, Integer.valueOf(entity.getStatus()));
            values.put(DatabaseMaterialAppConstants.Modeling3dReconstruct.COLUMN_CREATE_TIME, Long.valueOf(entity.getCreateTime()));
            if (entity.getFileSavePath() != null) {
                values.put(DatabaseMaterialAppConstants.Modeling3dReconstruct.COLUMN_SAVEPATH, entity.getFileSavePath());
            }
            if (entity.getFileUploadPath() != null) {
                values.put(DatabaseMaterialAppConstants.Modeling3dReconstruct.COLUMN_UPLOADPATH, entity.getFileUploadPath());
            }
            if (entity.getTaskId() != null) {
                values.put(DatabaseMaterialAppConstants.Modeling3dReconstruct.COLUMN_TASKID, entity.getTaskId());
            }
            if (entity.getModelType() != null) {
                values.put(DatabaseMaterialAppConstants.Modeling3dReconstruct.MODEL_TYPE, entity.getModelType());
            }
            return values;
        } catch (IllegalArgumentException e) {
            Log.e(TAG ,String.valueOf(e.getMessage()));
        }
        return null;
    }

    private static boolean isValiable(Object object) {
        if (object == null) {
            return false;
        }
        if ((object instanceof String) && (TextUtils.isEmpty((String) object))) {
            return false;
        }
        if (!isDatabaseUsable()) {
            return false;
        }
        return true;
    }

    private static boolean isDatabaseUsable() {
        SQLiteDatabase Database = DatabaseMaterialAppUtils.getDatabase();
        if ((Database == null) || (!Database.isOpen())
                || (!DatabaseMaterialAppUtils.isTableExist(DatabaseMaterialAppConstants.Modeling3dReconstruct.TABLE_NAME))) {
            return false;
        }
        return true;
    }

    public static ArrayList<TaskInfoMaterialAppDb> getAllTasks() {
        ArrayList<TaskInfoMaterialAppDb> result = new ArrayList<TaskInfoMaterialAppDb>();
        Cursor cursor = null;
        try {
            cursor = DatabaseMaterialAppUtils.getDatabase()
                    .query(DatabaseMaterialAppConstants.Modeling3dReconstruct.TABLE_NAME,
                            null, null, null, null, null,
                            null);
            while (cursor != null && cursor.moveToNext()) {
                TaskInfoMaterialAppDb info = parseFromCursor(cursor);
                if (info != null) {
                    result.add(info);
                }
            }
            return result;
        } finally {
            DatabaseMaterialAppUtils.closeCursor(cursor);
        }
    }

    private static TaskInfoMaterialAppDb parseFromCursor(Cursor cursor) {
        try {
            TaskInfoMaterialAppDb spec = new TaskInfoMaterialAppDb();
            spec.setCreateTime(
                    cursor.getLong(cursor.getColumnIndexOrThrow(DatabaseMaterialAppConstants.Modeling3dReconstruct.COLUMN_CREATE_TIME)));
            spec.setStatus(
                    cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseMaterialAppConstants.Modeling3dReconstruct.COLUMN_STATES)));
            spec.setIsDownload(
                    cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseMaterialAppConstants.Modeling3dReconstruct.COLUMN_ISDOWNLOAD)));
            spec.setModelType(
                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseMaterialAppConstants.Modeling3dReconstruct.MODEL_TYPE)));
            String taskId =
                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseMaterialAppConstants.Modeling3dReconstruct.COLUMN_TASKID));
            String savePath =
                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseMaterialAppConstants.Modeling3dReconstruct.COLUMN_SAVEPATH));
            String uploadPath =
                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseMaterialAppConstants.Modeling3dReconstruct.COLUMN_UPLOADPATH));
            if (!TextUtils.isEmpty(taskId) && !taskId.equals(NULL_STR)) {
                spec.setTaskId(taskId);
            }
            if (!TextUtils.isEmpty(savePath) && !savePath.equals(NULL_STR)) {
                spec.setFileSavePath(savePath);
            }
            if (!TextUtils.isEmpty(uploadPath) && !uploadPath.equals(NULL_STR)) {
                spec.setFileUploadPath(uploadPath);
            }
            return spec;
        } catch (IllegalArgumentException e) {
            Log.e(TAG ,String.valueOf(e.getMessage()));
        }
        return null;
    }

}