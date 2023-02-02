package com.gykj.ossmodule.bean;

/**
 * Created by ren on 2021/4/8
 */
public class OssFileBean {
    private String endpoint;
    private String bucketName;
    private String accessKeyId;
    // oss文件存放路径
    private String key;

    public OssFileBean(String endpoint, String bucketName, String accessKeyId) {
        this.endpoint = endpoint;
        this.bucketName = bucketName;
        this.accessKeyId = accessKeyId;
    }

    public OssFileBean(String endpoint, String bucketName, String accessKeyId, String key) {
        this.endpoint = endpoint;
        this.bucketName = bucketName;
        this.accessKeyId = accessKeyId;
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getBucketName() {
        return bucketName;
    }

    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }

    public String getAccessKeyId() {
        return accessKeyId;
    }

    public void setAccessKeyId(String accessKeyId) {
        this.accessKeyId = accessKeyId;
    }


}
