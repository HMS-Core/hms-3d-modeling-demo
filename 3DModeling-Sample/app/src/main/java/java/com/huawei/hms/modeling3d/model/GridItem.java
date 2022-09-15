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

package com.huawei.hms.modeling3d.model;

import android.graphics.Bitmap;

import com.huawei.hms.motioncapturesdk.Modeling3dMotionCaptureSkeleton;

import java.util.List;

public class GridItem {
    private Bitmap bitmap;

    private boolean isSelected = false;

    private List<Modeling3dMotionCaptureSkeleton> skeletonList;

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean isSelect) {
        isSelected = isSelect;
    }

    public List<Modeling3dMotionCaptureSkeleton> getSkeletonList() {
        return skeletonList;
    }

    public void setSkeletonList(List<Modeling3dMotionCaptureSkeleton> skeletonList) {
        this.skeletonList = skeletonList;
    }
}
