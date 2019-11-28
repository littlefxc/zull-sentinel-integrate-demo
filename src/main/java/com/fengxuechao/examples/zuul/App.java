package com.fengxuechao.examples.zuul;

import com.fengxuechao.examples.zuul.sentinel.CustomSentinelProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.cloud.client.SpringCloudApplication;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;
import org.springframework.core.io.Resource;
import org.springframework.scheduling.annotation.EnableScheduling;
import redis.clients.jedis.JedisCluster;

import java.nio.charset.StandardCharsets;

/**
 * @author fengxuechao
 * @version 0.1
 * @date 2019/11/22
 */
@EnableZuulProxy
@SpringCloudApplication
@EnableScheduling
@Slf4j
public class App implements ApplicationRunner {

    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }

    @Value("classpath:/sentinel/rules/gw_flow.json")
    private Resource resource;

    @Autowired
    private JedisCluster jedisCluster;

    @Autowired
    private CustomSentinelProperties customSentinelProperties;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        String json = IOUtils.toString(resource.getInputStream(), StandardCharsets.UTF_8);
        jedisCluster.set(customSentinelProperties.getGwFlowKey(), json);
    }
}
