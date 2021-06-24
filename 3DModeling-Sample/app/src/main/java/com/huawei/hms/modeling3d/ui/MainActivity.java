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

package com.huawei.hms.modeling3d.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.os.Bundle;
import android.util.Log;
import android.widget.RadioGroup;

import com.huawei.hms.modelingresource.db.DatabaseAppUtils;
import com.huawei.hms.modelingresource.materialdb.DatabaseMaterialAppUtils;
import com.huawei.hms.modeling3d.R;

import java.util.List;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

/**
 * @Description: Home Page
 * @Since: 2021-04-16
 */

public class MainActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {

    RadioGroup radioGroupHome;
    HomeFragment homeFragment;
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String[] PERMISSIONS = {
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.INTERNET};

    private static final int RC_CAMERA_AND_EXTERNAL_STORAGE = 0x01 << 8;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initListener();
        if (EasyPermissions.hasPermissions(MainActivity.this, PERMISSIONS)) {
            Log.i(TAG, "Permissions OK");
        } else {
            EasyPermissions.requestPermissions(MainActivity.this, "To use this app, you need to enable the permission.",
                    RC_CAMERA_AND_EXTERNAL_STORAGE, PERMISSIONS);
        }
    }

    private void initView() {
        homeFragment = new HomeFragment();
        radioGroupHome = findViewById(R.id.main_menu);
        replaceFragment(homeFragment);
    }

    private void initListener() {
        radioGroupHome.setOnCheckedChangeListener(new RgOnCheckedChangeListener());
    }

    private void replaceFragment(Fragment fragment) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

        if (fragment != null) {
            ft.replace(R.id.frame_layout, fragment);
        }
        ft.commit();
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        Log.i(TAG, "permissions = " + perms);
        if (requestCode == RC_CAMERA_AND_EXTERNAL_STORAGE && PERMISSIONS.length == perms.size()) {
            initView();
            initListener();
        }
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            new AppSettingsDialog.Builder(this)
                    .setRequestCode(RC_CAMERA_AND_EXTERNAL_STORAGE)
                    .setRationale("To use this app, you need to enable the permission.")
                    .setTitle("Insufficient permissions")
                    .build()
                    .show();
        }
    }

    private final class RgOnCheckedChangeListener implements RadioGroup.OnCheckedChangeListener {
        @Override
        public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
            if (checkedId == R.id.rb_home) {
                replaceFragment(homeFragment);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DatabaseAppUtils.closeDatabase();
        DatabaseMaterialAppUtils.closeDatabase();
    }
}