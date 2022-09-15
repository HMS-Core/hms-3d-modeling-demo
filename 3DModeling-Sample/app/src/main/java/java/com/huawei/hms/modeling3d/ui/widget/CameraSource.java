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
package com.huawei.hms.modeling3d.ui.widget;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.WindowManager;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresPermission;


import com.huawei.hms.modeling3d.model.FrameMetadata;
import com.huawei.hms.modeling3d.ui.activity.ChooserActivity;
import com.huawei.hms.modeling3d.utils.skeleton.HmsMotionImageProcessor;

import java.io.IOException;
import java.lang.Thread.State;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages the camera and allows UI updates on top of it (e.g. overlaying extra Graphics or
 * displaying extra information). This receives preview frames from the camera at a specified rate,
 * sending those frames to child classes' detectors / classifiers as fast as it is able to process.
 */
@SuppressLint("MissingPermission")
public class CameraSource {
    @SuppressLint("InlinedApi")
    public static final int CAMERA_FACING_BACK = CameraInfo.CAMERA_FACING_BACK;

    @SuppressLint("InlinedApi")
    public static final int CAMERA_FACING_FRONT = CameraInfo.CAMERA_FACING_FRONT;

    private static final String TAG = "MIDemoApp:CameraSource";

    /**
     * The dummy surface texture must be assigned a chosen name. Since we never use an OpenGL context,
     * we can choose any ID we want here. The dummy surface texture is not a crazy hack - it is
     * actually how the camera team recommends using the camera without a preview.
     */
    private static final int DUMMY_TEXTURE_NAME = 100;

    /**
     * If the absolute difference between a preview size aspect ratio and a picture size aspect ratio
     * is less than this tolerance, they are considered to be the same aspect ratio.
     */
    private static final float ASPECT_RATIO_TOLERANCE = 0.01f;

    protected Activity activity;

    private Camera camera;

    protected int facing = CAMERA_FACING_BACK;

    /**
     * Rotation of the device, and thus the associated preview images captured from the device. See
     * Frame.Metadata#getRotation().
     */
    private int rotation;

    private Size previewSize;

    public static final int WIDTH_SIZE = 1280;

    public static final int HEIGHT_SIZE = 720;

    private final float requestedFps = 30.0f;
    private int requestedPreviewWidth = 640;
    private int requestedPreviewHeight = 480;
    private final boolean requestedAutoFocus = true;

    // These instances need to be held onto to avoid GC of their underlying resources.  Even though
    // these aren't used outside of the method that creates them, they still must have hard
    // references maintained to them.
    private SurfaceTexture dummySurfaceTexture;
    private GraphicOverlay graphicOverlay;

    private BoneGLSurfaceView glSurfaceView;

    // True if a SurfaceTexture is being used for the preview, false if a SurfaceHolder is being
    // used for the preview.  We want to be compatible back to Gingerbread, but SurfaceTexture
    // wasn't introduced until Honeycomb.  Since the interface cannot use a SurfaceTexture, if the
    // developer wants to display a preview we must use a SurfaceHolder.  If the developer doesn't
    // want to display a preview we use a SurfaceTexture if we are running at least Honeycomb.
    private boolean usingSurfaceTexture;

    /**
     * Dedicated thread and associated runnable for calling into the detector with frames, as the
     * frames become available from the camera.
     */
    private Thread processingThread;

    private final FrameProcessingRunnable processingRunnable;

    private final Object processorLock = new Object();
    private HmsMotionImageProcessor frameProcessor;

    /**
     * Map to convert between a byte array, received from the camera, and its associated byte buffer.
     * We use byte buffers internally because this is a more efficient way to call into native code
     * later (avoids a potential copy).
     *
     * <p><b>Note:</b> uses IdentityHashMap here instead of HashMap because the behavior of an array's
     * equals, hashCode and toString methods is both useless and unexpected. IdentityHashMap enforces
     * identity ('==') check on the keys.
     */
    private final Map<byte[], ByteBuffer> bytesToByteBuffer = new IdentityHashMap<>();

