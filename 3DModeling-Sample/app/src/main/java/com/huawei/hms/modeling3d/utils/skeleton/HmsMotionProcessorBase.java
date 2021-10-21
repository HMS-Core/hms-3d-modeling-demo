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
package com.huawei.hms.modeling3d.utils.skeleton;

import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.util.SparseArray;

import com.huawei.hmf.tasks.OnFailureListener;
import com.huawei.hmf.tasks.OnSuccessListener;
import com.huawei.hmf.tasks.Task;
import com.huawei.hms.modeling3d.model.FrameMetadata;
import com.huawei.hms.modeling3d.ui.activity.ChooserActivity;
import com.huawei.hms.modeling3d.ui.widget.BoneGLSurfaceView;
import com.huawei.hms.modeling3d.utils.BitmapUtils;
import com.huawei.hms.motioncapturesdk.Modeling3dFrame;
import com.huawei.hms.motioncapturesdk.Modeling3dMotionCaptureSkeleton;

import java.nio.ByteBuffer;

public abstract class HmsMotionProcessorBase<T> implements HmsMotionImageProcessor {

    // To keep the latest images and its metadata.
    private ByteBuffer latestImage;

    private FrameMetadata latestImageMetaData;

    // To keep the images and metadata in process.
    private ByteBuffer processingImage;

    private FrameMetadata processingMetaData;
    private boolean isStop = false;

    @Override
    public void process(ByteBuffer data, final FrameMetadata frameMetadata, final BoneGLSurfaceView graphicOverlay) {
        latestImage = data;
        latestImageMetaData = frameMetadata;
        if (processingImage == null && processingMetaData == null) {
            processLatestImage(graphicOverlay);
        }
    }

    @Override
    public void process(Bitmap bitmap, final BoneGLSurfaceView
            graphicOverlay) {
        Modeling3dFrame frame = Modeling3dFrame.fromBitmap(bitmap);
        detectInVisionImage(null /* bitmap */, frame, null,
                graphicOverlay);
    }

    private void processLatestImage(final BoneGLSurfaceView graphicOverlay) {
        processingImage = latestImage;
        processingMetaData = latestImageMetaData;
        latestImage = null;
        latestImageMetaData = null;
        if (processingImage != null && processingMetaData != null) {
            processImage(processingImage, processingMetaData, graphicOverlay);
        }
    }

    private void processImage(ByteBuffer data, final FrameMetadata frameMetadata, final BoneGLSurfaceView graphicOverlay) {
        int quadant = frameMetadata.getRotation();
        Modeling3dFrame.Property property = new Modeling3dFrame.Property.Creator().setFormatType(ImageFormat.NV21)
                .setWidth(frameMetadata.getWidth())
                .setHeight(frameMetadata.getHeight())
                .setQuadrant(quadant)
                .create();

        if (ChooserActivity.isAsynchronous()) {
            detectInVisionImage(null, Modeling3dFrame.fromByteBuffer(data, property), frameMetadata, graphicOverlay);
        } else {
            Bitmap bitmap = BitmapUtils.getBitmap(data, frameMetadata);
            detectInVisionImage(bitmap, Modeling3dFrame.fromByteBuffer(data, property), frameMetadata, graphicOverlay);
        }
    }

    private void detectInVisionImage(final Bitmap originalCameraImage, Modeling3dFrame frame, final FrameMetadata metadata, final BoneGLSurfaceView graphicOverlay) {
        if (isStop) {
            return;
        }
        if (ChooserActivity.isAsynchronous()) {

            detectInImage(frame)
                    .addOnSuccessListener(
                            new OnSuccessListener<T>() {
                                @Override
                                public void onSuccess(T results) {
                                    HmsMotionProcessorBase.this.onSuccess(originalCameraImage, results,
                                            metadata,
                                            graphicOverlay);
                                    processLatestImage(graphicOverlay);
                                }
                            })
                    .addOnFailureListener(
                            new OnFailureListener() {
                                @Override
                                public void onFailure(Exception e) {
                                    HmsMotionProcessorBase.this.onFailure(e);
                                }
                            });
        }else {
            detectInImageSynchronize(frame) ;
        }
    }

    @Override
    public void stop() {
        isStop = true;
    }

    protected abstract Task<T> detectInImage(Modeling3dFrame frame);

    protected abstract SparseArray<Modeling3dMotionCaptureSkeleton> detectInImageSynchronize(Modeling3dFrame frame);

    protected abstract void onSuccess(
            Bitmap originalCameraImage,
            T results,
            FrameMetadata frameMetadata,
            BoneGLSurfaceView graphicOverlay);

    protected abstract void onFailure(Exception e);
}
