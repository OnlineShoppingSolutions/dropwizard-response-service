package com.onlineshoppingsolutions.cache.service.echo;

public class ResponseMessage {
    private boolean isCacheableHeaderSet;
    private int status;
    private String message;

    public ResponseMessage() {
    }

    public ResponseMessage(boolean isCacheableHeaderSet, int status) {
        this.isCacheableHeaderSet = isCacheableHeaderSet;
        this.status = status;
    }

    public ResponseMessage(boolean isCacheableHeaderSet, int status, String message) {
        this.isCacheableHeaderSet = isCacheableHeaderSet;
        this.status = status;
        this.message = message;
    }

    public boolean isCacheableHeaderSet() {
        return isCacheableHeaderSet;
    }

    public void setCacheableHeaderSet(boolean isCacheableHeaderSet) {
        this.isCacheableHeaderSet = isCacheableHeaderSet;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