    public CameraSource(Activity activity, GraphicOverlay overlay) {
        this.activity = activity;
        graphicOverlay = overlay;
        graphicOverlay.clear();
        processingRunnable = new FrameProcessingRunnable();

        if (Camera.getNumberOfCameras() == 1) {
            CameraInfo cameraInfo = new CameraInfo();
            Camera.getCameraInfo(0, cameraInfo);
            facing = cameraInfo.facing;
        }

    }

    public CameraSource(Activity activity, BoneGLSurfaceView overlay) {
        this.activity = activity;
        glSurfaceView = overlay;
        processingRunnable = new FrameProcessingRunnable();

        if (Camera.getNumberOfCameras() == 1) {
            CameraInfo cameraInfo = new CameraInfo();
            Camera.getCameraInfo(0, cameraInfo);
            facing = cameraInfo.facing;
        }

    }

    public void release() {
        synchronized (processorLock) {
            stop();
            processingRunnable.release();
            cleanScreen();

            if (frameProcessor != null) {
                frameProcessor.stop();
            }
        }
    }

    @RequiresPermission(Manifest.permission.CAMERA)
    public synchronized CameraSource start(SurfaceHolder surfaceHolder) throws IOException {
        if (camera != null) {
            return this;
        }

        camera = createCamera();
        if (ChooserActivity.isAsynchronous()) {
            usingSurfaceTexture = false;
            camera.setPreviewDisplay(surfaceHolder);
        } else {
            dummySurfaceTexture = new SurfaceTexture(DUMMY_TEXTURE_NAME);
            camera.setPreviewTexture(dummySurfaceTexture);
            usingSurfaceTexture = true;
        }
        camera.startPreview();
        processingThread = new Thread(processingRunnable);
        processingRunnable.setActive(true);
        processingThread.start();
        return this;
    }

    public synchronized void stop() {
        processingRunnable.setActive(false);
        if (processingThread != null) {
            try {
                // Wait for the thread to complete to ensure that we can't have multiple threads
                // executing at the same time (i.e., which would happen if we called start too
                // quickly after stop).
                processingThread.join();
            } catch (InterruptedException e) {
                Log.d(TAG, "Frame processing thread interrupted on release.");
            }
            processingThread = null;
        }

        if (camera != null) {
            camera.stopPreview();
            camera.setPreviewCallbackWithBuffer(null);
            try {
                if (usingSurfaceTexture) {
                    camera.setPreviewTexture(null);
                } else {
                    camera.setPreviewDisplay(null);
                }
            } catch (Exception e) {
                Log.e(TAG, "Failed to clear camera preview: " + e);
            }
            camera.release();
            camera = null;
        }

        // Release the reference to any image buffers, since these will no longer be in use.
        bytesToByteBuffer.clear();
    }

    public synchronized void setFacing(int facing) {
        if ((facing != CAMERA_FACING_BACK) && (facing != CAMERA_FACING_FRONT)) {
            throw new IllegalArgumentException("Invalid camera: " + facing);
        }
        this.facing = facing;
    }

    public Size getPreviewSize() {
        return previewSize;
    }

    public int getCameraFacing() {
        return facing;
    }

    @SuppressLint("InlinedApi")
    private Camera createCamera() throws IOException {
        int requestedCameraId = getIdForRequestedCamera(facing);
        Camera camera = Camera.open(requestedCameraId);
        SizePair sizePair = selectSizePair(camera, requestedPreviewWidth, requestedPreviewHeight);
        Size pictureSize = sizePair.pictureSize();
        previewSize = sizePair.previewSize();

        int[] previewFpsRange = selectPreviewFpsRange(camera, requestedFps);
        if (previewFpsRange == null) {
            throw new IOException("Could not find suitable preview frames per second range.");
        }

        Camera.Parameters parameters = camera.getParameters();

        if (pictureSize != null) {
            parameters.setPictureSize(pictureSize.getWidth(), pictureSize.getHeight());
        }
        parameters.setPreviewSize(previewSize.getWidth(), previewSize.getHeight());
        parameters.setPreviewFpsRange(
                previewFpsRange[Camera.Parameters.PREVIEW_FPS_MIN_INDEX],
                previewFpsRange[Camera.Parameters.PREVIEW_FPS_MAX_INDEX]);
        parameters.setPreviewFormat(ImageFormat.NV21);

        setRotation(camera, parameters, requestedCameraId);

        if (requestedAutoFocus) {
            if (parameters
                    .getSupportedFocusModes()
                    .contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
            } else {
                Log.i(TAG, "Camera auto focus is not supported on this device.");
            }
        }

        camera.setParameters(parameters);
        camera.setPreviewCallbackWithBuffer(new CameraPreviewCallback());
        camera.addCallbackBuffer(createPreviewBuffer(previewSize));
        camera.addCallbackBuffer(createPreviewBuffer(previewSize));
        camera.addCallbackBuffer(createPreviewBuffer(previewSize));
        camera.addCallbackBuffer(createPreviewBuffer(previewSize));

        return camera;
    }

