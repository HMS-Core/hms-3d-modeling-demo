package com.huawei.cameratakelib;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.os.Build;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.NonNull;

import com.huawei.cameratakelib.listener.CameraTakeListener;
import com.huawei.cameratakelib.utils.FileUtil;
import com.huawei.cameratakelib.utils.LogUtil;
import com.huawei.hms.magicresource.view.ResizeAbleSurfaceView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import top.zibin.luban.OnCompressListener;

public class SurfaceViewCallback implements SurfaceHolder.Callback ,Camera.PreviewCallback{

    public static final int NORMAL_MODEL = 1;
    public static final int MATERIAL_MODEL = 2;
    public static final int RGB_MODEL = 3;

    private int model = NORMAL_MODEL;

    private int width;

    private int top;

    private Activity activity;

    boolean previewing;

    private boolean hasSurface;

    Camera mCamera;

    int mCurrentCamIndex = 0;

    protected int mCameraInit = 0;

    /**
     * 为true时则开始捕捉照片
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

    private Integer index;

    private int CameraFacing = Camera.CameraInfo.CAMERA_FACING_BACK;
    protected Camera.CameraInfo mCameraInfo = null;
    /**
     * 拍照回调接口
     */
    CameraTakeListener listener;
    ResizeAbleSurfaceView surfaceView;

    private Camera.AutoFocusCallback myAutoFocusCallback = null;

    int widthDes;
    float ratio;
    int screenType;//0 1k 1 2k  2 4k

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

    public SurfaceViewCallback(Activity activity, CameraTakeListener listener, int model, ResizeAbleSurfaceView surfaceView) {
        previewing = false;
        hasSurface = false;

        this.activity = activity;
        this.listener = listener;
        this.model = model;
        this.surfaceView = surfaceView;
    }

    public SurfaceViewCallback(Activity activity, CameraTakeListener listener, ResizeAbleSurfaceView surfaceView) {
        previewing = false;
        hasSurface = false;

        this.activity = activity;
        this.listener = listener;
        this.surfaceView = surfaceView;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        LogUtil.i("surfaceCreated hasSurface = " + hasSurface);
        if (!hasSurface) {
            hasSurface = true;
            mCamera = openBakcFacingCamera();
            mCameraInit = 1;
            if (mCamera == null) {
                listener.onFail("没有可用的摄像头");
                return;
            } else {
                Camera.Parameters parameters = mCamera.getParameters();
                initParameters(parameters);
                mCamera.cancelAutoFocus();// 只有加上了这一句，才会自动对焦

                List<String> focusModes = parameters.getSupportedFocusModes();
                if (focusModes != null) {
                    if (focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
                    } else {
                        if (parameters.getMaxNumFocusAreas() > 0) {
                            List<Camera.Area> focusAreas = new ArrayList<Camera.Area>();
                            focusAreas.add(new Camera.Area(new Rect(100, 100, 100, 100), 1000));
                            parameters.setFocusAreas(focusAreas);
                        }
                    }
                }

                mCamera.setParameters(parameters);
            }
            mCamera.setPreviewCallback(SurfaceViewCallback.this);
        }
    }

