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
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.huawei.hms.magicresource.db.TaskInfoAppDb;
import com.huawei.hms.magicresource.db.TaskInfoAppDbUtils;
import com.huawei.hms.magicresource.util.Utils;
import com.huawei.hms.modeling3d.R;

public class ProgressCustomDialog extends Dialog {

    int type ;
    Context mContext ;
    TextView tvTitle ;
    TextView tvTitleTwo ;
    TextView tvProgress ;
    ProgressBar progressBar ;
    ImageView ivCancel ;
    ImageView ivCancelTwo ;
    RelativeLayout rlNoProgress ;
    RelativeLayout rlHasProgress ;
    String title ;
    OnItemCancelClickListener listener ;
    TaskInfoAppDb appDb ;
    public interface OnItemCancelClickListener {
        void onCancel(TaskInfoAppDb appDb);
    }

    public ProgressCustomDialog(@NonNull Context context,int type,String title) {
        super(context , R.style.BottomAnimDialogStyle);
        this.mContext = context ;
        this.type = type ;
        this.title = title ;
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
            window.setGravity(Gravity.BOTTOM);
            WindowManager.LayoutParams lp = window.getAttributes();
            lp.y = Utils.dip2px(mContext, 32);
            window.setAttributes(lp);
        }

        setContentView(view);
    }

    public ImageView getIvCancel() {
        return ivCancel;
    }

    private void initView(View view) {
        rlNoProgress = view.findViewById(R.id.rl_no_progress);
        rlHasProgress = view.findViewById(R.id.rl_has_progress);
        tvTitle = view.findViewById(R.id.tv_title);
        tvTitleTwo = view.findViewById(R.id.tv_title_two);
        progressBar = view.findViewById(R.id.pb_progress);
        ivCancel = view.findViewById(R.id.iv_cancel);
        ivCancelTwo = view.findViewById(R.id.iv_cancel_two);
        tvProgress = view.findViewById(R.id.tv_progress);

        if (type==2){
            rlHasProgress.setVisibility(View.GONE);
            rlNoProgress.setVisibility(View.VISIBLE);
            tvTitle.setVisibility(View.VISIBLE);
            tvTitle.setText(title);
        }else {
            rlHasProgress.setVisibility(View.VISIBLE);
            rlNoProgress.setVisibility(View.GONE);
            tvTitleTwo.setVisibility(View.VISIBLE);
            tvTitleTwo.setText(title);
        }

        ivCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onCancel(appDb);
                dismiss();
            }
        });

        ivCancelTwo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onCancel(appDb);
                dismiss();
            }
        });
    }

    public void setCurrentProgress(double progress){
        new Handler(mContext.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                progressBar.setProgress((int) progress);
                int current = (int) progress;
                tvProgress.setText(current+"%");
            }
        });
    }
    public void setListener(OnItemCancelClickListener listener) {
        this.listener = listener;
    }

    public void setListener(OnItemCancelClickListener listener, TaskInfoAppDb appDb) {
        this.listener = listener;
        this.appDb = appDb ;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (appDb!=null&&listener!=null) {
            listener.onCancel(appDb);
            TaskInfoAppDbUtils.updateTaskIdAndStatusByPath(appDb.getFileUploadPath(), null, 0);
        }
        dismiss();
    }
}
