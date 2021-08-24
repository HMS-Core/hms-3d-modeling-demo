package com.huawei.cameratakelib.listener;

import android.graphics.Bitmap;

import java.io.File;

/**
 * 图片拍摄回调
 * */
public interface CameraTakeListener {

    void onSuccess(File bitmapFile, Bitmap mBitmap);

    void onFail(String error);

}
