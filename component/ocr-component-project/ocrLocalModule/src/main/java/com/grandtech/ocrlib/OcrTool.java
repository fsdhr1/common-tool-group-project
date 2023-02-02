package com.grandtech.ocrlib;

import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

/**
 * @author zyp
 * 2021/7/30
 */

@RequiresApi(23)
public class OcrTool {
    private static final int OPEN_GALLERY_REQUEST_CODE = 0;
    private static final int TAKE_PHOTO_REQUEST_CODE = 1;

    private static final int REQUEST_LOAD_MODEL = 0;
    private static final int REQUEST_RUN_MODEL = 1;
    private static final int RESPONSE_LOAD_MODEL_SUCCESSED = 0;
    private static final int RESPONSE_LOAD_MODEL_FAILED = 1;
    private static final int RESPONSE_RUN_MODEL_SUCCESSED = 2;
    private static final int RESPONSE_RUN_MODEL_FAILED = 3;

    private Handler receiver = null; // Receive messages from worker thread
    private Handler sender = null; // Send command to worker thread
    private HandlerThread worker = null; // Worker thread to load&run model

    // Model settings of object detection
    private static final String modelPath = "models/ocr_v2_for_cpu";
    private static final String labelPath = "labels/ppocr_keys_v1.txt";
    private final Context mContext;
    private Predictor predictor;
    private final OcrListener mOcrListener;

    public OcrTool(@NonNull Context context,@NonNull OcrListener ocrListener) {
        mOcrListener = ocrListener;
        mContext = context;
        receiver = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case RESPONSE_LOAD_MODEL_SUCCESSED:

                        onLoadModelSuccessed();
                        break;
                    case RESPONSE_LOAD_MODEL_FAILED:

                        Toast.makeText(mContext, "模型加载失败", Toast.LENGTH_SHORT).show();
                        onLoadModelFailed();
                        break;
                    case RESPONSE_RUN_MODEL_SUCCESSED:
                        onRunModelSuccessed();
                        break;
                    case RESPONSE_RUN_MODEL_FAILED:

                        Toast.makeText(mContext, "ocr失败", Toast.LENGTH_SHORT).show();
                        onRunModelFailed();
                        break;
                    default:
                        break;
                }
            }
        };
       
    }

    /**
     * 初始化
     */
    public void init() {
        if(predictor == null){
            predictor = new Predictor();
        }
        if(worker != null){
            worker.quit();
        }
        worker = new HandlerThread("Predictor Worker");
        worker.start();
        sender = new Handler(worker.getLooper()) {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case REQUEST_LOAD_MODEL:
                        // Load model and reload test image
                        if (onLoadModel()) {
                            receiver.sendEmptyMessage(RESPONSE_LOAD_MODEL_SUCCESSED);
                        } else {
                            receiver.sendEmptyMessage(RESPONSE_LOAD_MODEL_FAILED);
                        }
                        break;
                    case REQUEST_RUN_MODEL:
                        // Run model if model is loaded
                        if (onRunModel()) {
                            receiver.sendEmptyMessage(RESPONSE_RUN_MODEL_SUCCESSED);
                        } else {
                            receiver.sendEmptyMessage(RESPONSE_RUN_MODEL_FAILED);
                        }
                        break;
                    default:
                        break;
                }
            }
        };
        loadModel();
    }

    /**
     *开始进行ocr
     */
    public void startOcr(Bitmap bitmap) {
        if(predictor == null) return;
        predictor.setInputImage(bitmap);
        sender.sendEmptyMessage(REQUEST_RUN_MODEL);
    }

    /**
     * 释放资源
     */
    public void release(){
        if (predictor != null) {
            predictor.releaseModel();
            predictor = null;
        }
        worker.quit();
    }
    
    private void loadModel() {
        sender.sendEmptyMessage(REQUEST_LOAD_MODEL);
    }

    private boolean onLoadModel() {
        if(predictor == null) return false;
        return predictor.init(mContext, modelPath, labelPath);
    }

    private boolean onRunModel() {
        if(predictor == null) return false;
        return predictor.isLoaded() && predictor.runModel();
    }

    private void onLoadModelSuccessed() {
        // Load test image from path and run model
        mOcrListener.onLoadModelSuccess();
    }

    private void onLoadModelFailed() {
        mOcrListener.onLoadModelFailed();
    }

    private void onRunModelSuccessed() {
        mOcrListener.onRunModelSuccess(predictor.getOcrResultModels());
    }

    private void onRunModelFailed() {
        mOcrListener.onRunModelFailed();
    }

    public interface OcrListener {
        void onLoadModelSuccess();

        void onLoadModelFailed();

        void onRunModelSuccess(List<OcrResultModel> results);

        void onRunModelFailed();
    }
}
