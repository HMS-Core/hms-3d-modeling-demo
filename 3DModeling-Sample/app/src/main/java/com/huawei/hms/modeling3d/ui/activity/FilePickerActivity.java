package com.huawei.hms.modeling3d.ui.activity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.huawei.hms.magicresource.db.TaskInfoAppDb;
import com.huawei.hms.magicresource.db.TaskInfoAppDbUtils;
import com.huawei.hms.magicresource.materialdb.TaskInfoMaterialAppDb;
import com.huawei.hms.magicresource.materialdb.TaskInfoMaterialAppDbUtils;
import com.huawei.hms.magicresource.util.Constants;
import com.huawei.hms.materialgeneratesdk.cloud.Modeling3dTextureEngine;
import com.huawei.hms.materialgeneratesdk.cloud.Modeling3dTextureInitResult;
import com.huawei.hms.materialgeneratesdk.cloud.Modeling3dTextureSetting;
import com.huawei.hms.materialgeneratesdk.cloud.Modeling3dTextureUploadListener;
import com.huawei.hms.materialgeneratesdk.cloud.Modeling3dTextureUploadResult;
import com.huawei.hms.modeling3d.R;
import com.huawei.hms.modeling3d.model.ConstantBean;
import com.huawei.hms.modeling3d.ui.adapter.FileAdapter;
import com.huawei.hms.modeling3d.ui.widget.PreviewConfigDialog;
import com.huawei.hms.modeling3d.ui.widget.ProgressCustomDialog;
import com.huawei.hms.modeling3d.utils.ToastUtil;
import com.huawei.hms.objreconstructsdk.cloud.Modeling3dReconstructEngine;
import com.huawei.hms.objreconstructsdk.cloud.Modeling3dReconstructInitResult;
import com.huawei.hms.objreconstructsdk.cloud.Modeling3dReconstructSetting;
import com.huawei.hms.objreconstructsdk.cloud.Modeling3dReconstructUploadListener;
import com.huawei.hms.objreconstructsdk.cloud.Modeling3dReconstructUploadResult;

import java.io.File;
import java.util.ArrayList;
import java.util.Objects;

