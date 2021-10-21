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
package com.huawei.hms.modeling3d.utils;

import android.graphics.Canvas;
import android.opengl.GLSurfaceView;

import com.huawei.hms.modeling3d.model.FrameMetadata;
import com.huawei.hms.modeling3d.ui.widget.GraphicOverlay;

public class LocalDataManager {


    private float previewWidth;
    private float previewHeight;
    private float widthScaleValue = 1.0f;
    private float heightScaleValue = 1.0f;

    boolean isLandScape = false;
    private Integer imageMaxWidth;
    private Integer imageMaxHeight;

    public void setLandScape(boolean landScape) {
        isLandScape = landScape;
    }


    public void setCameraInfo(GLSurfaceView graphicOverlay, Canvas canvas, float width, float height) {
        this.previewWidth = width * graphicOverlay.getWidth();
        this.previewHeight = height * graphicOverlay.getHeight();
        if ((previewWidth != 0) && (previewHeight != 0)) {
            widthScaleValue = (float) canvas.getWidth() / (float) previewWidth;
            heightScaleValue = (float) canvas.getHeight() / (float) previewHeight;
        }
    }

    public Integer getImageMaxWidth(FrameMetadata hmsFrameMetadata) {
        if (imageMaxWidth == null) {
            if (isLandScape) {
                imageMaxWidth = hmsFrameMetadata.getHeight();
            } else {
                imageMaxWidth = hmsFrameMetadata.getWidth();
            }
        }
        return imageMaxWidth;
    }

    public Integer getImageMaxHeight(FrameMetadata hmsMLFrameMetadata) {
        if (imageMaxHeight == null) {
            if (isLandScape) {
                imageMaxHeight = hmsMLFrameMetadata.getWidth();
            } else {
                imageMaxHeight = hmsMLFrameMetadata.getHeight();
            }
        }
        return imageMaxHeight;
    }
}
