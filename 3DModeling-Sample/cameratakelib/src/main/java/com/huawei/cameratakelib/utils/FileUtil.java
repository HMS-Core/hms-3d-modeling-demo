package com.huawei.cameratakelib.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.media.Image;
import android.opengl.GLES20;
import android.os.Environment;
import android.os.StatFs;
import android.text.TextUtils;
import android.util.Log;

import com.huawei.hms.magicresource.util.Constants;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import top.zibin.luban.CompressionPredicate;
import top.zibin.luban.Luban;
import top.zibin.luban.OnCompressListener;

public class FileUtil {

    /**
     * 计算Sdcard的剩余大小
     *
     * @return MB
     */
    public static long getAvailableSize() {
        //sd卡大小相关变量
        StatFs statFs;
        File file = Environment.getExternalStorageDirectory();
        statFs = new StatFs(file.getPath());
        //获得Sdcard上每个block的size
        long blockSize = statFs.getBlockSize();
        //获取可供程序使用的Block数量
        long blockavailable = statFs.getAvailableBlocks();
        //计算标准大小使用：1024，当然使用1000也可以
        long blockavailableTotal = blockSize * blockavailable / 1024 / 1024;
        return blockavailableTotal;
    }

    /**
     * SDCard 总容量大小
     *
     * @return MB
     */
    public static long getTotalSize() {
        StatFs statFs;
        File file = Environment.getExternalStorageDirectory();
        statFs = new StatFs(file.getPath());
        //获得sdcard上 block的总数
        long blockCount = statFs.getBlockCount();
        //获得sdcard上每个block 的大小
        long blockSize = statFs.getBlockSize();
        //计算标准大小使用：1024，当然使用1000也可以
        long bookTotalSize = blockCount * blockSize / 1024 / 1024;
        return bookTotalSize;
    }

