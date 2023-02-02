package com.gykj.autoupdate.BaseConfig;

public class Constants extends Constants_prod {
    /**
     * OSS配置
     */
    public static String OSS_ENDPOINT = "http://oss-cn-zhangjiakou.aliyuncs.com";//基地址如果在配置中没取到就取这个默认的
    public static String OSS_BUCKET_NAME = "ram-agriinsurance-prod";
    public static String OSS_ACCESSKEYID = "LTAIP816Jan0Nvjq";
    public static String OSS_PICBASEPATH = "{\"accessKeyId\":\"LTAIP816Jan0Nvjq\",\"bucket\":\"ram-agriinsurance-prod\",\"endpoint\":\"https://oss-cn-zhangjiakou.aliyuncs.com\"}";
    // OSS获取签名
    public final static String OSS_SIGN_URL = "http://api.gykj.com.cn/ossserver/alioss/getSignature/";

    // 检测版本url
    public static String CHECK_VERSION_URL = BASE_CHECK_URL + "agribigdata-server-sys/api/v1/autoupdate/version/checkVersion";

}
