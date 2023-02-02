package com.baidu.ocr.ui.util;

import android.graphics.Bitmap;

import com.baidu.net.RequestUtil;
import com.baidu.net.bean.BankCardResultBean;
import com.baidu.net.bean.BusinessLicenseResultBean;
import com.baidu.net.bean.DataResponse;
import com.baidu.net.bean.IDCardResultBean;
import com.baidu.net.bean.OCRError;
import com.baidu.net.bean.ResultBean;
import com.baidu.net.service.IOcrApi;
import com.blankj.utilcode.util.GsonUtils;
import com.blankj.utilcode.util.ImageUtils;
import com.blankj.utilcode.util.LogUtils;
import com.huawei.hmf.tasks.OnFailureListener;
import com.huawei.hmf.tasks.OnSuccessListener;
import com.huawei.hmf.tasks.Task;
import com.huawei.hms.mlplugin.card.bcr.MLBcrCaptureConfig;
import com.huawei.hms.mlsdk.card.MLBcrAnalyzerFactory;
import com.huawei.hms.mlsdk.card.MLCardAnalyzerFactory;
import com.huawei.hms.mlsdk.card.bcr.MLBankCard;
import com.huawei.hms.mlsdk.card.bcr.MLBcrAnalyzer;
import com.huawei.hms.mlsdk.card.bcr.MLBcrAnalyzerSetting;
import com.huawei.hms.mlsdk.card.icr.MLIcrAnalyzer;
import com.huawei.hms.mlsdk.card.icr.MLIcrAnalyzerSetting;
import com.huawei.hms.mlsdk.card.icr.MLIdCard;
import com.huawei.hms.mlsdk.common.MLFrame;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

/**
 * @author jyh
 * 2019-08-13
 */
public class OcrUtils extends RequestUtil {
    public static final String defultResult ="{\"words_result\":{\"姓名\":{\"words\":\"xingming\",\"location\":{\"top\":98,\"left\":194,\"width\":115,\"height\":41}},\"民族\":{\"words\":\"mingzu\",\"location\":{\"top\":173,\"left\":361,\"width\":29,\"height\":32}},\"住址\":{\"words\":\"zhuzhi\",\"location\":{\"top\":290,\"left\":207,\"width\":318,\"height\":71}},\"公民身份号码\":{\"words\":\"shenfenzhenghao\",\"location\":{\"top\":434,\"left\":343,\"width\":407,\"height\":38}},\"出生\":{\"words\":\"chusheng\",\"location\":{\"top\":230,\"left\":208,\"width\":256,\"height\":31}},\"性别\":{\"words\":\"xingbie\",\"location\":{\"top\":169,\"left\":196,\"width\":31,\"height\":33}},\"失效日期\":{\"words\":\"shixiaoqi\",\"location\":{\"top\":464,\"left\":551,\"width\":146,\"height\":34}},\"签发机关\":{\"words\":\"qianfajiguan\",\"location\":{\"top\":397,\"left\":396,\"width\":287,\"height\":36}},\"签发日期\":{\"words\":\"qianfaqi\",\"location\":{\"top\":460,\"left\":391,\"width\":145,\"height\":33}}},\"log_id\":1400754237385736192,\"words_result_num\":6,\"idcard_number_type\":1,\"image_status\":\"normal\",\"direction\":0}";

