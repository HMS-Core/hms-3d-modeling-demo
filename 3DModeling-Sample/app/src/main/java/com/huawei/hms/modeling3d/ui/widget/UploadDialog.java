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
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.huawei.hms.magicresource.util.Utils;
import com.huawei.hms.modeling3d.R;


public class UploadDialog extends Dialog {

    private Context mContext;
    TextView tvSure;
    TextView tvCancel;
    TextView tvUploadTips;
    OnItemSureClickListener listener;
    OnItemCancelClickListener clickListener;

    Integer mCurrentNum;

    public interface OnItemSureClickListener {
        void onClick();
    }

    public interface OnItemCancelClickListener {
        void onCancelClick(boolean isAgainCapture);
    }

    public UploadDialog(@NonNull Context context) {
        super(context, R.style.BottomAnimDialogStyle);
        this.mContext = context;
    }

    public UploadDialog(@NonNull Context context, Integer currentNum) {
        super(context, R.style.BottomAnimDialogStyle);
        this.mContext = context;
        this.mCurrentNum = currentNum;
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
        tvUploadTips = view.findViewById(R.id.tv_upload_tips);
        if (mCurrentNum != null&&mCurrentNum<50) {
            tvUploadTips.setVisibility(View.VISIBLE);
        }else {
            tvUploadTips.setVisibility(View.GONE);
        }
        tvSure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
                listener.onClick();
            }
        });

        tvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (clickListener != null) {
                    if (mCurrentNum >= 50) {
                        clickListener.onCancelClick(true);
                    } else {
                        clickListener.onCancelClick(false);
                    }
                }
                dismiss();
            }
        });
    }

    public void setListener(OnItemSureClickListener listener) {
        this.listener = listener;
    }

    public void setCancelListener(OnItemCancelClickListener listener) {
        this.clickListener = listener;
    }
}
