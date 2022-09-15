package com.huawei.hms.modeling3d.ui.activity;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.view.Surface;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.view.PreviewView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.huawei.hms.magicresource.db.TaskInfoAppDbUtils;
import com.huawei.hms.magicresource.util.Constants;
import com.huawei.hms.magicresource.view.CustomRoundAngleImageView;
import com.huawei.hms.modeling3d.R;
import com.huawei.hms.modeling3d.model.ConstantBean;
import com.huawei.hms.modeling3d.model.UserBean;
import com.huawei.hms.modeling3d.ui.adapter.RecycleBondImageAdapter;
import com.huawei.hms.modeling3d.ui.widget.ProgressCustomDialog;
import com.huawei.hms.modeling3d.utils.BaseUtils;
import com.huawei.hms.modeling3d.utils.CameraXManager;
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

public class ScanAgainActivity extends AppCompatActivity {

    @BindView(R.id.tv_photo_num)
    TextView tvPhotoNum;

    @BindView(R.id.rl_show_num)
    RelativeLayout rlShowNum;

    @BindView(R.id.iv_back)
    ImageView ivBack;

    @BindView(R.id.capture_button)
    ImageView ivCaptureButton;

    @BindView(R.id.view_finder)
    PreviewView previewView;

    @BindView(R.id.img_pic)
    CustomRoundAngleImageView imgPic;

    @BindView(R.id.rl_upload_doing)
    RelativeLayout rlUploadDoing;

    int minPhotoNum = 20;
    int maxPhotoNum = 60;

    Unbinder unbinder;
    UserBean userBean;
    int screenType = ConstantBean.SCREEN_MODEL_TYPE_ZERO;
    int currentPhotoNum = 0;
    CameraXManager xManager;
    String createTime;
    String saveInnerPath;
    String taskId;

    ProgressCustomDialog progressCustomDialog;

    private Modeling3dReconstructInitResult magic3dReconstructInitResult;
    private Modeling3dReconstructEngine magic3dReconstructEngine;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.scan_again_layout);
        unbinder = ButterKnife.bind(this);
        userBean = BaseUtils.getUser(ScanAgainActivity.this);
        screenType = userBean.getSelectResolutionModel();
        taskId = getIntent().getStringExtra("taskId");
        magic3dReconstructEngine = Modeling3dReconstructEngine.getInstance(ScanAgainActivity.this);
        initView();
        initPhotoSize();
    }

    private void initPhotoSize() {
        xManager = new CameraXManager(this, previewView, screenType);
        xManager.startCamera();
    }

    private void initView() {
        createTime = String.valueOf(System.currentTimeMillis());
    }

    @OnClick({R.id.iv_back, R.id.capture_button, R.id.rl_upload_doing})
    public void onClick(View view) {
        switch (view.getId()) {

            case R.id.iv_back:
                finish();
                break;

            case R.id.capture_button:
                saveInnerPath = new Constants(ScanAgainActivity.this).getCaptureImageFile() + "model" + createTime;
                File file = new File(saveInnerPath);
                if (!file.exists()) {
                    file.mkdir();
                }
                xManager.takePicture();

                xManager.setTakePicBack(new CameraXManager.TakePicBack() {
                    @Override
                    public void takePicBack(Bitmap bitmap) {
                        if (currentPhotoNum < maxPhotoNum) {
                            saveBitmap(bitmap);
                        }
                    }
                });
                break;
            case R.id.rl_upload_doing:
                upLoadData();
                break;
        }
    }

    private void upLoadData() {
        progressCustomDialog = new ProgressCustomDialog(ScanAgainActivity.this, ConstantBean.PROGRESS_CUSTOM_DIALOG_TYPE_ONE, getString(R.string.doing_post_text));
        progressCustomDialog.setCancelable(false);
        progressCustomDialog.show();
        progressCustomDialog.getIvCancel().setVisibility(View.GONE);
        progressCustomDialog.getIvCancelTwo().setVisibility(View.GONE);
        new Thread(new Runnable() {
            @Override
            public void run() {
                Modeling3dReconstructSetting setting = new Modeling3dReconstructSetting.Factory()
                        .setReconstructMode(Constants.RGB_MODEL)
                        .create();

                magic3dReconstructInitResult = magic3dReconstructEngine.initTask(setting);
                String taskId = magic3dReconstructInitResult.getTaskId();
                if (taskId != null) {
                    magic3dReconstructEngine.setReconstructUploadListener(uploadListener);
                    magic3dReconstructEngine.uploadFile(taskId, saveInnerPath);
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressCustomDialog.dismiss();
                            Toast.makeText(ScanAgainActivity.this, magic3dReconstructInitResult.getRetMsg(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        }).start();

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
                ScanAgainActivity.this.runOnUiThread(() -> {
                    progressCustomDialog.dismiss();
                    Toast.makeText(ScanAgainActivity.this, getString(R.string.upload_text_success), Toast.LENGTH_SHORT).show();
                    finish();
                });
            }
        }

        @Override
        public void onError(String taskId, int errorCode, String message) {
            runOnUiThread(() -> {
                progressCustomDialog.dismiss();
                Toast.makeText(ScanAgainActivity.this, message + errorCode, Toast.LENGTH_SHORT).show();
                finish();
            });

        }
    };

    private void saveBitmap(Bitmap bitmap) {
        Observable.create((Observable.OnSubscribe<File>) subscriber -> {
            File filePic;
            try {
                filePic = new File(saveInnerPath + "/" + String.format("%05d", currentPhotoNum + 1) + ".jpg");
                if (!filePic.exists()) {
                    filePic.getParentFile().mkdirs();
                    filePic.createNewFile();
                }
                FileOutputStream fos = new FileOutputStream(filePic);
                Bitmap newBitmap = adjustPhotoRotation(bitmap, getDisplayRotation(ScanAgainActivity.this));
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

    private void showCompressionResult(File mFile) {
        rlShowNum.setVisibility(View.VISIBLE);
        currentPhotoNum += 1;

        imgPic.setImageBitmap(BitmapFactory.decodeFile(mFile.getAbsolutePath()));
        tvPhotoNum.setText(String.valueOf(currentPhotoNum));

        if (currentPhotoNum >= minPhotoNum && currentPhotoNum <= maxPhotoNum) {
            rlUploadDoing.setVisibility(View.VISIBLE);
        } else {
            rlUploadDoing.setVisibility(View.GONE);
        }
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

}