    /**
     *离线识别银行卡
     */
    public static void scanBcrResult(String path,OnBankCardOcrResultListenerAli listener){
        int[] size =ImageUtils.getSize(path);
        float minSize = size[0]>size[1]?size[1]:size[0];
        if (minSize<512){
            DecimalFormat df = new DecimalFormat("0.00");
            String scale =df.format(512f/size[1]) ;
            Bitmap bitmap =ImageUtils.compressByScale(ImageUtils.getBitmap(path),Float.valueOf(scale),Float.valueOf(scale),false);
            ImageUtils.save(bitmap,path, Bitmap.CompressFormat.JPEG,true);
        }
        MLBcrAnalyzerSetting setting =new MLBcrAnalyzerSetting.Factory().setResultType(MLBcrCaptureConfig.RESULT_ALL).create();
        MLBcrAnalyzer banalyzer = MLBcrAnalyzerFactory.getInstance().getBcrAnalyzer(setting);
        MLFrame frame = MLFrame.fromBitmap(ImageUtils.getBitmap(path));
        Task<MLBankCard> task =banalyzer.asyncAnalyseFrame(frame);
        task.addOnSuccessListener(new OnSuccessListener<MLBankCard>() {
            @Override
            public void onSuccess(MLBankCard mlBankCard) {
                LogUtils.v(GsonUtils.toJson(mlBankCard));
                if(mlBankCard.getRetCode()!=-1){
                    BankCardResultBean bankCardResultBean= new BankCardResultBean();
                    BankCardResultBean.Result result = new BankCardResultBean.Result();
                    result.setBank_card_number(mlBankCard.getNumber()==null?"":mlBankCard.getNumber());
                    result.setValid_date(mlBankCard.getExpire()==null?"":mlBankCard.getExpire());
                    result.setBank_name(mlBankCard.getIssuer()==null?"":mlBankCard.getIssuer());
                    bankCardResultBean.setResult(result);
                    listener.onOcrResultBankCard(bankCardResultBean);
                }else {
                    OCRError ocrError = new OCRError();
                    ocrError.setErrorCode(-1);
                    ocrError.setErrorMessage("识别失败");
                }
                banalyzer.stop();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                LogUtils.v(e);
                OCRError ocrError = new OCRError();
                ocrError.setErrorCode(-1);
                ocrError.setErrorMessage("识别失败");
                listener.onError(ocrError);
                banalyzer.stop();
            }
        });

    }


    /**
     *离线识别身份证
     */
    private static MLIcrAnalyzer analyzer;
    public static void scanIDResult(String path,String contentType,OnIDCardOcrResultListenerAli listener){
        int[] size =ImageUtils.getSize(path);
        float minSize = size[0]>size[1]?size[1]:size[0];
        if (minSize<512){
            DecimalFormat df = new DecimalFormat("0.00");
            String scale =df.format(512f/size[1]) ;
            Bitmap bitmap =ImageUtils.compressByScale(ImageUtils.getBitmap(path),Float.valueOf(scale),Float.valueOf(scale),false);
            ImageUtils.save(bitmap,path, Bitmap.CompressFormat.JPEG,true);
        }
        MLIcrAnalyzerSetting setting = new MLIcrAnalyzerSetting.Factory()
                .setSideType(contentType.toUpperCase())
                .create();
        analyzer = MLCardAnalyzerFactory.getInstance().getIcrAnalyzer(setting);
        MLFrame frame = new MLFrame.Creator().setBitmap(ImageUtils.getBitmap(path)).create();
        Task<MLIdCard> task = analyzer.asyncAnalyseFrame(frame);
        task.addOnSuccessListener(new OnSuccessListener<MLIdCard>() {
            @Override
            public void onSuccess(MLIdCard idCard) {
                // 检测成功的处理逻辑。
                LogUtils.v(GsonUtils.toJson(idCard));
                String resultString =defultResult;
                if (idCard.getRetCode()!=-1){
                   String newResult = resultString.replace("xingming",idCard.getName()==null?"":idCard.getName())
                            .replace("shenfenzhenghao",idCard.getIdNum()==null?"":idCard.getIdNum())
                            .replace("mingzu",idCard.getNation()==null?"":idCard.getNation())
                            .replace("zhuzhi",idCard.getAddress()==null?"":idCard.getAddress());
                    if (idCard.getValidDate()!=null){
                        String qzri = idCard.getValidDate();
                        if (qzri.contains("-")){
                            String[] rq = qzri.split("-");
                            newResult = newResult.replace("qianfaqi",rq[0]).replace("shixiaoqi",rq[1]);
                        }
                    }
                    IDCardResultBean idCardResultBean = GsonUtils.fromJson(newResult, IDCardResultBean.class);
                    idCardResultBean.contentType = contentType;
                    listener.onOcrResultIDCard(idCardResultBean);
                }else {
                    OCRError ocrError = new OCRError();
                    ocrError.setErrorCode(-1);
                    ocrError.setErrorMessage("识别失败");
                    ocrError.contentType = contentType;
                }
                release();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                LogUtils.v(e);
                OCRError ocrError = new OCRError();
                ocrError.setErrorCode(-1);
                ocrError.setErrorMessage("识别失败");
                ocrError.contentType = contentType;
                listener.onError(ocrError);
                release();
            }
        });
    }
    public static void release(){
        if (analyzer != null) {
            try {
                analyzer.stop();
            } catch (IOException e) {
                // 异常处理。
            }
        }
    }

