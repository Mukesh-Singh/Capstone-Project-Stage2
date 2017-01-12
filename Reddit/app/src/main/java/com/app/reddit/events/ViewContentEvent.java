package com.app.reddit.events;

public class ViewContentEvent {

    private final String contentTitle;
    private final String url;

    public ViewContentEvent(String contentTitle, String url) {
        this.contentTitle = contentTitle;
        this.url = url;
    }

    public String getContentTitle() {
        return contentTitle;
    }

    public String getUrl() {
        return url;
    }
}
