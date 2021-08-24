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
package com.huawei.hms.modeling3d.ui.widget;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.huawei.cameratakelib.utils.FileUtil;
import com.huawei.hms.magicresource.materialdb.TaskInfoMaterialAppDb;
import com.huawei.hms.magicresource.materialdb.TaskInfoMaterialAppDbUtils;
import com.huawei.hms.magicresource.util.Utils;
import com.huawei.hms.materialgeneratesdk.Modeling3dTextureConstants;
import com.huawei.hms.materialgeneratesdk.cloud.Modeling3dTextureTaskUtils;
import com.huawei.hms.modeling3d.Modeling3dApp;
import com.huawei.hms.modeling3d.R;
import com.huawei.hms.modeling3d.model.ConstantBean;
import com.huawei.hms.modeling3d.ui.adapter.RecycleMaterialAdapter;
import com.huawei.hms.objreconstructsdk.Modeling3dReconstructConstants;
import com.huawei.hms.objreconstructsdk.cloud.Modeling3dReconstructTaskUtils;

import java.io.File;
import java.util.ArrayList;

public class HandlerMaterialPopDialog {

    private Context mContext;
    private TaskInfoMaterialAppDb appDb;
    RecycleMaterialAdapter.DataViewHolder holder;
    View contentView;
    RecycleMaterialAdapter adapter;
    PopupWindow popupWindow;
    TextView tvDownload;
    ArrayList<TaskInfoMaterialAppDb> dataList;
    public Modeling3dTextureTaskUtils modeling3dTextureTaskUtils;
    int status ;

    public HandlerMaterialPopDialog(Context mContext, RecycleMaterialAdapter adapter, TaskInfoMaterialAppDb appDb, RecycleMaterialAdapter.DataViewHolder holder, ArrayList<TaskInfoMaterialAppDb> dataList,int status) {
        this.mContext = mContext;
        this.appDb = appDb;
        this.holder = holder;
        this.adapter = adapter;
        this.dataList = dataList;
        this.status = status ;
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
        if (appDb.getStatus() == ConstantBean.MATERIAL_RECONSTRUCT_COMPLETED_STATUS) {
            tvDownload.setVisibility(View.VISIBLE);
            contentView.findViewById(R.id.tv_restrict_status).setVisibility(View.VISIBLE);
            if (status== Modeling3dReconstructConstants.RestrictStatus.UNRESTRICT){
                ((TextView)contentView.findViewById(R.id.tv_restrict_status)).setText(R.string.restricted_text);
            }else if (status==Modeling3dReconstructConstants.RestrictStatus.RESTRICT){
                ((TextView)contentView.findViewById(R.id.tv_restrict_status)).setText(R.string.unrestricted_text);
            }
        } else {
            tvDownload.setVisibility(View.GONE);
        }

        try {
            String savePath = TaskInfoMaterialAppDbUtils.getTasksByTaskId(appDb.getTaskId()).getFileSavePath();
            if (TextUtils.isEmpty(savePath)) {
                contentView.findViewById(R.id.tv_open_file).setVisibility(View.GONE);
            } else {
                contentView.findViewById(R.id.tv_open_file).setVisibility(View.VISIBLE);
            }
        } catch (Exception e) {
            contentView.findViewById(R.id.tv_open_file).setVisibility(View.GONE);
        }

        tvDownload.setOnClickListener(v -> {
            String savePath = TaskInfoMaterialAppDbUtils.getTasksByTaskId(appDb.getTaskId()).getFileSavePath();
            if (TextUtils.isEmpty(savePath)) {
                adapter.setOnDownLoadClick(appDb, holder);
            } else {
                File file = new File(savePath);
                if (!file.exists() || file.listFiles() == null || file.listFiles().length != 4) {
                    Utils.deleteDirectory(savePath);
                    adapter.setOnDownLoadClick(appDb, holder);
                } else {
                    Toast.makeText(mContext, "Material file already exists", Toast.LENGTH_LONG).show();
                }
            }
            if (popupWindow != null) {
                popupWindow.dismiss();
            }
        });
        contentView.findViewById(R.id.tv_delete).setOnClickListener(v -> {

            DeleteDialog deleteDialog = new DeleteDialog(mContext);
            deleteDialog.show();
            deleteDialog.getTvSure().setOnClickListener(view -> {
                deleteDialog.dismiss();
                if (!TextUtils.isEmpty(appDb.getFileUploadPath())) {
                    FileUtil.deleteFile(new File(appDb.getFileUploadPath()));
                }
                TaskInfoMaterialAppDbUtils.deleteByUploadFilePath(appDb.getFileUploadPath());
                dataList.remove(holder.getAdapterPosition());
                adapter.notifyDataSetChanged();
                if (popupWindow != null) {
                    popupWindow.dismiss();
                }
                modeling3dTextureTaskUtils = Modeling3dTextureTaskUtils.getInstance(Modeling3dApp.app);
                new Thread(() -> modeling3dTextureTaskUtils.deleteTask(appDb.getTaskId())).start();
            });

            deleteDialog.getTvCancel().setOnClickListener(view -> {
                deleteDialog.dismiss();
                if (popupWindow != null) {
                    popupWindow.dismiss();
                }
            });
        });

        contentView.findViewById(R.id.tv_open_file).setOnClickListener(view -> {
            String savePath = TaskInfoMaterialAppDbUtils.getTasksByTaskId(appDb.getTaskId()).getFileSavePath();
            Toast.makeText(mContext, savePath, Toast.LENGTH_LONG).show();
        });
        contentView.findViewById(R.id.tv_restrict_status).setOnClickListener(v -> {
            modeling3dTextureTaskUtils = Modeling3dTextureTaskUtils.getInstance(Modeling3dApp.app);
            new Thread(()->{
                if (status==Modeling3dTextureConstants.RestrictStatus.UNRESTRICT){
                   modeling3dTextureTaskUtils.setTaskRestrictStatus(appDb.getTaskId(), Modeling3dTextureConstants.RestrictStatus.RESTRICT);
                }else {
                    modeling3dTextureTaskUtils.setTaskRestrictStatus(appDb.getTaskId(),Modeling3dTextureConstants.RestrictStatus.UNRESTRICT);
                }
            }).start();
            if (popupWindow != null) {
                popupWindow.dismiss();
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
