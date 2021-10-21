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

import java.util.ArrayList;
import java.util.List;

public class SkeletonSaveBean {
    private List<List<List<Float>>> joints3d = new ArrayList<>();
    private List<List<List<Float>>> quaternion = new ArrayList<>();
    private List<List<Float>> trans = new ArrayList<>();

    public List<List<Float>> getTrans() {
        return trans;
    }

    public void setTrans(List<List<Float>> trans) {
        this.trans = trans;
    }
    public List<List<List<Float>>> getJoints3d() {
        return joints3d;
    }

    public void setJoints3d(List<List<List<Float>>> joints3d) {
        this.joints3d = joints3d;
    }

    public List<List<List<Float>>> getQuaternion() {
        return quaternion;
    }

    public void setQuaternion(List<List<List<Float>>> quaternion) {
        this.quaternion = quaternion;
    }
}
