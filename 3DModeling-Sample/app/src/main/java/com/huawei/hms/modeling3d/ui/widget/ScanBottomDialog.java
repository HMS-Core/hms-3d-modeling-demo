package com.huawei.hms.modeling3d.ui.widget;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
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
import com.huawei.hms.modeling3d.ui.activity.FilePickerActivity;

public class ScanBottomDialog extends Dialog {

    Context mContext;
    RelativeLayout rlRgb;
    RelativeLayout rlSlam;
    TextView tvCancel;
    ImageView ivRgb;
    ImageView ivSlam;
    SelectModelClick clickListener;
    String model;

    TextView tvRound;
    TextView tvCommon;
    TextView tvTitle;
    TextView tvLeft;
    String uploadType;

    public interface SelectModelClick {
        void modelClick(String model);
    }

    public ScanBottomDialog(@NonNull Context context, SelectModelClick click, String defaultModel) {
        super(context, R.style.BottomAnimDialogStyle);
        this.mContext = context;
        clickListener = click;
        this.model = defaultModel;
    }

    // model is null choose the directory to use
    public ScanBottomDialog(@NonNull Context context) {
        super(context, R.style.BottomAnimDialogStyle);
        this.mContext = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
    }

    private void init() {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(mContext.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.scan_bottom_dialog_layout, null);
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
        rlRgb = view.findViewById(R.id.rl_rgb);
        rlSlam = view.findViewById(R.id.rl_slam);
        tvCancel = view.findViewById(R.id.tv_cancel);
        ivRgb = view.findViewById(R.id.iv_rgb);
        ivSlam = view.findViewById(R.id.iv_slam);
        tvRound = view.findViewById(R.id.tv_round);
        tvCommon = view.findViewById(R.id.tv_common);
        tvTitle = view.findViewById(R.id.tv_title);
        tvLeft = view.findViewById(R.id.tv_left);
        if (model == null) {
            ivRgb.setImageResource(R.drawable.unselect_rb_icon);
            ivSlam.setImageResource(R.drawable.select_rb_icon);
            tvRound.setText(mContext.getText(R.string.model_material_text));
            tvCommon.setText(mContext.getText(R.string.model_material_material_text));
            tvTitle.setText(mContext.getText(R.string.upload_material_type_text));
            tvCancel.setText(mContext.getText(R.string.next_step_text));
            tvLeft.setVisibility(View.VISIBLE);
            uploadType = ConstantBean.UPLOAD_TO_BUILD_MODELS;
        } else {
            tvLeft.setVisibility(View.GONE);
            if (model.equals(mContext.getResources().getString(R.string.slam))) {
                ivRgb.setImageResource(R.drawable.unselect_rb_icon);
                ivSlam.setImageResource(R.drawable.select_rb_icon);
            } else {
                ivRgb.setImageResource(R.drawable.select_rb_icon);
                ivSlam.setImageResource(R.drawable.unselect_rb_icon);
            }
        }
        rlRgb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ivRgb.setImageResource(R.drawable.select_rb_icon);
                ivSlam.setImageResource(R.drawable.unselect_rb_icon);
                if (model == null) {
                    uploadType = ConstantBean.UPLOAD_TO_BUILD_MATERIALS;
                } else {
                    clickListener.modelClick(mContext.getResources().getString(R.string.rgb));
                    dismiss();
                }
            }
        });
        rlSlam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ivRgb.setImageResource(R.drawable.unselect_rb_icon);
                ivSlam.setImageResource(R.drawable.select_rb_icon);
                if (model == null) {
                    uploadType = ConstantBean.UPLOAD_TO_BUILD_MODELS;
                } else {
                    clickListener.modelClick(mContext.getResources().getString(R.string.slam));
                    dismiss();
                }
            }
        });
        tvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (model == null) {
                    Intent intent = new Intent(getContext(), FilePickerActivity.class);
                    intent.putExtra("fileType",uploadType);
                    mContext.startActivity(intent);
                }
                dismiss();
            }
        });

        tvLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
    }
}
