package com.study.liyq.technetty.controller;

import com.study.liyq.technetty.annotation.HttpHandler;
import com.study.liyq.technetty.model.HttpRequest;
import com.study.liyq.technetty.model.Response;

@HttpHandler(path="/")
public class HelloController implements IController{
    @Override
    public Response handleRequest(HttpRequest request) {
        Response<String> sResponse = new Response<>(0, "OK", "hello welcome to visit my netty-server");
        return sResponse;

    }
}
