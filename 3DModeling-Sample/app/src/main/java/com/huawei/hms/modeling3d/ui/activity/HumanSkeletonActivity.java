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
package com.huawei.hms.modeling3d.ui.activity;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.hardware.Camera;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat.OnRequestPermissionsResultCallback;

import com.huawei.hms.modeling3d.R;
import com.huawei.hms.modeling3d.ui.widget.BoneGLSurfaceView;
import com.huawei.hms.modeling3d.ui.widget.CameraSource;
import com.huawei.hms.modeling3d.ui.widget.CameraSourcePreview;
import com.huawei.hms.modeling3d.ui.widget.MyConfigChooser;
import com.huawei.hms.modeling3d.ui.widget.SwitchButtonView;
import com.huawei.hms.modeling3d.utils.BitmapUtils;
import com.huawei.hms.modeling3d.utils.LocalDataManager;
import com.huawei.hms.modeling3d.utils.skeleton.LocalSkeletonProcessor;

import java.io.IOException;
import java.lang.ref.WeakReference;

/**
 * Human Skeleton Scan main page
 *
 */
public final class HumanSkeletonActivity extends AppCompatActivity
        implements OnRequestPermissionsResultCallback, CompoundButton.OnCheckedChangeListener, View.OnClickListener {

    private static final String TAG = "HumanSkeletonActivity";

    private CameraSource cameraSource = null;

    private CameraSourcePreview preview;

    private BoneGLSurfaceView boneRenderManager;
    LocalSkeletonProcessor localSkeletonProcessor;

    GLSurfaceView glSurfaceView;

    private RelativeLayout glLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e(TAG, "onCreate");
        setContentView(R.layout.activity_human_skeleton);
        ChooserActivity.setIsAsynchronous(true);
        preview = findViewById(R.id.firePreview);
        glLayout = findViewById(R.id.rl_add_surface);
        findViewById(R.id.back).setOnClickListener(this);
        localSkeletonProcessor = new LocalSkeletonProcessor();
        ToggleButton facingSwitch = findViewById(R.id.facingSwitch);
        facingSwitch.setOnCheckedChangeListener(this);
        if (Camera.getNumberOfCameras() == 1) {
            facingSwitch.setVisibility(View.GONE);
        }

        glSurfaceView = new GLSurfaceView(this);
        boneRenderManager = new BoneGLSurfaceView();
        glSurfaceView.setPreserveEGLContextOnPause(true);
        glSurfaceView.setEGLConfigChooser(new MyConfigChooser());
        glSurfaceView.setEGLContextClientVersion(3);
        glSurfaceView.setRenderer(boneRenderManager);
        glLayout.addView(glSurfaceView);

        createCameraSource();
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.back) {
            finish();
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        Log.d(TAG, "Set facing");
        if (cameraSource != null) {
            if (isChecked) {
                cameraSource.setFacing(CameraSource.CAMERA_FACING_FRONT);
                localSkeletonProcessor.getDetector();
            } else {
                cameraSource.setFacing(CameraSource.CAMERA_FACING_BACK);
                localSkeletonProcessor.getDetector();
            }
        }
        preview.stop();
        startCameraSource();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        preview.stop();
        createCameraSource();
        startCameraSource();
    }

    private void createCameraSource() {
        // If there's no existing cameraSource, create one.
        if (cameraSource == null) {
            cameraSource = new CameraSource(this, boneRenderManager);
        }
        cameraSource.setMachineLearningFrameProcessor(localSkeletonProcessor);
    }

    private void startCameraSource() {
        if (cameraSource != null) {
            try {
                cameraSource.setRequestedPreviewWidth(CameraSource.WIDTH_SIZE);
                cameraSource.setRequestedPreviewHeight(CameraSource.HEIGHT_SIZE);
                preview.start(cameraSource, glSurfaceView);
            } catch (IOException e) {
                Log.e(TAG, "Unable to start camera source.", e);
                cameraSource.release();
                cameraSource = null;
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        startCameraSource();
    }

    @Override
    protected void onStop() {
        super.onStop();
        preview.stop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (cameraSource != null) {
            cameraSource.release();
        }

        if (localSkeletonProcessor != null) {
            localSkeletonProcessor.stop();
        }

        ChooserActivity.setIsAsynchronous(true);
    }

}
