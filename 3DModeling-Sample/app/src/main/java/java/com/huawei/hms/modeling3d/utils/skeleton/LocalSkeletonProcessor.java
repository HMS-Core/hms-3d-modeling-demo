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

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.util.SparseArray;

import com.huawei.hmf.tasks.Task;

import com.huawei.hms.modeling3d.model.FrameMetadata;
import com.huawei.hms.modeling3d.model.SkeletonSaveBean;
import com.huawei.hms.modeling3d.ui.widget.BoneGLSurfaceView;
import com.huawei.hms.modeling3d.utils.FilterUtils;
import com.huawei.hms.motioncapturesdk.Modeling3dFrame;
import com.huawei.hms.motioncapturesdk.Modeling3dMotionCaptureEngine;
import com.huawei.hms.motioncapturesdk.Modeling3dMotionCaptureEngineFactory;
import com.huawei.hms.motioncapturesdk.Modeling3dMotionCaptureEngineSetting;
import com.huawei.hms.motioncapturesdk.Modeling3dMotionCaptureSkeleton;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class LocalSkeletonProcessor extends HmsMotionProcessorBase<List<Modeling3dMotionCaptureSkeleton>> {
    private static final String TAG = "Skeleton";
    public static final int UPDATE_SCORES_VIEW = 1010;

    private Modeling3dMotionCaptureEngine detector;

    private FrameMetadata mFrameMetadata;

    private ByteBuffer latestImage;

    SkeletonSaveBean saveBean = new SkeletonSaveBean();
    List<List<List<Float>>> joints3d = new ArrayList<>();
    List<List<List<Float>>> quaternion = new ArrayList<>();
    List<List<Float>> shift = new ArrayList<>();

    Context mContext ;

    /**
     * Construction method
     */
    public LocalSkeletonProcessor() {
        Modeling3dMotionCaptureEngineSetting setting = new Modeling3dMotionCaptureEngineSetting.Factory()
                .setAnalyzeType(Modeling3dMotionCaptureEngineSetting.TYPE_3DSKELETON_QUATERNION
                        | Modeling3dMotionCaptureEngineSetting.TYPE_3DSKELETON)
                .create();
        detector = Modeling3dMotionCaptureEngineFactory.getInstance().getMotionCaptureEngine(setting);
    }


    @Override
    public void stop() {
        super.stop();
        try {
            detector.stop();
        } catch (IOException e) {
            Log.e(TAG, "Exception thrown while trying to close cloud image labeler!", e);
        }
    }

    public Modeling3dMotionCaptureEngine getDetector() {
        return detector;
    }

    @Override
    public Task<List<Modeling3dMotionCaptureSkeleton>> detectInImage(Modeling3dFrame frame) {
        latestImage = frame.getByteBuffer();
        return detector.asyncAnalyseFrame(frame);
    }

    @Override
    public SparseArray<Modeling3dMotionCaptureSkeleton> detectInImageSynchronize(Modeling3dFrame frame) {
        latestImage = frame.getByteBuffer();
        return detector.analyseFrame(frame);
    }


    public ByteBuffer getProcessingImage() {
        return latestImage;
    }

    public FrameMetadata getFrameMetadata() {
        return mFrameMetadata;
    }

    @Override
    protected void onSuccess(Bitmap originalCameraImage, List<Modeling3dMotionCaptureSkeleton> skeletons, FrameMetadata frameMetadata, BoneGLSurfaceView graphicOverlay) {
        mFrameMetadata = frameMetadata;
        if (skeletons.size() > 0) {
            for (int i = 0; i < skeletons.size(); i++) {
                joints3d.add(FilterUtils.filterDataJoints3ds(skeletons.get(i)));
                quaternion.add(FilterUtils.filterDataQuaternions(skeletons.get(i)));
                shift.add(FilterUtils.filterDataJointShift(skeletons.get(i)));
            }
            saveBean.setTrans(shift);
            saveBean.setQuaternion(quaternion);
            saveBean.setJoints3d(joints3d);

            graphicOverlay.setData(joints3d.get(joints3d.size() - 1), shift.get(shift.size() - 1));
        }

        if (skeletons == null || skeletons.isEmpty()) {
            graphicOverlay.setData(null, null);
        }
    }
    @Override
    protected void onFailure(Exception e) {
        Log.e(TAG, "skeleton detection failed " + e);
    }
}