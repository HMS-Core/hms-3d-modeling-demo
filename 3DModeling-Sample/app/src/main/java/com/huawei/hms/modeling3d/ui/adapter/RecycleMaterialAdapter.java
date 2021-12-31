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
package com.huawei.hms.modeling3d.ui.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.huawei.hms.magicresource.materialdb.TaskInfoMaterialAppDb;
import com.huawei.hms.magicresource.materialdb.TaskInfoMaterialAppDbUtils;
import com.huawei.hms.magicresource.util.Constants;
import com.huawei.hms.magicresource.util.FileSizeUtil;
import com.huawei.hms.magicresource.util.Utils;
import com.huawei.hms.magicresource.view.CustomRoundAngleImageView;
import com.huawei.hms.materialgeneratesdk.cloud.Modeling3dTextureEngine;
import com.huawei.hms.materialgeneratesdk.cloud.Modeling3dTexturePreviewListener;
import com.huawei.hms.materialgeneratesdk.cloud.Modeling3dTextureTaskUtils;
import com.huawei.hms.modeling3d.Modeling3dApp;
import com.huawei.hms.modeling3d.R;
import com.huawei.hms.modeling3d.model.ConstantBean;
import com.huawei.hms.modeling3d.ui.widget.HandlerMaterialPopDialog;
import com.huawei.hms.modeling3d.ui.widget.HandlerPopDialog;
import com.huawei.hms.objreconstructsdk.cloud.Modeling3dReconstructEngine;
import com.huawei.hms.objreconstructsdk.cloud.Modeling3dReconstructTaskUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Objects;

public class RecycleMaterialAdapter extends RecyclerView.Adapter<RecycleMaterialAdapter.DataViewHolder> {

    private ArrayList<TaskInfoMaterialAppDb> dataList;

    private Context mContext;

    private OnItemClickDownloadListener onItemClickDownloadListener = null;

    public RecycleMaterialAdapter(ArrayList<TaskInfoMaterialAppDb> dataList, Context context) {
        this.dataList = dataList;
        this.mContext = context;
    }

