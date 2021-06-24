/**
 * Copyright 2021. Huawei Technologies Co., Ltd. All rights reserved.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.huawei.hms.modelingresource.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.huawei.hms.modelingresource.R;


public class UploadDialog extends Dialog {

    private Context mContext;
    TextView tvSure;
    TextView tvCancel;
    OnItemSureClickListener listener;

    // Click OK.
    public interface OnItemSureClickListener {
        void onClick();
    }

    public UploadDialog(@NonNull Context context) {
        super(context, R.style.BottomAnimDialogStyle);
        this.mContext = context;
        this.listener = null;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
    }

    private void init() {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(mContext.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.upload_data_layout, null);
        initView(view);
        Window window = this.getWindow();
        if (window != null) {
            WindowManager.LayoutParams lp = window.getAttributes();
            WindowManager m = window.getWindowManager();
            Display d = m.getDefaultDisplay();
            lp.width = (int) (d.getWidth() * 0.7);
            window.setAttributes(lp);
        }
        setContentView(view);
    }

    private void initView(View view) {
        tvSure = view.findViewById(R.id.tv_sure);
        tvCancel = view.findViewById(R.id.tv_cancel);

        tvSure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
                if (listener != null) {
                    listener.onClick();
                }
            }
        });

        tvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
    }

    public void setListener(OnItemSureClickListener listener) {
        this.listener = listener;
    }
}
