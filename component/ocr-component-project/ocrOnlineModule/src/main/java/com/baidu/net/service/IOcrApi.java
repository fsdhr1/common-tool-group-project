package com.baidu.net.service;

import com.baidu.net.bean.DataResponse;

import io.reactivex.Observable;
import okhttp3.MultipartBody;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;

/**
 * Created by jyh on 2019-08-06.
 */

public interface IOcrApi {

    /**
     * ocr - 识别身份证
     *
     * @param file
     * @param contentType 正反面标识front or back
     * @return
     */
    @Multipart
    @POST("api/v1/ocr/idCard")
    Observable<DataResponse<String>> idCard(@Part MultipartBody.Part file, @Query("contentType") String contentType);

    /**
     * ocr - 识别银行卡
     *
     * @param file
     * @return
     */
    @Multipart
    @POST("api/v1/ocr/bankCard")
    Observable<DataResponse<String>> bankCard(@Part MultipartBody.Part file);

    /**
     * ocr - 识别营业执照
     *
     * @param file
     * @return
     */
    @Multipart
    @POST("api/v1/ocr/businessLicense")
    Observable<DataResponse<String>> businessLicense(@Part MultipartBody.Part file);


    /**
     * img - 动物识别
     *
     * @param file
     * @return
     */
    @Multipart
    @POST("api/v1/img/animalDetect")
    Observable<DataResponse<String>> animalDetect(@Part MultipartBody.Part file);


    /**
     * img - 果蔬识别
     *
     * @param file
     * @return
     */
    @Multipart
    @POST("api/v1/img/ingredient")
    Observable<DataResponse<String>> ingredient(@Part MultipartBody.Part file);

    /**
     * img - 植物识别
     *
     * @param file
     * @return
     */
    @Multipart
    @POST("api/v1/img/plantDetect")
    Observable<DataResponse<String>> plantDetect(@Part MultipartBody.Part file);
}
