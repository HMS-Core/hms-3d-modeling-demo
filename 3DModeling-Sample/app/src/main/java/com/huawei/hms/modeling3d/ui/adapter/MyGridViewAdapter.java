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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.huawei.hms.modeling3d.R;
import com.huawei.hms.modeling3d.model.GridItem;

import java.util.List;

public class MyGridViewAdapter extends BaseAdapter {
    private Context mContext;

    private List<GridItem> mDataList;

    /**
     * construct method
     *
     * @param dataList list
     * @param context context
     */
    public MyGridViewAdapter(List<GridItem> dataList, Context context) {
        super();
        mDataList = dataList;
        this.mContext = context;
    }

    public int getCount() {
        return mDataList.size();
    }

    public Object getItem(int position) {
        return mDataList.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        try {
            if (convertView == null) {
                convertView = LayoutInflater.from(mContext).inflate(R.layout.imageseg_gridview, null);
                holder = new ViewHolder();
                holder.imageView = convertView.findViewById(R.id.image);
                holder.selectIcon = convertView.findViewById(R.id.img_select);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            Bitmap bitmap = mDataList.get(position).getBitmap();
            Glide.with(mContext).load(bitmap).error(R.drawable.modeling3d2).into(holder.imageView);
        } catch (Exception e) {
            Log.e("MyGridViewAdapter", e.getMessage());
        }
        return convertView;
    }

    static class ViewHolder {
        ImageView imageView;

        ImageView selectIcon;
    }
}
