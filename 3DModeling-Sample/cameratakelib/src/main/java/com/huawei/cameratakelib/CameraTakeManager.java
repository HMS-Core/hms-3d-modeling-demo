package com.huawei.cameratakelib;

import android.app.Activity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.huawei.cameratakelib.listener.CameraTakeListener;
import com.huawei.hms.magicresource.view.ResizeAbleSurfaceView;

public class CameraTakeManager {

    Activity activity;
    ResizeAbleSurfaceView surfaceView;
    CameraTakeListener listener;

    SurfaceHolder surfaceHolder;

    public SurfaceViewCallback getSurfaceViewCallback() {
        return surfaceViewCallback;
    }

    SurfaceViewCallback surfaceViewCallback;


    public CameraTakeManager(Activity activity, ResizeAbleSurfaceView surfaceView, CameraTakeListener listener) {
        this.activity = activity;
        this.surfaceView = surfaceView;
        this.listener = listener;

        surfaceViewCallback = new SurfaceViewCallback(activity, listener,surfaceView);
        initCamera();
    }

    public CameraTakeManager(Activity activity, ResizeAbleSurfaceView surfaceView, CameraTakeListener listener, int model){
        this.activity = activity;
        this.surfaceView = surfaceView;
        this.listener = listener;

        surfaceViewCallback = new SurfaceViewCallback(activity, listener, model, surfaceView);
        initCamera();
    }

    /**
     * 初始化相机
     */
    private void initCamera() {
        //在surfaceView中获取holder
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(surfaceViewCallback);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    /**
     * 获取相机当前的照片
     * */
    public void takePhoto() {
        surfaceViewCallback.takePhoto();
    }
    /**
     * 获取相机当前的照片
     * */
    public void takePhoto(int top, int width) {
        surfaceViewCallback.takePhoto(top, width);
    }

    public void destroy() {
        surfaceViewCallback.destroy();
    }

    public void takePhotoRgb(float ratio, int type) {
        surfaceViewCallback.takePhotoRgb( ratio,type);
    }

}
