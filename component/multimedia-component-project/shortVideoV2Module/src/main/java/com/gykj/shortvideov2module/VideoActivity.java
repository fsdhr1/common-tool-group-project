package com.gykj.shortvideov2module;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.MediaMetadataRetriever;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.SystemClock;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicYuvToRGB;
import android.renderscript.Type;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.bumptech.glide.Glide;
import com.cazaea.sweetalert.SweetAlertDialog;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * @author Dell
 */
public class VideoActivity extends AppCompatActivity implements TextureView.SurfaceTextureListener{
    private TextureView mTextureView;//4.0?????????????????? surfaceview
    private TasksCompletedView taskView;
    private int mTotalProgress;
    private int mCurrentProgress;
    private Handler mHandler = new Handler();
    private boolean isClick;

    private CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {

        @Override
        public void onOpened(@NonNull CameraDevice cameraDevice) {
            mCameraDevice = cameraDevice;
            startPreview();
            mCameraOpenCloseLock.release();
            if (null != mTextureView) {
                configureTransform(mTextureView.getWidth(), mTextureView.getHeight());
            }
            rs = RenderScript.create(VideoActivity.this);
            yuvToRgbIntrinsic = ScriptIntrinsicYuvToRGB.create(rs, Element.U8_4(rs));
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice cameraDevice) {
            mCameraOpenCloseLock.release();
            cameraDevice.close();
            mCameraDevice = null;
        }

        @Override
        public void onError(@NonNull CameraDevice cameraDevice, int error) {
            mCameraOpenCloseLock.release();
            cameraDevice.close();
            mCameraDevice = null;
            finish();

        }

    };
    /**
     * Semaphore??????????????????????????????????????????????????????????????????????????????????????????????????????????????????
     */

    private Semaphore mCameraOpenCloseLock = new Semaphore(1);
    private HandlerThread mBackgroundThread;
    private Handler mBackgroundHandler;
    private Integer mSensorOrientation;
    private Size mVideoSize;
    private Size mPreviewSize;
    private CameraDevice mCameraDevice;
    private MediaRecorder mMediaRecorder;
    private CaptureRequest.Builder mPreviewBuilder;
    private CameraCaptureSession mPreviewSession;
    private boolean mIsRecordingVideo;
    private String mNextVideoAbsolutePath;
    private static final int SENSOR_ORIENTATION_DEFAULT_DEGREES = 90;
    private static final int SENSOR_ORIENTATION_INVERSE_DEGREES = 270;
    private static final SparseIntArray DEFAULT_ORIENTATIONS = new SparseIntArray();
    private static final SparseIntArray INVERSE_ORIENTATIONS = new SparseIntArray();
    private String basePath;
    private String videoName="";
    static {
        DEFAULT_ORIENTATIONS.append(Surface.ROTATION_0, 90);
        DEFAULT_ORIENTATIONS.append(Surface.ROTATION_90, 0);
        DEFAULT_ORIENTATIONS.append(Surface.ROTATION_180, 270);
        DEFAULT_ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    static {
        INVERSE_ORIENTATIONS.append(Surface.ROTATION_0, 270);
        INVERSE_ORIENTATIONS.append(Surface.ROTATION_90, 180);
        INVERSE_ORIENTATIONS.append(Surface.ROTATION_180, 90);
        INVERSE_ORIENTATIONS.append(Surface.ROTATION_270, 0);
    }

    private ImageView videoImage,videoLogo,ivImgSelect,ivImgCancel;
    private FrameLayout flResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video2);
        taskView = findViewById(R.id.tasks_view);
        videoImage = findViewById(R.id.video_image);
        videoLogo = findViewById(R.id.video_logo);
        flResult = findViewById(R.id.fl_result);
        ivImgSelect = findViewById(R.id.ivImgSelect);
        ivImgCancel = findViewById(R.id.ivImgCancel);
        mTextureView  = findViewById(R.id.camera_textureView);
        basePath = Environment.getExternalStorageDirectory().getAbsolutePath().toString()+"/video_common/";
        videoName = getIntent().getStringExtra("videoName");
        initEvent();//????????????????????????
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initEvent() {
        mTotalProgress = 100;
        mCurrentProgress = 0;
        taskView.setOnTouchListener((view, event) -> {
            switch (event.getAction()) {// ??????true??????????????????????????????????????????????????????
                case MotionEvent.ACTION_DOWN:
                    isClick = true;
                    mCurrentProgress = 0;
                    startplay();//????????????
                    return true;
                case MotionEvent.ACTION_UP:
                    isClick = false;
                    break;

                default:
                    break;
            }

            return false;
        });
        videoLogo.setOnClickListener(view -> {
            Intent intent =new Intent(VideoActivity.this,VideoPlayActivity.class);
            intent.putExtra("videoPath",mNextVideoAbsolutePath);
            startActivity(intent);
        });
        ivImgSelect.setOnClickListener(view -> {
            String thumbPicPath=bitmap2File(getVideoThumbnail(mNextVideoAbsolutePath));
            Intent intent = new Intent();
            intent.putExtra("videoPath",mNextVideoAbsolutePath);
            intent.putExtra("picPath",thumbPicPath);
            setResult(-1,intent);
            finish();
        });
        ivImgCancel.setOnClickListener(view -> comfireGiveUpCurrentDialog());
    }

