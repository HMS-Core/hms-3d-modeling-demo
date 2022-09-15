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
package com.huawei.hms.modeling3d.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityCompat.OnRequestPermissionsResultCallback;
import androidx.core.content.ContextCompat;

import com.huawei.hms.modeling3d.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Demo app chooser which takes care of runtime permission requesting and allows you to pick from
 * all available testing Activities.
 */
public final class ChooserActivity extends AppCompatActivity
        implements OnRequestPermissionsResultCallback, View.OnClickListener {
    private static final String TAG = "ChooserActivity";
    private static final int PERMISSION_REQUESTS = 1;

    // Whether the drawing of the camera preview interface is asynchronous, if it is synchronous, it will be very stuck frame by frame
    private static boolean isAsynchronous = false;

    // In the template (including the quantity that comes with the SDK and manually generated)
    private static int mCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        setContentView(R.layout.activity_chooser);
        findViewById(R.id.live).setOnClickListener(this);
        findViewById(R.id.still).setOnClickListener(this);
        findViewById(R.id.photo).setOnClickListener(this);
        if (!allPermissionsGranted()) {
            getRuntimePermissions();
        }

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.live:
                startActivity(new Intent(ChooserActivity.this, HumanSkeletonActivity.class));
                break;
            case R.id.still:
                startActivity(new Intent(ChooserActivity.this, SelectSourceVideoActivity.class));
                break;
            case R.id.photo:
                startActivity(new Intent(ChooserActivity.this, SelectSourcePhotoActivity.class));
                break;
        }
    }

    private String[] getRequiredPermissions() {
        try {
            PackageInfo info =
                    this.getPackageManager()
                            .getPackageInfo(this.getPackageName(), PackageManager.GET_PERMISSIONS);
            String[] ps = info.requestedPermissions;
            if (ps != null && ps.length > 0) {
                return ps;
            } else {
                return new String[0];
            }
        } catch (Exception e) {
            return new String[0];
        }
    }

    private boolean allPermissionsGranted() {
        for (String permission : getRequiredPermissions()) {
            if (!isPermissionGranted(this, permission)) {
                return false;
            }
        }
        return true;
    }

    private void getRuntimePermissions() {
        List<String> allNeededPermissions = new ArrayList<>();
        for (String permission : getRequiredPermissions()) {
            if (!isPermissionGranted(this, permission)) {
                allNeededPermissions.add(permission);
            }
        }
        if (!allNeededPermissions.isEmpty()) {
            ActivityCompat.requestPermissions(
                    this, allNeededPermissions.toArray(new String[0]), PERMISSION_REQUESTS);
        }
    }

    private static boolean isPermissionGranted(Context context, String permission) {
        if (ContextCompat.checkSelfPermission(context, permission)
                == PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "Permission granted: " + permission);
            return true;
        }
        Log.i(TAG, "Permission NOT granted: " + permission);
        return false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mCount = 0;
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    public static boolean isAsynchronous() {
        return isAsynchronous;
    }

    public static void setIsAsynchronous(boolean isAsynchronous) {
        ChooserActivity.isAsynchronous = isAsynchronous;
    }

}
