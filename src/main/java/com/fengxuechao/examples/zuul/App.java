package com.fengxuechao.examples.zuul;

import com.fengxuechao.examples.zuul.sentinel.properties.CustomSentinelProperties;
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

    @Autowired
    private CustomSentinelProperties customSentinelProperties;

    @Autowired
    private JedisCluster jedisCluster;

    /**
     * 热点参数限流规则
     */
    @Value("classpath:/sentinel/rules/parameter_flow.json")
    private Resource parameterFlow;

    /**
     * 网关流控规则
     */
    @Value("classpath:/sentinel/rules/gw_flow.json")
    private Resource gatewayFlow;

    /* 集群限流规则 */

    @Value("classpath:/sentinel/rules/cluster_parameter_flow.json")
    private Resource clusterParameterFlow;

    @Value("${spring.application.name}")
    private String namespace;

    /**
     * Callback used to run the bean.
     *
     * @param args incoming application arguments
     * @throws Exception on error
     */
    @Override
    public void run(ApplicationArguments args) throws Exception {
        {
            // 热点参数限流规则
            String json = IOUtils.toString(parameterFlow.getInputStream(), StandardCharsets.UTF_8);
            jedisCluster.set(customSentinelProperties.getParameterFlow().getKey(), json);
        }
        {
            // 网关流控限流规则
            String json = IOUtils.toString(gatewayFlow.getInputStream(), StandardCharsets.UTF_8);
            jedisCluster.set(customSentinelProperties.getGatewayFlow().getKey(), json);
        }
        {
            // 集群限流规则
            String json = IOUtils.toString(clusterParameterFlow.getInputStream(), StandardCharsets.UTF_8);
            jedisCluster.set(namespace, json);
        }
    }
}
