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

package com.huawei.cameratakelib;

import android.app.Activity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.huawei.cameratakelib.listener.CameraTakeListener;
import com.huawei.cameratakelib.utils.LogUtil;

public class CameraTakeManager {

    SurfaceView surfaceView;

    SurfaceHolder surfaceHolder;

    public SurfaceViewCallback getSurfaceViewCallback() {
        return surfaceViewCallback;
    }

    SurfaceViewCallback surfaceViewCallback;


    public CameraTakeManager(Activity activity, SurfaceView surfaceView, CameraTakeListener listener) {
        this.surfaceView = surfaceView;
        surfaceViewCallback = new SurfaceViewCallback(activity, listener, surfaceView);
        initCamera();
    }

    public CameraTakeManager(Activity activity, SurfaceView surfaceView, CameraTakeListener listener, int model) {
        this.surfaceView = surfaceView;
        surfaceViewCallback = new SurfaceViewCallback(activity, listener, model, surfaceView);
        initCamera();
    }

    /**
     * Initialize the camera.
     */
    private void initCamera() {
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(surfaceViewCallback);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    /**
     * Gets the current photo of the camera
     */
    public void takePhoto() {
        surfaceViewCallback.takePhoto();
    }

    /**
     * Gets the current photo of the camera
     *
     * @param top top
     * @param width width
     */
    public void takePhoto(int top, int width) {
        surfaceViewCallback.takePhoto(top, width);
    }

    public void destroy() {
        surfaceViewCallback.destroy();
    }

}
