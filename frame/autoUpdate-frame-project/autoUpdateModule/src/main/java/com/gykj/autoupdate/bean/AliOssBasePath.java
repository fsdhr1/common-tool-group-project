package com.gykj.autoupdate.bean;

import java.io.Serializable;

/**
 * 阿里云OSS基地址
 */
public class AliOssBasePath implements Serializable {

    /**
     * accessKeyId : LTAIP816Jan0Nvjq
     * bucket : ram-public
     * endpoint : http://oss-cn-zhangjiakou.aliyuncs.com
     */

    private String accessKeyId;
    private String bucket;
    private String endpoint;

    public String getAccessKeyId() {
        return accessKeyId;
    }

    public void setAccessKeyId(String accessKeyId) {
        this.accessKeyId = accessKeyId;
    }

    public String getBucket() {
        return bucket;
    }

    public void setBucket(String bucket) {
        this.bucket = bucket;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

}