    /**
     * 阿里云服务器 身份证识别
     *
     * @param isDelete 是否删除临时图片
     * @param filePath
     * @param contentType 正反面标识 front or back
     * @param token
     * @param listener
     */
    public static final String IDCARD_FRONT = "front";
    public static final String IDCARD_BACK = "back";
    
    public static void recIDCard (final boolean isDelete,  String filePath,  @IdCardType final String contentType, final String token,OnIDCardOcrResultListenerAli listener) {
        final File file = new File(filePath);

        RequestBody requestBody = RequestBody.create(MediaType.parse("multipart/form-data"), file);
        MultipartBody.Part part = MultipartBody.Part.createFormData("file", file.getName(), requestBody);
        request(IOcrApi.class, new NetWorkResult<DataResponse<String>>() {
            @Override
            public void onSuccess(DataResponse<String> data) {
                IDCardResultBean idCardResultBean = GsonUtils.fromJson(data.getData(), IDCardResultBean.class);
                idCardResultBean.contentType = contentType;
                //只判断身份证号码
                if (idCardResultBean==null||idCardResultBean.words_result==null||idCardResultBean.words_result.公民身份号码==null||idCardResultBean.words_result.公民身份号码.words==null){
                    scanIDResult(filePath,contentType,listener);
                    return;
                }
                listener.onOcrResultIDCard(idCardResultBean);

            }

            @Override
            public void onError(int err, String errMsg, Throwable t, DataResponse<String> data) {
                LogUtils.eTag("在线OCR识别失败：", err + "------" + errMsg);
                scanIDResult(filePath,contentType,listener);
            }
        }, token).idCard(part, contentType);
    }
    public interface OnIDCardOcrResultListenerAli {
        void onOcrResultIDCard(IDCardResultBean idCardResult);
        default void onError(OCRError ocrError){

        }
    }

    /**
     * 阿里云服务器 银行卡识别
     *
     * @param isDelete 是否删除临时图片
     * @param filePath
     * @param token
     * @param listener
     */
    public static void recBankCard (final boolean isDelete, final String filePath, final String token, final OnBankCardOcrResultListenerAli listener) {
        final File file = new File(filePath);

        RequestBody requestBody = RequestBody.create(MediaType.parse("multipart/form-data"), file);
        MultipartBody.Part part = MultipartBody.Part.createFormData("file", file.getName(), requestBody);
        request(IOcrApi.class, new NetWorkResult<DataResponse<String>>() {
            @Override
            public void onSuccess(DataResponse<String> data) {
                BankCardResultBean bankCardResultBean =GsonUtils.fromJson(data.getData(), BankCardResultBean.class);
                if (bankCardResultBean==null||bankCardResultBean.getResult()==null||bankCardResultBean.getResult().getBank_card_number()==null){
                    scanBcrResult(filePath,listener);
                    return;
                }
                listener.onOcrResultBankCard(bankCardResultBean);
            }

            @Override
            public void onError(int err, String errMsg, Throwable t, DataResponse<String> data) {
                LogUtils.eTag("OCR识别失败：", err + "------" + errMsg);
                scanBcrResult(filePath,listener);
            }
        }, token).bankCard(part);
    }
    public interface OnBankCardOcrResultListenerAli {
        void onOcrResultBankCard(BankCardResultBean bankCardResult);
        default void onError(OCRError ocrError){

        }
    }

