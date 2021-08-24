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
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.huawei.cameratakelib.CameraTakeManager;
import com.huawei.cameratakelib.SurfaceViewCallback;
import com.huawei.cameratakelib.listener.CameraTakeListener;
import com.huawei.cameratakelib.utils.LogUtil;
import com.huawei.hms.magicresource.db.TaskInfoAppDb;
import com.huawei.hms.magicresource.materialdb.TaskInfoMaterialAppDb;
import com.huawei.hms.magicresource.materialdb.TaskInfoMaterialAppDbUtils;
import com.huawei.hms.magicresource.util.Constants;
import com.huawei.hms.magicresource.view.ResizeAbleSurfaceView;
import com.huawei.hms.materialgeneratesdk.cloud.Modeling3dTextureEngine;
import com.huawei.hms.materialgeneratesdk.cloud.Modeling3dTextureInitResult;
import com.huawei.hms.materialgeneratesdk.cloud.Modeling3dTextureSetting;
import com.huawei.hms.materialgeneratesdk.cloud.Modeling3dTextureUploadListener;
import com.huawei.hms.materialgeneratesdk.cloud.Modeling3dTextureUploadResult;
import com.huawei.hms.modeling3d.Modeling3dApp;
import com.huawei.hms.modeling3d.R;
import com.huawei.hms.modeling3d.model.ConstantBean;
import com.huawei.hms.modeling3d.model.UserBean;
import com.huawei.hms.modeling3d.ui.adapter.RecycleImageAdapter;
import com.huawei.hms.modeling3d.ui.widget.ProgressCustomDialog;
import com.huawei.hms.modeling3d.utils.BaseUtils;


import java.io.File;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * @Description: Create material
 * @Since: 2021-04-28
 */
public class CaptureMaterialActivity extends AppCompatActivity implements RecycleImageAdapter.OnItemClickListener, ProgressCustomDialog.OnItemCancelClickListener {

    @BindView(R.id.iv_back)
    ImageView ivBack;

    @BindView(R.id.tv_title)
    TextView tvTitle;

    @BindView(R.id.rl_top)
    RelativeLayout rlTop;

    @BindView(R.id.rl_mid)
    RelativeLayout rlMid;

    @BindView(R.id.iv_capture)
    ImageView ivCapture;

    @BindView(R.id.surfaceView)
    ResizeAbleSurfaceView surfaceView;

    @BindView(R.id.tv_tip)
    TextView tvTip;

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

    @BindView(R.id.tv_upload)
    TextView tvUpload;

    @BindView(R.id.ll_image)
    LinearLayout llImage;

    @BindView(R.id.rl_toast)
    RelativeLayout rlToast;

    @BindView(R.id.rl_top_tips)
    RelativeLayout rlTopTips;

    private Unbinder unbinder;
    private CameraTakeManager cameraTakeManager;
    private int currentPhotoNum = 0;
    private int maxPhotoNum = 5;
    private int index;
    private String createTime;
    private RecycleImageAdapter adapter;
    private ArrayList<String> imagePaths = new ArrayList<>();

    private Modeling3dTextureEngine modeling3dTextureEngine;
    private Modeling3dTextureInitResult modeling3dTextureInitResult;
    private Modeling3dTextureSetting setting;
    ProgressCustomDialog progressCustomDialog;

    private boolean isTakePhoto = false;
    private long lastClickTime = 0;
    private String globalTaskId;

