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

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.huawei.hms.modelingresource.db.DatabaseAppUtils;
import com.huawei.hms.modelingresource.db.TaskInfoAppDbUtils;
import com.huawei.hms.modelingresource.materialdb.DatabaseMaterialAppUtils;
import com.huawei.hms.modelingresource.materialdb.TaskInfoMaterialAppDbUtils;
import com.huawei.hms.modeling3d.R;
import com.huawei.hms.modeling3d.ui.modelingui.ScanActivity;
import com.huawei.hms.modeling3d.ui.modelingui.CaptureMaterialActivity;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

/**
 * @Description: Home Page
 * @Since: 2021-04-16
 */

public class HomeFragment extends Fragment implements EasyPermissions.PermissionCallbacks {
    private Unbinder unbinder;

    private static final String TAG = HomeFragment.class.getSimpleName();

    @BindView(R.id.rl_create_model)
    RelativeLayout rlCreateModel;
    @BindView(R.id.tv_model_num)
    TextView tvModelNum;
    @BindView(R.id.tv_material_num)
    TextView tvMaterialNum;

    private static final String[] PERMISSIONS = {
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.INTERNET};

    private static final int RC_CAMERA_AND_EXTERNAL_STORAGE = 0x01 << 8;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.home_fragment_layout, null);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @OnClick({R.id.rl_create_model, R.id.rl_create_material, R.id.rl_my_material, R.id.rl_my_model})
    public void onViewClicked(View view) {
        Intent intent = null;
        switch (view.getId()) {
            case R.id.rl_create_model:
                if (EasyPermissions.hasPermissions(getActivity(), PERMISSIONS)) {
                    Context context = getActivity().getApplicationContext();
                    intent = new Intent(context, ScanActivity.class);
                    startActivity(intent);
                } else {
                    EasyPermissions.requestPermissions(getActivity(), "Requires relevant permissions",
                            RC_CAMERA_AND_EXTERNAL_STORAGE, PERMISSIONS);
                }
                break;
            case R.id.rl_create_material:
                if (EasyPermissions.hasPermissions(getActivity(), PERMISSIONS)) {
                    Context context = getActivity().getApplicationContext();
                    intent = new Intent(context, CaptureMaterialActivity.class);
                    startActivity(intent);
                } else {
                    EasyPermissions.requestPermissions(getActivity(), "Requires relevant permissions",
                            RC_CAMERA_AND_EXTERNAL_STORAGE, PERMISSIONS);
                }
                break;
            case R.id.rl_my_material:
                intent = new Intent(getContext(), HistoryActivity.class);
                intent.putExtra("index", 1);
                startActivity(intent);
                break;
            case R.id.rl_my_model:
                intent = new Intent(getContext(), HistoryActivity.class);
                intent.putExtra("index", 0);
                startActivity(intent);
                break;
            default:
                break;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        Log.i(TAG, "permissions = " + perms);
    }

    @Override
    public void onResume() {
        super.onResume();
        DatabaseAppUtils.initDatabase(getContext());
        DatabaseMaterialAppUtils.initDatabase(getContext());
        tvModelNum.setText(String.valueOf(TaskInfoAppDbUtils.getAllTasks().size()));
        tvMaterialNum.setText(String.valueOf(TaskInfoMaterialAppDbUtils.getAllTasks().size()));
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            new AppSettingsDialog.Builder(this)
                    .setRequestCode(RC_CAMERA_AND_EXTERNAL_STORAGE)
                    .setRationale("Information (supplemented)")
                    .setTitle("Title (Specific Supplement)")
                    .build()
                    .show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }
}
