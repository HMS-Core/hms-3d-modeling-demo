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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.huawei.cameratakelib.listener.CameraTakeListener;
import com.huawei.cameratakelib.utils.FileUtil;
import com.huawei.cameratakelib.utils.LogUtil;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.List;

public class SurfaceViewCallback implements SurfaceHolder.Callback {

    public static final int NORMAL_MODEL = 1;
    public static final int MATERIAL_MODEL = 2;

    private int model = NORMAL_MODEL;

    private int width;

    private int top;

    private Activity activity;

    boolean previewing;

    private boolean hasSurface;

    Camera mCamera;

    int mCurrentCamIndex = 0;

    /**
     * true to start capturing photos
     */
    boolean canTake;


    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getCreateTime() {
        return createTime;
    }

    private String createTime;

    public void setIndex(Integer index) {
        this.index = index;
    }

    private Integer index = 0;

    private int cameraFacingBack = Camera.CameraInfo.CAMERA_FACING_BACK;
    /**
     * Callback API for taking photos
     */
    CameraTakeListener listener;
    SurfaceView surfaceView;
    int widthDes;

    public int getWidthDes() {
        return widthDes;
    }

    public void setWidthDes(int widthDes) {
        this.widthDes = widthDes;
    }

    public SurfaceViewCallback(Activity activity, CameraTakeListener listener) {
        previewing = false;
        hasSurface = false;

        this.activity = activity;
        this.listener = listener;
    }

    public SurfaceViewCallback(Activity activity, CameraTakeListener listener, int model, SurfaceView surfaceView) {
        previewing = false;
        hasSurface = false;

        this.activity = activity;
        this.listener = listener;
        this.model = model;
        this.surfaceView = surfaceView;
    }

    public SurfaceViewCallback(Activity activity, CameraTakeListener listener, SurfaceView surfaceView) {
        previewing = false;
        hasSurface = false;

        this.activity = activity;
        this.listener = listener;
        this.surfaceView = surfaceView;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (!hasSurface) {
            hasSurface = true;
            mCamera = openBakcFacingCamera();
            if (mCamera == null) {
                listener.onFail("No camera available");
                return;
            } else {
                Camera.Parameters parameters = mCamera.getParameters();
                List<Camera.Size> pictureSizes = mCamera.getParameters()
                        .getSupportedPreviewSizes();
                sizeSort(pictureSizes);
                Camera.Size fs = null;
                int type = 0;
                for (int i = 0; i < pictureSizes.size(); i++) {
                    Camera.Size psize = pictureSizes.get(i);
                    if (fs == null && psize.width / 16 == psize.height / 9) {
                        fs = psize;
                        type = 1;
                    }
                    if (fs == null && psize.width / 4 == psize.height / 3) {
                        fs = psize;
                        type = 2;
                    }
                }
                Camera.Size fss = null;
                List<Camera.Size> pictureSizes1 = parameters.getSupportedPictureSizes();
                sizeSort(pictureSizes1);
                for (int i = 0; i < pictureSizes1.size(); i++) {
                    Camera.Size psize = pictureSizes1.get(i);
                    if (fss == null && type == 1 && psize.width / 16 == psize.height / 9) {
                        fss = psize;
                    }
                    if (fss == null && type == 2 && psize.width / 4 == psize.height / 3) {
                        fss = psize;
                    }
                }

                if (fss != null) {
                    parameters.setPictureSize(fss.width, fss.height);
                }
                if (fs != null) {
                    parameters.setPreviewSize(fs.width, fs.height);
                }
                if (model == MATERIAL_MODEL) {
                    parameters.setExposureCompensation(0);
                }


                if (cameraFacingBack == Camera.CameraInfo.CAMERA_FACING_BACK) {
                    if (parameters.getSupportedFocusModes().contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
                        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
                    } else {
                        parameters.setFocusMode(parameters.FOCUS_MODE_AUTO);
                    }
                }

                mCamera.setParameters(parameters);
            }
            mCamera.setPreviewCallback(new Camera.PreviewCallback() {
                @Override
                public void onPreviewFrame(byte[] bytes, Camera camera) {
                    if (canTake) {
                        getSurfacePic(bytes, camera);
                        canTake = false;
                    }
                }
            });
        }
    }

