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

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;

import com.huawei.hms.motioncapturesdk.Modeling3dMotionCaptureSkeleton;

import java.util.List;

public class SkeletonGraphic extends GraphicOverlay.Graphic {
    private List<Modeling3dMotionCaptureSkeleton> skeletons;

    private Paint circlePaint;

    private Paint linePaint;

    /**
     * Construction method
     *
     * @param overlay   Custom view
     * @param skeletons Bone data points
     */
    public SkeletonGraphic(GraphicOverlay overlay, List<Modeling3dMotionCaptureSkeleton> skeletons) {
        super(overlay);
        this.skeletons = skeletons;
        circlePaint = new Paint();
        circlePaint.setColor(Color.RED);
        circlePaint.setStyle(Paint.Style.FILL);
        circlePaint.setAntiAlias(true);

        linePaint = new Paint();
        linePaint.setColor(Color.GREEN);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setStrokeWidth(10f);
        linePaint.setAntiAlias(true);
    }

    @Override
    public void draw(Canvas canvas) {
    }

}
