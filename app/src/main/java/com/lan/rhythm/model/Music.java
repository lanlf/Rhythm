package com.lan.rhythm.model;

/**
 * Created by lan on 2016/7/19.
 */
public class Music {
    private String url;
    private long id;
    private String title;
    private String artist;
    private long duration;
    private long size;

    public String getArtist() {
        return artist;
    }

    public long getDuration() {
        return duration;
    }

    public long getId() {
        return id;
    }

    public long getSize() {
        return size;
    }

    public String getTitle() {
        return title;
    }

    public String getUrl() {
        return url;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
