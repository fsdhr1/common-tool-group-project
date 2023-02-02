package com.gykj.cameramodule.bean;

import java.io.Serializable;

/**
 * Description：
 * Author：LiuYM
 * Date： 2017-04-22 15:16
 */

public class ImagePagerBean implements Serializable, Cloneable {
    private String mUrl;
    private String mDesc;
    private String mSmallUrl;
    private static ImagePagerBean imagePagerBean = new ImagePagerBean();

    public ImagePagerBean() {
    }

    private String name;

    public ImagePagerBean(String url, String desc, String smallUrl, String name) {
        mUrl = url;
        mDesc = desc;
        this.name = name;
        this.mSmallUrl = smallUrl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ImagePagerBean(String url, String desc, String smallUrl) {
        mUrl = url;
        mDesc = desc;
        this.mSmallUrl = smallUrl;
    }

    public static ImagePagerBean getOneImagePagerBean(String url, String desc, String smallUrl) {
        try {
            ImagePagerBean imagePagerBeanN = (ImagePagerBean) imagePagerBean.clone();
            imagePagerBeanN.setUrl(url);
            imagePagerBeanN.setSmallUrl(smallUrl);
            imagePagerBeanN.setDesc(desc);
            return imagePagerBeanN;
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
            return new ImagePagerBean(url, desc, smallUrl);
        }

    }

    public String getSmallUrl() {
        return mSmallUrl;
    }

    public void setSmallUrl(String smallUrl) {
        mSmallUrl = smallUrl;
    }

    public String getUrl() {
        return mUrl;
    }

    public void setUrl(String url) {
        mUrl = url;
    }

    public String getDesc() {
        return mDesc;
    }

    public void setDesc(String desc) {
        mDesc = desc;
    }
}
