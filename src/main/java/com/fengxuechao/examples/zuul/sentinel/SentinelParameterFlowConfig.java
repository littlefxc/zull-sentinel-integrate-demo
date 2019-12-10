package com.fengxuechao.examples.zuul.sentinel;

import com.alibaba.cloud.sentinel.datasource.converter.JsonConverter;
import com.alibaba.cloud.sentinel.zuul.handler.FallBackProviderHandler;
import com.alibaba.csp.sentinel.adapter.gateway.zuul.filters.SentinelZuulPreFilter;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowRuleManager;
import com.fengxuechao.examples.zuul.sentinel.constants.CustomSentinelConstants;
import com.fengxuechao.examples.zuul.sentinel.datasource.JedisPullDataSource;
import com.fengxuechao.examples.zuul.sentinel.fallback.CustomBlockResponse;
import com.fengxuechao.examples.zuul.sentinel.fallback.CustomZuulBlockFallbackProvider;
import com.fengxuechao.examples.zuul.sentinel.filter.SentinelParameterFlowZuulPreFilter;
import com.fengxuechao.examples.zuul.sentinel.properties.CustomSentinelProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import redis.clients.jedis.JedisCluster;

import java.nio.charset.StandardCharsets;

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
@EnableConfigurationProperties({CustomSentinelProperties.class})
public class SentinelParameterFlowConfig implements InitializingBean, ApplicationRunner {

    @Autowired
    @Qualifier("sentinel-json-param-flow-converter")
    private JsonConverter jsonConverter;

    @Autowired
    private JedisCluster jedisCluster;

    @Autowired
    private CustomSentinelProperties customSentinelProperties;


    @Bean
    public SentinelParameterFlowZuulPreFilter sentinelParameterFlowZuulPreFilter() {
        return new SentinelParameterFlowZuulPreFilter(FILTER_ORDER_SENTINEL_PARAMETER_FLOW);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        String parameterFlowKey = customSentinelProperties.getParameterFlow().getKey();
        JedisPullDataSource redisDataSource = new JedisPullDataSource<>(jsonConverter, jedisCluster, parameterFlowKey);
        ParamFlowRuleManager.register2Property(redisDataSource.getProperty());
    }

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
    @ConditionalOnMissingBean(CustomZuulBlockFallbackProvider.class)
    public CustomZuulBlockFallbackProvider customZuulBlockFallbackProvider() {
        return new CustomZuulBlockFallbackProvider();
    }

    @Bean
    @ConditionalOnMissingBean(FallBackProviderHandler.class)
    public FallBackProviderHandler fallBackProviderHandler(
            DefaultListableBeanFactory beanFactory) {
        return new FallBackProviderHandler(beanFactory);
    }

    /* 仅供测试，缓存规则 */

    @Value("classpath:/sentinel/rules/parameter_flow.json")
    private Resource resource;

    /**
     * Callback used to run the bean.
     *
     * @param args incoming application arguments
     * @throws Exception on error
     */
    @Override
    public void run(ApplicationArguments args) throws Exception {
        // 热点参数限流规则
        String json = IOUtils.toString(resource.getInputStream(), StandardCharsets.UTF_8);
        jedisCluster.set(customSentinelProperties.getParameterFlow().getKey(), json);
    }
}
