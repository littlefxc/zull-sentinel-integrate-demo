package com.fengxuechao.examples.zuul.sentinel;

import com.alibaba.cloud.sentinel.datasource.converter.JsonConverter;
import com.alibaba.csp.sentinel.adapter.gateway.common.rule.GatewayRuleManager;
import com.fengxuechao.examples.zuul.sentinel.constants.CustomSentinelConstants;
import com.fengxuechao.examples.zuul.sentinel.datasource.JedisPullDataSource;
import com.fengxuechao.examples.zuul.sentinel.properties.CustomSentinelProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.JedisCluster;


/**
 * 流量控制配置类
 *
 * @author fengxuechao
 * @version 0.1
 * @date 2019/11/15
 * @see <a href="https://github.com/alibaba/Sentinel/wiki/%E7%BD%91%E5%85%B3%E9%99%90%E6%B5%81">网关限流</a>
 * @see <a href="https://github.com/alibaba/Sentinel/wiki/%E5%8A%A8%E6%80%81%E8%A7%84%E5%88%99%E6%89%A9%E5%B1%95">动态规则扩展</a>
 */
@Slf4j
@Configuration
@ConditionalOnProperty(prefix = CustomSentinelConstants.PREFIX_GATEWAY_FLOW, name = "enabled", havingValue = "true")
@EnableConfigurationProperties({CustomSentinelProperties.class})
public class SentinelGatewayFlowConfig implements InitializingBean {

    @Autowired
    @Qualifier("sentinel-json-gw-flow-converter")
    private JsonConverter jsonConverter;

    @Autowired
    private JedisCluster jedisCluster;

    @Autowired
    private CustomSentinelProperties customSentinelProperties;

    /**
     * bean 创建完成后执行
     * 1. 注册sentinel网关流控, 但是网关流控不能应用集群流控
     *
     * @throws Exception
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        String gwFlowKey = customSentinelProperties.getGatewayFlow().getKey();
        String gwFlowChanel = customSentinelProperties.getGatewayFlow().getChanel();
        JedisPullDataSource redisDataSource = new JedisPullDataSource<>(jsonConverter, jedisCluster, gwFlowKey);
        // 网关流控无法做到集群流控的功能，不适配我们现有的业务，应当需自定义
        GatewayRuleManager.register2Property(redisDataSource.getProperty());
    }
}
