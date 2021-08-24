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
import com.huawei.hms.magicresource.db.TaskInfoAppDb;
import com.huawei.hms.magicresource.db.TaskInfoAppDbUtils;
import com.huawei.hms.magicresource.util.Constants;
import com.huawei.hms.magicresource.util.FileSizeUtil;
import com.huawei.hms.magicresource.util.Utils;
import com.huawei.hms.magicresource.view.CustomRoundAngleImageView;
import com.huawei.hms.modeling3d.Modeling3dApp;
import com.huawei.hms.modeling3d.R;
import com.huawei.hms.modeling3d.model.ConstantBean;
import com.huawei.hms.modeling3d.ui.widget.HandlerPopDialog;
import com.huawei.hms.objreconstructsdk.cloud.Modeling3dReconstructEngine;
import com.huawei.hms.objreconstructsdk.cloud.Modeling3dReconstructPreviewListener;
import com.huawei.hms.objreconstructsdk.cloud.Modeling3dReconstructTaskUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Objects;

public class RecycleHistoryAdapter extends RecyclerView.Adapter<RecycleHistoryAdapter.DataViewHolder> {

    private ArrayList<TaskInfoAppDb> dataList;

    private Context mContext;

    private OnItemClickListener mOnItemClickListener = null;

    private OnItemClickDownloadListener onItemClickDownloadListener = null;

    public RecycleHistoryAdapter(ArrayList<TaskInfoAppDb> dataList, Context context) {
        this.dataList = dataList;
        this.mContext = context;
    }


    @NonNull
    @Override
    public DataViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.history_ls_item, parent, false);
        DataViewHolder viewHolder = new DataViewHolder(view);
        return viewHolder;
    }


    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull DataViewHolder holder, int position) {
        TaskInfoAppDb news = dataList.get(position);
        switch (news.getStatus()) {
            case ConstantBean.MODELS_INIT_STATUS:
                holder.tvStatus.setText(R.string.wait_to_upload_text);
                holder.tvStatus.setBackgroundResource(R.drawable.wait_to_upload_bg);
                holder.ivShowStatus.setVisibility(View.VISIBLE);
                holder.ivShowStatus.setImageResource(R.drawable.upload_data_icon);
                holder.ivShowStatus.setOnClickListener(v -> mOnItemClickListener.onClick(holder.ivShowStatus, position));
                break;
            case ConstantBean.MODELS_UPLOAD_COMPLETED_STATUS:
                holder.tvStatus.setText(R.string.wait_rebuild_text);
                holder.tvStatus.setBackgroundResource(R.drawable.wait_product_doing_bg);
                holder.ivShowStatus.setVisibility(View.VISIBLE);
                holder.ivShowStatus.setImageResource(R.drawable.product_doing_icon);
                holder.ivShowStatus.setOnClickListener(null);
                break;
            case ConstantBean.MODELS_RECONSTRUCT_START_STATUS:
                holder.tvStatus.setText(R.string.wait_rebuild_doing_text);
                holder.tvStatus.setBackgroundResource(R.drawable.product_doing_bg);
                holder.ivShowStatus.setVisibility(View.VISIBLE);
                holder.ivShowStatus.setImageResource(R.drawable.product_doing_icon);
                holder.ivShowStatus.setOnClickListener(null);
                break;
            case ConstantBean.MODELS_RECONSTRUCT_COMPLETED_STATUS:
                holder.tvStatus.setText(R.string.build_finish_text);
                holder.tvStatus.setBackgroundResource(R.drawable.finish_status_bg);
                holder.ivShowStatus.setVisibility(View.VISIBLE);
                holder.ivShowStatus.setImageResource(R.drawable.finish_doing_icon);
                holder.ivShowStatus.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                            Modeling3dReconstructEngine engine = Modeling3dReconstructEngine.getInstance(mContext);
                            engine.previewModel(news.getTaskId(), Modeling3dApp.app, new Modeling3dReconstructPreviewListener() {
                                @Override
                                public void onResult(String s, Object o) {

                                }

                                @Override
                                public void onError(String s, int i, String s1) {
                                    ((Activity)mContext).runOnUiThread(() -> {
                                        Toast.makeText(mContext, s1, Toast.LENGTH_LONG).show();
                                    });
                                }
                            });
                        }
                });
                break;
            case ConstantBean.MODELS_RECONSTRUCT_FAILED_STATUS:
                holder.tvStatus.setText(R.string.finish_fail_text);
                holder.tvStatus.setBackgroundResource(R.drawable.fail_status_bg);
                holder.ivShowStatus.setVisibility(View.INVISIBLE);

                break;
        }
        holder.tvTime.setText(Utils.systemCurrentToData(news.getCreateTime()));
        holder.ivPoint.setOnClickListener(v -> {
            Modeling3dReconstructTaskUtils magic3DReconstructTaskUtils = Modeling3dReconstructTaskUtils.getInstance(Modeling3dApp.app);
            new Thread(){
                @Override
                public void run() {
                    super.run();
                    int status = magic3DReconstructTaskUtils.queryTaskRestrictStatus(news.getTaskId());
                    if (news.getStatus() != ConstantBean.MODELS_RECONSTRUCT_START_STATUS) {
                        ((Activity)mContext).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                new HandlerPopDialog(mContext, RecycleHistoryAdapter.this, news, holder, dataList,status);
                            }
                        });
                    }
                }
            }.start();
        });

        holder.tvMemory.setText("" + FileSizeUtil.getFileOrFilesSize(news.getFileUploadPath(), FileSizeUtil.SIZETYPE_MB) + "Mb");
        File file = new File(news.getFileUploadPath());
        File[] files = file.listFiles();
        if (files != null && files.length > 0) {
            for (File value : files) {
                if (value.getPath().contains("jpg")||value.getPath().contains("png")||value.getPath().contains("Webp")) {
                    Glide.with(mContext).load(value.getPath()).into(holder.customRoundAngleImageView);
                    break;
                }
            }
        }
    }

    public void setOnItemClickListener(OnItemClickListener l) {
        this.mOnItemClickListener = l;
    }

    public void setOnItemClickDownloadListener(OnItemClickDownloadListener onItemClickDownloadListener) {
        this.onItemClickDownloadListener = onItemClickDownloadListener;
    }

    public interface OnItemClickListener {
        void onClick(View parent, int position);
    }

    public interface OnItemClickDownloadListener {
        void onClickDownLoad(TaskInfoAppDb appDb, DataViewHolder holder);
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    public void setOnDownLoadClick(TaskInfoAppDb appDb, DataViewHolder holder) {
        String save =  new Constants(mContext).getRgbDownFile() ;
        String downloadPath = save + System.currentTimeMillis() + "/";
        File dir = new File(downloadPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        appDb.setFileSavePath(downloadPath);
        TaskInfoAppDbUtils.updatePathByTaskId(appDb.getTaskId(), downloadPath);
        onItemClickDownloadListener.onClickDownLoad(appDb, holder);
    }

    public ArrayList<TaskInfoAppDb> getDataList() {
        return dataList;
    }

    public void setDataList(ArrayList<TaskInfoAppDb> dataList) {
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
