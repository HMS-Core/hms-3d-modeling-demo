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

package com.huawei.hms.modeling3d;

import android.app.Application;
import android.content.Context;
import androidx.multidex.MultiDex;

import com.huawei.hms.modelingresource.db.DatabaseAppUtils;
import com.huawei.hms.modelingresource.materialdb.DatabaseMaterialAppUtils;
import com.huawei.hms.modelingresource.util.OverseasContextWrapper;

import me.jessyan.autosize.AutoSizeConfig;
import me.jessyan.autosize.unit.Subunits;

/**
 * @Description:
 * @Since: 2021-04-16
 */

public class Modeling3dDemo extends Application {

    private static Modeling3dDemo app;

    @Override
    public void onCreate() {
        super.onCreate();
        // Initializing the Database
        DatabaseAppUtils.initDatabase(this);
        DatabaseMaterialAppUtils.initDatabase(this);
        setApp(this);
        initAutoSize();
        // Setting the Application Token: MaterialGenApplication.getInstance().setAccessToken("MaterialGenerateToken");
        // Setting the Application Token: ReconstructApplication.getInstance().setAccessToken("ObjectReconstructToken");
    }

    private void initAutoSize() {
        AutoSizeConfig.getInstance().getUnitsManager()
                .setSupportDP(false)
                .setSupportSP(false)
                .setSupportSubunits(Subunits.MM);
    }

    public static Modeling3dDemo getApp() {
        return app;
    }

    public static void setApp(Modeling3dDemo app) {
        Modeling3dDemo.app = app;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(OverseasContextWrapper.wrap(base, "en"));
        MultiDex.install(base);
    }
}