    UserBean userBean ;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_capture_material);
        unbinder = ButterKnife.bind(this, this);
        initView();
        init();
    }

    private void initView() {
        ivBack.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.icon_close_icon, null));
        tvTitle.setVisibility(View.GONE);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(RecyclerView.HORIZONTAL);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new RecycleImageAdapter(imagePaths, this);
        adapter.setOnItemClickListener(this);
        recyclerView.setAdapter(adapter);
        userBean = BaseUtils.getUser(CaptureMaterialActivity.this);
        if (userBean!=null) {
            if (userBean.getShowMaterialTips()){
                rlTopTips.setVisibility(View.VISIBLE);
            }else {
                rlTopTips.setVisibility(View.GONE);
            }
        }
    }

    private void init() {
        modeling3dTextureEngine = Modeling3dTextureEngine.getInstance(Modeling3dApp.app);
        setting = new Modeling3dTextureSetting.Factory()
                .setTextureMode(1)
                .create();
    }

    private void initCamera() {
        cameraTakeManager = new CameraTakeManager(this, surfaceView, new CameraTakeListener() {
            @Override
            public void onSuccess(File bitmapFile, Bitmap mBitmap) {
                rlToast.setVisibility(View.GONE);
                tvTip.setVisibility(View.VISIBLE);
                llImage.setVisibility(View.VISIBLE);
                LogUtil.i("onSuccess path = " + bitmapFile.getPath());
                currentPhotoNum++;
                index++;
                imagePaths.add(bitmapFile.getPath());
                adapter.notifyDataSetChanged();
                recyclerView.scrollToPosition(adapter.getItemCount() - 1);
                isTakePhoto = false;
            }

            @Override
            public void onFail(String error) {
                LogUtil.i("onFail error = " + error);
                Toast.makeText(CaptureMaterialActivity.this, error, Toast.LENGTH_LONG).show();
                isTakePhoto = false;
            }
        }, SurfaceViewCallback.MATERIAL_MODEL);
        if (createTime != null && createTime.length() > 0) {
            cameraTakeManager.getSurfaceViewCallback().setCreateTime(createTime);
            cameraTakeManager.getSurfaceViewCallback().setIndex(index);
        }
    }

    @OnClick({R.id.iv_back, R.id.iv_capture, R.id.tv_upload,R.id.tv_sure})
    void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.iv_back:
                finish();
                break;
            case R.id.iv_capture:
                synchronized (CaptureMaterialActivity.this) {
                    long time = System.currentTimeMillis();
                    if (time - lastClickTime > 1000) {
                        lastClickTime = time;
                        takePhoto();
                    }
                }
                break;
            case R.id.tv_upload:
                upLoadMaterial();
                break;

            case R.id.tv_sure:
                userBean.setShowMaterialTips(false);
                try {
                    BaseUtils.saveUser(CaptureMaterialActivity.this,userBean);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                rlTopTips.setVisibility(View.GONE);
                break;
        }
    }

    private void takePhoto() {
        LogUtil.i("iv_capture currentPhotoNum = " + currentPhotoNum);
        if (!isTakePhoto) {
            if (createTime == null || createTime.length() <= 0) {
                createTime = String.valueOf(System.currentTimeMillis());
                index = 0;
                cameraTakeManager.getSurfaceViewCallback().setCreateTime(createTime);
                cameraTakeManager.getSurfaceViewCallback().setIndex(index);
            }
            if (currentPhotoNum < maxPhotoNum) {
                int top = (int) rlMid.getY();
                int width = rlMid.getWidth();
                cameraTakeManager.takePhoto(top, width);
                isTakePhoto = true;
            } else {
                LogUtil.i("Excessive number of photos");
            }
        }
    }

    private void upLoadMaterial() {
        progressCustomDialog = new ProgressCustomDialog(CaptureMaterialActivity.this, ConstantBean.PROGRESS_CUSTOM_DIALOG_TYPE_ONE, getString(R.string.doing_post_text));
        progressCustomDialog.show();
        progressCustomDialog.setListener(CaptureMaterialActivity.this);
        progressCustomDialog.setCanceledOnTouchOutside(false);

        Observable.create((Observable.OnSubscribe<Modeling3dTextureInitResult>) subscriber -> {
            modeling3dTextureInitResult = modeling3dTextureEngine.initTask(setting);
            String taskId = modeling3dTextureInitResult.getTaskId();
            String filePath = new Constants(CaptureMaterialActivity.this).getCaptureMaterialImageFile() + createTime + "/";
            if (taskId != null && !taskId.equals("")) {
                globalTaskId = taskId;
                modeling3dTextureEngine.setTextureUploadListener(uploadListener);
                modeling3dTextureEngine.asyncUploadFile(taskId, filePath);
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
                Toast.makeText(CaptureMaterialActivity.this, modeling3dTextureInitResult.getRetMsg(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void clearImage() {
        createTime = null;
        currentPhotoNum = 0;
        index = 0;
        imagePaths.clear();
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        initCamera();
    }

    @Override
    public void onResume() {
        initCamera();
        super.onResume();
    }

    @Override
    public void onPause() {
        cameraTakeManager.getSurfaceViewCallback().onPause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
        cameraTakeManager.destroy();
    }

    @Override
    public void onClick(View parent, int position) {
        String path = imagePaths.get(position);
        File file = new File(path);
        boolean d = file.delete();
        if (d) {
            imagePaths.remove(path);
            adapter.notifyDataSetChanged();
            currentPhotoNum--;
        }
    }

    Modeling3dTextureUploadListener uploadListener = new Modeling3dTextureUploadListener() {
        @Override
        public void onUploadProgress(String taskId, double progress, Object ext) {
            progressCustomDialog.setCurrentProgress(progress);
        }

        @Override
        public void onResult(String taskId, Modeling3dTextureUploadResult result, Object ext) {
            progressCustomDialog.dismiss();

            if (result.isComplete()) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), getString(R.string.upload_text_success), Toast.LENGTH_SHORT).show();
                        if (cameraTakeManager.getSurfaceViewCallback().getWidthDes() < 1024) {
                            Toast.makeText(getApplicationContext(), "The current resolution does not meet the requirements of 1024 x 1024. The effect may be poor.", Toast.LENGTH_SHORT).show();
                        }
                        clearImage();
                    }
                });
                TaskInfoMaterialAppDb taskInfoDb = new TaskInfoMaterialAppDb();
                taskInfoDb.setStatus(2);
                taskInfoDb.setTaskId(taskId);
                taskInfoDb.setCreateTime(System.currentTimeMillis());
                taskInfoDb.setIsDownload(0);
                taskInfoDb.setFileUploadPath(new Constants(CaptureMaterialActivity.this).getCaptureMaterialImageFile() + cameraTakeManager.getSurfaceViewCallback().getCreateTime());
                TaskInfoMaterialAppDbUtils.insert(taskInfoDb);
            }

        }

        @Override
        public void onError(String taskId, int errorCode, String message) {
            runOnUiThread(() -> {
                progressCustomDialog.dismiss();
                Toast.makeText(CaptureMaterialActivity.this, message+errorCode, Toast.LENGTH_SHORT).show();
            });
        }
    };

    @Override
    public void onCancel(TaskInfoAppDb appDb) {
        Observable.create((Observable.OnSubscribe<Integer>) subscriber -> {
            int result = modeling3dTextureEngine.cancelUpload(globalTaskId);
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
                    Toast.makeText(CaptureMaterialActivity.this, "Cancel failed.", Toast.LENGTH_SHORT).show();
                } else if (result == 0) {
                    Toast.makeText(CaptureMaterialActivity.this, "Canceled successfully.", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
}
