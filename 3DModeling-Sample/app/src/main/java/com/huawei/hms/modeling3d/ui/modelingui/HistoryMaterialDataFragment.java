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

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.huawei.cameratakelib.utils.LogUtil;
import com.huawei.hms.materialgeneratesdk.Modeling3dTextureConstants;
import com.huawei.hms.modelingresource.db.TaskInfoAppDb;
import com.huawei.hms.modelingresource.materialdb.TaskInfoMaterialAppDbUtils;
import com.huawei.hms.modelingresource.view.ProgressCustomDialog;
import com.huawei.hms.materialgeneratesdk.cloud.Modeling3dTextureDownloadListener;
import com.huawei.hms.materialgeneratesdk.cloud.Modeling3dTextureDownloadResult;
import com.huawei.hms.materialgeneratesdk.cloud.Modeling3dTextureEngine;
import com.huawei.hms.materialgeneratesdk.cloud.Modeling3dTextureQueryResult;
import com.huawei.hms.materialgeneratesdk.cloud.Modeling3dTextureTaskUtils;
import com.huawei.hms.modeling3d.Modeling3dDemo;
import com.huawei.hms.modeling3d.R;
import com.huawei.hms.modeling3d.ui.adapter.RecycleMaterialAdapter;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class HistoryMaterialDataFragment extends Fragment implements RecycleMaterialAdapter.OnItemClickDownloadListener {

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

    private Unbinder unbinder;
    private Modeling3dTextureEngine modeling3dTextureEngine = null;
    private Modeling3dTextureTaskUtils modeling3dTextureTaskUtils = null;
    private RecycleMaterialAdapter adapter;
    public ArrayList<TaskInfoAppDb> dataBeans = new ArrayList<>();
    private Context mContext = null;
    ProgressCustomDialog dialog = null;
    private final Timer timer = new Timer();

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.mContext = context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Initialize Material Engine
        modeling3dTextureEngine = Modeling3dTextureEngine.getInstance(Modeling3dDemo.getApp());
        // Initialize Material Task Tool Class
        modeling3dTextureTaskUtils = Modeling3dTextureTaskUtils.getInstance(Modeling3dDemo.getApp());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.history_data_fragment_layout, null);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initData();
    }

    private void initData() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        adapter = new RecycleMaterialAdapter(dataBeans, getContext());
        adapter.setOnItemClickDownloadListener(this);
        recyclerView.setAdapter(adapter);

        dataBeans = TaskInfoMaterialAppDbUtils.getAllTasks();
        adapter.setDataList(dataBeans);
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                loadPage();
            }
        }, 1000, 5000);
    }

    @Override
    public void onClickDownLoad(TaskInfoAppDb appDb, RecycleMaterialAdapter.DataViewHolder holder) {
        dialog = new ProgressCustomDialog(getContext(), ProgressCustomDialog.PROGRESS_WITH_CIRCLE, getString(R.string.downloading_dialog_text));
        dialog.setCanceledOnTouchOutside(false);
        dialog.setShowCancer(false);
        dialog.show();
        // Setting Material Download Listening
        modeling3dTextureEngine.setTextureDownloadListener(modeling3dTextureDownloadListener);
        // Performing Material Download Tasks
        modeling3dTextureEngine.asyncDownloadTexture(appDb.getTaskId(), appDb.getFileSavePath());
    }

    // Material upload callback listening
    private Modeling3dTextureDownloadListener modeling3dTextureDownloadListener = new Modeling3dTextureDownloadListener() {
        @Override
        public void onDownloadProgress(String taskId, double progress, Object ext) {

        }

        @Override
        public void onResult(String taskId, Modeling3dTextureDownloadResult result, Object ext) {
            ((Activity) mContext).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getContext(), "Download complete", Toast.LENGTH_SHORT).show();
                    TaskInfoMaterialAppDbUtils.updateDownloadByTaskId(taskId, 1);
                    dialog.dismiss();
                }
            });
        }

        @Override
        public void onError(String taskId, int errorCode, String message) {
            LogUtil.e(taskId + " <---> " + errorCode + message);
            ((Activity) mContext).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getContext(), "Download failed." + message, Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                }
            });
        }
    };

    @Override
    public void onPause() {
        super.onPause();
    }

    public void loadPage() {
        for (int i = 0; i < dataBeans.size(); i++) {
            TaskInfoAppDb task = dataBeans.get(i);
            if (task.getStatus() < Modeling3dTextureConstants.ProgressStatus.TEXTURE_COMPLETED) {
                if (task.getTaskId() != null) {
                    new Thread("QueryThread") {
                        @Override
                        public void run() {
                            // Query the execution result of a material task. The options are as follows: 0:To be uploaded; 1: Generating; 3: Completed; 4: Failed.
                            Modeling3dTextureQueryResult queryResult = modeling3dTextureTaskUtils.queryTask(task.getTaskId());
                            if (queryResult.getRetCode() == 0) {
                                int ret = queryResult.getStatus();
                                TaskInfoMaterialAppDbUtils.updateStatusByTaskId(queryResult.getTaskId(), ret);
                                Lock lock = new ReentrantLock();
                                try {
                                    lock.lock();
                                    task.setStatus(ret);
                                    ((Activity) mContext).runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            adapter.notifyDataSetChanged();
                                        }
                                    });
                                } finally {
                                    lock.unlock();
                                }
                            }
                        }
                    }.start();
                }
            }
        }
    }
}
