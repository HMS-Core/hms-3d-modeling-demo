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


import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.media.Image;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.huawei.hms.modeling3d.R;
import com.huawei.hms.modeling3d.model.SkeletonSaveBean;
import com.huawei.hms.modeling3d.ui.widget.BoneGLSurfaceView;
import com.huawei.hms.modeling3d.ui.widget.MyConfigChooser;
import com.huawei.hms.modeling3d.utils.BitmapUtils;
import com.huawei.hms.modeling3d.utils.FilterUtils;
import com.huawei.hms.modeling3d.utils.VideoOutputImageFormat;
import com.huawei.hms.modeling3d.utils.VideoToFrames;
import com.huawei.hms.modeling3d.utils.skeleton.LocalSkeletonProcessor;
import com.huawei.hms.motioncapturesdk.Modeling3dFrame;
import com.huawei.hms.motioncapturesdk.Modeling3dMotionCaptureSkeleton;

import java.util.ArrayList;
import java.util.List;


public class SelectSourceVideoActivity extends Activity implements VideoToFrames.Callback {
    private VideoOutputImageFormat videoOutputImageFormat;
    private SelectSourceVideoActivity self = this;
    private String outputDir;

    public static long costTime = 0;
    public static int frameIndex = 0;

    private static final int REQUEST_CHOOSE_IMAGE = 1002;

    ImageView imageView;
    Button buttonFilePathInput;
    EditText editTextInputFilePath;
    Button buttonStart;
    SkeletonSaveBean saveBean = new SkeletonSaveBean();
    List<List<List<Float>>> joints3d = new ArrayList<>();
    List<List<List<Float>>> quaternion = new ArrayList<>();
    List<List<Float>> shift = new ArrayList<>();

    LocalSkeletonProcessor localSkeletonProcessor;
    private int rotation;
    String path;
    private RelativeLayout glLayout;
    private BoneGLSurfaceView boneRenderManager;
    GLSurfaceView glSurfaceView;
    VideoToFrames videoToFrames ;

    int index ;

    final Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            String str = (String) msg.obj;
            updateInfo(str);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.select_source_video_layout);

        initImageFormatSpinner();
        localSkeletonProcessor = new LocalSkeletonProcessor();
        buttonFilePathInput = (Button) findViewById(R.id.button_file_path_input);
        buttonFilePathInput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startChooseImageIntentForResult();
            }
        });

        buttonStart = (Button) findViewById(R.id.button_start);
        editTextInputFilePath = (EditText) findViewById(R.id.file_path_input);
        buttonStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (TextUtils.isEmpty(editTextInputFilePath.getText().toString())) {
                    return;
                }

                EditText editTextOutputFolder = (EditText) findViewById(R.id.folder_created);
                outputDir = Environment.getExternalStorageDirectory() + "/" + "motionCapture/" + editTextOutputFolder.getText().toString();
                editTextInputFilePath = (EditText) findViewById(R.id.file_path_input);
                String inputFilePath = editTextInputFilePath.getText().toString();
                videoToFrames = new VideoToFrames();
                videoToFrames.setCallback(self);
                buttonFilePathInput.setEnabled(false);
                try {
                    videoToFrames.setSaveFrames(outputDir, videoOutputImageFormat);
                    updateInfo("Running...");
                    videoToFrames.decode(inputFilePath);
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
        });


        imageView = findViewById(R.id.iv_show);

        findViewById(R.id.back_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        glLayout = findViewById(R.id.rl_add_surface);
        glSurfaceView = new GLSurfaceView(this);
        boneRenderManager = new BoneGLSurfaceView();
        glSurfaceView.setPreserveEGLContextOnPause(true);
        glSurfaceView.setEGLConfigChooser(new MyConfigChooser());
        glSurfaceView.setEGLContextClientVersion(3);
        glSurfaceView.setRenderer(boneRenderManager);
        glLayout.addView(glSurfaceView);
    }

    private void startChooseImageIntentForResult() {
        Intent intent = new Intent(Intent.ACTION_PICK, null);
        intent.setType("vnd.android.cursor.dir/video");
        startActivityForResult(intent, REQUEST_CHOOSE_IMAGE);

    }

    private void initImageFormatSpinner() {
        videoOutputImageFormat = VideoOutputImageFormat.NV21;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == REQUEST_CHOOSE_IMAGE && resultCode == RESULT_OK) {
            Uri imageUri = data.getData();
            if (imageUri == null) {
                return;
            }
            path = BitmapUtils.getImagePath(SelectSourceVideoActivity.this, imageUri);
            editTextInputFilePath.setText(path);
            rotation = getVideoRotation(path);
        }
    }

    private void updateInfo(String info) {
        TextView textView = (TextView) findViewById(R.id.info);
        textView.setText(info);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (localSkeletonProcessor != null) {
            localSkeletonProcessor.stop();
        }
        if (videoToFrames!=null){
            videoToFrames.stopDecode();
        }
    }

    public void onDecodeFrame(int index) {
        Message msg = handler.obtainMessage();
        msg.obj = "Running..." + index + "frame";
        handler.sendMessage(msg);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                buttonStart.setEnabled(false);
            }
        });
    }

    @Override
    public void getIndexBitmap(final Image image, final byte[] imagedata) {
        Rect crop = image.getCropRect();
        int width = crop.width();
        int height = crop.height();

        Modeling3dFrame.Property property = new Modeling3dFrame.Property.Creator().setFormatType(ImageFormat.NV21)
                .setWidth(width)
                .setHeight(height)
                .setQuadrant(rotation)
                .setItemIdentity(index)
                .create();

        index++;

        SparseArray<Modeling3dMotionCaptureSkeleton> data = localSkeletonProcessor.detectInImageSynchronize(Modeling3dFrame.fromByteArray(imagedata, property));
        if (data.size() != 0) {
            frameIndex++;
            int key = 0;
            for (int i = 0; i < data.size(); i++) {
                key = data.keyAt(i);
                Modeling3dMotionCaptureSkeleton obj = data.get(key);
                filterData(obj);
            }
        } else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(SelectSourceVideoActivity.this, "data array is empty", Toast.LENGTH_SHORT).show();
                }
            });
        }
        image.close();
    }


    public void onFinishDecode() {
        Message msg = handler.obtainMessage();
        msg.obj = "finish！";
        handler.sendMessage(msg);
        saveBean.setJoints3d(joints3d);
        saveBean.setQuaternion(quaternion);
        saveBean.setTrans(shift);
        saveBean = null;
        joints3d = null;
        quaternion = null;
        shift = null;
        saveBean = new SkeletonSaveBean();
        joints3d = new ArrayList<>();
        quaternion = new ArrayList<>();
        shift = new ArrayList<>();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                buttonStart.setEnabled(true);
                buttonFilePathInput.setEnabled(true);
            }
        });
    }

    public void filterData(Modeling3dMotionCaptureSkeleton fromData) {

        joints3d.add(FilterUtils.filterDataJoints3ds(fromData));
        quaternion.add(FilterUtils.filterDataQuaternions(fromData));
        shift.add(FilterUtils.filterDataJointShift(fromData));

        boneRenderManager.setData(joints3d.get(joints3d.size() - 1), shift.get(shift.size() - 1));
    }


    public int getVideoRotation(String mUri) {
        int rotation = 0;
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        try {
            mmr.setDataSource(mUri);
            String rotationStr = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION); // Video rotation direction
            return Integer.parseInt(rotationStr) / 90;
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            mmr.release();
        }
        return rotation;
    }

}