    private Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes, int w, int h) {
        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio = (double) w / h;
        if (sizes == null) return null;

        Camera.Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        int targetHeight = h;

        // Try to find an size match aspect ratio and size
        for (Camera.Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        // Cannot find the one match the aspect ratio, ignore the requirement
        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }
        return optimalSize;
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
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (!previewing)
            return;
        holder.removeCallback(this);
//        mCamera.setPreviewCallback(null);
//        mCamera.stopPreview();
//        mCamera.release();
        mCamera = null;
    }

    /**
     * 设置照相机播放的方向
     */
    private void setCameraDisplayOrientation(Activity activity, int cameraId, Camera camera) {
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();

        /** 度图片顺时针旋转的角度。有效值为0、90、180和270*/
        /** 起始位置为0（横向）*/
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
        }
        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {
            /** 背面*/
            result = (info.orientation - degrees + 360) % 360;
        }
        camera.setDisplayOrientation(result);
    }

    /**
     * 打开摄像头面板
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
     * 获取照片
     */
    public void getSurfacePic(byte[] data, Camera camera) {
        Camera.Size size = camera.getParameters().getPreviewSize();
        YuvImage image = new YuvImage(data, ImageFormat.NV21, size.width, size.height, null);
        if (image != null) {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            switch (model) {
                case NORMAL_MODEL:
                    image.compressToJpeg(new Rect(0, 0, size.width, size.height), 100, stream);
                    break;
                case MATERIAL_MODEL:
                    int width = this.width;
                    int left = this.top;
                    int top = (size.height - width) / 2;
                    int right = left + width;
                    int bottom = top + width;
                    image.compressToJpeg(new Rect(left, top, right, bottom), 100, stream);
                    break;
            }

            Bitmap bmp = BitmapFactory.decodeByteArray(stream.toByteArray(), 0, stream.size());
            /** 因为图片会放生旋转，因此要对图片进行旋转到和手机在一个方向上*/
            rotateMyBitmap(bmp);
        }
    }

    /**
     * 获取照片
     */
    public void getTakePic(byte[] data, Camera camera) {
        Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
        Camera.Size preSize = camera.getParameters().getPreviewSize();
        Camera.Size picSize = camera.getParameters().getPictureSize();
        int preWidth = preSize.width;
        int preHeight = preSize.height;
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
            listener.onFail("图片截取失败 图片宽：" + bmp.getHeight() + " 高：" + bmp.getWidth() + "截取起点 x轴：" + x + " y轴：" + y + " 截取范围 宽：" + hei + " 高：" + wid);
        }
    }

    /**
     * 旋转图片
     */
    public void rotateMyBitmap(Bitmap bmp) {
        Matrix matrix = new Matrix();
        matrix.postRotate(90);

        Bitmap nbmp2 = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true);

        saveMyBitmap(FileUtil.compressImage(nbmp2));

    }

    /**
     * 保存图片
     */
    public void saveMyBitmap(final Bitmap mBitmap) {
        if (FileUtil.getAvailableSize() > 512) {
            index += 1;
            index = index % 100;
            final File filePic = FileUtil.saveBitmap(activity, mBitmap, createTime, index);
            if (filePic == null) {
                /** 图片保存失败*/
                listener.onFail("图片保存失败");
                return;
            }
            listener.onSuccess(filePic, mBitmap);
        } else {
            listener.onFail("存储空间小于512M，图片无法正常保存");
        }
    }

    /**
     * 获取相机当前的照片
     */
    public void takePhoto() {
        this.canTake = true;
    }

    /**
     * 相机执行拍摄操作
     */
    public void takePhoto(int top, int width) {
//        this.canTake = true;
        this.top = top;
        this.width = width;
        mCamera.takePicture(null, null, new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
                mCamera.startPreview();
                getTakePic(data, camera);
            }
        });
    }

    /**
     * 释放
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

    /**
     * 获取照片
     */
    public void getTakeRgbPic(byte[] data, Camera camera) {
        Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
        int[] width = {1080, 1440, 2160};
        int[] height = {1920, 2560, 3840};
        int getWidthValue = width[0];
        int getHeightValue = height[0];
        if (screenType == 0) {
            getWidthValue = width[0];
            getHeightValue = height[0];
        } else if (screenType == 1) {
            getWidthValue = width[1];
            getHeightValue = height[1];
        } else if (screenType == 2) {
            getWidthValue = width[2];
            getHeightValue = height[2];
        }
        Bitmap resBmp = Bitmap.createScaledBitmap(bmp, getHeightValue, getWidthValue, true);
        rotateMyBitmap(resBmp);
    }

    public void takePhotoRgb(float ratio, int type) {
        this.screenType = type;
        this.ratio = ratio;
        mCamera.takePicture(null, null, new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
                camera.startPreview();
                camera.autoFocus(new Camera.AutoFocusCallback() {
                    @Override
                    public void onAutoFocus(boolean b, Camera camera) {
                        if (b){
                            getTakeRgbPic(data, camera);
                        }
                    }
                });
            }
        });
    }


    public void initParameters(Camera.Parameters parameters) {
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
        if (fs != null) {
            Display display = activity.getWindowManager().getDefaultDisplay();
            int width = display.getWidth();
            int height = display.getHeight();
            DecimalFormat df = new DecimalFormat("0.00");//格式化小数
            String num = df.format((float)height/width);//返回的是String类型
            int ratioPx = 0 ;
            int ratioPxWid = 0 ;
            if (height-fs.width>0){//以屏幕的高度为基础
                ratioPx = height-fs.width;
                ratioPxWid = (int) (ratioPx/Float.valueOf(num));
                surfaceView.resize(fs.height+ratioPxWid,fs.width+ratioPx);
            }
            parameters.setPreviewSize(fs.width, fs.height);
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
    public void onPreviewFrame(byte[] bytes, Camera camera) {
        if (canTake) {
            getSurfacePic(bytes, camera);
            canTake = false;
        }
    }
}
