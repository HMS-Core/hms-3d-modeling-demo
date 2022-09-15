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
import com.huawei.hms.modeling3d.ui.activity.BondCaptureActivity;
import com.huawei.hms.modeling3d.ui.activity.EmptySelectActivity;
import com.huawei.hms.modeling3d.ui.activity.NewScanActivity;

public class SelectCameraTypeDialog extends Dialog {
    Context mContext ;
    RelativeLayout rlRgb;
    RelativeLayout rlSlam;
    TextView tvCancel;
    ImageView ivRgb;
    ImageView ivSlam;

    int type = 1 ;

    public SelectCameraTypeDialog(@NonNull Context context) {
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
        View view = inflater.inflate(R.layout.select_camera_type_layout, null);
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

        rlSlam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ivRgb.setImageResource(R.drawable.unselect_rb_icon);
                ivSlam.setImageResource(R.drawable.select_rb_icon);
                type = 1 ;
            }
        });
        rlRgb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ivRgb.setImageResource(R.drawable.select_rb_icon);
                ivSlam.setImageResource(R.drawable.unselect_rb_icon);
                type = 2 ;
            }
        });

        tvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (type == 1){
                    Intent intent = new Intent(mContext, NewScanActivity.class) ;
                    mContext.startActivity(intent);
                }else {
                    Intent intent = new Intent(mContext, BondCaptureActivity.class) ;
                    mContext.startActivity(intent);
                }
                dismiss();
            }
        });

    }
}
