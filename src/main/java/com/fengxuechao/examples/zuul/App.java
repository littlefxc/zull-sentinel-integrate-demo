package com.fengxuechao.examples.zuul;

import org.springframework.boot.SpringApplication;
import org.springframework.cloud.client.SpringCloudApplication;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;

/**
 * @author fengxuechao
 * @version 0.1
 * @date 2019/11/22
 */
@EnableZuulProxy
@SpringCloudApplication
public class App {

    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }
}
