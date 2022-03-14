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
import java.io.Serializable;

public class UserBean implements Serializable {
    private static final long serialVersionUID = -5713945027627603702L;
    private String selectScanModel = ConstantBean.SCAN_MODEL_TYPE_ONE ;
    private Integer selectResolutionModel = ConstantBean.SCREEN_MODEL_TYPE_ONE ;
    private String selectBuildModel = ConstantBean.SCAN_RGB_TYPE;
    private String selectRGBMode = ConstantBean.NORMAL_MODE ;

    public String getSelectRGBMode() {
        return selectRGBMode;
    }

    public void setSelectRGBMode(String selectRGBMode) {
        this.selectRGBMode = selectRGBMode;
    }

    private Boolean showScanTips = true ;
    private Boolean ShowMaterialTips = true ;

    public Boolean getShowScanTips() {
        return showScanTips;
    }

    public void setShowScanTips(Boolean showScanTips) {
        this.showScanTips = showScanTips;
    }

    public Boolean getShowMaterialTips() {
        return ShowMaterialTips;
    }

    public void setShowMaterialTips(Boolean showMaterialTips) {
        ShowMaterialTips = showMaterialTips;
    }

    public String getSelectScanModel() {
        return selectScanModel;
    }

    public void setSelectScanModel(String selectScanModel) {
        this.selectScanModel = selectScanModel;
    }

    public Integer getSelectResolutionModel() {
        return selectResolutionModel;
    }

    public void setSelectResolutionModel(Integer selectResolutionModel) {
        this.selectResolutionModel = selectResolutionModel;
    }

    public String getSelectBuildModel() {
        return selectBuildModel;
    }

    public void setSelectBuildModel(String selectBuildModel) {
        this.selectBuildModel = selectBuildModel;
    }

}
