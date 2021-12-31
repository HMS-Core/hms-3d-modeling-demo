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
package com.huawei.hms.modeling3d.ui.fragment;

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

import com.huawei.hms.magicresource.db.TaskInfoAppDb;
import com.huawei.hms.magicresource.materialdb.TaskInfoMaterialAppDb;
import com.huawei.hms.magicresource.materialdb.TaskInfoMaterialAppDbUtils;
import com.huawei.hms.materialgeneratesdk.cloud.Modeling3dTextureDownloadListener;
import com.huawei.hms.materialgeneratesdk.cloud.Modeling3dTextureDownloadResult;
import com.huawei.hms.materialgeneratesdk.cloud.Modeling3dTextureEngine;
import com.huawei.hms.materialgeneratesdk.cloud.Modeling3dTextureQueryResult;
import com.huawei.hms.materialgeneratesdk.cloud.Modeling3dTextureTaskUtils;
import com.huawei.hms.modeling3d.Modeling3dApp;
import com.huawei.hms.modeling3d.R;
import com.huawei.hms.modeling3d.model.ConstantBean;
import com.huawei.hms.modeling3d.ui.adapter.RecycleMaterialAdapter;
import com.huawei.hms.modeling3d.ui.widget.ProgressCustomDialog;
import com.huawei.hms.modeling3d.utils.LogUtil;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class HistoryMaterialDataFragment extends Fragment implements RecycleMaterialAdapter.OnItemClickDownloadListener,ProgressCustomDialog.OnItemCancelClickListener {

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

    private Unbinder unbinder;
    private Modeling3dTextureEngine modeling3dTextureEngine;
    private Modeling3dTextureTaskUtils modeling3dTextureTaskUtils;
    private RecycleMaterialAdapter adapter;
    public ArrayList<TaskInfoMaterialAppDb> dataBeans = new ArrayList<>();
    private Context mContext;
    ProgressCustomDialog dialog;

    String taskId ;

    public HistoryMaterialDataFragment(){}

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.mContext = context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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
        modeling3dTextureTaskUtils = Modeling3dTextureTaskUtils.getInstance(Modeling3dApp.app);

        dataBeans = TaskInfoMaterialAppDbUtils.getAllTasks();
        adapter.setDataList(dataBeans);
    }

    @Override
    public void onResume() {
        super.onResume();
        loadPage();
    }

    @Override
    public void onClickDownLoad(TaskInfoMaterialAppDb appDb, RecycleMaterialAdapter.DataViewHolder holder) {
        modeling3dTextureEngine = Modeling3dTextureEngine .getInstance(Modeling3dApp.app);
        dialog = new ProgressCustomDialog(getContext(), ConstantBean.PROGRESS_CUSTOM_DIALOG_TYPE_ONE, getString(R.string.downloading_dialog_text));
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
        dialog.setListener(this);
        taskId = appDb.getTaskId() ;
        modeling3dTextureEngine.setTextureDownloadListener(modeling3dTextureDownloadListener);
        modeling3dTextureEngine.asyncDownloadTexture(appDb.getTaskId(), appDb.getFileSavePath());
    }

    private Modeling3dTextureDownloadListener modeling3dTextureDownloadListener = new Modeling3dTextureDownloadListener() {
        @Override
        public void onDownloadProgress(String taskId, double progress, Object ext) {
            ((Activity) mContext).runOnUiThread(() -> {
                dialog.setCurrentProgress(progress);
            });
        }

        @Override
        public void onResult(String taskId, Modeling3dTextureDownloadResult result, Object ext) {
            ((Activity) mContext).runOnUiThread(() -> {
                Toast.makeText(getContext(), "Download completed", Toast.LENGTH_SHORT).show();
                TaskInfoMaterialAppDbUtils.updateDownloadByTaskId(taskId, 1);
                dialog.dismiss();
            });
        }

        @Override
        public void onError(String taskId, int errorCode, String message) {
            LogUtil.e(taskId + " <---> " + errorCode + message);
            ((Activity) mContext).runOnUiThread(() -> {
                Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            });
        }
    };

    @Override
    public void onPause() {
        LogUtil.d("onPause");
        super.onPause();
        if (modeling3dTextureEngine!=null) {
            modeling3dTextureEngine.close();
        }
    }

    public void loadPage() {
        for (int i = 0; i < dataBeans.size(); i++) {
            TaskInfoMaterialAppDb task = dataBeans.get(i);
            if (task.getStatus() < 5) {
                if (task.getTaskId() != null) {
                    new Thread("queryThread") {
                        @Override
                        public void run() {
                            Modeling3dTextureQueryResult queryResult = modeling3dTextureTaskUtils.queryTask(task.getTaskId());
                            if (queryResult.getRetCode() == 0) {
                                int ret = queryResult.getStatus();
                                TaskInfoMaterialAppDbUtils.updateStatusByTaskId(queryResult.getTaskId(), ret);
                                Lock lock = new ReentrantLock();
                                try {
                                    lock.lock();
                                    task.setStatus(ret);
                                    ((Activity) mContext).runOnUiThread(() -> adapter.notifyDataSetChanged());
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

    @Override
    public void onCancel(TaskInfoAppDb appDb) {
        new Thread("cancelThread"){
            @Override
            public void run() {
                modeling3dTextureEngine = Modeling3dTextureEngine .getInstance(Modeling3dApp.app);
                int result = modeling3dTextureEngine.cancelDownload(taskId);
                if (result == 0) {
                    ((Activity) mContext).runOnUiThread(() -> Toast.makeText(getActivity(), "Cancel download successfully", Toast.LENGTH_SHORT).show());

                }
            }
        }.start();
    }
}
