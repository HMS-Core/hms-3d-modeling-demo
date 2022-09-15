package com.huawei.hms.modeling3d.ui.widget;

import android.app.Dialog;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.huawei.hms.magicresource.util.Utils;
import com.huawei.hms.modeling3d.R;
import com.huawei.hms.modeling3d.ui.activity.NewScanActivity;

public class ShootingStepsDialog extends Dialog {
    NewScanActivity mContext;
    ImageView ivStep ;
    TextView tvStep ;
    TextView tvTips ;
    TextView tvConfirm ;

    int currentStep ;

    public ShootingStepsDialog(@NonNull NewScanActivity context , int step) {
        super(context, R.style.BottomAnimDialogStyle);
        mContext = context;
        this.currentStep = step ;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
    }

    private void init() {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(mContext.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.shooting_steps_dialog_layout, null);
        Window window = this.getWindow();
        if (window != null) {
            window.setGravity(Gravity.BOTTOM);
            WindowManager.LayoutParams lp = window.getAttributes();
            lp.y = Utils.dip2px(mContext, 32);
            window.setAttributes(lp);
        }
        initView(view);
        setContentView(view);
    }

    private void initView(View view) {
        tvStep = view.findViewById(R.id.tv_step);
        ivStep = view.findViewById(R.id.iv_step);
        tvTips = view.findViewById(R.id.tv_tip);
        tvConfirm = view.findViewById(R.id.tv_confirm);
        if (currentStep==1){
            ivStep.setImageResource(R.drawable.shoot_step_one_icon);
            tvStep.setText(mContext.getText(R.string.first_step_text));
            tvTips.setText(mContext.getText(R.string.rotates_once_text));
        }else if (currentStep==2){
            ivStep.setImageResource(R.drawable.shoot_step_two_icon);
            tvStep.setText(mContext.getText(R.string.second_step_text));
            tvTips.setText(mContext.getText(R.string.rotates_two_text));
        }else if (currentStep==3){
            ivStep.setImageResource(R.drawable.shoot_step_three_icon);
            tvStep.setText(mContext.getText(R.string.third_step_text));
            tvTips.setText(mContext.getText(R.string.half_a_circle_text));
        }else if (currentStep==4){
            ivStep.setImageResource(R.drawable.shoot_step_four_icon);
            tvStep.setText(mContext.getText(R.string.the_fourth_step_text));
            tvTips.setText(mContext.getText(R.string.turntable_in_a_circle_text));
        }else if (currentStep==5){
            ivStep.setImageResource(R.drawable.shoot_step_five_icon);
            tvStep.setText(mContext.getText(R.string.the_fifth_step_text));
            tvTips.setText(mContext.getText(R.string.in_a_circle_text));
        }

        tvConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mContext.doNextStep();
                dismiss();
            }
        });
    }
}
