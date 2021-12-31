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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;
import android.widget.RadioGroup;

import com.huawei.hms.magicresource.db.DatabaseAppUtils;
import com.huawei.hms.magicresource.materialdb.DatabaseMaterialAppUtils;
import com.huawei.hms.modeling3d.R;
import com.huawei.hms.modeling3d.model.UserBean;
import com.huawei.hms.modeling3d.ui.fragment.HomeFragment;
import com.huawei.hms.modeling3d.utils.BaseUtils;

import java.util.List;

import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

/**
 * @Description: Home page
 * @Since: 2021-04-16
 */
public class MainActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks , HomeFragment.AskPermissionInterface {

    HomeFragment homeFragment;

    private static final int RC_CAMERA_AND_EXTERNAL_STORAGE = 0x01 << 8;

    UserBean bean ;
    int permissionType ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void initView() {
        homeFragment = new HomeFragment(this);
        replaceFragment(homeFragment);
        bean = BaseUtils.getUser(MainActivity.this);
        if (bean==null){
            bean = new UserBean();
            try {
                BaseUtils.saveUser(MainActivity.this,bean);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void replaceFragment(Fragment fragment){
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

        if (fragment!=null) {
            ft.replace(R.id.frame_layout, fragment);
        }
        ft.commit();
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        if (perms.size()>3) {

            if (permissionType==1){
                Intent rgbIntent = new Intent(MainActivity.this, NewScanActivity.class);
                startActivity(rgbIntent);
            }else if (permissionType==2){
                Intent intent = new Intent(MainActivity.this, CaptureMaterialActivity.class);
                startActivity(intent);
            }

        }
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            new AppSettingsDialog.Builder(this)
                    .setRequestCode(RC_CAMERA_AND_EXTERNAL_STORAGE)
                    .setRationale("Prompt description information (specific supplement)")
                    .setTitle("Title (specific supplement)")
                    .build()
                    .show();
        }
    }

    @Override
    public void askFromType(int type) {
        permissionType = type ;
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        DatabaseAppUtils.closeDatabase();
        DatabaseMaterialAppUtils.closeDatabase();
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

}