    private static int getIdForRequestedCamera(int facing) {
        CameraInfo cameraInfo = new CameraInfo();
        for (int i = 0; i < Camera.getNumberOfCameras(); ++i) {
            Camera.getCameraInfo(i, cameraInfo);
            if (cameraInfo.facing == facing) {
                return i;
            }
        }
        return -1;
    }

    private static SizePair selectSizePair(Camera camera, int desiredWidth, int desiredHeight) {
        List<SizePair> validPreviewSizes = generateValidPreviewSizeList(camera);

        SizePair selectedPair = null;
        int minDiff = Integer.MAX_VALUE;
        for (SizePair sizePair : validPreviewSizes) {
            Size size = sizePair.previewSize();
            int diff =
                    Math.abs(size.getWidth() - desiredWidth) + Math.abs(size.getHeight() - desiredHeight);
            if (diff < minDiff) {
                selectedPair = sizePair;
                minDiff = diff;
            }
        }

        return selectedPair;
    }

    private static class SizePair {
        private final Size preview;
        private Size picture;

        SizePair(
                Camera.Size previewSize,
                @Nullable Camera.Size pictureSize) {
            preview = new Size(previewSize.width, previewSize.height);
            if (pictureSize != null) {
                picture = new Size(pictureSize.width, pictureSize.height);
            }
        }

        Size previewSize() {
            return preview;
        }

        @Nullable
        Size pictureSize() {
            return picture;
        }
    }

    private static List<SizePair> generateValidPreviewSizeList(Camera camera) {
        Camera.Parameters parameters = camera.getParameters();
        List<Camera.Size> supportedPreviewSizes = parameters.getSupportedPreviewSizes();
        List<Camera.Size> supportedPictureSizes = parameters.getSupportedPictureSizes();
        List<SizePair> validPreviewSizes = new ArrayList<>();
        for (Camera.Size previewSize : supportedPreviewSizes) {
            float previewAspectRatio = (float) previewSize.width / (float) previewSize.height;
            for (Camera.Size pictureSize : supportedPictureSizes) {
                float pictureAspectRatio = (float) pictureSize.width / (float) pictureSize.height;
                if (Math.abs(previewAspectRatio - pictureAspectRatio) < ASPECT_RATIO_TOLERANCE) {
                    validPreviewSizes.add(new SizePair(previewSize, pictureSize));
                    break;
                }
            }
        }

        if (validPreviewSizes.size() == 0) {
            Log.w(TAG, "No preview sizes have a corresponding same-aspect-ratio picture size");
            for (Camera.Size previewSize : supportedPreviewSizes) {
                validPreviewSizes.add(new SizePair(previewSize, null));
            }
        }

        return validPreviewSizes;
    }

