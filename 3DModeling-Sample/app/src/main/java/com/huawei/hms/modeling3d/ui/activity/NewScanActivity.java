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

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioAttributes;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.Surface;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.view.PreviewView;
import androidx.core.content.res.ResourcesCompat;

import com.huawei.hms.magicresource.db.DatabaseAppUtils;
import com.huawei.hms.magicresource.db.TaskInfoAppDb;
import com.huawei.hms.magicresource.db.TaskInfoAppDbUtils;
import com.huawei.hms.magicresource.util.Constants;
import com.huawei.hms.magicresource.view.CustomRoundAngleImageView;
import com.huawei.hms.modeling3d.R;
import com.huawei.hms.modeling3d.model.ConstantBean;
import com.huawei.hms.modeling3d.model.UserBean;
import com.huawei.hms.modeling3d.ui.widget.ProgressCustomDialog;
import com.huawei.hms.modeling3d.ui.widget.UploadDialog;
import com.huawei.hms.modeling3d.utils.BaseUtils;
import com.huawei.hms.modeling3d.utils.CameraXManager;
import com.huawei.hms.modeling3d.utils.ToastUtil;
import com.huawei.hms.objreconstructsdk.cloud.Modeling3dReconstructEngine;
import com.huawei.hms.objreconstructsdk.cloud.Modeling3dReconstructInitResult;
import com.huawei.hms.objreconstructsdk.cloud.Modeling3dReconstructSetting;
import com.huawei.hms.objreconstructsdk.cloud.Modeling3dReconstructUploadListener;
import com.huawei.hms.objreconstructsdk.cloud.Modeling3dReconstructUploadResult;

import java.io.File;
import java.io.FileOutputStream;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * @Description: Scan page
 * @Since: 2021-04-28
 */
public class NewScanActivity extends AppCompatActivity implements UploadDialog.OnItemSureClickListener, ProgressCustomDialog.OnItemCancelClickListener, UploadDialog.OnItemCancelClickListener, SensorEventListener {
    private static final int UPDATE_INTERVAL = 100;
    private long mLastUpdateTime;
    private float mLastX;
    private float mLastY;
    private float mLastZ;
    private int shakeThreshold = 1000;

    @BindView(R.id.img_pic)
    CustomRoundAngleImageView imgPic;

    @BindView(R.id.rl_top_tips)
    RelativeLayout rlTopTips;
    @BindView(R.id.tv_photo_num)
    TextView tvPhotoNum;

    @BindView(R.id.rl_show_num)
    RelativeLayout rlShowNum;


    @BindView(R.id.iv_back)
    ImageView ivBack;

    @BindView(R.id.capture_button)
    ImageView ivCaptureButton;

    @BindView(R.id.rl_upload_doing)
    RelativeLayout rlUploadDoing;

    @BindView(R.id.view_finder)
    PreviewView previewView;

    @BindView(R.id.tv_show_steps)
    TextView tvShowSteps;

    @BindView(R.id.ll_show_steps)
    LinearLayout llShowSteps;

    @BindView(R.id.rl_parent)
    RelativeLayout rlParent;

    @BindView(R.id.tv_upload)
    TextView tvUpload;


    private Modeling3dReconstructInitResult magic3dReconstructInitResult;
    private Modeling3dReconstructEngine magic3dReconstructEngine;
    ProgressCustomDialog progressCustomDialog;

    int screenType = ConstantBean.SCREEN_MODEL_TYPE_ONE;
    String scanModel = ConstantBean.SCAN_MODEL_TYPE_TWO;
    Unbinder unbinder;
    String createTime;
    String saveInnerPath;
    private String allTaskId;
    boolean isPause = false;
    Vibrator vibrator;
    int currentPhotoNum = 0;
    int minPhotoNum = 20;
    int maxPhotoNum = 200;
    Integer continuousShootingInterval;

    private long lastClickTime = 0L;
    private static final int FAST_CLICK_DELAY_TIME = 1000;

    CameraXManager xManager;
    Handler handler = new Handler();
    TakeCallBack takeCallBack = new TakeCallBack();
    private SensorManager sensorManager;
    private Sensor sensor;