    /**
     * ??????????????????????????????
     */
    public Bitmap getVideoThumbnail(String videoPath) {
        Bitmap bitmap = null;
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(videoPath);
            bitmap = retriever.getFrameAtTime();
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
        } catch (RuntimeException ex) {
            ex.printStackTrace();
        } finally {
            try {
                retriever.release();
            } catch (RuntimeException ex) {
                ex.printStackTrace();
            }
        }
        return bitmap;
    }

    public String bitmap2File(Bitmap bitmap) {
        String name =videoName+System.currentTimeMillis();
        File f = new File(basePath, name + ".jpg");
        if (f.exists()) {
            f.delete();
        }
        FileOutputStream fOut = null;
        try {
            fOut = new FileOutputStream(f);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
            fOut.flush();
            fOut.close();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return f.getAbsolutePath();
    }

    /**
     * description: ???????????? <br>
     */
    private void startplay() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mCurrentProgress < mTotalProgress) {
                    if (isClick) {// ????????????
                        if(!mIsRecordingVideo){
                            startRecordingVideo();//????????????
                        }
                        mCurrentProgress += 1;
                        mHandler.postDelayed(this, 50);
                        taskView.setProgress(mCurrentProgress);
                    } else {// ??????????????????
                        if (mCurrentProgress >= 50) {// ????????????50%????????????100%???
                            mCurrentProgress += 1;
                            mHandler.postDelayed(this, 10);
                            taskView.setProgress(mCurrentProgress);
                        } else {// ????????????50%?????????0
                            mCurrentProgress = 0;
                            taskView.setProgress(mCurrentProgress);
                        }
                    }
                }else if (mCurrentProgress==mTotalProgress){
                    if (mIsRecordingVideo){
                        stopRecordingVideo();//????????????
                        taskView.setVisibility(View.GONE);
                        flResult.setVisibility(View.VISIBLE);
                        Glide.with(VideoActivity.this).load(Uri.fromFile(new File(mNextVideoAbsolutePath))).into(videoImage);
                    }
                }
            }
        });

    }

    public void comfireGiveUpCurrentDialog(){
        SweetAlertDialog dialog = new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE);
        dialog.setTitleText("??????")
                .setContentText("????????????????????????")
                .setConfirmText("??????")
                .setCancelText("??????")
                .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        sweetAlertDialog.dismiss();
                    }
                })
                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        flResult.setVisibility(View.GONE);
                        taskView.setVisibility(View.VISIBLE);
                        FileUtils.delete(mNextVideoAbsolutePath);
                        mNextVideoAbsolutePath = null;
                        mCurrentProgress = 0;
                        taskView.setProgress(mCurrentProgress);
                        sweetAlertDialog.dismiss();
                    }
                })
                .show();

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            if (flResult.getVisibility()==View.VISIBLE){
                comfireGiveUpCurrentDialog();
                return true;
            }
            return super.onKeyDown(keyCode, event);
        }else {
            return super.onKeyDown(keyCode, event);
        }
    }

    @Override
    public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surfaceTexture, int width, int height) {
        openCamera(width, height);//????????????
    }

    @Override
    public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture surfaceTexture, int width, int height) {
        configureTransform(width, height);//????????????????????????
    }

    @Override
    public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture surfaceTexture) {
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(@NonNull SurfaceTexture surfaceTexture) {

    }


    @Override
    public void onResume() {
        super.onResume();
        startBackgroundThread();
        if (mTextureView.isAvailable()) {
            openCamera(mTextureView.getWidth(), mTextureView.getHeight());
        } else {
            mTextureView.setSurfaceTextureListener(this);
        }
    }

    /**
     * description: ???????????? <br>
     */
    private void stopRecordingVideo() {
        mIsRecordingVideo = false;
        mMediaRecorder.stop();
        mMediaRecorder.reset();
        //mNextVideoAbsolutePath = null;
        startPreview();
    }
    /**
     * description: ???????????? <br>
     */
    private void startRecordingVideo() {
        //texttureview ????????????
        if (null == mCameraDevice || !mTextureView.isAvailable() || null == mPreviewSize) {
            return;
        }
        try {
            closePreviewSession();//????????????
            setUpMediaRecorder();
            SurfaceTexture texture = mTextureView.getSurfaceTexture();
            assert texture != null;
            texture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
            mPreviewBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_RECORD);
            List<Surface> surfaces = new ArrayList<>();

            // surfac?????? ????????????
            Surface previewSurface = new Surface(texture);
            surfaces.add(previewSurface);
            mPreviewBuilder.addTarget(previewSurface);

            // android ??????????????????
            Surface recorderSurface = mMediaRecorder.getSurface();
            surfaces.add(recorderSurface);
            mPreviewBuilder.addTarget(recorderSurface);

            // Camera2.0 ??????????????????
            mCameraDevice.createCaptureSession(surfaces, new CameraCaptureSession.StateCallback() {

                @Override
                public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                    mPreviewSession = cameraCaptureSession;
                    updatePreview();//?????????????????????
                    //??????ui????????????
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            mIsRecordingVideo = true;

                            mMediaRecorder.start();
                        }
                    });
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                   ToastUtils.showShort("??????????????????");
                }
            }, mBackgroundHandler);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }
    /**
     * ????????????????????????
     */

    private void setUpMediaRecorder() throws IOException {

        mMediaRecorder = new MediaRecorder();
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        if (mNextVideoAbsolutePath == null || mNextVideoAbsolutePath.isEmpty()) {
            mNextVideoAbsolutePath = getVideoFilePath();

        }
        mMediaRecorder.setOutputFile(mNextVideoAbsolutePath);
        mMediaRecorder.setVideoEncodingBitRate(10000000);
        mMediaRecorder.setVideoFrameRate(30);
        mMediaRecorder.setVideoSize(mVideoSize.getWidth(), mVideoSize.getHeight());
        mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        int rotation = getWindowManager().getDefaultDisplay().getRotation();
        switch (mSensorOrientation) {
            case SENSOR_ORIENTATION_DEFAULT_DEGREES:
                mMediaRecorder.setOrientationHint(DEFAULT_ORIENTATIONS.get(rotation));
                break;
            case SENSOR_ORIENTATION_INVERSE_DEGREES:
                mMediaRecorder.setOrientationHint(INVERSE_ORIENTATIONS.get(rotation));
                break;
            default:
        }
        mMediaRecorder.prepare();
    }
    /**
     * description: ???????????? <br>
     */
    private String getVideoFilePath() {
        FileUtils.createOrExistsDir(basePath);
        String video =basePath+ SystemClock.currentThreadTimeMillis() +videoName+ ".mp4";
        File dir = new File(video);
        return video;
    }


    private void startBackgroundThread() {
        //??????????????????
        mBackgroundThread = new HandlerThread("CameraBackground");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }



    private static Size chooseVideoSize(Size[] choices) {
        for (Size size : choices) {
            if (size.getWidth() == size.getHeight() * 4 / 3 && size.getWidth() <= 1080) {
                return size;
            }
        }
        return choices[choices.length - 1];
    }


    private static Size chooseOptimalSize(Size[] choices, int width, int height, Size aspectRatio) {

        List<Size> bigEnough = new ArrayList<>();
        int w = aspectRatio.getWidth();
        int h = aspectRatio.getHeight();
        for (Size option : choices) {
            if (option.getHeight() == option.getWidth() * h / w &&
                    option.getWidth() >= width && option.getHeight() >= height) {
                bigEnough.add(option);
            }
        }
        if (bigEnough.size() > 0) {
            return Collections.min(bigEnough, new CompareSizesByArea());
        } else {
            return choices[0];
        }
    }


    static class CompareSizesByArea implements Comparator<Size> {

        @Override
        public int compare(Size lhs, Size rhs) {
            // We cast here to ensure the multiplications won't overflow
            return Long.signum((long) lhs.getWidth() * lhs.getHeight() -
                    (long) rhs.getWidth() * rhs.getHeight());
        }

    }

   /**
    * description: ???????????????????????? <br>
    */
    private void configureTransform(int viewWidth, int viewHeight) {
        if (null == mTextureView || null == mPreviewSize) {
            return;
        }
        int rotation = getWindowManager().getDefaultDisplay().getRotation();
        Matrix matrix = new Matrix();
        RectF viewRect = new RectF(0, 0, viewWidth, viewHeight);
        RectF bufferRect = new RectF(0, 0, mPreviewSize.getHeight(), mPreviewSize.getWidth());
        float centerX = viewRect.centerX();
        float centerY = viewRect.centerY();
        if (Surface.ROTATION_90 == rotation || Surface.ROTATION_270 == rotation) {
            bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY());
            matrix.setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL);
            float scale = Math.max(
                    (float) viewHeight / mPreviewSize.getHeight(),
                    (float) viewWidth / mPreviewSize.getWidth());
            matrix.postScale(scale, scale, centerX, centerY);
            matrix.postRotate(90 * (rotation - 2), centerX, centerY);
        }
        mTextureView.setTransform(matrix);
    }

    @SuppressWarnings("MissingPermission")
    private void openCamera(int width, int height) {

        if (isFinishing()) {
            return;
        }
        CameraManager manager = (CameraManager)getSystemService(Context.CAMERA_SERVICE);
        try {
            //????????????tryAcquire?????????????????????????????????1???????????????????????????????????????false
            if (!mCameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
               return;
            }
            String cameraId = manager.getCameraIdList()[0];

            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
            StreamConfigurationMap map = characteristics
                    .get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            mSensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
            float maxZoom = (characteristics.get(CameraCharacteristics.SCALER_AVAILABLE_MAX_DIGITAL_ZOOM)) * 10;
            Rect activeRect = characteristics.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE);
            if (map == null) {
                return;
            }
            // TODO: 2019/4/25  ?????????????????? 4:3 16:9
            mVideoSize = chooseVideoSize(map.getOutputSizes(MediaRecorder.class));//??????????????????
            mPreviewSize = chooseOptimalSize(map.getOutputSizes(SurfaceTexture.class),//??????????????????
                    width, height, mVideoSize);

            configureTransform(width, height);
            manager.openCamera(cameraId, mStateCallback, null);
        } catch (CameraAccessException e) {
            ToastUtils.showShort("????????????");
           finish();
        } catch (NullPointerException e) {
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    /**
     * description: ???????????? <br>
     */
    private void startPreview() {
        if (null == mCameraDevice || !mTextureView.isAvailable() || null == mPreviewSize) {
            return;
        }
        try {
            closePreviewSession();
            SurfaceTexture texture = mTextureView.getSurfaceTexture();
            assert texture != null;
            texture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
            mPreviewBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);

            Surface previewSurface = new Surface(texture);
            mPreviewBuilder.addTarget(previewSurface);

            mCameraDevice.createCaptureSession(Collections.singletonList(previewSurface),
                    new CameraCaptureSession.StateCallback() {

                        @Override
                        public void onConfigured(@NonNull CameraCaptureSession session) {
                            mPreviewSession = session;
                            updatePreview();//????????????
                        }

                        @Override
                        public void onConfigureFailed(@NonNull CameraCaptureSession session) {

                            ToastUtils.showShort("??????");

                        }
                    }, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * description: ???????????? <br>
     */
    private void updatePreview() {
        if (null == mCameraDevice) {
            return;
        }
        try {
            setUpCaptureRequestBuilder(mPreviewBuilder);
            mPreviewSession.setRepeatingRequest(mPreviewBuilder.build(), null, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }
    /**
     * description: ????????????????????????3A????????????????????????????????????????????????????????? <br>
     */
    private void setUpCaptureRequestBuilder(CaptureRequest.Builder builder) {
        builder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
    }
    /**
     * description: ???????????? <br>
     */
    private void closePreviewSession() {
        if (mPreviewSession != null) {
            mPreviewSession.close();
            mPreviewSession = null;
        }
    }

    /**
     * ?????????????????????
     */

    private byte[] dealByte(byte[] dst) {

        Bitmap bitmapAll = nv21ToBitmap(dst, mPreviewSize.getWidth(),
                mPreviewSize.getHeight());

        Bitmap bitmapAllNew=bitmapAll.copy(Bitmap.Config.ARGB_8888,true);
        Canvas canvas = new Canvas(bitmapAllNew);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.WHITE);
        paint.setTextSize(80);
        canvas.drawText("????????????", mPreviewSize.getWidth()/2, 100, paint);
        byte[] newBytes = bitmapToNv21(bitmapAllNew,mPreviewSize.getWidth(), mPreviewSize.getHeight());
        if(newBytes!=null){
            return newBytes;
        }else{
            return null;
        }
    }

    public static byte[] bitmapToNv21(Bitmap src, int width, int height) {
        if (src != null && src.getWidth() >= width && src.getHeight() >= height) {
            int[] argb = new int[width * height];
            src.getPixels(argb, 0, width, 0, 0, width, height);
            return argbToNv21(argb, width, height);
        } else {
            return null;
        }
    }
    private Type.Builder yuvType, rgbaType;
    private Allocation in, out;
    private RenderScript rs;
    private ScriptIntrinsicYuvToRGB yuvToRgbIntrinsic;
    public Bitmap nv21ToBitmap(byte[] nv21, int width, int height){
        if (yuvType == null){
            yuvType = new Type.Builder(rs, Element.U8(rs)).setX(nv21.length);
            in = Allocation.createTyped(rs, yuvType.create(), Allocation.USAGE_SCRIPT);
            rgbaType = new Type.Builder(rs, Element.RGBA_8888(rs)).setX(width).setY(height);
            out = Allocation.createTyped(rs, rgbaType.create(), Allocation.USAGE_SCRIPT);
        }
        in.copyFrom(nv21);
        yuvToRgbIntrinsic.setInput(in);
        yuvToRgbIntrinsic.forEach(out);
        Bitmap bmpout = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        out.copyTo(bmpout);
        return bmpout;
    }


    /**
     * ARGB???????????????NV21??????
     *
     * @param argb   argb??????
     * @param width  ??????
     * @param height ??????
     * @return nv21??????
     */
    private static byte[] argbToNv21(int[] argb, int width, int height) {
        int frameSize = width * height;
        int yIndex = 0;
        int uvIndex = frameSize;
        int index = 0;
        byte[] nv21 = new byte[width * height * 3 / 2];
        for (int j = 0; j < height; ++j) {
            for (int i = 0; i < width; ++i) {
                int R = (argb[index] & 0xFF0000) >> 16;
                int G = (argb[index] & 0x00FF00) >> 8;
                int B = argb[index] & 0x0000FF;
                int Y = (66 * R + 129 * G + 25 * B + 128 >> 8) + 16;
                int U = (-38 * R - 74 * G + 112 * B + 128 >> 8) + 128;
                int V = (112 * R - 94 * G - 18 * B + 128 >> 8) + 128;
                nv21[yIndex++] = (byte) (Y < 0 ? 0 : (Y > 255 ? 255 : Y));
                if (j % 2 == 0 && index % 2 == 0 && uvIndex < nv21.length - 2) {
                    nv21[uvIndex++] = (byte) (V < 0 ? 0 : (V > 255 ? 255 : V));
                    nv21[uvIndex++] = (byte) (U < 0 ? 0 : (U > 255 ? 255 : U));
                }

                ++index;
            }
        }
        return nv21;
    }

}
