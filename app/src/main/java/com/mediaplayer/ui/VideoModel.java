package com.mediaplayer.ui;

public class VideoModel {
    private  String url;
    private  int mediaid;
    private  String name;
    private boolean islandscape;

    public VideoModel(String url, int mediaid, String name,boolean islandscape) {
        this.url = url;
        this.mediaid = mediaid;
        this.name = name;
        this.islandscape=islandscape;
    }


    public boolean islandscape() {
        return islandscape;
    }

    public String getUrl() {
        return url;
    }

    public int getMediaid() {
        return mediaid;
    }

    public String getName() {
        return name;
    }
}