    @NonNull
    @Override
    public DataViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.history_ls_item, parent, false);
        return new RecycleMaterialAdapter.DataViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull DataViewHolder holder, int position) {
        TaskInfoMaterialAppDb news = dataList.get(position);
        switch (news.getStatus()) {
            case ConstantBean.MATERIAL_UPLOAD_COMPLETED_STATUS:
            case ConstantBean.MATERIAL_RECONSTRUCT_START_STATUS:
                holder.tvStatus.setText(R.string.product_doing_text);
                holder.ivShowStatus.setVisibility(View.VISIBLE);
                holder.tvStatus.setBackgroundResource(R.drawable.product_doing_bg);
                holder.ivShowStatus.setImageResource(R.drawable.product_doing_icon);
                holder.ivShowStatus.setOnClickListener(null);
                break;
            case ConstantBean.MATERIAL_RECONSTRUCT_COMPLETED_STATUS:
                holder.tvStatus.setText(R.string.finish_text);
                holder.ivShowStatus.setVisibility(View.VISIBLE);
                holder.tvStatus.setBackgroundResource(R.drawable.finish_status_bg);
                holder.ivShowStatus.setImageResource(R.drawable.finish_doing_icon);
                holder.ivShowStatus.setOnClickListener(v -> {
                        Modeling3dTextureEngine engine = Modeling3dTextureEngine.getInstance(mContext);

                        engine.previewTexture(news.getTaskId(), Modeling3dApp.app, new Modeling3dTexturePreviewListener() {
                            @Override
                            public void onResult(String taskId, Object ext) {

                            }

                            @Override
                            public void onError(String taskId, int errorCode, String message) {
                                ((Activity)mContext).runOnUiThread(() -> {
                                    Toast.makeText(mContext, message, Toast.LENGTH_LONG).show();
                                });
                            }
                        });
                });
                break;
            case ConstantBean.MATERIAL_RECONSTRUCT_FAILED_STATUS:
                holder.tvStatus.setText(R.string.finish_fail_text1);
                holder.tvStatus.setBackgroundResource(R.drawable.fail_status_bg);
                holder.ivShowStatus.setVisibility(View.INVISIBLE);
                break;
        }
        holder.tvTime.setText(Utils.systemCurrentToData(news.getCreateTime()));

        holder.ivPoint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Modeling3dTextureTaskUtils modeling3dTextureTaskUtils = Modeling3dTextureTaskUtils.getInstance(mContext);
                new Thread(){
                    @Override
                    public void run() {
                        super.run();
                        int status = modeling3dTextureTaskUtils.queryTaskRestrictStatus(news.getTaskId());
                        if (news.getStatus() != ConstantBean.MODELS_RECONSTRUCT_START_STATUS) {
                            ((Activity)mContext).runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    new HandlerMaterialPopDialog(mContext, RecycleMaterialAdapter.this, news, holder, dataList,status);
                                }
                            });
                        }
                    }
                }.start();
            }
        });
        if (news.getFileUploadPath()!=null) {
            File file = new File(news.getFileUploadPath());
            if (file.exists()) {
                holder.tvMemory.setText("" + FileSizeUtil.getFileOrFilesSize(news.getFileUploadPath(), FileSizeUtil.SIZETYPE_MB) + "Mb");
                File files = new File(news.getFileUploadPath());
                File[] fileNew = files.listFiles();
                if (fileNew != null && fileNew.length > 0) {
                    for (int i = 0; i < fileNew.length; i++) {
                        if (fileNew[i].getPath().contains("jpg") || fileNew[i].getPath().contains("png") || fileNew[i].getPath().contains("Webp")) {
                            Glide.with(mContext).load(fileNew[i].getPath()).into(holder.customRoundAngleImageView);
                            break;
                        }
                    }
                }
            }
        }
    }

    public void setOnItemClickDownloadListener(OnItemClickDownloadListener onItemClickDownloadListener) {
        this.onItemClickDownloadListener = onItemClickDownloadListener;
    }

    public interface OnItemClickDownloadListener {
        void onClickDownLoad(TaskInfoMaterialAppDb appDb, DataViewHolder holder);
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    public void setOnDownLoadClick(TaskInfoMaterialAppDb appDb, DataViewHolder holder) {
        String downloadPath = new Constants(mContext).getMaterialDownFile() + System.currentTimeMillis() + "/";
        File dir = new File(downloadPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        appDb.setFileSavePath(downloadPath);
        TaskInfoMaterialAppDbUtils.updatePathByTaskId(appDb.getTaskId(), downloadPath);
        onItemClickDownloadListener.onClickDownLoad(appDb, holder);
    }

    public ArrayList<TaskInfoMaterialAppDb> getDataList() {
        return dataList;
    }

    public void setDataList(ArrayList<TaskInfoMaterialAppDb> dataList) {
        this.dataList.clear();
        this.dataList = dataList;
        notifyDataSetChanged();
    }

    public static class DataViewHolder extends RecyclerView.ViewHolder {

        TextView tvStatus;
        ImageView ivShowStatus;
        TextView tvTime;
        public ImageView ivPoint;
        TextView tvMemory;
        CustomRoundAngleImageView customRoundAngleImageView;

        public DataViewHolder(@NonNull View itemView) {
            super(itemView);
            tvStatus = itemView.findViewById(R.id.tv_status);
            ivShowStatus = itemView.findViewById(R.id.iv_show_status);
            tvTime = itemView.findViewById(R.id.tv_time);
            ivPoint = itemView.findViewById(R.id.iv_point);
            tvMemory = itemView.findViewById(R.id.tv_memory);
            customRoundAngleImageView = itemView.findViewById(R.id.iv_icon);
        }
    }
}
