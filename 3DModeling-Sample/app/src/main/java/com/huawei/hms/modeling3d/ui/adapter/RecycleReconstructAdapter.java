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

package com.huawei.hms.modeling3d.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.huawei.cameratakelib.utils.LogUtil;
import com.huawei.hms.modelingresource.db.TaskInfoAppDb;
import com.huawei.hms.modelingresource.db.TaskInfoAppDbUtils;
import com.huawei.hms.modelingresource.util.Constants;
import com.huawei.hms.modelingresource.util.FileSizeUtil;
import com.huawei.hms.modelingresource.util.Utils;
import com.huawei.hms.modelingresource.view.CustomRoundAngleImageView;
import com.huawei.hms.modeling3d.R;
import com.huawei.hms.modeling3d.ui.modelingui.widget.HandlerPopDialog;
import com.huawei.hms.objreconstructsdk.Modeling3dReconstructConstants;

import java.io.File;
import java.util.ArrayList;


public class RecycleReconstructAdapter extends RecyclerView.Adapter<RecycleReconstructAdapter.DataViewHolder> {

    private final static String TAG = RecycleReconstructAdapter.class.getSimpleName();

    private ArrayList<TaskInfoAppDb> dataList;

    private Context mContext;

    private OnItemClickListener mOnItemClickListener = null;

    private OnItemClickDownloadListener onItemClickDownloadListener = null;

    public RecycleReconstructAdapter(ArrayList<TaskInfoAppDb> dataList, Context context) {
        this.dataList = dataList;
        this.mContext = context;
    }


    @NonNull
    @Override
    public DataViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.history_ls_item, parent, false);
        return new DataViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull DataViewHolder holder, int position) {
        TaskInfoAppDb news = dataList.get(position);
        switch (news.getStatus()) {
            case Modeling3dReconstructConstants.ProgressStatus.INITED:
                holder.tvStatus.setText(R.string.wait_to_upload_text);
                holder.tvStatus.setBackgroundResource(R.drawable.wait_to_upload_bg);
                holder.ivShowStatus.setVisibility(View.VISIBLE);
                holder.ivShowStatus.setImageResource(R.drawable.upload_data_icon);
                // Upload
                holder.ivShowStatus.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mOnItemClickListener.onClick(holder.ivShowStatus, position);
                    }
                });
                break;
            case Modeling3dReconstructConstants.ProgressStatus.UPLOAD_COMPLETED:
                holder.tvStatus.setText(R.string.wait_rebuild_text);
                holder.tvStatus.setBackgroundResource(R.drawable.wait_product_doing_bg);
                holder.ivShowStatus.setVisibility(View.VISIBLE);
                holder.ivShowStatus.setImageResource(R.drawable.product_doing_icon);
                holder.ivShowStatus.setOnClickListener(null);
                break;
            case Modeling3dReconstructConstants.ProgressStatus.RECONSTRUCT_START:
                holder.tvStatus.setText(R.string.wait_rebuild_doing_text);
                holder.tvStatus.setBackgroundResource(R.drawable.product_doing_bg);
                holder.ivShowStatus.setVisibility(View.VISIBLE);
                holder.ivShowStatus.setImageResource(R.drawable.product_doing_icon);
                holder.ivShowStatus.setOnClickListener(null);
                break;
            case Modeling3dReconstructConstants.ProgressStatus.RECONSTRUCT_COMPLETED:
                holder.tvStatus.setText(R.string.finish_text);
                holder.tvStatus.setBackgroundResource(R.drawable.finish_status_bg);
                holder.ivShowStatus.setVisibility(View.INVISIBLE);
                break;
            case Modeling3dReconstructConstants.ProgressStatus.RECONSTRUCT_FAILED:
                holder.tvStatus.setText(R.string.finish_fail_text);
                holder.tvStatus.setBackgroundResource(R.drawable.fail_status_bg);
                holder.ivShowStatus.setVisibility(View.INVISIBLE);
                break;
            default:
        }
        holder.tvTime.setText(Utils.systemCurrentToData(news.getCreateTime()));
        // Set Download
        holder.ivPoint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (news.getStatus() == Modeling3dReconstructConstants.ProgressStatus.INITED ||
                        news.getStatus() == Modeling3dReconstructConstants.ProgressStatus.UPLOAD_COMPLETED ||
                        news.getStatus() == Modeling3dReconstructConstants.ProgressStatus.RECONSTRUCT_COMPLETED ||
                        news.getStatus() == Modeling3dReconstructConstants.ProgressStatus.RECONSTRUCT_FAILED) {
                    new HandlerPopDialog(mContext, RecycleReconstructAdapter.this, news, holder, dataList);
                }
            }
        });

        holder.tvMemory.setText("" + FileSizeUtil.getFileOrFilesSize(news.getFileUploadPath(), FileSizeUtil.SIZETYPE_MB) + "Mb");
        File file = new File(news.getFileUploadPath());
        File[] files = file.listFiles();
        if (files != null && files.length > 0) {
            for (File mFile : files) {
                if (mFile.getPath().contains("jpg")) {
                    Glide.with(mContext).load(mFile.getPath()).into(holder.customRoundAngleImageView);
                    break;
                }
            }
        }
    }

    // Set the click event.
    public void setOnItemClickListener(OnItemClickListener l) {
        this.mOnItemClickListener = l;
    }

    public void setOnItemClickDownloadListener(OnItemClickDownloadListener onItemClickDownloadListener) {
        this.onItemClickDownloadListener = onItemClickDownloadListener;
    }

    // Interface for Clicking to Upload an Event
    public interface OnItemClickListener {
        void onClick(View parent, int position);
    }

    // Click Download Event Interface
    public interface OnItemClickDownloadListener {
        void onClickDownLoad(TaskInfoAppDb appDb, DataViewHolder holder);
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    // Click Download.
    public void setOnDownLoadClick(TaskInfoAppDb appDb, DataViewHolder holder) {
        String save = new Constants(mContext).getRgbDownFile();
        String downloadPath = save + System.currentTimeMillis() + "/";
        File dir = new File(downloadPath);
        if (!dir.exists()) {
            boolean isCreate = dir.mkdirs();
            if (isCreate){
                LogUtil.d("create successful");
            }
        }
        appDb.setFileSavePath(downloadPath);
        TaskInfoAppDbUtils.updatePathByTaskId(appDb.getTaskId(), downloadPath);
        onItemClickDownloadListener.onClickDownLoad(appDb, holder);
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