    /**
     * Selects the most suitable preview frames per second range, given the desired frames per second.
     *
     * @param camera            the camera to select a frames per second range from
     * @param desiredPreviewFps the desired frames per second for the camera preview frames
     * @return the selected preview frames per second range
     */
    @SuppressLint("InlinedApi")
    private static int[] selectPreviewFpsRange(Camera camera, float desiredPreviewFps) {

        int desiredPreviewFpsScaled = (int) (desiredPreviewFps * 1000.0f);

        int[] selectedFpsRange = null;
        int minDiff = Integer.MAX_VALUE;
        List<int[]> previewFpsRangeList = camera.getParameters().getSupportedPreviewFpsRange();
        for (int[] range : previewFpsRangeList) {
            int deltaMin = desiredPreviewFpsScaled - range[Camera.Parameters.PREVIEW_FPS_MIN_INDEX];
            int deltaMax = desiredPreviewFpsScaled - range[Camera.Parameters.PREVIEW_FPS_MAX_INDEX];
            int diff = Math.abs(deltaMin) + Math.abs(deltaMax);
            if (diff < minDiff) {
                selectedFpsRange = range;
                minDiff = diff;
            }
        }
        return selectedFpsRange;
    }

    private void setRotation(Camera camera, Camera.Parameters parameters, int cameraId) {
        WindowManager windowManager = (WindowManager) activity.getSystemService(Context.WINDOW_SERVICE);
        int degrees = 0;
        int rotation = windowManager.getDefaultDisplay().getRotation();
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
                Log.e(TAG, "Bad rotation value: " + rotation);
        }

        CameraInfo cameraInfo = new CameraInfo();
        Camera.getCameraInfo(cameraId, cameraInfo);

        int angle;
        int displayAngle;
        if (cameraInfo.facing == CameraInfo.CAMERA_FACING_FRONT) {
            angle = (cameraInfo.orientation + degrees) % 360;
            displayAngle = (360 - angle) % 360; // compensate for it being mirrored
        } else { // back-facing
            angle = (cameraInfo.orientation - degrees + 360) % 360;
            displayAngle = angle;
        }

