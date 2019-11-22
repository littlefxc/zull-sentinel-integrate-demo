package com.fengxuechao.examples.zuul.config;

import com.ecwid.consul.v1.ConsulClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.consul.ConditionalOnConsulEnabled;
import org.springframework.cloud.consul.discovery.ConsulDiscoveryProperties;
import org.springframework.cloud.consul.discovery.HeartbeatProperties;
import org.springframework.cloud.consul.discovery.TtlScheduler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author fengxuechao
 */
@Configuration
@ConditionalOnConsulEnabled
public class ConsulConfig {

    @Autowired(required = false)
    private TtlScheduler ttlScheduler;

    /**
     * 重写register方法
     * @param consulClient
     * @param properties
     * @param heartbeatProperties
     * @return
     */
    @Bean
    public ServiceIdRegister consulServiceRegistry(ConsulClient consulClient, ConsulDiscoveryProperties properties,
                                                   HeartbeatProperties heartbeatProperties) {
        return new ServiceIdRegister(consulClient, properties, ttlScheduler, heartbeatProperties);
    }
}