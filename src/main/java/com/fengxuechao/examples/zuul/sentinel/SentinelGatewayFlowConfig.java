package com.fengxuechao.examples.zuul.sentinel;

import com.alibaba.cloud.sentinel.datasource.converter.JsonConverter;
import com.alibaba.cloud.sentinel.zuul.handler.FallBackProviderHandler;
import com.alibaba.csp.sentinel.adapter.gateway.common.rule.GatewayRuleManager;
import com.alibaba.csp.sentinel.adapter.gateway.zuul.filters.SentinelZuulPreFilter;
import com.fengxuechao.examples.zuul.sentinel.datasource.JedisPullDataSource;
import com.fengxuechao.examples.zuul.sentinel.fallback.CustomBlockResponse;
import com.fengxuechao.examples.zuul.sentinel.fallback.CustomZuulBlockFallbackProvider;
import com.fengxuechao.examples.zuul.sentinel.properties.CustomSentinelProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
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
     * 自定义限流返回消息
     * 与业务的返回消息保持一致
     *
     * @return CustomZuulBlockFallbackProvider
     * @see CustomBlockResponse CustomBlockResponse 自定义 BlockResponse,重写 toString() 自定义返回消息
     * @see FallBackProviderHandler FallBackProviderHandler 实现了接口 SmartInitializingSingleton，故此利用 Spring Bean 生命周期原理将默认的 ZuulBlockFallbackProvider 替换为自定义的返回限流处理
     * @see SentinelZuulPreFilter SentinelZuulPreFilter 捕获 BlockException, 设置限流返回消息，也就是 CustomBlockResponse
     */
    @Bean
    public CustomZuulBlockFallbackProvider customZuulBlockFallbackProvider() {
        return new CustomZuulBlockFallbackProvider();
    }

    /**
     * bean 创建完成后执行
     * 1. 注册sentinel网关流控
     * 2. 注册sentinel集群流控
     *
     * @throws Exception
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        String gwFlowKey = customSentinelProperties.getGwFlowKey();
        String gwFlowChanel = customSentinelProperties.getGwFlowChanel();
        JedisPullDataSource redisDataSource = new JedisPullDataSource<>(jsonConverter, jedisCluster, gwFlowKey, gwFlowChanel);
        // 网关流控无法做到集群流控的功能，不适配我们现有的业务，应当需自定义
        GatewayRuleManager.register2Property(redisDataSource.getProperty());
    }
}
