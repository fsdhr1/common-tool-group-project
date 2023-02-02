package com.grandtech.ocrlib;

import static android.graphics.Color.blue;
import static android.graphics.Color.green;
import static android.graphics.Color.red;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.util.Log;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.ThreadUtils;

public class Predictor {
    private static final String TAG = Predictor.class.getSimpleName();
    private boolean isLoaded = false;
    private int warmupIterNum = 0;
    private int inferIterNum = 1;
    private int cpuThreadNum = 4;
    private String cpuPowerMode = "LITE_POWER_FULL";
    private String modelPath = "";
    private String modelName = "";
    private OCRPredictorNative paddlePredictor = null;
    private float inferenceTime = 0;
    // Only for object detection
    private Vector<String> wordLabels = new Vector<String>();
    private String inputColorFormat = "BGR";
    private long[] inputShape = new long[]{1, 3, 960};
    private float[] inputMean = new float[]{0.485f, 0.456f, 0.406f};
    private float[] inputStd = new float[]{0.229f, 0.224f, 0.225f};
    private float scoreThreshold = 0.1f;
    private Bitmap inputImage = null;
    private Bitmap outputImage = null;
    private volatile String outputResult = "";
    private float preprocessTime = 0;
    private float postprocessTime = 0;
    private ArrayList<OcrResultModel> mOcrResultModels;


    public Predictor() {
       cpuThreadNum = Runtime.getRuntime().availableProcessors();
    }

    public boolean init(Context appCtx, String modelPath, String labelPath) {
        isLoaded = loadModel(appCtx, modelPath, cpuThreadNum, cpuPowerMode);
        if (!isLoaded) {
            return false;
        }
        isLoaded = loadLabel(appCtx, labelPath);
        return isLoaded;
    }


    public boolean init(Context appCtx, String modelPath, String labelPath, int cpuThreadNum, String cpuPowerMode,
                        String inputColorFormat,
                        long[] inputShape, float[] inputMean,
                        float[] inputStd, float scoreThreshold) {
        if (inputShape.length != 3) {
            Log.e(TAG, "Size of input shape should be: 3");
            return false;
        }
        if (inputMean.length != inputShape[1]) {
            Log.e(TAG, "Size of input mean should be: " + Long.toString(inputShape[1]));
            return false;
        }
        if (inputStd.length != inputShape[1]) {
            Log.e(TAG, "Size of input std should be: " + Long.toString(inputShape[1]));
            return false;
        }
        if (inputShape[0] != 1) {
            Log.e(TAG, "Only one batch is supported in the image classification demo, you can use any batch size in " +
                    "your Apps!");
            return false;
        }
        if (inputShape[1] != 1 && inputShape[1] != 3) {
            Log.e(TAG, "Only one/three channels are supported in the image classification demo, you can use any " +
                    "channel size in your Apps!");
            return false;
        }
        if (!inputColorFormat.equalsIgnoreCase("BGR")) {
            Log.e(TAG, "Only  BGR color format is supported.");
            return false;
        }
        boolean isLoaded = init(appCtx, modelPath, labelPath);
        if (!isLoaded) {
            return false;
        }
        this.inputColorFormat = inputColorFormat;
        this.inputShape = inputShape;
        this.inputMean = inputMean;
        this.inputStd = inputStd;
        this.scoreThreshold = scoreThreshold;
        return true;
    }

