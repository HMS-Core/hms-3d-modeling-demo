/*
 * Copyright (c) Huawei Technologies Co., Ltd. 2020-2021. All rights reserved.
 */

package com.huawei.hms.modeling3d.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.huawei.hms.modeling3d.R;
import com.huawei.hms.modeling3d.model.ConstantBean;
import com.huawei.hms.modeling3d.model.UserBean;
import com.huawei.hms.modeling3d.ui.widget.ScanBottomDialog;
import com.huawei.hms.modeling3d.ui.widget.ScanResolutionBottomDialog;
import com.huawei.hms.modeling3d.utils.BaseUtils;
import com.huawei.hms.modeling3d.utils.ToastUtil;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * SettingModelActivity
 *
 * @author HW
 * @since 2020-09-18
 */
public class SettingModelActivity extends AppCompatActivity implements ScanBottomDialog.SelectModelClick {

    @BindView(R.id.iv_back)
    ImageView ivBack;


    @BindView(R.id.rl_shooting_mode)
    RelativeLayout rlShootingMode;

    @BindView(R.id.tv_shooting_mode)
    TextView tvShootingMode;

    private String model;
    private String scanModel;

    int modelScreen;
    String rgbModel;


    UserBean userBean;

    public void getModel(int i) {
        modelScreen = i;
    }

    public void getScanModel(String model) {
        scanModel = model;
        tvShootingMode.setText(getScnStr(scanModel));
    }

    private String getScnStr(String modelScreen) {
        userBean.setSelectScanModel(modelScreen);

        if (modelScreen.equals(ConstantBean.SCAN_MODEL_TYPE_TWO)) {
            return getString(R.string.continuous_shooting_text);
        } else if (modelScreen.equals(ConstantBean.SCAN_MODEL_TYPE_ONE)) {
            return getString(R.string.click_shooting_text);
        }
        return null;
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setting_model_activity);
        ButterKnife.bind(this);
        userBean = BaseUtils.getUser(SettingModelActivity.this);
        if (userBean == null) {
            userBean = new UserBean();
        }
        model = userBean.getSelectBuildModel();
        modelScreen = userBean.getSelectResolutionModel();
        scanModel = userBean.getSelectScanModel();

        tvShootingMode.setText(getScnStr(scanModel));
    }

    private int getRgbText(String selectRGBMode) {
        if (selectRGBMode.equals(ConstantBean.TURNTABLE_MODE)) {
            return R.string.turntable_mode_text;
        } else {
            return R.string.normal_mode_text;
        }
    }

    @OnClick({ R.id.iv_back, R.id.rl_shooting_mode})
    public void onViewClicked(View view) {
        ScanResolutionBottomDialog bottomDialog;
        switch (view.getId()) {

            case R.id.iv_back:
                changePage();
                break;

            case R.id.rl_shooting_mode:
                bottomDialog = new ScanResolutionBottomDialog(SettingModelActivity.this, modelScreen, scanModel);
                bottomDialog.show();
                break;

        }

    }

    @Override
    public void modelClick(String clickModel) {
        userBean.setSelectBuildModel(clickModel);
        model = clickModel;
        if (clickModel.equals(getString(R.string.rgb))) {
            rlShootingMode.setVisibility(View.VISIBLE);
        } else {
            rlShootingMode.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            changePage();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        changePage();
    }

    public void changePage() {

        if (model.equals(getResources().getString(R.string.slam))) {
            ToastUtil.showToast(SettingModelActivity.this,"slam mode is temporarily unavailable");
        } else {
            userBean.setSelectBuildModel(getString(R.string.rgb));
            try {
                BaseUtils.saveUser(SettingModelActivity.this, userBean);
            } catch (Exception e) {
                e.printStackTrace();
            }
            Intent intent = new Intent(SettingModelActivity.this, NewScanActivity.class);
            startActivity(intent);
        }
        finish();
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);

    }


    public void getRgbModel(String normalMode) {
        rgbModel = normalMode;
        userBean.setSelectRGBMode(normalMode);
    }
}
