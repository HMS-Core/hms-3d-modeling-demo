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
 * 人体骨骼扫描主页面
 *
 */
public final class HumanSkeletonActivity extends AppCompatActivity
        implements OnRequestPermissionsResultCallback, CompoundButton.OnCheckedChangeListener, View.OnClickListener {

    /**
     * 自动拍照
     */
    public static final int AUTO_TAKE_PHOTO = 101;

    /**
     * 刷新置信度显示界面
     */
    public static final int UPDATE_SCORES_VIEW = LocalSkeletonProcessor.UPDATE_SCORES_VIEW;

    /**
     * 刷新相似度显示界面
     */
    public static final int UPDATE_SIMILARITY_VIEW = 102;

    private static final String TAG = "HumanSkeletonActivity";

    private static boolean isOpenStatus = false;

    private CameraSource cameraSource = null;

    private CameraSourcePreview preview;

    // 显示置信度数据
    private TextView infoTxtView;

    private SwitchButtonView switchButton;

    private Button selectTemplate;

    private Button modifyThreshold;

    private Handler mHandler = new MsgHandler(this);

    private Bitmap bitmap;

    private Bitmap bitmapCopy;

    private RelativeLayout zoomImageLayout;

    private ImageView zoomImageView;

    private TextView similarityTv;

    private RelativeLayout similarityImageview;

    private BoneGLSurfaceView boneRenderManager;
    TextView tvStopPreview;

    LocalSkeletonProcessor localSkeletonProcessor;

    GLSurfaceView glSurfaceView;

    private RelativeLayout glLayout;

    private static class MsgHandler extends Handler {
        WeakReference<HumanSkeletonActivity> mMainActivityWeakReference;

        MsgHandler(HumanSkeletonActivity mainActivity) {
            mMainActivityWeakReference = new WeakReference<>(mainActivity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            HumanSkeletonActivity mainActivity = mMainActivityWeakReference.get();
            if (mainActivity == null) {
                return;
            }
            Log.d(TAG, "msg what :" + msg.what);
            switch (msg.what) {
                case AUTO_TAKE_PHOTO:
                    if (mainActivity.zoomImageLayout.getVisibility() == View.VISIBLE) {
                        return;
                    }
                    mainActivity.takePicture();
                    break;
                case UPDATE_SIMILARITY_VIEW:
                    Bundle bundle = msg.getData();
                    float result = bundle.getFloat("similarity");
                    mainActivity.similarityTv.setVisibility(View.VISIBLE);
                    mainActivity.similarityTv.setText("similarity:" + (int) (result * 100) + "%  ");
                    break;
                case UPDATE_SCORES_VIEW:
                    String infoStr = (msg.obj == null) ? null : msg.obj.toString();
                    mainActivity.infoTxtView.setText(infoStr);
                    if (infoStr == null || infoStr.isEmpty()) {
                        mainActivity.infoTxtView.setVisibility(View.GONE);
                    } else {
                        mainActivity.infoTxtView.setVisibility(View.VISIBLE);
                    }
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e(TAG, "onCreate");
        setContentView(R.layout.activity_human_skeleton);
        ChooserActivity.setIsAsynchronous(true);
        preview = findViewById(R.id.firePreview);
        similarityTv = findViewById(R.id.tv_similarity);
        similarityImageview = findViewById(R.id.similarity_imageview);
        zoomImageLayout = findViewById(R.id.zoomImageLayout);
        zoomImageView = findViewById(R.id.take_picture_overlay);
        selectTemplate = findViewById(R.id.select_template);
        switchButton = findViewById(R.id.switch_button_view);
        tvStopPreview = findViewById(R.id.tv_stop_preview);
        glLayout = findViewById(R.id.rl_add_surface);
        switchButton.setOnToggleStateChangeListener(new SwitchButtonView.OnToggleStateChangeListener() {
            @Override
            public void onToggleStateChange(boolean isOpen) {
                if (isOpen) {
                    isOpenStatus = true;
                    similarityImageview.setVisibility(View.VISIBLE);
                    modifyThreshold.setVisibility(View.VISIBLE);

                } else {
                    isOpenStatus = false;
                    similarityImageview.setVisibility(View.GONE);
                    modifyThreshold.setVisibility(View.GONE);
                }
            }
        });
        findViewById(R.id.zoomImageClose).setOnClickListener(this);
        findViewById(R.id.zoomImageSave).setOnClickListener(this);
        findViewById(R.id.back).setOnClickListener(this);
        modifyThreshold = findViewById(R.id.threshold_mod);
        modifyThreshold.setOnClickListener(this);
        selectTemplate.setOnClickListener(this);
        localSkeletonProcessor = new LocalSkeletonProcessor(HumanSkeletonActivity.this);
        infoTxtView = findViewById(R.id.live_info_txt);
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
        if (view.getId() == R.id.zoomImageClose) {
            zoomImageLayout.setVisibility(View.GONE);
            recycleBitmap();
            startCameraSource();
        } else if (view.getId() == R.id.zoomImageSave) {
            BitmapUtils.saveToAlbum(bitmapCopy, getApplicationContext());
            zoomImageLayout.setVisibility(View.GONE);
            recycleBitmap();
            startCameraSource();
            Toast.makeText(this, "Save success", Toast.LENGTH_SHORT).show();
        } else if (view.getId() == R.id.back) {
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

        isOpenStatus = false;
        ChooserActivity.setIsAsynchronous(true);
    }

    private void takePicture() {
        preview.stop();
        mHandler.removeMessages(AUTO_TAKE_PHOTO);
        zoomImageLayout.setVisibility(View.VISIBLE);
        LocalDataManager localDataManager = new LocalDataManager();
        localDataManager.setLandScape(false);
        bitmap = BitmapUtils.getBitmap(localSkeletonProcessor.getProcessingImage(),
                localSkeletonProcessor.getFrameMetadata());

        float previewWidth = localDataManager.getImageMaxWidth(localSkeletonProcessor.getFrameMetadata());
        float previewHeight = localDataManager.getImageMaxHeight(localSkeletonProcessor.getFrameMetadata());
        bitmapCopy = Bitmap.createBitmap(bitmap).copy(Bitmap.Config.ARGB_8888, true);

        Canvas canvas = new Canvas(bitmapCopy);
        float min = Math.min(previewWidth, previewHeight);
        float max = Math.max(previewWidth, previewHeight);
        setBitmapBorder(canvas);
        localDataManager.setCameraInfo(glSurfaceView, canvas, min, max);
        zoomImageView.setImageBitmap(bitmapCopy);
    }

    private void setBitmapBorder(Canvas canvas) {
        Paint paint = new Paint();

        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.STROKE);

        paint.setStrokeWidth(15);
        Rect rect = canvas.getClipBounds();
        canvas.drawRect(rect, paint);
    }

    private void recycleBitmap() {
        if (bitmap != null && !bitmap.isRecycled()) {
            bitmap.recycle();
            bitmap = null;
        }
        if (bitmapCopy != null && !bitmapCopy.isRecycled()) {
            bitmapCopy.recycle();
            bitmapCopy = null;
        }
    }

    @Override
    public void onBackPressed() {
        if (zoomImageLayout.getVisibility() == View.VISIBLE) {
            zoomImageLayout.setVisibility(View.GONE);
            recycleBitmap();
            startCameraSource();
        } else {
            super.onBackPressed();
        }
    }
}
