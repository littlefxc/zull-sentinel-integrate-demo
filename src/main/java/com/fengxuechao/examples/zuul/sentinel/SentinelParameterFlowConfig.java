package com.fengxuechao.examples.zuul.sentinel;

import com.alibaba.cloud.sentinel.datasource.converter.JsonConverter;
import com.alibaba.cloud.sentinel.zuul.SentinelZuulProperties;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowRuleManager;
import com.fengxuechao.examples.zuul.sentinel.constants.CustomSentinelConstants;
import com.fengxuechao.examples.zuul.sentinel.datasource.JedisPullDataSource;
import com.fengxuechao.examples.zuul.sentinel.filter.SentinelParameterFlowZuulPreFilter;
import com.fengxuechao.examples.zuul.sentinel.filter.SentinelZuulErrorFilter;
import com.fengxuechao.examples.zuul.sentinel.filter.SentinelZuulPostFilter;
import com.fengxuechao.examples.zuul.sentinel.properties.CustomSentinelProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.JedisCluster;

import static com.fengxuechao.examples.zuul.sentinel.constants.CustomSentinelConstants.FILTER_ORDER_SENTINEL_PARAMETER_FLOW;

/**
 * This pre-filter will regard all {@code proxyId} and all customized API as resources.
 * When a BlockException caught, the filter will try to find a fallback to execute.
 *
 * @author fengxuechao
 * @version 0.1
 * @date 2019/12/10
 */
@Slf4j
@Configuration
@ConditionalOnProperty(prefix = CustomSentinelConstants.PREFIX_PARAMETER_FLOW, name = "enabled", havingValue = "true")
@EnableConfigurationProperties({CustomSentinelProperties.class, SentinelZuulProperties.class})
public class SentinelParameterFlowConfig implements InitializingBean {

    @Autowired
    @Qualifier("sentinel-json-param-flow-converter")
    private JsonConverter jsonConverter;

    @Autowired
    private JedisCluster jedisCluster;

    @Autowired
    private CustomSentinelProperties customSentinelProperties;

    @Autowired
    private SentinelZuulProperties zuulProperties;

    @Bean
    public SentinelParameterFlowZuulPreFilter sentinelParameterFlowZuulPreFilter() {
        return new SentinelParameterFlowZuulPreFilter(FILTER_ORDER_SENTINEL_PARAMETER_FLOW);
    }

    @Bean
    @ConditionalOnMissingBean
    public SentinelZuulPostFilter sentinelZuulPostFilter() {
        log.info("[Sentinel Zuul] register SentinelZuulPostFilter {}",
                zuulProperties.getOrder().getPost());
        return new SentinelZuulPostFilter(zuulProperties.getOrder().getPost());
    }

    @Bean
    @ConditionalOnMissingBean
    public SentinelZuulErrorFilter sentinelZuulErrorFilter() {
        log.info("[Sentinel Zuul] register SentinelZuulErrorFilter {}",
                zuulProperties.getOrder().getError());
        return new SentinelZuulErrorFilter(zuulProperties.getOrder().getError());
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        String parameterFlowKey = customSentinelProperties.getParameterFlow().getKey();
        JedisPullDataSource redisDataSource = new JedisPullDataSource<>(jsonConverter, jedisCluster, parameterFlowKey);
        ParamFlowRuleManager.register2Property(redisDataSource.getProperty());
    }
}
