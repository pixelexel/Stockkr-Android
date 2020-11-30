package com.akshay.stocks;

public class NewsItem {
    private String imageUrl;
    private String website;
    private String title;


    public NewsItem(String imageUrl, String website, String title) {
        this.imageUrl = imageUrl;
        this.website = website;
        this.title = title;
    }

    public String getImageUrl(){
        return imageUrl;
    }

    public String getTitle() {
        return title;
    }

    public String getWebsite() {
        return website;
    }
}
