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
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
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
import com.huawei.hms.modelingresource.db.TaskInfoAppDb;
import com.huawei.hms.modelingresource.db.TaskInfoAppDbUtils;
import com.huawei.hms.modelingresource.util.Constants;
import com.huawei.hms.modelingresource.view.ProgressCustomDialog;
import com.huawei.hms.modeling3d.Modeling3dDemo;
import com.huawei.hms.modeling3d.R;
import com.huawei.hms.modeling3d.ui.adapter.RecycleReconstructAdapter;
import com.huawei.hms.objreconstructsdk.Modeling3dReconstructConstants;
import com.huawei.hms.objreconstructsdk.cloud.Modeling3dReconstructDownloadListener;
import com.huawei.hms.objreconstructsdk.cloud.Modeling3dReconstructDownloadResult;
import com.huawei.hms.objreconstructsdk.cloud.Modeling3dReconstructEngine;
import com.huawei.hms.objreconstructsdk.cloud.Modeling3dReconstructInitResult;
import com.huawei.hms.objreconstructsdk.cloud.Modeling3dReconstructQueryResult;
import com.huawei.hms.objreconstructsdk.cloud.Modeling3dReconstructSetting;
import com.huawei.hms.objreconstructsdk.cloud.Modeling3dReconstructTaskUtils;
import com.huawei.hms.objreconstructsdk.cloud.Modeling3dReconstructUploadListener;
import com.huawei.hms.objreconstructsdk.cloud.Modeling3dReconstructUploadResult;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class HistoryModelDataFragment extends Fragment implements RecycleReconstructAdapter.OnItemClickListener,
        RecycleReconstructAdapter.OnItemClickDownloadListener {
    private Unbinder unbinder = null;
    private Modeling3dReconstructEngine modeling3dReconstructEngine = null;
    public Modeling3dReconstructTaskUtils modeling3dReconstructTaskUtils = null;
    public ArrayList<TaskInfoAppDb> dataBeans = new ArrayList<>();
    private RecycleReconstructAdapter adapter;
    private Context mContext = null;
    private Handler handler;
    private HandlerThread thread;
    String TAG = "HistoryDataFragment";

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

    ProgressCustomDialog dialog = null;
    private Timer timer = new Timer();


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.mContext = context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Initializing the Rebuild Engine
        modeling3dReconstructEngine = Modeling3dReconstructEngine.getInstance(Modeling3dDemo.getApp());
        // Initialize the reconstruction task tool class.
        modeling3dReconstructTaskUtils = Modeling3dReconstructTaskUtils.getInstance(Modeling3dDemo.getApp());
        thread = new HandlerThread("Handler Thread");
        thread.start();
        handler = new Handler(thread.getLooper()) {
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
                if (msg.what == 1) {
                    Toast.makeText(getContext(), "Upload failed.", Toast.LENGTH_SHORT).show();
                }
            }
        };
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
        adapter = new RecycleReconstructAdapter(dataBeans, getContext());
        adapter.setOnItemClickListener(this);
        adapter.setOnItemClickDownloadListener(this);
        recyclerView.setAdapter(adapter);

        dataBeans = TaskInfoAppDbUtils.getAllTasks();
        adapter.setDataList(dataBeans);
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                loadPage();
            }
        }, 1000, 15000);

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
        timer = null;
    }

    @Override
    public void onClick(View parent, int position) {
        // Setting the Rebuild Upload Listener
        modeling3dReconstructEngine.setReconstructUploadListener(uploadListener);
        dialog = new ProgressCustomDialog(getActivity(), 2, getString(R.string.doing_post_text));
        dialog.setShowCancer(false);
        dialog.show();
        dialog.setCanceledOnTouchOutside(false);
        TaskInfoAppDb news = dataBeans.get(position);
        if (!TextUtils.isEmpty(news.getTaskId())) {
            // Upload the file path to the cloud.
            modeling3dReconstructEngine.uploadFile(news.getTaskId(), news.getFileUploadPath());
        } else {
            // Initializing the RGB Mode
            Modeling3dReconstructSetting setting = new Modeling3dReconstructSetting.Factory()
                    .setReconstructMode(Constants.RGB_MODEL)
                    .create();
            new Thread("QueryThread") {
                @Override
                public void run() {
                    // Initializing a Rebuild Task
                    Modeling3dReconstructInitResult result = modeling3dReconstructEngine.initTask(setting);
                    String taskId = result.getTaskId();
                    if (taskId == null || taskId.equals("")) {
                        new Handler(mContext.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                dialog.dismiss();
                                Toast.makeText(mContext, "Upload failed.", Toast.LENGTH_LONG).show();
                            }
                        });
                    } else {
                        news.setTaskId(taskId);
                        // Update the latest data containing taskID.
                        TaskInfoAppDbUtils.updateTaskIdAndStatusByPath(news.getFileUploadPath(), taskId, 0);
                        // Executing a Rebuild Upload Task
                        modeling3dReconstructEngine.uploadFile(taskId, news.getFileUploadPath());
                    }
                }
            }.start();
        }
    }

    @Override
    public void onClickDownLoad(TaskInfoAppDb appDb, RecycleReconstructAdapter.DataViewHolder holder) {
        dialog = new ProgressCustomDialog(getContext(), 2, getString(R.string.downloading_dialog_text));
        dialog.setCanceledOnTouchOutside(false);
        dialog.setShowCancer(false);
        dialog.show();
        // Setting Rebuild Download Listening
        modeling3dReconstructEngine.setReconstructDownloadListener(modeling3dReconstructDownloadListener);
        // Executing a Rebuild Download Task
        modeling3dReconstructEngine.downloadModel(appDb.getTaskId(), appDb.getFileSavePath());
    }


    // Re-establishing the upload callback listener
    private final Modeling3dReconstructUploadListener uploadListener = new Modeling3dReconstructUploadListener() {
        @Override
        public void onUploadProgress(String taskId, double progress, Object ext) {
            // Upload progress
        }

        @Override
        public void onResult(String taskId, Modeling3dReconstructUploadResult result, Object ext) {
            if (result.isComplete()) {
                TaskInfoAppDbUtils.updateStatusByTaskId(taskId, 1);
                // Update Task Status
                updateTaskStatus(adapter, taskId, 1);
                dialog.dismiss();
            }
        }

        @Override
        public void onError(String taskId, int errorCode, String message) {
            Log.e(TAG, "UPLOAD FAILED-->" + errorCode + "<----->" + message);
            Message msg = handler.obtainMessage();
            msg.what = 1;
            handler.sendMessage(msg);
            dialog.dismiss();
        }
    };

    // Re-establishing the download callback listener
    private Modeling3dReconstructDownloadListener modeling3dReconstructDownloadListener = new Modeling3dReconstructDownloadListener() {
        @Override
        public void onDownloadProgress(String taskId, double progress, Object ext) {
            ((Activity) mContext).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    dialog.show();
                }
            });
        }

        @Override
        public void onResult(String taskId, Modeling3dReconstructDownloadResult result, Object ext) {
            ((Activity) mContext).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getContext(), "Download complete", Toast.LENGTH_SHORT).show();
                    TaskInfoAppDbUtils.updateDownloadByTaskId(taskId, 1);
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

    private void updateTaskStatus(RecycleReconstructAdapter adapter, String taskId, int comStatus) {
        ArrayList<TaskInfoAppDb> appDbs = dataBeans;
        int count = appDbs.size();
        for (int i = 0; i < count; i++) {
            if (appDbs.get(i).getTaskId() != null && appDbs.get(i).getTaskId().equals(taskId)) {
                appDbs.get(i).setStatus(comStatus);
                break;
            }
        }
        ((Activity) mContext).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getContext(), getString(R.string.upload_text_success), Toast.LENGTH_SHORT).show();
                adapter.notifyDataSetChanged();
            }
        });
    }


    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    public void loadPage() {
        for (int i = 0; i < dataBeans.size(); i++) {
            TaskInfoAppDb task = dataBeans.get(i);
            if (task.getStatus() < Modeling3dReconstructConstants.ProgressStatus.RECONSTRUCT_COMPLETED) {
                if (task.getTaskId() != null) {
                    new Thread("QueryThread") {
                        @Override
                        public void run() {
                            // Query the reconstruction task execution result. The options are as follows: 0: To be uploaded; 1: Generating; 3: Completed; 4: Failed.
                            Modeling3dReconstructQueryResult queryResult = modeling3dReconstructTaskUtils.queryTask(task.getTaskId());
                            if (queryResult.getRetCode() == 0) {
                                int ret = queryResult.getStatus();
                                TaskInfoAppDbUtils.updateStatusByTaskId(queryResult.getTaskId(), ret);
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
