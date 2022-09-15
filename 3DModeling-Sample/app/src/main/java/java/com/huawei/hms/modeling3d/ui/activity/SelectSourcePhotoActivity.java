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

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.util.SparseArray;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.MenuPopupHelper;
import androidx.appcompat.widget.PopupMenu;

import com.huawei.hms.modeling3d.R;
import com.huawei.hms.modeling3d.model.GridItem;
import com.huawei.hms.modeling3d.model.SkeletonSaveBean;
import com.huawei.hms.modeling3d.ui.adapter.MyGridViewAdapter;
import com.huawei.hms.modeling3d.ui.widget.BoneGLSurfaceView;
import com.huawei.hms.modeling3d.ui.widget.CameraImageGraphic;
import com.huawei.hms.modeling3d.ui.widget.GraphicOverlay;
import com.huawei.hms.modeling3d.ui.widget.MyConfigChooser;
import com.huawei.hms.modeling3d.ui.widget.SkeletonGraphic;
import com.huawei.hms.modeling3d.utils.BitmapUtils;
import com.huawei.hms.modeling3d.utils.FilterUtils;
import com.huawei.hms.modeling3d.utils.ToastUtil;
import com.huawei.hms.modeling3d.utils.skeleton.LocalSkeletonProcessor;
import com.huawei.hms.motioncapturesdk.Modeling3dFrame;
import com.huawei.hms.motioncapturesdk.Modeling3dMotionCaptureSkeleton;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SelectSourcePhotoActivity extends AppCompatActivity {
    private static final String TAG = "TemplateActivity";

    private static final int REQUEST_IMAGE_CAPTURE = 1001;

    private static final int REQUEST_CHOOSE_IMAGE = 1002;

    private static final int UNKNOW = -1;

    private static Map<String, GridItem> templateDataMap = new HashMap<String, GridItem>();

    private static int sSelectedIndex = UNKNOW;

    private GridView mGridView;

    private MyGridViewAdapter mAdapter;

    private ImageView mLoadPhoto;

    private Uri imageUri;

    private Bitmap originBitmap;

    private List<GridItem> templateDatalist = new ArrayList<GridItem>();

    private GraphicOverlay graphicOverlay;

    LocalSkeletonProcessor localSkeletonProcessor;

    SkeletonSaveBean saveBean = new SkeletonSaveBean();
    List<List<List<Float>>> joints3d = new ArrayList<>();
    List<List<List<Float>>> quaternion = new ArrayList<>();
    List<List<Float>> shift = new ArrayList<>();
    private RelativeLayout glLayout;
    private BoneGLSurfaceView boneRenderManager;
    GLSurfaceView glSurfaceView ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_source_photo_layout);
        initData();
        findViewById(R.id.back_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        graphicOverlay = findViewById(R.id.previewOverlay);
        mGridView = findViewById(R.id.gridview);
        mAdapter = new MyGridViewAdapter(templateDatalist, this);
        mGridView.setAdapter(mAdapter);
        this.mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (templateDatalist.get(position).isSelected()) {
                    templateDatalist.get(position).setSelected(false);
                } else {
                    templateDatalist.get(position).setSelected(true);
                }
                setSelectedIndex(UNKNOW);
                for (int i = 0; i < templateDatalist.size(); i++) {
                    if (i != position) {
                        templateDatalist.get(i).setSelected(false);
                    }
                    if (templateDatalist.get(i).isSelected()) {
                        setSelectedIndex(i);
                    }
                }
                mAdapter.notifyDataSetChanged();
            }
        });
        mLoadPhoto = findViewById(R.id.load_photo);
        mLoadPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showChooseDialog(view);
            }
        });
        localSkeletonProcessor = new LocalSkeletonProcessor();

        glLayout = findViewById(R.id.rl_add_surface);
        glSurfaceView = new GLSurfaceView(this);
        boneRenderManager = new BoneGLSurfaceView();
        glSurfaceView.setPreserveEGLContextOnPause(true);
        glSurfaceView.setEGLConfigChooser(new MyConfigChooser());
        glSurfaceView.setEGLContextClientVersion(3);
        glSurfaceView.setRenderer(boneRenderManager);
        glLayout.addView(glSurfaceView);
    }

    private void initData() {
        for (int i = 0; i < templateDataMap.size(); i++) {
            GridItem gridItem = templateDataMap.get("key" + i);
            if (getSelectedIndex() == UNKNOW) {
                if (i == 0) {
                    gridItem.setSelected(true);
                    setSelectedIndex(0);
                } else {
                    gridItem.setSelected(false);
                }
            } else {
                if (getSelectedIndex() != i) {
                    gridItem.setSelected(false);
                } else {
                    gridItem.setSelected(true);
                }
            }
            templateDatalist.add(gridItem);
        }
    }

    @SuppressLint("RestrictedApi")
    private void showChooseDialog(View view) {
        PopupMenu popup = new PopupMenu(SelectSourcePhotoActivity.this, view);
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                if (menuItem.getItemId() == R.id.select_images_from_local) {
                    startChooseImageIntentForResult();
                    return true;
                }
                return false;
            }
        });
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.camera_button_menu, popup.getMenu());
        try {
            Field field = popup.getClass().getDeclaredField("mPopup");
            field.setAccessible(true);
            MenuPopupHelper mHelper = (MenuPopupHelper) field.get(popup);
            mHelper.setForceShowIcon(true);
        } catch (IllegalAccessException e) {
            Log.w(TAG, "IllegalAccessException " + e.getMessage());
        } catch (NoSuchFieldException e) {
            Log.w(TAG, "NoSuchFieldException " + e.getMessage());
        }
        popup.show();
    }

    private void startChooseImageIntentForResult() {
        Intent intent = new Intent(Intent.ACTION_PICK, null);
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        startActivityForResult(intent, REQUEST_CHOOSE_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            loadImage();
        } else if (requestCode == REQUEST_CHOOSE_IMAGE && resultCode == RESULT_OK) {
            imageUri = data.getData();
            loadImage();
        }
    }

    private void loadImage() {
        String path = BitmapUtils.getImagePath(SelectSourcePhotoActivity.this, imageUri);
        originBitmap = BitmapFactory.decodeFile(path);
        if (originBitmap == null) {
            return;
        }

        Modeling3dFrame frame = Modeling3dFrame.fromBitmap(originBitmap);

        SparseArray<Modeling3dMotionCaptureSkeleton> data = localSkeletonProcessor.detectInImageSynchronize(frame);
        if (data.size() != 0) {
            int key = 0;
            for (int i = 0; i < data.size(); i++) {
                key = data.keyAt(i);
                Modeling3dMotionCaptureSkeleton obj = data.get(key);
                filterData(obj);
                List<Modeling3dMotionCaptureSkeleton> result = new ArrayList<>();
                result.add(obj);
                showPicture(result);
            }
            saveBean = new SkeletonSaveBean();
            joints3d = new ArrayList<>();
            quaternion = new ArrayList<>();
            shift = new ArrayList<>();
        } else {
            ToastUtil.showToast(SelectSourcePhotoActivity.this, "data array is empty");
        }

    }

    public void showPicture(List<Modeling3dMotionCaptureSkeleton> results) {
        graphicOverlay.clear();
        CameraImageGraphic imageGraphic = new CameraImageGraphic(graphicOverlay, originBitmap);
        graphicOverlay.add(imageGraphic);

        SkeletonGraphic skeletonGraphic = new SkeletonGraphic(graphicOverlay, results);
        graphicOverlay.add(skeletonGraphic);

        graphicOverlay.postInvalidate();
        GridItem gridItem = new GridItem();
        gridItem.setBitmap(BitmapUtils.loadBitmapFromView(graphicOverlay, originBitmap.getWidth(),
                originBitmap.getHeight()));
        gridItem.setSkeletonList(results);
        templateDatalist.add(gridItem);
        mAdapter.notifyDataSetChanged();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (localSkeletonProcessor != null) {
            localSkeletonProcessor.stop();
        }
    }

    public static void clearData() {
        templateDataMap.clear();
    }

    public static Map<String, GridItem> getTemplateDataMap() {
        return templateDataMap;
    }

    public static int getSelectedIndex() {
        return sSelectedIndex;
    }

    public static void setSelectedIndex(int index) {
        SelectSourcePhotoActivity.sSelectedIndex = index;
    }

    public void filterData(Modeling3dMotionCaptureSkeleton fromData) {

        quaternion.add(FilterUtils.filterDataQuaternions(fromData));
        shift.add(FilterUtils.filterDataJointShift(fromData));
        joints3d.add(FilterUtils.filterDataJoints3ds(fromData));
        saveBean.setJoints3d(joints3d);
        saveBean.setQuaternion(quaternion);
        saveBean.setTrans(shift);
        boneRenderManager.setData(joints3d.get(joints3d.size() - 1), shift.get(shift.size() - 1));
    }

}