    UserBean userBean;
    File pauseLastFile;

    int rgbMode = Constants.RGB_MODEL;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        userBean = BaseUtils.getUser(NewScanActivity.this);
        setContentView(R.layout.new_scan_layout);
        unbinder = ButterKnife.bind(this);
        initView();
        initPhotoSize();

    }

    private void initView() {
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
        window.setAttributes(lp);
        window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        magic3dReconstructEngine = Modeling3dReconstructEngine.getInstance(NewScanActivity.this);
        ivBack.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.icon_close_icon, null));
        createTime = String.valueOf(System.currentTimeMillis());
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ALL);
        vibrator = (Vibrator) getSystemService(Service.VIBRATOR_SERVICE);
        showInitUi();
    }

    void showInitUi() {
        if (userBean != null && userBean.getSelectScanModel() != null && userBean.getSelectResolutionModel() != null) {
            scanModel = userBean.getSelectScanModel();
            screenType = userBean.getSelectResolutionModel();
            if (userBean.getShowScanTips()) {
                rlTopTips.setVisibility(View.VISIBLE);
            } else {
                rlTopTips.setVisibility(View.GONE);
            }
            continuousShootingInterval = 150;

        }
    }

    void initPhotoSize() {
        xManager = new CameraXManager(this, previewView, screenType);
        xManager.startCamera();
    }


    @OnClick({R.id.tv_sure, R.id.iv_back, R.id.rl_upload_doing, R.id.capture_button})
    public void onClick(View view) {
        switch (view.getId()) {

            case R.id.tv_sure:
                userBean.setShowScanTips(false);
                try {
                    BaseUtils.saveUser(NewScanActivity.this, userBean);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                rlTopTips.setVisibility(View.GONE);
                break;
            case R.id.iv_back:
                saveData();
                finish();
                break;

            case R.id.rl_upload_doing:
                upLoadData();
                break;
            case R.id.capture_button:
                saveInnerPath = new Constants(NewScanActivity.this).getCaptureImageFile() + "model" + createTime;
                File file = new File(saveInnerPath);
                if (!file.exists()) {
                    file.mkdir();
                }
                if (scanModel.equals(ConstantBean.SCAN_MODEL_TYPE_TWO)) {
                    if (!isPause) {
                        initTakePhoto(scanModel);
                        isPause = true;
                        ivCaptureButton.setImageResource(R.drawable.zanting_icon);
                    } else {
                        onCapturePause();
                    }
                } else {
                    if (System.currentTimeMillis() - lastClickTime >= FAST_CLICK_DELAY_TIME) {
                        initTakePhoto(scanModel);
                        lastClickTime = System.currentTimeMillis();
                    }
                }
                break;
            default:
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (sensorManager != null) {
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (scanModel.equals(ConstantBean.SCAN_MODEL_TYPE_TWO)) {
            onCapturePause();
        }
        xManager.cameraDestroy();
        if (sensorManager != null) {
            sensorManager.unregisterListener(this, sensor);
        }
    }

    private void initTakePhoto(String scanModel) {

        if (scanModel.equals(ConstantBean.SCAN_MODEL_TYPE_TWO)) {
            handler.removeCallbacks(takeCallBack);
            if (currentPhotoNum == 0) {
                handler.post(takeCallBack);
            } else {
                handler.postDelayed(takeCallBack, continuousShootingInterval);
            }
        } else {
            doCapture();
        }

    }

    private void doCapture() {

        xManager.takePicture();
        xManager.setTakePicBack(bitmap -> saveBitmap(bitmap));

    }

    public void saveBitmap(Bitmap mBitmap) {

        Observable.create((Observable.OnSubscribe<File>) subscriber -> {
            if (scanModel.equals(ConstantBean.SCAN_MODEL_TYPE_TWO)) {
                if (!isPause) {
                    return;
                }
            }
            File filePic;
            try {
                filePic = new File(saveInnerPath + "/" + System.currentTimeMillis() + ".jpg");
                if (!filePic.exists()) {
                    filePic.getParentFile().mkdirs();
                    filePic.createNewFile();
                }
                FileOutputStream fos = new FileOutputStream(filePic);
                Bitmap newBitmap = adjustPhotoRotation(mBitmap, getDisplayRotation(NewScanActivity.this));
                newBitmap.compress(Bitmap.CompressFormat.JPEG, 95, fos);
                fos.flush();
                fos.close();
                newBitmap.recycle();
                subscriber.onNext(filePic);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<File>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(File result) {
                showCompressionResult(result);
            }
        });

    }

    public Bitmap adjustPhotoRotation(Bitmap bm, final int orientationDegree) {
        Matrix m = new Matrix();
        m.setRotate(orientationDegree, (float) bm.getWidth() / 2, (float) bm.getHeight() / 2);
        try {
            return Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), m, true);
        } catch (OutOfMemoryError ex) {
            ex.fillInStackTrace();
        }
        return bm;
    }


    /**
     * Get the current screen rotation angle
     *
     * @param activity Current activity
     * @return 0 means vertical screen; 90 means left horizontal screen; 180 means reverse vertical screen; 270 means right horizontal screen
     */
    public int getDisplayRotation(Activity activity) {
        if (activity == null) {
            return 0;
        }

        int rotation = activity.getWindowManager().getDefaultDisplay()
                .getRotation();
        switch (rotation) {
            case Surface.ROTATION_0:
                return 0;
            case Surface.ROTATION_90:
                return 270;
            case Surface.ROTATION_180:
                return 180;
            case Surface.ROTATION_270:
                return 90;
        }
        return 0;
    }

    public void showCompressionResult(File mFile) {
        if (scanModel.equals(ConstantBean.SCAN_MODEL_TYPE_TWO)) {
            handler.removeCallbacks(takeCallBack);
            handler.postDelayed(takeCallBack, continuousShootingInterval);
        }

        rlShowNum.setVisibility(View.VISIBLE);
        currentPhotoNum += 1;

        imgPic.setImageBitmap(BitmapFactory.decodeFile(mFile.getAbsolutePath()));
        tvPhotoNum.setText(String.valueOf(currentPhotoNum));
        if (currentPhotoNum < minPhotoNum) {
            rlUploadDoing.setVisibility(View.GONE);
        } else {
            rlUploadDoing.setVisibility(View.VISIBLE);
        }
        pauseLastFile = mFile;
    }


    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    private class TakeCallBack implements Runnable {
        @Override
        public void run() {
            if (currentPhotoNum < maxPhotoNum) {
                doCapture();
            }
        }
    }

    public void upLoadData() {
        UploadDialog dialog = new UploadDialog(NewScanActivity.this, currentPhotoNum);
        dialog.setListener(NewScanActivity.this);
        dialog.setCancelListener(NewScanActivity.this);
        dialog.show();
        dialog.setCanceledOnTouchOutside(false);
        if (!TaskInfoAppDbUtils.isExistedPath(saveInnerPath)) {
            TaskInfoAppDb taskInfoDb = new TaskInfoAppDb();
            taskInfoDb.setStatus(ConstantBean.MODELS_INIT_STATUS);
            taskInfoDb.setCreateTime(System.currentTimeMillis());
            taskInfoDb.setIsDownload(0);
            taskInfoDb.setFileUploadPath(saveInnerPath);
            taskInfoDb.setModelType(String.valueOf(rgbMode));
            taskInfoDb.setTaskId(allTaskId);
            long status = TaskInfoAppDbUtils.insert(taskInfoDb);
            if (status == -1) {
                DatabaseAppUtils.initDatabase(this);
                saveData();
            }
        }
        onCapturePause();
    }

    private void clearImage() {
        currentPhotoNum = 0;
        createTime = String.valueOf(System.currentTimeMillis());
        rlShowNum.setVisibility(View.GONE);
        rlUploadDoing.setVisibility(View.GONE);
    }

    @Override
    public void onCancelClick(boolean isAgainCapture) {
        if (isAgainCapture) {
            clearImage();
        }
    }

    @Override
    public void onCancel(TaskInfoAppDb appDb) {
        clearImage();
        Observable.create((Observable.OnSubscribe<Integer>) subscriber -> {
            int result = magic3dReconstructEngine.cancelUpload(allTaskId);
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
                    Toast.makeText(NewScanActivity.this, "Cancel failed.", Toast.LENGTH_SHORT).show();
                } else if (result == 0) {
                    Toast.makeText(NewScanActivity.this, "Canceled successfully.", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    @Override
    public void onClick() {
        progressCustomDialog = new ProgressCustomDialog(NewScanActivity.this, ConstantBean.PROGRESS_CUSTOM_DIALOG_TYPE_ONE, getString(R.string.doing_post_text));
        progressCustomDialog.show();
        progressCustomDialog.setListener(NewScanActivity.this);
        progressCustomDialog.setCanceledOnTouchOutside(false);
        Observable.create((Observable.OnSubscribe<Modeling3dReconstructInitResult>) subscriber -> {
            Modeling3dReconstructSetting setting = new Modeling3dReconstructSetting.Factory()
                    .setReconstructMode(rgbMode)
                    .create();
            magic3dReconstructInitResult = magic3dReconstructEngine.initTask(setting);
            String taskId = magic3dReconstructInitResult.getTaskId();
            if (taskId == null || taskId.equals("")) {
                subscriber.onNext(magic3dReconstructInitResult);
            } else {
                allTaskId = taskId;
                magic3dReconstructEngine.setReconstructUploadListener(uploadListener);
                magic3dReconstructEngine.uploadFile(taskId, saveInnerPath);
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
                Toast.makeText(NewScanActivity.this, result.getRetMsg(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private final Modeling3dReconstructUploadListener uploadListener = new Modeling3dReconstructUploadListener() {
        @Override
        public void onUploadProgress(String taskId, double progress, Object ext) {

            progressCustomDialog.setCurrentProgress(progress);
        }

        @Override
        public void onResult(String taskId, Modeling3dReconstructUploadResult result, Object ext) {
            if (result.isComplete()) {
                TaskInfoAppDbUtils.updateTaskIdAndStatusByPath(saveInnerPath, taskId, 1);
                NewScanActivity.this.runOnUiThread(() -> {
                    clearImage();
                    if (userBean.getSelectRGBMode() != null && userBean.getSelectRGBMode().equals(ConstantBean.NORMAL_MODE)) {
                        if (ivCaptureButton != null) {
                            ivCaptureButton.setVisibility(View.VISIBLE);
                        }
                    }
                    progressCustomDialog.dismiss();
                    Toast.makeText(NewScanActivity.this, getString(R.string.upload_text_success), Toast.LENGTH_SHORT).show();
                });
            }
        }

        @Override
        public void onError(String taskId, int errorCode, String message) {
            runOnUiThread(() -> {
                clearImage();
                progressCustomDialog.dismiss();
                Toast.makeText(NewScanActivity.this, message + errorCode, Toast.LENGTH_SHORT).show();
            });

        }
    };

    public void saveData() {
        if (currentPhotoNum >= minPhotoNum && currentPhotoNum <= maxPhotoNum) {
            if (!TaskInfoAppDbUtils.isExistedPath(saveInnerPath)) {
                TaskInfoAppDb taskInfoDb = new TaskInfoAppDb();
                taskInfoDb.setStatus(ConstantBean.MODELS_INIT_STATUS);
                taskInfoDb.setCreateTime(System.currentTimeMillis());
                taskInfoDb.setIsDownload(0);
                taskInfoDb.setFileUploadPath(saveInnerPath);
                taskInfoDb.setModelType(String.valueOf(rgbMode));
                taskInfoDb.setTaskId(allTaskId);
                long status = TaskInfoAppDbUtils.insert(taskInfoDb);
                if (status == -1) {
                    DatabaseAppUtils.initDatabase(this);
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        magic3dReconstructEngine.close();
        xManager.cameraDestroy();
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        initPhotoSize();
        onCapturePause();
        super.onConfigurationChanged(newConfig);
    }

    public void onCapturePause() {
        ivCaptureButton.setImageResource(R.drawable.capture_photo_icon);
        isPause = false;
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        saveData();
    }
}
