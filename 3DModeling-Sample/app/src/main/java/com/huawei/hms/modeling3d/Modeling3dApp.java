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
package com.huawei.hms.modeling3d;

import android.app.Application;
import android.content.Context;

import com.huawei.hms.magicresource.db.DatabaseAppUtils;
import com.huawei.hms.magicresource.materialdb.DatabaseMaterialAppUtils;
import com.huawei.hms.magicresource.util.OverseasContextWrapper;
import com.huawei.hms.materialgeneratesdk.MaterialGenApplication;
import com.huawei.hms.motioncapturesdk.MotionCaptureApplication;
import com.huawei.hms.objreconstructsdk.ReconstructApplication;

import me.jessyan.autosize.AutoSizeConfig;
import me.jessyan.autosize.unit.Subunits;

/**
 * @Description:  App
 * @Since: 2021-04-16
 */

public class Modeling3dApp extends Application {

    public static Modeling3dApp app;

    @Override
    public void onCreate() {
        super.onCreate();
        DatabaseAppUtils.initDatabase(this);
        DatabaseMaterialAppUtils.initDatabase(this);
        app = this;
        initAutoSize();
//        ReconstructApplication.getInstance().setAccessToken("CgB6e3x9SrM7/s4Qi7qY/vCJCO2HpHaIzp6KD4OEsxuTTktN5OEQEdyjzBS1o6Hz/BctpcdRRfuPU0sHK+RQ6ts1");
//        MaterialGenApplication.getInstance().setApiKey("CgB6e3x9SrM7/s4Qi7qY/vCJCO2HpHaIzp6KD4OEsxuTTktN5OEQEdyjzBS1o6Hz/BctpcdRRfuPU0sHK+RQ6ts1");

        //Dynamic loading of dynamic paving model
        MotionCaptureApplication.getInstance();
    }

    private void initAutoSize() {
        AutoSizeConfig.getInstance().getUnitsManager()
                .setSupportDP(false)
                .setSupportSP(false)
                .setSupportSubunits(Subunits.MM);
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(OverseasContextWrapper.wrap(base,"en"));
    }

}