    /**
     * 阿里云服务器 营业执照识别
     *
     * @param isDelete 是否删除临时图片
     * @param filePath
     * @param token
     * @param listener
     */
    public static void recBusinessLicense (final boolean isDelete, final String filePath, final String token, final OnBusinessLicenseOcrResultListenerAli listener) {
        final File file = new File(filePath);

        RequestBody requestBody = RequestBody.create(MediaType.parse("multipart/form-data"), file);
        MultipartBody.Part part = MultipartBody.Part.createFormData("file", file.getName(), requestBody);
        request(IOcrApi.class, new NetWorkResult<DataResponse<String>>() {
            @Override
            public void onSuccess(DataResponse<String> data) {
                if(isDelete)
                    file.delete();
                listener.OnOcrResultBusinessLicense(GsonUtils.fromJson(data.getData(), BusinessLicenseResultBean.class));
            }

            @Override
            public void onError(int err, String errMsg, Throwable t, DataResponse<String> data) {
                LogUtils.eTag("OCR识别失败：", err + "------" + errMsg);
                file.delete();
                OCRError ocrError = new OCRError();
                ocrError.setErrorCode(err);
                ocrError.setErrorMessage(errMsg);
                listener.onError(ocrError);
            }
        }, token).businessLicense(part);
    }
    public interface OnBusinessLicenseOcrResultListenerAli {
        void OnOcrResultBusinessLicense(BusinessLicenseResultBean businessLicenseResult);
        default void onError(OCRError ocrError){

        }
    }
    /**
     * 动物识别
     *
     * @param isDelete 是否删除临时图片
     * @param filePath
     * @param token
     * @param listener
     */
    public static void animalDetect (final boolean isDelete, final String filePath, final String token, final OnAnimalDetectResultListenerAi listener) {
        final File file = new File(filePath);

        RequestBody requestBody = RequestBody.create(MediaType.parse("multipart/form-data"), file);
        MultipartBody.Part part = MultipartBody.Part.createFormData("file", file.getName(), requestBody);
        request(IOcrApi.class, new NetWorkResult<DataResponse<String>>() {
            @Override
            public void onSuccess(DataResponse<String> data) {
                if(isDelete)
                    file.delete();
                listener.onAnimalDetectLicense(GsonUtils.fromJson(data.getData(), ResultBean.class));
            }

            @Override
            public void onError(int err, String errMsg, Throwable t, DataResponse<String> data) {
                LogUtils.eTag("OCR识别失败：", err + "------" + errMsg);
                file.delete();
                OCRError ocrError = new OCRError();
                ocrError.setErrorCode(err);
                ocrError.setErrorMessage(errMsg);
                listener.onError(ocrError);
            }
        }, token).animalDetect(part);
    }
    public interface OnAnimalDetectResultListenerAi {
        void onAnimalDetectLicense(ResultBean resultBean);
        default void onError(OCRError ocrError){

        }
    }


    /**
     * 植物识别
     *
     * @param isDelete 是否删除临时图片
     * @param filePath
     * @param token
     * @param listener
     */
    public static void plantDetect (final boolean isDelete, final String filePath, final String token, final OnPlantDetectResultListenerAi listener) {
        final File file = new File(filePath);

        RequestBody requestBody = RequestBody.create(MediaType.parse("multipart/form-data"), file);
        MultipartBody.Part part = MultipartBody.Part.createFormData("file", file.getName(), requestBody);
        request(IOcrApi.class, new NetWorkResult<DataResponse<String>>() {
            @Override
            public void onSuccess(DataResponse<String> data) {
                if(isDelete)
                    file.delete();
                listener.onPlantDetectLicense(GsonUtils.fromJson(data.getData(), ResultBean.class));
            }

            @Override
            public void onError(int err, String errMsg, Throwable t, DataResponse<String> data) {
                LogUtils.eTag("OCR识别失败：", err + "------" + errMsg);
                file.delete();
                OCRError ocrError = new OCRError();
                ocrError.setErrorCode(err);
                ocrError.setErrorMessage(errMsg);
                listener.onError(ocrError);
            }
        }, token).plantDetect(part);
    }
    public interface OnPlantDetectResultListenerAi {
        void onPlantDetectLicense(ResultBean resultBean);
        default void onError(OCRError ocrError){

        }
    }


    /**
     * 果蔬识别
     *
     * @param isDelete 是否删除临时图片
     * @param filePath
     * @param token
     * @param listener
     */
    public static void ingredient (final boolean isDelete, final String filePath, final String token, final OnIngredientResultListenerAi listener) {
        final File file = new File(filePath);

        RequestBody requestBody = RequestBody.create(MediaType.parse("multipart/form-data"), file);
        MultipartBody.Part part = MultipartBody.Part.createFormData("file", file.getName(), requestBody);
        request(IOcrApi.class, new NetWorkResult<DataResponse<String>>() {
            @Override
            public void onSuccess(DataResponse<String> data) {
                if(isDelete)
                    file.delete();
                listener.onIngredientLicense(GsonUtils.fromJson(data.getData(), ResultBean.class));
            }

            @Override
            public void onError(int err, String errMsg, Throwable t, DataResponse<String> data) {
                LogUtils.eTag("OCR识别失败：", err + "------" + errMsg);
                file.delete();
                OCRError ocrError = new OCRError();
                ocrError.setErrorCode(err);
                ocrError.setErrorMessage(errMsg);
                listener.onError(ocrError);
            }
        }, token).ingredient(part);
    }
    public interface OnIngredientResultListenerAi {
        void onIngredientLicense(ResultBean resultBean);
        default void onError(OCRError ocrError){

        }
    }
}