    protected boolean loadModel(Context appCtx, String modelPath, int cpuThreadNum, String cpuPowerMode) {
        // Release model if exists
        releaseModel();

        // Load model
        if (modelPath.isEmpty()) {
            return false;
        }
        String realPath = modelPath;
        if (!modelPath.substring(0, 1).equals("/")) {
            // Read model files from custom path if the first character of mode path is '/'
            // otherwise copy model to cache from assets
            realPath = appCtx.getCacheDir() + "/" + modelPath;
            Utils.copyDirectoryFromAssets(appCtx, modelPath, realPath);
        }
        if (realPath.isEmpty()) {
            return false;
        }

        OCRPredictorNative.Config config = new OCRPredictorNative.Config();
        config.cpuThreadNum = cpuThreadNum;
        config.detModelFilename = realPath + File.separator + "ch_ppocr_mobile_v2.0_det_opt.nb";
        config.recModelFilename = realPath + File.separator + "ch_ppocr_mobile_v2.0_rec_opt.nb";
        config.clsModelFilename = realPath + File.separator + "ch_ppocr_mobile_v2.0_cls_opt.nb";
        Log.e("Predictor", "model path" + config.detModelFilename + " ; " + config.recModelFilename + ";" + config.clsModelFilename);
        config.cpuPower = cpuPowerMode;
        paddlePredictor = new OCRPredictorNative(config);

        this.cpuThreadNum = cpuThreadNum;
        this.cpuPowerMode = cpuPowerMode;
        this.modelPath = realPath;
        this.modelName = realPath.substring(realPath.lastIndexOf("/") + 1);
        return true;
    }

    public void releaseModel() {
        if (paddlePredictor != null) {
            paddlePredictor.destory();
            paddlePredictor = null;
        }
        isLoaded = false;
        cpuThreadNum = 1;
        cpuPowerMode = "LITE_POWER_HIGH";
        modelPath = "";
        modelName = "";
    }

    protected boolean loadLabel(Context appCtx, String labelPath) {
        wordLabels.clear();
        wordLabels.add("black");
        // Load word labels from file
        try {
            InputStream assetsInputStream = appCtx.getAssets().open(labelPath);
            int available = assetsInputStream.available();
            byte[] lines = new byte[available];
            assetsInputStream.read(lines);
            assetsInputStream.close();
            String words = new String(lines);
            String[] contents = words.split("\n");
            for (String content : contents) {
                wordLabels.add(content);
            }
            Log.i(TAG, "Word label size: " + wordLabels.size());
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            return false;
        }
        return true;
    }


