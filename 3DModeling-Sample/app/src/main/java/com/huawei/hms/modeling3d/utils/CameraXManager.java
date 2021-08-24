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

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.util.Log;
import android.util.Size;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;

import androidx.camera.core.Camera;
import androidx.camera.core.CameraControl;
import androidx.camera.core.CameraInfo;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.FocusMeteringAction;
import androidx.camera.core.FocusMeteringResult;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import com.google.common.util.concurrent.ListenableFuture;
import com.huawei.hms.modeling3d.model.ConstantBean;

import java.nio.ByteBuffer;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class CameraXManager {
    private Context context;
    private PreviewView previewView;
    private Executor executor = Executors.newSingleThreadExecutor();
    private ImageCapture imageCapture;
    private TakePicBack takePicBack;

    private CameraControl mCameraControl;

    int getPicWidthValue;
    int getPicHeightValue;
    int screenType;

    public void setTakePicBack(TakePicBack takePicBack) {
        this.takePicBack = takePicBack;
    }

    public CameraXManager(Context context, PreviewView previewView, int screenType) {
        this.context = context;
        this.screenType = screenType;
        this.previewView = previewView;
        initPar();
    }

    public void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(context);
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                bindPreview(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                // No errors need to be handled for this Future.
                // This should never be reached.
            }
        }, ContextCompat.getMainExecutor(context));
    }

    @SuppressLint("WrongConstant")
    private void bindPreview(@NonNull ProcessCameraProvider cameraProvider) {

        cameraProvider.unbindAll();
        Preview preview = new Preview.Builder()
                .build();

        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();


        preview.setSurfaceProvider(previewView.getSurfaceProvider());


        Camera camera = cameraProvider.bindToLifecycle((LifecycleOwner) context, cameraSelector, preview);

        mCameraControl = camera.getCameraControl();

        initCameraFocus();
        imageCapture = new ImageCapture.Builder()
                .setTargetResolution(new Size(getPicWidthValue, getPicHeightValue))
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                .setTargetRotation(0)
                .build();


        ImageAnalysis imageAnalysis =
                new ImageAnalysis.Builder()
                        .setTargetResolution(new Size(previewView.getWidth(), previewView.getHeight()))
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();

        cameraProvider.bindToLifecycle((LifecycleOwner) context, cameraSelector, imageCapture, imageAnalysis, preview);
    }

    public void takePicture() {

        imageCapture.takePicture(executor, new ImageCapture.OnImageCapturedCallback() {
            @Override
            public void onCaptureSuccess(@NonNull ImageProxy im) {
                @SuppressLint("UnsafeExperimentalUsageError") Bitmap bitmap = imageToBitMap(im.getImage());
                if (takePicBack != null) {
                    takePicBack.takePicBack(bitmap);
                }
                super.onCaptureSuccess(im);
                im.close();
            }

            @Override
            public void onError(@NonNull ImageCaptureException exception) {
                super.onError(exception);
            }
        });
    }


    public void cameraDestroy() {
        if (previewView != null) {
            previewView = null;
        }

    }

    public void initCameraFocus() {

        previewView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                float x = motionEvent.getX();
                float y = motionEvent.getY();
                try {
                    FocusMeteringAction focusMeteringAction = new FocusMeteringAction.Builder(previewView.getMeteringPointFactory()
                            .createPoint(x, y)).build();

                    ListenableFuture future = mCameraControl.startFocusAndMetering(focusMeteringAction);
                    future.addListener(() -> {
                        try {
                            FocusMeteringResult result = (FocusMeteringResult) future.get();
                            if (result.isFocusSuccessful()) {
                                Log.e("initCameraFocus", "FocusSuccessful");
                            } else {
                                Log.e("initCameraFocus", "FocusFailed");
                            }
                        } catch (Exception e) {
                            Log.e("initCameraFocus", "FocusFailed");
                        }
                    }, executor);
                }catch (Exception e){
                    Log.e("initCameraFocus", "FocusFailed");
                }
                return true;
            }
        });

    }


    public interface TakePicBack {
        void takePicBack(Bitmap bitmap);
    }

    public Bitmap imageToBitMap(Image image) {
        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
        byte[] data = new byte[buffer.remaining()];
        buffer.get(data);
        return BitmapFactory.decodeByteArray(data, 0, data.length);
    }

    public void initPar() {
        int[] width = {1080, 1440, 2160};
        int[] height = {1920, 2560, 3840};
        if (screenType == ConstantBean.SCREEN_MODEL_TYPE_ZERO) {
            getPicWidthValue = width[0];
            getPicHeightValue = height[0];
        } else if (screenType == ConstantBean.SCREEN_MODEL_TYPE_ONE) {
            getPicWidthValue = width[1];
            getPicHeightValue = height[1];
        } else if (screenType == ConstantBean.SCREEN_MODEL_TYPE_TWO) {
            getPicWidthValue = width[2];
            getPicHeightValue = height[2];
        }
    }


}
