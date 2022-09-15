/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 */

package com.huawei.hms.modeling3d.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.huawei.hms.magicresource.db.TaskInfoAppDb;
import com.huawei.hms.magicresource.db.TaskInfoAppDbUtils;
import com.huawei.hms.magicresource.util.Constants;

import com.huawei.hms.modeling3d.R;
import com.huawei.hms.modeling3d.model.ConstantBean;
import com.huawei.hms.modeling3d.model.UserBean;
import com.huawei.hms.modeling3d.ui.widget.ProgressCustomDialog;
import com.huawei.hms.modeling3d.ui.widget.ScanBottomDialog;
import com.huawei.hms.modeling3d.ui.widget.ScanResolutionBottomDialog;
import com.huawei.hms.modeling3d.ui.widget.UploadDialog;
import com.huawei.hms.modeling3d.ui.widget.UploadSlamDialog;
import com.huawei.hms.modeling3d.utils.BaseUtils;
import com.huawei.hms.modeling3d.utils.LogUtil;
import com.huawei.hms.modeling3d.utils.ToastUtil;
import com.huawei.hms.modeling3dcapturesdk.Modeling3dCaptureImageEngine;
import com.huawei.hms.modeling3dcapturesdk.Modeling3dCaptureImageListener;
import com.huawei.hms.modeling3dcapturesdk.Modeling3dCaptureSetting;
import com.huawei.hms.motioncapturesdk.Modeling3dMotionCaptureEngineSetting;
import com.huawei.hms.objreconstructsdk.cloud.Modeling3dReconstructEngine;
import com.huawei.hms.objreconstructsdk.cloud.Modeling3dReconstructInitResult;
import com.huawei.hms.objreconstructsdk.cloud.Modeling3dReconstructSetting;
import com.huawei.hms.objreconstructsdk.cloud.Modeling3dReconstructTaskUtils;
import com.huawei.hms.objreconstructsdk.cloud.Modeling3dReconstructUploadListener;
import com.huawei.hms.objreconstructsdk.cloud.Modeling3dReconstructUploadResult;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * SettingModelActivity
 *
 * @author HW
 * @since 2020-09-18
 */
public class SettingModelActivity extends AppCompatActivity implements ScanBottomDialog.SelectModelClick, ProgressCustomDialog.OnItemCancelClickListener {

    @BindView(R.id.iv_back)
    ImageView ivBack;


    @BindView(R.id.rl_shooting_mode)
    RelativeLayout rlShootingMode;

    @BindView(R.id.tv_shooting_mode)
    TextView tvShootingMode;

    private String model;
    private String scanModel;

    int modelScreen;
    String rgbModel;

    UserBean userBean;

    Handler upLoadHandle;
    Modeling3dReconstructEngine magic3dReconstructEngine;
    private Modeling3dReconstructInitResult magic3dReconstructInitResult;
    private String modelTaskId;
    Modeling3dReconstructTaskUtils magic3dReconstructTaskUtils;


    public void getModel(int i) {
        modelScreen = i;
    }

    public void getScanModel(String model) {
        scanModel = model;
        tvShootingMode.setText(getScnStr(scanModel));
    }

