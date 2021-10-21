package com.huawei.hms.modeling3d.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.huawei.hms.modeling3d.R;
import com.huawei.hms.modeling3d.model.ConstantBean;
import com.huawei.hms.modeling3d.model.UserBean;
import com.huawei.hms.modeling3d.utils.BaseUtils;

import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class EmptySelectActivity extends AppCompatActivity {

    private Unbinder unbinder;

    UserBean userBean ;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.empty_select_activity);
        unbinder = ButterKnife.bind(this, this);
        userBean = BaseUtils.getUser(EmptySelectActivity.this);
        if (userBean==null){
            userBean = new UserBean();
        }
    }


    @OnClick({R.id.iv_back, R.id.ll_round_scan, R.id.ll_normal_scan})
    void onViewClicked(View view) {
        Intent intent = new Intent(EmptySelectActivity.this, NewScanActivity.class) ;
        switch (view.getId()) {
            case R.id.iv_back:
                finish();
                break;
            case R.id.ll_normal_scan :
                userBean.setSelectRGBMode(ConstantBean.NORMAL_MODE);
                startActivity(intent);
                finish();
                break;

            case R.id.ll_round_scan :
                userBean.setSelectRGBMode(ConstantBean.TURNTABLE_MODE);
                startActivity(intent);
                finish();
                break;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            BaseUtils.saveUser(EmptySelectActivity.this,userBean);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
    }
}
