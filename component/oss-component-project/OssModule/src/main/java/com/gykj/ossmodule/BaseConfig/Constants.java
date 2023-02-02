package com.gykj.ossmodule.BaseConfig;

public class Constants extends Constants_prod {
    public static String OSS_ENDPOINT = "http://oss-cn-zhangjiakou.aliyuncs.com";//基地址如果在配置中没取到就取这个默认的
    public static String OSS_BUCKET_NAME = "ram-agriinsurance-test";
    public static String OSS_ACCESSKEYID = "LTAIP816Jan0Nvjq";
    public static String OSS_PICBASEPATH = "{\"accessKeyId\":\"LTAIP816Jan0Nvjq\",\"bucket\":\"ram-agriinsurance-prod\",\"endpoint\":\"https://oss-cn-zhangjiakou.aliyuncs.com\"}";

    public static final String BASE_URL = "http://api.gykj.com.cn/";
    // 获取token
    public final static String GET_TOKEN = BASE_URL + "api-auth/api/v1/manager/auth/getSignature";
    // OSS获取签名
    public final static String OSS_GET_SIGNATURE = BASE_URL + "ossserver/alioss/getSignature/";



}
