package com.acme.leavemgmt.model;

import java.time.LocalDateTime;

public class Notification {
    private int id, userId;
    private String title, body, linkUrl;
    private boolean read;
    private LocalDateTime createdAt;

    // getters/setters
    public int getId() { return id; }
    public void setId(int id){ this.id=id; }
    public int getUserId(){ return userId; }
    public void setUserId(int userId){ this.userId=userId; }
    public String getTitle(){ return title; }
    public void setTitle(String title){ this.title=title; }
    public String getBody(){ return body; }
    public void setBody(String body){ this.body=body; }
    public String getLinkUrl(){ return linkUrl; }
    public void setLinkUrl(String linkUrl){ this.linkUrl=linkUrl; }
    public boolean isRead(){ return read; }
    public void setRead(boolean read){ this.read=read; }
    public LocalDateTime getCreatedAt(){ return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt){ this.createdAt=createdAt; }
}
