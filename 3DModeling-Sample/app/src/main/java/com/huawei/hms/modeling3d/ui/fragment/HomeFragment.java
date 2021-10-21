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
package com.huawei.hms.modeling3d.ui.fragment;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;


import com.huawei.hms.magicresource.db.DatabaseAppUtils;
import com.huawei.hms.magicresource.db.TaskInfoAppDbUtils;
import com.huawei.hms.magicresource.materialdb.DatabaseMaterialAppUtils;
import com.huawei.hms.magicresource.materialdb.TaskInfoMaterialAppDbUtils;
import com.huawei.hms.modeling3d.R;
import com.huawei.hms.modeling3d.model.UserBean;
import com.huawei.hms.modeling3d.ui.activity.CaptureMaterialActivity;
import com.huawei.hms.modeling3d.ui.activity.ChooserActivity;
import com.huawei.hms.modeling3d.ui.activity.EmptySelectActivity;
import com.huawei.hms.modeling3d.ui.activity.HistoryActivity;
import com.huawei.hms.modeling3d.ui.activity.NewScanActivity;
import com.huawei.hms.modeling3d.ui.widget.ScanBottomDialog;
import com.huawei.hms.modeling3d.utils.BaseUtils;

import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import pub.devrel.easypermissions.EasyPermissions;

/**
 * @Description: home
 * @Since: 2021-04-16
 */

public class HomeFragment extends Fragment {
    private Unbinder unbinder;

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

    UserBean userBean;

    AskPermissionInterface anInterface;

    public HomeFragment(AskPermissionInterface anInterface) {
        this.anInterface = anInterface;
    }

    public HomeFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.home_fragment_layout, null);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        DatabaseAppUtils.initDatabase(getContext());
        DatabaseMaterialAppUtils.initDatabase(getContext());
        userBean = BaseUtils.getUser(Objects.requireNonNull(getContext()));
        if (TaskInfoAppDbUtils.getAllTasks() != null) {
            tvModelNum.setText(String.valueOf(TaskInfoAppDbUtils.getAllTasks().size()));
        }
        if (TaskInfoMaterialAppDbUtils.getAllTasks() != null) {
            tvMaterialNum.setText(String.valueOf(TaskInfoMaterialAppDbUtils.getAllTasks().size()));
        }
    }

    @OnClick({R.id.rl_create_model, R.id.rl_create_material, R.id.rl_my_material, R.id.rl_my_model,R.id.rl_create_motion,R.id.rl_upload_file})
    public void onViewClicked(View view) {
        Intent intent = null;
        switch (view.getId()) {
            case R.id.rl_create_model:
                if (EasyPermissions.hasPermissions(getActivity(), PERMISSIONS)) {
                    if (userBean != null && userBean.getSelectBuildModel() != null) {
                        if (userBean.getSelectBuildModel().equals(getString(R.string.rgb))) {
                            Context context = getActivity().getApplicationContext();
                            Intent rgbIntent = new Intent(context, EmptySelectActivity.class);
                            startActivity(rgbIntent);
                        }
                    }
                } else {
                    anInterface.askFromType(1);
                    EasyPermissions.requestPermissions(getActivity(), "To use the app, you need to open permissions",
                            RC_CAMERA_AND_EXTERNAL_STORAGE, PERMISSIONS);
                }
                break;
            case R.id.rl_create_material:
                if (EasyPermissions.hasPermissions(getActivity(), PERMISSIONS)) {
                    Context context = getActivity().getApplicationContext();
                    Intent intents = new Intent(context, CaptureMaterialActivity.class);
                    startActivity(intents);
                } else {
                    anInterface.askFromType(2);
                    EasyPermissions.requestPermissions(getActivity(), "To use the app, you need to open permissions",
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
            case R.id.rl_create_motion:
                intent = new Intent(getContext(), ChooserActivity.class);
                startActivity(intent);
                break;

            case R.id.rl_upload_file:
                if (EasyPermissions.hasPermissions(getActivity(), PERMISSIONS)) {
                    ScanBottomDialog dialog = new ScanBottomDialog(getContext());
                    dialog.show();
                } else {
                    anInterface.askFromType(3);
                    EasyPermissions.requestPermissions(getActivity(), "To use the app, you need to open permissions",
                            RC_CAMERA_AND_EXTERNAL_STORAGE, PERMISSIONS);
                }
                break;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    public interface AskPermissionInterface {
        void askFromType(int type);
    }

}
