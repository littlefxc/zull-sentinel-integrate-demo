package com.fengxuechao.examples.zuul;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.cloud.client.SpringCloudApplication;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @author fengxuechao
 * @version 0.1
 * @date 2019/11/22
 */
@EnableZuulProxy
@SpringCloudApplication
@EnableScheduling
@Slf4j
public class App {

    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }
}
