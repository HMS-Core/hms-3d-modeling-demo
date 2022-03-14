package com.huawei.hms.modeling3d.ui.widget;

import android.app.Dialog;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.huawei.hms.magicresource.util.Utils;
import com.huawei.hms.modeling3d.R;
import com.huawei.hms.modeling3d.model.ConstantBean;
import com.huawei.hms.modeling3d.ui.activity.SettingModelActivity;

public class ScanResolutionBottomDialog extends Dialog {

    SettingModelActivity mContext;
    RelativeLayout rlRgb;
    RelativeLayout rlSlam;
    RelativeLayout rl3K;
    TextView tvCancel;
    ImageView ivRgb;
    ImageView ivSlam;
    ImageView iv3k;
    TextView tvSlam;
    TextView tvRgb;
    TextView textViewUpper;
    String scanModel = null;
    int modelScreen;

    public ScanResolutionBottomDialog(@NonNull SettingModelActivity context, int modelScreen, String model) {
        super(context, R.style.BottomAnimDialogStyle);
        this.mContext = context;
        this.modelScreen = modelScreen;
        this.scanModel = model;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
    }

    private void init() {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(mContext.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.scan_resolution_bottom_dialog_layout, null);
        Window window = this.getWindow();
        if (window != null) {
            window.setGravity(Gravity.BOTTOM);
            WindowManager.LayoutParams lp = window.getAttributes();
            lp.y = Utils.dip2px(mContext, 16);
            window.setAttributes(lp);
        }
        initView(view);
        setContentView(view);
    }

    private void initView(View view) {
        textViewUpper = view.findViewById(R.id.tv_title_head);
        rlRgb = view.findViewById(R.id.rl_rgb);
        rlSlam = view.findViewById(R.id.rl_slam);
        tvCancel = view.findViewById(R.id.tv_cancel);
        ivRgb = view.findViewById(R.id.iv_rgb);
        ivSlam = view.findViewById(R.id.iv_slam);
        rl3K = view.findViewById(R.id.rl_3k);
        iv3k = view.findViewById(R.id.iv_3k);
        tvSlam = view.findViewById(R.id.tv_slam);
        tvRgb = view.findViewById(R.id.tv_rgb);
        if (scanModel != null) {
            textViewUpper.setText(R.string.ShotMode_setting_text);
            rl3K.setVisibility(View.GONE);
            tvRgb.setText(R.string.continuous_shooting_text);
            tvSlam.setText(R.string.click_shooting_text);
            if (scanModel.equals(ConstantBean.SCAN_MODEL_TYPE_ONE)) {
                ivRgb.setImageResource(R.drawable.unselect_rb_icon);
                ivSlam.setImageResource(R.drawable.select_rb_icon);
            } else if (scanModel.equals(ConstantBean.SCAN_MODEL_TYPE_TWO)) {
                ivRgb.setImageResource(R.drawable.select_rb_icon);
                ivSlam.setImageResource(R.drawable.unselect_rb_icon);
            }
        } else {
            if (modelScreen == ConstantBean.SCREEN_MODEL_TYPE_ZERO) {
                ivRgb.setImageResource(R.drawable.unselect_rb_icon);
                ivSlam.setImageResource(R.drawable.select_rb_icon);
                iv3k.setImageResource(R.drawable.unselect_rb_icon);
            } else if (modelScreen == ConstantBean.SCREEN_MODEL_TYPE_ONE) {
                ivRgb.setImageResource(R.drawable.select_rb_icon);
                ivSlam.setImageResource(R.drawable.unselect_rb_icon);
                iv3k.setImageResource(R.drawable.unselect_rb_icon);
            } else {
                ivRgb.setImageResource(R.drawable.unselect_rb_icon);
                ivSlam.setImageResource(R.drawable.unselect_rb_icon);
                iv3k.setImageResource(R.drawable.select_rb_icon);
            }
        }
        rlSlam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ivRgb.setImageResource(R.drawable.unselect_rb_icon);
                ivSlam.setImageResource(R.drawable.select_rb_icon);
                iv3k.setImageResource(R.drawable.unselect_rb_icon);
                if (scanModel == null) {
                    mContext.getModel(ConstantBean.SCREEN_MODEL_TYPE_ZERO);
                } else {
                    mContext.getScanModel(ConstantBean.SCAN_MODEL_TYPE_ONE);
                }
                dismiss();
            }
        });
        rlRgb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ivRgb.setImageResource(R.drawable.select_rb_icon);
                ivSlam.setImageResource(R.drawable.unselect_rb_icon);
                iv3k.setImageResource(R.drawable.unselect_rb_icon);
                if (scanModel == null) {
                    mContext.getModel(ConstantBean.SCREEN_MODEL_TYPE_ONE);
                } else {
                    mContext.getScanModel(ConstantBean.SCAN_MODEL_TYPE_TWO);
                }
                dismiss();
            }
        });

        rl3K.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ivRgb.setImageResource(R.drawable.unselect_rb_icon);
                ivSlam.setImageResource(R.drawable.unselect_rb_icon);
                iv3k.setImageResource(R.drawable.select_rb_icon);
                mContext.getModel(ConstantBean.SCREEN_MODEL_TYPE_TWO);
                dismiss();
            }
        });
        tvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }
}
