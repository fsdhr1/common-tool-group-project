package com.gykj.autoupdate.bean;



import java.io.Serializable;

/**
 * @Description
 * @Author ren
 * @Date 2019-08-26
 */

public class SysVersion implements Serializable {

    /**
     * 应用唯一标识key
     */
    private String applicationKey;

    /**
     * 应用名称
     */
    private String applicationName;

    /**
     * 版本id
     */
    private String versionId;

    /**
     * versionCode
     */
    private Long versionCode;

    /**
     * versionName
     */
    private String versionName;

    /**
     * 更新内容
     */
    private String versionTip;

    /**
     * 更新方式 0默认更新 /1强制更新
     */
    private Long updateType;

    /**
     * version状态 0正常更新 / 1 停止更新
     */
    private Long versionState;

    /**
     * 版本地址
     */
    private String versionPath;

    /**
     * 创建时间
     */
    private String gmtCreate;

    /**
     * 修改时间
     */
    private String gmtModify;

    /**
     * 结束时间
     */
    private String gmtFinish;

    /**
     * 热修复版本
     */
    private int hotfixVersion;

    public int getHotfixVersion() {
        return hotfixVersion;
    }

    public void setHotfixVersion(int mHotfixVersion) {
        hotfixVersion = mHotfixVersion;
    }

    public String getApplicationKey() {
        return this.applicationKey;
    }

    public void setApplicationKey(String applicationKey) {
        this.applicationKey = applicationKey;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public String getVersionId() {
        return this.versionId;
    }

    public void setVersionId(String versionId) {
        this.versionId = versionId;
    }

    public Long getVersionCode() {
        return this.versionCode;
    }

    public void setVersionCode(Long versionCode) {
        this.versionCode = versionCode;
    }

    public String getVersionName() {
        return this.versionName;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }

    public String getVersionTip() {
        return this.versionTip;
    }

    public void setVersionTip(String versionTip) {
        this.versionTip = versionTip;
    }

    public Long getUpdateType() {
        return this.updateType;
    }

    public void setUpdateType(Long updateType) {
        this.updateType = updateType;
    }

    public Long getVersionState() {
        return this.versionState;
    }

    public void setVersionState(Long versionState) {
        this.versionState = versionState;
    }

    public String getVersionPath() {
        return this.versionPath;
    }

    public void setVersionPath(String versionPath) {
        this.versionPath = versionPath;
    }

    public String getGmtCreate() {
        return this.gmtCreate;
    }

    public void setGmtCreate(String gmtCreate) {
        this.gmtCreate = gmtCreate;
    }

    public String getGmtModify() {
        return this.gmtModify;
    }

    public void setGmtModify(String gmtModify) {
        this.gmtModify = gmtModify;
    }

    public String getGmtFinish() {
        return this.gmtFinish;
    }

    public void setGmtFinish(String gmtFinish) {
        this.gmtFinish = gmtFinish;
    }

    @Override
    public String toString() {
        return "SysVersion{" +
                "applicationKey='" + applicationKey + '\'' +
                ", applicationName='" + applicationName + '\'' +
                ", versionId='" + versionId + '\'' +
                ", versionCode=" + versionCode +
                ", versionName='" + versionName + '\'' +
                ", versionTip='" + versionTip + '\'' +
                ", updateType=" + updateType +
                ", versionState=" + versionState +
                ", versionPath='" + versionPath + '\'' +
                ", gmtCreate='" + gmtCreate + '\'' +
                ", gmtModify='" + gmtModify + '\'' +
                ", gmtFinish='" + gmtFinish + '\'' +
                ", hotfixVersion='" + hotfixVersion + '\'' +
                '}';
    }
}
