package com.huawei.hms.modeling3d.ui.widget;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.huawei.hms.magicresource.util.Utils;
import com.huawei.hms.modeling3d.R;

public class UploadSlamDialog extends Dialog {

    private Context mContext;
    TextView tvSure;
    TextView tvCancel;

    public UploadSlamDialog(@NonNull Context context) {
        super(context, R.style.BottomAnimDialogStyle);
        this.mContext = context;
    }

    public TextView getTvSure() {
        return tvSure;
    }

    public TextView getTvCancel() {
        return tvCancel;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
    }

    private void init() {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(mContext.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.upload_slam_data_layout, null);
        initView(view);
        Window window = this.getWindow();
        if (window != null) {
            window.setGravity(Gravity.BOTTOM);
            WindowManager.LayoutParams lp = window.getAttributes();
            lp.y = Utils.dip2px(mContext, 32);
            window.setAttributes(lp);
        }
        setContentView(view);
    }

    private void initView(View view) {
        tvSure = view.findViewById(R.id.tv_sure);
        tvCancel = view.findViewById(R.id.tv_cancel);
    }

}
