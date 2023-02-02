package com.grandtech.mapframe.core.layer.bean;
import java.util.List;
/**
 * @ClassName Satellite
 * @Description TODO
 * @Author: fs
 * @Date: 2021/6/7 9:06
 * @Version 2.0
 */
public class Satellite {

    private String type;
    private List<String> tiles;
    private int tileSize;
    public void setType(String type) {
        this.type = type;
    }
    public String getType() {
        return type;
    }

    public void setTiles(List<String> tiles) {
        this.tiles = tiles;
    }
    public List<String> getTiles() {
        return tiles;
    }

    public void setTileSize(int tileSize) {
        this.tileSize = tileSize;
    }
    public int getTileSize() {
        return tileSize;
    }

}