        // This corresponds to the rotation constants.
        this.rotation = angle / 90;
        camera.setDisplayOrientation(displayAngle);
        parameters.setRotation(angle);
    }

    @SuppressLint("InlinedApi")
    private byte[] createPreviewBuffer(Size previewSize) {
        int bitsPerPixel = ImageFormat.getBitsPerPixel(ImageFormat.NV21);
        long sizeInBits = (long) previewSize.getHeight() * previewSize.getWidth() * bitsPerPixel;
        int bufferSize = (int) Math.ceil(sizeInBits / 8.0d) + 1;

        // Creating the byte array this way and wrapping it, as opposed to using .allocate(),
        // should guarantee that there will be an array to work with.
        byte[] byteArray = new byte[bufferSize];
        ByteBuffer buffer = ByteBuffer.wrap(byteArray);
        if (!buffer.hasArray() || (buffer.array() != byteArray)) {
            // I don't think that this will ever happen.  But if it does, then we wouldn't be
            // passing the preview content to the underlying detector later.
            throw new IllegalStateException("Failed to create valid buffer for camera source.");
        }

        bytesToByteBuffer.put(byteArray, buffer);
        return byteArray;
    }

    // ==============================================================================================
    // Frame processing
    // ==============================================================================================

    /**
     * Called when the camera has a new preview frame.
     */
    private class CameraPreviewCallback implements Camera.PreviewCallback {
        @Override
        public void onPreviewFrame(byte[] data, Camera camera) {
            processingRunnable.setNextFrame(data, camera);
        }
    }

    public void setMachineLearningFrameProcessor(HmsMotionImageProcessor processor) {
        synchronized (processorLock) {
            cleanScreen();
            if (frameProcessor != null) {
                frameProcessor.stop();
            }
            frameProcessor = processor;
        }
    }

    public void setRequestedPreviewWidth(int requestedPreviewWidth) {
        this.requestedPreviewWidth = requestedPreviewWidth;
    }

    public void setRequestedPreviewHeight(int requestedPreviewHeight) {
        this.requestedPreviewHeight = requestedPreviewHeight;
    }

    public Camera getCamera() {
        return camera;
    }

    public void setCamera(Camera camera) {
        this.camera = camera;
    }

    /**
     * This runnable controls access to the underlying receiver, calling it to process frames when
     * available from the camera. This is designed to run detection on frames as fast as possible
     * (i.e., without unnecessary context switching or waiting on the next frame).
     *
     * <p>While detection is running on a frame, new frames may be received from the camera. As these
     * frames come in, the most recent frame is held onto as pending. As soon as detection and its
     * associated processing is done for the previous frame, detection on the mostly recently received
     * frame will immediately start on the same thread.
     */
    private class FrameProcessingRunnable implements Runnable {

        // This lock guards all of the member variables below.
        private final Object lock = new Object();
        private boolean active = true;

        // These pending variables hold the state associated with the new frame awaiting processing.
        private ByteBuffer pendingFrameData;

        FrameProcessingRunnable() {
        }

        /**
         * Releases the underlying receiver. This is only safe to do after the associated thread has
         * completed, which is managed in camera source's release method above.
         */
        @SuppressLint("Assert")
        void release() {
        }

        void setActive(boolean active) {
            synchronized (lock) {
                this.active = active;
                lock.notifyAll();
            }
        }

        void setNextFrame(byte[] data, Camera camera) {
            synchronized (lock) {
                if (pendingFrameData != null) {
                    camera.addCallbackBuffer(pendingFrameData.array());
                    pendingFrameData = null;
                }

                if (!bytesToByteBuffer.containsKey(data)) {
                    Log.d(TAG, "Skipping frame. Could not find ByteBuffer associated with the image " + "data from the camera.");
                    return;
                }

                pendingFrameData = bytesToByteBuffer.get(data);

                // Notify the processor thread if it is waiting on the next frame (see below).
                lock.notifyAll();
            }
        }

        /**
         * As long as the processing thread is active, this executes detection on frames continuously.
         * The next pending frame is either immediately available or hasn't been received yet. Once it
         * is available, we transfer the frame info to local variables and run detection on that frame.
         * It immediately loops back for the next frame without pausing.
         *
         * <p>If detection takes longer than the time in between new frames from the camera, this will
         * mean that this loop will run without ever waiting on a frame, avoiding any context switching
         * or frame acquisition time latency.
         *
         * <p>If you find that this is using more CPU than you'd like, you should probably decrease the
         * FPS setting above to allow for some idle time in between frames.
         */
        @SuppressLint("InlinedApi")
        @SuppressWarnings("GuardedBy")
        @Override
        public void run() {
            ByteBuffer data;

            while (true) {
                synchronized (lock) {
                    while (active && (pendingFrameData == null)) {
                        try {
                            // Wait for the next frame to be received from the camera, since we
                            // don't have it yet.
                            lock.wait();
                        } catch (InterruptedException e) {
                            Log.w(TAG, "Frame processing loop terminated.", e);
                            return;
                        }
                    }

                    if (!active) {
                        // Exit the loop once this camera source is stopped or released.  We check
                        // this here, immediately after the wait() above, to handle the case where
                        // setActive(false) had been called, triggering the termination of this
                        // loop.
                        return;
                    }

                    // Hold onto the frame data locally, so that we can use this for detection
                    // below.  We need to clear pendingFrameData to ensure that this buffer isn't
                    // recycled back to the camera before we are done using that data.
                    data = pendingFrameData;
                    pendingFrameData = null;
                }

                // The code below needs to run outside of synchronization, because this will allow
                // the camera to add pending frame(s) while we are running detection on the current
                // frame.

                try {
                    synchronized (processorLock) {
                        Log.d(TAG, "Process an image");

                        frameProcessor.process(data, new FrameMetadata.Builder()
                                        .setWidth(previewSize.getWidth())
                                        .setHeight(previewSize.getHeight())
                                        .setRotation(rotation)
                                        .setCameraFacing(facing)
                                        .build(),
                                glSurfaceView);
                    }
                } catch (Throwable t) {
                    Log.e(TAG, "Exception thrown from receiver.", t);
                } finally {
                    camera.addCallbackBuffer(data.array());
                }
            }
        }
    }

    static final class Size {
        private final int width;
        private final int height;

        public Size(int width, int height) {
            this.width = width;
            this.height = height;
        }

        public int getWidth() {
            return this.width;
        }

        public int getHeight() {
            return this.height;
        }
    }

    /**
     * Cleans up graphicOverlay and child classes can do their cleanups as well .
     */
    private void cleanScreen() {
        if (graphicOverlay != null) {
            graphicOverlay.clear();
        }
    }
}
