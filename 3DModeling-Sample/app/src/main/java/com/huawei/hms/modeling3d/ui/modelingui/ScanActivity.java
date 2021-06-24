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

package com.huawei.hms.modeling3d.ui.modelingui;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.huawei.cameratakelib.CameraTakeManager;
import com.huawei.cameratakelib.listener.CameraTakeListener;
import com.huawei.cameratakelib.utils.LogUtil;
import com.huawei.hms.modelingresource.db.TaskInfoAppDb;
import com.huawei.hms.modelingresource.db.TaskInfoAppDbUtils;
import com.huawei.hms.modelingresource.util.Constants;
import com.huawei.hms.modelingresource.view.CustomRoundAngleImageView;
import com.huawei.hms.modelingresource.view.ProgressCustomDialog;
import com.huawei.hms.modelingresource.view.UploadDialog;
import com.huawei.hms.modeling3d.R;
import com.huawei.hms.objreconstructsdk.Modeling3dReconstructConstants;
import com.huawei.hms.objreconstructsdk.cloud.Modeling3dReconstructEngine;
import com.huawei.hms.objreconstructsdk.cloud.Modeling3dReconstructInitResult;
import com.huawei.hms.objreconstructsdk.cloud.Modeling3dReconstructSetting;
import com.huawei.hms.objreconstructsdk.cloud.Modeling3dReconstructUploadListener;
import com.huawei.hms.objreconstructsdk.cloud.Modeling3dReconstructUploadResult;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class ScanActivity extends AppCompatActivity implements UploadDialog.OnItemSureClickListener {

    private static final String TAG = ScanActivity.class.getSimpleName();

    Unbinder unbinder;
    private Context mContext;

    HandlerThread thread = new HandlerThread("upload");
    Handler upLoadHandle;

    @BindView(R.id.surfaceview)
    SurfaceView previewView;
    @BindView(R.id.img_pic)
    CustomRoundAngleImageView imgPic;
    @BindView(R.id.tv_pic_dir)
    TextView tvPicDir;
    @BindView(R.id.rl_scan_doing)
    RelativeLayout rlScanDoing;
    @BindView(R.id.tv_sure)
    TextView tvSure;
    @BindView(R.id.rl_top_tips)
    RelativeLayout rlTopTips;
    @BindView(R.id.tv_photo_num)
    TextView tvPhotoNum;
    @BindView(R.id.iv_status)
    ImageView ivStatus;
    @BindView(R.id.tv_scan)
    TextView tvScan;
    @BindView(R.id.rl_show_num)
    RelativeLayout rlShowNum;
    @BindView(R.id.rl_toast)
    RelativeLayout rlToast;
    @BindView(R.id.rl_rebuild_model)
    RelativeLayout rlRebuildModel;

    private Modeling3dReconstructEngine modeling3dReconstructEngine;
    private Modeling3dReconstructInitResult modeling3dReconstructInitResult;

    CameraTakeManager manager;

    private Timer mTimer;
    private TimerTask timerTask;
    private int currentPhotoNum = 0;
    private int minPhotoNum = 20;
    private final static int PERIOD = 500;
    ProgressCustomDialog progressCustomDialog;
    String time;

    boolean isUpload = false;
    boolean isPause = false;

    // Re-establishing the upload callback listener
    private final Modeling3dReconstructUploadListener uploadListener = new Modeling3dReconstructUploadListener() {
        @Override
        public void onUploadProgress(String taskId, double progress, Object ext) {
            // Upload progress
        }

        @Override
        public void onResult(String taskId, Modeling3dReconstructUploadResult result, Object ext) {
            if (result.isComplete()) {
                isUpload = true;
                ScanActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressCustomDialog.dismiss();
                        Toast.makeText(ScanActivity.this, getString(R.string.upload_text_success), Toast.LENGTH_SHORT).show();
                    }
                });
                TaskInfoAppDbUtils.updateTaskIdAndStatusByPath(new Constants(ScanActivity.this).getCaptureImageFile() + manager.getSurfaceViewCallback().getCreateTime(), taskId, 1);
            }
        }

        @Override
        public void onError(String taskId, int errorCode, String message) {
            isUpload = false;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    progressCustomDialog.dismiss();
                    Toast.makeText(ScanActivity.this, "Upload failed." + message, Toast.LENGTH_SHORT).show();
                    LogUtil.e("taskid" + taskId + "errorCode: " + errorCode + " errorMessage: " + message);
                }
            });

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);
        unbinder = ButterKnife.bind(this);
        thread.start();
        upLoadHandle = new Handler(thread.getLooper());
        mContext = this;
        // Initializing the Rebuild Engine
        modeling3dReconstructEngine = Modeling3dReconstructEngine.getInstance(mContext);
        time = String.valueOf(System.currentTimeMillis());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        init();
    }

    private void init() {
        manager = new CameraTakeManager(this, previewView, new CameraTakeListener() {
            @Override
            public void onSuccess(File bitmapFile, Bitmap mBitmap) {
                rlShowNum.setVisibility(View.VISIBLE);
                imgPic.setImageBitmap(mBitmap);
                currentPhotoNum++;
                tvPhotoNum.setText(String.valueOf(currentPhotoNum));
                ivStatus.setVisibility(View.GONE);
                tvScan.setText(R.string.upload_text);
                rlToast.setVisibility(View.VISIBLE);
                rlTopTips.setVisibility(View.GONE);
                if (currentPhotoNum >= minPhotoNum) {
                    rlScanDoing.setEnabled(true);
                } else {
                    if (isPause) {
                        isPause = false;
                    } else {
                        rlScanDoing.setEnabled(false);
                    }
                }
            }

            @Override
            public void onFail(String error) {
                LogUtil.e(error);
            }
        });

        rlScanDoing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doClickScan();
            }
        });

        rlRebuildModel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentPhotoNum = 0;
                isUpload = false;
                time = String.valueOf(System.currentTimeMillis());
                rlScanDoing.performClick();
                rlRebuildModel.setVisibility(View.GONE);
                rlToast.setVisibility(View.VISIBLE);
            }
        });
    }


    public void upLoadData() {
        tvScan.setText(R.string.upload_text);
        if (!isUpload) {
            rlToast.setVisibility(View.GONE);
            UploadDialog dialog = new UploadDialog(ScanActivity.this);
            dialog.setListener(ScanActivity.this);
            dialog.show();
            dialog.setCanceledOnTouchOutside(false);
            if (!TaskInfoAppDbUtils.isExistedPath(new Constants(ScanActivity.this).getCaptureImageFile() + manager.getSurfaceViewCallback().getCreateTime())) {
                TaskInfoAppDb taskInfoDb = new TaskInfoAppDb();
                taskInfoDb.setStatus(Modeling3dReconstructConstants.ProgressStatus.INITED);
                taskInfoDb.setCreateTime(System.currentTimeMillis());
                taskInfoDb.setIsDownload(0);
                taskInfoDb.setFileUploadPath(new Constants(ScanActivity.this).getCaptureImageFile() + manager.getSurfaceViewCallback().getCreateTime());
                taskInfoDb.setModelType(getString(R.string.rgb));
                TaskInfoAppDbUtils.insert(taskInfoDb);
            }
        } else {
            Toast.makeText(ScanActivity.this, "Do not upload it again.", Toast.LENGTH_SHORT).show();
        }
        rlRebuildModel.setVisibility(View.VISIBLE);
    }


    @OnClick({R.id.tv_sure, R.id.iv_close, R.id.iv_set})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_sure:
                rlTopTips.setVisibility(View.GONE);
                break;
            case R.id.iv_close:
                finish();
                break;
            case R.id.iv_set:
                Intent intent = new Intent("SettingModelActivity");
                intent.putExtra("model", ScanActivity.this.getResources().getString(R.string.rgb));
                startActivity(intent);
                break;
            default:
        }
    }


    public void doClickScan() {
        manager.getSurfaceViewCallback().setCreateTime(time);
        if (currentPhotoNum >= minPhotoNum) {
            if (mTimer != null) {
                mTimer.cancel();
                mTimer = null;
            }
            upLoadData();
        } else {
            mTimer = new Timer();
            timerTask = new TimerTask() {
                @Override
                public void run() {
                    LogUtil.i("Change The take value");
                    manager.takePhoto();
                }
            };
            mTimer.schedule(timerTask, 0, PERIOD);
            manager.getSurfaceViewCallback().setIndex(currentPhotoNum);
            rlScanDoing.setEnabled(false);
            ivStatus.setVisibility(View.GONE);
            tvScan.setText(R.string.upload_text);
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        manager.destroy();
        if (mTimer != null) {
            mTimer.cancel();
            timerTask.cancel();
            timerTask = null;
            mTimer = null;
        }
        // Turn off the rebuild engine
        modeling3dReconstructEngine.close();
    }

    @Override
    public void onResume() {
        init();
        super.onResume();
    }

    @Override
    public void onPause() {
        manager.getSurfaceViewCallback().onPause();
        if (mTimer != null) {
            mTimer.cancel();
            timerTask.cancel();
            timerTask = null;
            mTimer = null;
        }
        isPause = true;
        rlScanDoing.setEnabled(true);
        super.onPause();
    }

    @Override
    public void onClick() {
        rlScanDoing.setEnabled(true);
        progressCustomDialog = new ProgressCustomDialog(ScanActivity.this, ProgressCustomDialog.PROGRESS_WITH_CIRCLE, getString(R.string.doing_post_text));
        progressCustomDialog.show();
        progressCustomDialog.setCanceledOnTouchOutside(false);
        new Thread(new Runnable() {
            @Override
            public void run() {
                // Initializing the RGB Mode
                Modeling3dReconstructSetting setting = new Modeling3dReconstructSetting.Factory()
                        .setReconstructMode(Modeling3dReconstructConstants.ReconstructMode.PICTURE)
                        .create();
                Log.i(TAG, "Obtain the 3D reconstruction mode.: " + setting.getReconstructMode());
                String filePath = new Constants(ScanActivity.this).getCaptureImageFile() + manager.getSurfaceViewCallback().getCreateTime();
                // Creating a Rebuilding Task
                modeling3dReconstructInitResult = modeling3dReconstructEngine.initTask(setting);
                // Creating a Rebuilding Task
                String taskId = modeling3dReconstructInitResult.getTaskId();
                // Check whether taskId is empty.
                if (taskId == null || taskId.equals("")) {
                    LogUtil.i("get taskID error " + modeling3dReconstructInitResult.getRetMsg());
                } else {
                    // Check whether taskId is empty.
                    modeling3dReconstructEngine.setReconstructUploadListener(uploadListener);
                    // Executing a Rebuild Upload Task
                    modeling3dReconstructEngine.uploadFile(taskId, filePath);
                }

            }
        }).start();
    }

}