    private String getScnStr(String modelScreen) {
        userBean.setSelectScanModel(modelScreen);

        if (modelScreen.equals(ConstantBean.SCAN_MODEL_TYPE_TWO)) {
            return getString(R.string.continuous_shooting_text);
        } else if (modelScreen.equals(ConstantBean.SCAN_MODEL_TYPE_ONE)) {
            return getString(R.string.click_shooting_text);
        }
        return null;
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setting_model_activity);
        ButterKnife.bind(this);
        userBean = BaseUtils.getUser(SettingModelActivity.this);
        if (userBean == null) {
            userBean = new UserBean();
        }
        model = userBean.getSelectBuildModel();
        modelScreen = userBean.getSelectResolutionModel();
        scanModel = userBean.getSelectScanModel();
        tvShootingMode.setText(getScnStr(scanModel));
    }


    private final Modeling3dReconstructUploadListener uploadListener = new Modeling3dReconstructUploadListener() {
        @Override
        public void onUploadProgress(String taskId, double progress, Object ext) {
            LogUtil.e(taskId + "UploadProgress" + "<------>" + progress);
        }

        @Override
        public void onResult(String taskId, Modeling3dReconstructUploadResult result, Object ext) {
            LogUtil.e(taskId + "UploadSuccess" + "<------>" + result.isComplete());
            if (result.isComplete()) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(SettingModelActivity.this, "UploadSuccess", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }

        @Override
        public void onError(String taskId, int errorCode, String message) {
            LogUtil.e(taskId + "<------>" + errorCode + message);
        }
    };


    @OnClick({R.id.iv_back, R.id.rl_shooting_mode, R.id.rl_scan_model,})
    public void onViewClicked(View view) {
        ScanResolutionBottomDialog bottomDialog;
        switch (view.getId()) {

            case R.id.rl_scan_model:
                ScanBottomDialog dialog = new ScanBottomDialog(this, this, SettingModelActivity.this.getResources().getString(R.string.rgb));
                dialog.show();
                break;

            case R.id.iv_back:
                changePage();
                break;

            case R.id.rl_shooting_mode:
                bottomDialog = new ScanResolutionBottomDialog(SettingModelActivity.this, modelScreen, scanModel);
                bottomDialog.show();
                break;

        }

    }

    @Override
    public void modelClick(String clickModel) {
        userBean.setSelectBuildModel(clickModel);
        model = clickModel;
        if (model.equals(SettingModelActivity.this.getResources().getString(R.string.slam))) {
            openSlam();
        }
    }

    private void openSlam() {


        Modeling3dCaptureImageEngine captureImageEngine = Modeling3dCaptureImageEngine.getInstance();
        Modeling3dCaptureSetting setting = new Modeling3dCaptureSetting.Factory()
                .setAzimuthNum(30)
                .setLatitudeNum(3)
                .setRadius(2)
                .create();
        captureImageEngine.setCaptureConfig(setting);

        String savePath = new Constants(SettingModelActivity.this).getCaptureImageFile() + System.currentTimeMillis();

        captureImageEngine.captureImage(savePath, SettingModelActivity.this, new Modeling3dCaptureImageListener(){
            @Override
            public void onResult() {
                UploadSlamDialog dialog = new UploadSlamDialog(SettingModelActivity.this);
                dialog.show();
                dialog.setCanceledOnTouchOutside(false);

                dialog.getTvSure().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        uploadFile(savePath);
                        dialog.dismiss();
                    }
                });

                dialog.getTvCancel().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                    }
                });

            }

            @Override
            public void onProgress(int progress) {

            }

            @Override
            public void onError(int i, String s) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(SettingModelActivity.this,s,Toast.LENGTH_SHORT).show();
                    }
                });
            }

        });
    }

    private void uploadFile(String savePath) {
        ProgressCustomDialog progressCustomDialog = new ProgressCustomDialog(SettingModelActivity.this, ConstantBean.PROGRESS_CUSTOM_DIALOG_TYPE_ONE, getString(R.string.doing_post_text));
        progressCustomDialog.show();
        progressCustomDialog.setListener(SettingModelActivity.this);
        progressCustomDialog.setCanceledOnTouchOutside(false);
        magic3dReconstructEngine = Modeling3dReconstructEngine.getInstance(SettingModelActivity.this);
        magic3dReconstructTaskUtils = Modeling3dReconstructTaskUtils.getInstance(SettingModelActivity.this);
        Observable.create((Observable.OnSubscribe<Modeling3dReconstructInitResult>) subscriber -> {
            Modeling3dReconstructSetting setting = null;
            setting = new Modeling3dReconstructSetting.Factory()
                    .setReconstructMode(Constants.RGB_MODEL)
                    .setTextureMode(1)
                    .create();
            magic3dReconstructInitResult = magic3dReconstructEngine.initTask(setting);
            String taskId = magic3dReconstructInitResult.getTaskId();
            if (taskId == null || taskId.equals("")) {
                subscriber.onNext(magic3dReconstructInitResult);
            } else {
                modelTaskId = taskId;
                magic3dReconstructEngine.setReconstructUploadListener(new Modeling3dReconstructUploadListener() {
                    @Override
                    public void onUploadProgress(String s, double v, Object o) {
                        progressCustomDialog.setCurrentProgress(v);
                    }

                    @Override
                    public void onResult(String s, Modeling3dReconstructUploadResult modeling3dReconstructUploadResult, Object o) {
                        if (modeling3dReconstructUploadResult.isComplete()) {
                            runOnUiThread(() -> {
                                progressCustomDialog.dismiss();
                                ToastUtil.showToast(SettingModelActivity.this, getString(R.string.upload_text_success));
                                saveModelData(savePath);

                            });
                        }
                    }

                    @Override
                    public void onError(String s, int i, String message) {
                        runOnUiThread(() -> {
                            progressCustomDialog.dismiss();
                            ToastUtil.showToast(SettingModelActivity.this, message);
                            saveModelData(savePath);
                        });
                    }
                });
                magic3dReconstructEngine.uploadFile(taskId, savePath);
            }
            subscriber.onCompleted();
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<Modeling3dReconstructInitResult>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(Modeling3dReconstructInitResult result) {
                progressCustomDialog.dismiss();
                Toast.makeText(SettingModelActivity.this, result.getRetMsg()+result.getRetCode(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveModelData(String saveInnerPath) {
        TaskInfoAppDb taskInfoDb = new TaskInfoAppDb();
        taskInfoDb.setStatus(ConstantBean.MODELS_UPLOAD_COMPLETED_STATUS);
        taskInfoDb.setCreateTime(System.currentTimeMillis());
        taskInfoDb.setIsDownload(0);
        taskInfoDb.setFileUploadPath(saveInnerPath);
        taskInfoDb.setModelType(getString(R.string.rgb));
        taskInfoDb.setTaskId(modelTaskId);
        TaskInfoAppDbUtils.insert(taskInfoDb);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            changePage();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        changePage();
    }

    public void changePage() {

        userBean.setSelectBuildModel(getString(R.string.rgb));
        try {
            BaseUtils.saveUser(SettingModelActivity.this, userBean);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Intent intent = new Intent(SettingModelActivity.this, NewScanActivity.class);
        startActivity(intent);

        finish();
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);

    }


    public void getRgbModel(String normalMode) {
        rgbModel = normalMode;
        userBean.setSelectRGBMode(normalMode);
    }

    @Override
    public void onCancel(TaskInfoAppDb appDb) {
        if (modelTaskId != null) {
            Observable.create((Observable.OnSubscribe<Integer>) subscriber -> {
                int result = magic3dReconstructEngine.cancelUpload(modelTaskId);
                subscriber.onNext(result);
            }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<Integer>() {
                @Override
                public void onCompleted() {

                }

                @Override
                public void onError(Throwable e) {

                }

                @Override
                public void onNext(Integer result) {
                    if (result == 1) {
                        Toast.makeText(SettingModelActivity.this, "Cancel failed.", Toast.LENGTH_SHORT).show();
                    } else if (result == 0) {
                        Toast.makeText(SettingModelActivity.this, "Canceled successfully.", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }
}