    public boolean runModel() {
        if (inputImage == null || !isLoaded()) {
            return false;
        }

        // Pre-process image, and feed input tensor with pre-processed data

        Bitmap scaleImage = Utils.resizeWithStep(inputImage, Long.valueOf(inputShape[2]).intValue(), 32);

        Date start = new Date();
        int channels = (int) inputShape[1];
        int width = scaleImage.getWidth();
        int height = scaleImage.getHeight();
        float[] inputData = new float[channels * width * height];
        if (channels == 3) {
            int[] channelIdx = null;
            if (inputColorFormat.equalsIgnoreCase("RGB")) {
                channelIdx = new int[]{0, 1, 2};
            } else if (inputColorFormat.equalsIgnoreCase("BGR")) {
                channelIdx = new int[]{2, 1, 0};
            } else {
                Log.i(TAG, "Unknown color format " + inputColorFormat + ", only RGB and BGR color format is " +
                        "supported!");
                return false;
            }
            int[] channelStride = new int[]{width * height, width * height * 2};
            proProcessDataMultiThread(inputData,scaleImage,height,width,channelStride,channelIdx);
            /*for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int color = scaleImage.getPixel(x, y);
                    float[] rgb = new float[]{(float) red(color) / 255.0f, (float) green(color) / 255.0f,
                            (float) blue(color) / 255.0f};
                    inputData[y * width + x] = (rgb[channelIdx[0]] - inputMean[0]) / inputStd[0];
                    inputData[y * width + x + channelStride[0]] = (rgb[channelIdx[1]] - inputMean[1]) / inputStd[1];
                    inputData[y * width + x + channelStride[1]] = (rgb[channelIdx[2]] - inputMean[2]) / inputStd[2];

                }
            }*/
        } 
        
        else if (channels == 1) {
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int color = inputImage.getPixel(x, y);
                    float gray = (float) (red(color) + green(color) + blue(color)) / 3.0f / 255.0f;
                    inputData[y * width + x] = (gray - inputMean[0]) / inputStd[0];
                }
            }
        } else {
            Log.i(TAG, "Unsupported channel size " + Integer.toString(channels) + ",  only channel 1 and 3 is " +
                    "supported!");
            return false;
        }
        float[] pixels = inputData;
        Log.i(TAG, "pixels " + pixels[0] + " " + pixels[1] + " " + pixels[2] + " " + pixels[3]
                + " " + pixels[pixels.length / 2] + " " + pixels[pixels.length / 2 + 1] + " " + pixels[pixels.length - 2] + " " + pixels[pixels.length - 1]);
        Date end = new Date();
        preprocessTime = (float) (end.getTime() - start.getTime());

        // Warm up
        for (int i = 0; i < warmupIterNum; i++) {
            paddlePredictor.runImage(inputData, width, height, channels, inputImage);
        }
        warmupIterNum = 0; // do not need warm
        // Run inference
        start = new Date();
        mOcrResultModels = paddlePredictor.runImage(inputData, width, height, channels, inputImage);
        end = new Date();
        inferenceTime = (end.getTime() - start.getTime()) / (float) inferIterNum;
        mOcrResultModels = postprocess(mOcrResultModels);
        Log.i(TAG, "[stat] Preprocess Time: " + preprocessTime
                + " ; Inference Time: " + inferenceTime + " ;Box Size " + mOcrResultModels.size());
        //drawResults(mOcrResultModels);

        return true;
    }


    public boolean isLoaded() {
        return paddlePredictor != null && isLoaded;
    }

    public String modelPath() {
        return modelPath;
    }

    public String modelName() {
        return modelName;
    }

    public int cpuThreadNum() {
        return cpuThreadNum;
    }

    public String cpuPowerMode() {
        return cpuPowerMode;
    }

    public float inferenceTime() {
        return inferenceTime;
    }

    public Bitmap inputImage() {
        return inputImage;
    }

    public Bitmap outputImage() {
        return outputImage;
    }

    public String outputResult() {
        return outputResult;
    }

    public float preprocessTime() {
        return preprocessTime;
    }

    public float postprocessTime() {
        return postprocessTime;
    }

    public ArrayList<OcrResultModel> getOcrResultModels() {
        return mOcrResultModels;
    }

    public void setInputImage(Bitmap image) {
        if (image == null) {
            return;
        }
        this.inputImage = image.copy(Bitmap.Config.ARGB_8888, true);
    }


    private ArrayList<OcrResultModel> postprocess(ArrayList<OcrResultModel> results) {
        for (OcrResultModel r : results) {
            StringBuffer word = new StringBuffer();
            for (int index : r.getWordIndex()) {
                if (index >= 0 && index < wordLabels.size()) {
                    word.append(wordLabels.get(index));
                } else {
                    Log.e(TAG, "Word index is not in label list:" + index);
                    word.append("×");
                }
            }
            r.setLabel(word.toString());
        }
        return results;
    }

    private void drawResults(ArrayList<OcrResultModel> results) {
        StringBuffer outputResultSb = new StringBuffer("");
        for (int i = 0; i < results.size(); i++) {
            OcrResultModel result = results.get(i);
            StringBuilder sb = new StringBuilder("");
            sb.append(result.getLabel());
            sb.append(" ").append(result.getConfidence());
            sb.append("; Points: ");
            for (Point p : result.getPoints()) {
                sb.append("(").append(p.x).append(",").append(p.y).append(") ");
            }
            Log.i(TAG, sb.toString()); // show LOG in Logcat panel
            outputResultSb.append(i + 1).append(": ").append(result.getLabel()).append("\n");
        }
        outputResult = outputResultSb.toString();
        /*outputImage = inputImage;
        Canvas canvas = new Canvas(outputImage);
        Paint paintFillAlpha = new Paint();
        paintFillAlpha.setStyle(Paint.Style.FILL);
        paintFillAlpha.setColor(Color.parseColor("#3B85F5"));
        paintFillAlpha.setAlpha(50);

        Paint paint = new Paint();
        paint.setColor(Color.parseColor("#3B85F5"));
        paint.setStrokeWidth(5);
        paint.setStyle(Paint.Style.STROKE);

        for (OcrResultModel result : results) {
            Path path = new Path();
            List<Point> points = result.getPoints();
            path.moveTo(points.get(0).x, points.get(0).y);
            for (int i = points.size() - 1; i >= 0; i--) {
                Point p = points.get(i);
                path.lineTo(p.x, p.y);
            }
            canvas.drawPath(path, paint);
            canvas.drawPath(path, paintFillAlpha);
        }*/
    }

    /**
     * 华为p30 4核: Preprocess Time: 698.0 ; Inference Time: 467.0 ;Box Size 8
     * 华为p30为8核:  Preprocess Time: 693.0 ; Inference Time: 399.0 ;Box Size 8
     *
     * 小米 10x5g 4核:Preprocess Time: 905.0 ; Inference Time: 493.0 ;Box Size 10
     * 小米 10x5g 8核:[Preprocess Time: 727.0 ; Inference Time: 420.0 ;Box Size 10
     *
     * 昂达 x20 4核:Preprocess Time: 2154.0 ; Inference Time: 1330.0 ;Box Size 8
     * 昂达 x20 10核:Preprocess Time: 2881.0 ; Inference Time: 1114.0 ;Box Size 8
     *
     */
    private void proProcessDataMultiThread(float[] inputData,Bitmap scaleImage, int height, int width, int[] channelStride,int[] channelIdx) {
        int threadNum = cpuThreadNum;
        if (threadNum > height) {
            threadNum = height;
        }
        int threadHandleCount = height / threadNum;
        int lastThreadHandleCount = threadHandleCount + height % threadNum;
        HandleDataThread[] handleDataThreads = new HandleDataThread[threadNum];
        for (int i = 0;i < threadNum;i++){
            handleDataThreads[i] = new HandleDataThread(inputData,scaleImage,width,channelStride,channelIdx);
            handleDataThreads[i].threadNum = i;
            handleDataThreads[i].indexStart = i * threadHandleCount;
            if(i != handleDataThreads.length - 1){
                handleDataThreads[i].indexEnd = handleDataThreads[i].indexStart + threadHandleCount;
            }else {
                handleDataThreads[i].indexEnd = handleDataThreads[i].indexStart + lastThreadHandleCount;
            }
        }
        ExecutorService cpuPool = ThreadUtils.getCpuPool();
        List<Future> futures = new ArrayList<>();
        for (HandleDataThread handleDataThread : handleDataThreads) {
            Future<?> future = cpuPool.submit(handleDataThread);
            futures.add(future);
        }
        for (Future future : futures) {
            try {
                future.get();
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }
        //LogUtils.aTag("HandleDataThread","跳出循环");

    }
    
    private class HandleDataThread extends Thread {
        private int indexStart;
        private int indexEnd;
        private Bitmap scaleImage;
        private int width;
        private float[] inputData;
        private int[] channelStride;
        private int[] channelIdx;
        private int threadNum;
        public HandleDataThread(float[] inputData, Bitmap scaleImage, int width, int[] channelStride,int[] channelIdx) {
            this.inputData = inputData;
            this.scaleImage = scaleImage;
            this.width = width;
            this.channelStride = channelStride;
            this.channelIdx = channelIdx;
        }

        @Override
        public void run() {
           // LogUtils.dTag("HandleDataThread",threadNum + "启动");
            long start = System.currentTimeMillis();
            for (int y = indexStart;y < indexEnd;y++){
                for (int x = 0; x < width; x++) {
                    int color = scaleImage.getPixel(x, y);
                    float[] rgb = new float[]{(float) red(color) / 255.0f, (float) green(color) / 255.0f,
                            (float) blue(color) / 255.0f};
                    inputData[y * width + x] = (rgb[channelIdx[0]] - inputMean[0]) / inputStd[0];
                    inputData[y * width + x + channelStride[0]] = (rgb[channelIdx[1]] - inputMean[1]) / inputStd[1];
                    inputData[y * width + x + channelStride[1]] = (rgb[channelIdx[2]] - inputMean[2]) / inputStd[2];
                }
            }
            long end = System.currentTimeMillis();
            LogUtils.eTag("HandleDataThread",threadNum + "执行完成:"+ (end - start));
        }
    }
}
