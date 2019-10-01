package com.study.liyq.techmemcached;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties
public class TechMemcachedApplication {

	public static void main(String[] args) {
		SpringApplication.run(TechMemcachedApplication.class, args);
	}

}
