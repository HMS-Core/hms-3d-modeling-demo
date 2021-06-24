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
import android.text.TextUtils;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;

import com.huawei.hms.modelingresource.R;

import static android.view.View.GONE;
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;


public class ProgressCustomDialog extends Dialog {

    int type;
    Context mContext;
    TextView tvTitle;
    ImageView ivCancel;
    ImageView ivCancelTwo;
    RelativeLayout rlNoProgress;
    RelativeLayout rlHasProgress;
    String title;
    private boolean showCancel;
    public static final int PROGRESS_WITH_CIRCLE = 2;

    // type 1 Progress bar with progress 2 Progress bar with circle
    public ProgressCustomDialog(@NonNull Context context, int type, String title) {
        super(context, R.style.BottomAnimDialogStyle);
        this.mContext = context;
        this.type = type;
        this.title = title;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
    }

    private void init() {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(mContext.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.progress_custom_layout, null);
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
        rlNoProgress = view.findViewById(R.id.rl_no_progress);
        rlHasProgress = view.findViewById(R.id.rl_has_progress);
        tvTitle = view.findViewById(R.id.tv_title);
        ivCancel = view.findViewById(R.id.iv_cancel);
        ivCancelTwo = view.findViewById(R.id.iv_cancel_two);
        if (!TextUtils.isEmpty(title)) {
            tvTitle.setText(title);
        }
        if (type == 1) {
            rlHasProgress.setVisibility(VISIBLE);
            rlNoProgress.setVisibility(GONE);
        } else {
            rlHasProgress.setVisibility(GONE);
            rlNoProgress.setVisibility(VISIBLE);
        }
        if (!showCancel) {
            ivCancel.setVisibility(INVISIBLE);
        }

        ivCancelTwo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
    }

    public void setShowCancer(boolean show) {
        showCancel = show;
    }


}
