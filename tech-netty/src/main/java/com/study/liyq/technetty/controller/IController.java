package com.study.liyq.technetty.controller;


import com.study.liyq.technetty.model.HttpRequest;
import com.study.liyq.technetty.model.Response;

public interface IController<T> {

    Response<T> handleRequest(HttpRequest request);
}