    private void sizeSort(List<Camera.Size> sizeList) {
        for (int i = 0; i < sizeList.size() - 1; i++) {
            for (int j = 0; j < sizeList.size() - i - 1; j++) {
                if (sizeList.get(j).width < sizeList.get(j + 1).width) {
                    Camera.Size size = sizeList.get(j);
                    sizeList.set(j, sizeList.get(j + 1));
                    sizeList.set(j + 1, size);
                }
            }
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        if (previewing) {
            mCamera.stopPreview();
            previewing = false;
        }

        try {
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();
            previewing = true;
            setCameraDisplayOrientation(activity, mCurrentCamIndex, mCamera);
        } catch (Exception e) {
            LogUtil.e("Failed to set camera preview: " + e.getLocalizedMessage());
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (!previewing) {
            return;
        }
        holder.removeCallback(this);
        mCamera = null;
    }

    /**
     * Sets the direction in which the camera plays
     *
     * @param activity Interface display
     * @param cameraId Camera id
     * @param camera camera
     */

    private void setCameraDisplayOrientation(Activity activity, int cameraId, Camera camera) {
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();

        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
            default:
        }
        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {
            result = (info.orientation - degrees + 360) % 360;
        }
        camera.setDisplayOrientation(result);
    }

    /**
     * Open the camera panel.
     *
     * @return camera
     */
    private Camera openBakcFacingCamera() {
        int cameraCount = 0;
        Camera cam = null;
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        cameraCount = Camera.getNumberOfCameras();

        for (int camIdx = 0; camIdx < cameraCount; camIdx++) {
            Camera.getCameraInfo(camIdx, cameraInfo);
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                try {
                    cam = Camera.open(camIdx);
                    mCurrentCamIndex = camIdx;
                } catch (RuntimeException e) {
                    LogUtil.e("Camera failed to open: " + e.getLocalizedMessage());
                }
            }

        }
        return cam;
    }

    /**
     * Get Photos
     *
     * @param data Byte stream
     * @param camera camera
     */
    public void getSurfacePic(byte[] data, Camera camera) {
        Camera.Size size = camera.getParameters().getPreviewSize();
        YuvImage image = new YuvImage(data, ImageFormat.NV21, size.width, size.height, null);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        image.compressToJpeg(new Rect(0, 0, size.width, size.height), 100, stream);
        Bitmap bmp = BitmapFactory.decodeByteArray(stream.toByteArray(), 0, stream.size());
        rotateMyBitmap(bmp);

    }


    /**
     * Get Photos
     *
     * @param data Byte stream
     * @param camera camera
     */

    public void getTakePic(byte[] data, Camera camera) {
        Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
        Camera.Size picSize = camera.getParameters().getPictureSize();
        int picWidth = picSize.width;
        int picHeight = picSize.height;
        int x = top * picWidth / surfaceView.getHeight();
        int hei = width * picWidth / surfaceView.getHeight();
        int wid = width * picHeight / surfaceView.getWidth();
        int y = (picHeight - wid) / 2;
        if (y > 0 && x > 0 && x + hei < bmp.getWidth() && y + wid < bmp.getHeight()) {
            Matrix matrix = new Matrix();
            matrix.postRotate(90);
            Bitmap resBmp = Bitmap.createBitmap(bmp, x, y, hei, wid, matrix, false);
            int leng = Math.max(hei, wid);
            setWidthDes(leng);
            Bitmap resBmp1 = Bitmap.createScaledBitmap(resBmp, leng, leng, true);
            saveMyBitmap(FileUtil.compressImage(resBmp1));
        } else {
            mCamera.startPreview();
            listener.onFail("Failed to capture the image. The width of the image is as follows:" + bmp.getHeight() + " high：" + bmp.getWidth() + "Truncated start point x axis：" + x + " y-axis：" + y + " Wide interception range：" + hei + " high：" + wid);
        }
    }

    /**
     * Rotate Pictures
     *
     * @param bmp picture
     */
    public void rotateMyBitmap(Bitmap bmp) {
        Matrix matrix = new Matrix();
        matrix.postRotate(90);

        Bitmap nbmp2 = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true);

        saveMyBitmap(FileUtil.compressImage(nbmp2));

    }

    /**
     * Save Pictures
     *
     * @param mBitmap picture
     */
    public void saveMyBitmap(final Bitmap mBitmap) {
        if (model == MATERIAL_MODEL) {
            mCamera.startPreview();
        }
        if (FileUtil.getAvailableSize() > 512) {
            index += 1;
            index = index % 100;
            final File filePic = FileUtil.saveBitmap(activity, mBitmap, createTime, index);
            if (filePic == null) {
                listener.onFail("Failed to save the image.");
                return;
            }
            listener.onSuccess(filePic, mBitmap);
        } else {
            listener.onFail("The storage space is less than 512 MB, and images cannot be saved.");
        }
    }

    /**
     * Gets the current photo of the camera
     */
    public void takePhoto() {
        this.canTake = true;
    }

    /**
     * The camera performs a shooting operation.
     *
     * @param top height
     * @param width width
     */
    public void takePhoto(int top, int width) {
        this.top = top;
        this.width = width;
        mCamera.takePicture(null, null, new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
                getTakePic(data, camera);
            }
        });
    }

    /**
     * release
     */
    public void destroy() {
        hasSurface = false;
    }


    public void onPause() {
        if (null != mCamera) {
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }
}
