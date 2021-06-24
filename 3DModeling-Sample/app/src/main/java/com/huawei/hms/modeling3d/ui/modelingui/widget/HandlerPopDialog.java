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

package com.huawei.hms.modeling3d.ui.modelingui.widget;

import android.content.Context;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.huawei.cameratakelib.utils.FileUtil;
import com.huawei.hms.modelingresource.db.TaskInfoAppDb;
import com.huawei.hms.modelingresource.db.TaskInfoAppDbUtils;
import com.huawei.hms.modelingresource.util.Utils;
import com.huawei.hms.modeling3d.Modeling3dDemo;
import com.huawei.hms.modeling3d.R;
import com.huawei.hms.modeling3d.ui.adapter.RecycleReconstructAdapter;
import com.huawei.hms.objreconstructsdk.Modeling3dReconstructConstants;
import com.huawei.hms.objreconstructsdk.cloud.Modeling3dReconstructTaskUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Objects;

public class HandlerPopDialog {
    private Context mContext;
    private TaskInfoAppDb appDb;
    RecycleReconstructAdapter.DataViewHolder holder;
    View contentView;
    RecycleReconstructAdapter adapter;
    PopupWindow popupWindow;
    TextView tvDownload;
    TextView tvOpenFolder;
    ArrayList<TaskInfoAppDb> dataList;
    public Modeling3dReconstructTaskUtils modeling3dReconstructTaskUtils;

    public HandlerPopDialog(Context mContext, RecycleReconstructAdapter adapter, TaskInfoAppDb appDb, RecycleReconstructAdapter.DataViewHolder holder, ArrayList<TaskInfoAppDb> dataList) {
        this.mContext = mContext;
        this.appDb = appDb;
        this.holder = holder;
        this.adapter = adapter;
        this.dataList = dataList;
        contentView = LayoutInflater.from(mContext).inflate(R.layout.pop_dialog_layout, null);
        popupWindow = new PopupWindow(contentView, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        popupWindow.setFocusable(true);
        popupWindow.setOutsideTouchable(true);
        int[] windowPos = calculatePopWindowPos(holder.ivPoint, contentView);
        popupWindow.showAtLocation(holder.ivPoint, Gravity.START | Gravity.TOP, windowPos[0] - Utils.dip2px(mContext, 25), windowPos[1] + Utils.dip2px(mContext, 25));
        initView(contentView);
    }

    private void initView(View contentView) {
        tvDownload = contentView.findViewById(R.id.tv_download);
        tvOpenFolder = contentView.findViewById(R.id.tv_open_folder);
        if (appDb.getStatus() == Modeling3dReconstructConstants.ProgressStatus.RECONSTRUCT_COMPLETED) {
            tvDownload.setVisibility(View.VISIBLE);
            tvOpenFolder.setVisibility(View.VISIBLE);
        } else {
            tvDownload.setVisibility(View.GONE);
            tvOpenFolder.setVisibility(View.GONE);
        }
        tvDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String savePath = TaskInfoAppDbUtils.getTasksByTaskId(appDb.getTaskId()).getFileSavePath();
                if (TextUtils.isEmpty(savePath)) {
                    adapter.setOnDownLoadClick(appDb, holder);
                    if (popupWindow != null) {
                        popupWindow.dismiss();
                    }
                } else {
                    if (!TextUtils.isEmpty(savePath)) {
                        File file = new File(savePath);
                        if (file.exists() && file.listFiles() != null) {
                            File[] files =  file.listFiles();
                            if (files != null && files.length == 3) {
                                Toast.makeText(mContext, "The model file already exists.", Toast.LENGTH_LONG).show();
                            } else {
                                FileUtil.deleteDirectory(savePath);
                                adapter.setOnDownLoadClick(appDb, holder);
                            }
                        } else {
                            FileUtil.deleteDirectory(savePath);
                            adapter.setOnDownLoadClick(appDb, holder);
                        }
                    }
                }
            }
        });
        contentView.findViewById(R.id.tv_delete).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TaskInfoAppDbUtils.deleteByUploadFilePath(appDb.getFileUploadPath());
                dataList.remove(holder.getAdapterPosition());
                adapter.notifyDataSetChanged();
                if (popupWindow != null) {
                    popupWindow.dismiss();
                }
                // Initialize the reconstruction task tool class.
                modeling3dReconstructTaskUtils = Modeling3dReconstructTaskUtils.getInstance(Modeling3dDemo.getApp());
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        // Deleting a Rebuilding Task
                        modeling3dReconstructTaskUtils.deleteTask(appDb.getTaskId());
                    }
                }).start();
            }
        });
        tvOpenFolder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String savePath = TaskInfoAppDbUtils.getTasksByTaskId(appDb.getTaskId()).getFileSavePath();
                if (TextUtils.isEmpty(savePath)) {
                    Toast.makeText(mContext, "Please download it first.", Toast.LENGTH_LONG).show();
                } else {
                    if (popupWindow != null) {
                        popupWindow.dismiss();
                    }
                    if (!TextUtils.isEmpty(savePath)) {
                        File file = new File(savePath);
                        if (file.exists() && file.listFiles() != null) {
                            File[] files =  file.listFiles();
                            if (files != null && files.length == 3) {
                                Toast.makeText(mContext, "File path:" + savePath, Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(mContext, "The folder has been deleted. Please download it again.", Toast.LENGTH_LONG).show();
                            }
                        } else {
                            Toast.makeText(mContext, "The folder has been deleted. Please download it again.", Toast.LENGTH_LONG).show();
                        }
                    }
                }
            }
        });
    }


    private static int[] calculatePopWindowPos(final View anchorView, final View contentView) {
        final int[] windowPos = new int[2];
        final int[] anchorLoc = new int[2];
        anchorView.getLocationOnScreen(anchorLoc);
        final int anchorHeight = anchorView.getHeight();
        final int screenHeight = getScreenHeight(anchorView.getContext());
        final int screenWidth = getScreenWidth(anchorView.getContext());
        contentView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        final int windowHeight = contentView.getMeasuredHeight();
        final int windowWidth = contentView.getMeasuredWidth();
        final boolean isNeedShowUp = (screenHeight - anchorLoc[1] - anchorHeight < windowHeight);
        if (isNeedShowUp) {
            windowPos[0] = screenWidth - windowWidth;
            windowPos[1] = anchorLoc[1] - windowHeight;
        } else {
            windowPos[0] = screenWidth - windowWidth;
            windowPos[1] = anchorLoc[1] + anchorHeight;
        }
        return windowPos;
    }

    public static int getScreenHeight(Context context) {
        return context.getResources().getDisplayMetrics().heightPixels;
    }

    public static int getScreenWidth(Context context) {
        return context.getResources().getDisplayMetrics().widthPixels;
    }
}
