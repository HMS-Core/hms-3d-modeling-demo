package com.huawei.hms.modeling3d.ui.widget;

import android.app.Dialog;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.huawei.hms.magicresource.util.Utils;
import com.huawei.hms.modeling3d.R;
import com.huawei.hms.modeling3d.ui.activity.NewScanActivity;
import com.huawei.hms.modeling3d.utils.ToastUtil;

public class TurntableSpeedDialog extends Dialog {
    NewScanActivity mContext;
    TextView tvConfirm;
    EditText editText;

    public TurntableSpeedDialog(@NonNull NewScanActivity context) {
        super(context, R.style.BottomAnimDialogStyle);
        mContext = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
    }

    private void init() {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(mContext.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.turntable_speed_layout, null);
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
        tvConfirm = view.findViewById(R.id.tv_confirm);
        editText = view.findViewById(R.id.edt_num);
        tvConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!editText.getText().toString().equals("") && Integer.parseInt(editText.getText().toString()) >= 20 && Integer.parseInt(editText.getText().toString()) <= 200) {
                    mContext.getTurnTableTime(Integer.parseInt(editText.getText().toString()));
                } else {
                    ToastUtil.showToast(mContext, "Input is illegal");
                }
            }
        });
    }
}
