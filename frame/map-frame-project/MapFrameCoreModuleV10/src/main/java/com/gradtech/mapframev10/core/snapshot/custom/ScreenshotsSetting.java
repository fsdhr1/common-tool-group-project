package com.gradtech.mapframev10.core.snapshot.custom;


import com.gradtech.mapframev10.core.rules.Rules;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @program: MapScreenshot
 * @description: 总坐落图生成的一些参数配置
 * @author: 冯帅
 * @create: 2022-03-18 08:25
 **/

public class ScreenshotsSetting implements Serializable , Rules {

    //默认0.6 截图级别的因数
    private double tileDensity = 0.6;



    //截图级别
    private int zoom = 16;

    //图片宽高压缩率 1为原宽高默认给0.8，可根据情况调整大小，可以减少图片的大小，同时降低质量
    private float imageScale=0.8f;

    //图片质量 1为原质量默认给1
    private float imageQuality=1.0f;

    ///是否绘制外框以及四至坐标点
    private boolean drawRect = false;
    //影像地址
    private String imageUrl;

    //其他需要填充的影像源
    private List<String> otherImageUrls = new ArrayList<>();

    private List<ScreenshotsFeatureStye> screenshotsFeatureStyes;

    private  List<String> waters;

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public List<String> getWaters() {
        return waters;
    }

    public void setWaters(List<String> waters) {
        this.waters = waters;
    }

    public List<String> getOtherImageUrls() {
        return otherImageUrls;
    }

    public void setOtherImageUrls(List<String> otherImageUrls) {
        this.otherImageUrls = otherImageUrls;
    }

    public boolean isDrawRect() {
        return drawRect;
    }

    public void setDrawRect(boolean drawRect) {
        this.drawRect = drawRect;
    }

    public float getImageScale() {
        return imageScale;
    }

    public void setImageScale(float imageScale) {
        this.imageScale = imageScale;
    }

    public float getImageQuality() {
        return imageQuality;
    }

    public void setImageQuality(float imageQuality) {
        this.imageQuality = imageQuality;
    }

    public int getZoom() {
        return zoom;
    }

    public void setZoom(int zoom) {
        this.zoom = zoom;
    }

    public List<ScreenshotsFeatureStye> getScreenshotsFeatureStyes() {
        return screenshotsFeatureStyes;
    }

    public void setScreenshotsFeatureStyes(List<ScreenshotsFeatureStye> screenshotsFeatureStyes) {
        this.screenshotsFeatureStyes = screenshotsFeatureStyes;
    }
    public double getTileDensity() {
        return tileDensity;
    }

    public void setTileDensity(double tileDensity) {
        this.tileDensity = tileDensity;
    }
    public void addScreenshotsFeatureStye(ScreenshotsFeatureStye screenshotsFeatureStye) {
        if(screenshotsFeatureStyes == null){
            screenshotsFeatureStyes = new ArrayList<>();
        }
        screenshotsFeatureStyes.add(screenshotsFeatureStye);
    }
    public static FeatureCollection getFeatureCollections(List<ScreenshotsFeatureStye> screenshotsFeatureStyes){
        if(screenshotsFeatureStyes == null) {
            return null;
        }
        List<Feature> features = new ArrayList<>();
        for (ScreenshotsFeatureStye screenshotsFeatureStye : screenshotsFeatureStyes) {
            features.add(screenshotsFeatureStye.getFeature());
        }
        return FeatureCollection.fromFeatures(features);
    }
}
