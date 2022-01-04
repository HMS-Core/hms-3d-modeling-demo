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
package com.huawei.hms.modeling3d.ui.widget;

import android.app.Dialog;
import android.content.Context;
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

import com.huawei.hms.magicresource.db.TaskInfoAppDb;
import com.huawei.hms.magicresource.util.Utils;
import com.huawei.hms.modeling3d.R;
import com.huawei.hms.modeling3d.ui.fragment.HistoryModelDataFragment;

public class PreviewConfigDialog extends Dialog {
    RelativeLayout rlLow;
    RelativeLayout rlHigh;
    ImageView ivLow;
    ImageView ivHigh;
    TextView tvCancel;
    Integer textureMode = 0;
    Context mContext;

    public TextView getTvCancel() {
        return tvCancel;
    }

    public void setTvCancel(TextView tvCancel) {
        this.tvCancel = tvCancel;
    }

    public Integer getTextureMode() {
        return textureMode;
    }

    public void setTextureMode(Integer textureMode) {
        this.textureMode = textureMode;
    }

    public PreviewConfigDialog(@NonNull Context context) {
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
        View view = inflater.inflate(R.layout.select_model_dialog_layout, null);
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
        rlLow = view.findViewById(R.id.rl_low);
        rlHigh = view.findViewById(R.id.rl_high);
        ivLow = view.findViewById(R.id.iv_low);
        ivHigh = view.findViewById(R.id.iv_high);
        tvCancel = view.findViewById(R.id.tv_cancel);
        view.findViewById(R.id.ll_model).setVisibility(View.GONE);


        rlLow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ivLow.setImageResource(R.drawable.select_rb_icon);
                ivHigh.setImageResource(R.drawable.unselect_rb_icon);
                textureMode = 0;
            }
        });
        rlHigh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ivLow.setImageResource(R.drawable.unselect_rb_icon);
                ivHigh.setImageResource(R.drawable.select_rb_icon);
                textureMode = 1;
            }
        });

    }

}
