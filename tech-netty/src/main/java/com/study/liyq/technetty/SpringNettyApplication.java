package com.study.liyq.technetty;

import com.study.liyq.technetty.annotation.HttpHandler;
import com.study.liyq.technetty.controller.IController;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(includeFilters = @ComponentScan.Filter(HttpHandler.class))
public class SpringNettyApplication {

    public static void main(String[] args){
        //remove web container from nested spring boot
        new SpringApplicationBuilder(SpringNettyApplication.class).web(WebApplicationType.NONE).run(args);
    }

}
