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
package com.huawei.hms.modeling3d.ui.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;
import com.google.android.material.tabs.TabLayout;
import com.huawei.hms.modeling3d.R;
import com.huawei.hms.modeling3d.ui.adapter.ContentPagerAdapter;
import com.huawei.hms.modeling3d.ui.fragment.HistoryMaterialDataFragment;
import com.huawei.hms.modeling3d.ui.fragment.HistoryModelDataFragment;
import java.util.ArrayList;
import java.util.List;

/**
 * HistoryActivity
 *
 * @since 2020-09-18
 */
public class HistoryActivity extends AppCompatActivity {

    private TabLayout mTabTl;
    private ViewPager mContentVp;
    private final static String TAG = HistoryActivity.class.getSimpleName();
    private List<String> tabIndicators;
    private List<Fragment> tabFragments;
    private ContentPagerAdapter contentAdapter;
    private int index = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.history_activity_layout);
        index = getIntent().getIntExtra("index", 0);
        init();
        initContent();
    }

    private void init() {
        findViewById(R.id.iv_back).setOnClickListener(v -> finish());
        ((TextView) findViewById(R.id.tv_title)).setText(R.string.history_data_text);
        mTabTl = findViewById(R.id.tl_tab);
        mContentVp = findViewById(R.id.vp_content);
    }

    private void initContent() {
        tabIndicators = new ArrayList<>();
        tabIndicators.add(getString(R.string.model_history_text));
        tabIndicators.add(getString(R.string.material_history_text));
        tabFragments = new ArrayList<>();
        for (int i = 0; i < tabIndicators.size(); i++) {
            tabFragments.add(new HistoryModelDataFragment());
            tabFragments.add(new HistoryMaterialDataFragment());
        }
        contentAdapter = new ContentPagerAdapter(getSupportFragmentManager(), tabIndicators, tabFragments);
        mContentVp.setAdapter(contentAdapter);
        mTabTl.setupWithViewPager(mContentVp);
        mContentVp.setCurrentItem(index);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        tabIndicators.clear();
        tabFragments.clear();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }

}
