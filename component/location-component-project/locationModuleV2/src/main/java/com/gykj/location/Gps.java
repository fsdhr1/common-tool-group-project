package com.gykj.location;

public class Gps {

    private double wgLat;

    private double wgLon;

    public Gps(double lat, double lon) {
        wgLat = lat;
        wgLon = lon;
    }

    public double getWgLat() {
        return wgLat;
    }

    public void setWgLat(double wgLat) {
        this.wgLat = wgLat;
    }

    public double getWgLon() {
        return wgLon;
    }

    public void setWgLon(double wgLon) {
        this.wgLon = wgLon;
    }
}
