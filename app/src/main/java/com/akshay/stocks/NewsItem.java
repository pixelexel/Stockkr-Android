package com.akshay.stocks;

public class NewsItem {
    private String imageUrl;
    private String website;
    private String title;
    private String url;
    private String time;


    public NewsItem(String imageUrl, String website, String title, String url, String time) {
        this.imageUrl = imageUrl;
        this.website = website;
        this.title = title;
        this.url = url;
        this.time = time;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getTitle() {
        return title;
    }

    public String getWebsite() {
        return website;
    }

    public String getUrl() {
        return url;
    }

    public String getTime() {
        return time;
    }
}
