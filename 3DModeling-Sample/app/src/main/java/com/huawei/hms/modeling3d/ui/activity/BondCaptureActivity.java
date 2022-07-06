package com.huawei.hms.modeling3d.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.view.Surface;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.view.PreviewView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.huawei.hms.magicresource.util.Constants;
import com.huawei.hms.magicresource.view.CustomRoundAngleImageView;
import com.huawei.hms.modeling3d.R;
import com.huawei.hms.modeling3d.model.ConstantBean;
import com.huawei.hms.modeling3d.model.UserBean;
import com.huawei.hms.modeling3d.ui.adapter.RecycleBondImageAdapter;
import com.huawei.hms.modeling3d.utils.BaseUtils;
import com.huawei.hms.modeling3d.utils.CameraXManager;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class BondCaptureActivity extends AppCompatActivity implements RecycleBondImageAdapter.OnItemClickListener {

    @BindView(R.id.tv_photo_num)
    TextView tvPhotoNum;

    @BindView(R.id.rl_show_num)
    RelativeLayout rlShowNum;

    @BindView(R.id.iv_back)
    ImageView ivBack;

    @BindView(R.id.capture_button)
    ImageView ivCaptureButton;

    @BindView(R.id.view_finder)
    PreviewView previewView;

    @BindView(R.id.img_pic)
    CustomRoundAngleImageView imgPic;

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

    @BindView(R.id.tv_title)
    TextView tv_title;

    private RecycleBondImageAdapter adapter;
    private ArrayList<String> imagePaths = new ArrayList<>();


    int screenType = ConstantBean.SCREEN_MODEL_TYPE_ZERO;
    int currentPhotoNum = 0;
    CameraXManager xManager;
    String createTime;
    String saveInnerPath;
    Unbinder unbinder;
    UserBean userBean ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bond_capture_layout);
        unbinder = ButterKnife.bind(this);
        userBean = BaseUtils.getUser(BondCaptureActivity.this);
        screenType = userBean.getSelectResolutionModel();
        initView();
        initPhotoSize();
    }

    private void initPhotoSize() {
        xManager = new CameraXManager(this, previewView, screenType);
        xManager.startCamera();
    }

    private void initView() {
        createTime = String.valueOf(System.currentTimeMillis());
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(RecyclerView.HORIZONTAL);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new RecycleBondImageAdapter(imagePaths, this);
        adapter.setOnItemClickListener(this);
        recyclerView.setAdapter(adapter);
    }

    @OnClick({R.id.iv_back, R.id.capture_button, R.id.tv_title})
    public void onClick(View view) {
        switch (view.getId()) {

            case R.id.iv_back:
                finish();
                break;

            case R.id.capture_button:
                saveInnerPath = new Constants(BondCaptureActivity.this).getCaptureImageFile() + "model" + createTime;
                File file = new File(saveInnerPath);
                if (!file.exists()) {
                    file.mkdir();
                }
                xManager.takePicture();

                xManager.setTakePicBack(new CameraXManager.TakePicBack() {
                    @Override
                    public void takePicBack(Bitmap bitmap) {
                        if (currentPhotoNum < 4) {
                            saveBitmap(bitmap);
                        }
                    }
                });
                break;

            case R.id.tv_title:
                if (currentPhotoNum<2){
                    Toast.makeText(BondCaptureActivity.this,getString(R.string.take_photo_bond_text),Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent intent = new Intent(BondCaptureActivity.this, NewScanActivity.class);
                intent.putExtra("number", currentPhotoNum);
                intent.putExtra("path", saveInnerPath);
                startActivity(intent);
                finish();
            default:
                break;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    public Bitmap adjustPhotoRotation(Bitmap bm, final int orientationDegree) {
        Matrix m = new Matrix();
        m.setRotate(orientationDegree, (float) bm.getWidth() / 2, (float) bm.getHeight() / 2);
        try {
            return Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), m, true);
        } catch (OutOfMemoryError ex) {
            ex.fillInStackTrace();
        }
        return bm;
    }

    public int getDisplayRotation(Activity activity) {
        if (activity == null) {
            return 0;
        }

        int rotation = activity.getWindowManager().getDefaultDisplay()
                .getRotation();
        switch (rotation) {
            case Surface.ROTATION_0:
                return 0;
            case Surface.ROTATION_90:
                return 270;
            case Surface.ROTATION_180:
                return 180;
            case Surface.ROTATION_270:
                return 90;
        }
        return 0;
    }


    private void saveBitmap(Bitmap bitmap) {
        Observable.create((Observable.OnSubscribe<File>) subscriber -> {
            File filePic;
            try {
                filePic = new File(saveInnerPath + "/" + String.format("%05d", currentPhotoNum + 1) + ".jpg");
                if (!filePic.exists()) {
                    filePic.getParentFile().mkdirs();
                    filePic.createNewFile();
                }
                FileOutputStream fos = new FileOutputStream(filePic);
                Bitmap newBitmap = adjustPhotoRotation(bitmap, getDisplayRotation(BondCaptureActivity.this));
                newBitmap.compress(Bitmap.CompressFormat.JPEG, 95, fos);
                fos.flush();
                fos.close();
                newBitmap.recycle();
                subscriber.onNext(filePic);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<File>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(File result) {
                showCompressionResult(result);
            }
        });

    }


    public void showCompressionResult(File mFile) {
        rlShowNum.setVisibility(View.GONE);
        currentPhotoNum += 1;

        imgPic.setImageBitmap(BitmapFactory.decodeFile(mFile.getAbsolutePath()));
        tvPhotoNum.setText(String.valueOf(currentPhotoNum));
        imagePaths.add(mFile.getAbsolutePath());
        adapter.notifyDataSetChanged();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        xManager.cameraDestroy();
    }

    @Override
    public void onClick(View parent, int position) {
        String path = imagePaths.get(position);
        File file = new File(path);
        boolean d = file.delete();
        if (d) {
            imagePaths.remove(path);
            adapter.notifyDataSetChanged();
            currentPhotoNum--;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
