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

import com.huawei.hms.modeling3d.R;

import java.io.IOException;
import java.util.ArrayList;

public class RecycleImageAdapter extends RecyclerView.Adapter<RecycleImageAdapter.DataViewHolder> {

    private ArrayList<String> imagePaths;
    private Context mContext;
    private Paint paint;
    private Paint paint1;
    private OnItemClickListener mOnItemClickListener;

    public RecycleImageAdapter(ArrayList<String> imagePaths, Context context) {
        this.imagePaths = imagePaths;
        this.mContext = context;
        paint = new Paint();
        paint1 = new Paint();
    }

    @NonNull
    @Override
    public DataViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_image_material, parent, false);
        return new RecycleImageAdapter.DataViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DataViewHolder holder, int position) {
        holder.ivDelete.setOnClickListener(v -> mOnItemClickListener.onClick(holder.ivDelete, position));
        String imagePath = imagePaths.get(position);
        Bitmap bitmap  = getThumb(imagePath);
        if(bitmap!=null) {
            int angle=50;
            int strokeWidth=5;
            Bitmap temp=Bitmap.createBitmap(bitmap.getWidth(),bitmap.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(temp);
            paint.setShader(new BitmapShader(bitmap, BitmapShader.TileMode.CLAMP, BitmapShader.TileMode.CLAMP));
            paint.setAntiAlias(true);
            RectF rectF = new RectF(0f, 0f, bitmap.getWidth(), bitmap.getHeight());
            canvas.drawRoundRect(rectF, angle, angle, paint);
            paint1.setStyle(Paint.Style.STROKE);
            paint1.setStrokeWidth(strokeWidth);
            RectF rectF2 = new RectF(strokeWidth / 2, strokeWidth / 2, bitmap.getWidth() - strokeWidth / 2, bitmap.getHeight() - strokeWidth / 2);
            paint1.setColor(ContextCompat.getColor(mContext, R.color.bottom_whit));
            canvas.drawRoundRect(rectF2, angle, angle, paint1);
            holder.ivMaterial.setImageBitmap(temp);
        }
    }

    private Bitmap getThumb(String path){
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);
        int reqWidth;
        int reqHeight;
        int width=options.outWidth;
        int height=options.outHeight;
        if (width > height){
            reqWidth = 198;
            reqHeight = (reqWidth * height)/width;
        }else{
            reqHeight = 198;
            reqWidth = (width * reqHeight)/height;
        }
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }
        try{
            options.inSampleSize = inSampleSize;
            options.inJustDecodeBounds = false;
            Matrix mat = new Matrix();
            Bitmap bitmap = BitmapFactory.decodeFile(path, options);
            ExifInterface ei =  new ExifInterface(path);
            int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            switch(orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    mat.postRotate(90);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    mat.postRotate(180);
                    break;
            }
            return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), mat, true);
        }catch (IOException e){
            return null;
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

        ImageView ivMaterial;
        ImageView ivDelete;

        public DataViewHolder(@NonNull View itemView) {
            super(itemView);
            ivMaterial = itemView.findViewById(R.id.iv_material);
            ivDelete = itemView.findViewById(R.id.iv_delete);
        }
    }
}