    /**
     * 保存bitmap到本地
     *
     * @param bitmap
     * @return
     */
    public static File saveBitmap(Context context, Bitmap bitmap, String createTime, Integer index) {
        String savePath = new Constants(context).getCaptureMaterialImageFile() + createTime + "/";
//        String savePath = "/data/data/com.huawei.hms.modeling/files/3DMagic" + createTime + "/";
//        String savePath = "/sdcard/3DMagic/3DMagic" + createTime + "/";
        File filePic;
        try {
            filePic = new File(savePath + index + ".jpg");
            if (!filePic.exists()) {
                if (!filePic.getParentFile().exists()) {
                    filePic.getParentFile().mkdirs();
                }
                filePic.createNewFile();
            }
            FileOutputStream fos = new FileOutputStream(filePic);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, fos);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
            LogUtil.d("saveBitmap: 2return");
            return null;
        }
        LogUtil.d("saveBitmap: " + filePic.getAbsolutePath());
        return filePic;
    }

    public static void saveBitmap(Bitmap bitmap, String fileName) {
        try {
            File filePic = new File(fileName);
            if (!filePic.exists()) {
                if (!filePic.getParentFile().exists()) {
                    filePic.getParentFile().mkdirs();
                }
//                        filePic.createNewFile();
            }

            FileOutputStream fos = new FileOutputStream(filePic);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();

        } catch (IOException ioe) {
            throw new RuntimeException("Unable to create output file ", ioe);
        }
    }

    public static Bitmap rotateBitmap(Bitmap bmp) {
        Matrix matrix = new Matrix();
        matrix.postRotate(-90);
        return Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true);
    }

    /**
     * Image 转 Bitmap
     * @param image
     * @return
     */
    public static Bitmap image2Bitmap(Image image) {
        int mHeight = image.getHeight();
        int mWidth = image.getWidth();

        int pixelData[] = new int[mWidth * mHeight];
        IntBuffer buf = IntBuffer.wrap(pixelData);
        buf.position(0);
        GLES20.glReadPixels(0, 0, mWidth, mHeight,
                GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, buf);

        int bitmapData[] = new int[pixelData.length];
        for (int i = 0; i < mHeight; i++) {
            for (int j = 0; j < mWidth; j++) {
                int p = pixelData[i * mWidth + j];
                int b = (p & 0x00ff0000) >> 16;
                int r = (p & 0x000000ff) << 16;
                int ga = p & 0xff00ff00;
                bitmapData[(mHeight - i - 1) * mWidth + j] = ga | r | b;
            }
        }
        return Bitmap.createBitmap(bitmapData,
                mWidth, mHeight, Bitmap.Config.ARGB_8888);
    }

    /**
     * 压缩图片
     *
     * @param image
     * @return
     */
    public static Bitmap compressImage(Bitmap image) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        /** 质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中*/
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        /** 把压缩后的数据baos存放到ByteArrayInputStream中*/
        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());
        /** 把ByteArrayInputStream数据生成图片*/
        Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);
        return bitmap;
    }

    /**
     * 文件夹删除
     * */
    public static void deleteFile(File file) {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            for (int i = 0; i < files.length; i++) {
                File f = files[i];
                deleteFile(f);
            }
            file.delete();//如要保留文件夹，只删除文件，请注释这行
        } else if (file.exists()) {
            file.delete();
        }
    }

    /**
     * 压缩图片文件
     * */
    public static void compressRgbPic(Context context,String createTime ,int index ,final File picFile, OnCompressListener listener){
//        String savePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
//                .toString()
//                + File.separator;
        String savePath = "/sdcard/3DMagic/3DMagic/Compression/" + createTime + "/"+createTime+"/";
        File dir = new File(savePath);
        if (!dir.exists()) {
            if(!dir.mkdirs()) {
                return;
            }
        }
        Luban.with(context)
                .load(picFile.getPath())
                .ignoreBy(100)
                .setTargetDir(savePath)

                .filter(new CompressionPredicate() {
                    @Override
                    public boolean apply(String path) {
                        return !(TextUtils.isEmpty(path) || path.toLowerCase().endsWith(".gif"));
                    }
                })
                .setCompressListener(listener).launch();
    }

    private static final int COLOR_FormatI420 = 1;
    private static final int COLOR_FormatNV21 = 2;

    private static boolean isImageFormatSupported(Image image) {
        int format = image.getFormat();
        switch (format) {
            case ImageFormat.YUV_420_888:
            case ImageFormat.NV21:
            case ImageFormat.YV12:
                return true;
        }
        return false;
    }

    public static byte[] getDataFromImage(Image image, int colorFormat) {
        if (colorFormat != COLOR_FormatI420 && colorFormat != COLOR_FormatNV21) {
            throw new IllegalArgumentException("only support COLOR_FormatI420 " + "and COLOR_FormatNV21");
        }
        if (!isImageFormatSupported(image)) {
            throw new RuntimeException("can't convert Image to byte array, format " + image.getFormat());
        }
        Rect crop = image.getCropRect();
        int format = image.getFormat();
        int width = crop.width();
        int height = crop.height();
        Image.Plane[] planes = image.getPlanes();
        byte[] data = new byte[width * height * ImageFormat.getBitsPerPixel(format) / 8];
        byte[] rowData = new byte[planes[0].getRowStride()];
//        if (VERBOSE) Log.v(TAG, "get data from " + planes.length + " planes");
        int channelOffset = 0;
        int outputStride = 1;
        for (int i = 0; i < planes.length; i++) {
            switch (i) {
                case 0:
                    channelOffset = 0;
                    outputStride = 1;
                    break;
                case 1:
                    if (colorFormat == COLOR_FormatI420) {
                        channelOffset = width * height;
                        outputStride = 1;
                    } else if (colorFormat == COLOR_FormatNV21) {
                        channelOffset = width * height + 1;
                        outputStride = 2;
                    }
                    break;
                case 2:
                    if (colorFormat == COLOR_FormatI420) {
                        channelOffset = (int) (width * height * 1.25);
                        outputStride = 1;
                    } else if (colorFormat == COLOR_FormatNV21) {
                        channelOffset = width * height;
                        outputStride = 2;
                    }
                    break;
            }
            ByteBuffer buffer = planes[i].getBuffer();
            int rowStride = planes[i].getRowStride();
            int pixelStride = planes[i].getPixelStride();
//            if (VERBOSE) {
//                Log.v(TAG, "pixelStride " + pixelStride);
//                Log.v(TAG, "rowStride " + rowStride);
//                Log.v(TAG, "width " + width);
//                Log.v(TAG, "height " + height);
//                Log.v(TAG, "buffer size " + buffer.remaining());
//            }
            int shift = (i == 0) ? 0 : 1;
            int w = width >> shift;
            int h = height >> shift;
            buffer.position(rowStride * (crop.top >> shift) + pixelStride * (crop.left >> shift));
            for (int row = 0; row < h; row++) {
                int length;
                if (pixelStride == 1 && outputStride == 1) {
                    length = w;
                    buffer.get(data, channelOffset, length);
                    channelOffset += length;
                } else {
                    length = (w - 1) * pixelStride + 1;
                    buffer.get(rowData, 0, length);
                    for (int col = 0; col < w; col++) {
                        data[channelOffset] = rowData[col * pixelStride];
                        channelOffset += outputStride;
                    }
                }
                if (row < h - 1) {
                    buffer.position(buffer.position() + rowStride - length);
                }
            }
//            if (VERBOSE) Log.v(TAG, "Finished reading data from plane " + i);
        }
        return data;
    }


    public static File saveRgbBitmap(Context context, Bitmap bitmap, String createTime, Integer index) {
        String savePath = "/sdcard/3DMagic/3DMagic/Compression/" + createTime + "/";
        File filePic;
        try {
            filePic = new File(savePath + index + ".jpg");
            if (!filePic.exists()) {
                if (!filePic.getParentFile().exists()) {
                    filePic.getParentFile().mkdirs();
                }
                filePic.createNewFile();
            }
            FileOutputStream fos = new FileOutputStream(filePic);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, fos);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
            LogUtil.d("saveBitmap: 2return");
            return null;
        }
        LogUtil.d("saveBitmap: " + filePic.getAbsolutePath());
        return filePic;
    }

}
