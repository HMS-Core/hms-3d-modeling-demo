/**
 * Copyright 2020. Huawei Technologies Co., Ltd. All rights reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.huawei.hms.modeling3d.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.huawei.featurelayer.sharedfeature.xrkit.sdk.IArSceneView;
import com.huawei.featurelayer.sharedfeature.xrkit.sdk.IModel;
import com.huawei.featurelayer.sharedfeature.xrkit.sdk.IXrKitFeature;
import com.huawei.featurelayer.sharedfeature.xrkit.sdk.exceptions.XrKitUnavailableServiceApkTooOldException;
import com.huawei.featurelayer.sharedfeature.xrkit.sdk.exceptions.XrkitUnavailableArEngineNotAvailableException;
import com.huawei.featurelayer.sharedfeature.xrkit.sdk.listener.IFeatureEventListener;
import com.huawei.featurelayer.sharedfeature.xrkit.sdk.remoteloader.XrKitFeatureFactory;
import com.huawei.hms.modeling3d.R;
import com.huawei.hms.modeling3d.utils.BaseUtils;

/**
 * XRKit ArSceneView example. This sample code and simple way show developers how to use XRKit
 * ArSceneView scene. It can be divided into the following steps:
 * 1、Determine whether XRKit is installed on the current device.
 * 2、Create xrKitFeature through XrKitFeatureFactory, and create SceneView through xrKitFeature.
 * 3、Get the view of ArSceneView and add it to the constraint layout of current application.
 * 4、Scene settings, according to their own needs. Load the virtual model, set the background of 3D scene,
 * set model monitoring, set the visibility of AR scene plane and initialize to 3D state.
 */
public class ArSceneViewActivity extends AppCompatActivity implements IFeatureEventListener {
    private static final String TAG = "ArSceneViewActivity";

    // 3D scene background file, if the file is stored in the device, the URL is URL_SCHEMA_STORAGE + "file path"
    private static final String IMG_BLACK = IArSceneView.URL_SCHEMA_ASSET + "background/background.png";

    // Virtual model file, if the file is stored in the device, the URL is URL_SCHEMA_STORAGE + "file path"
    private  String GONGCHENGSHI = IArSceneView.URL_SCHEMA_ASSET + "glb/out.gltf";

    private RelativeLayout layout;

    private IArSceneView sceneView;

    private IXrKitFeature xrKitFeature;

    String path ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "activity create.");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_arsceneview);
        layout = findViewById(R.id.xrkitLayout);
        path = getIntent().getStringExtra("path");
        GONGCHENGSHI = IArSceneView.URL_SCHEMA_STORAGE+path+"out.gltf";
        if (layout == null) {
            return;
        }
        if (!createXrKit()) {
            finish();
            Log.d(TAG, "activity create.");
        }
        findViewById(R.id.iv_back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        findViewById(R.id.tv_title).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bitmap bitmap = BaseUtils.takeScreenShot(ArSceneViewActivity.this);
                BaseUtils.saveBitmap(bitmap,ArSceneViewActivity.this);
            }
        });
    }

    @Override
    public void onFeatureEventCalled(FeatureEventType featureEventType, Object object, String string) {
        Log.d(TAG, "onFeatureEventCalled ++");
        switch (featureEventType) {
            case MODEL_LOADED: // When the model is loaded successfully, the XRKit server will call back this enum.
                runOnUiThread(() -> {
                    if (object instanceof IModel) {
                        Toast.makeText(this, "successfully load mode." + ((IModel) object).getTag(), Toast.LENGTH_SHORT)
                                .show();
                    }
                });
                break;
            case MODEL_SELECTED: // In ArSceneView, when the model is selected, the XRKit server makes this callback.
                runOnUiThread(() -> {
                    Toast.makeText(this, "model is selected.", Toast.LENGTH_SHORT).show();
                });
                break;
            case MODEL_ERROR:
                runOnUiThread(() -> { // When a model loading error occurs, the XRKit server will call back this enum.
                    Toast.makeText(this, "load model error." + string, Toast.LENGTH_SHORT).show();
                });
                break;
            case MODEL_PLACED:
                // In ArSceneView, when the plane is recognized, the model falls into the plane,
                // and the server will call back this enum.
                Log.d(TAG, "onFeatureEventCalled MODEL_PLACED ++");
                runOnUiThread(() -> {
                    Toast.makeText(this, "model is planed on plane." + object + " " + string, Toast.LENGTH_SHORT)
                            .show();
                });
                break;
            default:
                Log.e(TAG, "Unknown featureEventType.");
        }
    }

    private boolean createXrKit() {
        // 1.Availability check to determine whether the current machine is installed XRKit.apk.
        if (!XrKitFeatureFactory.isXrKitExist(getApplicationContext())) {
            Toast.makeText(this, "XRKit is not available ", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (sceneView != null) {
            return true;
        }

        // 2.Create ArFaceView and set XRKit feature listener.
        try {
            if (xrKitFeature == null) {
                xrKitFeature = XrKitFeatureFactory.createXrKitFeature(getApplicationContext());
            }
            xrKitFeature.setFeatureEventListener(this);
            sceneView = xrKitFeature.createArSceneView(getApplicationContext());
        } catch (XrKitUnavailableServiceApkTooOldException e) {
            Toast.makeText(this, "XRKit Service is Too Old, Please upgrade!", Toast.LENGTH_SHORT).show();
            return false;
        }

        // 3.Get the view of the XRKit server in ArSceneView and add it to the
        // ConstraintLayout of the current application.
        layout.addView(sceneView.getView(), 0, new ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        // Set 3D mode or AR mode according to the intent value.
        int mode = getIntent().getIntExtra("mode", 1);
        if (mode == 0) {
            sceneView.setArMode(false);
        } else if (mode == 1) {
            try {
                sceneView.setArMode(true);
            } catch (XrkitUnavailableArEngineNotAvailableException e) {
                Toast.makeText(this, "AREngine server is not available", Toast.LENGTH_SHORT).show();
                finish();
            }
        } else {
            Log.d(TAG, "unknown mode");
        }

        // 4. Set the scene according to your own needs. In this configuration, load the
        // virtual model, set the background of 3D scene, set the visibility of AR scene
        // plane, and initialize to 3D state.
        sceneView.loadModel(GONGCHENGSHI, "out.gltf");
        sceneView.setBackground(IMG_BLACK);
        sceneView.setPlaneVisible(true);
        return true;
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "activity resume.");
        super.onResume();
        if (sceneView != null) {
            sceneView.resume();
        }
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "activity onPause.");
        super.onPause();
        if (sceneView != null) {
            sceneView.pause();
        }
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "activity onDestroy.");
        super.onDestroy();
        if (sceneView != null) {
            layout.removeView(sceneView.getView());
            sceneView.destroy();
            sceneView = null;
        }
        try {
            if (xrKitFeature != null) {
                xrKitFeature.setFeatureEventListener(null);
                XrKitFeatureFactory.releaseFeature(xrKitFeature);
                xrKitFeature = null;
            }
        } catch (XrKitUnavailableServiceApkTooOldException e) {
            Log.w(TAG, "XRKit Service is Too Old.");
        }
    }
}