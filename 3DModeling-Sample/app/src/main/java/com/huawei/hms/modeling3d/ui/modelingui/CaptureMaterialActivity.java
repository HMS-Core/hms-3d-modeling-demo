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

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;
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
import com.huawei.hms.materialgeneratesdk.Modeling3dTextureConstants;
import com.huawei.hms.modelingresource.db.TaskInfoAppDb;
import com.huawei.hms.modelingresource.materialdb.TaskInfoMaterialAppDbUtils;
import com.huawei.hms.modelingresource.util.Constants;
import com.huawei.hms.modelingresource.view.ProgressCustomDialog;
import com.huawei.hms.materialgeneratesdk.cloud.Modeling3dTextureEngine;
import com.huawei.hms.materialgeneratesdk.cloud.Modeling3dTextureInitResult;
import com.huawei.hms.materialgeneratesdk.cloud.Modeling3dTextureSetting;
import com.huawei.hms.materialgeneratesdk.cloud.Modeling3dTextureUploadListener;
import com.huawei.hms.materialgeneratesdk.cloud.Modeling3dTextureUploadResult;
import com.huawei.hms.modeling3d.Modeling3dDemo;
import com.huawei.hms.modeling3d.R;
import com.huawei.hms.modeling3d.ui.adapter.RecycleImageAdapter;

import java.io.File;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * @Description: Create Materials
 * @since: 2021-04-28
 */
public class CaptureMaterialActivity extends AppCompatActivity implements RecycleImageAdapter.OnItemClickListener {

    @BindView(R.id.iv_back)
    ImageView ivBack;

    @BindView(R.id.tv_title)
    TextView tvTitle;

    @BindView(R.id.rl_top)
    RelativeLayout rlTop;

    @BindView(R.id.rl_mid)
    RelativeLayout rlMid;

    @BindView(R.id.surfaceView)
    SurfaceView surfaceView;

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

    private Unbinder unbinder;
    private CameraTakeManager cameraTakeManager;
    private int currentPhotoNum = 0;
    private final int maxPhotoNum = 5;
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

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_capture_material);
        unbinder = ButterKnife.bind(this, this);
        initView();
        init();
    }

    private void initView() {
        ivBack.setImageDrawable(ResourcesCompat.getDrawable(getResources(), R.drawable.close_page_icon, null));
        tvTitle.setVisibility(View.GONE);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(RecyclerView.HORIZONTAL);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new RecycleImageAdapter(imagePaths, this);
        adapter.setOnItemClickListener(this);
        recyclerView.setAdapter(adapter);
    }

    private void init() {
        // Initialize Material Engine
        modeling3dTextureEngine = Modeling3dTextureEngine.getInstance(Modeling3dDemo.getApp());
        // Initializing the AI Mode
        setting = new Modeling3dTextureSetting.Factory()
                .setTextureMode(Modeling3dTextureConstants.AlgorithmMode.AI)
                .create();
    }

    private void initCamera() {
        cameraTakeManager = new CameraTakeManager(this, surfaceView, new CameraTakeListener() {
            @Override
            public void onSuccess(File bitmapFile, Bitmap mBitmap) {
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
                isTakePhoto = false;
            }
        }, SurfaceViewCallback.MATERIAL_MODEL);
        if (createTime != null && createTime.length() > 0) {
            cameraTakeManager.getSurfaceViewCallback().setCreateTime(createTime);
            cameraTakeManager.getSurfaceViewCallback().setIndex(index);
        }
    }


    @OnClick({R.id.iv_back, R.id.iv_capture, R.id.tv_upload})
    void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.iv_back:
                LogUtil.i("iv_back");
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
            default:
        }
    }

    private void takePhoto() {
        LogUtil.i("iv_capture currentPhotoNum = " + currentPhotoNum);
        if (isTakePhoto) {
            Toast.makeText(CaptureMaterialActivity.this, "Processing pictures. Please wait.", Toast.LENGTH_SHORT).show();
        } else {
            if (createTime == null || createTime.length() <= 0) {
                createTime = String.valueOf(System.currentTimeMillis());
                index = 0;
                cameraTakeManager.getSurfaceViewCallback().setCreateTime(createTime);
                cameraTakeManager.getSurfaceViewCallback().setIndex(index);
            }
            if (currentPhotoNum < maxPhotoNum) {
                int top = rlTop.getHeight();
                int width = rlMid.getWidth();
                cameraTakeManager.takePhoto(top, width);
                isTakePhoto = true;
            } else {
                LogUtil.i("The number of photos exceeds the limit.");
            }
        }
    }

    private void upLoadMaterial() {
        progressCustomDialog = new ProgressCustomDialog(CaptureMaterialActivity.this, ProgressCustomDialog.PROGRESS_WITH_CIRCLE, getString(R.string.doing_post_text));
        progressCustomDialog.setCanceledOnTouchOutside(false);
        progressCustomDialog.show();
        new Thread(new Runnable() {
            @Override
            public void run() {
                // Create Material Task
                modeling3dTextureInitResult = modeling3dTextureEngine.initTask(setting);
                // Obtains the material task ID.
                String taskId = modeling3dTextureInitResult.getTaskId();
                String filePath = new Constants(CaptureMaterialActivity.this).getCaptureImageFile() + createTime + "/";
                if (taskId != null && !taskId.equals("")) {
                    // Setting material upload listeners
                    modeling3dTextureEngine.setTextureUploadListener(uploadListener);
                    // Executing Material Upload Tasks
                    modeling3dTextureEngine.asyncUploadFile(taskId, filePath);
                }
            }
        }).start();
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

    // Material upload callback listening
    Modeling3dTextureUploadListener uploadListener = new Modeling3dTextureUploadListener() {
        @Override
        public void onUploadProgress(String taskId, double progress, Object ext) {
            // Upload progress
        }

        @Override
        public void onResult(String taskId, Modeling3dTextureUploadResult result, Object ext) {
            progressCustomDialog.dismiss();
            // Operations after successful upload or download
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
                TaskInfoAppDb taskInfoDb = new TaskInfoAppDb();
                taskInfoDb.setStatus(Modeling3dTextureConstants.ProgressStatus.UPLOAD_COMPLETED);
                taskInfoDb.setTaskId(taskId);
                taskInfoDb.setCreateTime(System.currentTimeMillis());
                taskInfoDb.setIsDownload(0);
                taskInfoDb.setFileUploadPath(new Constants(CaptureMaterialActivity.this).getCaptureImageFile() + cameraTakeManager.getSurfaceViewCallback().getCreateTime());
                TaskInfoMaterialAppDbUtils.insert(taskInfoDb);
            }
            LogUtil.e("Upload SUCCESS----------------->" + taskId);

        }

        @Override
        public void onError(String taskId, int errorCode, String message) {
            progressCustomDialog.dismiss();
            // Upload or download failure information
            LogUtil.e("UploadError " + message + "   " + errorCode);
        }
    };

}
