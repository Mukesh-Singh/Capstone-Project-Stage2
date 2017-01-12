package com.app.reddit.events;

public class ShareContentEvent {

    private final String content;
    private final String mimeType;

    public ShareContentEvent(String content) {
        this.content = content;
        this.mimeType = "text/plain";
    }

    public String getContent() {
        return content;
    }

    public String getMimeType() {
        return mimeType;
    }
}
