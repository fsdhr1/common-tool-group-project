package com.gykj.autoupdate.oss;

import android.text.TextUtils;

import java.io.Serializable;

/**
 * Created by Administrator on 2019-07-04.
 * {"accessKeyId":"LTAIP816Jan0Nvjq","bucket":"ram-insurance","endpoint":"https://oss-cn-zhangjiakou.aliyuncs.com"}
 */

public class BasePathMsg implements Serializable {

    private String accessKeyId;

    private String bucket;

    private String endpoint;

    public BasePathMsg(){}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BasePathMsg)) return false;
        BasePathMsg that = (BasePathMsg) o;
        return equalsStr(getAccessKeyId(), that.getAccessKeyId()) &&
                equalsStr(getBucket(), that.getBucket()) &&
                equalsStr(getEndpoint(), that.getEndpoint());
    }

    private boolean equalsStr(String str1, String str2){
        if(TextUtils.isEmpty(str1) && TextUtils.isEmpty(str2)){
            return true;
        }
        if(!TextUtils.isEmpty(str1) && str1.equals(str2)){
            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + (accessKeyId == null ? 0 : accessKeyId.hashCode());
        result = 31 * result + (bucket == null ? 0 : bucket.hashCode());
        result = 31 * result + (endpoint== null ? 0 : endpoint.hashCode());
        return result;
    }

    public BasePathMsg(String accessKeyId, String bucket, String endpoint) {
        this.accessKeyId = accessKeyId;
        this.bucket = bucket;
        this.endpoint = endpoint;
    }

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

    @Override
    public String toString() {
        return "BasePathMsg{" +
                "accessKeyId='" + accessKeyId + '\'' +
                ", bucket='" + bucket + '\'' +
                ", endpoint='" + endpoint + '\'' +
                '}';
    }
}
