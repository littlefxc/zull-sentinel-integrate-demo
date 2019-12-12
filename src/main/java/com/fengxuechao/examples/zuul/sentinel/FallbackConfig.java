package com.fengxuechao.examples.zuul.sentinel;

import com.alibaba.cloud.sentinel.zuul.handler.FallBackProviderHandler;
import com.alibaba.csp.sentinel.adapter.gateway.zuul.filters.SentinelZuulPreFilter;
import com.fengxuechao.examples.zuul.sentinel.fallback.CustomBlockResponse;
import com.fengxuechao.examples.zuul.sentinel.fallback.CustomZuulBlockFallbackProvider;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 自定义限流配置类
 *
 * @author fengxuechao
 * @version 0.1
 * @date 2019/12/10
 */
@Configuration
public class FallbackConfig {

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
}
