package com.study.liyq.technetty.model;

import com.google.gson.GsonBuilder;

public class Response<T> {
    private int code;
    private String message;
    private long timestamp;
    private T data;


    public Response(int code, String message, T data)
    {
        this.code = code;
        this.message = message;
        this.data = data;
        this.timestamp = System.currentTimeMillis();
    }
    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String toJSONString(){
        return new GsonBuilder().create().toJson(this);
    }
}