import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class FilePickerActivity extends AppCompatActivity implements ProgressCustomDialog.OnItemCancelClickListener {

    private RecyclerView mRecyclerView;
    private FileAdapter mAdapter;
    String fileType;
    TextView tvTitle;
    ProgressCustomDialog progressCustomDialog;

    private String filePath;

    private Modeling3dReconstructEngine magic3dReconstructEngine;
    private Modeling3dReconstructInitResult magic3dReconstructInitResult;

    private Modeling3dTextureEngine modeling3dTextureEngine;
    private Modeling3dTextureInitResult modeling3dTextureInitResult;

    private String modelTaskId;
    private String materialTaskId;

    private long lastClickTime = 0L;
    // The interval between two clicks cannot be less than 300ms
    private static final int FAST_CLICK_DELAY_TIME = 500;

    ArrayList<String> dataAllFile = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_file_picker);
        magic3dReconstructEngine = Modeling3dReconstructEngine.getInstance(FilePickerActivity.this);
        modeling3dTextureEngine = Modeling3dTextureEngine.getInstance(FilePickerActivity.this);
        fileType = getIntent().getStringExtra("fileType");
        filePath = getIntent().getStringExtra("path");
        initView();
    }

    private void initView() {

        findViewById(R.id.iv_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        tvTitle = findViewById(R.id.tv_title);
        mRecyclerView = findViewById(R.id.file_recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setItemAnimator(null);
        String path;
        if (filePath == null) {
            path = Environment.getExternalStorageDirectory().getAbsolutePath();
        } else {
            path = filePath;
            findViewById(R.id.tv_show_tips).setVisibility(View.GONE);
            findViewById(R.id.ll_do_upload).setVisibility(View.GONE);
            findViewById(R.id.tv_copy).setVisibility(View.VISIBLE);
        }
        tvTitle.setText(path);
        mAdapter = new FileAdapter(this, path);
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.setOnDirChangeListener(currentDirectory -> tvTitle.setText(currentDirectory.getAbsolutePath()));
        findViewById(R.id.ll_do_upload).setOnClickListener(view -> {
            if (mAdapter.getChoosePaths() != null) {

                if (mAdapter.getChoosePaths().size() > 1) {
                    ToastUtil.showToast(FilePickerActivity.this, "Only supports single folder upload, please select again");
                } else {
                    ArrayList<String> data = mAdapter.getChoosePaths();
                    String dataFilePath = data.get(0);
                    if (System.currentTimeMillis() - lastClickTime >= FAST_CLICK_DELAY_TIME) {
                        File file = new File(dataFilePath);
                        if (fileType.equals(ConstantBean.UPLOAD_TO_BUILD_MODELS)) {
                            if (Objects.requireNonNull(file.list()).length >= 20) {
                                uploadModel(dataFilePath);
                            } else {
                                ToastUtil.showToast(FilePickerActivity.this, "More than 20 pictures in the model folder");
                            }
                        } else if (fileType.equals(ConstantBean.UPLOAD_TO_BUILD_MATERIALS)) {
                            File[] files = file.listFiles();
                            if (files != null && files.length > 0) {
                                for (File value : files) {
                                    if (value.getPath().contains("jpg")|| value.getPath().contains("png") || value.getPath().contains("Webp")) {
                                        uploadMaterial(dataFilePath);
                                        break;
                                    }else {
                                        ToastUtil.showToast(FilePickerActivity.this, "At least 1 picture is required under the material folder");
                                        break;
                                    }
                                }
                            }
                        }
                        lastClickTime = System.currentTimeMillis();
                    }
                }
            } else {
                ToastUtil.showToast(FilePickerActivity.this, "Please select a folder before uploading");
            }
        });

        findViewById(R.id.tv_copy).setOnClickListener(view -> {
            ClipboardManager cmb = (ClipboardManager) FilePickerActivity.this.getSystemService(Context.CLIPBOARD_SERVICE);
            String[] strings = path.split("/");
            ClipData clipDataSet = ClipData.newPlainText(null, strings[strings.length - 1]);
            cmb.setPrimaryClip(clipDataSet);
            ToastUtil.showToast(FilePickerActivity.this, "Copy is successful, go to the file manager to paste");
        });
    }

    private void chooseCancel() {
        setResult(RESULT_CANCELED, null);
        finish();
    }


    @Override
    public void onBackPressed() {
        mAdapter.quitMode();
        if (!mAdapter.isRootDir()) {
            mAdapter.backParent();
            return;
        }
        chooseCancel();
    }


    int allCount ;
    int currentCount ;

    public void uploadModel(String saveInnerPath) {
        PreviewConfigDialog dialog = new PreviewConfigDialog(FilePickerActivity.this);
        dialog.show();
        dialog.setCanceledOnTouchOutside(false);
        dialog.getTvCancel().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressCustomDialog = new ProgressCustomDialog(FilePickerActivity.this, ConstantBean.PROGRESS_CUSTOM_DIALOG_TYPE_ONE, getString(R.string.doing_post_text));
                progressCustomDialog.show();
                progressCustomDialog.setListener(FilePickerActivity.this);
                progressCustomDialog.setCanceledOnTouchOutside(false);
                initModeTask(dialog.getTextureMode(),saveInnerPath);
                dialog.dismiss();
            }
        });
    }

    public void initModeTask(Integer textureMode,String saveInnerPath){
        Observable.create((Observable.OnSubscribe<Modeling3dReconstructInitResult>) subscriber -> {
            Modeling3dReconstructSetting setting = new Modeling3dReconstructSetting.Factory()
                    .setReconstructMode(Constants.RGB_MODEL)
                    .setTextureMode(textureMode)
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
                                ToastUtil.showToast(FilePickerActivity.this, getString(R.string.upload_text_success));
                                saveModelData(saveInnerPath);

                            });
                        }
                    }

                    @Override
                    public void onError(String s, int i, String message) {
                        runOnUiThread(() -> {
                            progressCustomDialog.dismiss();
                            ToastUtil.showToast(FilePickerActivity.this, message);
                            saveModelData(saveInnerPath);
                        });
                    }
                });
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
                Toast.makeText(FilePickerActivity.this, result.getRetMsg(), Toast.LENGTH_SHORT).show();
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


    private void uploadMaterial(String savePath) {
        progressCustomDialog = new ProgressCustomDialog(FilePickerActivity.this, ConstantBean.PROGRESS_CUSTOM_DIALOG_TYPE_ONE, getString(R.string.doing_post_text));
        progressCustomDialog.show();
        progressCustomDialog.setListener(this);
        progressCustomDialog.setCanceledOnTouchOutside(false);

        Observable.create((Observable.OnSubscribe<Modeling3dTextureInitResult>) subscriber -> {
            Modeling3dTextureSetting setting = new Modeling3dTextureSetting.Factory()
                    .setTextureMode(1)
                    .create();
            modeling3dTextureInitResult = modeling3dTextureEngine.initTask(setting);
            String taskId = modeling3dTextureInitResult.getTaskId();
            if (taskId != null && !taskId.equals("")) {
                materialTaskId = taskId;
                modeling3dTextureEngine.setTextureUploadListener(new Modeling3dTextureUploadListener() {
                    @Override
                    public void onUploadProgress(String s, double v, Object o) {
                        progressCustomDialog.setCurrentProgress(v);
                    }

                    @Override
                    public void onResult(String s, Modeling3dTextureUploadResult modeling3dTextureUploadResult, Object o) {
                        if (modeling3dTextureUploadResult.isComplete()) {
                            runOnUiThread(() -> {
                                saveMaterial(savePath);
                                Toast.makeText(getApplicationContext(), getString(R.string.upload_text_success), Toast.LENGTH_SHORT).show();
                                progressCustomDialog.dismiss();
                            });
                        }
                    }

                    @Override
                    public void onError(String s, int errorCode, String message) {
                        runOnUiThread(() -> {
                            progressCustomDialog.dismiss();
                            ToastUtil.showToast(FilePickerActivity.this, message + "" + errorCode);
                        });
                    }
                });
                modeling3dTextureEngine.asyncUploadFile(taskId, savePath);
            } else {
                subscriber.onNext(modeling3dTextureInitResult);
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Subscriber<Modeling3dTextureInitResult>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(Modeling3dTextureInitResult modeling3dTextureInitResult) {
                progressCustomDialog.dismiss();
                Toast.makeText(FilePickerActivity.this, modeling3dTextureInitResult.getRetMsg(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void saveMaterial(String savePath) {
        TaskInfoMaterialAppDb taskInfoDb = new TaskInfoMaterialAppDb();
        taskInfoDb.setStatus(ConstantBean.MATERIAL_RECONSTRUCT_START_STATUS);
        taskInfoDb.setTaskId(materialTaskId);
        taskInfoDb.setCreateTime(System.currentTimeMillis());
        taskInfoDb.setIsDownload(0);
        taskInfoDb.setFileUploadPath(savePath);
        TaskInfoMaterialAppDbUtils.insert(taskInfoDb);
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
                        Toast.makeText(FilePickerActivity.this, "Cancel failed.", Toast.LENGTH_SHORT).show();
                    } else if (result == 0) {
                        Toast.makeText(FilePickerActivity.this, "Canceled successfully.", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

        if (materialTaskId != null) {
            Observable.create((Observable.OnSubscribe<Integer>) subscriber -> {
                int result = modeling3dTextureEngine.cancelUpload(materialTaskId);
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
                        Toast.makeText(FilePickerActivity.this, "Cancel failed.", Toast.LENGTH_SHORT).show();
                    } else if (result == 0) {
                        Toast.makeText(FilePickerActivity.this, "Canceled successfully.", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }
}
