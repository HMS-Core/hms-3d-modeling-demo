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

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.media.ExifInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.huawei.hms.magicresource.view.CustomRoundAngleImageView;
import com.huawei.hms.modeling3d.R;

import java.io.IOException;
import java.util.ArrayList;

public class RecycleBondImageAdapter extends RecyclerView.Adapter<RecycleBondImageAdapter.DataViewHolder> {

    private ArrayList<String> imagePaths;
    private Context mContext;
    private Paint paint;
    private Paint paint1;
    private OnItemClickListener mOnItemClickListener;

    public RecycleBondImageAdapter(ArrayList<String> imagePaths, Context context) {
        this.imagePaths = imagePaths;
        this.mContext = context;
        paint = new Paint();
        paint1 = new Paint();
    }

    @NonNull
    @Override
    public DataViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_image_bond, parent, false);
        return new RecycleBondImageAdapter.DataViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DataViewHolder holder, int position) {
        holder.ivDelete.setOnClickListener(v -> mOnItemClickListener.onClick(holder.ivDelete, position));
        String imagePath = imagePaths.get(position);
        Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
        if (bitmap != null) {
            holder.ivMaterial.setImageBitmap(bitmap);
        }
    }


    @Override
    public int getItemCount() {
        return imagePaths.size();
    }

    public void setOnItemClickListener(OnItemClickListener l) {
        this.mOnItemClickListener = l;
    }

    public interface OnItemClickListener {
        void onClick(View parent, int position);
    }

    public static class DataViewHolder extends RecyclerView.ViewHolder {

        CustomRoundAngleImageView ivMaterial;
        ImageView ivDelete;

        public DataViewHolder(@NonNull View itemView) {
            super(itemView);
            ivMaterial = itemView.findViewById(R.id.iv_material);
            ivDelete = itemView.findViewById(R.id.iv_delete);
        }
    }
}
